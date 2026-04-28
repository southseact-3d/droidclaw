package com.agentapp.agent

import com.agentapp.data.db.MessageDao
import com.agentapp.data.db.SkillDao
import com.agentapp.data.models.*
import com.agentapp.data.repository.SettingsRepository
import com.agentapp.providers.ChatMessage
import com.agentapp.providers.LlmProviderClient
import com.agentapp.providers.LlmResult
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

data class AgentResponse(
    val messageId: Long,
    val tokens: Flow<String>,
    val provider: String
)

@Singleton
class AgentCore @Inject constructor(
    private val llmClient: LlmProviderClient,
    private val messageDao: MessageDao,
    private val skillDao: SkillDao,
    private val settingsRepo: SettingsRepository
) {
    private val gson = Gson()

    // ── Chat (streaming) ──────────────────────────────────────────────────────

    suspend fun chat(
        userText: String,
        sessionId: String
    ): Flow<LlmResult> = flow {
        val providers = settingsRepo.providers.first()
        val systemPrompt = settingsRepo.systemPrompt.first()
        val skills = skillDao.getEnabledSkills()

        // Save user message
        messageDao.insert(
            Message(
                sessionId = sessionId,
                role = Role.USER,
                content = userText,
                source = MessageSource.CHAT
            )
        )

        // Assemble messages
        val messages = buildMessages(sessionId, systemPrompt, skills, userText)

        // Stream from LLM with fallback
        var fullResponse = ""
        var usedProvider = ""

        llmClient.chatWithFallback(messages, providers).collect { result ->
            when (result) {
                is LlmResult.Token -> {
                    fullResponse += result.text
                    usedProvider = result.provider.name
                    emit(result)
                }
                is LlmResult.Complete -> {
                    if (result.fullText.isNotEmpty()) fullResponse = result.fullText
                    usedProvider = result.provider.name
                    emit(result)
                }
                is LlmResult.Error -> emit(result)
            }
        }

        // Persist assistant response
        if (fullResponse.isNotEmpty()) {
            messageDao.insert(
                Message(
                    sessionId = sessionId,
                    role = Role.ASSISTANT,
                    content = fullResponse,
                    source = MessageSource.CHAT,
                    providerUsed = usedProvider
                )
            )
        }
    }

    // ── Heartbeat (non-streaming, for WorkManager) ────────────────────────────

    suspend fun runHeartbeat(heartbeatMd: String): HeartbeatResult {
        val providers = settingsRepo.providers.first()
        val systemPrompt = settingsRepo.systemPrompt.first()
        val sessionId = "heartbeat"

        val heartbeatContext = if (heartbeatMd.isNotBlank()) {
            "\n\n---\n# HEARTBEAT.md\n$heartbeatMd\n---"
        } else ""

        val messages = listOf(
            ChatMessage("system", systemPrompt + heartbeatContext),
            ChatMessage(
                "user",
                "Heartbeat check. Review your HEARTBEAT.md checklist if present. " +
                "Reply HEARTBEAT_OK if nothing needs attention, otherwise summarise what requires attention."
            )
        )

        return try {
            val (response, providerType) = llmClient.chatOnce(messages, providers, maxTokens = 256)
            val isOk = response.trim().startsWith("HEARTBEAT_OK", ignoreCase = true)

            if (!isOk) {
                // Persist to heartbeat session
                messageDao.insert(
                    Message(
                        sessionId = sessionId,
                        role = Role.ASSISTANT,
                        content = response,
                        source = MessageSource.HEARTBEAT,
                        providerUsed = providerType.name
                    )
                )
            }

            HeartbeatResult(
                needsAttention = !isOk,
                summary = if (isOk) null else response,
                provider = providerType.name
            )
        } catch (e: Exception) {
            HeartbeatResult(needsAttention = false, error = e.message)
        }
    }

    // ── Cron job run ──────────────────────────────────────────────────────────

    suspend fun runCronJob(job: ScheduledJob): CronResult {
        val providers = settingsRepo.providers.first()
        val systemPrompt = settingsRepo.systemPrompt.first()

        val messages = listOf(
            ChatMessage("system", systemPrompt),
            ChatMessage("user", job.prompt)
        )

        return try {
            val (response, providerType) = llmClient.chatOnce(messages, providers, maxTokens = 512)

            messageDao.insert(
                Message(
                    sessionId = "cron_${job.id}",
                    role = Role.ASSISTANT,
                    content = response,
                    source = MessageSource.CRON,
                    providerUsed = providerType.name
                )
            )

            CronResult(success = true, result = response, provider = providerType.name)
        } catch (e: Exception) {
            CronResult(success = false, error = e.message)
        }
    }

    // ── Context assembly ──────────────────────────────────────────────────────

    private suspend fun buildMessages(
        sessionId: String,
        systemPrompt: String,
        skills: List<Skill>,
        userText: String
    ): List<ChatMessage> {
        val messages = mutableListOf<ChatMessage>()

        // System prompt + skills
        val skillContext = buildSkillContext(skills)
        messages.add(ChatMessage("system", systemPrompt + skillContext))

        // Recent conversation history (last 20 messages)
        val history = messageDao.getRecentMessages(sessionId, 20).reversed()
        history.forEach { msg ->
            val role = when (msg.role) {
                Role.USER -> "user"
                Role.ASSISTANT -> "assistant"
                Role.SYSTEM -> "system"
                else -> return@forEach
            }
            messages.add(ChatMessage(role, msg.content))
        }

        // Current user message
        messages.add(ChatMessage("user", userText))

        return messages
    }

    private fun buildSkillContext(skills: List<Skill>): String {
        if (skills.isEmpty()) return ""

        val sb = StringBuilder("\n\n---\n# Active Skills\n\n")
        skills.forEach { skill ->
            sb.appendLine("## ${skill.name}")
            sb.appendLine(skill.markdownContent)
            sb.appendLine()

            // Include tool definitions if present
            if (skill.toolDefinitions != "[]") {
                try {
                    val type = object : TypeToken<List<Map<String, Any>>>() {}.type
                    val tools: List<Map<String, Any>> = gson.fromJson(skill.toolDefinitions, type)
                    if (tools.isNotEmpty()) {
                        sb.appendLine("Available tools from this skill:")
                        tools.forEach { tool ->
                            sb.appendLine("- ${tool["name"]}: ${tool["description"]}")
                        }
                    }
                } catch (_: Exception) {}
            }
        }
        sb.appendLine("---")
        return sb.toString()
    }
}

data class HeartbeatResult(
    val needsAttention: Boolean,
    val summary: String? = null,
    val provider: String? = null,
    val error: String? = null
)

data class CronResult(
    val success: Boolean,
    val result: String? = null,
    val provider: String? = null,
    val error: String? = null
)
