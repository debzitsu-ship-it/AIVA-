package com.aiva.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.FlashOn
import com.aiva.core.model.ModelInfo
import com.aiva.core.model.SpeedProfile
import com.aiva.core.model.ReasoningLevel

@Composable
fun ModelPickerDialog(
    currentModel: ModelInfo?,
    availableModels: List<ModelInfo>,
    onSelect: (ModelInfo) -> Unit,
    onDismiss: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black.copy(alpha = 0.5f)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .height(500.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Select Model",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                    
                    // Model list
                    androidx.compose.foundation.lazy.LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        androidx.compose.foundation.lazy.items(availableModels) { model ->
                            ModelItem(
                                model = model,
                                isSelected = currentModel?.id == model.id,
                                onClick = { onSelect(model) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ModelItem(
    model: ModelInfo,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val speedIcon = when (model.speedProfile) {
        SpeedProfile.FASTEST -> Icons.Default.FlashOn
        SpeedProfile.FAST -> Icons.Default.Speed
        SpeedProfile.BALANCED -> Icons.Default.Psychology
        SpeedProfile.SLOW -> Icons.Default.Visibility
    }
    
    val reasoningIcon = when (model.reasoningCapability) {
        ReasoningLevel.NONE -> Icons.Default.VisibilityOff
        ReasoningLevel.BASIC -> Icons.Default.Psychology
        ReasoningLevel.ADVANCED -> Icons.Default.Psychology
        ReasoningLevel.EXPERT -> Icons.Default.AutoAwesome
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onClick,
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
                else MaterialTheme.colorScheme.surfaceContainerHighest
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = model.displayName,
                        fontSize = 16.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                    )
                    
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Selected",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Provider badge
                    Badge(text = model.provider, color = MaterialTheme.colorScheme.secondaryContainer)
                    
                    // Speed profile badge
                    Badge(text = model.speedProfile.name, icon = speedIcon, color = MaterialTheme.colorScheme.tertiaryContainer)
                    
                    // Reasoning badge
                    if (model.reasoningCapability != ReasoningLevel.NONE) {
                        Badge(text = model.reasoningCapability.name, icon = reasoningIcon, color = MaterialTheme.colorScheme.primaryContainer)
                    }
                    
                    // Capabilities
                    if (model.agentSuitability) Badge(text = "Agent", color = MaterialTheme.colorScheme.tertiaryContainer)
                    if (model.visionCapability) Badge(text = "Vision", color = MaterialTheme.colorScheme.secondaryContainer)
                    if (model.streamingCapability) Badge(text = "Stream", color = MaterialTheme.colorScheme.primaryContainer)
                }
            }
        }
    }
}

@Composable
fun Badge(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    color: Color
) {
    Row(
        modifier = Modifier
            .height(20.dp)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon?.let {
            androidx.compose.material3.Icon(
                imageVector = it,
                contentDescription = text,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.size(12.dp)
            )
        }
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
    }
}