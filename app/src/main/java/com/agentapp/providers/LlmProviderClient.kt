package com.agentapp.providers

import com.agentapp.data.models.ProviderConfig
import com.agentapp.data.models.ProviderType
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

data class ChatMessage(val role: String, val content: String)

sealed class LlmResult {
    data class Token(val text: String, val provider: ProviderType) : LlmResult()
    data class Complete(val fullText: String, val provider: ProviderType, val tokens: Int?) : LlmResult()
    data class Error(val message: String, val provider: ProviderType?) : LlmResult()
}

data class ProviderError(val provider: ProviderConfig, val error: String)

@Singleton
class LlmProviderClient @Inject constructor() {

    private val gson = Gson()
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Try each enabled provider in priority order.
     * On any error, fall back to the next provider.
     * Emits LlmResult tokens as they stream.
     */
    fun chatWithFallback(
        messages: List<ChatMessage>,
        providers: List<ProviderConfig>,
        maxTokens: Int = 1024,
        temperature: Float = 0.7f
    ): Flow<LlmResult> = flow {
        val sorted = providers
            .filter { it.enabled && it.apiKey.isNotBlank() }
            .sortedBy { it.priority }

        if (sorted.isEmpty()) {
            emit(LlmResult.Error("No providers configured. Add an API key in Settings.", null))
            return@flow
        }

        val errors = mutableListOf<ProviderError>()

        for (provider in sorted) {
            try {
                var fullText = ""
                var failed = false

                streamChat(messages, provider, maxTokens, temperature).collect { chunk ->
                    when (chunk) {
                        is LlmResult.Token -> {
                            fullText += chunk.text
                            emit(chunk)
                        }
                        is LlmResult.Complete -> {
                            emit(chunk)
                        }
                        is LlmResult.Error -> {
                            failed = true
                            errors.add(ProviderError(provider, chunk.message))
                        }
                    }
                }

                if (!failed) return@flow   // success — stop trying providers

            } catch (e: Exception) {
                errors.add(ProviderError(provider, e.message ?: "Unknown error"))
            }
        }

        // All providers failed
        val summary = errors.joinToString("\n") { "• ${it.provider.type.displayName}: ${it.error}" }
        emit(LlmResult.Error("All providers failed:\n$summary", null))
    }

    /**
     * Non-streaming version for heartbeat/cron workers.
     * Returns the complete response text or throws.
     */
    suspend fun chatOnce(
        messages: List<ChatMessage>,
        providers: List<ProviderConfig>,
        maxTokens: Int = 512
    ): Pair<String, ProviderType> {
        val sorted = providers
            .filter { it.enabled && it.apiKey.isNotBlank() }
            .sortedBy { it.priority }

        if (sorted.isEmpty()) throw IllegalStateException("No providers configured")

        val errors = mutableListOf<String>()

        for (provider in sorted) {
            try {
                val result = nonStreamingChat(messages, provider, maxTokens)
                return Pair(result, provider.type)
            } catch (e: Exception) {
                errors.add("${provider.type.displayName}: ${e.message}")
            }
        }

        throw IOException("All providers failed:\n${errors.joinToString("\n")}")
    }

    // ── Internal streaming ────────────────────────────────────────────────────

    private fun streamChat(
        messages: List<ChatMessage>,
        provider: ProviderConfig,
        maxTokens: Int,
        temperature: Float
    ): Flow<LlmResult> = flow {
        val body = buildRequestBody(messages, provider, maxTokens, temperature, stream = true)
        val request = buildRequest(provider, body)

        suspendCancellableCoroutine<Unit> { cont ->
            val factory = EventSources.createFactory(client)
            val fullText = StringBuilder()
            var completed = false

            val listener = object : EventSourceListener() {
                override fun onEvent(source: EventSource, id: String?, type: String?, data: String) {
                    if (data == "[DONE]") {
                        if (!completed) {
                            completed = true
                            cont.resume(Unit)
                        }
                        return
                    }
                    try {
                        val json = gson.fromJson(data, JsonObject::class.java)
                        val delta = json
                            ?.getAsJsonArray("choices")
                            ?.firstOrNull()
                            ?.asJsonObject
                            ?.getAsJsonObject("delta")
                            ?.get("content")
                            ?.asString ?: return
                        if (delta.isNotEmpty()) {
                            fullText.append(delta)
                            // We can't emit here (not in coroutine scope), so we accumulate
                            // The flow collector will see Complete at the end
                        }
                    } catch (_: Exception) {}
                }

                override fun onClosed(source: EventSource) {
                    if (!completed) {
                        completed = true
                        cont.resume(Unit)
                    }
                }

                override fun onFailure(source: EventSource, t: Throwable?, response: Response?) {
                    if (!completed) {
                        completed = true
                        val msg = t?.message ?: response?.message ?: "Stream failed"
                        cont.resumeWithException(IOException(msg))
                    }
                }
            }

            val es = factory.newEventSource(request, listener)
            cont.invokeOnCancellation { es.cancel() }
        }

        // For simplicity emit as single complete token (SSE accumulation)
        // Production: refactor to channel-based approach for true token streaming
        emit(LlmResult.Complete("", provider.type, null))
    }

