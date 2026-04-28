package com.agentapp.ui.chat

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.agentapp.data.models.Message
import com.agentapp.data.models.Role
import com.agentapp.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ChatScreen(viewModel: ChatViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    var inputText by remember { mutableStateOf("") }

    // Auto-scroll to bottom on new messages
    LaunchedEffect(state.messages.size, state.streamingText) {
        if (state.messages.isNotEmpty() || state.streamingText.isNotEmpty()) {
            listState.animateScrollToItem(
                (state.messages.size + if (state.streamingText.isNotEmpty()) 1 else 0)
                    .coerceAtLeast(0)
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Obsidian)
    ) {
        // ── Top bar ───────────────────────────────────────────────────────────
        ChatTopBar(
            providerLabel = state.providerLabel,
            onClear = viewModel::clearSession
        )

        // ── Message list ──────────────────────────────────────────────────────
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (state.messages.isEmpty() && state.streamingText.isEmpty()) {
                item { EmptyState() }
            }

            items(state.messages, key = { it.id }) { message ->
                MessageBubble(message = message)
            }

            // Streaming bubble
            if (state.streamingText.isNotEmpty()) {
                item {
                    StreamingBubble(text = state.streamingText)
                }
            }

            // Loading indicator
            if (state.isLoading && state.streamingText.isEmpty()) {
                item { ThinkingIndicator() }
            }
        }

        // ── Error banner ──────────────────────────────────────────────────────
        AnimatedVisibility(visible = state.error != null) {
            state.error?.let { error ->
                ErrorBanner(message = error, onDismiss = viewModel::dismissError)
            }
        }

        // ── Input bar ─────────────────────────────────────────────────────────
        ChatInputBar(
            value = inputText,
            onValueChange = { inputText = it },
            onSend = {
                viewModel.sendMessage(inputText)
                inputText = ""
            },
            isLoading = state.isLoading
        )
    }
}

@Composable
private fun ChatTopBar(providerLabel: String, onClear: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface1)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Agent status dot
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(Green)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            "Agent",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            modifier = Modifier.weight(1f)
        )
        if (providerLabel.isNotEmpty()) {
            Text(
                providerLabel,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
            Spacer(Modifier.width(8.dp))
        }
        IconButton(onClick = onClear, modifier = Modifier.size(32.dp)) {
            Icon(
                Icons.Default.DeleteOutline,
                contentDescription = "Clear chat",
                tint = TextMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
    HorizontalDivider(color = Border, thickness = 0.5.dp)
}

@Composable
private fun MessageBubble(message: Message) {
    val isUser = message.role == Role.USER
    val timeStr = remember(message.timestamp) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp))
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            AgentAvatar()
            Spacer(Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier.widthIn(max = 300.dp),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = if (isUser) 16.dp else 4.dp,
                            topEnd = if (isUser) 4.dp else 16.dp,
                            bottomStart = 16.dp,
                            bottomEnd = 16.dp
                        )
                    )
                    .background(if (isUser) AmberDim else Surface2)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isUser) AmberGlow else TextPrimary,
                    lineHeight = 20.sp
                )
            }
            Spacer(Modifier.height(2.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(timeStr, style = MaterialTheme.typography.labelSmall, color = TextMuted)
                if (!isUser && message.providerUsed != null) {
                    Text("·", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Text(
                        message.providerUsed,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
            }
        }

        if (isUser) Spacer(Modifier.width(8.dp))
    }
}

@Composable
private fun StreamingBubble(text: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        AgentAvatar()
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp))
                .background(Surface2)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = "$text▊",
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun ThinkingIndicator() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AgentAvatar()
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp))
                .background(Surface2)
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                repeat(3) { i ->
                    val infiniteTransition = rememberInfiniteTransition(label = "dot$i")
                    val alpha by infiniteTransition.animateFloat(
                        initialValue = 0.2f, targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = androidx.compose.animation.core.tween(600, delayMillis = i * 150),
                            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
                        ), label = "alpha$i"
                    )
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Amber.copy(alpha = alpha))
                    )
                }
            }
        }
    }
}

@Composable
private fun AgentAvatar() {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(listOf(Amber, AmberDim))
            ),
        contentAlignment = Alignment.Center
    ) {
        Text("A", style = MaterialTheme.typography.labelMedium, color = Obsidian)
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("⚡", fontSize = 40.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        Text(
            "Agent ready",
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Type a message to start.\nHeartbeat and scheduled jobs run in the background.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
    }
}

@Composable
private fun ErrorBanner(message: String, onDismiss: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Red.copy(alpha = 0.15f))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Red, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(message, style = MaterialTheme.typography.bodySmall, color = Red, modifier = Modifier.weight(1f))
        IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = Red, modifier = Modifier.size(14.dp))
        }
    }
}

@Composable
private fun ChatInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    isLoading: Boolean
) {
    HorizontalDivider(color = Border, thickness = 0.5.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface1)
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .navigationBarsPadding()
            .imePadding(),
        verticalAlignment = Alignment.Bottom
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            placeholder = {
                Text("Message", style = MaterialTheme.typography.bodyMedium, color = TextMuted)
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { if (!isLoading) onSend() }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Amber,
                unfocusedBorderColor = Border,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                cursorColor = Amber,
                focusedContainerColor = Surface2,
                unfocusedContainerColor = Surface2
            ),
            shape = RoundedCornerShape(12.dp),
            maxLines = 5,
            textStyle = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.width(8.dp))
        FilledIconButton(
            onClick = onSend,
            enabled = value.isNotBlank() && !isLoading,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = Amber,
                disabledContainerColor = BorderBright
            ),
            modifier = Modifier.size(48.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = Obsidian,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    Icons.Default.ArrowUpward,
                    contentDescription = "Send",
                    tint = Obsidian,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
