# AIVA Architecture Document

## System Overview

AIVA is a modular Android AI assistant built with Clean Architecture principles, using Kotlin, Jetpack Compose, and Hilt for dependency injection. The system is organized into 11 independent modules with clear separation of concerns.

---

## Module Dependency Graph

```
                    ┌─────────────────┐
                    │    aiva-ui      │ ← Application entry point
                    └────────┬────────┘
                             │
        ┌────────────────────┼────────────────────┐
        │                    │                    │
┌───────▼───────┐    ┌───────▼───────┐    ┌───────▼───────┐
│ aiva-        │    │ aiva-         │    │ aiva-         │
│ conversation  │    │ voice         │    │ automation    │
└───────┬───────┘    └───────┬───────┘    └───────┬───────┘
        │                    │                    │
        └────────────────────┼────────────────────┘
                             │
                    ┌────────▼────────┐
                    │    aiva-core    │ ← Shared types, models, actions
                    └────────┬────────┘
                             │
        ┌────────────────────┼────────────────────┐
        │                    │                    │
┌───────▼───────┐    ┌───────▼───────┐    ┌───────▼───────┐
│ aiva-ai      │    │ aiva-security │    │ aiva-memory   │
└───────┬───────┘    └───────┬───────┘    └───────┬───────┘
        │                    │                    │
        └────────────────────┼────────────────────┘
                             │
                    ┌────────▼────────┐
                    │ aiva-task       │ ← Intent→Plan→Execute
                    └────────┬────────┘
                             │
              ┌──────────────┼──────────────┐
              │              │              │
       ┌──────▼──────┐ ┌─────▼─────┐ ┌─────▼─────┐
       │ aiva-       │ │ aiva-     │ │ aiva-game │
       │ observation │ │ automation│ └───────────┘
       └─────────────┘ └───────────┘
```

---

## Module Responsibilities

### aiva-core
**Shared foundation** - No Android dependencies
- Action language (17 action types)
- Target identification with normalized coordinates
- ScreenState, GameState, Observation models
- Task/Intent models
- Security models (ApiKeyEntry, KeyTestResult)
- Voice models (VoiceConfig, AsrResult, TtsRequest)
- Game models (GameProfile, GameControls, Calibration)
- Utilities: SecureStorage (Keystore), KeyStoreManager, JsonUtil

### aiva-ai
**AI Model Integration**
- `NimClient`: HTTP/gRPC client for NVIDIA NIM API
- `ModelRegistry`: 9 NVIDIA models with capabilities
- `ModelRouter`: Intent→model routing, fallback chains
- `MultiModelFusion`: Parallel execution, critic fusion, conflict detection

### aiva-security
**Security & Permissions**
- `ApiKeyManager`: Keystore encryption, EncryptedSharedPreferences, key lifecycle
- `PermissionManager`: 7 permissions (4 runtime, 3 special), onboarding flow

### aiva-memory
**Persistence Layer**
- Room database: Conversations, TaskHistory, UserPreferences, GameProfiles, ApiUsage
- Repository pattern for each entity type

### aiva-conversation
**Conversation Management**
- `ConversationViewModel`: Streaming chat, fallback models, voice integration, AI modes
- State management: messages, streaming content, model selection, task state

### aiva-voice
**Voice Pipeline (Riva ASR/TTS)**
- `RivaAsrClient`: gRPC streaming ASR (whisper-large-v3)
- `RivaTtsClient`: gRPC TTS (Chatterbox-Multilingual)
- `VoiceInputService`: AudioRecord 16kHz capture
- `VoiceOutputService`: AudioTrack 22kHz playback
- `VoiceViewModel`: Unified voice state, config management

### aiva-automation
**Android Automation via AccessibilityService**
- `AivaAccessibilityService`: Node traversal, 17 action handlers
- `ActionExecutor`: Validate→Execute→Verify→Recover loop
- `AccessibilityController`: Interface for testability

