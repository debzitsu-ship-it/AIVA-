package com.aiva.core.task

import kotlinx.serialization.Serializable

enum class TaskState {
    IDLE,
    LISTENING,
    UNDERSTANDING,
    PLANNING,
    OBSERVING,
    ACTING,
    VERIFYING,
    WAITING,
    SUCCESS,
    ERROR,
    STOPPED
}

enum class IntentType {
    CHAT,
    QUESTION,
    EXPLANATION,
    ACTION,
    AUTOMATION,
    FORM_FILL,
    QUIZ,
    NAVIGATION,
    GAME_CONTROL,
    SCREEN_ANALYSIS,
    MULTI_STEP_WORKFLOW
}

@Serializable
data class Intent(
    val type: IntentType,
    val confidence: Float,
    val originalQuery: String,
    val extractedEntities: Map<String, String> = emptyMap(),
    val requiresConfirmation: Boolean = false,
    val targetApp: String? = null
)

@Serializable
data class TaskPlan(
    val id: String,
    val intent: Intent,
    val steps: List<PlanStep>,
    val estimatedDuration: Long,
    val requiredCapabilities: Set<String> = emptySet()
)

@Serializable
data class PlanStep(
    val id: String,
    val action: com.aiva.core.action.Action,
    val description: String,
    val expectedOutcome: String,
    val fallbackAction: com.aiva.core.action.Action? = null,
    val verificationQuery: String? = null
)

@Serializable
data class TaskExecution(
    val plan: TaskPlan,
    val currentStepIndex: Int = 0,
    val completedSteps: List<StepResult> = emptyList(),
    val state: TaskState = TaskState.PLANNING,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null
)

@Serializable
data class StepResult(
    val stepId: String,
    val action: com.aiva.core.action.Action,
    val success: Boolean,
    val result: com.aiva.core.action.ActionResult?,
    val observation: com.aiva.core.observation.ObservationResult?,
    val timestamp: Long = System.currentTimeMillis(),
    val retries: Int = 0
)

@Serializable
data class TaskContext(
    val currentApp: String? = null,
    val currentScreen: String? = null,
    val recentCommands: List<String> = emptyList(),
    val selectedModel: String? = null,
    val userPreferences: Map<String, String> = emptyMap(),
    val activeGameProfile: String? = null
)