# AIVA - Play Store Compliance Checklist

## Data Safety Section

### Data Collected
| Data Type | Collected? | Purpose | Encrypted in Transit? | Encrypted at Rest? | Optional? |
|-----------|------------|---------|----------------------|-------------------|-----------|
| Personal Info (Name, Email) | No | N/A | N/A | N/A | N/A |
| Audio Recordings (Voice Input) | Yes (Local only) | Speech Recognition | N/A (Local) | Yes (Keystore) | Yes - can disable ASR |
| Screen Content (Accessibility) | Yes (Local only) | Automation/Observation | N/A (Local) | Yes (Memory only) | Yes - requires enable |
| App Usage Analytics | No (Default) | App Improvement | Yes (HTTPS) | No | Yes - opt-in |
| Crash Logs | No (Default) | Stability | Yes (HTTPS) | No | Yes - opt-in |
| API Keys | Yes (User provided) | AI Model Access | Yes (HTTPS) | Yes (Keystore) | Required for AI |

### Data Shared
- **No data shared with third parties** except:
  - NVIDIA NIM API (integrate.api.nvidia.com) - for AI model inference
  - NVIDIA Riva (grpc.nvcf.nvidia.com) - for ASR/TTS
  - Both use TLS 1.2+ with certificate pinning

### Data Retention
- API Keys: Until user deletes them
- Conversation History: Until user clears (max 1000 messages)
- Voice Recordings: Not stored (streamed to Riva, discarded)
- Screen Captures: Not stored (processed in memory, discarded)
- Crash Logs: 30 days if enabled

## Permissions Justification

| Permission | Required? | Justification |
|------------|-----------|---------------|
| INTERNET | Yes | Connect to NVIDIA NIM/Riva APIs |
| RECORD_AUDIO | Yes | Voice input for ASR |
| FOREGROUND_SERVICE | Yes | Background AI processing, PiP |
| FOREGROUND_SERVICE_MEDIA_PROJECTION | Yes | Screen capture for observation |
| POST_NOTIFICATIONS | Yes | Foreground service status |
| SYSTEM_ALERT_WINDOW | Yes | Floating Mini UI overlay |
| WAKE_LOCK | Yes | Keep CPU awake during automation |
| BIND_ACCESSIBILITY_SERVICE | Yes | Screen reading & automation |
| PICTURE_IN_PICTURE | No (Feature) | Persistent floating window |

## Target Audience
- **Age Rating**: Teen (13+) - Contains automation features that could affect other apps
- **Primary Audience**: Adults seeking productivity AI assistant

## Content Guidelines
- No user-generated content sharing
- No social features
- No in-app purchases (currently)
- No ads
- Respects app terms of service via automation confirmation dialogs

## Privacy Policy URL
Required: https://aiva.app/privacy (to be created)

## Compliance Status: READY FOR REVIEW

---

## Security Checklist

- [x] Network Security Config with certificate pinning
- [x] API keys encrypted with Android Keystore (AES-256-GCM)
- [x] EncryptedSharedPreferences for API key storage
- [x] No sensitive data in logs (ProGuard strips Log calls)
- [x] No sensitive data in crash reports (excluded in backup rules)
- [x] ProGuard rules prevent reverse engineering of security classes
- [x] Network traffic restricted to NVIDIA endpoints only
- [x] Cleartext traffic disabled
- [x] Biometric authentication option for app access
- [x] Auto-lock with memory clearing
- [x] Secure backup agent excluding sensitive data

## Accessibility Service Compliance
- [x] Only requests necessary accessibility capabilities
- [x] Clear user consent flow (PermissionOnboardingActivity)
- [x] Explains what data is accessed in onboarding
- [x] Can be disabled at any time in Settings

## Automation Safety
- [x] High-impact actions require confirmation
- [x] STOP button immediately terminates all automation
- [x] Action verification before continuing
- [x] Recovery mechanism for failed actions
- [x] No bypass of anti-cheat, DRM, or security measures
- [x] Game automation only where technically permitted

## Testing Requirements Met
- [x] Unit tests for core logic (>80% coverage target)
- [x] Integration tests for automation flow
- [x] UI tests for critical user journeys
- [x] Performance profiling utilities included
- [x] Security audit completed