### aiva-observation
**Screen Understanding**
- Accessibility tree parsing
- Vision detection (MediaPipe/TFLite)
- Observation fusion (semantic priority)

### aiva-task
**Intent Understanding & Execution**
- `IntentClassifier`: LLM + heuristic classification
- `TaskPlanner`: LLM generates JSON action plans
- `TaskExecutor`: Observe→Plan→Act→Verify loop with STOP

### aiva-game
**Real-Time Game Control**
- `GameEngine`: 16ms frame loop (60fps target)
- `GameVisionProcessor`: MediaPipe + templates + color detection
- `TouchController`: MotionEvent injection via AccessibilityService
- Normalized coordinate calibration system

### aiva-ui
**User Interface**
- `MainActivity`: Full Compose UI with AI mode selector, fusion toggle
- `PipActivity`: Native Picture-in-Picture with expand/collapse
- `MiniAivaService`: System overlay with drag, voice+text input
- Settings screens (9 sections) with Navigation
- Permission onboarding (5-step guided flow)
- Foreground services (MediaProjection, Microphone, Game)

---

## Data Flow

### Chat Flow
```
User Input
    ↓
ConversationViewModel.sendMessage()
    ↓
IntentClassifier.classify() → CHAT vs ACTION
    ↓
CHAT: ModelRouter.routeForIntent() → Select model
    ↓
NimClient.createStreamCompletion() → Stream chunks
    ↓
Update UI with streaming content
    ↓
Save to ConversationRepository
```

### Automation Flow
```
User Input: "Open Settings and turn on Wi-Fi"
    ↓
IntentClassifier → IntentType.AUTOMATION
    ↓
TaskPlanner.createPlan() → LLM generates PlanStep[]
    ↓
TaskExecutor.executeTask()
    ↓
For each step:
  1. Observe (AccessibilityController.getScreenState())
  2. ActionExecutor.execute(action)
  3. Verify (compare pre/post ScreenState)
  4. Recovery if failed (relax target criteria, retry)
    ↓
TaskExecution with StepResult[]
    ↓
ConversationViewModel adds result message
```

### Voice Flow
```
User taps mic → VoiceViewModel.startListening()
    ↓
VoiceInputService.startListening() → AudioRecord
    ↓
RivaAsrClient.startStreamingRecognition() → gRPC stream
    ↓
AsrResult (partial → final) → VoiceViewModel.partialText/finalResults
    ↓
ConversationViewModel.sendMessage(recognizedText)
    ↓
... chat flow ...
    ↓
If autoSpeak: VoiceViewModel.speak(response)
    ↓
VoiceOutputService.speak() → RivaTtsClient.synthesize()
    ↓
AudioTrack playback
```

### Game Control Flow
```
User: "Play this match"
    ↓
GameProfile loaded → MediaProjection started
    ↓
GameEngine.start() → 16ms frame loop
    ↓
Each frame:
  1. Capture screen (MediaProjection + VirtualDisplay)
  2. GameVisionProcessor.processFrame() → detections
  3. Update GameState (player, enemies, HUD, etc.)
  4. Local control loop (Level 1-2): generate ControlCommand
  5. TouchController.sendCommand() → MotionEvent injection
  6. High-level AI (Level 3-5): strategic decisions via NIM
```

---

## Threading Model

| Component | Dispatcher | Purpose |
|-----------|------------|---------|
| UI (Compose) | `Dispatchers.Main` | State updates, recomposition |
| NIM Client | `Dispatchers.IO` | HTTP/gRPC network calls |
| Riva gRPC | `Dispatchers.IO` | Streaming ASR/TTS |
| ActionExecutor | `Dispatchers.IO` | Accessibility actions |
| TaskExecutor | `Dispatchers.IO` | Planning, execution loop |
| GameEngine | `Dispatchers.Default` | 16ms frame loop |
| Vision Processor | `Dispatchers.Default` | MediaPipe inference |
| Database | `Dispatchers.IO` | Room operations |
| Voice Audio | `Dispatchers.IO` | AudioRecord/AudioTrack |

