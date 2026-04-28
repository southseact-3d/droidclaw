package com.agentapp.ui.settings

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.agentapp.data.models.ProviderConfig
import com.agentapp.data.models.ProviderType
import com.agentapp.providers.defaultBaseUrl
import com.agentapp.providers.displayName
import com.agentapp.providers.modelSuggestions
import com.agentapp.ui.skills.agentTextFieldColors
import com.agentapp.ui.theme.*

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().background(Obsidian)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().background(Surface1)
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Text("Settings", style = MaterialTheme.typography.headlineMedium, color = TextPrimary)
        }
        HorizontalDivider(color = Border, thickness = 0.5.dp)

        Spacer(Modifier.height(12.dp))

        // ── Providers ─────────────────────────────────────────────────────────
        SectionHeader("AI PROVIDERS — FALLBACK ORDER")
        Text(
            "Providers are tried top-to-bottom. On error, the next enabled provider is used.",
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )
        Spacer(Modifier.height(8.dp))

        state.providers.forEachIndexed { index, provider ->
            ProviderCard(
                provider = provider,
                index = index,
                total = state.providers.size,
                onEdit = { viewModel.editProvider(index) },
                onToggle = {
                    viewModel.updateProvider(index, provider.copy(enabled = !provider.enabled))
                },
                onMoveUp = { viewModel.moveProviderUp(index) },
                onMoveDown = { viewModel.moveProviderDown(index) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        Spacer(Modifier.height(20.dp))

        // ── System prompt ─────────────────────────────────────────────────────
        SectionHeader("SYSTEM PROMPT")
        Spacer(Modifier.height(8.dp))
        var systemPromptLocal by remember(state.systemPrompt) {
            mutableStateOf(state.systemPrompt)
        }
        OutlinedTextField(
            value = systemPromptLocal,
            onValueChange = { systemPromptLocal = it },
            modifier = Modifier.fillMaxWidth().height(160.dp)
                .padding(horizontal = 16.dp),
            colors = agentTextFieldColors(),
            textStyle = MaterialTheme.typography.bodySmall,
            maxLines = 20
        )
        Spacer(Modifier.height(6.dp))
        Row(modifier = Modifier.padding(horizontal = 16.dp)) {
            Spacer(Modifier.weight(1f))
            FilledTonalButton(
                onClick = { viewModel.setSystemPrompt(systemPromptLocal) },
                colors = ButtonDefaults.filledTonalButtonColors(containerColor = AmberDim)
            ) {
                Text("Save", color = AmberGlow, style = MaterialTheme.typography.labelLarge)
            }
        }

        Spacer(Modifier.height(20.dp))

        // ── Notifications ─────────────────────────────────────────────────────
        SectionHeader("NOTIFICATIONS")
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(10.dp)).background(Surface2).padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Push notifications", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Text("Agent notifies you when it has something to report",
                    style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            Switch(
                checked = state.notificationsEnabled,
                onCheckedChange = viewModel::setNotificationsEnabled,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Obsidian, checkedTrackColor = Amber,
                    uncheckedTrackColor = Surface3
                )
            )
        }

        Spacer(Modifier.height(32.dp))
    }

    // Provider edit dialog
    state.editingProviderIndex?.let { index ->
        if (index < state.providers.size) {
            ProviderEditDialog(
                provider = state.providers[index],
                onSave = { updated ->
                    viewModel.updateProvider(index, updated)
                    viewModel.closeEditor()
                },
                onDismiss = viewModel::closeEditor
            )
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelSmall,
        color = TextMuted,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
    )
}

