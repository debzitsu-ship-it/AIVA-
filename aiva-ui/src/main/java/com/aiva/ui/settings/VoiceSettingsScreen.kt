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
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.VoiceOver
import androidx.compose.material.icons.filled.Tune

@Composable
fun VoiceSettingsScreen(onBack: () -> Unit) {
    // Voice config would come from ApiKeyManager/UserPreferences
    var asrEnabled by remember { mutableStateOf(true) }
    var ttsEnabled by remember { mutableStateOf(true) }
    var autoSpeak by remember { mutableStateOf(false) }
    var interruptOnSpeech by remember { mutableStateOf(true) }
    var language by remember { mutableStateOf("en-US") }
    var voiceName by remember { mutableStateOf("Chatterbox-Multilingual.en-US.Male") }
    var speechSpeed by remember { mutableStateOf(1.0f) }
    var volume by remember { mutableStateOf(1.0f) }
    var vadSensitivity by remember { mutableStateOf(0.5f) }
    
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        TopAppBar(
            title = { Text("Voice Settings", fontWeight = FontWeight.Bold) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } }
        )
        
        // ASR Settings
        SettingsSectionCard(title = "Speech Recognition", icon = Icons.Default.Mic) {
            ToggleRow(
                title = "Enable Speech Recognition",
                subtitle = "Use microphone for voice input",
                checked = asrEnabled,
                onCheckedChange = { asrEnabled = it }
            )
            
            ToggleRow(
                title = "Interrupt on User Speech",
                subtitle = "Stop TTS when user starts speaking",
                checked = interruptOnSpeech,
                onCheckedChange = { interruptOnSpeech = it }
            )
            
            SliderRow(
                title = "VAD Sensitivity",
                subtitle = "Voice activity detection threshold",
                value = vadSensitivity,
                onValueChange = { vadSensitivity = it },
                min = 0.1f,
                max = 1.0f
            )
        }
        
        // TTS Settings
        SettingsSectionCard(title = "Text to Speech", icon = Icons.Default.VolumeUp) {
            ToggleRow(
                title = "Enable Text to Speech",
                subtitle = "Speak AI responses aloud",
                checked = ttsEnabled,
                onCheckedChange = { ttsEnabled = it }
            )
            
            ToggleRow(
                title = "Auto Speak Responses",
                subtitle = "Automatically speak AI responses",
                checked = autoSpeak,
                onCheckedChange = { autoSpeak = it },
                enabled = ttsEnabled
            )
            
            TextFieldRow(
                title = "Language",
                value = language,
                onValueChange = { language = it },
                placeholder = "en-US",
                icon = Icons.Default.Language
            )
            
            TextFieldRow(
                title = "Voice",
                value = voiceName,
                onValueChange = { voiceName = it },
                placeholder = "Chatterbox-Multilingual.en-US.Male",
                icon = Icons.Default.VoiceOver
            )
            
            SliderRow(
                title = "Speech Speed",
                subtitle = "Playback speed multiplier",
                value = speechSpeed,
                onValueChange = { speechSpeed = it },
                min = 0.5f,
                max = 2.0f,
                valueFormat = "%.1fx"
            )
            
            SliderRow(
                title = "Volume",
                subtitle = "Playback volume",
                value = volume,
                onValueChange = { volume = it },
                min = 0.0f,
                max = 1.0f,
                valueFormat = "%.0f%%"
            )
        }
        
        // Test buttons
        SettingsSectionCard(title = "Test", icon = Icons.Default.Tune) {
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = { /* Test TTS */ }) {
                    Icon(Icons.Default.VolumeUp, contentDescription = "Test TTS")
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(8.dp))
                    Text("Test TTS")
                }
                Button(onClick = { /* Test ASR */ }) {
                    Icon(Icons.Default.Mic, contentDescription = "Test ASR")
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(8.dp))
                    Text("Test ASR")
                }
            }
        }
    }
}

@Composable
fun SettingsSectionCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)
    ) {
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
fun ToggleRow(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit, enabled: Boolean = true) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .alpha(if (enabled) 1f else 0.5f),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
        Switch(
            checked = checked,
            onCheckedChange = if (enabled) onCheckedChange else null,
            colors = androidx.compose.material3.SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
    }
}

@Composable
fun SliderRow(
    title: String,
    subtitle: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    min: Float,
    max: Float,
    valueFormat: String = "%.2f"
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
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = min..max,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            colors = androidx.compose.material3.SliderDefaults.colors(
                activeTrackColor = MaterialTheme.colorScheme.primary,
                activeTickColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            )
        )
    }
}

@Composable
fun TextFieldRow(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = title, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(title) },
            placeholder = { Text(placeholder) },
            modifier = Modifier.weight(1f).fillMaxWidth(),
            singleLine = true
        )
    }
}