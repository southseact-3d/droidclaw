// Quick verification file - all MCP related types
package com.agentapp

import com.agentapp.data.models.*
import com.agentapp.data.db.MpcDao
import com.agentapp.providers.MpcClient
import com.agentapp.agent.AgentCore
import com.agentapp.ui.settings.SettingsViewModel
import com.agentapp.providers.LlmProviderClient
import com.agentapp.data.repository.SettingsRepository

// Verify all types are accessible
class TypeCheck {
    fun check() {
        val server: MpcServer = MpcServer(
            name = "Test",
            url = "https://example.com",
            secret = "key",
            enabled = true
        )
        
        val tool: MpcTool = MpcTool(
            name = "test",
            description = "test tool",
            serverId = server.id,
            inputSchema = "{}"
        )
        
        val result: ToolExecutionResult = ToolExecutionResult(
            success = true,
            content = "ok",
            toolName = "test",
            serverName = "Test"
        )
    }
}
