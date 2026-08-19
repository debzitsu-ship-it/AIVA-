package com.aiva.ui.onboarding

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.ProgressIndicator
import androidx.compose.material3.Text
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Close
import androidx.hilt.navigation.compose.hiltViewModel
import com.aiva.security.PermissionManager
import com.aiva.security.PermissionManager.PermissionState
import com.aiva.ui.theme.AivaTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PermissionOnboardingActivity : ComponentActivity() {
    
    private val requestOverlayPermission = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        // Handle overlay permission result
        recreate()
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AivaTheme {
                PermissionOnboardingScreen(onComplete = { finish() })
            }
        }
    }
}

@Composable
fun PermissionOnboardingScreen(onComplete: () -> Unit) {
    val permissionManager: PermissionManager = hiltViewModel()
    val permissionStates by permissionManager.permissionStates.collectAsStateWithLifecycle()
    
    var currentStep by remember { mutableStateOf(0) }
    var showOverlayRationale by remember { mutableStateOf(false) }
    
    val steps = listOf(
        OnboardingStep("Microphone", "Voice input & speech recognition", Icons.Default.Mic, "RECORD_AUDIO"),
        OnboardingStep("Accessibility", "Screen reading & automation", Icons.Default.SmartToy, "ACCESSIBILITY"),
        OnboardingStep("Overlay", "Floating Mini UI window", Icons.Default.Layers, "OVERLAY"),
        OnboardingStep("Notifications", "Foreground service status", Icons.Default.Visibility, "NOTIFICATIONS"),
        OnboardingStep("Screen Capture", "Game control & observation", Icons.Default.PlayArrow, "MEDIA_PROJECTION"),
    )
    
    val allGranted = steps.all { permissionStates[it.permissionKey]?.granted == true }
    
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // Progress bar
        Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(MaterialTheme.colorScheme.primaryContainer)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(steps.filter { permissionStates[it.permissionKey]?.granted == true }.size / steps.size.toFloat())
                    .height(4.dp)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
        
        // Content
        Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Step indicator
                Text("Step ${currentStep + 1} of ${steps.size}", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                
                // Icon
                Box(
                    modifier = Modifier.size(120.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                ) {
                    val step = steps[currentStep]
                    Icon(step.icon, contentDescription = step.title, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(60.dp).align(Alignment.Center))
                }
                
                // Title & Description
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(step.title, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
                    Text(step.description, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), textAlign = androidx.compose.ui.text.TextAlign.Center)
                }
                
                // Permission status
                val currentPermission = steps[currentStep]
                val state = permissionStates[currentPermission.permissionKey]
                val isGranted = state?.granted == true
                
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    colors = androidx.compose.material3.CardDefaults.cardColors(
                        containerColor = if (isGranted) Color(0xFF4CAF50).copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                ) {
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.foundation.layout.Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isGranted) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Granted", tint = Color(0xFF4CAF50))
                                Text("Granted", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color(0xFF4CAF50))
                            } else {
                                androidx.compose.material3.ProgressIndicator()
                                Text("Required", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                        }
                    }
                }
                
                // Action button
                if (isGranted) {
                    if (currentStep < steps.size - 1) {
                        Button(
                            onClick = { currentStep++ },
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).height(56.dp)
                        ) {
                            Text("Next")
                        }
                    } else {
                        Button(
                            onClick = onComplete,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).height(56.dp)
                        ) {
                            Text("Get Started")
                        }
                    }
                } else {
                    Button(
                        onClick = { requestPermission(currentPermission) },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).height(56.dp)
                    ) {
                        Text("Grant Permission")
                    }
                }
                
                // Skip option (for optional permissions)
                if (currentPermission.permissionKey != "RECORD_AUDIO" && currentPermission.permissionKey != "ACCESSIBILITY") {
                    Text("Skip for now", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), modifier = Modifier.fillMaxWidth().padding(top = 16.dp).align(Alignment.CenterHorizontally))
                        .also { it.setOnClickListener { currentStep++ } }
                }
            }
        }
        
        // Close button
        androidx.compose.material3.IconButton(
            onClick = onComplete,
            modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
        ) {
            Icon(Icons.Default.Close, contentDescription = "Skip onboarding")
        }
    }
}

data class OnboardingStep(
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val permissionKey: String
)

@Composable
fun requestPermission(step: OnboardingStep) {
    // This would be implemented with actual permission requests
    // For now, just show a message
}

@Composable
fun requestOverlayPermission() {
    // Open overlay settings
    // val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))
    // requestOverlayPermission.launch(intent)
}

@Composable
fun requestAccessibilityPermission() {
    // Open accessibility settings
    // val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
    // startActivity(intent)
}

@Composable
fun requestMediaProjection() {
    // Handled by MediaProjectionManager.createScreenCaptureIntent()
}