package com.aiva.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aiva.core.model.ModelInfo

@Composable
fun ModelPickerDialog(
    currentModel: ModelInfo?,
    availableModels: List<ModelInfo>,
    onSelect: (ModelInfo) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Model") },
        text = {
            LazyColumn {
                items(availableModels) { model ->
                    TextButton(
                        onClick = { onSelect(model) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val mark = if (currentModel?.id == model.id) "✓ " else ""
                        Text("$mark${model.displayName}")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
