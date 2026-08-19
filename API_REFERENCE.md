# AIVA API Reference

## Overview

This document describes the internal APIs and data structures used by AIVA modules. External developers can use this to understand the architecture or build extensions.

---

## Core Data Structures

### Action Language (`com.aiva.core.action`)

All automation actions are represented as sealed class hierarchies, serializable to JSON.

#### Actions

```kotlin
sealed interface Action {
    data class LaunchApp(val packageName: String, val action: String?)
    data class OpenUrl(val url: String)
    data class Tap(val target: Target)
    data class DoubleTap(val target: Target)
    data class LongPress(val target: Target, val duration: Int = 1000)
    data class Swipe(val from: Point, val to: Point, val duration: Int = 300)
    data class Scroll(val direction: ScrollDirection, val target: Target?, val amount: Int = 500)
    data class Type(val text: String, val target: Target?, val replace: Boolean = false)
    data class ReplaceText(val target: Target, val newText: String)
    data class Copy(val target: Target?)
    data class Paste(val target: Target?)
    data class Back()
    data class Home()
    data class Observe(val query: String)
    data class Wait(val ms: Long)
    data class Find(val target: Target)
    data class Select(val target: Target, val option: String)
    data class Finish(val result: String)
    data class AskUser(val question: String)
    data class Stop()
}
```

#### Target Identification

```kotlin
data class Target(
    val text: String? = null,
    val resourceId: String? = null,
    val contentDescription: String? = null,
    val className: String? = null,
    val index: Int? = null,
    val visionHint: String? = null,
    val normalizedBounds: NormalizedBounds? = null
) {
    fun hasAnyCriteria(): Boolean
    fun copy(...): Target
}

data class NormalizedBounds(
    val left: Float, val top: Float, 
    val right: Float, val bottom: Float
) {
    fun center(): Point
    fun width(): Float
    fun height(): Float
    fun contains(x: Float, y: Float): Boolean
}

data class Point(val x: Float, val y: Float)
```

#### Action Result

```kotlin
data class ActionResult(
    val success: Boolean,
    val message: String? = null,
    val newScreenState: ScreenState? = null
)
```

---

### Screen Observation (`com.aiva.core.observation`)

#### Screen State

```kotlin
data class ScreenState(
    val packageName: String?,
    val activityName: String?,
    val nodes: List<AccessibilityNodeSummary>,
    val screenshot: ByteArray? = null,
    val timestamp: Long = System.currentTimeMillis()
)
```

#### Accessibility Node Summary

```kotlin
data class AccessibilityNodeSummary(
    val id: Int,
    val text: String?,
    val contentDescription: String?,
    val resourceId: String?,
    val className: String?,
    val bounds: NormalizedBounds?,
    val clickable: Boolean,
    val scrollable: Boolean,
    val editable: Boolean,
    val checkable: Boolean,
    val checked: Boolean,
    val focused: Boolean,
    val selected: Boolean,
    val enabled: Boolean,
    val visible: Boolean,
    val children: List<AccessibilityNodeSummary> = emptyList()
)
```

#### Vision Detection

```kotlin
data class VisionDetection(
    val label: String,
    val confidence: Float,
    val bounds: NormalizedBounds,
    val category: DetectionCategory
)

enum class DetectionCategory {
    BUTTON, ICON, TEXT, MENU, DIALOG, FORM_FIELD,
    GAME_HUD, HEALTH_BAR, AMMO, MINIMAP, CROSSHAIR,
    JOYSTICK, FIRE_BUTTON, VEHICLE_CONTROL, INVENTORY,
    PLAYER, ENEMY, ALLY, OBJECTIVE, UNKNOWN
}
```

#### Observation Result

```kotlin
data class ObservationResult(
    val screenState: ScreenState,
    val visionDetections: List<VisionDetection> = emptyList(),
    val accessibilityTree: AccessibilityTree? = null,
    val timestamp: Long = System.currentTimeMillis()
)
```

---

### Task System (`com.aiva.core.task`)

#### Intent Classification

