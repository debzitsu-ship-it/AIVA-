package com.aiva.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.aiva.conversation.ConversationViewModel
import com.aiva.ui.screen.MainScreen
import com.aiva.ui.theme.AivaTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private val viewModel: ConversationViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        lifecycleScope.launch {
            // Initialize secure storage
            com.aiva.core.util.SecureStorage.init(this@MainActivity)
            // Initialize keystore
            com.aiva.core.util.KeyStoreManager.generateKey()
        }
        
        setContent {
            AivaTheme {
                Surface(
                    modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(viewModel = viewModel)
                }
            }
        }
    }
}