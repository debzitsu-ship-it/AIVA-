# Workflow Fix - Manual Application Required

Due to GitHub App permissions, the fixed workflow file could not be pushed automatically via the `arena` sandbox. The GitHub App token lacks `workflows: write` permission, which is required to update files under `.github/workflows/`.

## What was fixed

The original `.github/workflows/build.yml` used deprecated `android-actions/setup-android@v3` inputs:

```yaml
- uses: android-actions/setup-android@v3
  with:
    api-level: 34
    ndk-version: r27c
    build-tools-version: 34.0.0
    cmake-version: 3.22.1
```

These inputs are no longer valid. Valid inputs are `cmdline-tools-version`, `accept-android-sdk-licenses`, `log-accepted-android-sdk-licenses`, `packages`. The job was emitting warnings:

```
Unexpected input(s) 'api-level', 'ndk-version', 'build-tools-version', 'cmake-version'
```

Additionally, `actions/setup-java@v4` and `actions/checkout@v4` are deprecated (Node 20). The fixed workflow upgrades to:

- `actions/checkout@v5`
- `actions/setup-java@v5`
- `android-actions/setup-android@v3` with `packages:`
- `gradle/actions/setup-gradle@v4` for caching (replaces manual `actions/cache`)
- `actions/upload-artifact@v5`
- Added `arena/**` to push triggers and `pull_request` trigger so CI runs on PRs
- Fixed SDK packages to include `ndk;27.0.12077973` etc.

## How to apply manually

On your local machine with a PAT that has `workflow` scope:

```bash
# From repo root on your local machine (not sandbox)
cp FIXED_WORKFLOW.yml .github/workflows/build.yml
# or
cp docs/FIXED_BUILD_WORKFLOW.yml .github/workflows/build.yml

git add .github/workflows/build.yml
git commit -m "ci: fix workflow - upgrade actions, fix setup-android inputs"
git push origin HEAD:master   # or create PR
```

Alternatively, use GitHub web UI:
1. Open `.github/workflows/build.yml` on GitHub
2. Click pencil icon (Edit)
3. Replace content with `FIXED_WORKFLOW.yml` / `docs/FIXED_BUILD_WORKFLOW.yml`
4. Commit directly to master or create PR

The fixed workflow is already present in this repo as:
- `./FIXED_WORKFLOW.yml` (root)
- `./docs/FIXED_BUILD_WORKFLOW.yml`

Both contain identical content ready to copy.

## Verification

Once applied, push to `master` or open PR targeting `master` - the three jobs (Build Debug APK, Lint, Unit Tests) should pass. The previous failures were:

- `Build Debug APK` - Hilt compilation error (VoiceViewModel @Singleton ViewModel) + missing compose.foundation
- `Lint` - same Hilt error + wrong SDK setup
- `Unit Tests` - same Hilt error

All code-level fixes have already been pushed to `arena/01a018eb-aiva` and are included in PR #2.