```kotlin
enum class IntentType {
    CHAT, QUESTION, EXPLANATION, ACTION, AUTOMATION,
    FORM_FILL, QUIZ, NAVIGATION, GAME_CONTROL,
    SCREEN_ANALYSIS, MULTI_STEP_WORKFLOW
}

data class Intent(
    val type: IntentType,
    val confidence: Float,
    val originalQuery: String,
    val extractedEntities: Map<String, String> = emptyMap(),
    val requiresConfirmation: Boolean = false,
    val targetApp: String? = null
)
```

#### Task Planning

```kotlin
data class TaskPlan(
    val id: String,
    val intent: Intent,
    val steps: List<PlanStep>,
    val estimatedDuration: Long,
    val requiredCapabilities: Set<String> = emptySet()
)

data class PlanStep(
    val id: String,
    val action: Action,
    val description: String,
    val expectedOutcome: String,
    val fallbackAction: Action? = null,
    val verificationQuery: String? = null
)
```

#### Task Execution

```kotlin
enum class TaskState {
    IDLE, LISTENING, UNDERSTANDING, PLANNING, OBSERVING,
    ACTING, VERIFYING, WAITING, SUCCESS, ERROR, STOPPED
}

data class TaskExecution(
    val plan: TaskPlan,
    val currentStepIndex: Int = 0,
    val completedSteps: List<StepResult> = emptyList(),
    val state: TaskState = TaskState.PLANNING,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null
)

data class StepResult(
    val stepId: String,
    val action: Action,
    val success: Boolean,
    val result: ActionResult?,
    val observation: ObservationResult?,
    val timestamp: Long = System.currentTimeMillis(),
    val retries: Int = 0
)
```

---

### Models (`com.aiva.core.model`)

```kotlin
data class ModelInfo(
    val id: String,
    val displayName: String,
    val provider: String,
    val speedProfile: SpeedProfile,
    val reasoningCapability: ReasoningLevel,
    val agentSuitability: Boolean,
    val visionCapability: Boolean,
    val streamingCapability: Boolean,
    val enabled: Boolean = true
)

enum class SpeedProfile { FASTEST, FAST, BALANCED, SLOW }
enum class ReasoningLevel { NONE, BASIC, ADVANCED, EXPERT }

data class ModelCapabilities(
    val supportsStreaming: Boolean,
    val supportsVision: Boolean,
    val supportsReasoning: Boolean,
    val supportsAgent: Boolean,
    val maxTokens: Int,
    val supportsChatTemplateKwargs: Boolean,
    val supportsReasoningBudget: Boolean
)

data class ChatMessage(
    val role: String,        // "system" | "user" | "assistant"
    val content: String?,
    val reasoningContent: String? = null  // Hidden from user
)

data class ChatCompletionRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val temperature: Float = 1.0f,
    val topP: Float = 1.0f,
    val maxTokens: Int = 16384,
    val seed: Long? = null,
    val stream: Boolean = false,
    val chatTemplateKwargs: Map<String, Any>? = null,
    val reasoningBudget: Int? = null
)

data class ChatCompletionResponse(
    val id: String,
    val choices: List<Choice>,
    val created: Long,
    val model: String,
    val usage: Usage?
)

data class Choice(
    val index: Int,
    val message: ChatMessage?,
    val delta: ChatMessage?,
    val finishReason: String?
)
```

---

### Security (`com.aiva.core.security`)

```kotlin
data class ApiKeyEntry(
    val id: String,
    val name: String,
    val keyHash: String,
    val encryptedKey: String,
    val models: List<String>,
    val enabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val lastTested: Long? = null,
    val testResult: KeyTestResult? = null
)

enum class KeyTestResult {
    UNTESTED, TESTING, SUCCESS, FAILED,
    INVALID_KEY, RATE_LIMITED, NETWORK_ERROR
}

sealed interface SecurityEvent {
    data class KeyAccessed(val keyId: String) : SecurityEvent
    data class KeyTested(val keyId: String, val success: Boolean) : SecurityEvent
    data class KeyAdded(val keyId: String) : SecurityEvent
    data class KeyRemoved(val keyId: String) : SecurityEvent
    data class FailedAttempt(val reason: String) : SecurityEvent
    data class BiometricPromptShown : SecurityEvent
    data class BiometricSuccess : SecurityEvent
    data class BiometricFailed : SecurityEvent
}
```

---

### Voice (`com.aiva.core.voice`)

