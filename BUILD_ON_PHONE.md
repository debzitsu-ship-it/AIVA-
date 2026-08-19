# 📱 Build AIVA APK on Phone (No Laptop Needed)

Since Java is blocked on this device, use **GitHub Actions** to build in the cloud.

---

## 🚀 Quick Steps

### 1. Create GitHub Repo
- Go to https://github.com/new
- Create repo named `AIVA` (public or private)
- **Don't** initialize with README/license/gitignore

### 2. Push Code from This Device
```bash
cd /workspace/aiva
git remote add origin https://github.com/YOUR_USERNAME/AIVA.git
git branch -M main
git push -u origin main
```
Replace `YOUR_USERNAME` with your GitHub username.

### 3. Trigger Build
- Go to your repo → **Actions** tab
- Click **"Build AIVA APK"** workflow
- Click **"Run workflow"** → **Run workflow** button
- Wait 5-10 minutes

### 4. Download APK
- In Actions, click the completed run
- Scroll to **Artifacts** → download `aiva-debug-apk.zip`
- Unzip → `aiva-ui-debug.apk`
- Tap to install on your phone

---

## 🔑 For Release Build (Optional)

Add these **Repository Secrets** (Settings → Secrets → Actions):
- `KEYSTORE_PATH` - path in repo (e.g., `keystore.jks`)
- `KEYSTORE_PASSWORD` - keystore password
- `KEY_ALIAS` - key alias
- `KEY_PASSWORD` - key password

Then upload your keystore to the repo (or keep it local and only build debug).

---

## 📋 What You Get

| Build Type | APK Name | Use For |
|------------|----------|---------|
| Debug | `aiva-ui-debug.apk` | Testing, development |
| Release | `aiva-ui-release.apk` | Distribution (needs signing) |

---

## ⚡ Alternative: One-Click Build Badge

Add to your README:
```markdown
[![Build AIVA](https://github.com/YOUR_USERNAME/AIVA/actions/workflows/build.yml/badge.svg)](https://github.com/YOUR_USERNAME/AIVA/actions/workflows/build.yml)
```

---

## 🐛 Troubleshooting

| Issue | Fix |
|-------|-----|
| Build fails | Check Actions log, usually missing SDK license acceptance |
| APK won't install | Enable "Install unknown apps" for your browser/file manager |
| Permissions denied | Grant all 7 permissions on first launch |

---

## 📦 What's in the APK

- Full conversational AI with 9 NVIDIA models
- Voice input/output (Riva ASR/TTS)
- Android automation (AccessibilityService)
- Floating PiP + Mini UI
- Game control engine (CoD/PUBG profiles)
- Settings (API keys, models, voice, automation, games, privacy)

---

**Need help?** Check the Actions logs or open an issue on GitHub.