package com.agentapp.ui.skills

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agentapp.data.db.SkillDao
import com.agentapp.data.db.MpcDao
import com.agentapp.data.models.*
import com.agentapp.providers.MpcClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.UUID
import javax.inject.Inject

data class SkillsUiState(
    val skills: List<Skill> = emptyList(),
    val mpcTools: List<Pair<MpcTool, MpcServer>> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshingTools: Boolean = false,
    val error: String? = null,
    val showAddDialog: Boolean = false
)

@HiltViewModel
class SkillsViewModel @Inject constructor(
    private val skillDao: SkillDao,
    private val mpcDao: MpcDao,
    private val mpcClient: MpcClient
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
    viewModelScope.launch {
      mpcDao.getEnabledServers().collect { servers ->
        servers.forEach { server ->
          loadToolsFromServer(server)
        }
      }
    }
    // Combine tools and servers for display
    viewModelScope.launch {
      combine(
        mpcDao.getEnabledServers(),
        mpcDao.getServers()
      ) { enabledServers, allServers ->
        val enabledIds = enabledServers.map { it.id }
        allServers.filter { it.id in enabledIds }
      }.flatMapConcat { servers ->
        if (servers.isEmpty()) {
          flowOf(emptyList())
        } else {
          mpcDao.getToolsForServers(servers.map { it.id }).map { tools ->
            tools.mapNotNull { tool ->
              val server = servers.find { it.id == tool.serverId }
              server?.let { tool to it }
            }
          }
        }
      }.collect { toolsWithServers ->
        _state.update { it.copy(mpcTools = toolsWithServers) }
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
                val (name, description, toolDefs) = parseFrontMatter(content)

                val skill = Skill(
                    id = UUID.randomUUID().toString(),
                    name = name ?: url.substringAfterLast("/"),
                    description = description ?: "",
                    markdownContent = content,
                    toolDefinitions = toolDefs ?: "[]",
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
                markdownContent = content,
                toolDefinitions = "[]"
            )
            skillDao.insert(skill)
            _state.update { it.copy(showAddDialog = false) }
        }
    }

    fun refreshMpcTools() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshingTools = true) }
            mpcDao.getEnabledServers().first().forEach { server ->
                loadToolsFromServer(server)
            }
            _state.update { it.copy(isRefreshingTools = false) }
        }
    }

    private suspend fun loadToolsFromServer(server: MpcServer) {
        try {
            val tools = mpcClient.listTools(server)
            mpcDao.deleteToolsForServer(server.id)
            tools.forEach { tool ->
                mpcDao.insertTool(tool.copy(serverId = server.id))
            }
        } catch (e: Exception) {
            // Server unreachable - remove cached tools
            mpcDao.deleteToolsForServer(server.id)
        }
    }

    fun showAddDialog() = _state.update { it.copy(showAddDialog = true) }
    fun hideAddDialog() = _state.update { it.copy(showAddDialog = false, error = null) }
    fun dismissError() = _state.update { it.copy(error = null) }

    private fun parseFrontMatter(content: String): Triple<String?, String?, String?> {
        val lines = content.lines()
        if (lines.size < 3 || lines[0] != "---") {
            return Triple(null, null, null)
        }
        var name: String? = null
        var description: String? = null
        var toolDefs: String? = null
        var inFrontMatter = true

        for (i in 1 until lines.size) {
            if (lines[i] == "---") {
                inFrontMatter = false
                break
            }
            val colon = lines[i].indexOf(":")
            if (colon > 0) {
                val key = lines[i].substring(0, colon)
                val value = lines[i].substring(colon + 1).trim()
                when (key) {
                    "name" -> name = value
                    "description" -> description = value
                    "version" -> {}
                    "tools" -> { // Could parse tool definitions here
                    }
                }
            }
        }
        return Triple(name, description, toolDefs)
    }
}
