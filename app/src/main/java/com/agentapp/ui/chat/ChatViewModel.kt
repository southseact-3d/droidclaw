package com.agentapp.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agentapp.agent.AgentCore
import com.agentapp.data.db.MessageDao
import com.agentapp.data.db.SessionDao
import com.agentapp.data.models.*
import com.agentapp.data.repository.SettingsRepository
import com.agentapp.providers.LlmResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val streamingText: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val sessionId: String = "main",
    val providerLabel: String = ""
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val agentCore: AgentCore,
    private val messageDao: MessageDao,
    private val sessionDao: SessionDao,
    private val settingsRepo: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepo.activeSession.collect { sessionId ->
                _uiState.update { it.copy(sessionId = sessionId) }
                observeMessages(sessionId)
                ensureSession(sessionId)
            }
        }
    }

    private fun observeMessages(sessionId: String) {
        viewModelScope.launch {
            messageDao.getMessages(sessionId).collect { messages ->
                _uiState.update { it.copy(messages = messages) }
            }
        }
    }

    private suspend fun ensureSession(sessionId: String) {
        if (sessionDao.getSession(sessionId) == null) {
            sessionDao.insert(Session(id = sessionId, name = "Main"))
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank() || _uiState.value.isLoading) return

        val sessionId = _uiState.value.sessionId
        _uiState.update { it.copy(isLoading = true, streamingText = "", error = null) }

        viewModelScope.launch {
            var streamText = ""
            try {
                agentCore.chat(text, sessionId).collect { result ->
                    when (result) {
                        is LlmResult.Token -> {
                            streamText += result.text
                            _uiState.update { it.copy(
                                streamingText = streamText,
                                providerLabel = result.provider.name
                            )}
                        }
                        is LlmResult.Complete -> {
                            _uiState.update { it.copy(
                                isLoading = false,
                                streamingText = "",
                                providerLabel = result.provider.name
                            )}
                        }
                        is LlmResult.Error -> {
                            _uiState.update { it.copy(
                                isLoading = false,
                                streamingText = "",
                                error = result.message
                            )}
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    streamingText = "",
                    error = e.message ?: "Unknown error"
                )}
            }
        }
    }

    fun clearSession() {
        viewModelScope.launch {
            messageDao.clearSession(_uiState.value.sessionId)
        }
    }

    fun dismissError() = _uiState.update { it.copy(error = null) }
}
