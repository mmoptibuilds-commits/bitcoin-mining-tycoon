# Current state and evidence ledger

Updated 2026-09-30. Active branch v1.2. Baseline v1.0 commit `33787bc4fa61ea4f78de06b7b81dd4094371cf39`.

## Status

| Milestone | Implemented | Verification |
|---|---|---|
| Repo preparation | docs/guidance/version branches/cleanup delivered | recorded preparation checks below; no Android build claimed |
| M0 workstation/baseline | baseline JVM/lint/debug checks and serializer fixtures recorded | partial; release baseline hit Maven Central HTTP 429, and API31/API36 emulator evidence is blocked by missing Studio, images and virtualization |
| M1 save compatibility/correctness | partial; schema-2, migration/recovery and deterministic runtime contracts implemented on v1.2 | JVM/build/lint pass; Android instrumentation is compiled but cannot run because the workspace has no device and Maven Central returned HTTP 429 for UTP dependencies |
| M2 economy/content | software implemented on v1.2 | JVM content/economy/policy suite, lint, debug packaging and instrumentation-source compilation pass; device screenshots and connected flows remain blocked by the current emulator/UTP environment |
| M3 home/teaching/navigation | pending | existing v1 UI remains |
| M4 facility scene/feedback | pending | new scenes/haptics not implemented |
| M5 upgrades/secondary screens/stats | pending | expanded content/stats not implemented |
| M6 Android identity | pending | template launcher artwork remains |
| M7 full release/audit | pending | no candidate v1.2 APK has been built/tested here |

## Preparation evidence — 2026-09-30

This evidence covers repository preparation only; gameplay, migration, icon and APK work remain pending.

| Check | Performed evidence |
|---|---|
| Baseline/version refs | GitHub branch reads confirmed main and v1.0 both at `33787bc4fa61ea4f78de06b7b81dd4094371cf39`; v1.0 was created from the existing main tip |
| Cleanup commit | `e5dcedf6a5bbf48206e859219f397b11348dba3d` pushed normally to v1.2; tracked build/cache output and workstation junk removed; 22 historical PNGs moved with unchanged Git blob hashes |
| Candidate structure | `python3 preparation/validate.py`: 0 errors; 48 prepared text files, 200 removed paths including the 22 moved screenshot paths, 22 moves, 220 projected blobs, 0 runtime changes |
| Whitespace/scope | `git diff --no-index --check preparation/originals preparation/candidate`: no whitespace diagnostics; exit 1 represents the expected differences |
| Remote objects | GitHub recursive tree reads and Git blob comparisons confirmed the prepared text uploads, unchanged app source/build configuration/wrapper blobs, and unchanged historical screenshot blobs |
| Review | Read-only GPT-6 Luna Max document/scope review identified save-field coverage, optional-harness completion and evidence gaps; those contracts were corrected before the documentation commit |

The preparation scripts and detailed validation output are session scratch checks, not Android tests or shipped app tooling. The final documentation commit and its complete changed-file history are recorded in v1.2 Git history; this ledger does not embed its own commit hash. Recheck refs and scope before starting implementation.

## M0 execution evidence — 2026-09-30

M0 is **partial, not complete**. JVM, lint and debug packaging establish the source baseline; release packaging and every device-dependent observation remain unverified until their environment blockers are resolved.