```kotlin
data class VoiceConfig(
    val asrEnabled: Boolean = true,
    val ttsEnabled: Boolean = true,
    val autoSpeak: Boolean = false,
    val language: String = "en-US",
    val voiceName: String = "Chatterbox-Multilingual.en-US.Male",
    val speechSpeed: Float = 1.0f,
    val volume: Float = 1.0f,
    val interruptOnUserSpeech: Boolean = true,
    val vadSensitivity: Float = 0.5f
)

data class AsrResult(
    val text: String,
    val confidence: Float,
    val language: String?,
    val isFinal: Boolean,
    val alternatives: List<String> = emptyList()
)

data class TtsRequest(
    val text: String,
    val voice: String = "Chatterbox-Multilingual.en-US.Male",
    val language: String = "en-US",
    val speed: Float = 1.0f,
    val volume: Float = 1.0f
)

enum class VoiceState { IDLE, LISTENING, PROCESSING, SPEAKING, INTERRUPTED, ERROR }
```

---

### Game (`com.aiva.core.game`)

```kotlin
data class GameProfile(
    val id: String,
    val name: String,
    val packageName: String,
    val activityNames: List<String>,
    val screenWidth: Int,
    val screenHeight: Int,
    val controls: GameControls,
    val detectionRegions: List<DetectionRegion>,
    val colorProfiles: Map<String, ColorProfile>,
    val calibration: CalibrationData?,
    val version: Int = 1
)

data class GameControls(
    val movementJoystick: ControlRegion,
    val aimArea: ControlRegion,
    val fireButton: ControlRegion,
    val reloadButton: ControlRegion?,
    val jumpButton: ControlRegion?,
    val crouchButton: ControlRegion?,
    val proneButton: ControlRegion?,
    val weaponSwitch: ControlRegion?,
    val mapButton: ControlRegion?,
    val inventoryButton: ControlRegion?,
    val interactButton: ControlRegion?,
    val vehicleControls: Map<String, ControlRegion> = emptyMap()
)

data class ControlRegion(
    val normalizedBounds: NormalizedBounds,
    val touchType: TouchType = TouchType.TAP,
    val swipeDirection: SwipeDirection? = null,
    val holdDuration: Int = 0
)

enum class TouchType { TAP, DOUBLE_TAP, LONG_PRESS, SWIPE, HOLD, JOYSTICK }
enum class SwipeDirection { UP, DOWN, LEFT, RIGHT, UP_LEFT, UP_RIGHT, DOWN_LEFT, DOWN_RIGHT }

data class GameState(
    val gameProfile: String?,
    val playerPosition: Point?,
    val crosshairPosition: Point?,
    val health: Int?, val armor: Int?, val ammo: Int?,
    val currentWeapon: String?,
    val enemies: List<GameEntity>,
    val allies: List<GameEntity>,
    val minimapData: MinimapData?,
    val objectives: List<GameObjective>,
    val matchState: MatchState,
    val controls: GameControls?
)
```

---

## Module APIs

### NIM Client (`com.aiva.ai.client.NimClient`)

```kotlin
interface NimClient {
    suspend fun createCompletion(
        apiKeyEntry: ApiKeyEntry,
        request: ChatCompletionRequest
    ): ChatCompletionResponse

    fun createStreamCompletion(
        apiKeyEntry: ApiKeyEntry,
        request: ChatCompletionRequest
    ): Flow<StreamChunk>
    
    fun invalidateCache()
}
```

### Model Registry (`com.aiva.ai.registry.ModelRegistry`)

```kotlin
interface ModelRegistry {
    fun getAllModels(): List<ModelInfo>
    fun getModel(id: String): ModelInfo?
    fun getCapabilities(id: String): ModelCapabilities?
    fun getEnabledModels(enabledIds: Set<String>): List<ModelInfo>
    fun getModelsForTask(taskType: TaskType, enabledIds: Set<String>): List<ModelInfo>
    fun getFallbackChain(primaryModelId: String, enabledIds: Set<String>): List<ModelInfo>
}
```

### Model Router (`com.aiva.ai.router.ModelRouter`)

