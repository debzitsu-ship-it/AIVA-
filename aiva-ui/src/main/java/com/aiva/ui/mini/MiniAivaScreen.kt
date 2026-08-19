package com.aiva.ui.mini

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aiva.conversation.ConversationViewModel

@Composable
fun MiniAivaScreen(
    viewModel: ConversationViewModel,
    onDismiss: () -> Unit,
    onExpand: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Card(modifier = Modifier.padding(8.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("AIVA")
            Text(state.name)
            Button(onClick = onExpand) { Text("Expand") }
            Button(onClick = onDismiss) { Text("Close") }
        }
    }
}
