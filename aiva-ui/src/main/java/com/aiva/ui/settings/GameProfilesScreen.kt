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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff

@Composable
fun GameProfilesScreen(onBack: () -> Unit) {
    var showAddDialog by remember { mutableStateOf(false) }
    var newProfileName by remember { mutableStateOf("") }
    var newPackageName by remember { mutableStateOf("") }
    
    // Mock profiles - would come from GameProfileRepository
    val profiles = remember { mutableStateOf<List<GameProfileUi>>(listOf(
        GameProfileUi("1", "Call of Duty Mobile", "com.activision.callofduty.mobile", true),
        GameProfileUi("2", "PUBG Mobile", "com.pubg.mobile", false),
    )) }
    
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        TopAppBar(
            title = { Text("Game Profiles", fontWeight = FontWeight.Bold) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } },
            actions = {
                IconButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Profile")
                }
            }
        )
        
        // Help text
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.Default.Info, contentDescription = "Info", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                Column(modifier = Modifier.weight(1f)) {
                    Text("Create profiles for games you want to control", fontSize = 13.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text("Calibrate control positions for your screen resolution", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                }
            }
        }
        
        if (profiles.value.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(Icons.Default.SportsEsports, contentDescription = "No profiles", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f), modifier = Modifier.size(64.dp))
                    Text("No game profiles", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Text("Tap + to add a game profile", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Button(onClick = { showAddDialog = true }) { Text("Add Profile") }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp, 8.dp, 16.dp, 100.dp)
            ) {
                items(profiles.value) { profile ->
                    GameProfileCard(
                        profile = profile,
                        onEdit = { /* TODO */ },
                        onDelete = { /* TODO */ },
                        onCalibrate = { /* TODO */ },
                        onActivate = { /* TODO */ }
                    )
                }
            }
        }
        
        if (showAddDialog) {
            AddGameProfileDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { name, packageName ->
                    // TODO: Add profile
                    showAddDialog = false
                }
            )
        }
    }
}

data class GameProfileUi(
    val id: String,
    val name: String,
    val packageName: String,
    val isActive: Boolean
)

@Composable
fun GameProfileCard(
    profile: GameProfileUi,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCalibrate: () -> Unit,
    onActivate: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(profile.name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(profile.packageName, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
                
                if (profile.isActive) {
                    androidx.compose.material3.Chip(
                        onClick = { /* already active */ },
                        colors = androidx.compose.material3.ChipDefaults.colors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Text("Active", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
            
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = onActivate, enabled = !profile.isActive) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Activate")
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(4.dp))
                    Text(if (profile.isActive) "Active" else "Activate")
                }
                Button(onClick = onCalibrate) {
                    Icon(Icons.Default.Settings, contentDescription = "Calibrate")
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(4.dp))
                    Text("Calibrate")
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
fun AddGameProfileDialog(onDismiss: () -> Unit, onAdd: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var packageName by remember { mutableStateOf("") }
    
    Surface(modifier = Modifier.fillMaxSize(), color = Color.Black.copy(alpha = 0.5f)) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Card(modifier = Modifier.fillMaxWidth().padding(24.dp), shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    androidx.compose.foundation.layout.Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Add Game Profile", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Close") }
                    }
                    
                    TextField(value = name, onValueChange = { name = it }, label = { Text("Profile Name") }, placeholder = { Text("Call of Duty Mobile") }, modifier = Modifier.fillMaxWidth())
                    TextField(value = packageName, onValueChange = { packageName = it }, label = { Text("Package Name") }, placeholder = { Text("com.activision.callofduty.mobile") }, modifier = Modifier.fillMaxWidth())
                    
                    androidx.compose.foundation.layout.Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = onDismiss) { Text("Cancel") }
                        Button(onClick = { onAdd(name, packageName) }, colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary), enabled = name.isNotBlank() && packageName.isNotBlank()) { Text("Add Profile") }
                    }
                }
            }
        }
    }
}