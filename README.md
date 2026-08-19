# AIVA - Android AI Agent

A production-quality Android AI assistant combining conversational AI, voice interaction, multi-model reasoning, screen understanding, Android automation, and real-time game interaction.

## Architecture

```
AIVA Application
├── Core Modules (11)
│   ├── aiva-core          # Shared types, actions, models, utilities
│   ├── aiva-ai            # NIM client, ModelRegistry, Router, Multi-Model Fusion
│   ├── aiva-security      # API Key Manager (Keystore), Permission Manager
│   ├── aiva-memory        # Room DB, repositories
│   ├── aiva-conversation  # ConversationViewModel, streaming, fallback
│   ├── aiva-voice         # Riva ASR/TTS gRPC clients, Voice services
│   ├── aiva-automation    # AccessibilityService, ActionExecutor
│   ├── aiva-observation   # Screen reading + Vision (MediaPipe/TFLite)
│   ├── aiva-task          # IntentClassifier, TaskPlanner, TaskExecutor
│   ├── aiva-game          # Real-time game engine, vision, touch control
│   └── aiva-ui            # MainActivity, PiP, Mini UI, Foreground services
```

## Features Implemented

### Phase 1-3: Foundation, Voice, Automation
- ✅ **NIM Client**: Streaming + non-streaming chat completions with 9 NVIDIA models
- ✅ **Model Registry**: All models with capabilities (speed, reasoning, agent, vision)
- ✅ **Model Router**: Intent→model routing with fallback chains
- ✅ **Multi-Model Fusion**: Parallel execution, critic fusion, conflict detection
- ✅ **Secure API Keys**: Android Keystore + EncryptedSharedPreferences
- ✅ **Riva ASR**: Whisper Large V3 via gRPC (grpc.nvcf.nvidia.com:443)
- ✅ **Riva TTS**: Chatterbox Multilingual via gRPC
- ✅ **AccessibilityService**: Full node traversal, 17 action types
- ✅ **ActionExecutor**: Validate→Execute→Verify→Recover loop
- ✅ **Task Planner**: LLM-generated JSON plans
- ✅ **Task Executor**: Observe→Plan→Act→Verify loop with STOP

### Phase 4-5: Floating UI
- ✅ **MainActivity**: Compose UI with AI mode selector, fusion toggle
- ✅ **PiP Activity**: Native Picture-in-Picture with expand/collapse
- ✅ **MiniAivaService**: Overlay window with drag, voice + text input
- ✅ **Foreground Services**: MediaProjection, Microphone

### Phase 6: Game Engine
- ✅ **GameEngine**: 16ms frame loop (60fps target)
- ✅ **GameVisionProcessor**: MediaPipe object detection + template matching + color detection
- ✅ **TouchController**: MotionEvent injection via AccessibilityService

## Tech Stack

- **Language**: Kotlin 2.0
- **UI**: Jetpack Compose + Material 3
- **Architecture**: MVVM + Clean Architecture + Hilt DI
- **Async**: Coroutines + Flow
- **Network**: OkHttp + Retrofit + gRPC (Kotlin)
- **Database**: Room
- **Security**: Android Keystore + EncryptedSharedPreferences
- **Vision**: MediaPipe Tasks Vision + TensorFlow Lite
- **Build**: Gradle Kotlin DSL + Version Catalogs

## NVIDIA Models Supported

| Model | Speed | Reasoning | Agent | Use Case |
|-------|-------|-----------|-------|----------|
| z-ai/glm-5.2 | Fast | Advanced | ✅ | General |
| poolside/laguna-xs-2.1 | Fastest | Basic | ✅ | Fast coding |
| nvidia/nemotron-3.5-lightning-30b-a3b | Fastest | Advanced | ✅ | Real-time |
| meta/muse-glimmer-30b | Fast | Advanced | ❌ | Creative |
| google/gemma-4-31b-it | Balanced | Advanced | ✅ | General |
| google/diffusiongemma-26b-a4b-it | Fast | Basic | ❌ | Diffusion |
| nvidia/nemotron-3-ultra-550b-a55b | Slow | Expert | ✅ | Complex reasoning |
| thinkingmachines/inkling | Balanced | Advanced | ❌ | General |
| openai/gpt-oss-120b | Balanced | Expert | ✅ | Open source |

## Build

```bash
# Requires Android SDK (API 34) and Java 17
./gradlew :aiva-ui:assembleDebug
```

## Project Structure

```
/workspace/aiva/
├── settings.gradle.kts
├── build.gradle.kts
├── gradle/libs.versions.toml
├── gradle.properties
├── local.properties          # sdk.dir=/opt/android-sdk
├── gradlew / gradlew.bat
├── gradle/wrapper/
├── aiva-core/
├── aiva-ai/
├── aiva-security/
├── aiva-memory/
├── aiva-conversation/
├── aiva-voice/
├── aiva-automation/
├── aiva-observation/
├── aiva-task/
├── aiva-game/
└── aiva-ui/
```

## Security

- API keys never hardcoded - entered via secure settings UI
- Android Keystore for encryption keys
- EncryptedSharedPreferences for key storage
- ProGuard rules strip key references
- No sensitive data in logs/crashes
- Network security config prevents leakage

## Next Steps (Phases 7-8)

1. **Settings UI**: Complete API key management, model selection, voice config
2. **Game Profiles**: CoD/PUBG calibration UI with normalized coordinates
3. **Testing**: Unit tests (80%+), integration tests, UI tests
4. **Performance**: Profile UI rendering, optimize frame processing
5. **Play Store**: Compliance review, crash reporting (no sensitive data)
6. **Documentation**: User guide, API reference# Build fix
