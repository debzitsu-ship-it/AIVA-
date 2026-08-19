package com.aiva.ui.onboarding

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.aiva.ui.theme.AivaTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PermissionOnboardingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AivaTheme { OnboardingScreen(onDone = { finish() }) }
        }
    }
}

@Composable
fun OnboardingScreen(onDone: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Button(onClick = onDone) { Text("Continue") }
    }
}

data class OnboardingStep(
    val title: String,
    val description: String,
    val permission: String
)
