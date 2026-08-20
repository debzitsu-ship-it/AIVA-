package com.aiva.task.classifier

import com.aiva.core.task.Intent
import com.aiva.core.task.IntentType

class IntentClassifier {
    data class TaskContext(
        val currentApp: String? = null,
        val currentScreen: String? = null,
        val recentCommands: List<String> = emptyList(),
        val selectedModel: String? = null,
        val userPreferences: Map<String, String> = emptyMap(),
        val activeGameProfile: String? = null
    )

    fun classifyFallback(input: String): Intent = Intent(IntentType.CHAT, 0.5f, input)
}
