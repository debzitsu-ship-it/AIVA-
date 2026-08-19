package com.aiva.conversation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aiva.ai.client.NimClient
import com.aiva.ai.fusion.MultiModelFusion
import com.aiva.ai.registry.ModelRegistry
import com.aiva.ai.router.ModelRouter
import com.aiva.automation.accessibility.AccessibilityController
import com.aiva.automation.accessibility.AivaAccessibilityService
import com.aiva.automation.executor.ActionExecutor
import com.aiva.core.action.Action
import com.aiva.core.action.ActionResult
import com.aiva.core.model.ChatCompletionRequest
import com.aiva.core.model.ChatMessage
import com.aiva.core.model.ModelInfo
import com.aiva.core.observation.ScreenState
import com.aiva.core.security.ApiKeyEntry
import com.aiva.core.task.Intent
import com.aiva.core.task.TaskState
import com.aiva.memory.repository.ConversationRepository
import com.aiva.security.ApiKeyManager
import com.aiva.task.classifier.IntentClassifier
import com.aiva.task.executor.TaskExecutor
import com.aiva.task.planner.TaskPlanner
import com.aiva.voice.riva.RivaAsrClient
import com.aiva.voice.riva.RivaTtsClient
import com.aiva.voice.service.VoiceInputService
import com.aiva.voice.service.VoiceOutputService
import com.aiva.voice.viewmodel.VoiceViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ConversationViewModel(application: Application) : AndroidViewModel(application) {
    private val apiKeyManager = ApiKeyManager(application)
    private val nimClient = NimClient()
    private val modelRegistry = ModelRegistry()
    private val modelRouter = ModelRouter(modelRegistry)
    private val multiModelFusion = MultiModelFusion(nimClient, modelRegistry)
    private val conversationRepository = ConversationRepository()
    private val taskExecutor = TaskExecutor(
        IntentClassifier(nimClient, modelRegistry, modelRouter),
        TaskPlanner(nimClient, modelRegistry, modelRouter),
        ActionExecutor(
            AivaAccessibilityService.getInstance() ?: object : AccessibilityController {
                override fun executeAction(action: Action): ActionResult {
                    return ActionResult(success = false, message = "Accessibility service not enabled")
                }

                override val screenState: StateFlow<ScreenState?> = MutableStateFlow(null)
                override val serviceEnabled: StateFlow<Boolean> = MutableStateFlow(false)
                override fun getCurrentScreenState(): ScreenState? = null
                override fun cancelCurrentAction() {}
            }
        )
    )
    private val voice = VoiceViewModel(
        apiKeyManager,
        VoiceInputService(RivaAsrClient()),
        VoiceOutputService(RivaTtsClient())
    )
    
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages
    
    private val _state = MutableStateFlow<TaskState>(TaskState.IDLE)
    val state: StateFlow<TaskState> = _state
    
    private val _currentModel = MutableStateFlow<ModelInfo?>(null)
    val currentModel: StateFlow<ModelInfo?> = _currentModel
    
    val voiceViewModel: VoiceViewModel get() = voice
    
    private val _availableModels = MutableStateFlow<List<ModelInfo>>(emptyList())
    val availableModels: StateFlow<List<ModelInfo>> = _availableModels
    
    private val _streamingContent = MutableStateFlow<String>("")
    val streamingContent: StateFlow<String> = _streamingContent
    
    private val _useFusion = MutableStateFlow<Boolean>(false)
    val useFusion: StateFlow<Boolean> = _useFusion
    
    private val _aiMode = MutableStateFlow<AiMode>(AiMode.AUTO)
    val aiMode: StateFlow<AiMode> = _aiMode
    
    private var currentConversationId: String? = null
    private var currentApiKeys: Map<String, ApiKeyEntry> = emptyMap()
    private var currentScreenState: com.aiva.core.observation.ScreenState? = null
    
    init {
        loadAvailableModels()
        observeVoice()
    }
    
    private fun loadAvailableModels() {
        val enabledModels = apiKeyManager.getAllKeys()
            .flatMap { it.models }
            .toSet()
        _availableModels.value = modelRegistry.getEnabledModels(enabledModels)
        
        // Set default model
        _currentModel.value = _availableModels.value.firstOrNull()
        if (_currentModel.value != null) {
            loadApiKeysForModel(_currentModel.value!!.id)
        }
    }
    
    private fun observeVoice() {
        viewModelScope.launch {
            voiceViewModel.recognizedText.collect { text ->
                if (text.isNotBlank()) {
                    sendMessage(text)
                }
            }
        }
        
        viewModelScope.launch {
            voiceViewModel.voiceState.collect { voiceState ->
                // Sync voice state with conversation state
            }
        }
    }
    
    fun setModel(model: ModelInfo) {
        _currentModel.value = model
        loadApiKeysForModel(model.id)
    }
    
    private fun loadApiKeysForModel(modelId: String) {
        currentApiKeys = apiKeyManager.getAllKeys()
            .filter { it.enabled && it.models.contains(modelId) }
            .associateBy { it.id }
        
        if (currentApiKeys.isEmpty()) {
            // Fallback to any available key
            currentApiKeys = apiKeyManager.getAllKeys()
                .filter { it.enabled }
                .associateBy { it.id }
        }
    }
    
    fun setAiMode(mode: AiMode) {
        _aiMode.value = mode
    }
    
    fun setUseFusion(use: Boolean) {
        _useFusion.value = use
    }
    
    fun sendMessage(content: String, intent: Intent? = null) {
        val model = _currentModel.value ?: return
        
        if (currentApiKeys.isEmpty()) {
            _state.value = TaskState.ERROR
            return
        }
        
        val userMessage = ChatMessage(role = "user", content = content)
        _messages.value = _messages.value + userMessage
        _state.value = TaskState.UNDERSTANDING
        _streamingContent.value = ""
        
        viewModelScope.launch {
            // Check if this is an action request
            val detectedIntent = intent ?: classifyIntent(content)
            
            if (isActionIntent(detectedIntent)) {
                executeAction(detectedIntent, content)
            } else if (_useFusion.value && _aiMode.value == AiMode.MULTI_MODEL_FUSION) {
                executeFusion(content)
            } else {
                executeChat(model, detectedIntent)
            }
        }
    }
    
    private fun classifyIntent(content: String): Intent {
        // Simple heuristic - in real implementation uses IntentClassifier
        val lower = content.lowercase()
        return when {
            lower.matches("""^(open|launch|click|tap|type|scroll|swipe|go|navigate|fill|submit|play|auto).*""".toRegex()) ->
                Intent(com.aiva.core.task.IntentType.ACTION, 0.8f, content)
            lower.matches("""^(what|who|where|when|why|how|define|explain|tell me).*""".toRegex()) ->
                Intent(com.aiva.core.task.IntentType.QUESTION, 0.7f, content)
            else -> Intent(com.aiva.core.task.IntentType.CHAT, 0.5f, content)
        }
    }
    
    private fun isActionIntent(intent: Intent): Boolean {
        return intent.type in setOf(
            com.aiva.core.task.IntentType.ACTION,
            com.aiva.core.task.IntentType.AUTOMATION,
            com.aiva.core.task.IntentType.FORM_FILL,
            com.aiva.core.task.IntentType.NAVIGATION,
            com.aiva.core.task.IntentType.GAME_CONTROL,
            com.aiva.core.task.IntentType.MULTI_STEP_WORKFLOW
        )
    }
    
    private suspend fun executeAction(intent: Intent, originalQuery: String) {
        _state.value = TaskState.PLANNING
        
        val execution = taskExecutor.executeTask(
            userInput = originalQuery,
            screenState = currentScreenState,
            apiKeys = currentApiKeys,
            context = com.aiva.task.executor.TaskExecutor.ExecutorContext(
                currentApp = currentScreenState?.packageName,
                currentScreen = currentScreenState?.activityName,
                activeGameProfile = null
            )
        )
        
        // Add result to conversation
        val resultMessage = ChatMessage(
            role = "assistant",
            content = if (execution.state == TaskState.SUCCESS) 
                "Task completed: ${execution.completedSteps.lastOrNull()?.result?.message ?: "Done"}"
            else
                "Task failed: ${execution.completedSteps.lastOrNull()?.result?.message ?: "Unknown error"}"
        )
        _messages.value = _messages.value + resultMessage
        _state.value = execution.state
        
        saveConversation()
    }
    
    private suspend fun executeFusion(content: String) {
        val model = _currentModel.value ?: return
        
        try {
            _state.value = TaskState.ACTING
            
            val request = ChatCompletionRequest(
                model = model.id,
                messages = _messages.value,
                stream = true
            )
            
            val primaryKey = currentApiKeys.values.firstOrNull() ?: return
            
            // For fusion, use multiple models
            val modelIds = _availableModels.value
                .filter { currentApiKeys.containsKey(it.id) }
                .take(3)
                .map { it.id }
            
            if (modelIds.size >= 2) {
                val fusionResult = multiModelFusion.executeFusion(
                    apiKeys = currentApiKeys,
                    request = request.copy(stream = false),
                    modelIds = modelIds
                )
                
                val assistantMessage = ChatMessage(role = "assistant", content = fusionResult.fusedResponse)
                _messages.value = _messages.value + assistantMessage
                _state.value = TaskState.SUCCESS
                
                // Auto-speak if enabled
                if (voiceViewModel.config.value.ttsEnabled && voiceViewModel.config.value.autoSpeak) {
                    voiceViewModel.speak(fusionResult.fusedResponse)
                }
            } else {
                // Fallback to single model
                executeChat(model, Intent(com.aiva.core.task.IntentType.CHAT, 1f, content))
            }
            
            saveConversation()
            
        } catch (e: Exception) {
            _state.value = TaskState.ERROR
            tryFallbackModels(ChatCompletionRequest(
                model = model.id,
                messages = _messages.value,
                stream = true
            ))
        }
    }
    
    private suspend fun executeChat(model: ModelInfo, intent: Intent) {
        val request = ChatCompletionRequest(
            model = model.id,
            messages = _messages.value,
            stream = true
        )
        
        val primaryKey = currentApiKeys.values.firstOrNull() ?: return
        
        try {
            _state.value = TaskState.ACTING
            
            nimClient.createStreamCompletion(primaryKey, request)
                .collect { chunk ->
                    val delta = chunk.choices.firstOrNull()?.delta?.content ?: return@collect
                    _streamingContent.value += delta
                    
                    val messages = _messages.value.toMutableList()
                    if (messages.lastOrNull()?.role == "assistant") {
                        messages[messages.lastIndex] = messages.last().copy(
                            content = _streamingContent.value
                        )
                    } else {
                        messages.add(ChatMessage(role = "assistant", content = _streamingContent.value))
                    }
                    _messages.value = messages
                }
            
            saveConversation()
            _state.value = TaskState.SUCCESS
            
            // Auto-speak if enabled
            if (voiceViewModel.config.value.ttsEnabled && voiceViewModel.config.value.autoSpeak) {
                voiceViewModel.speak(_streamingContent.value)
            }
            
        } catch (e: Exception) {
            _state.value = TaskState.ERROR
            tryFallbackModels(request)
        }
    }
    
    private suspend fun tryFallbackModels(request: ChatCompletionRequest) {
        val model = _currentModel.value ?: return
        val fallbackModels = modelRouter.getFallbackChain(model.id, currentApiKeys.keys.toSet())
        
        for (fallbackModel in fallbackModels) {
            val apiKey = currentApiKeys[fallbackModel.id] ?: continue
            try {
                _state.value = TaskState.ACTING
                _streamingContent.value = ""
                
                val fallbackRequest = request.copy(model = fallbackModel.id)
                
                nimClient.createStreamCompletion(apiKey, fallbackRequest)
                    .collect { chunk ->
                        val delta = chunk.choices.firstOrNull()?.delta?.content ?: return@collect
                        _streamingContent.value += delta
                        
                        val messages = _messages.value.toMutableList()
                        if (messages.lastOrNull()?.role == "assistant") {
                            messages[messages.lastIndex] = messages.last().copy(
                                content = _streamingContent.value
                            )
                        } else {
                            messages.add(ChatMessage(role = "assistant", content = _streamingContent.value))
                        }
                        _messages.value = messages
                    }
                
                saveConversation()
                _currentModel.value = fallbackModel
                _state.value = TaskState.SUCCESS
                return
                
            } catch (e: Exception) {
                continue
            }
        }
        
        _state.value = TaskState.ERROR
    }
    
    private suspend fun saveConversation() {
        if (_messages.value.isNotEmpty()) {
            val conversation = if (currentConversationId != null) {
                conversationRepository.get(currentConversationId!!)?.copy(
                    messagesJson = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }.encodeToString(
                        kotlinx.serialization.builtins.ListSerializer(ChatMessage.serializer()),
                        _messages.value
                    ),
                    updatedAt = System.currentTimeMillis()
                ) ?: conversationRepository.createNew(
                    "Conversation ${java.text.SimpleDateFormat("MM/dd HH:mm").format(java.util.Date())}",
                    _currentModel.value?.id ?: "unknown",
                    _messages.value
                )
            } else {
                conversationRepository.createNew(
                    "Conversation ${java.text.SimpleDateFormat("MM/dd HH:mm").format(java.util.Date())}",
                    _currentModel.value?.id ?: "unknown",
                    _messages.value
                )
            }
            
            conversationRepository.save(conversation)
            currentConversationId = conversation.id
        }
    }
    
    fun newConversation() {
        _messages.value = emptyList()
        _streamingContent.value = ""
        currentConversationId = null
        _state.value = TaskState.IDLE
    }
    
    fun stopGeneration() {
        _state.value = TaskState.STOPPED
        taskExecutor.stop()
        voice.interrupt()
    }
    
    fun updateAvailableModels(enabledModelIds: Set<String>) {
        _availableModels.value = modelRegistry.getEnabledModels(enabledModelIds)
    }
    
    fun updateScreenState(screenState: com.aiva.core.observation.ScreenState) {
        currentScreenState = screenState
    }
    
    enum class AiMode {
        AUTO,
        FASTEST,
        BEST_REASONING,
        MULTI_MODEL_FUSION,
        AGENT
    }
}