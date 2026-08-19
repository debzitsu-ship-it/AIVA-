package com.aiva.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.MergeType
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Speed
import com.aiva.conversation.ConversationViewModel

@Composable
fun AiModeDropdown(
    currentMode: ConversationViewModel.AiMode,
    onModeChange: (ConversationViewModel.AiMode) -> Unit,
    expanded: Boolean,
    onExpandChange: (Boolean) -> Unit
) {
    val modes = listOf(
        ConversationViewModel.AiMode.AUTO to "Auto" to Icons.Default.FlashOn,
        ConversationViewModel.AiMode.FASTEST to "Fastest" to Icons.Default.Bolt,
        ConversationViewModel.AiMode.BEST_REASONING to "Best Reasoning" to Icons.Default.Psychology,
        ConversationViewModel.AiMode.MULTI_MODEL_FUSION to "Fusion" to Icons.Default.MergeType,
        ConversationViewModel.AiMode.AGENT to "Agent" to Icons.Default.Speed
    )
    
    var showMenu by remember { mutableStateOf(expanded) }
    
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .fillMaxWidth()
            .width(120.dp)
    ) {
        // Dropdown button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val current = modes.find { it.first == currentMode } ?: modes[0]
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = current.third,
                    contentDescription = current.second,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = current.second,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Expand",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        
        // Dropdown menu
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { 
                showMenu = false
                onExpandChange(false)
            }
        ) {
            modes.forEach { (mode, label, icon) ->
                DropdownMenuItem(
                    onClick = {
                        onModeChange(mode)
                        showMenu = false
                        onExpandChange(false)
                    },
                    content = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = if (mode == currentMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = label,
                                color = if (mode == currentMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (mode == currentMode) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                )
            }
        }
    }
}