# Windows setup — existing v1.2 project

Use your existing Windows PC, Android Studio and emulators. No phone/USB debugging step is required. Do not create a new Android project or copy source into a different package.

## 1. Get the version branch

In the existing checkout:

```powershell
git fetch origin
git status --short
git switch v1.2
git pull --ff-only origin v1.2
```

If v1.2 is only remote, use `git switch --track origin/v1.2`. For a new checkout, clone the repository with GitHub Desktop or authenticated Git and select v1.2. Preserve local changes; never reset them to make checkout easier. Historical v1.0/main are preserved.

## 2. Open Android Studio

Open the repository root containing settings.gradle.kts. Let Gradle sync. Use JDK17 as required by the current project. Install SDK platform36 and needed build tools through SDK Manager. Keep the current stable project versions unless a concrete build error justifies a documented change. `local.properties` is local and ignored; Android Studio can generate its SDK path. Signing material is local too.

## 3. Create emulator test surfaces

Use Android Studio Device Manager, not a mandatory Android CLI command. Enable supported hardware virtualization through the normal Windows/firmware setup if emulator acceleration requires it. Create an API31 Android12 phone and API36 phone for the current target. Run one at a time on the older i7/20GB machine; begin with 2 CPU cores and ~2GB emulator RAM, then adjust from observed responsiveness. Use compact/tall sizes sequentially and change font scale for accessibility tests. If targetSDK changes later, add that target's emulator.

Google's Android CLI overview currently lists Windows restrictions for `android emulator` and PowerShell download. Device Manager + the SDK emulator/ADB tools are the primary reliable path. CLI support may change; check current docs and installed help rather than treating old restrictions as permanent.

## 4. Validate baseline commands

From the repository root in PowerShell:

```powershell
.\gradlew.bat --version
.\gradlew.bat testDebugUnitTest
.\gradlew.bat lintDebug
.\gradlew.bat assembleDebug
# Start a single emulator in Device Manager first.
adb devices
.\gradlew.bat connectedDebugAndroidTest
```

Use Android Studio's terminal SDK tools or add the installed SDK platform-tools to PATH if `adb` is unavailable. Do not copy someone else's absolute SDK path. Use the wrapper; no separate system Gradle is needed. Check actual task availability with `.\gradlew.bat tasks --all` if tasks differ.

## 5. Codex

Open this checkout in your chosen Codex app/CLI/IDE. Select GPT-6 Luna and Max reasoning in the model controls when available on your account. This repo does not change your model or subscription limits. Read WORKFLOW, then copy prompt P1 from PROMPTS using `/plan`. Review/approve the generated implementation plan; activate P2 `/goal` afterward. If your installed surface lacks `/goal`, use its normal-prompt equivalent given in PROMPTS and require the same ledger/acceptance contract.

## 6. Optional Android CLI helpers

Get the current installer from [official Android tools](https://developer.android.com/tools/agents/android-cli). Do not depend on an unverified winget ID. After install, inspect:

```powershell
android --version
android info
android init -h
android skills -h
android studio -h
```

Use installed help to configure Codex/Antigravity skills; `android init` installs CLI guidance. Discover current testing, edge-to-edge, Navigation3, R8 and profiling skill IDs rather than guessing flags. Android Studio analysis/Compose-preview helpers are optional; use Studio/Gradle/emulator alternatives if unavailable. Do not replace native tests with browser screenshots.

## 7. Optional Antigravity handoff

Codex can complete implementation, verification and fixes without a second harness. If using Antigravity for a second pass, wait until Codex commits/pushes and stops writing, fetch v1.2, record the SHA and open the same project in Android Studio. Use P5 to plan/reproduce emulator verification, then P6 to fix verified findings after ownership transfers. Codex may retain ownership and fix the findings instead. Do not launch two writers in the same checkout. Preserve package/signing/storage identity during installation.

## 8. Install/update safety

Baseline release uses the debug certificate. To update an old install without losing data, candidate must have the same signing certificate and a higher versionCode. Reuse the existing local key, never commit it. Different machines may generate different debug keys. If Android refuses a signature-mismatched update, retain the old install and follow SAVE_COMPATIBILITY; uninstall is not a preservation solution.

Official refs: [Android CLI](https://developer.android.com/tools/agents/android-cli), [emulator](https://developer.android.com/studio/run/emulator), [ADB](https://developer.android.com/tools/adb).