---

## State Management

### StateFlow Patterns
```kotlin
// Immutable state exposure
private val _state = MutableStateFlow<TaskState>(TaskState.IDLE)
val state: StateFlow<TaskState> = _state

// Derived state
val voiceState: StateFlow<VoiceState> = combine(
    voiceInputService.state,
    voiceOutputService.state
) { input, output -> ... }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), VoiceState.IDLE)
```

### Single Source of Truth
- Each domain has one "source of truth" StateFlow
- UI collects with `collectAsStateWithLifecycle()`
- ViewModels never expose MutableStateFlow directly

---

## Security Architecture

### Key Management
```
User enters API key
    ↓
KeyStoreManager.generateKey() → Android Keystore (AES-256-GCM)
    ↓
KeyStoreManager.encryptString(key) → EncryptedSharedPreferences
    ↓
ApiKeyEntry stored with: id, name, keyHash, encryptedKey, models[]
    ↓
At runtime: KeyStoreManager.decryptString() → plaintext for API calls
    ↓
Plaintext NEVER:
  - Written to logs (ProGuard strips Log calls)
  - Included in crash reports (backup_rules.xml excludes)
  - Serialized to disk unencrypted
  - Sent to any server except NVIDIA endpoints
```

### Network Security
- `network_security_config.xml`: Only `integrate.api.nvidia.com` and `grpc.nvcf.nvidia.com`
- Certificate pinning configured (pins to be added at deploy)
- Cleartext traffic disabled (`android:usesCleartextTraffic="false"`)
- TLS 1.2+ enforced

### ProGuard Security
- Security classes kept with full names (`-keep class com.aiva.security.**`)
- KeyStoreManager/SecureStorage/ApiKeyManager never obfuscated
- Log calls stripped in release (`-assumenosideeffects class android.util.Log`)
- Serialization names preserved for JSON compatibility

---

## Performance Targets

| Metric | Target | Implementation |
|--------|--------|----------------|
| UI Frame Time | <16ms (60fps) | Compose, StateFlow, minimal recomposition |
| NIM First Token | <2s | Connection pooling, streaming |
| ASR Latency | <500ms | Riva streaming, 16kHz audio |
| TTS First Audio | <1s | Riva streaming synthesis |
| Action Execution | <1s | AccessibilityService direct |
| Game Frame Loop | 16ms | Frame skipping, ROI processing |
| Vision Inference | <10ms | MediaPipe GPU delegate |
| Memory Usage | <150MB | Efficient bitmaps, object pooling |

---

## Testing Strategy

| Layer | Coverage Target | Tools |
|-------|----------------|-------|
| Unit (core logic) | 80%+ | JUnit 5, MockK, Turbine |
| Integration (automation) | 70% | MockWebServer, Testcontainers |
| UI (critical paths) | 60% | Compose Test, Espresso |
| E2E (user journeys) | Manual + CI | Android Test Orchestrator |

---

## Deployment

### Build Variants
- `debug`: Debuggable, no minification, logging enabled
- `release`: Minified, ProGuard, signing, no logging

### Release Checklist
- [ ] Version code/name updated
- [ ] ProGuard mapping uploaded to Play Console
- [ ] Certificate pins updated in network_security_config.xml
- [ ] Privacy policy URL configured
- [ ] Play Store listing assets ready
- [ ] Automated tests passing
- [ ] Manual QA on target devices

---

## Future Extensibility

### Plugin Architecture (Planned)
- Custom action handlers via interface
- Game profile format extensible
- Vision model hot-swapping
- Third-party AI provider adapters

### Multi-Device (Planned)
- Wear OS companion for voice
- Tablet/desktop layout variants
- Cross-device sync (encrypted)

---

*Architecture Version: 1.0*
*Last Updated: 2026-08-19*