@Composable
private fun ProviderCard(
    provider: ProviderConfig,
    index: Int,
    total: Int,
    onEdit: () -> Unit,
    onToggle: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasKey = provider.apiKey.isNotBlank()
    val statusColor = when {
        !provider.enabled -> TextMuted
        !hasKey -> Red
        else -> Green
    }
    val statusLabel = when {
        !provider.enabled -> "Disabled"
        !hasKey -> "No API key"
        else -> "Ready"
    }

    Row(
        modifier = modifier.clip(RoundedCornerShape(10.dp)).background(Surface2).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Priority badge
        Box(
            modifier = Modifier.size(24.dp).clip(RoundedCornerShape(6.dp)).background(Surface3),
            contentAlignment = Alignment.Center
        ) {
            Text("${index + 1}", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
        }

        Spacer(Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(provider.type.displayName, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier.size(6.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(statusColor)
                )
                Text(statusLabel, style = MaterialTheme.typography.labelSmall, color = statusColor)
                if (hasKey && provider.enabled) {
                    Text("·", color = TextMuted, style = MaterialTheme.typography.labelSmall)
                    Text(provider.model, style = MaterialTheme.typography.labelSmall,
                        color = TextMuted, maxLines = 1)
                }
            }
        }

        // Reorder
        Column {
            IconButton(onClick = onMoveUp, modifier = Modifier.size(28.dp), enabled = index > 0) {
                Icon(Icons.Default.KeyboardArrowUp, null,
                    tint = if (index > 0) TextSecondary else TextMuted,
                    modifier = Modifier.size(16.dp))
            }
            IconButton(onClick = onMoveDown, modifier = Modifier.size(28.dp), enabled = index < total - 1) {
                Icon(Icons.Default.KeyboardArrowDown, null,
                    tint = if (index < total - 1) TextSecondary else TextMuted,
                    modifier = Modifier.size(16.dp))
            }
        }

        Switch(
            checked = provider.enabled, onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Obsidian, checkedTrackColor = Amber,
                uncheckedTrackColor = Surface3
            )
        )

        IconButton(onClick = onEdit) {
            Icon(Icons.Default.Edit, "Edit", tint = TextSecondary, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun ProviderEditDialog(
    provider: ProviderConfig,
    onSave: (ProviderConfig) -> Unit,
    onDismiss: () -> Unit
) {
    var apiKey by remember { mutableStateOf(provider.apiKey) }
    var model by remember { mutableStateOf(provider.model) }
    var baseUrl by remember { mutableStateOf(provider.baseUrl.ifBlank { provider.type.defaultBaseUrl }) }
    var showKey by remember { mutableStateOf(false) }
    var showModelSuggestions by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Surface2,
        title = {
            Text(provider.type.displayName, color = TextPrimary,
                style = MaterialTheme.typography.headlineMedium)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                // API Key field
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text("API Key") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = agentTextFieldColors(),
                    singleLine = true,
                    visualTransformation = if (showKey) VisualTransformation.None
                        else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { showKey = !showKey }) {
                            Icon(
                                if (showKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                null, tint = TextSecondary, modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                )

                // Model field + suggestions
                Column {
                    OutlinedTextField(
                        value = model,
                        onValueChange = { model = it },
                        label = { Text("Model") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = agentTextFieldColors(),
                        singleLine = true,
                        trailingIcon = {
                            IconButton(onClick = { showModelSuggestions = !showModelSuggestions }) {
                                Icon(Icons.Default.ArrowDropDown, null, tint = TextSecondary)
                            }
                        }
                    )
                    if (showModelSuggestions) {
                        Column(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                                .background(Surface3).padding(4.dp)
                        ) {
                            provider.type.modelSuggestions.forEach { suggestion ->
                                Row(
                                    modifier = Modifier.fillMaxWidth()
                                        .clickable { model = suggestion; showModelSuggestions = false }
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(suggestion, style = MaterialTheme.typography.bodySmall,
                                        color = if (suggestion == model) Amber else TextPrimary)
                                }
                            }
                        }
                    }
                }

                // Base URL
                OutlinedTextField(
                    value = baseUrl,
                    onValueChange = { baseUrl = it },
                    label = { Text("Base URL") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = agentTextFieldColors(),
                    singleLine = true,
                    placeholder = { Text(provider.type.defaultBaseUrl, color = TextMuted) }
                )

                // Reset URL hint
                if (baseUrl != provider.type.defaultBaseUrl) {
                    TextButton(
                        onClick = { baseUrl = provider.type.defaultBaseUrl },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Reset to default URL", color = TextSecondary,
                            style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(provider.copy(apiKey = apiKey, model = model, baseUrl = baseUrl))
                },
                colors = ButtonDefaults.buttonColors(containerColor = Amber, contentColor = Obsidian)
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        }
    )
}
