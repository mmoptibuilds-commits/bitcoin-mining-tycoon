# Installation and updates

## Public APK availability

As of 2026-10-03, this repository does not identify a verified production-signed APK for public installation. The APK built in this workspace uses a local debug certificate. GitHub release access returned `Forbidden`, the configured GitHub CLI credential is invalid, and the production signing key plus external version-code history have not been verified. Do not redistribute that APK or use it to replace another signed installation.

When an official package is published, use the [GitHub Releases page](https://github.com/mmoptibuilds-commits/bitcoin-mining-tycoon/releases), check the APK checksum and signing certificate listed with that release, and install it only if its publisher/signing identity is trusted.

## Device requirements

- Android 12 or later (minimum API 31).
- The game is offline and has no account, wallet, exchange, mining pool or game-server connection.
- A physical USB phone is optional; Android Studio Device Manager with an API31-or-newer emulator is supported for development and verification.

## Install an official APK

After an officially signed APK becomes available:

1. Download the APK from the project's Releases page and compare its SHA-256 with the release notes.
2. Open the APK on the Android device and follow Android's package-installer prompt. If Android asks, allow the selected browser/file manager to install this package.
3. For an existing install, install over the app only when the APK has the same signing certificate and a higher version code. If Android reports a signature/version mismatch, stop and contact the publisher; do not uninstall or clear data to force the update.

## Build and install a debug APK

### Requirements

- Git
- Android Studio with JDK 17
- Android SDK Platform 36 and Build Tools 36.0.0
- An API31-or-newer emulator or Android device with developer options and ADB enabled

Clone the default integration branch:

```bash
git clone https://github.com/mmoptibuilds-commits/bitcoin-mining-tycoon.git
cd bitcoin-mining-tycoon
git switch main
```

In Android Studio, open this repository root and let Gradle sync. Or build from a shell:

```bash
bash ./gradlew assembleDebug
adb devices -l
adb install app/build/outputs/apk/debug/app-debug.apk
```

For Windows PowerShell, use:

```powershell
.\gradlew.bat assembleDebug
adb devices -l
adb install app\build\outputs\apk\debug\app-debug.apk
```

Invoke the wrapper through `bash` on Linux/macOS checkouts where its executable bit is unset.

The debug build is for local development. Its signing key is workspace-specific, so debug APKs from different machines may not update each other. The repository's current release build also uses the local debug signing configuration and is not a public release artifact.

## Save and update safety

Game progress is stored in app-private local DataStore. It is not synchronized to an account or cloud service. Uninstalling the app or clearing its storage removes that local progress. For an update, use the same app package and signing certificate with a higher version code; Android's in-place install keeps app data. Never uninstall or clear data to work around an update rejection.

See [save compatibility](SAVE_COMPATIBILITY.md) for the migration contract and [release checklist](RELEASE_CHECKLIST.md) for verified package identity and remaining release gates.