| Check | Performed evidence |
|---|---|
| Approved plan / refs | Plan commit `1bcabb4ea6cf924cd658d92313b9e8c2d0aed2d9` pushed normally to `v1.2`; `origin/main` remains `f911b9fb403a986265f2755647ef07a969c3f4c1`, `origin/v1.0` remains `33787bc4fa61ea4f78de06b7b81dd4094371cf39`, and `origin/v1.2` is `1bcabb4ea6cf924cd658d92313b9e8c2d0aed2d9` at this check. Existing local `work` ref at the main merge commit is preserved. |
| Toolchain | JDK `17.0.20.1`; Gradle wrapper `9.1.0`; Android Gradle Plugin `9.0.1`; SDK platform 36 and build-tools `36.0.0`; app ID/namespace `com.antigravity.bitcoinminingtycoon`, minSdk 31, target/compile SDK 36. SDK processing emitted the known XML-v4-versus-v3 parser warning. |
| Baseline tests/lint | `bash ./gradlew --no-daemon --console=plain testDebugUnitTest --rerun-tasks` passed: 24 XML suites, 94 tests, 0 failures/errors/skips. `bash ./gradlew --no-daemon --console=plain lintDebug --rerun-tasks` passed. |
| Baseline debug APK | `bash ./gradlew --no-daemon --console=plain assembleDebug --rerun-tasks` passed. SHA-256: `ea7eddeed31520647dcd3eccd79f4746e4aef41a2d6eb7c16cee570b3b82b077`. `aapt dump badging` reports version `1.0`/code `1`, minSdk `31`, target/compile `36`. `aapt dump permissions` shows only the generated dynamic-receiver permission; no INTERNET permission. `apksigner` certificate SHA-256: `e8c7f74bc7c2e017e55880394510c4c2df728e6cb8e6cd45bb4835a89eb84932`. |
| Schema-1 fixtures | Archived `origin/v1.0` at `33787bc4fa61ea4f78de06b7b81dd4094371cf39`; a temporary test in that source snapshot called the actual v1.0 `GameSaveSerializer`. `BMT_FIXTURE_OUT_DIR=/workspace/bitcoin-mining-tycoon/app/src/test/resources/saves/v1 bash ./gradlew --no-daemon --console=plain testDebugUnitTest --tests '*LegacyFixtureWriterTest'` passed: 1 test, 0 failures/errors/skips. It generated ten anonymized JSON fixtures: early, mid, late, prestiged, daily-claimed, settings-disabled, large-value, missing-optional, unknown-content and expired-event. The temporary writer was not added to v1.2 source sets. |
| Old APK/signing comparison | Archived v1.0 `assembleDebug --rerun-tasks` passed. `artifacts/m0/v1.0-debug.apk` reports the same package, version `1.0`/code `1`, and certificate SHA-256 as the v1.2 baseline debug APK; its APK hash is also `ea7eddeed31520647dcd3eccd79f4746e4aef41a2d6eb7c16cee570b3b82b077`. This is useful same-key debug baseline evidence, not the planned minified v1.0 release APK. |
| Release baseline | Current-source `assembleRelease --rerun-tasks` and archived v1.0 `assembleRelease` both failed before packaging because Maven Central returned HTTP 429 for uncached `org.jetbrains.kotlin:compose-group-mapping:2.3.20` (“Your ip has exceeded rate limits”). The exact artifact is not in the Gradle cache. Direct GETs to Maven Central and repo1 both returned 429; do not change pinned versions or claim a release APK. |
| Emulator / Studio | Correctly configured `adb devices -l` starts the daemon and lists no devices. Android CLI `1.0.16457483` has no existing AVD. Supported `emulator create` selects a device profile but exposes no API selector; SDK catalog queries find no API31/API36 system images (including the broad `system-images` query). No emulator binary, Android Studio executable or `/dev/kvm` exists; `android-cli ... studio check` reports no running Studio and exits 1. No screenshot, Compose instrumentation, TalkBack, process-death UI or launcher evidence is claimed. Physical USB is not required; Device Manager/image/virtualization availability is the present blocker. |

Verbose Gradle logs and permission output are under ignored `artifacts/m0/`; the v1.0 debug APK is retained there as a same-key baseline. The emulator and release-build blockers are independent of M1 implementation and remain open; M0 must not be marked verified until its required baseline release/device evidence is produced or explicitly recorded as blocked.

## M1 execution evidence — 2026-09-30

M1 is **software-implemented but not complete**. Source, fixture, JVM, debug package, lint and Android-test compilation evidence are available. No connected Compose result or v1.0 in-place emulator update is claimed; the environment currently has no attached device and the UTP test runner's uncached dependencies are rate-limited.

