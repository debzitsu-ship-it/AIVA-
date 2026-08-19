package com.aiva.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.spacer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.MergeType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.width
import androidx.compose.ui.unit.dp
import com.aiva.conversation.ConversationViewModel
import com.aiva.core.model.ChatMessage
import com.aiva.core.task.TaskState
import com.aiva.ui.component.AiModeDropdown
import com.aiva.ui.component.FusionToggle

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
    
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "AIVA",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface
            ),
            navigationIcon = {
                IconButton(onClick = { showSettings = true }) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings"
                    )
                }
            },
            actions = {
                // AI Mode selector
                AiModeDropdown(
                    currentMode = aiMode,
                    onModeChange = { viewModel.setAiMode(it) },
                    expanded = showAiModeMenu,
                    onExpandChange = { showAiModeMenu = it }
                )
                
                // Fusion toggle
                FusionToggle(
                    enabled = useFusion,
                    onChange = { viewModel.setUseFusion(it) }
                )
                
                // Model picker
                IconButton(onClick = { showModelPicker = true }) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Model Settings"
                    )
                }
            }
        )
        
        // Model indicator
        currentModel?.let { model ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp, 8.dp),
                colors = androidx.compose.material3.CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${model.displayName} (${model.speedProfile.name})",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 12.sp
                    )
                    
                    // Voice status indicator
                    Box(
                        modifier = Modifier.size(8.dp)
                            .graphicsLayer { scaleX = 1f; scaleY = 1f }
                            .background(
                                color = when {
                                    isListening || voiceState == com.aiva.core.voice.VoiceState.LISTENING -> Color(0xFFEF5350)
                                    voiceState == com.aiva.core.voice.VoiceState.SPEAKING -> Color(0xFF81C784)
                                    else -> MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.4f)
                                },
                                shape = androidx.compose.foundation.shape.CircleShape
                            )
                    )
                }
            }
        }
        
        // State indicator
        when (state) {
            TaskState.LISTENING, TaskState.UNDERSTANDING, TaskState.PLANNING,
            TaskState.OBSERVING, TaskState.ACTING, TaskState.VERIFYING -> {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp, 8.dp),
                    colors = androidx.compose.material3.CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        AnimatedStateIcon(state = state)
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AIVA is ${state.name.lowercase().replace("_", " ")}...",
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            fontSize = 12.sp
                        )
                    }
                }
            }
            TaskState.ERROR -> {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp, 8.dp),
                    colors = androidx.compose.material3.CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = "Error occurred. Tap to retry.",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontSize = 12.sp
                    )
                }
            }
            TaskState.STOPPED -> {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp, 8.dp),
                    colors = androidx.compose.material3.CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Text(
                        text = "Stopped by user",
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontSize = 12.sp
                    )
                }
            }
        }
        
        // Messages list
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            reverseLayout = true,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp, 16.dp)
        ) {
            items(messages.reversed()) { message ->
                MessageBubble(message = message, isStreaming = streamingContent.isNotEmpty())
            }
        }
        
        // Streaming indicator
        if (streamingContent.isNotEmpty() && state == TaskState.ACTING) {
            Text(
                text = "▌",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 16.sp,
                modifier = Modifier.padding(16.dp, 0.dp, 16.dp, 8.dp)
            )
        }
        
        // Input area
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Voice and text input row
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Voice button
                Button(
                    onClick = { 
                        if (isListening) {
                            viewModel.voiceViewModel.stopListening()
                        } else {
                            viewModel.voiceViewModel.startListening { text ->
                                // Handled by voiceViewModel observer
                            }
                        }
                    },
                    modifier = Modifier.size(48.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = if (isListening || voiceState == com.aiva.core.voice.VoiceState.LISTENING) 
                            MaterialTheme.colorScheme.error 
                            else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = if (isListening) "Stop Listening" else "Voice Input",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                
                // Text input
                TextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Type a command...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    singleLine = true,
                    keyboardOptions = androidx.compose.ui.text.input.KeyboardOptions(
                        imeAction = androidx.compose.ui.text.input.ImeAction.Send
                    ),
                    keyboardActions = androidx.compose.ui.text.input.KeyboardActions(
                        onDone = { sendMessage() }
                    )
                )
                
                // Send/Stop button
                if (state == TaskState.ACTING || state == TaskState.PLANNING || state == TaskState.OBSERVING) {
                    Button(
                        onClick = { viewModel.stopGeneration() },
                        modifier = Modifier.size(48.dp),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Stop",
                            tint = MaterialTheme.colorScheme.onError
                        )
                    }
                } else {
                    Button(
                        onClick = { sendMessage() },
                        modifier = Modifier.size(48.dp),
                        enabled = inputText.isNotBlank()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
        
        // Settings dialog
        if (showSettings) {
            SettingsDialog(onDismiss = { showSettings = false })
        }
        
        // Model picker dialog
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
    
    fun sendMessage() {
        if (inputText.isNotBlank()) {
            val text = inputText
            inputText = ""
            viewModel.sendMessage(text)
        }
    }
}

@Composable
fun AnimatedStateIcon(state: TaskState) {
    val (icon, color) = when (state) {
        TaskState.LISTENING -> Icons.Default.Mic to Color(0xFFEF5350)
        TaskState.UNDERSTANDING -> Icons.Default.Psychology to Color(0xFFFFB74D)
        TaskState.PLANNING -> Icons.Default.Construction to Color(0xFFFFB74D)
        TaskState.OBSERVING -> Icons.Default.Visibility to Color(0xFF4FC3F7)
        TaskState.ACTING -> Icons.Default.PlayArrow to Color(0xFF81C784)
        TaskState.VERIFYING -> Icons.Default.Verified to Color(0xFFBA68C8)
        TaskState.SUCCESS -> Icons.Default.CheckCircle to Color(0xFF4CAF50)
        TaskState.ERROR -> Icons.Default.Error to Color(0xFFF44336)
        TaskState.STOPPED -> Icons.Default.StopCircle to Color(0xFF9E9E9E)
        TaskState.WAITING -> Icons.Default.HourglassEmpty to Color(0xFFFFB74D)
        else -> Icons.Default.SmartToy to MaterialTheme.colorScheme.primary
    }
    
    Icon(
        imageVector = icon,
        contentDescription = state.name,
        tint = color,
        modifier = Modifier.size(16.dp)
    )
}

@Composable
fun MessageBubble(message: ChatMessage, isStreaming: Boolean) {
    val isUser = message.role == "user"
    val isAssistant = message.role == "assistant"
    
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            colors = androidx.compose.material3.CardDefaults.cardColors(
                containerColor = if (isUser) MaterialTheme.colorScheme.primary 
                    else MaterialTheme.colorScheme.surfaceContainerHighest
            ),
            modifier = Modifier.widthIn(min = 0.dp, max = 280.dp)
        ) {
            Text(
                text = message.content ?: "",
                color = if (isUser) MaterialTheme.colorScheme.onPrimary 
                    else MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}