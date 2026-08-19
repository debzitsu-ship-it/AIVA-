package com.aiva.task.classifier

import com.aiva.ai.client.NimClient
import com.aiva.ai.registry.ModelRegistry
import com.aiva.ai.router.ModelRouter
import com.aiva.core.model.ChatCompletionRequest
import com.aiva.core.model.ChatMessage
import com.aiva.core.model.ModelInfo
import com.aiva.core.security.ApiKeyEntry
import com.aiva.core.task.Intent
import com.aiva.core.task.IntentType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IntentClassifier @Inject constructor(
    private val nimClient: NimClient,
    private val modelRegistry: ModelRegistry,
    private val modelRouter: ModelRouter
) {
    
    private val _lastIntent = MutableStateFlow<Intent?>(null)
    val lastIntent: StateFlow<Intent?> = _lastIntent
    
    private val CLASSIFIER_PROMPT = """
        You are an intent classifier for AIVA, an Android AI assistant.
        
        Classify the user's input into ONE of these intent types:
        
        1. CHAT - Casual conversation, greetings, small talk
        2. QUESTION - Asking for information, facts, explanations
        3. EXPLANATION - Requesting detailed explanation of a concept
        4. ACTION - Single action like "open settings", "turn on wifi"
        5. AUTOMATION - Multi-step automation task
        6. FORM_FILL - Filling out a form or fields
        7. QUIZ - Answering quiz/educational questions
        8. NAVIGATION - Navigating to a location or screen
        9. GAME_CONTROL - Controlling a game
        10. SCREEN_ANALYSIS - Analyzing current screen content
        11. MULTI_STEP_WORKFLOW - Complex multi-app workflow
        
        Consider context:
        - If user says "open X", "click Y", "tap Z" → ACTION
        - If user says "how do I", "what is", "explain" → QUESTION/EXPLANATION
        - If user says "fill this form", "enter my info" → FORM_FILL
        - If user says "play this game", "auto play" → GAME_CONTROL
        - If user says "what's on screen", "read this" → SCREEN_ANALYSIS
        - If user describes multi-step process → MULTI_STEP_WORKFLOW
        
        Return JSON only:
        {
          "type": "INTENT_TYPE",
          "confidence": 0.95,
          "entities": {"key": "value"},
          "requiresConfirmation": false,
          "targetApp": "package.name"
        }
    """.trimIndent()
    
    suspend fun classify(
        userInput: String,
        context: TaskContext,
        availableApiKeys: Map<String, ApiKeyEntry>
    ): Intent {
        val model = selectClassifierModel(availableApiKeys)
        if (model == null) {
            return fallbackClassification(userInput)
        }
        
        val apiKey = availableApiKeys[model.id] ?: return fallbackClassification(userInput)
        
        val messages = listOf(
            ChatMessage(role = "system", content = CLASSIFIER_PROMPT),
            ChatMessage(role = "user", content = buildContextualPrompt(userInput, context))
        )
        
        val request = ChatCompletionRequest(
            model = model.id,
            messages = messages,
            temperature = 0.1f,
            maxTokens = 512,
            stream = false
        )
        
        return try {
            val response = nimClient.createCompletion(apiKey, request)
            val content = response.choices.firstOrNull()?.message?.content ?: ""
            parseClassification(content, userInput)
        } catch (e: Exception) {
            fallbackClassification(userInput)
        }
    }
    
    private fun selectClassifierModel(apiKeys: Map<String, ApiKeyEntry>): ModelInfo? {
        // Use fast model for classification
        return modelRegistry.getAllModels()
            .filter { it.speedProfile.name == "FASTEST" && apiKeys.containsKey(it.id) }
            .firstOrNull()
    }
    
    private fun buildContextualPrompt(input: String, context: TaskContext): String {
        val sb = StringBuilder()
        sb.append("User input: $input\n\n")
        
        if (context.currentApp != null) {
            sb.append("Current app: ${context.currentApp}\n")
        }
        if (context.currentScreen != null) {
            sb.append("Current screen: ${context.currentScreen}\n")
        }
        if (context.recentCommands.isNotEmpty()) {
            sb.append("Recent commands: ${context.recentCommands.joinToString(", ")}\n")
        }
        if (context.activeGameProfile != null) {
            sb.append("Active game: ${context.activeGameProfile}\n")
        }
        
        sb.append("\nClassify the intent.")
        return sb.toString()
    }
    
    private fun parseClassification(json: String, originalQuery: String): Intent {
        try {
            val parsed = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
                .decodeFromString<ClassificationResult>(json)
            
            return Intent(
                type = IntentType.valueOf(parsed.type),
                confidence = parsed.confidence,
                originalQuery = originalQuery,
                extractedEntities = parsed.entities,
                requiresConfirmation = parsed.requiresConfirmation,
                targetApp = parsed.targetApp
            )
        } catch (e: Exception) {
            return fallbackClassification(originalQuery)
        }
    }
    
    private fun fallbackClassification(input: String): Intent {
        val lower = input.lowercase()
        
        return when {
            lower.matches("""^(hi|hello|hey|thanks|thank you|bye|goodbye).*""".toRegex()) -> 
                Intent(IntentType.CHAT, 0.8f, input)
            lower.matches("""^(what|who|where|when|why|how|define|explain|tell me).*""".toRegex()) -> 
                Intent(IntentType.QUESTION, 0.7f, input)
            lower.matches("""^(open|launch|start|click|tap|press|type|scroll|swipe|go back|go home).*""".toRegex()) -> 
                Intent(IntentType.ACTION, 0.8f, input)
            lower.matches("""^(fill|enter|input|complete|submit).*form.*""".toRegex()) -> 
                Intent(IntentType.FORM_FILL, 0.8f, input)
            lower.matches("""^(answer|solve|quiz|question|multiple choice).*""".toRegex()) -> 
                Intent(IntentType.QUIZ, 0.8f, input)
            lower.matches("""^(play|auto|bot|cheat|hack).*game.*""".toRegex()) -> 
                Intent(IntentType.GAME_CONTROL, 0.7f, input)
            lower.matches("""^(what.*screen|read.*screen|analyze|describe).*""".toRegex()) -> 
                Intent(IntentType.SCREEN_ANALYSIS, 0.7f, input)
            lower.contains("then") || lower.contains("after that") || lower.contains("and then") -> 
                Intent(IntentType.MULTI_STEP_WORKFLOW, 0.6f, input)
            else -> Intent(IntentType.CHAT, 0.5f, input)
        }
    }
    
    @kotlinx.serialization.Serializable
    private data class ClassificationResult(
        val type: String,
        val confidence: Float,
        val entities: Map<String, String> = emptyMap(),
        val requiresConfirmation: Boolean = false,
        val targetApp: String? = null
    )
    
    data class TaskContext(
        val currentApp: String? = null,
        val currentScreen: String? = null,
        val recentCommands: List<String> = emptyList(),
        val selectedModel: String? = null,
        val userPreferences: Map<String, String> = emptyMap(),
        val activeGameProfile: String? = null
    )
}