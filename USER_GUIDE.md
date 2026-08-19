# AIVA User Guide

## Getting Started

### 1. Installation
1. Download AIVA from Google Play Store
2. Open the app - you'll see the Permission Onboarding screen
3. Grant all requested permissions for full functionality:
   - **Microphone**: Voice input
   - **Accessibility**: Screen reading & automation
   - **Display over apps**: Floating Mini UI
   - **Notifications**: Background service status
   - **Screen capture**: Game control & observation

### 2. API Key Setup
1. Go to **Settings → API Keys**
2. Tap **+** to add a key
3. Enter your NVIDIA API key (format: `nvapi-...`)
4. Select which models to enable for this key
5. Tap **Test Key** to verify
6. Tap **Save**

Get your API key from: https://integrate.api.nvidia.com

### 3. First Conversation
- Type or speak your request
- AIVA will understand whether you want to **chat** or **act**

---

## Core Features

### 💬 Conversational AI
**Chat naturally with AIVA:**
- Ask questions: "What's the capital of France?"
- Get explanations: "How does photosynthesis work?"
- Creative tasks: "Write a haiku about coding"
- Coding help: "Debug this Python snippet"

### 🎤 Voice Interaction
**Talk to AIVA hands-free:**
1. Tap the **microphone** button
2. Speak your request
3. AIVA responds with voice (if TTS enabled)

**Voice Settings** (Settings → Voice):
- Enable/disable ASR (Speech Recognition)
- Enable/disable TTS (Text-to-Speech)
- Auto-speak responses
- Voice selection, speed, volume
- Interrupt on user speech

### 🤖 Android Automation
**Control your phone with natural language:**

**Examples:**
- "Open YouTube"
- "Turn on Wi-Fi"
- "Open Settings and go to Battery"
- "Scroll down"
- "Click the Settings button"
- "Type 'hello world' in the search box"
- "Take a screenshot"

**How it works:**
1. AIVA observes current screen via Accessibility
2. Plans the steps needed
3. Executes each step with verification
4. Recovers if something changes

### 📱 Floating Mini UI (PiP)
**AIVA stays accessible while you use other apps:**

1. Press **Home** or **Back** from main app
2. AIVA enters **Picture-in-Picture** mode (small circle)
3. Tap the circle → **Mini UI** expands
4. Full voice + text input available
5. Tap **Expand** → returns to full app
6. Drag to reposition, drag to bottom to dismiss

**PiP States:**
- 🔵 Blue = Idle/Ready
- 🔴 Red = Listening
- 🟡 Yellow = Thinking/Planning
- 🟢 Green = Acting
- ✅ Green check = Success
- ❌ Red X = Error

### 🎮 Game Control
**Real-time game interaction (where permitted):**

**Supported Games:**
- Call of Duty Mobile
- PUBG Mobile
- Custom touch-controlled games

**Game Modes:**
- **Observe Only** - Watch and analyze
- **Assist** - Help with aiming/movement
- **Full Automation** - Complete control (where allowed)
- **Manual** - You play, AIVA advises

**Setup:**
1. Settings → Game Profiles → Add Profile
2. Enter game package name (e.g., `com.activision.callofduty.mobile`)
3. Tap **Calibrate** - position controls on screen
4. Select mode and start

⚠️ **Important:** AIVA never bypasses anti-cheat. Use only where game permits.

### 📝 Forms & Quizzes
**Automate repetitive tasks:**
- "Fill out this form with my info"
- "Answer this quiz question"
- "Submit the application"

---

## AI Models & Modes

### Available Models (9 NVIDIA Models)
| Model | Speed | Reasoning | Best For |
|-------|-------|-----------|----------|
| Nemotron 3.5 Lightning | ⚡⚡⚡ | Advanced | Real-time, automation |
| Laguna XS 2.1 | ⚡⚡⚡ | Basic | Fast coding |
| GLM 5.2 | ⚡⚡ | Advanced | General chat |
| Muse Glimmer 30B | ⚡⚡ | Advanced | Creative |
| Gemma 4 31B | ⚡ | Advanced | Balanced |
| DiffusionGemma 26B | ⚡⚡ | Basic | Diffusion |
| Nemotron 3 Ultra | ⚡ | Expert | Complex reasoning |
| Inkling | ⚡ | Advanced | General |
| GPT-OSS 120B | ⚡ | Expert | Open source |

