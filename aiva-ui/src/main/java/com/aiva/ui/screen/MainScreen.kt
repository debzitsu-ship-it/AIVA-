@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.aiva.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aiva.conversation.ConversationViewModel
import com.aiva.core.model.ChatMessage
import com.aiva.core.task.TaskState
import com.aiva.core.voice.VoiceState
import com.aiva.ui.component.AiModeDropdown
import com.aiva.ui.component.FusionToggle
import com.aiva.ui.component.ModelPickerDialog
import com.aiva.ui.component.SettingsDialog

@Composable
fun MainScreen(viewModel: ConversationViewModel) {
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val streamingContent by viewModel.streamingContent.collectAsStateWithLifecycle()
    val currentModel by viewModel.currentModel.collectAsStateWithLifecycle()
    val availableModels by viewModel.availableModels.collectAsStateWithLifecycle()
    val aiMode by viewModel.aiMode.collectAsStateWithLifecycle()
    val useFusion by viewModel.useFusion.collectAsStateWithLifecycle()
    val voiceState by viewModel.voiceViewModel.voiceState.collectAsStateWithLifecycle()
    val isListening by viewModel.voiceViewModel.isListening.collectAsStateWithLifecycle()

    var inputText by remember { mutableStateOf("") }
    var showModelPicker by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showAiModeMenu by remember { mutableStateOf(false) }

    fun sendMessage() {
        if (inputText.isNotBlank()) {
            val text = inputText
            inputText = ""
            viewModel.sendMessage(text)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(text = "AIVA", fontWeight = FontWeight.Bold)
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            navigationIcon = {
                IconButton(onClick = { showSettings = true }) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings")
                }
            },
            actions = {
                AiModeDropdown(
                    currentMode = aiMode,
                    onModeChange = { viewModel.setAiMode(it) },
                    expanded = showAiModeMenu,
                    onExpandChange = { showAiModeMenu = it }
                )
                FusionToggle(
                    enabled = useFusion,
                    onChange = { viewModel.setUseFusion(it) }
                )
                IconButton(onClick = { showModelPicker = true }) {
                    Icon(Icons.Default.Tune, contentDescription = "Model Settings")
                }
            }
        )

        currentModel?.let { model ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp, 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${model.displayName} (${model.speedProfile.name})",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 12.sp
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = when {
                                    isListening || voiceState == VoiceState.LISTENING -> Color(0xFFEF5350)
                                    voiceState == VoiceState.SPEAKING -> Color(0xFF81C784)
                                    else -> MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.4f)
                                },
                                shape = CircleShape
                            )
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            reverseLayout = true,
            contentPadding = PaddingValues(0.dp, 16.dp)
        ) {
            items(messages.reversed()) { message ->
                MessageBubble(message = message)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    if (isListening) {
                        viewModel.voiceViewModel.stopListening()
                    } else {
                        viewModel.voiceViewModel.startListening { }
                    }
                },
                modifier = Modifier.size(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isListening) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Default.Mic, contentDescription = "Voice")
            }

            TextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("Type a command...") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )

            Button(
                onClick = {
                    if (state == TaskState.ACTING || state == TaskState.PLANNING) {
                        viewModel.stopGeneration()
                    } else {
                        sendMessage()
                    }
                },
                modifier = Modifier.size(48.dp),
                enabled = state == TaskState.ACTING || inputText.isNotBlank()
            ) {
                Icon(
                    imageVector = if (state == TaskState.ACTING) Icons.Default.Stop else Icons.Default.Send,
                    contentDescription = "Send"
                )
            }
        }
    }

    if (showSettings) {
        SettingsDialog(onDismiss = { showSettings = false })
    }
    if (showModelPicker) {
        ModelPickerDialog(
            currentModel = currentModel,
            availableModels = availableModels,
            onSelect = { model ->
                viewModel.setModel(model)
                showModelPicker = false
            },
            onDismiss = { showModelPicker = false }
        )
    }
}

@Composable
fun AnimatedStateIcon(state: TaskState) {
    val (icon, color) = when (state) {
        TaskState.LISTENING -> Icons.Default.Mic to Color(0xFFEF5350)
        TaskState.UNDERSTANDING -> Icons.Default.Psychology to Color(0xFFFFB74D)
        TaskState.PLANNING -> Icons.Default.Build to Color(0xFFFFB74D)
        TaskState.OBSERVING -> Icons.Default.Visibility to Color(0xFF4FC3F7)
        TaskState.ACTING -> Icons.Default.PlayArrow to Color(0xFF81C784)
        TaskState.VERIFYING -> Icons.Default.Verified to Color(0xFFBA68C8)
        TaskState.SUCCESS -> Icons.Default.CheckCircle to Color(0xFF4CAF50)
        TaskState.ERROR -> Icons.Default.Error to Color(0xFFF44336)
        TaskState.STOPPED -> Icons.Default.StopCircle to Color(0xFF9E9E9E)
        TaskState.WAITING -> Icons.Default.HourglassEmpty to Color(0xFFFFB74D)
        else -> Icons.Default.SmartToy to MaterialTheme.colorScheme.primary
    }
    Icon(imageVector = icon, contentDescription = state.name, tint = color, modifier = Modifier.size(16.dp))
}

@Composable
fun MessageBubble(message: ChatMessage, isStreaming: Boolean = false) {
    val isUser = message.role == "user"
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier.widthIn(min = 0.dp, max = 280.dp)
        ) {
            Text(
                text = message.content.orEmpty(),
                color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}