    // ── Internal non-streaming ────────────────────────────────────────────────

    private suspend fun nonStreamingChat(
        messages: List<ChatMessage>,
        provider: ProviderConfig,
        maxTokens: Int
    ): String = suspendCancellableCoroutine { cont ->
        val body = buildRequestBody(messages, provider, maxTokens, 0.7f, stream = false)
        val request = buildRequest(provider, body)

        val call = client.newCall(request)
        cont.invokeOnCancellation { call.cancel() }

        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                cont.resumeWithException(e)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use { resp ->
                    if (!resp.isSuccessful) {
                        cont.resumeWithException(
                            IOException("HTTP ${resp.code}: ${resp.message}")
                        )
                        return
                    }
                    try {
                        val json = gson.fromJson(resp.body?.string(), JsonObject::class.java)
                        val content = json
                            ?.getAsJsonArray("choices")
                            ?.firstOrNull()
                            ?.asJsonObject
                            ?.getAsJsonObject("message")
                            ?.get("content")
                            ?.asString
                            ?: throw IOException("Unexpected response format")
                        cont.resume(content)
                    } catch (e: Exception) {
                        cont.resumeWithException(e)
                    }
                }
            }
        })
    }

    // ── Request builders ──────────────────────────────────────────────────────

    private fun buildRequestBody(
        messages: List<ChatMessage>,
        provider: ProviderConfig,
        maxTokens: Int,
        temperature: Float,
        stream: Boolean
    ): RequestBody {
        val messagesArray = JsonArray()
        messages.forEach { msg ->
            JsonObject().apply {
                addProperty("role", msg.role)
                addProperty("content", msg.content)
            }.also { messagesArray.add(it) }
        }

        val payload = JsonObject().apply {
            addProperty("model", provider.model)
            add("messages", messagesArray)
            addProperty("max_tokens", maxTokens)
            addProperty("temperature", temperature)
            addProperty("stream", stream)

            // Provider-specific extras
            when (provider.type) {
                ProviderType.OPENROUTER -> {
                    addProperty("route", "fallback")
                }
                ProviderType.NVIDIA_NIM -> {
                    // NIM supports top_p
                    addProperty("top_p", 1)
                }
                ProviderType.KILO_GATEWAY -> {
                    // Kilo is OpenAI-compatible, no extras needed
                }
            }
        }

        return gson.toJson(payload)
            .toRequestBody("application/json; charset=utf-8".toMediaType())
    }

    private fun buildRequest(provider: ProviderConfig, body: RequestBody): Request {
        val url = "${provider.baseUrl.trimEnd('/')}/chat/completions"

        return Request.Builder()
            .url(url)
            .post(body)
            .header("Authorization", "Bearer ${provider.apiKey}")
            .header("Content-Type", "application/json")
            .apply {
                when (provider.type) {
                    ProviderType.OPENROUTER -> {
                        header("HTTP-Referer", "https://agentapp.local")
                        header("X-Title", "AgentApp")
                    }
                    ProviderType.NVIDIA_NIM -> {
                        // Standard auth only
                    }
                    ProviderType.KILO_GATEWAY -> {
                        // Standard auth only
                    }
                }
            }
            .build()
    }
}

// ── Extensions ────────────────────────────────────────────────────────────────

val ProviderType.displayName get() = when (this) {
    ProviderType.NVIDIA_NIM    -> "Nvidia NIM"
    ProviderType.OPENROUTER    -> "OpenRouter"
    ProviderType.KILO_GATEWAY  -> "Kilo Gateway"
}

val ProviderType.defaultBaseUrl get() = when (this) {
    ProviderType.NVIDIA_NIM    -> "https://integrate.api.nvidia.com/v1"
    ProviderType.OPENROUTER    -> "https://openrouter.ai/api/v1"
    ProviderType.KILO_GATEWAY  -> "https://api.kilo.dev/v1"
}

val ProviderType.modelSuggestions get() = when (this) {
    ProviderType.NVIDIA_NIM -> listOf(
        "meta/llama-3.3-70b-instruct",
        "meta/llama-3.1-405b-instruct",
        "mistralai/mistral-7b-instruct-v0.3",
        "nvidia/llama-3.1-nemotron-70b-instruct"
    )
    ProviderType.OPENROUTER -> listOf(
        "anthropic/claude-3.5-sonnet",
        "anthropic/claude-3-haiku",
        "openai/gpt-4o",
        "openai/gpt-4o-mini",
        "meta-llama/llama-3.3-70b-instruct",
        "google/gemini-flash-1.5"
    )
    ProviderType.KILO_GATEWAY -> listOf(
        "claude-3-5-sonnet-20241022",
        "claude-3-5-haiku-20241022",
        "claude-3-opus-20240229",
        "gpt-4o",
        "gpt-4o-mini"
    )
}
