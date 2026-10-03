# Bitcoin Mining Tycoon

A native Android idle game: mine fictional Bitcoin, sell it for Cash, buy machines, and grow from a salvaged PC into a sci-fi mining empire. The simulation runs locally and works offline; it does not connect to a wallet, exchange, mining pool, or game server.

## Project status

The v1.2 redesign is integrated into `main`. The `v1.2` line is retained for version history, and `v1.0` remains the baseline archive. The implementation passed 194 JVM tests and 26 API31 Compose tests. See [current state](docs/CURRENT_STATE.md) for the exact evidence and remaining device checks.

This checkout does not provide a verified production-signed APK for public installation. The locally built release APK is signed with a workspace debug key and must not be redistributed or used to replace an unrelated installed copy. GitHub release publishing could not be completed because release access is unavailable here and the production signing identity/version history remain unverified. Check the [GitHub Releases page](https://github.com/mmoptibuilds-commits/bitcoin-mining-tycoon/releases) for a future official package; verify its checksum and signing details before installing.

## Install for development

The app supports Android 12 or later (minimum API 31). The simplest development path is Android Studio with an API31-or-newer emulator; a physical USB phone is optional.

1. Install Git, Android Studio, JDK 17, Android SDK Platform 36 and Android Build Tools 36.0.0.
2. Clone this repository and open its root directory in Android Studio:

   ```bash
   git clone https://github.com/mmoptibuilds-commits/bitcoin-mining-tycoon.git
   cd bitcoin-mining-tycoon
   git switch main
   ```

3. Let Android Studio sync Gradle. Create and start an Android 12/API31 or newer emulator in Device Manager.
4. Build and install the debug app:

   ```bash
   bash ./gradlew assembleDebug
   adb devices -l
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

On Windows PowerShell, use `.\gradlew.bat assembleDebug` in place of `bash ./gradlew assembleDebug`.

In Android Studio, you can instead select the `app` run configuration and press **Run** after Gradle sync and starting an emulator.

See the full [installation and update guide](docs/INSTALLATION.md) and [Windows development setup](SETUP_WINDOWS.md).

## Verify a checkout

From the repository root, run:

```bash
bash ./gradlew testDebugUnitTest
bash ./gradlew lintDebug lintRelease
bash ./gradlew assembleDebug assembleRelease
```

To run Compose instrumentation, start one emulator first, then run `bash ./gradlew connectedDebugAndroidTest`. Full commands and recorded results are in [test strategy](docs/TEST_STRATEGY.md).

## Product and engineering docs

| Document | Contents |
|---|---|
| [PRD](docs/PRD.md), [Features](docs/FEATURES.md) | Requirements and F01–F25 acceptance contract |
| [Game design](docs/GAME_DESIGN.md), [Economy](docs/ECONOMY_BALANCE.md) | Gameplay rules and pacing targets |
| [Design system](docs/DESIGN_SYSTEM.md), [UX](docs/UX_SPEC.md), [App identity](docs/APP_IDENTITY.md) | Screens, interaction and Android packaging |
| [Architecture](docs/ARCHITECTURE.md), [Save compatibility](docs/SAVE_COMPATIBILITY.md) | Implementation boundaries and save migration rules |
| [Installation](docs/INSTALLATION.md), [Windows setup](SETUP_WINDOWS.md) | Build, install and update instructions |
| [Current state](docs/CURRENT_STATE.md), [Test strategy](docs/TEST_STRATEGY.md), [Release checklist](docs/RELEASE_CHECKLIST.md) | Evidence, limitations and release gates |
| [Version control](docs/VERSION_CONTROL.md), [Changelog](CHANGELOG.md) | Branch policy and version history |

Historical screenshots are in `docs/screenshots/v1/`; they show the old implementation, not the v1.2 visuals.