Implementation commit `0c9c14c` (`feat: harden save migration and game state transactions`) was pushed normally to `v1.2`.

| Check | Performed evidence |
|---|---|
| Scope / refs | Work remains on `v1.2`; local `main`/`work`, `origin/main`, `origin/v1.0` and `origin/v1.2` were inspected before this change. `git diff --check` passed. No historical ref was changed. |
| Schema and fixture migration | Added schema-2 statistics, fractional playtime, bounded energy history, discovery state and pending offline summaries. The migration decoder is field-aware, rejects malformed/future schemas, checkpoints recoverable damaged data before field repair and preserves unknown content IDs. `SaveMigrationsTest` runs all ten actual v1.0 serializer fixtures. A DataStore integration test proves valid schema-1 `mid.json` is committed as schema 2 before repository readiness; a transient in-memory marker forces that commit even when the migrated value is otherwise equal. |
| Recovery contract | `SaveRecoveryCheckpoint` keeps a bounded raw copy and consent token in app-private storage. Corrupt input stays locked until explicit confirmation; DataStore applies consent on the next launch, then repository initialization durably writes a schema-2 fresh save before acknowledging the token. Future-schema and oversized saves remain read-only. `SaveReadinessScreenTest` covers confirmation/restart messaging and unsupported schema, and compiles as Android instrumentation. |
| Runtime correctness | Added serialized latest-state mutations with durable critical actions; startup atomically credits bounded offline production and advances the save boundary; events split at expiry; subsecond playtime accumulates; RNG continuation and production/reward source totals persist. Prestige uses exact `BigInteger` Newton square root compatible with minSdk 31 and saturating point counters. Fixed stable upgrade ID `gpu_6x_custom_os` to target `gpu_rig_6x`. |
| JVM tests | `bash ./gradlew --no-daemon --console=plain testDebugUnitTest` passed: 25 XML suites, 126 tests, 0 failures, 0 errors, 0 skipped. Targeted migration/repository/ViewModel/offline/event/market/prestige/game/adversarial/number/content suite also passed. Full output is in ignored `artifacts/m1/testDebugUnitTest.log`. |
| Android source/package checks | `bash ./gradlew --no-daemon --console=plain lintDebug assembleDebug compileDebugAndroidTestKotlin` passed. Lint reports 0 errors and 16 existing warnings. The Compose recovery test source compiled; `app/build/outputs/apk/debug/app-debug.apk` was produced. Full output is in ignored `artifacts/m1/lint-assemble-androidtest-compile.log`. |
| Connected / signed update | `adb devices -l` listed no devices. `bash ./gradlew --no-daemon --console=plain connectedDebugAndroidTest` failed before device selection because Maven Central returned HTTP 429 for uncached Unified Test Platform dependencies (`proto-google-common-protos`, `auto-service`, `dagger`, protobuf, Kotlin reflect and coroutines). No screenshot, live DataStore relaunch, process-death test or signed in-place upgrade is claimed. |
| Remaining M1 gate | Obtain Android Studio Device Manager or a supported API31/API36 emulator plus resolved UTP dependencies; run `connectedDebugAndroidTest`, save-upgrade Journey 08, and record screenshots/process-death evidence. No physical USB phone is required by the plan. |

The JVM tests establish logic and serializer behavior only. The Android Compose test was compiled, not executed. Do not change M1 to complete until its emulator acceptance is observed or the blocker is resolved and evidence is recorded.

## M2 execution evidence — 2026-09-30

M2 is **software implemented but not fully verified**. All numeric tuning and content changes are centralized and the production-engine policy checks pass. The milestone's required emulator screenshots and Journey 02/03 remain blocked by the documented environment; do not mark M2 complete until those observations are run.

