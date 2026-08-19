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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.License
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.OpenInNew

@Composable
fun AboutScreen(onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("About", fontWeight = FontWeight.Bold) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
            navigationIcon = { androidx.compose.material3.IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } }
        )
        
        // App info
        Box(
            modifier = Modifier.fillMaxWidth().padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // App icon placeholder
                Box(
                    modifier = Modifier.size(120.dp)
                        .background(MaterialTheme.colorScheme.primary, androidx.compose.foundation.shape.RoundedCornerShape(24.dp))
                        .graphicsLayer { scaleX = 1f; scaleY = 1f }
                ) {
                    Icon(Icons.Default.SmartToy, contentDescription = "AIVA", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(64.dp).align(Alignment.Center))
                }
                
                Text("AIVA", fontSize = 32.sp, fontWeight = FontWeight.Bold)
                Text("AI Virtual Assistant", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Text("Version 1.0.0", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                Text("Build 1", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
            }
        }
        
        // Description
        Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("About AIVA", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "AIVA is a personal AI assistant for Android that combines conversational AI, " +
                    "voice interaction, screen understanding, Android automation, and real-time game control. " +
                    "It uses NVIDIA's NIM and Riva services for state-of-the-art AI models and speech processing.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    lineHeight = 22.sp
                )
            }
        }
        
        // Features
        Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Features", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(12.dp))
                
                FeatureRow(Icons.Default.Mic, "Voice Input & Output", "Riva ASR/TTS with Whisper & Chatterbox")
                FeatureRow(Icons.Default.Psychology, "Multi-Model AI", "9 NVIDIA models with smart routing & fusion")
                FeatureRow(Icons.Default.SmartToy, "Android Automation", "AccessibilityService-based control")
                FeatureRow(Icons.Default.Visibility, "Screen Understanding", "Accessibility + Vision fallback")
                FeatureRow(Icons.Default.SportsEsports, "Game Control", "Real-time 60fps control loop")
                FeatureRow(Icons.Default.Lock, "Privacy First", "Keystore encryption, no cloud sync")
            }
        }
        
        // Links
        Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Links", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(12.dp))
                
                LinkRow(Icons.Default.OpenInNew, "NVIDIA NIM API", "https://integrate.api.nvidia.com")
                LinkRow(Icons.Default.OpenInNew, "NVIDIA Riva ASR/TTS", "https://github.com/nvidia-riva/python-clients")
                LinkRow(Icons.Default.Code, "Source Code", "https://github.com/aiva/aiva")
                LinkRow(Icons.Default.BugReport, "Report Issue", "https://github.com/aiva/aiva/issues")
                LinkRow(Icons.Default.License, "Licenses", "https://github.com/aiva/aiva/blob/main/LICENSE")
            }
        }
        
        // Credits
        Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Credits", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(12.dp))
                
                CreditRow("NVIDIA", "NIM API & Riva ASR/TTS")
                CreditRow("Google", "Jetpack Compose, MediaPipe, TensorFlow Lite")
                CreditRow("JetBrains", "Kotlin & Coroutines")
                CreditRow("Square", "OkHttp, Retrofit, Moshi")
                CreditRow("Google", "Dagger Hilt, Room, Accompanist")
                CreditRow("Open Source Community", "All open source libraries used")
            }
        }
        
        // Legal
        Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Legal", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    "AIVA is provided as-is without warranty. Use at your own risk. " +
                    "Respect app terms of service and game rules when using automation features. " +
                    "AIVA does not bypass anti-cheat, DRM, or security measures.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))
                Text("© 2026 AIVA Project. Licensed under Apache 2.0.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
fun FeatureRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, description: String) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = title, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        Column {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text(description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
    }
}

@Composable
fun LinkRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, url: String) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp).fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.foundation.layout.Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = title, tint = MaterialTheme.colorScheme.primary)
            Text(title, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
        }
        Icon(Icons.Default.ArrowForwardIos, contentDescription = "Open", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
    }
}

@Composable
fun CreditRow(name: String, contribution: String) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(name, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Text(contribution, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
    }
}