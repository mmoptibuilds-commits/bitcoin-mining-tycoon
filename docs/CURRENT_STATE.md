# Current state and evidence ledger

Updated 2026-09-30. Active branch v1.2. Baseline v1.0 commit `33787bc4fa61ea4f78de06b7b81dd4094371cf39`.

## Status

| Milestone | Implemented | Verification |
|---|---|---|
| Repo preparation | docs/guidance/version branches/cleanup delivered | recorded preparation checks below; no Android build claimed |
| M0 workstation/baseline | pending | run on user's Windows/Android Studio environment |
| M1 save compatibility/correctness | pending | no migrations or runtime changes in preparation |
| M2 economy/content | pending | no new balance simulation results yet |
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
