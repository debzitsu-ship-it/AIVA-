package com.aiva.ui.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun KeyTestDialog(
    onDismiss: () -> Unit,
    onTest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Test API Key") },
        text = { Text("This will verify your API key works with NVIDIA NIM.") },
        confirmButton = {
            TextButton(onClick = onTest) { Text("Test") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

enum class KeyTestState {
    IDLE, TESTING, SUCCESS, ERROR
}
