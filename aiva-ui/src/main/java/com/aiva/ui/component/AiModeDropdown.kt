package com.aiva.ui.component

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.aiva.conversation.ConversationViewModel

@Composable
fun AiModeDropdown(
    currentMode: ConversationViewModel.AiMode,
    onModeChange: (ConversationViewModel.AiMode) -> Unit,
    expanded: Boolean,
    onExpandChange: (Boolean) -> Unit
) {
    TextButton(onClick = { onExpandChange(!expanded) }) {
        Text(currentMode.name.replace('_', ' '))
    }
    DropdownMenu(expanded = expanded, onDismissRequest = { onExpandChange(false) }) {
        ConversationViewModel.AiMode.entries.forEach { mode ->
            DropdownMenuItem(
                text = { Text(mode.name.replace('_', ' ')) },
                onClick = {
                    onModeChange(mode)
                    onExpandChange(false)
                }
            )
        }
    }
}