```kotlin
interface ModelRouter {
    fun routeForIntent(
        intent: Intent,
        enabledModelIds: Set<String>,
        userPreferredModel: String?
    ): ModelInfo?
    
    fun routeForFusion(
        intent: Intent,
        enabledModelIds: Set<String>
    ): List<ModelInfo>
    
    fun getFallbackChain(
        primaryModelId: String,
        enabledModelIds: Set<String>
    ): List<ModelInfo>
}
```

### Multi-Model Fusion (`com.aiva.ai.fusion.MultiModelFusion`)

```kotlin
interface MultiModelFusion {
    suspend fun executeFusion(
        apiKeys: Map<String, ApiKeyEntry>,
        request: ChatCompletionRequest,
        modelIds: List<String>,
        fusionStrategy: FusionStrategy
    ): FusionResult

    fun executeStreamingFusion(
        apiKeys: Map<String, ApiKeyEntry>,
        request: ChatCompletionRequest,
        modelIds: List<String>
    ): Flow<FusionProgress>
}

data class FusionResult(
    val fusedResponse: String,
    val modelResponses: Map<String, String>,
    val conflicts: List<Conflict>,
    val confidence: Float
)

data class Conflict(
    val models: List<String>,
    val description: String,
    val resolution: String
)

enum class FusionStrategy { CRITIC_FUSION, MAJORITY_VOTE, BEST_REASONING, CONCATENATE }
```

### API Key Manager (`com.aiva.security.ApiKeyManager`)

```kotlin
interface ApiKeyManager {
    val keys: StateFlow<Map<String, ApiKeyEntry>>
    val config: StateFlow<SecurityConfig>
    val events: StateFlow<List<SecurityEvent>>
    
    fun addKey(name: String, plainKey: String, models: List<String>): ApiKeyEntry
    fun replaceKey(id: String, plainKey: String, models: List<String>?): Boolean
    fun deleteKey(id: String): Boolean
    fun getKey(id: String): ApiKeyEntry?
    fun getDecryptedKey(id: String): String?
    fun testKey(id: String, testCallback: (KeyTestResult) -> Unit)
    fun enableModel(keyId: String, modelId: String): Boolean
    fun disableModel(keyId: String, modelId: String): Boolean
    fun getKeysForModel(modelId: String): List<ApiKeyEntry>
    fun getAllKeys(): List<ApiKeyEntry>
    fun updateConfig(config: SecurityConfig)
    fun clearAllData()
}
```

### Accessibility Controller (`com.aiva.automation.accessibility.AccessibilityController`)

```kotlin
interface AccessibilityController {
    fun executeAction(action: Action): ActionResult
    val screenState: StateFlow<ScreenState?>
    val serviceEnabled: StateFlow<Boolean>
    fun getCurrentScreenState(): ScreenState?
    fun cancelCurrentAction()
}
```

### Action Executor (`com.aiva.automation.executor.ActionExecutor`)

```kotlin
interface ActionExecutor {
    val state: StateFlow<TaskState>
    val lastResult: StateFlow<ActionResult?>
    
    suspend fun execute(action: Action): ActionResult
    fun executeAll(actions: List<Action>): List<ActionResult>
    fun setMaxRetries(retries: Int)
    fun setRetryDelay(delayMs: Long)
    fun cancel()
}
```

### Task Executor (`com.aiva.task.executor.TaskExecutor`)

```kotlin
interface TaskExecutor {
    val state: StateFlow<TaskState>
    val currentExecution: StateFlow<TaskExecution?>
    val progress: StateFlow<String>
    
    suspend fun executeTask(
        userInput: String,
        screenState: ScreenState?,
        apiKeys: Map<String, ApiKeyEntry>,
        context: ExecutorContext
    ): TaskExecution
    
    fun stop()
}
```

### Voice ViewModel (`com.aiva.voice.viewmodel.VoiceViewModel`)

```kotlin
interface VoiceViewModel {
    val config: StateFlow<VoiceConfig>
    val isListening: StateFlow<Boolean>
    val isSpeaking: StateFlow<Boolean>
    val partialText: StateFlow<String>
    val recognizedText: StateFlow<String>
    val voiceState: StateFlow<VoiceState>
    
    fun startListening(onResult: (String) -> Unit)
    fun stopListening(): String
    fun cancelListening()
    fun speak(text: String)
    fun stopSpeaking()
    fun interrupt()
    suspend fun updateConfig(newConfig: VoiceConfig)
    fun getAvailableKeysForAsr(): List<ApiKeyEntry>
    fun getAvailableKeysForTts(): List<ApiKeyEntry>
}
```

