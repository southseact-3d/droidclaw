package com.agentapp.ui.skills

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agentapp.data.db.SkillDao
import com.agentapp.data.models.Skill
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.UUID
import javax.inject.Inject

data class SkillsUiState(
    val skills: List<Skill> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showAddDialog: Boolean = false
)

@HiltViewModel
class SkillsViewModel @Inject constructor(
    private val skillDao: SkillDao
) : ViewModel() {

    private val _state = MutableStateFlow(SkillsUiState())
    val state: StateFlow<SkillsUiState> = _state.asStateFlow()

    private val httpClient = OkHttpClient()

    init {
        viewModelScope.launch {
            skillDao.getAllSkills().collect { skills ->
                _state.update { it.copy(skills = skills) }
            }
        }
    }

    fun toggleSkill(skill: Skill) {
        viewModelScope.launch {
            skillDao.setEnabled(skill.id, !skill.enabled)
        }
    }

    fun deleteSkill(skillId: String) {
        viewModelScope.launch { skillDao.delete(skillId) }
    }

    fun installFromUrl(url: String) {
        if (url.isBlank()) return
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val request = Request.Builder().url(url).build()
                val response = httpClient.newCall(request).execute()
                if (!response.isSuccessful) throw Exception("HTTP ${response.code}")
                val content = response.body?.string() ?: throw Exception("Empty response")

                // Parse SKILL.md front-matter
                val name = parseFrontMatter(content, "name") ?: url.substringAfterLast("/")
                val description = parseFrontMatter(content, "description") ?: ""

                val skill = Skill(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    description = description,
                    markdownContent = content,
                    sourceUrl = url
                )
                skillDao.insert(skill)
                _state.update { it.copy(isLoading = false, showAddDialog = false) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = "Install failed: ${e.message}") }
            }
        }
    }

    fun createManualSkill(name: String, description: String, content: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val skill = Skill(
                id = UUID.randomUUID().toString(),
                name = name,
                description = description,
                markdownContent = content
            )
            skillDao.insert(skill)
            _state.update { it.copy(showAddDialog = false) }
        }
    }

    fun showAddDialog() = _state.update { it.copy(showAddDialog = true) }
    fun hideAddDialog() = _state.update { it.copy(showAddDialog = false, error = null) }
    fun dismissError() = _state.update { it.copy(error = null) }

    private fun parseFrontMatter(content: String, key: String): String? {
        val pattern = Regex("^$key:\\s*(.+)$", RegexOption.MULTILINE)
        return pattern.find(content)?.groupValues?.get(1)?.trim()
    }
}
