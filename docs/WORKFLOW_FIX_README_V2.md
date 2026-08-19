# Workflow Fix V2 — Remove Setup Android SDK (it was failing)

**Latest run 32253624281 ("Update build.yml") failed at `Setup Android SDK` in 11s for all 3 jobs.**

The step `android-actions/setup-android@v3` with `packages:` tries to run `sdkmanager --install` for `ndk;27.0.12077973` and `cmake;3.22.1` etc. On `ubuntu-latest` the runner **already has** Android SDK 34, build-tools 34.0.0, platform-tools preinstalled. The install fails (likely license or network) and the whole job is cancelled before `Setup Gradle` / `Build` runs.

**Fix:** Remove the `Setup Android SDK` step entirely and rely on the preinstalled SDK. `gradle/actions/setup-gradle@v4` + `actions/setup-java@v5` (with `cache: gradle`) is enough. This is the standard approach for `ubuntu-latest` (see GitHub docs: `ubuntu-latest` already includes Android SDK).

## New fixed workflow (V2)

See `FIXED_WORKFLOW.yml` / `docs/FIXED_BUILD_WORKFLOW.yml` — same as V1 but **without** the `Setup Android SDK` step.

If you need NDK/CMake for a specific build, add it back later with:

```yaml
- name: Setup Android SDK (optional)
  uses: android-actions/setup-android@v3
  continue-on-error: true
  with:
    packages: |
      ndk;27.0.12077973
      cmake;3.22.1
```

But for now, remove it.

## How to apply V2

Via Web UI (same as before, no fork needed):

1. Open https://github.com/debzitsu-ship-it/AIVA-/blob/master/.github/workflows/build.yml
2. Click pencil ✏️ Edit → `Ctrl+A` → Delete
3. Copy **entire** `FIXED_WORKFLOW_V2.yml` (or `FIXED_WORKFLOW.yml` after this update) → Paste
4. Commit directly to `master` (message: `ci: fix workflow v2 - remove failing setup-android`)

You can also copy from `docs/FIXED_BUILD_WORKFLOW.yml` in PR #2.

After pushing, Actions → `Build AIVA APK` should go green in ~2-3 min (all 3 jobs use preinstalled SDK + gradle cache).

