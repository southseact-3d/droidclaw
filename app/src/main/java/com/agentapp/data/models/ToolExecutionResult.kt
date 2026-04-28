package com.agentapp.data.models

data class ToolExecutionResult(
    val success: Boolean,
    val content: String,
    val toolName: String,
    val serverName: String
)
