package com.agentapp.ui.scheduler

import androidx.compose.animation.AnimatedVisibility
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
import com.agentapp.data.models.JobStatus
import com.agentapp.data.models.ScheduledJob
import com.agentapp.ui.skills.agentTextFieldColors
import com.agentapp.ui.theme.*

@Composable
fun SchedulerScreen(viewModel: SchedulerViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(Obsidian)) {
        LazyColumn(contentPadding = PaddingValues(bottom = 80.dp)) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().background(Surface1)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Scheduler", style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary, modifier = Modifier.weight(1f))
                    FilledTonalButton(
                        onClick = viewModel::showAddDialog,
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = AmberDim)
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp), tint = AmberGlow)
                        Spacer(Modifier.width(4.dp))
                        Text("Add job", color = AmberGlow, style = MaterialTheme.typography.labelLarge)
                    }
                }
                HorizontalDivider(color = Border, thickness = 0.5.dp)
            }

            // Heartbeat section
            item {
                HeartbeatSection(
                    enabled = state.heartbeatEnabled,
                    intervalMinutes = state.heartbeatIntervalMinutes,
                    heartbeatMd = state.heartbeatMd,
                    activeHoursStart = state.activeHoursStart,
                    activeHoursEnd = state.activeHoursEnd,
                    onToggle = viewModel::setHeartbeatEnabled,
                    onIntervalChange = viewModel::setHeartbeatInterval,
                    onMdChange = viewModel::setHeartbeatMd,
                    onActiveHoursChange = viewModel::setActiveHours
                )
            }

            // Cron jobs section
            item {
                Text(
                    "CRON JOBS",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 8.dp)
                )
            }

            if (state.jobs.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center) {
                        Text("No scheduled jobs yet.\nTap 'Add job' to create one.",
                            style = MaterialTheme.typography.bodyMedium, color = TextMuted,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }
            } else {
                items(state.jobs, key = { it.id }) { job ->
                    JobCard(
                        job = job,
                        onToggle = { viewModel.toggleJob(job) },
                        onDelete = { viewModel.deleteJob(job.id) },
                        onRunNow = { viewModel.runNow(job) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)
                    )
                }
            }
        }
    }

    if (state.showAddDialog) {
        AddJobDialog(
            onAdd = viewModel::addCronJob,
            onDismiss = viewModel::hideAddDialog
        )
    }
}

@Composable
private fun HeartbeatSection(
    enabled: Boolean,
    intervalMinutes: Int,
    heartbeatMd: String,
    activeHoursStart: Int,
    activeHoursEnd: Int,
    onToggle: (Boolean) -> Unit,
    onIntervalChange: (Int) -> Unit,
    onMdChange: (String) -> Unit,
    onActiveHoursChange: (Int, Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val intervals = listOf(15, 30, 60, 120, 240)

    Column(
        modifier = Modifier.padding(16.dp).clip(RoundedCornerShape(12.dp))
            .background(Surface2).padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(8.dp).clip(androidx.compose.foundation.shape.CircleShape)
                            .background(if (enabled) Green else TextMuted)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Heartbeat", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    if (enabled) "Checking every $intervalMinutes min · ${activeHoursStart}:00–${activeHoursEnd}:00"
                    else "Paused",
                    style = MaterialTheme.typography.bodySmall, color = TextSecondary
                )
            }
            IconButton(onClick = { expanded = !expanded }) {
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    null, tint = TextSecondary
                )
            }
            Switch(
                checked = enabled, onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Obsidian, checkedTrackColor = Amber,
                    uncheckedTrackColor = Surface3
                )
            )
        }

        AnimatedVisibility(visible = expanded) {
            Column {
                HorizontalDivider(color = Border, modifier = Modifier.padding(vertical = 12.dp))

                // Interval selector
                Text("Check interval", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    intervals.forEach { mins ->
                        val selected = intervalMinutes == mins
                        FilterChip(
                            selected = selected,
                            onClick = { onIntervalChange(mins) },
                            label = {
                                Text(
                                    if (mins < 60) "${mins}m" else "${mins / 60}h",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AmberDim,
                                selectedLabelColor = AmberGlow,
                                containerColor = Surface3,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Active hours
                Text("Active hours", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    HourPicker("From", activeHoursStart) { onActiveHoursChange(it, activeHoursEnd) }
                    Text("–", color = TextSecondary)
                    HourPicker("To", activeHoursEnd) { onActiveHoursChange(activeHoursStart, it) }
                }

                Spacer(Modifier.height(12.dp))

                // HEARTBEAT.md
                Text("HEARTBEAT.md checklist", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = heartbeatMd,
                    onValueChange = onMdChange,
                    placeholder = { Text("- Check if any emails need urgent replies\n- Review today's calendar\n- ...", color = TextMuted) },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    colors = agentTextFieldColors(),
                    textStyle = MaterialTheme.typography.bodySmall,
                    maxLines = 10
                )
            }
        }
    }
}

@Composable
private fun HourPicker(label: String, value: Int, onSelect: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(
            onClick = { expanded = true },
            border = BorderStroke(0.5.dp, Border),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
        ) {
            Text("${value.toString().padStart(2, '0')}:00",
                style = MaterialTheme.typography.bodyMedium)
            Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(16.dp))
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Surface2).heightIn(max = 200.dp)
        ) {
            (0..23).forEach { hour ->
                DropdownMenuItem(
                    text = { Text("${hour.toString().padStart(2, '0')}:00", color = if (hour == value) Amber else TextPrimary) },
                    onClick = { onSelect(hour); expanded = false }
                )
            }
        }
    }
}

