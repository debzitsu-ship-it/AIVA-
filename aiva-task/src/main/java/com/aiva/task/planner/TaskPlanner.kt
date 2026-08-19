package com.aiva.task.planner

import com.aiva.ai.client.NimClient
import com.aiva.ai.registry.ModelRegistry
import com.aiva.ai.router.ModelRouter
import com.aiva.core.action.Action
import com.aiva.core.model.ChatCompletionRequest
import com.aiva.core.model.ChatMessage
import com.aiva.core.model.ModelInfo
import com.aiva.core.security.ApiKeyEntry
import com.aiva.core.task.Intent
import com.aiva.core.task.PlanStep
import com.aiva.core.task.TaskPlan
import com.aiva.core.task.TaskState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskPlanner @Inject constructor(
    private val nimClient: NimClient,
    private val modelRegistry: ModelRegistry,
    private val modelRouter: ModelRouter
) {
    
    private val _state = MutableStateFlow<TaskState>(TaskState.IDLE)
    val state: StateFlow<TaskState> = _state
    
    private val PLANNER_PROMPT = """
        You are AIVA's task planner. Create a step-by-step plan to accomplish the user's intent.
        
        Available actions:
        - launch_app(packageName, action?) - Launch an Android app
        - open_url(url) - Open a URL in browser
        - tap(target) - Tap on a UI element
        - double_tap(target) - Double tap
        - long_press(target, duration) - Long press
        - swipe(from, to, duration) - Swipe gesture
        - scroll(direction, target?, amount) - Scroll
        - type(text, target?, replace) - Type text
        - replace_text(target, newText) - Replace text in field
        - copy(target?) - Copy from field
        - paste(target?) - Paste into field
        - back() - Press back button
        - home() - Press home button
        - observe(query) - Observe screen for information
        - wait(ms) - Wait
        - find(target) - Find element
        - select(target, option) - Select checkbox/radio
        - finish(result) - Complete task with result
        - ask_user(question) - Ask user for clarification
        - stop() - Stop execution
        
        Target identification:
        - text: visible text
        - resourceId: Android resource ID
        - contentDescription: accessibility description
        - className: view class
        - index: element index
        - visionHint: "button in lower right", "search bar at top"
        
        Rules:
        1. Prefer semantic targets (text, resourceId) over coordinates
        2. Always observe before acting when uncertain
        3. Include verification steps for critical actions
        4. Add wait steps between actions when needed
        5. For high-impact actions (purchases, sends, deletes), use ask_user first
        6. Keep plans concise - prefer fewer, larger steps
        
        Return JSON only:
        {
          "steps": [
            {
              "id": "step_1",
              "action": {"type": "tap", "target": {"text": "Settings"}},
              "description": "Open Settings",
              "expectedOutcome": "Settings screen opens",
              "verificationQuery": "Is Settings open?"
            }
          ],
          "estimatedDuration": 5000,
          "requiredCapabilities": ["accessibility", "automation"]
        }
    """.trimIndent()
    
    suspend fun createPlan(
        intent: Intent,
        screenState: com.aiva.core.observation.ScreenState?,
        availableApiKeys: Map<String, ApiKeyEntry>,
        context: PlannerContext
    ): TaskPlan {
        _state.value = TaskState.PLANNING
        
        val model = selectPlannerModel(availableApiKeys, intent)
        val apiKey = model?.let { availableApiKeys[it.id] } ?: return fallbackPlan(intent)
        
        val messages = listOf(
            ChatMessage(role = "system", content = PLANNER_PROMPT),
            ChatMessage(role = "user", content = buildPlannerPrompt(intent, screenState, context))
        )
        
        val request = ChatCompletionRequest(
            model = model?.id ?: "nvidia/nemotron-3.5-lightning-30b-a3b",
            messages = messages,
            temperature = 0.2f,
            maxTokens = 4096,
            stream = false
        )
        
        return try {
            val response = nimClient.createCompletion(apiKey, request)
            val content = response.choices.firstOrNull()?.message?.content ?: ""
            parsePlan(content, intent)
        } catch (e: Exception) {
            fallbackPlan(intent)
        }
    }
    
    private fun selectPlannerModel(
        apiKeys: Map<String, ApiKeyEntry>,
        intent: Intent
    ): ModelInfo? {
        val candidates = modelRouter.routeForFusion(intent, apiKeys.keys.toSet())
        return candidates
            .filter { it.agentSuitability && apiKeys.containsKey(it.id) }
            .firstOrNull()
    }
    
    private fun buildPlannerPrompt(
        intent: Intent,
        screenState: com.aiva.core.observation.ScreenState?,
        context: PlannerContext
    ): String {
        val sb = StringBuilder()
        sb.append("Intent: ${intent.type} (confidence: ${intent.confidence})\n")
        sb.append("Query: ${intent.originalQuery}\n")
        sb.append("Entities: ${intent.extractedEntities}\n")
        sb.append("Requires confirmation: ${intent.requiresConfirmation}\n")
        sb.append("Target app: ${intent.targetApp ?: "any"}\n\n")
        
        if (screenState != null) {
            sb.append("Current screen: ${screenState.packageName}\n")
            sb.append("Visible elements:\n")
            screenState.nodes.take(30).forEach { node ->
                val desc = buildNodeDescription(node)
                if (desc.isNotBlank()) sb.append("  - $desc\n")
            }
            sb.append("\n")
        }
        
        sb.append("Context:\n")
        context.currentApp?.let { sb.append("  Current app: $it\n") }
        context.activeGameProfile?.let { sb.append("  Active game: $it\n") }
        
        sb.append("\nCreate a plan to accomplish this intent.")
        return sb.toString()
    }
    
    private fun buildNodeDescription(node: com.aiva.core.observation.AccessibilityNodeSummary): String {
        val parts = mutableListOf<String>()
        node.text?.let { parts.add("text=\"$it\"") }
        node.resourceId?.let { parts.add("id=\"$it\"") }
        node.contentDescription?.let { parts.add("desc=\"$it\"") }
        node.className?.let { parts.add("class=\"${it.substringAfterLast('.')}\"") }
        node.bounds?.let { parts.add("bounds=[${it.left},${it.top},${it.right},${it.bottom}]") }
        val flags = mutableListOf<String>()
        if (node.clickable) flags.add("clickable")
        if (node.scrollable) flags.add("scrollable")
        if (node.editable) flags.add("editable")
        if (flags.isNotEmpty()) parts.add(flags.joinToString(","))
        return parts.joinToString(" ")
    }
    
    private fun parsePlan(json: String, intent: Intent): TaskPlan {
        if (json.isBlank()) return fallbackPlan(intent)
        return fallbackPlan(intent)
    }

    private fun fallbackPlan(intent: Intent): TaskPlan {
        val steps = when (intent.type) {
            com.aiva.core.task.IntentType.ACTION -> listOf(
                PlanStep(
                    id = "step_1",
                    action = Action.Observe(query = "Find target for: ${intent.originalQuery}"),
                    description = "Observe screen",
                    expectedOutcome = "Target identified",
                    verificationQuery = "Target found?"
                ),
                PlanStep(
                    id = "step_2",
                    action = Action.AskUser(question = "Could not automatically determine action for: ${intent.originalQuery}. Please specify."),
                    description = "Ask user for clarification",
                    expectedOutcome = "User provides details",
                    verificationQuery = "User responded?"
                )
            )
            else -> listOf(
                PlanStep(
                    id = "step_1",
                    action = Action.Finish(result = "Planning not available for ${intent.type}"),
                    description = "Finish with message",
                    expectedOutcome = "Task completed",
                    verificationQuery = null
                )
            )
        }
        
        return TaskPlan(
            id = java.util.UUID.randomUUID().toString(),
            intent = intent,
            steps = steps,
            estimatedDuration = 5000,
            requiredCapabilities = setOf("accessibility")
        )
    }
    
    data class PlannerContext(
        val currentApp: String? = null,
        val currentScreen: String? = null,
        val activeGameProfile: String? = null
    )
}