| Check | Performed evidence |
|---|---|
| Scope / identity | Changes remain scoped to existing content, engines, model/save defaults, tests and `v1.2`. Application ID, namespace, module count, storage identity and signing configuration were not changed. Stable miner, upgrade, event, achievement and prestige IDs are retained; the known GPU 6x upgrade target points to `gpu_rig_6x`. |
| Central balance | Added `BalanceConfig` as the tested source for manual/default money and market state, all 20 miner numeric records, all 10 power and 7 cooling stages, 56 upgrade numeric/effect records, trends, reward caps, event timings, thermal rules, prestige multipliers, offline bounds and reference targets. SHA-256 balance identity: `e16b83ec18600218997543f848d811e6b3b52c77a4e3e08bec909e4466e0532a`. Schema remains 2; balance rules version is 2; recognized legacy 10-hash defaults normalize to 50,000 while custom fixture values remain intact. |
| Content and effect integrity | Retains 20 miners, 10 power stages, 7 cooling stages, all retained stable content IDs, all 12 prestige nodes, 40 achievements and 10 events. Ordinary upgrades now total 56: Tapping 9, Compute 22, Power 8, Cooling 8, Automation 9. Content tests verify IDs/counts/targets, prerequisites, market distributions and effect-handler coverage. Auto-sell unlock, offline buffer, event response and persisted-RNG critical tap have focused engine tests; critical tap was introduced after the no-critical-tap reference passed. |
| Fixed reference policy | Test-only simulator drives `GameEngine`, `MarketEngine`, `FleetEngine`, `UpgradeEngine`, `PowerEngine`, `ThermalEngine` and `PrestigeEngine`. Seed 1337, fixed $50,000 market, no random events/windfalls/daily reward/critical tap: first sale and machine 45s; GPU 360s; infrastructure 420s; ASIC 1,155s; first prestige 1,645s (27m25s). At minute 5, passive BTC `0.003487500000` exceeds manual BTC `0.000525000000`. Full purchase log and timing trace: ignored `artifacts/m2/reference-policy.txt`. |
| Faster follow-up / alternate policies | With Efficient Silicon, same policy reaches the corresponding next prestige in 485s (29.5% of the first duration). Five random market/event/windfall seeds 7, 42, 1337, 2026 and 8675309 reach first prestige in 870–1,405s; median 1,115s, variance range 535s. Additional deterministic runs record low-tap 1,735s, frequent seller 1,620s, day-one reward 1,630s, market-holder threshold $52,000 in 2,065s, immediate power remedy at 5s, +300s delayed prestige, two consecutive prestige cycles in 2,130s, and migrated early/mid/late fixture starts. Report: ignored `artifacts/m2/alternate-policies.txt`; seeded variance report: ignored `artifacts/m2/seeded-market-event-policy.txt`. |
| Economy edge cases | Added saturating miner purchase counters and bounded geometric MAX correction with a finite cost sentinel. Full tests cover fractional production, event-expiry segmentation, 12-hour offline cap, offline buffer cap, claim persistence, legacy asset/unlock continuity, bulk totals/affordability, extreme counters, prestige point accounting, one-time special effects and deterministic RNG. |
| JVM verification | `bash ./gradlew --no-daemon --console=plain testDebugUnitTest` passed: 139 tests, 0 failures/errors/skips. Final output: ignored `artifacts/m2/m2-full-final-3.log`. |
| Android source/package verification | `bash ./gradlew --no-daemon --console=plain lintDebug assembleDebug compileDebugAndroidTestKotlin` passed after final M2 changes. The SDK XML v4 versus parser-v3 warning persists. This compiles Android tests but does not execute or visually inspect them. Output: ignored `artifacts/m2/m2-android-final.log`. |
| Device acceptance | `adb devices -l` has no device, Android Studio/Device Manager and API31/API36 images are unavailable in this workspace, and prior `connectedDebugAndroidTest` was blocked resolving uncached UTP dependencies due Maven Central HTTP 429. Consequently M2 screenshots, Journey 02/03 live transaction/remedy checks, TalkBack/font-scale review and physical display acceptance are not claimed. No physical USB phone is required; Android Studio Device Manager plus Gradle/ADB is the planned route when available. |

