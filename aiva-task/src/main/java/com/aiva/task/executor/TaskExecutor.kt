package com.aiva.task.executor

import com.aiva.core.observation.ScreenState
import com.aiva.core.security.ApiKeyEntry
import com.aiva.core.task.Intent
import com.aiva.core.task.TaskExecution
import com.aiva.core.task.TaskPlan
import com.aiva.core.task.TaskState

class TaskExecutor {
    suspend fun executeTask(
        userInput: String,
        screenState: ScreenState?,
        apiKeys: Map<String, ApiKeyEntry>,
        context: ExecutorContext
    ): TaskExecution {
        val plan = TaskPlan(
            id = java.util.UUID.randomUUID().toString(),
            intent = com.aiva.core.task.Intent(
                type = com.aiva.core.task.IntentType.CHAT,
                confidence = 1f,
                originalQuery = userInput
            ),
            steps = emptyList(),
            estimatedDuration = 0
        )
        return TaskExecution(plan = plan, state = TaskState.SUCCESS, endTime = System.currentTimeMillis())
    }

    fun stop() = Unit

    data class ExecutorContext(
        val currentApp: String? = null,
        val currentScreen: String? = null,
        val recentCommands: List<String> = emptyList(),
        val selectedModel: String? = null,
        val userPreferences: Map<String, String> = emptyMap(),
        val activeGameProfile: String? = null
    )
}
