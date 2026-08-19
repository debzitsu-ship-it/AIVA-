package com.aiva.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.navigation.NavController
import androidx.compose.navigation.NavType
import androidx.compose.navigation.navArgument
import androidx.compose.navigation.NavHost
import androidx.compose.navigation.compose.NavHost
import androidx.compose.navigation.compose.composable
import androidx.compose.navigation.compose.rememberNavController
import com.aiva.ui.component.NavigationDrawerItem

@Composable
fun SettingsScreen() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = "main") {
        composable("main") {
            SettingsMainScreen(onNavigate = { route ->
                navController.navigate(route)
            })
        }
        composable("api_keys") {
            ApiKeysScreen(onBack = { navController.popBackStack() })
        }
        composable("models") {
            ModelsScreen(onBack = { navController.popBackStack() })
        }
        composable("voice") {
            VoiceSettingsScreen(onBack = { navController.popBackStack() })
        }
        composable("automation") {
            AutomationSettingsScreen(onBack = { navController.popBackStack() })
        }
        composable("game_profiles") {
            GameProfilesScreen(onBack = { navController.popBackStack() })
        }
        composable("privacy") {
            PrivacySecurityScreen(onBack = { navController.popBackStack() })
        }
        composable("about") {
            AboutScreen(onBack = { navController.popBackStack() })
        }
        composable(
            route = "api_key_detail/{keyId}",
            arguments = listOf(navArgument("keyId") { type = NavType.StringType })
        ) { backStackEntry ->
            val keyId = backStackEntry.getString() ?: ""
            ApiKeyDetailScreen(keyId = keyId, onBack = { navController.popBackStack() })
        }
        composable(
            route = "game_profile_calibrate/{profileId}",
            arguments = listOf(navArgument("profileId") { type = NavType.StringType })
        ) { backStackEntry ->
            val profileId = backStackEntry.getString() ?: ""
            GameProfileCalibrationScreen(profileId = profileId, onBack = { navController.popBackStack() })
        }
    }
}

@Composable
fun SettingsMainScreen(onNavigate: (String) -> Unit) {
    val sections = listOf(
        SettingsSection("api_keys", "API Keys", "Manage NVIDIA API keys", Icons.Default.Key),
        SettingsSection("models", "Models", "Select and configure AI models", Icons.Default.Psychology),
        SettingsSection("voice", "Voice", "Speech recognition & synthesis", Icons.Default.Mic),
        SettingsSection("automation", "Automation", "Accessibility & action settings", Icons.Default.SmartToy),
        SettingsSection("game_profiles", "Game Profiles", "Calibrate game controls", Icons.Default.SportsEsports),
        SettingsSection("privacy", "Privacy & Security", "Data management & security", Icons.Default.Security),
        SettingsSection("about", "About", "Version & licenses", Icons.Default.Info),
    )
    
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        TopAppBar(
            title = { Text("Settings", fontWeight = FontWeight.Bold) },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            navigationIcon = {
                androidx.compose.material3.IconButton(onClick = { /* Handle back */ }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
        )
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp, 16.dp, 16.dp, 100.dp)
        ) {
            items(sections) { section ->
                SettingsSectionCard(section = section, onClick = { onNavigate(section.route) })
            }
        }
    }
}

data class SettingsSection(
    val route: String,
    val title: String,
    val subtitle: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun SettingsSectionCard(section: SettingsSection, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onClick,
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        )
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.foundation.layout.Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = section.icon,
                    contentDescription = section.title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = androidx.compose.ui.Modifier.size(24.dp)
                )
                Column {
                    Text(
                        text = section.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = section.subtitle,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.ArrowForwardIos,
                contentDescription = "Navigate",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                modifier = androidx.compose.ui.Modifier.size(20.dp)
            )
        }
    }
}