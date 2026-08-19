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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudDone

@Composable
fun PrivacySecurityScreen(onBack: () -> Unit) {
    var biometricRequired by remember { mutableStateOf(false) }
    var autoLockEnabled by remember { mutableStateOf(true) }
    var clearMemoryOnLock by remember { mutableStateOf(true) }
    var allowExport by remember { mutableStateOf(false) }
    var crashReporting by remember { mutableStateOf(false) }
    var analyticsEnabled by remember { mutableStateOf(false) }
    
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Privacy & Security", fontWeight = FontWeight.Bold) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
            navigationIcon = { androidx.compose.material3.IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } }
        )
        
        // Security Status
        Card(modifier = Modifier.fillMaxWidth().padding(16.dp), colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Column(modifier = Modifier.padding(16.dp)) {
                androidx.compose.foundation.layout.Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Security, contentDescription = "Security", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Your Data is Secure", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text("API keys encrypted with Android Keystore • No cloud sync • Local processing only", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                    }
                    Icon(Icons.Default.CheckCircle, contentDescription = "Secure", tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(24.dp))
                }
            }
        }
        
        // Data Protection
        SettingsSectionCard(title = "Data Protection", icon = Icons.Default.Lock) {
            ToggleRow(
                title = "Require Biometric Unlock",
                subtitle = "Authenticate with fingerprint/face to access AIVA",
                checked = biometricRequired,
                onCheckedChange = { biometricRequired = it }
            )
            
            ToggleRow(
                title = "Auto Lock",
                subtitle = "Lock app after 5 minutes of inactivity",
                checked = autoLockEnabled,
                onCheckedChange = { autoLockEnabled = it }
            )
            
            ToggleRow(
                title = "Clear Memory on Lock",
                subtitle = "Clear decrypted keys and screen data when locked",
                checked = clearMemoryOnLock,
                onCheckedChange = { clearMemoryOnLock = it }
            )
            
            ToggleRow(
                title = "Allow Export",
                subtitle = "Allow exporting encrypted settings (not keys)",
                checked = allowExport,
                onCheckedChange = { allowExport = it }
            )
        }
        
        // Privacy
        SettingsSectionCard(title = "Privacy", icon = Icons.Default.Visibility) {
            ToggleRow(
                title = "Crash Reporting",
                subtitle = "Send anonymous crash reports (no screen data or keys)",
                checked = crashReporting,
                onCheckedChange = { crashReporting = it }
            )
            
            ToggleRow(
                title = "Usage Analytics",
                subtitle = "Help improve AIVA with anonymous usage stats",
                checked = analyticsEnabled,
                onCheckedChange = { analyticsEnabled = it }
            )
            
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Network Access", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        Text("Required for AI models (NVIDIA NIM) and voice (Riva)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                    Icon(Icons.Default.CloudDone, contentDescription = "Online", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
        
        // Data Management
        SettingsSectionCard(title = "Data Management", icon = Icons.Default.Delete) {
            Button(
                onClick = { /* Clear conversation history */ },
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Clear")
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(8.dp))
                Text("Clear Conversation History")
            }
            
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = { /* Clear all data */ },
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Clear All")
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(8.dp))
                Text("Clear All Data (Keys, Settings, History)")
            }
            
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
            
            Card(modifier = Modifier.fillMaxWidth()) {
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Export Settings", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        Text("Export encrypted settings backup (no API keys)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                    Icon(Icons.Default.CloudOff, contentDescription = "Export", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
        
        // What We Don't Collect
        SettingsSectionCard(title = "What AIVA Never Does", icon = Icons.Default.Warning) {
            Column(modifier = Modifier.fillMaxWidth()) {
                PrivacyPoint("Never logs or stores your API keys in plaintext")
                PrivacyPoint("Never sends screen content to AI models without your action")
                PrivacyPoint("Never uploads screenshots or recordings")
                PrivacyPoint("Never accesses contacts, messages, or personal files")
                PrivacyPoint("Never shares data with third parties")
                PrivacyPoint("Never uses your data for training")
            }
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
fun PrivacyPoint(text: String) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(Icons.Default.CheckCircle, contentDescription = "Check", tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp).padding(top = 2.dp))
        Text(text, fontSize = 14.sp, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
    }
}