package com.agentapp.providers

import android.util.Log
import com.agentapp.data.models.MpcServer
import com.agentapp.data.models.MpcTool
import com.agentapp.data.models.ToolExecutionResult
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MpcClient @Inject constructor() {
    private val gson = Gson()
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val tag = "MpcClient"

    // List available tools from an MCP server
    suspend fun listTools(server: MpcServer): List<MpcTool> = withContext(Dispatchers.IO) {
        try {
            val body = JsonObject().apply {
                addProperty("jsonrpc", "2.0")
                addProperty("id", 1)
                addProperty("method", "tools/list")
                add("params", JsonObject().apply { add("cursor", JsonObject()) })
            }

            val request = buildRequest(server, body)
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                Log.w(tag, "Failed to list tools from ${server.url}: ${response.code}")
                return@withContext emptyList()
            }

            val responseBody = response.body?.string()
            if (responseBody.isNullOrBlank()) {
                return@withContext emptyList()
            }

            try {
                val json = gson.fromJson(responseBody, JsonObject::class.java)
                val result = json.getAsJsonObject("result")
                val toolsArray = result?.getAsJsonArray("tools")

                if (toolsArray == null || toolsArray.size() == 0) {
                    return@withContext emptyList()
                }

                toolsArray.mapNotNull { toolJson ->
                    try {
                        val toolObj = toolJson.asJsonObject
                        val name = toolObj.get("name")?.asString ?: return@mapNotNull null
                        val description = toolObj.get("description")?.asString ?: ""
                        val inputSchema = toolObj.get("inputSchema")?.asJsonObject
                            ?.toString() ?: "{}"

                        MpcTool(
                            serverId = server.id,
                            name = name,
                            description = description,
                            inputSchema = inputSchema
                        )
                    } catch (e: Exception) {
                        Log.w(tag, "Failed to parse tool: ${e.message}")
                        null
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Failed to parse tools response: ${e.message}", e)
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to fetch tools from ${server.url}: ${e.message}", e)
            emptyList()
        }
    }

    // Execute a tool on an MCP server
    suspend fun executeTool(
        server: MpcServer,
        toolName: String,
        input: Map<String, Any>
    ): ToolExecutionResult = withContext(Dispatchers.IO) {
        try {
            val params = JsonObject().apply {
                add("name", JsonObject().apply { addProperty("type", "text"); addProperty("text", toolName) })
                add("arguments", gson.toJsonTree(input))
            }

            val body = JsonObject().apply {
                addProperty("jsonrpc", "2.0")
                addProperty("id", 1)
                addProperty("method", "tools/call")
                add("params", params)
            }

            val request = buildRequest(server, body)
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                return@withContext ToolExecutionResult(
                    success = false,
                    content = "HTTP ${response.code}: ${response.message}",
                    toolName = toolName,
                    serverName = server.name
                )
            }

            val responseBody = response.body?.string()
            if (responseBody.isNullOrBlank()) {
                return@withContext ToolExecutionResult(
                    success = false,
                    content = "Empty response from server",
                    toolName = toolName,
                    serverName = server.name
                )
            }

            try {
                val json = gson.fromJson(responseBody, JsonObject::class.java)
                val result = json.getAsJsonObject("result")
                val content = result?.get("content")?.asString
                    ?: result?.toString()
                    ?: "No content in response"

                ToolExecutionResult(
                    success = true,
                    content = content,
                    toolName = toolName,
                    serverName = server.name
                )
            } catch (e: Exception) {
                ToolExecutionResult(
                    success = false,
                    content = "Failed to parse response: ${e.message}",
                    toolName = toolName,
                    serverName = server.name
                )
            }
        } catch (e: Exception) {
            ToolExecutionResult(
                success = false,
                content = "Error: ${e.message}",
                toolName = toolName,
                serverName = server.name
            )
        }
    }

    // Test connection to an MCP server
    suspend fun testConnection(server: MpcServer): Boolean = withContext(Dispatchers.IO) {
        try {
            val body = JsonObject().apply {
                addProperty("jsonrpc", "2.0")
                addProperty("id", 1)
                addProperty("method", "tools/list")
                add("params", JsonObject().apply { add("cursor", JsonObject()) })
            }

            val request = buildRequest(server, body)
            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            Log.e(tag, "Connection test failed for ${server.url}: ${e.message}", e)
            false
        }
    }

    private fun buildRequest(server: MpcServer, body: JsonObject): Request {
        val url = server.url.trimEnd('/') + "/mcp"
        return Request.Builder()
            .url(url)
            .post(body.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
            .header("Authorization", "Bearer ${server.secret}")
            .header("Content-Type", "application/json")
            .build()
    }
}
