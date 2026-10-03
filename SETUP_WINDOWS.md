# Windows development setup — main / v1.2

Use your Windows PC, Android Studio and emulators. No phone/USB debugging step is required. Do not create a new Android project or copy source into a different package. The v1.2 redesign is complete and integrated into `main`; `v1.0` and `v1.2` are retained as version-history branches. For end-user and debug-APK steps, see [Installation](docs/INSTALLATION.md).

## 1. Get the version branch

For the current integrated code, update the existing checkout or clone the repository and use `main`:

```powershell
git fetch origin
git status --short
git switch main
git pull --ff-only origin main
```

For a new checkout, use GitHub Desktop or authenticated Git, then select `main`. The PowerShell equivalent is:

```powershell
git clone https://github.com/mmoptibuilds-commits/bitcoin-mining-tycoon.git
cd bitcoin-mining-tycoon
git switch main
```

To inspect the retained version line instead, use `git switch v1.2`; `v1.0` is the baseline archive. Preserve local changes; never reset them to make checkout easier.

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

Open this checkout in your chosen Codex app/CLI/IDE. The initial v1.2 P1/P2 plan and execution prompts in PROMPTS are archived records of completed work; do not rerun them as if the redesign were pending. For new work, read WORKFLOW and CURRENT_STATE and use a task-specific plan and acceptance criteria. The repo does not change your model or subscription limits.

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

Codex can complete verification and fixes without a second harness. If using Antigravity for a second pass, wait until Codex commits/pushes and stops writing, fetch the authorized target branch (`main` for the integrated tree or `v1.2` for that version line), record the SHA and open the same project in Android Studio. Do not launch two writers in the same checkout. Preserve package/signing/storage identity during installation.

## 8. Install/update safety

The local baseline and v1.2 candidate use a workspace debug certificate; no production-signed public APK is verified or published by this checkout. Different machines may generate different debug keys. To update an old install without losing data, a candidate must have the same signing certificate and a higher versionCode. Never commit a private key. If Android refuses a signature-mismatched update, retain the old install and follow SAVE_COMPATIBILITY; uninstall is not a preservation solution.

Official refs: [Android CLI](https://developer.android.com/tools/agents/android-cli), [emulator](https://developer.android.com/studio/run/emulator), [ADB](https://developer.android.com/tools/adb).