### Game Engine (`com.aiva.game.engine.GameEngine`)

```kotlin
interface GameEngine {
    val state: StateFlow<GameEngineState>
    val gameState: StateFlow<GameState>
    val fps: StateFlow<Int>
    
    fun start(mediaProjection: MediaProjection, profile: GameProfile)
    fun stop()
    fun sendHighLevelCommand(command: ControlCommand)
}

enum class GameEngineState { IDLE, INITIALIZING, RUNNING, PAUSED, ERROR, STOPPED }
```

### Game Vision Processor (`com.aiva.game.vision.GameVisionProcessor`)

```kotlin
interface GameVisionProcessor {
    suspend fun processFrame(
        frameData: FrameData,
        profile: GameProfile
    ): List<VisionDetection>
}
```

### Touch Controller (`com.aiva.game.control.TouchController`)

```kotlin
interface TouchController {
    fun start()
    fun stop()
    fun sendCommand(command: ControlCommand)
}
```

---

## NVIDIA API Integration

### NIM Chat Completions

**Endpoint:** `https://integrate.api.nvidia.com/v1/chat/completions`

**Headers:**
```
Authorization: Bearer <API_KEY>
Content-Type: application/json
Accept: application/json (or text/event-stream for streaming)
```

**Request Body:**
```json
{
  "model": "nvidia/nemotron-3.5-lightning-30b-a3b",
  "messages": [
    {"role": "system", "content": "You are helpful"},
    {"role": "user", "content": "Hello"}
  ],
  "temperature": 0.7,
  "top_p": 0.9,
  "max_tokens": 4096,
  "stream": true,
  "chat_template_kwargs": {"enable_thinking": true},
  "reasoning_budget": 8192
}
```

**Streaming Response (SSE):**
```
data: {"id":"...","choices":[{"index":0,"delta":{"content":"Hello"},"finish_reason":null}],"created":...,"model":"..."}
data: {"id":"...","choices":[{"index":0,"delta":{"content":" world"},"finish_reason":null}],"created":...,"model":"..."}
data: {"id":"...","choices":[{"index":0,"delta":{},"finish_reason":"stop"}],"created":...,"model":"..."}
data: [DONE]
```

### Riva ASR

**gRPC Endpoint:** `grpc.nvcf.nvidia.com:443`

**Function ID:** `b702f636-f60c-4a3d-a6f4-f3568c13bd7d`

**Model:** `whisper-large-v3`

**Metadata:**
```
authorization: Bearer <API_KEY>
function-id: b702f636-f60c-4a3d-a6f4-f3568c13bd7d
```

**Audio Format:** 16kHz, 16-bit PCM, mono

### Riva TTS

**gRPC Endpoint:** `grpc.nvcf.nvidia.com:443`

**Function ID:** `ddacc747-1269-4fab-bfd9-8f593dead106`

**Voice:** `Chatterbox-Multilingual.en-US.Male`

**Metadata:**
```
authorization: Bearer <API_KEY>
function-id: ddacc747-1269-4fab-bfd9-8f593dead106
```

**Audio Output:** 22.05kHz or 44.1kHz LINEAR16

---

## Error Codes

| Code | Description | Recovery |
|------|-------------|----------|
| `AUTH_FAILED` | Invalid API key | Re-enter key in Settings |
| `RATE_LIMITED` | Too many requests | Wait, implement backoff |
| `MODEL_UNAVAILABLE` | Model not loaded | Try fallback model |
| `ACCESSIBILITY_DISABLED` | Service not enabled | Guide user to Settings |
| `OVERLAY_DENIED` | Permission denied | Guide user to Settings |
| `MEDIA_PROJECTION_DENIED` | Screen capture denied | Re-request permission |
| `ACTION_TIMEOUT` | Action took >30s | Retry with recovery |
| `TARGET_NOT_FOUND` | UI element missing | Re-observe, relax criteria |
| `VERIFICATION_FAILED` | Action didn't produce expected result | Try fallback, ask user |

---

## Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0.0 | 2026-08-19 | Initial release |

---

*Generated from AIVA source code*