The policy simulation is model evidence, not usability evidence. It uses the current production engine transitions but cannot prove that novice teaching, prices, power remedies or pacing are understandable on device. M2 implementation commit `8ba6df585caeee42d8ed815b67dcedd49fdc0df6` was pushed normally to `v1.2`; this ledger update records the measured evidence in a separate documentation commit.

## Existing implementation observed in source

Kotlin/Compose single app module, decimal helpers, immutable GameState, pure engines, ViewModel/StateFlow, DataStore and schema-1 serializer/migration entry point. 20 miner definitions, 32 ordinary upgrades, 40 achievements, 10 events, 12 prestige nodes, 10 power stages and 7 cooling stages are the prior inspected content baseline. JVM engine/repository/number/ViewModel tests and JVM 'journey' tests exist. `androidTest` currently has MineScreenTest; do not equate JVM journeys with real emulator UI tests.

Manifest has launcher/round icon references, correct app label and backup disabled. Icon foreground/background are Android Studio's default robot/green grid. Theme is a basic light platform parent; branded system splash needs implementation and visual inspection.

Observed build: application ID `com.antigravity.bitcoinminingtycoon`; minSdk 31/compileSdk 36/targetSdk 36; versionName 1.0/versionCode 1; R8/resource shrinking enabled, release signed using debug config. Old docs claiming API 37 and a newly scaffolded build are superseded.

## Confirmed source mismatches; reproduce/fix during implementation

| ID | Evidence | Required proof |
|---|---|---|
| C01 | Upgrade `gpu_6x_custom_os` targets nonexistent `gpu_6x_rig`; miner ID is `gpu_rig_6x` | integrity test plus effective output before/after purchase |
| C02 | Manual defaults: GameState 50000, GameSave 10, prestige reset 10; ViewModel factory reset constructs GameState | launch/load/reset/prestige fixture tests; centralized baseline |
| C03 | No dedicated production haptics implementation found, but settings promises haptics | actual API wiring/fake/capability tests; tactile feel unverified |
| C04 | GameEngine adds `deltaSeconds.toLong()` each tick to playtime | subsecond tick reproduction then fractional accumulation |
| C05 | SaveMigrations catches exceptions and returns fresh GameSave; future versions decode as current | valid legacy fixtures; raw-data recovery and unsupported-schema handling |
| C06 | OfflineEngine uses one effective rate across interval and returns zero earnings below its 60s report threshold; comment claims expiry/idempotence that helper alone does not enforce | short-absence production, event-expiry/repeated-claim/ViewModel tests before assigning severity |
| C07 | Day 1 gives $100 while first CPU costs $10 | no-reward/claim simulations and revised onboarding/reward curve |
| C08 | Default Android launcher resources and unbranded launch theme | F23 surface screenshots and resource/manifest checks |

These are source findings, not claims that runtime regressions have been reproduced or fixed. Audit RNG persistence, action/tick save ordering, high-magnitude prestige conversion, rewards vs lifetime-point accounting, event modifier wiring and legacy unlocks during M1/M2.

## Tooling and constraints

Approved: Windows + Android Studio emulators, no physical phone USB debugging. Primary validation is Gradle/ADB/Device Manager. Android CLI helpers depend on installed support; official overview currently lists Windows emulator-command restrictions. Physical vibration feel, phone launcher variations and real-phone performance stay unverified. User-selected Codex model is GPT-6 Luna Max; set it in the harness.

## Updating this ledger

For each milestone add: commit, changed feature IDs, actual commands/results, emulator/API/config, simulation seeds/timings, screenshot/report paths, defects fixed, remaining blockers and next action. Do not replace pending with verified based on code inspection. Keep verbose reports under ignored artifacts; retain concise reviewed evidence here. Record the exact commit reviewed in P7. If an optional Antigravity pass is used, identify its reviewed commit and any later fix commits separately.
