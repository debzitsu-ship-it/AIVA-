package com.aiva.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Swipe
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Info

@Composable
fun AutomationSettingsScreen(onBack: () -> Unit) {
    var accessibilityEnabled by remember { mutableStateOf(false) }
    var overlayEnabled by remember { mutableStateOf(false) }
    var autoScroll by remember { mutableStateOf(true) }
    var scrollSpeed by remember { mutableStateOf(1.0f) }
    var tapDelay by remember { mutableStateOf(100) }
    var swipeDuration by remember { mutableStateOf(300) }
    var maxRetries by remember { mutableStateOf(3) }
    var verifyActions by remember { mutableStateOf(true) }
    var askBeforeHighImpact by remember { mutableStateOf(true) }
    
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Automation", fontWeight = FontWeight.Bold) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } }
        )
        
        // Accessibility Status
        Card(modifier = Modifier.fillMaxWidth().padding(16.dp), colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Column(modifier = Modifier.padding(16.dp)) {
                androidx.compose.foundation.layout.Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = "Info", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Accessibility Service Required", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text("Enable AIVA Accessibility Service in Settings to allow screen reading and automation", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                    }
                }
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = { /* Open accessibility settings */ }) {
                    Text("Open Accessibility Settings")
                }
            }
        }
        
        // Core Permissions
        SettingsSectionCard(title = "Permissions", icon = Icons.Default.Settings) {
            ToggleRow(
                title = "System Overlay (Mini UI)",
                subtitle = "Show floating Mini UI over other apps",
                checked = overlayEnabled,
                onCheckedChange = { overlayEnabled = it }
            )
            
            ToggleRow(
                title = "Accessibility Service",
                subtitle = "Read screen content and perform actions",
                checked = accessibilityEnabled,
                onCheckedChange = { accessibilityEnabled = it }
            )
        }
        
        // Interaction Settings
        SettingsSectionCard(title = "Interaction", icon = Icons.Default.TouchApp) {
            ToggleRow(
                title = "Auto Scroll",
                subtitle = "Automatically scroll when needed",
                checked = autoScroll,
                onCheckedChange = { autoScroll = it }
            )
            
            SliderRow(
                title = "Scroll Speed",
                subtitle = "Scroll animation speed",
                value = scrollSpeed,
                onValueChange = { scrollSpeed = it },
                min = 0.5f,
                max = 3.0f,
                valueFormat = "%.1fx"
            )
            
            SliderRow(
                title = "Tap Delay",
                subtitle = "Delay between taps (ms)",
                value = tapDelay.toFloat(),
                onValueChange = { tapDelay = it.roundToInt() },
                min = 50f,
                max = 500f,
                valueFormat = "%.0fms"
            )
            
            SliderRow(
                title = "Swipe Duration",
                subtitle = "Swipe gesture duration (ms)",
                value = swipeDuration.toFloat(),
                onValueChange = { swipeDuration = it.roundToInt() },
                min = 100f,
                max = 1000f,
                valueFormat = "%.0fms"
            )
        }
        
        // Recovery Settings
        SettingsSectionCard(title = "Recovery & Safety", icon = Icons.Default.Visibility) {
            ToggleRow(
                title = "Verify Actions",
                subtitle = "Confirm each action succeeded before continuing",
                checked = verifyActions,
                onCheckedChange = { verifyActions = it }
            )
            
            ToggleRow(
                title = "Ask Before High-Impact Actions",
                subtitle = "Confirm purchases, sends, deletes, etc.",
                checked = askBeforeHighImpact,
                onCheckedChange = { askBeforeHighImpact = it }
            )
            
            SliderRow(
                title = "Max Retries",
                subtitle = "Retry failed actions this many times",
                value = maxRetries.toFloat(),
                onValueChange = { maxRetries = it.roundToInt() },
                min = 0f,
                max = 10f,
                valueFormat = "%.0f"
            )
        }
        
        // Advanced
        SettingsSectionCard(title = "Advanced", icon = Icons.Default.Speed) {
            Text("Action Timeout: 30s", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))
            Text("Screen Observation: Accessibility + Vision fallback", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))
            Text("Coordinate System: Normalized (0-1) with vision fallback", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))
        }
    }
}

@Composable
fun SettingsSectionCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, content: @Composable () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(16.dp), colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)) {
        Column(modifier = Modifier.padding(16.dp)) {
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, contentDescription = title, tint = MaterialTheme.colorScheme.primary)
                Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun ToggleRow(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
        androidx.compose.material3.Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = androidx.compose.material3.SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
    )
}

@Composable
fun SliderRow(
    title: String,
    subtitle: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    min: Float,
    max: Float,
    valueFormat: String
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
            Text(String.format(valueFormat, value), fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary)
        }
        androidx.compose.material3.Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = min..max,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            colors = androidx.compose.material3.SliderDefaults.colors(
                activeTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}