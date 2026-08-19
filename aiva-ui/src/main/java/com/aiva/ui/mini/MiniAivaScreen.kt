package com.aiva.ui.mini

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.input.pointer.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import com.aiva.conversation.ConversationViewModel
import com.aiva.core.task.TaskState

@Composable
fun MiniAivaScreen(
    viewModel: ConversationViewModel,
    onDismiss: () -> Unit,
    onExpand: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val streamingContent by viewModel.streamingContent.collectAsStateWithLifecycle()
    val currentModel by viewModel.currentModel.collectAsStateWithLifecycle()
    
    var inputText by remember { mutableStateOf("") }
    var isListening by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .shadow(Shadow(elevation = 16.dp, shape = RoundedCornerShape(16.dp))),
        shape = RoundedCornerShape(16.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface
        ),
        elevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AIVA",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                )
                
                androidx.compose.foundation.layout.Row(
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(4.dp)
                ) {
                    // Model indicator
                    currentModel?.let { model ->
                        Text(
                            text = model.displayName,
                            fontSize = 10.sp,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    // Expand button
                    androidx.compose.material3.IconButton(onClick = onExpand) {
                        Icon(imageVector = Icons.Default.Fullscreen, contentDescription = "Expand")
                    }
                    
                    // Dismiss button
                    androidx.compose.material3.IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Dismiss")
                    }
                }
            }
            
            // Status indicator
            MiniStatusIndicator(state = state, isListening = isListening)
            
            // Input area
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Voice button
                Button(
                    onClick = { 
                        isListening = !isListening
                        if (isListening) {
                            // TODO: Start voice input
                        } else {
                            // TODO: Stop voice input
                        }
                    },
                    modifier = Modifier.size(44.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = if (isListening || state == TaskState.LISTENING) 
                            androidx.compose.material3.MaterialTheme.colorScheme.error 
                            else androidx.compose.material3.MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice",
                        tint = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary
                    )
                }
                
                // Text input
                TextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Type a command...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
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
                        modifier = Modifier.size(44.dp),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Stop",
                            tint = androidx.compose.material3.MaterialTheme.colorScheme.onError
                        )
                    }
                } else {
                    Button(
                        onClick = { sendMessage() },
                        modifier = Modifier.size(44.dp),
                        enabled = inputText.isNotBlank()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
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
fun MiniStatusIndicator(state: TaskState, isListening: Boolean) {
    val (color, text, icon) = when {
        isListening || state == TaskState.LISTENING -> 
            androidx.compose.material3.MaterialTheme.colorScheme.error to "Listening..." to Icons.Default.Mic
        state == TaskState.UNDERSTANDING -> 
            androidx.compose.material3.MaterialTheme.colorScheme.secondary to "Understanding..." to Icons.Default.Psychology
        state == TaskState.PLANNING -> 
            androidx.compose.material3.MaterialTheme.colorScheme.tertiary to "Planning..." to Icons.Default.Construction
        state == TaskState.OBSERVING -> 
            androidx.compose.material3.MaterialTheme.colorScheme.primary to "Observing screen..." to Icons.Default.Visibility
        state == TaskState.ACTING -> 
            androidx.compose.material3.MaterialTheme.colorScheme.primary to "Acting..." to Icons.Default.PlayArrow
        state == TaskState.VERIFYING -> 
            androidx.compose.material3.MaterialTheme.colorScheme.secondary to "Verifying..." to Icons.Default.Verified
        state == TaskState.SUCCESS -> 
            Color(0xFF4CAF50) to "Done" to Icons.Default.CheckCircle
        state == TaskState.ERROR -> 
            Color(0xFFF44336) to "Error" to Icons.Default.Error
        state == TaskState.STOPPED -> 
            Color(0xFF9E9E9E) to "Stopped" to Icons.Default.StopCircle
        state == TaskState.WAITING -> 
            androidx.compose.material3.MaterialTheme.colorScheme.tertiary to "Waiting..." to Icons.Default.HourglassEmpty
        else -> 
            androidx.compose.material3.MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) to "Ready" to Icons.Default.SmartToy
    }
    
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.foundation.layout.PaddingValues(8.dp, 0.dp, 0.dp, 0.dp))
        Text(
            text = text,
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}