@Composable
private fun JobCard(
    job: ScheduledJob,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onRunNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDelete by remember { mutableStateOf(false) }
    val isActive = job.status == JobStatus.ACTIVE

    Column(
        modifier = modifier.clip(RoundedCornerShape(10.dp))
            .background(Surface2).padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(job.name, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Spacer(Modifier.height(2.dp))
                Text(
                    "Every ${if (job.intervalMinutes < 60) "${job.intervalMinutes}m" else "${job.intervalMinutes/60}h"}" +
                        " · ran ${job.runCount}×",
                    style = MaterialTheme.typography.bodySmall, color = TextSecondary
                )
            }
            // Run now button
            IconButton(onClick = onRunNow, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.PlayArrow, "Run now", tint = Green, modifier = Modifier.size(18.dp))
            }
            // Toggle
            Switch(
                checked = isActive, onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Obsidian, checkedTrackColor = Amber,
                    uncheckedTrackColor = Surface3
                )
            )
            // Delete
            IconButton(onClick = { showDelete = true }, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.DeleteOutline, null, tint = TextMuted, modifier = Modifier.size(16.dp))
            }
        }

        // Prompt preview
        Spacer(Modifier.height(6.dp))
        Text(
            job.prompt,
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.clip(RoundedCornerShape(6.dp))
                .background(Surface3).padding(8.dp)
        )
    }

    if (showDelete) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            containerColor = Surface2,
            title = { Text("Delete job?", color = TextPrimary) },
            text = { Text("\"${job.name}\" will be removed.", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = { onDelete(); showDelete = false }) {
                    Text("Delete", color = Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDelete = false }) { Text("Cancel", color = TextSecondary) }
            }
        )
    }
}

@Composable
private fun AddJobDialog(
    onAdd: (String, String, Int, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var prompt by remember { mutableStateOf("") }
    var intervalMinutes by remember { mutableIntStateOf(60) }
    var notifyOnResult by remember { mutableStateOf(true) }
    val intervals = listOf(15 to "15 min", 30 to "30 min", 60 to "1 hour",
        120 to "2 hours", 240 to "4 hours", 480 to "8 hours", 1440 to "Daily")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Surface2,
        title = { Text("New scheduled job", color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it },
                    label = { Text("Job name") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth(), colors = agentTextFieldColors())

                OutlinedTextField(value = prompt, onValueChange = { prompt = it },
                    label = { Text("Prompt / task") },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    colors = agentTextFieldColors(), maxLines = 5)

                // Interval picker
                Text("Run every", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(intervals.size) { i ->
                        val (mins, label) = intervals[i]
                        FilterChip(
                            selected = intervalMinutes == mins,
                            onClick = { intervalMinutes = mins },
                            label = { Text(label, style = MaterialTheme.typography.labelMedium) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AmberDim,
                                selectedLabelColor = AmberGlow,
                                containerColor = Surface3,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Notify on result", style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary, modifier = Modifier.weight(1f))
                    Switch(
                        checked = notifyOnResult, onCheckedChange = { notifyOnResult = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Obsidian, checkedTrackColor = Amber,
                            uncheckedTrackColor = Surface3
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onAdd(name, prompt, intervalMinutes, notifyOnResult) },
                enabled = name.isNotBlank() && prompt.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Amber, contentColor = Obsidian)
            ) { Text("Create") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        }
    )
}
