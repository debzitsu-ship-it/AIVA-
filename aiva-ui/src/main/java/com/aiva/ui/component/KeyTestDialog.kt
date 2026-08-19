package com.aiva.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info

@Composable
fun KeyTestDialog(
    onDismiss: () -> Unit,
    onTest: () -> Unit
) {
    var testState by remember { mutableStateOf<KeyTestState>(KeyTestState.IDLE) }
    var testResult by remember { mutableStateOf<String>("") }
    
    Surface(modifier = Modifier.fillMaxSize(), color = Color.Black.copy(alpha = 0.5f)) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Test API Key", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Close") }
                    }
                    
                    when (testState) {
                        KeyTestState.IDLE -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Info, contentDescription = "Test", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
                                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))
                                Text("This will verify your API key works with NVIDIA NIM", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), textAlign = androidx.compose.ui.text.TextAlign.Center)
                                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(24.dp))
                                Button(onClick = {
                                    testState = KeyTestState.TESTING
                                    onTest()
                                }, modifier = Modifier.fillMaxWidth()) {
                                    Text("Test Key")
                                }
                            }
                        }
                        KeyTestState.TESTING -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(modifier = Modifier.size(48.dp))
                                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))
                                Text("Testing key...", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                                Text("This may take a few seconds", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                        }
                        KeyTestState.SUCCESS -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Success", tint = Color(0xFF4CAF50), modifier = Modifier.size(48.dp))
                                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))
                                Text("Key Verified!", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                                Text(testResult, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), textAlign = androidx.compose.ui.text.TextAlign.Center)
                                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(24.dp))
                                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Done") }
                            }
                        }
                        KeyTestState.ERROR -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Error, contentDescription = "Error", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
                                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))
                                Text("Test Failed", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                                Text(testResult, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), textAlign = androidx.compose.ui.text.TextAlign.Center)
                                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(24.dp))
                                Button(onClick = { testState = KeyTestState.IDLE }, modifier = Modifier.fillMaxWidth()) { Text("Try Again") }
                                Button(onClick = onDismiss, colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest), modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) { Text("Cancel") }
                            }
                        }
                    }
                }
            }
        }
    }
}

enum class KeyTestState {
    IDLE, TESTING, SUCCESS, ERROR
}