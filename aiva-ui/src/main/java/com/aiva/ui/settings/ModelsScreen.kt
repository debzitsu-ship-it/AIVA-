package com.aiva.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Chip
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsStateWithLifecycle
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Info
import com.aiva.ai.registry.ModelRegistry
import com.aiva.core.model.ModelInfo
import com.aiva.core.model.ReasoningLevel
import com.aiva.core.model.SpeedProfile
import com.aiva.security.ApiKeyManager
import dagger.hilt.android.AndroidEntryPoint

@Composable
fun ModelsScreen(onBack: () -> Unit) {
    val modelRegistry: ModelRegistry = remember { androidx.hilt.navigation.compose.hiltViewModel() }
    val apiKeyManager: ApiKeyManager = remember { androidx.hilt.navigation.compose.hiltViewModel() }
    
    val allModels = modelRegistry.getAllModels()
    val keys = apiKeyManager.getAllKeys()
    val enabledModelIds = keys.flatMap { it.models }.toSet()
    
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        TopAppBar(
            title = { Text("Models", fontWeight = FontWeight.Bold) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } }
        )
        
        // AI Mode selector
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("AI Mode", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text("Choose how AIVA selects models for your requests", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AiModeChip(mode = "AUTO", label = "Auto", icon = Icons.Default.FlashOn, description = "Automatically selects best model")
                    AiModeChip(mode = "FASTEST", label = "Fastest", icon = Icons.Default.Speed, description = "Prioritizes speed")
                    AiModeChip(mode = "BEST_REASONING", label = "Best Reasoning", icon = Icons.Default.Psychology, description = "Uses most capable models")
                    AiModeChip(mode = "FUSION", label = "Multi-Model Fusion", icon = Icons.Default.MergeType, description = "Runs multiple models in parallel")
                    AiModeChip(mode = "AGENT", label = "Agent", icon = Icons.Default.AutoAwesome, description = "For automation tasks")
                }
            }
        }
        
        // Models list
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp, 8.dp, 16.dp, 100.dp)
        ) {
            items(allModels) { model ->
                ModelSettingsCard(
                    model = model,
                    isEnabled = enabledModelIds.contains(model.id),
                    onToggle = { enabled ->
                        // TODO: Enable/disable model for all keys that support it
                    }
                )
            }
        }
    }
}

@Composable
fun AiModeChip(mode: String, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, description: String) {
    val selected by remember { mutableStateOf(false) }
    
    Chip(
        onClick = { /* TODO */ },
        modifier = Modifier.weight(1f).fillMaxWidth().height(80.dp),
        colors = androidx.compose.material3.ChipDefaults.colors(
            containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest
        ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = label, tint = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(4.dp))
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
            Text(description, fontSize = 10.sp, color = if (selected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), textAlign = androidx.compose.ui.text.TextAlign.Center, maxLines = 2)
        }
    }
}

@Composable
fun ModelSettingsCard(model: ModelInfo, isEnabled: Boolean, onToggle: (Boolean) -> Unit) {
    val speedIcon = when (model.speedProfile) {
        SpeedProfile.FASTEST -> Icons.Default.FlashOn
        SpeedProfile.FAST -> Icons.Default.Speed
        SpeedProfile.BALANCED -> Icons.Default.Psychology
        SpeedProfile.SLOW -> Icons.Default.VisibilityOff
    }
    
    val reasoningIcon = when (model.reasoningCapability) {
        ReasoningLevel.NONE -> Icons.Default.VisibilityOff
        ReasoningLevel.BASIC -> Icons.Default.Psychology
        ReasoningLevel.ADVANCED -> Icons.Default.Psychology
        ReasoningLevel.EXPERT -> Icons.Default.AutoAwesome
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = if (isEnabled) MaterialTheme.colorScheme.surfaceContainerHighest else MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                androidx.compose.foundation.layout.Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(model.displayName, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(model.provider, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
                
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CapabilityBadge(text = model.speedProfile.name, icon = speedIcon)
                    CapabilityBadge(text = model.reasoningCapability.name, icon = reasoningIcon)
                    if (model.agentSuitability) CapabilityBadge(text = "Agent", icon = Icons.Default.SmartToy)
                    if (model.visionCapability) CapabilityBadge(text = "Vision", icon = Icons.Default.Visibility)
                    if (model.streamingCapability) CapabilityBadge(text = "Stream", icon = Icons.Default.Sync)
                }
            }
            
            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle,
                modifier = Modifier.padding(start = 16.dp),
                colors = androidx.compose.material3.SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}

@Composable
fun CapabilityBadge(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .height(20.dp)
            .padding(horizontal = 8.dp)
            .background(MaterialTheme.colorScheme.tertiaryContainer, androidx.compose.foundation.shape.RoundedCornerShape(10.dp)),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = text, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), modifier = Modifier.size(12.dp))
        Text(text, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
    }
}