### AI Modes (Top Bar Selector)
- **Auto** - Smart routing (default)
- **Fastest** - Speed priority
- **Best Reasoning** - Quality priority
- **Multi-Model Fusion** - Multiple models + critic
- **Agent** - Automation optimized

### Multi-Model Fusion
When enabled, AIVA runs 2-3 models in parallel, compares outputs, and fuses the best answer. Use for:
- Complex research questions
- Critical decisions
- When accuracy matters more than speed

---

## Settings Guide

### API Keys
- Add multiple keys for different models
- Enable/disable models per key
- Test keys before saving
- Delete keys securely

### Models
- Toggle models on/off
- Set default AI mode
- View model capabilities

### Voice
- ASR/TTS toggles
- Language & voice selection
- Speed/volume sliders
- VAD sensitivity
- Auto-speak & interrupt settings

### Automation
- Accessibility service status
- Overlay permission
- Tap delay, swipe duration, scroll speed
- Max retries, action verification
- High-impact confirmation

### Game Profiles
- Add/edit game profiles
- Calibrate control positions (normalized coordinates)
- Set active profile
- Per-game mode settings

### Privacy & Security
- Biometric unlock
- Auto-lock timeout
- Clear memory on lock
- Crash reporting (opt-in)
- Usage analytics (opt-in)
- Clear all data button

---

## Tips & Tricks

### Natural Language Commands
AIVA understands context:
- "Open the second one" → knows what "the second one" refers to
- "Click that button" → finds button near previous action
- "Go back" → presses back button
- "Do the same thing" → repeats last action

### Keyboard Shortcuts (Mini UI)
- **Enter** - Send message
- **Esc** - Collapse Mini UI
- **Ctrl+Space** - Toggle voice

### Performance Tips
- Use **Fastest** mode for simple tasks
- Enable **Fusion** only for complex questions
- Close unused apps for game automation
- Keep screen brightness moderate for vision

---

## Troubleshooting

### "Accessibility Service Not Enabled"
1. Settings → Accessibility → AIVA Accessibility Service
2. Toggle ON
3. Return to AIVA

### "Overlay Permission Denied"
1. Settings → Apps → AIVA → Display over other apps
2. Allow
3. Return to AIVA

### "API Key Invalid"
1. Check key format: `nvapi-xxxxx`
2. Verify key has model access at integrate.api.nvidia.com
3. Test key in Settings → API Keys

### Voice Not Working
1. Check microphone permission
2. Verify ASR enabled in Voice settings
3. Check internet connection (Riva is cloud-based)
4. Try different language

### Game Automation Not Working
1. Verify game profile package name matches
2. Re-calibrate controls for your screen resolution
3. Check game allows automation (some block it)
4. Try "Observe Only" mode first

### High Battery Usage
- Reduce automation poll rate in Settings
- Disable auto-speak
- Use PiP instead of full app when possible
- Limit game automation session length

---

## Privacy & Security

### Your Data Stays Local
- API keys encrypted with **Android Keystore** (AES-256-GCM)
- Never sent to AIVA servers
- Only sent to NVIDIA APIs you configure
- Screen content processed locally via Accessibility

### What AIVA Never Does
- ❌ Log your API keys
- ❌ Upload screenshots
- ❌ Access contacts/messages
- ❌ Share data with third parties
- ❌ Use your data for training
- ❌ Bypass app security

### Clearing Data
Settings → Privacy → Clear All Data
Removes: API keys, conversations, settings, game profiles

---

## Support

- **GitHub Issues**: https://github.com/aiva/aiva/issues
- **Documentation**: https://aiva.app/docs
- **Privacy Policy**: https://aiva.app/privacy
- **License**: Apache 2.0

---

## Advanced: Custom Game Profiles

### Calibration Process
1. Start game, go to main gameplay screen
2. AIVA Calibration → Tap each control:
   - Movement joystick area
   - Aim/look area
   - Fire button
   - Reload, jump, crouch, etc.
3. AIVA saves **normalized coordinates** (0-1)
4. Works across resolutions/aspect ratios

### Control Types
- **Joystick** - Continuous drag (movement, aim)
- **Tap** - Single press (fire, jump, reload)
- **Hold** - Long press (ADS, sprint)
- **Swipe** - Directional (grenade throw, melee)

### Color Detection
For HUD elements (health, ammo, minimap):
1. Select "Color Detection" in profile
2. Pick target color from screenshot
3. Set tolerance (default 30)
4. AIVA scans region each frame

---

*Last updated: 2026*
*Version: 1.0.0*