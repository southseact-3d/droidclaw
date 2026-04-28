package com.agentapp.ui.skills

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.agentapp.data.models.Skill
import com.agentapp.ui.theme.*

@Composable
fun SkillsScreen(viewModel: SkillsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(Obsidian)) {
        Column {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().background(Surface1)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Skills", style = MaterialTheme.typography.headlineMedium, color = TextPrimary,
                    modifier = Modifier.weight(1f))
                FilledTonalButton(
                    onClick = viewModel::showAddDialog,
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = AmberDim)
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp), tint = AmberGlow)
                    Spacer(Modifier.width(4.dp))
                    Text("Add", color = AmberGlow, style = MaterialTheme.typography.labelLarge)
                }
            }
            HorizontalDivider(color = Border, thickness = 0.5.dp)

            if (state.skills.isEmpty()) {
                EmptySkillsState(onAdd = viewModel::showAddDialog)
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(state.skills, key = { it.id }) { skill ->
                        SkillCard(
                            skill = skill,
                            onToggle = { viewModel.toggleSkill(skill) },
                            onDelete = { viewModel.deleteSkill(skill.id) }
                        )
                    }
                }
            }
        }

        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Amber
            )
        }
    }

    if (state.showAddDialog) {
        AddSkillDialog(
            isLoading = state.isLoading,
            error = state.error,
            onInstallUrl = viewModel::installFromUrl,
            onCreateManual = viewModel::createManualSkill,
            onDismiss = viewModel::hideAddDialog
        )
    }
}

@Composable
private fun SkillCard(skill: Skill, onToggle: () -> Unit, onDelete: () -> Unit) {
    var showDelete by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Surface2)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(skill.name, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                if (!skill.enabled) {
                    Spacer(Modifier.width(8.dp))
                    Text("OFF", style = MaterialTheme.typography.labelSmall, color = TextMuted,
                        modifier = Modifier.background(Surface3, RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp))
                }
            }
            if (skill.description.isNotEmpty()) {
                Spacer(Modifier.height(2.dp))
                Text(skill.description, style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            if (skill.sourceUrl != null) {
                Spacer(Modifier.height(2.dp))
                Text(skill.sourceUrl, style = MaterialTheme.typography.labelSmall,
                    color = TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }

        Switch(
            checked = skill.enabled,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Obsidian,
                checkedTrackColor = Amber,
                uncheckedTrackColor = Surface3
            )
        )
        IconButton(onClick = { showDelete = true }, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.DeleteOutline, null, tint = TextMuted, modifier = Modifier.size(16.dp))
        }
    }

    if (showDelete) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            title = { Text("Delete skill?") },
            text = { Text("\"${skill.name}\" will be removed permanently.") },
            confirmButton = {
                TextButton(onClick = { onDelete(); showDelete = false }) {
                    Text("Delete", color = Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDelete = false }) { Text("Cancel") }
            },
            containerColor = Surface2
        )
    }
}

@Composable
private fun AddSkillDialog(
    isLoading: Boolean,
    error: String?,
    onInstallUrl: (String) -> Unit,
    onCreateManual: (String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var tab by remember { mutableIntStateOf(0) }
    var url by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Surface2,
        title = { Text("Add Skill", color = TextPrimary) },
        text = {
            Column {
                TabRow(selectedTabIndex = tab, containerColor = Surface3, contentColor = Amber) {
                    Tab(selected = tab == 0, onClick = { tab = 0 }) {
                        Text("Install URL", modifier = Modifier.padding(vertical = 8.dp),
                            style = MaterialTheme.typography.labelLarge)
                    }
                    Tab(selected = tab == 1, onClick = { tab = 1 }) {
                        Text("Write manually", modifier = Modifier.padding(vertical = 8.dp),
                            style = MaterialTheme.typography.labelLarge)
                    }
                }
                Spacer(Modifier.height(12.dp))

                if (tab == 0) {
                    OutlinedTextField(
                        value = url, onValueChange = { url = it },
                        label = { Text("SKILL.md URL") },
                        placeholder = { Text("https://example.com/skill/SKILL.md") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = agentTextFieldColors(),
                        singleLine = true
                    )
                } else {
                    OutlinedTextField(value = name, onValueChange = { name = it },
                        label = { Text("Name") }, modifier = Modifier.fillMaxWidth(),
                        colors = agentTextFieldColors(), singleLine = true)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = description, onValueChange = { description = it },
                        label = { Text("Description") }, modifier = Modifier.fillMaxWidth(),
                        colors = agentTextFieldColors(), singleLine = true)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = content, onValueChange = { content = it },
                        label = { Text("Skill content (Markdown)") },
                        modifier = Modifier.fillMaxWidth().height(150.dp),
                        colors = agentTextFieldColors(), maxLines = 10)
                }

                error?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = Red, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (tab == 0) onInstallUrl(url)
                    else onCreateManual(name, description, content)
                },
                enabled = !isLoading && (if (tab == 0) url.isNotBlank() else name.isNotBlank()),
                colors = ButtonDefaults.buttonColors(containerColor = Amber, contentColor = Obsidian)
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Obsidian, strokeWidth = 2.dp)
                else Text(if (tab == 0) "Install" else "Create")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) } }
    )
}

@Composable
private fun EmptySkillsState(onAdd: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🧩", style = MaterialTheme.typography.displayLarge)
        Spacer(Modifier.height(16.dp))
        Text("No skills yet", style = MaterialTheme.typography.headlineMedium, color = TextPrimary)
        Spacer(Modifier.height(8.dp))
        Text("Skills inject capabilities into the agent.\nInstall from a URL or write your own.",
            style = MaterialTheme.typography.bodyMedium, color = TextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onAdd,
            colors = ButtonDefaults.buttonColors(containerColor = Amber, contentColor = Obsidian)) {
            Text("Add first skill")
        }
    }
}

@Composable
fun agentTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Amber,
    unfocusedBorderColor = Border,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    cursorColor = Amber,
    focusedLabelColor = Amber,
    unfocusedLabelColor = TextSecondary
)
