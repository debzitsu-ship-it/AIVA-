package com.aiva.ui.pip

import android.app.Activity
import android.app.PictureInPictureParams
import android.content.res.Configuration
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.util.Rational
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.aiva.conversation.ConversationViewModel
import com.aiva.core.task.TaskState
import com.aiva.ui.theme.AivaTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PipActivity : ComponentActivity() {
    
    private val viewModel: ConversationViewModel by androidx.activity.viewModels()
    private var isInPipMode = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable PiP
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setPictureInPictureParams(
                PictureInPictureParams.Builder()
                    .setAspectRatio(Rational(1, 1))
                    .setActions(
                        arrayOf(
                            android.app.RemoteAction(
                                android.graphics.drawable.Icon.createWithResource(this, android.R.drawable.ic_media_play),
                                "Expand",
                                "Expand AIVA",
                                android.app.PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), android.app.PendingIntent.FLAG_IMMUTABLE)
                            )
                        )
                    )
                    .build()
            )
        }
        
        setContent {
            AivaTheme {
                PipScreen(viewModel = viewModel)
            }
        }
    }
    
    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        isInPipMode = isInPictureInPictureMode
        
        if (isInPictureInPictureMode) {
            // Entered PiP - minimize UI
            lifecycleScope.launch {
                viewModel.stopGeneration()
            }
        } else {
            // Exited PiP - could restore full UI
        }
    }
    
    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        // Enter PiP when user presses home/back
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            enterPictureInPictureMode(
                PictureInPictureParams.Builder()
                    .setAspectRatio(Rational(1, 1))
                    .build()
            )
        }
    }
    
    companion object {
        fun start(activity: Activity) {
            val intent = android.content.Intent(activity, PipActivity::class.java)
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            activity.startActivity(intent)
        }
    }
}

@Composable
fun PipScreen(viewModel: ConversationViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currentModel by viewModel.currentModel.collectAsStateWithLifecycle()
    
    var isExpanded by remember { mutableStateOf(false) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { isExpanded = !isExpanded }
                )
            }
            .background(Color.Transparent)
    ) {
        // Compact circular indicator when not expanded
        if (!isExpanded) {
            CompactPipIndicator(state = state, model = currentModel)
        } else {
            // Expanded mini UI
            ExpandedPipUI(state = state, model = currentModel, onCollapse = { isExpanded = false })
        }
    }
}

@Composable
fun CompactPipIndicator(
    state: TaskState,
    model: com.aiva.core.model.ModelInfo?
) {
    val (color, label) = when (state) {
        TaskState.IDLE -> Color(0xFF64B5F6) to "AIVA"
        TaskState.LISTENING -> Color(0xFFEF5350) to "●"
        TaskState.UNDERSTANDING, TaskState.PLANNING -> Color(0xFFFFB74D) to "⟳"
        TaskState.OBSERVING -> Color(0xFF4FC3F7) to "👁"
        TaskState.ACTING -> Color(0xFF81C784) to "▶"
        TaskState.VERIFYING -> Color(0xFFBA68C8) to "✓"
        TaskState.SUCCESS -> Color(0xFF4CAF50) to "✓"
        TaskState.ERROR -> Color(0xFFF44336) to "✗"
        TaskState.STOPPED -> Color(0xFF9E9E9E) to "■"
        TaskState.WAITING -> Color(0xFFFFB74D) to "⏳"
    }
    
    Card(
        modifier = Modifier.size(80.dp),
        shape = RoundedCornerShape(40.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.9f)
        ),
        elevation = 8.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ExpandedPipUI(
    state: TaskState,
    model: com.aiva.core.model.ModelInfo?,
    onCollapse: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Header
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
        ) {
            Text(
                text = "AIVA ${model?.displayName ?: ""}",
                fontWeight = FontWeight.Bold,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
            )
            androidx.compose.material3.IconButton(onClick = onCollapse) {
                androidx.compose.material3.Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.Close,
                    contentDescription = "Collapse"
                )
            }
        }
        
        // Status
        Text(
            text = state.name.lowercase().replace("_", " "),
            color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
            fontSize = 12.sp
        )
        
        // Quick actions
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
        ) {
            QuickActionButton(icon = androidx.compose.material.icons.Icons.Default.Mic, label = "Voice") { /* TODO */ }
            QuickActionButton(icon = androidx.compose.material.icons.Icons.Default.Keyboard, label = "Type") { /* TODO */ }
            QuickActionButton(icon = androidx.compose.material.icons.Icons.Default.Stop, label = "Stop") { /* TODO */ }
            QuickActionButton(icon = androidx.compose.material.icons.Icons.Default.Fullscreen, label = "Expand") { /* TODO */ }
        }
    }
}

@Composable
fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    androidx.compose.material3.Card(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .height(56.dp),
        onClick = onClick
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = androidx.compose.material3.MaterialTheme.colorScheme.primary)
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = 4.dp))
            Text(text = label, fontSize = 10.sp, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface)
        }
    }
}