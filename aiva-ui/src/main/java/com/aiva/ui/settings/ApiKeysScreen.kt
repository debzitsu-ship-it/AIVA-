package com.aiva.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsStateWithLifecycle
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Info
import com.aiva.security.ApiKeyManager
import com.aiva.core.security.ApiKeyEntry
import com.aiva.core.security.KeyTestResult
import com.aiva.ui.component.KeyTestDialog
import javax.inject.Inject
import dagger.hilt.android.AndroidEntryPoint

@Composable
fun ApiKeysScreen(onBack: () -> Unit) {
    val apiKeyManager: ApiKeyManager = remember { 
        androidx.hilt.navigation.compose.hiltViewModel() 
    }
    val keys by apiKeyManager.keys.collectAsStateWithLifecycle()
    val keyList = keys.values.toList()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var newKeyName by remember { mutableStateOf("") }
    var newKeyValue by remember { mutableStateOf("") }
    var selectedModels by remember { mutableStateOf<Set<String>>(emptySet()) }
    
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // Top App Bar
        androidx.compose.material3.TopAppBar(
            title = { Text("API Keys", fontWeight = FontWeight.Bold) },
            colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            navigationIcon = {
                androidx.compose.material3.IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                IconButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add API Key")
                }
            }
        )
        
        // Help text
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = androidx.compose.material3.CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.Default.Info, contentDescription = "Info", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                androidx.compose.foundation.layout.Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Add your NVIDIA API keys to enable AI models", fontSize = 13.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text("Keys are encrypted with Android Keystore and never leave your device", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                }
            }
        }
        
        // Keys list
        if (keyList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(Icons.Default.Key, contentDescription = "No keys", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f), modifier = Modifier.size(64.dp))
                    Text("No API keys configured", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Text("Tap + to add your first NVIDIA API key", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Button(onClick = { showAddDialog = true }) {
                        Text("Add API Key")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp, 8.dp, 16.dp, 100.dp)
            ) {
                items(keyList) { key ->
                    ApiKeyCard(
                        key = key,
                        onEdit = { /* TODO */ },
                        onDelete = { /* TODO */ },
                        onTest = { /* TODO */ }
                    )
                }
            }
        }
        
        // Add key dialog
        if (showAddDialog) {
            AddApiKeyDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { name, key, models ->
                    // TODO: Add key via ApiKeyManager
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun ApiKeyCard(
    key: ApiKeyEntry,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onTest: () -> Unit
) {
    val testState = remember { mutableStateOf(key.testResult ?: com.aiva.core.security.KeyTestResult.UNTESTED) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(key.name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("${key.models.size} models • ${formatTimestamp(key.createdAt)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
                
                // Status badge
                TestStatusBadge(result = testState.value)
            }
            
            // Models chips
            if (key.models.isNotEmpty()) {
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    key.models.forEach { modelId ->
                        ModelChip(modelId = modelId)
                    }
                }
            }
            
            // Actions
            androidx.compose.foundation.layout.Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = onTest) {
                    Icon(Icons.Default.Help, contentDescription = "Test")
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(4.dp))
                    Text("Test")
                }
                Button(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit")
                }
                Button(
                    onClick = onDelete,
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete")
                }
            }
        }
    }
}

@Composable
fun TestStatusBadge(result: KeyTestResult) {
    val (color, icon, text) = when (result) {
        KeyTestResult.SUCCESS -> MaterialTheme.colorScheme.primary to Icons.Default.CheckCircle to "Verified"
        KeyTestResult.FAILED, KeyTestResult.INVALID_KEY -> MaterialTheme.colorScheme.error to Icons.Default.Error to "Failed"
        KeyTestResult.RATE_LIMITED -> MaterialTheme.colorScheme.tertiary to Icons.Default.Error to "Rate Limited"
        KeyTestResult.NETWORK_ERROR -> MaterialTheme.colorScheme.error to Icons.Default.Error to "Network Error"
        KeyTestResult.TESTING -> MaterialTheme.colorScheme.secondary to Icons.Default.Sync to "Testing..."
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f) to Icons.Default.Help to "Untested"
    }
    
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .background(color.copy(alpha = 0.1f), androidx.compose.foundation.shape.RoundedCornerShape(12.dp)),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = text, tint = color, modifier = Modifier.size(14.dp))
        Text(text, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = color)
    }
}

@Composable
fun ModelChip(modelId: String) {
    val displayName = modelId.split("/").last().replace("-", " ").uppercase()
    androidx.compose.material3.Chip(
        onClick = { /* TODO */ },
        modifier = Modifier.padding(4.dp, 0.dp),
        colors = androidx.compose.material3.ChipDefaults.colors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Text(displayName, fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}

@Composable
fun AddApiKeyDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, Set<String>) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var keyValue by remember { mutableStateOf("") }
    var showKey by remember { mutableStateOf(false) }
    var selectedModels by remember { mutableStateOf<Set<String>>(emptySet()) }
    
    val availableModels = listOf(
        "z-ai/glm-5.2", "poolside/laguna-xs-2.1", "nvidia/nemotron-3.5-lightning-30b-a3b",
        "meta/muse-glimmer-30b", "google/gemma-4-31b-it", "google/diffusiongemma-26b-a4b-it",
        "nvidia/nemotron-3-ultra-550b-a55b", "thinkingmachines/inkling", "openai/gpt-oss-120b"
    )
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black.copy(alpha = 0.5f)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .height(500.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Add API Key", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Close") }
                    }
                    
                    // Name field
                    TextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Key Name") },
                        placeholder = { Text("My NVIDIA Key") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // Key field
                    TextField(
                        value = keyValue,
                        onValueChange = { keyValue = it },
                        label = { Text("API Key") },
                        placeholder = { Text("nvapi-...") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (showKey) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        keyboardOptions = androidx.compose.ui.text.input.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Text),
                        trailingIcon = {
                            IconButton(onClick = { showKey = !showKey }) {
                                Icon(if (showKey) Icons.Default.Visibility else Icons.Default.VisibilityOff, contentDescription = if (showKey) "Hide" else "Show")
                            }
                        }
                    )
                    
                    // Model selection
                    Text("Enabled Models", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(availableModels) { model ->
                            androidx.compose.material3.Checkbox(
                                checked = selectedModels.contains(model),
                                onCheckedChange = { checked ->
                                    if (checked) selectedModels += model else selectedModels -= model
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(model, fontSize = 13.sp)
                            }
                        }
                    }
                    
                    // Buttons
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(onClick = onDismiss) { Text("Cancel") }
                        Button(
                            onClick = { onAdd(name, keyValue, selectedModels) },
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            enabled = name.isNotBlank() && keyValue.isNotBlank() && selectedModels.isNotEmpty()
                        ) { Text("Add Key") }
                    }
                }
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    return java.text.SimpleDateFormat("MM/dd/yyyy", java.util.Locale.getDefault()).format(java.util.Date(timestamp))
}