package com.agentapp.agent

import com.agentapp.data.db.MessageDao
import com.agentapp.data.db.SkillDao
import com.agentapp.data.models.*
import com.agentapp.data.repository.SettingsRepository
import com.agentapp.providers.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AgentCore @Inject constructor(
    private val llmClient: LlmProviderClient,
    private val mpcClient: MpcClient,
    private val messageDao: MessageDao,
    private val skillDao: SkillDao,
    private val settingsRepo: SettingsRepository
) {
    private val gson = Gson()
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // ── Chat (streaming) ──────────────────────────────────────────────────────

    suspend fun chat(
        userText: String,
        sessionId: String
    ): Flow<LlmResult> = flow {
        val providers = settingsRepo.providers.first()
        val systemPrompt = settingsRepo.systemPrompt.first()
        val skills = skillDao.getEnabledSkills()
        val mpcServers = if (settingsRepo.mpcEnabled.first()) {
            settingsRepo.mpcServers.first().filter { it.enabled }
        } else {
            emptyList()
        }

        // Save user message
        messageDao.insert(
            Message(
                sessionId = sessionId,
                role = Role.USER,
                content = userText,
                source = MessageSource.CHAT
            )
        )

        // Assemble messages with tool context
        val messages = buildMessagesWithTools(
            sessionId,
            systemPrompt,
            skills,
            mpcServers,
            userText
        )

        // Check if we have tools that could be relevant
        val availableTools = if (mpcServers.isNotEmpty()) {
            mpcServers.flatMap { server ->
                try {
                    skillDao.getToolsForServer(server.id).map { it to server }
                } catch (e: Exception) {
                    emptyList()
                }
            }
        } else {
            emptyList()
        }

        var fullResponse = ""
        var usedProvider = ""
        var toolUsed = false

        // Stream from LLM with fallback
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
                is LlmResult.Error -> {
                    // Check if error suggests we should try a tool
                    if (availableTools.isNotEmpty() && !toolUsed) {
                        toolUsed = true
                        val toolResult = tryExecuteRelevantTool(userText, availableTools)
                        if (toolResult != null && toolResult.success) {
                            // Re-prompt with tool result
                            val retryMessages = messages.toMutableList().apply {
                                add(ChatMessage("assistant", fullResponse))
                                add(ChatMessage("tool", "Tool: ${toolResult.toolName} (${toolResult.serverName})\nResult: ${toolResult.content}"))
                                add(ChatMessage("user", "Based on the tool result above, please provide a helpful response."))
                            }
                            
                            llmClient.chatWithFallback(retryMessages, providers).collect { retryResult ->
                                when (retryResult) {
                                    is LlmResult.Token -> {
                                        fullResponse += "\n\n[Using external tool: ${toolResult.toolName}]\n" + retryResult.text
                                        usedProvider = retryResult.provider.name
                                        emit(retryResult)
                                    }
                                    is LlmResult.Complete -> {
                                        if (retryResult.fullText.isNotEmpty()) {
                                            fullResponse += "\n\n[Using external tool: ${toolResult.toolName}]\n" + retryResult.fullText
                                            usedProvider = retryResult.provider.name
                                        }
                                        emit(retryResult)
                                    }
                                    is LlmResult.Error -> emit(retryResult)
                                }
                            }
                        } else {
                            emit(result)
                        }
                    } else {
                        emit(result)
                    }
                }
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
                    providerUsed = usedProvider,
                    toolsUsed = if (toolUsed) "MCP tools available" else null
                )
            )
        }
    }

    // ── Heartbeat (non-streaming) ──────────────────────────────────────────────

    suspend fun runHeartbeat(heartbeatMd: String): HeartbeatResult {
        val providers = settingsRepo.providers.first()
        val systemPrompt = settingsRepo.systemPrompt.first()
        val mpcEnabled = settingsRepo.mpcEnabled.first()
        val mpcServers = if (mpcEnabled) {
            settingsRepo.mpcServers.first().filter { it.enabled }
        } else {
            emptyList()
        }
        val sessionId = "heartbeat"

        val heartbeatContext = if (heartbeatMd.isNotBlank()) {
            "\n\n---\n# HEARTBEAT.md\n$heartbeatMd\n---"
        } else ""

        val toolContext = buildToolContext(mpcServers)

        val messages = listOf(
            ChatMessage("system", systemPrompt + heartbeatContext + toolContext),
            ChatMessage(
                "user",
                "Heartbeat check. Review your HEARTBEAT.md checklist if present. " +
                "Reply HEARTBEAT_OK if nothing needs attention, otherwise summarise what requires attention. " +
                if (mpcServers.isNotEmpty()) "You may use available MCP tools to gather information." else ""
            )
        )

        return try {
            // Try to execute relevant tools first if any seem applicable
            val mpcTools = mpcServers.flatMap { server ->
                try {
                    skillDao.getToolsForServer(server.id).map { it to server }
                } catch (e: Exception) {
                    emptyList()
                }
            }

            var toolResultContext = ""
            if (mpcTools.isNotEmpty()) {
                // Try tools that might be relevant for heartbeat (e.g., status checks)
                val relevantTools = mpcTools.filter { (tool, _) ->
                    tool.name.contains("status", ignoreCase = true) ||
                    tool.name.contains("check", ignoreCase = true) ||
                    tool.name.contains("health", ignoreCase = true)
                }
                if (relevantTools.isNotEmpty()) {
                    val results = relevantTools.mapNotNull { (tool, server) ->
                        try {
                            val result = mpcClient.executeTool(server, tool.name, emptyMap())
                            if (result.success) result else null
                        } catch (e: Exception) {
                            null
                        }
                    }
                    if (results.isNotEmpty()) {
                        toolResultContext = "\n\nTool results:\n" + results.joinToString("\n") { r ->
                            "- [${r.serverName}] ${r.toolName}: ${r.content.take(200)}"
                        }
                    }
                }
            }

            val (response, providerType) = llmClient.chatOnce(
                messages + if (toolResultContext.isNotBlank()) listOf(
                    ChatMessage("tool", "Tool results:$toolResultContext")
                ) else emptyList(),
                providers,
                maxTokens = 256
            )
            val fullResponse = if (toolResultContext.isNotBlank()) "$toolResultContext\n\n$response" else response
            val isOk = response.trim().startsWith("HEARTBEAT_OK", ignoreCase = true)

            if (!isOk) {
                messageDao.insert(
                    Message(
                        sessionId = sessionId,
                        role = Role.ASSISTANT,
                        content = fullResponse,
                        source = MessageSource.HEARTBEAT,
                        providerUsed = providerType.name,
                        toolsUsed = if (toolResultContext.isNotBlank()) "MCP tools used" else null
                    )
                )
            }

            HeartbeatResult(
                needsAttention = !isOk,
                summary = if (isOk) null else fullResponse,
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
        val mpcEnabled = settingsRepo.mpcEnabled.first()
        val mpcServers = if (mpcEnabled) {
            settingsRepo.mpcServers.first().filter { it.enabled }
        } else {
            emptyList()
        }

        val toolContext = buildToolContext(mpcServers)

        val messages = listOf(
            ChatMessage("system", systemPrompt + toolContext),
            ChatMessage("user", job.prompt)
        )

        return try {
            // Check if job prompt suggests tool usage
            val mpcTools = mpcServers.flatMap { server ->
                try {
                    skillDao.getToolsForServer(server.id).map { it to server }
                } catch (e: Exception) {
                    emptyList()
                }
            }

            var toolResultContext = ""
            if (mpcTools.isNotEmpty()) {
                // Try to identify relevant tools from prompt
                val relevantTools = mpcTools.filter { (tool, _) ->
                    job.prompt.contains(tool.name.replace("_", " "), ignoreCase = true) ||
                    job.prompt.contains(tool.description.take(50), ignoreCase = true)
                }
                if (relevantTools.isNotEmpty()) {
                    val results = relevantTools.mapNotNull { (tool, server) ->
                        try {
                            // Try to parse potential parameters from prompt
                            val result = mpcClient.executeTool(server, tool.name, emptyMap())
                            if (result.success) result else null
                        } catch (e: Exception) {
                            null
                        }
                    }
                    if (results.isNotEmpty()) {
                        toolResultContext = "\n\nTool results:\n" + results.joinToString("\n") { r ->
                            "- [${r.serverName}] ${r.toolName}: ${r.content.take(200)}"
                        }
                    }
                }
            }

            val (response, providerType) = llmClient.chatOnce(
                messages + if (toolResultContext.isNotBlank()) listOf(
                    ChatMessage("tool", "Tool results:$toolResultContext")
                ) else emptyList(),
                providers,
                maxTokens = 512
            )
            val fullResponse = if (toolResultContext.isNotBlank()) "$toolResultContext\n\n$response" else response

            messageDao.insert(
                Message(
                    sessionId = "cron_${job.id}",
                    role = Role.ASSISTANT,
                    content = fullResponse,
                    source = MessageSource.CRON,
                    providerUsed = providerType.name,
                    toolsUsed = if (toolResultContext.isNotBlank()) "MCP tools used" else null
                )
            )

            CronResult(success = true, result = fullResponse, provider = providerType.name)
        } catch (e: Exception) {
            CronResult(success = false, error = e.message)
        }
    }

    // ── Tool execution helpers ─────────────────────────────────────────────────

    private suspend fun tryExecuteRelevantTool(
        userPrompt: String,
        availableTools: List<Pair<MpcTool, MpcServer>>
    ): ToolExecutionResult? {
        // Simple heuristic: check if prompt mentions tool names
        for ((tool, server) in availableTools) {
            if (userPrompt.contains(tool.name.replace("_", " "), ignoreCase = true) ||
                userPrompt.contains(tool.description.take(30), ignoreCase = true)) {
                return try {
                    mpcClient.executeTool(server, tool.name, emptyMap())
                } catch (e: Exception) {
                    null
                }
            }
        }
        return null
    }

    private fun buildToolContext(servers: List<MpcServer>): String {
        if (servers.isEmpty()) return ""
        val sb = StringBuilder("\n\n---")
        sb.appendLine("\nAvailable MCP Tools:")
        servers.forEach { server ->
            sb.appendLine("\nFrom ${server.name}:")
            sb.appendLine("(Server: ${server.url})")
            // Note: Tools are enumerated at runtime
            sb.appendLine("- Various tools available via this server")
        }
        sb.appendLine("\nThe AI can request to execute these tools when needed.")
        sb.appendLine("---")
        return sb.toString()
    }

    // ── Context assembly (updated for tool support) ───────────────────────────

    private suspend fun buildMessagesWithTools(
        sessionId: String,
        systemPrompt: String,
        skills: List<Skill>,
        mpcServers: List<MpcServer>,
        userText: String
    ): List<ChatMessage> {
        val messages = mutableListOf<ChatMessage>()

        // System prompt + skills + tool context
        val skillContext = buildSkillContext(skills)
        val toolContext = buildToolContext(mpcServers)
        messages.add(ChatMessage("system", systemPrompt + skillContext + toolContext))

        // Recent conversation history (last 20 messages)
        val history = messageDao.getRecentMessages(sessionId, 20).reversed()
        history.forEach { msg ->
            val role = when (msg.role) {
                Role.USER -> "user"
                Role.ASSISTANT -> "assistant"
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

        val sb = StringBuilder("\n\n---")
        sb.appendLine("\nActive Skills:")
        skills.forEach { skill ->
            sb.appendLine("\n## ${skill.name}")
            sb.appendLine(skill.markdownContent.take(500))
            if (skill.markdownContent.length > 500) sb.appendLine("...")

            // Include tool definitions if present
            if (skill.toolDefinitions?.isNotEmpty() == true && skill.toolDefinitions != "[]") {
                try {
                    val type = object : TypeToken<List<Map<String, Any>>>() {}.type
                    val tools: List<Map<String, Any>> = gson.fromJson(skill.toolDefinitions, type)
                    if (tools.isNotEmpty()) {
                        sb.appendLine("\nTools from this skill:")
                        tools.forEach { tool ->
                            sb.appendLine("- ${tool["name"]}: ${tool["description"]}")
                        }
                    }
                } catch (_: Exception) {}
            }
        }
        sb.appendLine("\n---")
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
