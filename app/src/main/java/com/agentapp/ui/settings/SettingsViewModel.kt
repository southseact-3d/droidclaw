package com.agentapp.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agentapp.data.models.ProviderConfig
import com.agentapp.data.models.ProviderType
import com.agentapp.data.repository.SettingsRepository
import com.agentapp.providers.defaultBaseUrl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val providers: List<ProviderConfig> = emptyList(),
    val systemPrompt: String = "",
    val notificationsEnabled: Boolean = true,
    val editingProviderIndex: Int? = null,
    val testResult: String? = null,
    val isTesting: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepo: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                settingsRepo.providers,
                settingsRepo.systemPrompt,
                settingsRepo.notificationsEnabled
            ) { providers, systemPrompt, notifications ->
                _state.update {
                    it.copy(
                        providers = providers,
                        systemPrompt = systemPrompt,
                        notificationsEnabled = notifications
                    )
                }
            }.collect()
        }
    }

    fun updateProvider(index: Int, updated: ProviderConfig) {
        viewModelScope.launch {
            val list = _state.value.providers.toMutableList()
            if (index < list.size) {
                list[index] = updated
                settingsRepo.saveProviders(list)
            }
        }
    }

    fun moveProviderUp(index: Int) {
        if (index <= 0) return
        viewModelScope.launch {
            val list = _state.value.providers.toMutableList()
            val temp = list[index]
            list[index] = list[index - 1].copy(priority = index)
            list[index - 1] = temp.copy(priority = index - 1)
            settingsRepo.saveProviders(list)
        }
    }

    fun moveProviderDown(index: Int) {
        val list = _state.value.providers
        if (index >= list.size - 1) return
        viewModelScope.launch {
            val mutable = list.toMutableList()
            val temp = mutable[index]
            mutable[index] = mutable[index + 1].copy(priority = index)
            mutable[index + 1] = temp.copy(priority = index + 1)
            settingsRepo.saveProviders(mutable)
        }
    }

    fun setSystemPrompt(prompt: String) {
        viewModelScope.launch { settingsRepo.setSystemPrompt(prompt) }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepo.setNotificationsEnabled(enabled) }
    }

    fun editProvider(index: Int) = _state.update { it.copy(editingProviderIndex = index) }
    fun closeEditor() = _state.update { it.copy(editingProviderIndex = null, testResult = null) }
    fun clearTestResult() = _state.update { it.copy(testResult = null) }
}
