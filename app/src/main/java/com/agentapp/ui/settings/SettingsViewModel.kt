package com.agentapp.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agentapp.data.models.*
import com.agentapp.data.repository.SettingsRepository
import com.agentapp.providers.MpcClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class SettingsUiState(
    val providers: List<ProviderConfig> = emptyList(),
    val systemPrompt: String = "",
    val notificationsEnabled: Boolean = true,
    val editingProviderIndex: Int? = null,
    val testResult: String? = null,
    val isTesting: Boolean = false,
    val mpcServers: List<MpcServer> = emptyList(),
    val mpcEnabled: Boolean = true,
    val editingMpcServerId: String? = null,
    val availableMpcTools: List<MpcTool> = emptyList(),
    val isRefreshingTools: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepo: SettingsRepository,
    private val mpcClient: MpcClient
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                settingsRepo.providers,
                settingsRepo.systemPrompt,
                settingsRepo.notificationsEnabled,
                settingsRepo.mpcServers,
                settingsRepo.mpcEnabled
            ) { providers, systemPrompt, notifications, mpcServers, mpcEnabled ->
                _state.update {
                    it.copy(
                        providers = providers,
                        systemPrompt = systemPrompt,
                        notificationsEnabled = notifications,
                        mpcServers = mpcServers,
                        mpcEnabled = mpcEnabled
                    )
                }
            }.collect()
        }
    }

    // Provider management
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

    // MCP Server management
    fun addMpcServer(server: MpcServer) {
        viewModelScope.launch {
            val current = _state.value.mpcServers.toMutableList()
            val newServer = server.copy(priority = current.size)
            current.add(newServer)
            settingsRepo.saveMpcServers(current)
        }
    }

    fun updateMpcServer(serverId: String, updated: MpcServer) {
        viewModelScope.launch {
            val current = _state.value.mpcServers.toMutableList()
            val index = current.indexOfFirst { it.id == serverId }
            if (index >= 0) {
                current[index] = updated
                settingsRepo.saveMpcServers(current)
            }
        }
    }

    fun deleteMpcServer(serverId: String) {
        viewModelScope.launch {
            val current = _state.value.mpcServers.toMutableList()
            current.removeAll { it.id == serverId }
            // Reorder priorities
            current.forEachIndexed { i, s ->
                // Priority is just for ordering, stored in memory only
            }
            settingsRepo.saveMpcServers(current)
        }
    }

    fun moveMpcServerUp(serverId: String) {
        viewModelScope.launch {
            val current = _state.value.mpcServers.toMutableList()
            val index = current.indexOfFirst { it.id == serverId }
            if (index > 0) {
                val temp = current[index]
                current[index] = current[index - 1]
                current[index - 1] = temp
                settingsRepo.saveMpcServers(current)
            }
        }
    }

    fun moveMpcServerDown(serverId: String) {
        viewModelScope.launch {
            val current = _state.value.mpcServers.toMutableList()
            val index = current.indexOfFirst { it.id == serverId }
            if (index >= 0 && index < current.size - 1) {
                val temp = current[index]
                current[index] = current[index + 1]
                current[index + 1] = temp
                settingsRepo.saveMpcServers(current)
            }
        }
    }

    fun setMpcEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepo.setMpcEnabled(enabled) }
    }

    fun editMpcServer(serverId: String?) = _state.update { it.copy(editingMpcServerId = serverId) }
    fun closeMpcEditor() = _state.update { it.copy(editingMpcServerId = null) }

    // Tool management
    suspend fun refreshToolsForServer(server: MpcServer): List<MpcTool> = withContext(viewModelScope.coroutineContext) {
        _state.update { it.copy(isRefreshingTools = true) }
        try {
            val tools = mpcClient.listTools(server)
            // Note: tools are cached in database by repository layer
            _state.update { it.copy(isRefreshingTools = false) }
            tools
        } catch (e: Exception) {
            _state.update { it.copy(isRefreshingTools = false) }
            emptyList()
        }
    }

    suspend fun testMpcConnection(server: MpcServer): Boolean {
        _state.update { it.copy(isTesting = true, testResult = null) }
        return try {
            val connected = mpcClient.testConnection(server)
            _state.update { 
                it.copy(
                    isTesting = false,
                    testResult = if (connected) "Connection successful!" else "Connection failed"
                )
            }
            connected
        } catch (e: Exception) {
            _state.update { 
                it.copy(
                    isTesting = false,
                    testResult = "Error: ${e.message}"
                )
            }
            false
        }
    }

    fun setAvailableMpcTools(tools: List<MpcTool>) {
        _state.update { it.copy(availableMpcTools = tools) }
    }
}
