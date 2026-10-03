# Test strategy and acceptance matrix — v1.2

Tests prove player outcomes and save preservation, not implementation trivia. Preparation was documentation-only; actual Android results belong to M0–M7. Every result identifies command, commit, fixture/seed and emulator/API when relevant.

## Commands

PowerShell from repo root, single emulator running for connected tests:

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat lintDebug
.\gradlew.bat lintRelease
.\gradlew.bat assembleDebug
.\gradlew.bat connectedDebugAndroidTest
.\gradlew.bat assembleRelease
```

For targeted tests use the actual existing class, e.g. `testDebugUnitTest --tests '*SaveMigrationsTest'`. On Linux/macOS use `./gradlew`. Discover custom simulation tasks with `tasks --all`; do not invent a registered task. Baseline simulations can be JUnit tests selected by class. Install minified release using the SDK's ADB with the correct serial if multiple emulators exist; inspect logs and perform release journeys after debug instrumentation, so the final smoke actually exercises the release artifact.

In this workspace the wrapper is not executable; set the prepared JDK/SDK and invoke it with `bash`:

~~~bash
export JAVA_HOME=/workspace/.java17-root/usr/lib/jvm/java-17-openjdk-amd64
export ANDROID_HOME=/workspace/.android-sdk
export ANDROID_SDK_ROOT=/workspace/.android-sdk
export ANDROID_USER_HOME=/workspace/.android-user
export GRADLE_USER_HOME=/workspace/.gradle
export PATH=$JAVA_HOME/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/build-tools/36.0.0:$PATH
bash ./gradlew testDebugUnitTest
bash ./gradlew lintDebug
bash ./gradlew lintRelease
bash ./gradlew connectedDebugAndroidTest
bash ./gradlew assembleDebug
bash ./gradlew assembleRelease
adb devices -l
~~~

## M7 audit record — 2026-10-03

The v1.2 redesign implementation is complete on the retained version branch and integrated into `main`. M7 validation includes 38 JVM XML suites, the API31 connected Compose suite, a minified APK install/update smoke, and a schema-1 old-save migration. The results below keep host limitations separate from checks that passed.

- Full JVM validation: `bash ./gradlew --no-daemon --console=plain testDebugUnitTest --rerun-tasks` passed 194 tests in 38 suites, 0 failures/errors/skips. The exact post-rate-fix result is retained at `artifacts/m7/final-offline-rate-validation.log`.
- API31 connected suite: `bash ./gradlew --no-daemon --console=plain connectedDebugAndroidTest` passed 26 tests, 0 failures/errors/skips on `emulator-5554` (API31, x86_64, software TCG). Results are in `artifacts/m7/api31-connected-final-offline-rate-fix.log` and `app/build/outputs/androidTest-results/connected/debug/TEST-emulator-5554 - 12-_app-.xml`.
- Build/lint validation: `bash ./gradlew --no-daemon --console=plain lintDebug lintRelease assembleDebug assembleRelease compileDebugAndroidTestKotlin` passed after the migration and offline-summary fixes. Current release archive hash and signer are recorded in `RELEASE_CHECKLIST.md`.
- Migration fix: the schema-3 serializer emits `pendingOfflineSummary: null`; migration now treats JSON null as absent. The offline report also computes a time-weighted hashrate across event-expiry segments and persists that value in the optional `averageEffectiveHashrate` field. Existing pending summaries without the field remain valid and render “Not recorded”; merged summaries retain total duration (bounded at 100 years) and a duration-weighted rate. The 12-hour earnings cap remains per offline session.
- API31 update profile: the locally rebuilt v1.0 release and v1.2 release share certificate SHA-256 `e8c7f74bc7c2e017e55880394510c4c2df728e6cb8e6cd45bb4835a89eb84932`. A serializer-generated schema-1 fixture was staged in the v1.0 app, the app was updated in place using `adb install -r`, and the resulting schema-3 save retained balances, miners, infrastructure, prestige points/nodes, onboarding and settings. No uninstall/data clear occurred during the profile. The v1.2 release was relaunched and the facility screen plus offline report were inspected.
- API36 limitation: the installed x86_64 image could not start with default acceleration because `/dev/kvm` is absent. Supported `-accel off -no-window` options started a TCG process, but its framework did not finish booting after more than four minutes at full CPU. No API36 app install/launch is claimed. Android Studio/Device Manager are absent; API31 was run through the native emulator/ADB CLI.
- External release limitation: GitHub release-history lookup returned `Forbidden`; no tags exist. Version code `2` is above the tracked v1.0 code `1`, but externally distributed higher codes or signing identities remain unknown. The local release APK is debug-key signed and is not a public release artifact.

### Main integration validation — 2026-10-03

The user authorized integrating `v1.2` into `main`; normal merge commit `221c548` has the same source tree as the connected-tested `v1.2` tree. The final validation command was run on merged `main`:

~~~bash
bash ./gradlew --no-daemon --console=plain testDebugUnitTest --rerun-tasks lintDebug lintRelease assembleDebug assembleRelease compileDebugAndroidTestKotlin
~~~

It passed in 3m18s: 38 JVM XML suites / 194 tests, 0 failures/errors/skips; `lintDebug`, `lintRelease`, debug/release assembly and Android-test Kotlin compilation all succeeded. Each lint task reported 12 warnings and 0 errors. The connected API31 suite had already passed 26/26 on the identical source/test tree. `v1.0` and `v1.2` remain separate branch refs. No tag or public release was created; GitHub release access was `Forbidden`, CLI authentication invalid, and the APK uses a debug signing key. See [INSTALLATION.md](INSTALLATION.md) for the current package availability and safe update instructions.

Exact Linux validation commands from the repository root:

~~~bash
export JAVA_HOME=/workspace/.java17-root/usr/lib/jvm/java-17-openjdk-amd64
export ANDROID_HOME=/workspace/.android-sdk
export ANDROID_SDK_ROOT=/workspace/.android-sdk
export ANDROID_USER_HOME=/workspace/.android-user
export ANDROID_AVD_HOME=/workspace/.android-user/avd
export PATH=$JAVA_HOME/bin:/workspace/.android-sdk/platform-tools:/workspace/.android-sdk/emulator:/workspace/.android-sdk/build-tools/36.0.0:$PATH
emulator -avd BMT_API31 -port 5554 -accel off -no-window -no-snapshot -no-audio -no-boot-anim
bash ./gradlew --no-daemon --console=plain testDebugUnitTest --rerun-tasks
bash ./gradlew --no-daemon --console=plain lintDebug lintRelease assembleDebug assembleRelease compileDebugAndroidTestKotlin
bash ./gradlew --no-daemon --console=plain connectedDebugAndroidTest
~~~

The in-place update profile used these Android commands after API31 had booted; it never called `adb uninstall`, `pm clear`, or `install -t`:

~~~bash
adb -s emulator-5554 install -r -d artifacts/m0/v1.0-debug.apk
adb -s emulator-5554 shell am start -n com.antigravity.bitcoinminingtycoon/.MainActivity
adb -s emulator-5554 shell am force-stop com.antigravity.bitcoinminingtycoon
cat artifacts/m7/api31-v1.0-save-before-update.json | adb -s emulator-5554 shell run-as com.antigravity.bitcoinminingtycoon tee /data/user/0/com.antigravity.bitcoinminingtycoon/files/datastore/game_save.json >/dev/null
adb -s emulator-5554 install -r artifacts/m0/v1.0-release.apk
adb -s emulator-5554 shell am start -n com.antigravity.bitcoinminingtycoon/.MainActivity
adb -s emulator-5554 install -r app/build/outputs/apk/release/app-release.apk
adb -s emulator-5554 shell am start -n com.antigravity.bitcoinminingtycoon/.MainActivity
adb -s emulator-5554 shell dumpsys package com.antigravity.bitcoinminingtycoon
adb -s emulator-5554 shell am force-stop com.antigravity.bitcoinminingtycoon
adb -s emulator-5554 shell am start -n com.antigravity.bitcoinminingtycoon/.MainActivity
~~~

### Nine documented journeys

Each file in `journeys/` is a manual Android journey. JVM tests and compiled Compose assertions are supporting evidence, not proof the manual journey ran.

| Journey | Offline evidence | API31/API36 result |
|---|---|---|
| 01 First session | Economy/ViewModel rules pass JVM tests. | API31 `FirstSessionLoopTest` passes mine→sell→first machine; migrated facility screenshot exists. API36, TalkBack and large-text checks remain. |
| 02 Market/sell | `MarketSellJourneyTest` and market engine tests pass. | API31 first-session sell and threshold-editor UI assertions pass; full guided market automation and API36 remain. |
| 03 Hardware/bulk | `HardwareBulkJourneyTest`, content and purchase JVM tests pass. | API31 UI checks reach all 20 tiers and explain cash/power/cooling remedies; rapid-buy pacing and API36 remain. |
| 04 Offline/daily/events | Clock rollback, fractional intervals, 12-hour per-interval cap, event expiry and duplicate-claim rules pass JVM tests. | API31 offline/daily/event sheets pass; force-stop/relaunch collection remains. Saved offline rate is time-weighted and legacy unknown rates are labeled unavailable. |
| 05 Prestige/tree | `PrestigeJourneyTest`, engine and ViewModel tests pass. | API31 prestige reset/preserved progression preview is visible; applying/canceling every path and API36 remain. |
| 06 Settings/accessibility | `SettingsAndStatsJourneyTest` passes. | API31 settings persistence, stats coverage and About package metadata assertions pass; font scale 1.5, TalkBack, insets/back and API36 remain. |
| 07 Process death | Repository/ViewModel adversarial tests pass for serialized purchases/claims and tick ordering. | Force-stop/relaunch recovery and duplicate-claim lifecycle remain unverified. |
| 08 v1.0 save upgrade | Schema-1 fixtures, migrations and recovery tests pass. | API31 local v1.0 release→v1.2 release `install -r` preserved schema-1 balance/hardware/settings/prestige data and migrated to schema 3; no process-death-after-update claim. |
| 09 App identity | `IdentityUpdateTest`, `aapt`, `apksigner`, merged manifest and resources pass inspection; authored icon layers were inspected locally. | API31 About reports installed metadata and release launches on facility home. Launcher masks, App Info, recents, splash transition and API36 remain. |

API31 facility/update screenshots and the connected-test report are under ignored `artifacts/m7/` and `app/build/outputs/androidTest-results/connected/debug/`. The 70-second force-stop/relaunch restored the saved offline report and its average rate; collecting it through the full process-death UI lifecycle and duplicate-claim handling remain outside the connected suite. The suite also does not prove TalkBack, 1.5 font scale, tactile vibration, launcher/App Info/recents or API36 behavior. Complete those checks on a hardware-accelerated API36 host or Android Studio Device Manager. Never uninstall or clear the dedicated update profile.

## Evidence layers

- JVM: pure economy/content/precision/clock/RNG/prestige/state invariants; meaningful simulations through real engine.
- Storage: old schema fixtures, preserving fields, idempotent migration, unsupported/corrupt recovery, durability and action/tick ordering.
- Compose: real action/state assertions, not just view existence. Test first sale/buy, bulk, unlocks, settings, rewards and modal cancellation.
- Visual/semantics: early/mid/late/migrated/huge-number states, compact/tall, 1.0/1.5 font and reduced motion. Inspect screenshots and hierarchy, not compile alone.
- Emulator: API31 minimum + API36 current target class, sequentially. Native navigation/insets/lifecycle/App Info/launcher/recents and APK update.
- Release: actual minified APK install/launch/restore/upgrade, manifest/resources/serialization/R8, icon/splash and critical journeys.

## Acceptance matrix

| Feature | Required proof |
|---|---|
| F01 teaching/discovery | clean/migrated Compose flow; persists cues; no blocked remedy |
| F02 tapping/feedback | deterministic output/critical RNG; settings/reduced motion; rapid-tap emulator stress |
| F03 passive | tick-rate equivalence; action/tick race; no animation-dependent output |
| F04 balances | decimal boundaries/dust/huge formatting; no negative legal state |
| F05 market/sell | sell10/50/MAX/zero; local price floor; auto-sell prereq/reset/settings |
| F06 hardware | 20 stable IDs; unlock/benefit; legacy owned output and UI |
| F07 bulk | exact geometric cost/count; huge MAX bounded; double-tap atomicity |
| F08 power | stage benefit/demand deficit/remedy; soft constraint not permanent stall |
| F09 cooling | thermal threshold edges; stage usefulness; huge load safety |
| F10 upgrades | 50–60 useful effects, every reference/prereq valid, legacy IDs and no duplicate grants |
| F11 achievements | one-time persisted unlock/reward and browsable accessible progress |
| F12 events | stacking/expiry; windfall collection once; offline expiry; release UI timers |
| F13 prestige | preview=apply, zero/cancel/duplicate/huge/legacy points and reset list |
| F14 Satoshi tree | dependency DAG, atomic points, saved ownership and faster rebuild |
| F15 offline | 0/rollback/59s/60s/11h59m/12h/>12h/extremes; repeat collection/process death |
| F16 daily | first-machine disclosure; 20h boundary/rollback/repeated claim/streak preserved; balance assist |
| F17 stats | source attribution, fractional time/energy, bounded histories; legacy coverage; huge values |
| F18 saves | v1→v2 fields, repeat migration, interruptions, raw recovery/future schema, signed update |
| F19 settings/reset | persists all options; cancel/confirm; usable post-reset, About actual build |
| F20 sound/haptics | fake/capability API checks; independent disabling/rate limits/resources; no tactile claim |
| F21 accessibility/native | compact/tall1.5 font, TalkBack actions, targets, insets, predictive back |
| F22 facility | all tier mappings/count caps/reset/discovery; screenshots every stage; paused animations |
| F23 identity | launcher/round/themed/App Info/recents/splash; no default robot/resources |
| F24 versions/package | branch preservation, app/code/schema separation, matching certificate update and APK record |
| F25 release/repo | full relevant suites, minified release flows, manifest/performance/no placeholders |

## Economy evidence

Use ECONOMY_BALANCE reference + alternate policies, publish seeds/config/purchase trace. Fixed-normal-price first run targets 25–35 minutes without rewards/luck, passive exceeds manual by minute5, second run≥25% faster. Actual market seeds report variance honestly. Run legacy and 12h idle cases; no dynamic sleep, UI-only 'balance review' or changed policy to camouflage regression.

## Adversarial cases

Exact price and one unit below; sell fractional dust/zero; MAX at huge magnitude; modifiers combined/event expiry mid-tick; upgrade during event; already/unaffordable node; background during buy/claim/prestige; kill immediately after confirmed transaction; repeated offline/daily modal; rapid navigation; overwritten future save; backward/extreme wall clocks; loading state economic taps; subsecond runtime; reduced motion plus critical/milestone; huge counts; unavailable audio/haptics; release serializer shrinking.

Use real process stop/relaunch without clearing data; distinguish background/task removal/force-stop. All tests that manipulate app data use a dedicated emulator profile. An actual preservation journey updates the package, never clears it.

## Performance evidence

Bound histories, visual objects and effects; avoid per-tap unbounded jobs and per-tick disk writes. Inspect emulator frame/allocation/CPU traces when rapid taps or scenes cause jank. Production continues correctly under slow UI. Emulator performance depends on host; do not turn it into an unsupported real-phone benchmark. Background app has no active mining/visual service.

## Reporting

Each milestone records feature IDs/command output/commit/fixture/emulator/screenshots and remaining checks. Reports/artifacts are ignored by Git; retain concise reviewed summaries. A passed JVM suite does not imply UI/device proof. Blocked checks remain blocked. Test failures are fixed at cause; valid assertions are not deleted or weakened to claim completion.
