package com.aiva.ui.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun SettingsDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Settings") },
        text = { Text("Open Settings from the app menu for API keys, models, voice, and privacy.") },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
