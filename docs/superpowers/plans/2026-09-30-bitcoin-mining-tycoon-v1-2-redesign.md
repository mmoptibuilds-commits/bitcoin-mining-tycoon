# Bitcoin Mining Tycoon v1.2 Redesign Implementation Plan

> **For agentic workers:** After approval, execute M0 through M7 sequentially on the existing v1.2 branch. Use the project feature-cycle workflow at every gameplay milestone. One production writer; no parallel implementation. Preserve main, v1.0, v1.2 and every historical ref.

**Goal:** Deliver the complete native v1.2 redesign for F01–F25 and a minified, emulator-verified sideload APK while preserving valid v1.0 saves, install identity and version history.

**Architecture:** Keep the current one-module Kotlin/Compose app, immutable GameState, deterministic engines, GameNumber/BigDecimal arithmetic, ViewModel/StateFlow, coroutines and DataStore. Add schema-2 migration/recovery, a single serialized mutation boundary, centralized balance/content definitions, derived discovery/facility presentation, bounded Canvas scenes and platform haptics at the existing boundaries.

**Tech Stack:** Kotlin, Jetpack Compose/Material 3, existing Navigation 3 dependencies, StateFlow/coroutines, kotlinx serialization/DataStore, JDK 17, Gradle 9.1.0, AGP 9.0.1, compile/target SDK 36, minSdk 31.

**Spec:** docs/PRD.md, docs/FEATURES.md, docs/PROJECT_PLAN.md, docs/GAME_DESIGN.md, docs/ECONOMY_BALANCE.md, docs/DESIGN_SYSTEM.md, docs/UX_SPEC.md, docs/APP_IDENTITY.md, docs/ARCHITECTURE.md, docs/SAVE_COMPATIBILITY.md, docs/TEST_STRATEGY.md, docs/RELEASE_CHECKLIST.md, docs/VERSION_CONTROL.md and journeys/01–09.

**Path convention:** Kotlin production paths below are relative to `app/src/main/java/com/antigravity/bitcoinminingtycoon/`; JVM test paths are relative to `app/src/test/java/com/antigravity/bitcoinminingtycoon/`; instrumented test paths are relative to `app/src/androidTest/java/com/antigravity/bitcoinminingtycoon/`. Any full path is written explicitly. Existing IDs and serialization keys are stable unless a migration adds an optional field.

## Global Constraints

- Preserve applicationId and namespace com.antigravity.bitcoinminingtycoon, DataStore file filesDir/datastore/game_save.json, signing continuity, stable content IDs and all valid schema-1 assets.
- Keep one Android app module, local/offline-only play, no internet permission, backend, ads, analytics, real crypto, web wrapper, generated artwork or background mining service.
- Keep minSdk 31, compileSdk/targetSdk 36 and pinned dependencies unless a reproduced platform requirement justifies a documented focused change.
- Use BigDecimal/GameNumber for economic magnitudes; keep MAX, histories, scene objects, animation effects and recovery files bounded.
- Foreground time comes from monotonic elapsed time; offline time is wall-clock defensive and capped at 43,200 seconds. Persist RNG and verify deterministic reload.
- Critical purchases, sales, rewards, prestige and settings commit durably before success feedback; tick and actions share one serialized mutation path.
- Approval was granted before implementation and this plan was pushed as a docs-only commit to `v1.2` before M0. Keep any future plan revision reviewable and scoped separately from source changes.
- At the end of each passed milestone, create one scoped commit and push normally to `origin/v1.2`. Never bundle a later milestone, push another branch, rewrite history or claim unavailable emulator/signing evidence.
- Each milestone's JVM/connected Gradle commands are separate invocations; stop on the first nonzero result, fix the cause and rerun the affected checks before proceeding.
- New stats are tracked from v1.2 and labelled honestly on old saves. Never invent historical source attribution or playtime.
- First prestige must take 25–35 minutes under the ECONOMY_BALANCE reference policy; passive contribution exceeds manual by minute 5; the comparable next run is at least 25% faster.
- Use Android Studio Device Manager, Gradle and ADB; a physical phone is not required. If an emulator check is unavailable, report it blocked and do not mark the milestone verified.

## Review Focus

1. An early, late or prestiged v1.0 save retains every valid asset, claim, setting and onboarding fact after migration and in-place update. Tests: M1 fixture field-by-field migration and M6 signed update journey.
2. A tap, purchase, claim, prestige, tick and flush arriving together cannot lose production, overspend or award twice. Tests: M1 repository/ViewModel serialization and process-death cases.
3. Fractional ticks, wall-clock rollback, short offline gaps and event expiry conserve time and production. Tests: M1 foreground/offline boundary matrix.
4. Low-tap play, ordinary market seeds and legacy high balances retain a reachable next step; luck and rewards are not needed for the reference target. Tests: M2 fixed and seeded actual-engine simulations.
5. Compact, tall, 1.5-font, TalkBack, reduced-motion and migrated layouts remain usable, and launcher/update identity remains native. Tests: M3–M6 Compose and emulator journeys.

---

## Baseline Findings and Decisions

- Planning started from v1.2 commit 38a4736949995b44814c4c79f17cc96df0dfa6c7. The checkout is on local v1.2 tracking origin/v1.2 with no tracked source changes; this uncommitted plan document is the only untracked repository file. Remote main is f911b9fb403a986265f2755647ef07a969c3f4c1; v1.0 is 33787bc4fa61ea4f78de06b7b81dd4094371cf39. A local work ref at main's merge commit is left intact. I added only `origin/v1.0` and `origin/v1.2` to this checkout's local fetch refspec to inspect the required refs; no remote ref changed. v1.2 has no app-source diff from v1.0; the approved redesign is still unimplemented.
- The existing save path is filesDir/datastore/game_save.json. GameSave schema is 1. App ID/namespace are correct; v1.0 is versionCode 1/versionName 1.0, release is minified/resource-shrunk and currently debug-signed.
- Current implementation mismatches to reproduce/fix: gpu_6x_custom_os points to gpu_6x_rig instead of gpu_rig_6x; GameState defaults to 50,000 manual strength while GameSave/prestige use 10; onboarding adds $15; GameEngine truncates every deltaSeconds tick to Long playtime; SaveMigrations silently defaults corrupt data and decodes future schemas as current; offline earnings suppress positive gaps under 60 seconds and apply one snapshot rate across event expiry; Day 1 gives $100 although the first CPU costs $10; app icon remains Android robot/green grid; Settings promises haptics without a haptics implementation.
- Navigation 3 libraries are pinned, but live AppNavHost uses local remember state and BackHandler; no NavDisplay/NavKey API is currently used. M3 wires the existing Navigation 3 dependency in place and does not replace or upgrade the library.
- Planned default reconciliation: centralize the recognized legacy manual baselines 10 and 50,000 at 50,000, matching current GameState and the reference loop; preserve custom values and separate purchased/permanent multipliers. Start with zero cash and remove the onboarding cash grant so the first machine follows the mine/sell/cash loop. M2 may tune centralized values only against the locked simulation targets. Day 1 cash is capped at 30% of first miner cost.
- Upgrade target: 56 ordinary upgrades, retaining the current 32 IDs and adding 24 tested effects. Planned distribution: Tapping 9, Compute 22, Infrastructure 8 Power + 8 Cooling, Automation 9. Existing category storage remains compatible; UI maps TAP→Tapping, HARDWARE→Compute, POWER/COOLING→Infrastructure subgroups, MARKET→Automation.
- Current UI tests contain one instrumented MineScreenTest. JVM classes named Journey are JVM logic journeys, not emulator E2E. No screenshot-testing plugin, Robolectric or DI framework is configured; use current JUnit4 fakes, Compose androidTest and Android Studio/ADB captures without adding Hilt, a screenshot plugin or another module.
- The original planning pass changed documentation only and left production/test source and runtime configuration untouched. After approval, M0–M6 implementation proceeded as scoped commits on `v1.2`; M7 is recording release evidence and remaining device blockers.

## Shared Component and State Contracts

- GameRepository: add `enum class MutationDurability { COALESCED, IMMEDIATE }`, `sealed interface MutationResult { data class Applied(val state: GameState) : MutationResult; data class Blocked(val readiness: SaveReadiness) : MutationResult; data class PersistenceFailed(val cause: Throwable) : MutationResult }`, and `suspend fun mutateLatest(durability: MutationDurability, reduce: (GameState) -> GameState): MutationResult`. Both modes hold one mutex from latest-state read through reduce/apply. IMMEDIATE persists the prospective state before publishing that action; on failure it retains the prior visible state and returns `PersistenceFailed`. COALESCED publishes and marks state dirty; a bounded writer later takes the same mutex and persists the latest state, never a captured stale snapshot. A failed immediate write emits no success feedback. Tick, tap, purchase, sale, claims, prestige, settings, offline credit and flush all use this boundary. Remove caller-computed stale GameState writes from GameViewModel.
- Save loading: expose `sealed interface SaveReadiness { data object Loading : SaveReadiness; data object Ready : SaveReadiness; data class CorruptCheckpointed(val reason: String) : SaveReadiness; data class CorruptUncheckpointed(val reason: String) : SaveReadiness; data class UnsupportedSchema(val schemaVersion: Int) : SaveReadiness }` from repository into GameUiState. Economic controls are disabled until Ready; unsupported future schema stays read-only. Corrupt data can start a new save only when checkpointed and after explicit confirmation; checkpoint failure or an oversized save stays blocked without a destructive reset action.
- Migration: replace the ambiguous default-returning migration contract with `sealed interface SaveMigrationResult { data class Ready(val save: GameSave, val migratedFromVersion: Int?) : SaveMigrationResult; data class RecoveryRequired(val reason: String, val schemaVersion: Int?, val checkpointed: Boolean) : SaveMigrationResult }`. `SaveRecoveryCheckpoint` is injected into the `GameSaveSerializer` created by `BitcoinMiningTycoonApp`, copies at most 4 MiB of original bytes atomically to `filesDir/save_recovery/corrupt-save.json`, and never logs payload contents. `GameSaveSerializer.readFrom` reads at most 4 MiB + 1 byte; oversize input is left untouched and surfaced as CorruptUncheckpointed. `DataStoreSaveDataSource.saveFlow` maps the recovery exception to readiness instead of emitting a fresh default. Keep the current `filesDir/datastore/game_save.json` serializer path and install no corruption handler that silently replaces the original.
- Offline: persist PendingOfflineSummary(durationSeconds, creditedBtc, creditedAtWallMillis) with offline credit and advanced lastSaveWallMillis in the same DataStore commit. Dismissing/reopening the sheet only changes the pending summary; it never reapplies BTC.
- GameUiState: retain gameState and existing formatted values, and add readiness, DiscoveryPresentation, SaleQuote, FacilitySceneModel and an ephemeral SharedFlow<GameplayFeedbackEvent> with a 24-event DROP_OLDEST cap. No economy, migration, clock or unlock formula moves into Compose.
- DiscoveryPresentation: a pure state-derived object containing available system sections and one deterministic NextGoal (label, reason, progress and destination action). Inputs are GameState and persisted completedTeachingCueIds; do not store balances or duplicate unlock truth.
- FacilitySceneModel: pure stage, capped density, owned milestone features, next-stage silhouette and discovered-stage marker. Current scene derives from current owned miners; highestDiscoveredFacilityStage persists across prestige.
- UpgradeDefinition: retain stable id/category/cost/multiplier/target/prerequisite fields. Add a typed optional special-effect list only for behavior not expressed by existing multipliers (critical tap, auto-sell unlock, offline-rate bonus). Every special effect gets an engine and content-integrity assertion.
- Statistics: preserve `lifetimeBtcMined`, `lifetimeUsdEarned`, `peakHashrate`, `totalManualTaps`, `totalMinersPurchased`, `totalUpgradesPurchased`, `totalBtcSold`, `highestPriceObserved`, `lowestPriceObserved`, `totalPrestiges`, `lifetimeSatoshiPointsEarned`, `totalPlaytimeSeconds` and `totalEventsTriggered`. Add decimal-string `manualBtc`, `foregroundPassiveBtc`, `offlineBtc`, `dailyRewardBtc`, `windfallBtc` and `achievementRewardBtc`; add `playtimeFractionalSeconds` as a decimal string constrained to `[0,1)`, finite `peakTemperatureC`, `prestigePointsBaselineV2`, `prestigePointsEarnedSinceV2`, `dailyPointsEarnedSinceV2`, and `powerEnergyHistory: List<PowerEnergySample(elapsedSeconds: Long, demandKw: String, capacityKw: String, energyKwh: String)>`, capped at 288 entries sampled every 300 foreground seconds. Derive achievement count from the preserved achievement IDs. Current balances remain the existing GameState fields. Legacy source totals initialize to zero and are labelled “tracked since v1.2”.
- Haptics: add HapticEffect (TAP, PURCHASE, INVALID, MILESTONE, PRESTIGE) and an injectable Haptics interface. ViewModel checks SettingsState.hapticsEnabled; Android implementation uses a capability-aware, rate-limited platform vibrator API; tests inject a fake.

## M0 — Branch, Environment and v1.0 Baseline (F18/F21/F23–F25)

**Files:** no production files. Create schema-1 test resources at `app/src/test/resources/saves/v1/` and an M0 evidence ledger at ignored `artifacts/m0/`; update `docs/CURRENT_STATE.md` only with performed evidence. Use a temporary `git archive` source snapshot of v1.0 for the baseline APK and old-serializer fixtures; do not create another Git worktree or change any historical branch.

- [x] Recheck v1.2 cleanliness/history, local/remote refs, remote URL and worktree. The normal checkout is on `v1.2`; `main`/`v1.0` and historical refs remain preserved and `v1.2` is the sole write target.
- [x] Confirm wrapper/JDK/SDK, registered Gradle tasks and supported Android CLI forms. The CLI exposes device profiles but no API selector; no API31/API36 emulator is available. Device Manager/SDK Manager remains the documented route; no unsupported flag is invented.
- [x] Rerun and record baseline JVM/lint/debug checks. At M0 the unchanged release build hit Maven Central HTTP 429, recorded in its log; M6 later produced the minified current candidate and v1.0 snapshot successfully after dependencies became available. The historical M0 baseline failure remains explicit.
- [x] Generate old serializer fixtures in a temporary archived v1.0 source snapshot; rebuild `origin/v1.0` as ignored `artifacts/m0/v1.0-release.apk` with the local debug key and record its public certificate fingerprint. The temporary fixture writer was removed from the snapshot; only serializer-produced fixtures remain in v1.2 tests.
- [ ] On a supported Android Studio Device Manager host, create API31 and API36 portrait phone AVDs, run one at a time, and seed a dedicated preservation profile following `journeys/08-save-upgrade.md`. Capture baseline Mine/Hardware/Settings screens and the v1.0 asset/settings/claim snapshot. Do not uninstall or clear data during the upgrade profile. Blocked in this workspace because Studio, system images and emulator support are absent.
- [x] Record exact local commands/results, APK/certificate facts, source ref and emulator blocker in CURRENT_STATE / ignored artifacts; M0 evidence is in v1.2 history.

**Commands:** see the JDK/SDK exports below; run each command separately and record its output. Old-source build/fixture sequence:

~~~bash
mkdir -p artifacts/m0 app/src/test/resources/saves/v1
legacy_source_dir="$(mktemp -d /tmp/bitcoin-mining-tycoon-v1.0.XXXXXX)"
git archive origin/v1.0 | tar -x -C "$legacy_source_dir"
(cd "$legacy_source_dir" && bash ./gradlew assembleRelease)
cp "$legacy_source_dir"/app/build/outputs/apk/release/app-release.apk artifacts/m0/v1.0-release.apk
apksigner verify --print-certs artifacts/m0/v1.0-release.apk
fixture_dir="$(pwd)/app/src/test/resources/saves/v1"
(cd "$legacy_source_dir" && BMT_FIXTURE_OUT_DIR="$fixture_dir" bash ./gradlew testDebugUnitTest --tests '*LegacyFixtureWriterTest')
rm "$legacy_source_dir"/app/src/test/java/com/antigravity/bitcoinminingtycoon/data/LegacyFixtureWriterTest.kt
rm -rf "$legacy_source_dir"
~~~

The temporary fixture-writer test must be authored at that exact test path in the archive snapshot before invoking the command; it writes only through `BMT_FIXTURE_OUT_DIR`. Run current-tree Gradle commands under the exact environment below.

**Observable acceptance:** branch refs remain intact; baseline source/build identity is known; v1.0 fixtures originate from the old serializer; baseline first-launch, market, hardware, settings and identity captures exist; emulator-dependent checks are either evidenced or explicitly blocked.

### Preliminary onboarding validation (2026-09-30)

- `bash ./gradlew --no-daemon --console=plain :app:testDebugUnitTest --rerun-tasks`: success; XML reports 24 suites, 94 tests, 0 failures, 0 errors and 0 skipped.
- `bash ./gradlew --no-daemon --console=plain :app:lintDebug --rerun-tasks`: success.
- `bash ./gradlew --no-daemon --console=plain :app:assembleDebug --rerun-tasks`: success; debug APK SHA-256 `ea7eddeed31520647dcd3eccd79f4746e4aef41a2d6eb7c16cee570b3b82b077`.
- `aapt dump badging app/build/outputs/apk/debug/app-debug.apk`: package `com.antigravity.bitcoinminingtycoon`, versionName `1.0`, versionCode `1`, min SDK `31`, target/compile SDK `36`; no INTERNET permission appears in the merged APK manifest.
- Gradle emits an SDK metadata warning because the installed SDK XML is v4 while this processing tool reports support through v3. Build, lint and JVM tests still complete successfully. Recheck after Android Studio/tooling is available; do not upgrade pinned project dependencies to silence the warning.
- `adb devices -l` lists no device. No emulator screenshot, instrumentation result or app launch is claimed.

## M1 — Save Safety, Time and Serialized Transactions (F03/F05/F07/F12–F20/F24)

**Modify:** `model/GameState.kt`; `data/GameSave.kt`, `SaveDataSource.kt`, `GameRepository.kt`, `migrations/SaveMigrations.kt`; `app/BitcoinMiningTycoonApp.kt`; `viewmodel/GameViewModel.kt`; `engine/GameEngine.kt`, `EconomyEngine.kt`, `AchievementEngine.kt`, `OfflineEngine.kt`, `EventEngine.kt`, `MarketEngine.kt`, `FleetEngine.kt`, `UpgradeEngine.kt`, `PowerEngine.kt`, `ThermalEngine.kt`, `PrestigeEngine.kt`; `content/DailyRewards.kt`; `platform/ClockProvider.kt`; `util/GameNumber.kt`.

**Create:** `data/SaveRecoveryCheckpoint.kt`; `data/SaveMigrationResult.kt`; schema-1 fixtures under `app/src/test/resources/saves/v1/`; `content/ContentIntegrityTest.kt`; `app/src/androidTest/java/com/antigravity/bitcoinminingtycoon/data/SaveReadinessRecoveryTest.kt`. Extend existing `data/SaveMigrationsTest.kt`, `data/GameRepositoryTest.kt`, `viewmodel/GameViewModelTest.kt`, engine `GameEngineTest.kt`, `OfflineDailyTest.kt`, `EventAchievementTest.kt`, `MarketEngineTest.kt`, `PrestigeEngineTest.kt` and `adversarial/AdversarialBugHuntTest.kt`.

- [x] Write field-preservation tests against serializer-produced early/mid/late/prestiged/daily/settings/large/missing/unknown/expired v1.0 fixtures; assert preserved values, durable schema-2 migration, defaults and idempotence.
- [x] Implement schema-1→2 migration and checkpointing in `SaveDataSource`, `SaveMigrations`, `SaveRecoveryCheckpoint` and `BitcoinMiningTycoonApp`. Individually validate values, preserve known/unknown IDs safely, retain bounded raw payloads before recovery, block writes on checkpoint failure and keep future schemas read-only.
- [x] Add schema-2 defaults/counters including balance rules, discovery, battery-friendly mode, attribution coverage, fractional playtime, bounded energy history, thermal peak, offline summary and prestige bookkeeping. Existing lifetime totals/settings/claims/IDs are covered by fixture tests.
- [x] Serialize latest-state ticks/actions, durable commits and flush through the repository mutex; ViewModel reducers use latest state and success feedback follows commit success.
- [x] Persist fractional elapsed time and RNG continuation; clamp and validate timers/counts and numeric bounds.
- [x] Credit every positive offline interval, split at event expiry, cap at 43,200 seconds, and atomically store BTC/source stats/summary/timestamp. The sheet only acknowledges the already-credited summary.
- [x] Use exact decimal/integer-sqrt prestige accounting, prevent duplicate award replay on legacy saves, and keep spendable/daily/cumulative point counters distinct through reset.
- [x] Add JVM regressions for interleavings, concurrent MAX buys, fractional time/RNG reload, event expiry, repeated claims, clock extremes, short/long offline, huge prestige, recovery/future schema and legacy unlocks. Process-kill behavior remains OS-level below.
- [x] Run targeted migration/repository/ViewModel/offline/event/prestige/engine/adversarial/content JUnit suites; 99 focused M7 cases passed. Compose recovery test sources compile.
- [ ] Run Journey 07/08 force-stop and signed `adb install -r` update on API31/API36; capture migrated early/late/recovery screens and verify the v1 save after process death. No emulator/device is available.

**Tests/commands:** `bash ./gradlew testDebugUnitTest --tests '*SaveMigrationsTest' --tests '*GameRepositoryTest' --tests '*GameViewModelTest' --tests '*OfflineDailyTest' --tests '*EventAchievementTest' --tests '*PrestigeEngineTest' --tests '*GameEngineTest' --tests '*AdversarialBugHuntTest' --tests '*ContentIntegrityTest'`; `bash ./gradlew connectedDebugAndroidTest`.

**Observable acceptance:** all schema-1 fields survive fixtures, unknown/future/corrupt data cannot silently overwrite originals, repeated claims/tick races do not duplicate or lose state, and emulator relaunch/process death retains the credited state. Missing emulator proof stays blocked.

## M2 — Balance, Content and Reference Pacing (F02–F16/F22)

**Create:** `content/BalanceConfig.kt`; `engine/ReferencePolicySimulator.kt`; `engine/ReferencePacingSimulationTest.kt`. `content/ContentIntegrityTest.kt` is created in M1.

**Modify:** `content/Miners.kt`, `Upgrades.kt`, `Infrastructure.kt`, `DailyRewards.kt`, `Events.kt`, `PrestigeNodes.kt`; `engine/EconomyEngine.kt`, `GameEngine.kt`, `MarketEngine.kt`, `FleetEngine.kt`, `UpgradeEngine.kt`, `PowerEngine.kt`, `ThermalEngine.kt`, `EventEngine.kt`, `AchievementEngine.kt`, `OfflineEngine.kt`, `PrestigeEngine.kt`; `util/GameNumber.kt`; the corresponding existing engine test files listed in the test command below.

- [x] Centralize opening cash `0`, manual baseline `50,000`, market price `$50,000`, first miner cost `$10`, progression/effect/reward/prestige/offline constants in tested `BalanceConfig`. Fresh/load/reset/prestige defaults align; recognized legacy 10-H/s defaults normalize while custom values remain. No onboarding cash grant; $3 day-one assist is deferred until first machine ownership.
- [x] Preserve all 20 miner IDs, ten power/seven cooling stages and owned content; tune costs/rates/loads without hard deficit traps. Correct `gpu_6x_custom_os` target to `gpu_rig_6x` without changing its saved ID.
- [x] Retain 32 upgrade IDs and add 24 effect-tested stable definitions: Tapping 9, Compute 22, Power 8, Cooling 8, Automation 9 (56 total). Optional critical tap was added only after no-crit reference passed; auto-sell/offline/event automation have engine-backed effects.
- [x] Preserve existing achievement/event/prestige/reward IDs and definitions. `ContentIntegrityTest` checks catalogue counts/targets/handlers; one-time rewards, stacking/expiry, 7-day cycle, 20-hour cooldown and point separation are covered. The bounded day-one assist and later stage-aware rewards remain in scope.
- [x] Build `ReferencePolicySimulator` from production-engine transitions and fixed injected clock/RNG; record tie breaks, config hash, milestones and purchase trace.
- [x] Simulation asserts first sale/machine, GPU/infrastructure/ASIC and first prestige at 1,645s (27m25s); minute-five passive BTC exceeds manual; Efficient Silicon next comparable run is 485s (8m05s). Five seeded market runs plus low-tap/reward/holder/infra/delayed/multiple-prestige/legacy/offline policies and variance reports are recorded in ignored `artifacts/m2/`.
- [x] Verify geometric bulk totals/affordability/extreme MAX bounds, effect-once, modifier application, finite positive values and power/cooling soft constraints in the full engine/content suite.
- [x] Run engine/content/simulation tests; `ReferencePacingSimulationTest`, `ContentIntegrityTest`, `EconomyEngineTest`, `GameEngineTest`, `MarketEngineTest`, `BulkPurchaseTest`, `UpgradeEngineTest`, `PowerThermalTest`, `OfflineDailyTest` and `PrestigeEngineTest` passed in the full/focused suites.
- [ ] Capture sale/machine/GPU/ASIC/upgrade/infrastructure views and execute Journeys 02/03 on API31/API36. No emulator/device is available.

**Tests/commands:** bash ./gradlew testDebugUnitTest --tests '*ReferencePacingSimulationTest' --tests '*ContentIntegrityTest' --tests '*EconomyEngineTest' --tests '*GameEngineTest' --tests '*MarketEngineTest' --tests '*BulkPurchaseTest' --tests '*UpgradeEngineTest' --tests '*PowerThermalTest' --tests '*OfflineDailyTest' --tests '*PrestigeEngineTest'; bash ./gradlew connectedDebugAndroidTest.

**Observable acceptance:** deterministic report shows every locked target with seed/config/purchase trace; five live RNG seeds document variance; all content integrity checks pass; emulator sale/purchase and power/cooling remedy flows match those same engine values.

## M3 — Facility Home, Progressive Teaching and Navigation (F01/F04/F05/F19/F21)

**Modify:** ui/navigation/AppNavHost.kt, NavigationKeys.kt; ui/screens/mine/MineScreen.kt, MarketCard.kt, EnvironmentalPanel.kt, onboarding/OnboardingDialog.kt; ui/theme/AppColors.kt, AppTypography.kt, Theme.kt; viewmodel/GameViewModel.kt and GameUiState.

**Create:** `ui/presentation/DiscoveryPresentation.kt`; `ui/components/TeachingCue.kt`; `app/src/test/java/com/antigravity/bitcoinminingtycoon/ui/presentation/DiscoveryPresentationTest.kt`; `app/src/androidTest/java/com/antigravity/bitcoinminingtycoon/ui/FirstSessionLoopTest.kt`; `app/src/androidTest/java/com/antigravity/bitcoinminingtycoon/ui/NavigationAccessibilityTest.kt`.

- [x] Replace the equal Mine/Hardware/Upgrades/Stats dashboard with Mine home, reachable Hardware/Upgrades and utility Stats/Settings/About. Navigation 3 owns saveable routes/back behavior and retained list/bulk state; no second navigation library.
- [x] Make the evolving facility home anchor Bitcoin/Cash/Mine/Mining speed/sell action and one next goal, with graphite/copper styling and concise offline-simulation copy; remove the v1 console dashboard hierarchy.
- [x] Implement GameState-backed persisted discovery for Mine→Bitcoin→Sell→Cash→first machine→passive income; migrated assets reveal relevant systems and retain onboarding. No reward grant or slide deck blocks the first machine.
- [x] Show deterministic Sell10/50/MAX previews using the same market price/rounding as sales; expand market details progressively and route power/cooling deficits to remedies.
- [x] Add core Compose semantics, explicit locked/error reasons, 48dp navigation/header targets, safe insets/resize and deterministic back paths. Full font-scale/TalkBack layout acceptance remains device-level below.
- [x] Add clean/migrated Compose first-session, navigation and discovery assertions. Test sources compile and JVM discovery/sale/ViewModel suites pass.
- [ ] Capture clean/migrated screens and execute Journeys 01/06 with font scale 1.5, TalkBack, compact/tall layout and back/insets checks on API31/API36. No emulator/device is available.

**Tests/commands:** bash ./gradlew testDebugUnitTest --tests '*DiscoveryPresentationTest' --tests '*GameViewModelTest'; bash ./gradlew connectedDebugAndroidTest.

**Observable acceptance:** on clean profile a novice completes Mine→Bitcoin→Sell→Cash→first machine without tutorial slides or reward subsidy; migrated profile retains settings/onboarding and immediately exposes relevant systems; screenshots and semantic tree show facility-first hierarchy.

## M4 — Evolving Facility Scene, Feedback and Haptics (F02/F20/F21/F22)

**Modify:** `ui/screens/mine/MineScreen.kt`; `ui/components/CoreMineButton.kt`; `ui/navigation/AppNavHost.kt`; `platform/SoundPlayer.kt`; `data/migrations/SaveMigrations.kt`; `app/BitcoinMiningTycoonApp.kt`; `MainActivity.kt`; `viewmodel/GameViewModel.kt`; `app/src/main/AndroidManifest.xml` only for the required normal vibration permission.

**Create:** `content/FacilityStageCatalog.kt`; `ui/facility/FacilityStage.kt`, `FacilityPresentation.kt`, `FacilityScene.kt`; `ui/components/MiningFeedbackLayer.kt`, `FeedbackMotionPolicy.kt`; `viewmodel/GameplayFeedback.kt`; `platform/Haptics.kt`; JVM `FacilityPresentationTest.kt`, `FeedbackMotionPolicyTest.kt`, `HapticsTest.kt`, `AudioTrackSoundPlayerTest.kt`, `GameplayFeedbackBusTest.kt`; update `GameViewModelTest.kt`; Android `ui/facility/FacilitySceneTest.kt` and `ui/CoreMineButtonFeedbackTest.kt`.

- [x] Map every saved miner ID to these ten visible stages: salvaged PC = `ancient_cpu`, `gaming_cpu`; GPU bench = `gaming_gpu`; rig workshop = `dual_gpu_rig`, `gpu_rig_6x`; ASIC room = `entry_asic`, `industrial_asic`; warehouse = `asic_rack`, `server_room`, `mining_warehouse`; energy campus = `mining_farm`, `hydro_facility`, `geothermal_complex`, `nuclear_campus`; fusion megafarm = `immersion_megafarm`, `fusion_complex`; orbital array = `orbital_solar_miner`; lunar/quantum base = `lunar_mining_array`, `quantum_hash_facility`; Dyson swarm = `dyson_hash_swarm`. Highest current owned tier selects scene; counts add capped density/detail; discovered milestone persists across prestige while current scene resets. Preview only the next stage silhouette.
- [x] Draw original deterministic Canvas geometry for desktop, GPU bench, linked rigs, ASICs, racks, campus, fusion, orbital, lunar/quantum and Dyson stages. Tests map every stable miner ID and bound scene presentation at 48 logical elements. Capped animation queues at 24.
- [x] Keep gameplay output in GameEngine. Show readable tap deltas, 100ms press compression and bounded particles; reduced-motion disables tap particles/press scale/fades/fan motion; battery-friendly mode suppresses optional particles/fan motion; fan loops stop when the screen lifecycle leaves RESUMED.
- [x] Add Android haptics with capability fallback and per-signal rate limits for tap, purchase, invalid action, milestone and prestige. Persisted haptics setting gates each path independently of sound. Keep existing procedural sound and guarantee AudioTrack release on success/failure; no background music.
- [x] Add deterministic scene mapping/count/prestige tests, feedback-bus bounds, pure presentation-motion policy tests, haptic fake/capability/setting/rate-limit tests, and sound disable/release tests. `testDebugUnitTest`, `compileDebugAndroidTestKotlin`, `lint` and `assembleDebug` pass locally: 161 JVM tests, zero failures/errors/skips, and 11 lint warnings (none from the new M4 Compose APIs). These local assertions do not equal emulator rendering.
- [ ] Render all ten fixture stages in Compose previews/instrumented states; capture early, industrial and sci-fi scenes on emulator. Stress rapid taps/navigation and inspect reduced-motion and background loop behavior; record frame/allocation traces only if jank appears.
- [ ] Run Journey 01 and 03 with the evolving scene; separately exercise reset/prestige scene change and persistent discovery. Emulator verifies API wiring, not physical vibration quality.

**Tests/commands:** `bash ./gradlew --no-daemon --console=plain testDebugUnitTest --tests '*FacilityPresentationTest' --tests '*HapticsTest' --tests '*GameEngineTest' --tests '*GameViewModelTest' --tests '*SoundPlayerTest'`; `bash ./gradlew --no-daemon --console=plain compileDebugAndroidTestKotlin lint assembleDebug`; `bash ./gradlew --no-daemon --console=plain connectedDebugAndroidTest`.

**Observable acceptance:** all tier fixtures select an accurate scene/feature; counts and effects stay bounded; scene and tap visuals respond without influencing mining; independent haptics/audio/reduced-motion settings work on emulator.

## M5 — Hardware, Upgrades, Systems, Statistics and Settings (F05–F19/F21)

**Modify:** ui/screens/hardware/HardwareScreen.kt and HardwareCard.kt; upgrades/UpgradesScreen.kt; stats/StatsScreen.kt; settings/SettingsScreen.kt; prestige/PrestigeSheet.kt and SatoshiTreeScreen.kt; mine/OfflineReturnSheet.kt and DailyRewardSheet.kt; components/EventBanner.kt and AchievementBanner.kt; model/GameState.kt, data/GameSave.kt, ViewModel and engines only where M1/M2 contracts require UI presentation.

**Create:** `ui/screens/upgrades/UpgradeTrack.kt` and `UpgradeGroup.kt`; `ui/screens/settings/AboutScreen.kt`; `ui/screens/stats/StatsBreakdown.kt`; `app/src/androidTest/java/com/antigravity/bitcoinminingtycoon/ui/HardwareFlowTest.kt`, `UpgradeGroupsTest.kt`, `StatsCoverageTest.kt`, `RewardsEventsPrestigeTest.kt` and `SettingsResetAboutTest.kt`.

- [x] Hardware rows show all 20 stable tiers, base hashrate, effective tier output, owned count, power/heat per unit, unlock requirement and x1/x10/x25/MAX cost/count. Each deficit is explicit; a purchase-mode shortfall has a Bitcoin-sale route and infrastructure throttling opens Power/Cooling upgrades.
- [x] Present exactly 56 ordinary upgrades under Tapping, Compute, Infrastructure (Power/Cooling subgroups), Automation. Preserve all IDs, expose the four tracks and their two infrastructure subgroups, show each saved effect/price/prerequisite/availability state, and explain exact lifetime-BTC or prerequisite locks. Keep the existing 12-node Satoshi tree separate.
- [x] Keep all 40 achievements accessible in Stats with criterion progress and one-time unlock state. The ten event definitions expose actual modifier/market descriptions and remaining expiry; windfalls stay reachable. Daily reward remains the seven-day cumulative/20-hour flow and is offered after the first machine. Offline return reports credited raw BTC once.
- [x] Complete source statistics: current/lifetime BTC and Cash, current/peak mining speed, manual taps/tap BTC, foreground/offline/reward/event BTC, hardware/upgrades, BTC sold, price extremes, event/achievement/prestige/point counts, fractional playtime, current/peak temperature and bounded power/energy history. Schema-1 and pre-marker schema-2 saves keep lifetime totals and display partial/unassigned source coverage; fresh v1.2 tracking is marked complete.
- [x] Complete independent sound/haptics/reduced-motion/battery-friendly/number-format settings, reset cancel/confirm and About. The Settings route reads actual PackageInfo/application metadata through its Android Context, and About displays package, version name/code, build variant, minimum API, simulation notice and release highlights. Reset confirmation names the cleared fields.
- [x] Add JVM coverage for all grouped upgrade IDs/subgroups, lock reasons, all achievement progress mappings, source coverage/fractional time/energy, effective zero-owned hardware output, legacy migration marker, stable achievement targets and persisted independent battery preference. Add Compose instrumentation source tests for all hardware tiers, deficit/funding routes, grouped upgrades, statistics/achievement browsing, event/reward/offline/prestige sheets, About metadata and reset behavior. `testDebugUnitTest --rerun-tasks compileDebugAndroidTestKotlin lint assembleDebug` passes locally: 170 JVM tests and instrumentation Kotlin compiles; UI assertions remain unexecuted without a device.
- [ ] Run Compose journeys 02–06 and M5 tests on API31/API36 emulator; inspect early/mid/late/migrated/locked/deficit screens, font scale 1.5, TalkBack, reward/prestige cancellation, duplicate clicks and exact layout at compact/large text sizes. Capture screenshots after Android Studio Device Manager and an image/AVD are available.

**Tests/commands:** `bash ./gradlew --no-daemon --console=plain testDebugUnitTest --rerun-tasks compileDebugAndroidTestKotlin lint assembleDebug`; `bash ./gradlew --no-daemon --console=plain connectedDebugAndroidTest`.

**Observable acceptance:** all retained software systems remain reachable; settings change actual behavior; source/lifetime stats do not guess legacy attribution; every core route has a meaningful semantic test. Compose layout/accessibility acceptance stays pending until emulator execution.

## M6 — Native Identity, Version and Signed Update (F18/F23–F25)

**Modify:** `app/src/main/res/drawable/ic_launcher_foreground.xml`, `ic_launcher_background.xml`; `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml` and `ic_launcher_round.xml`; `app/src/main/res/mipmap-{mdpi,hdpi,xhdpi,xxhdpi,xxxhdpi}/ic_launcher.webp` and `ic_launcher_round.webp`; `app/src/main/res/values/themes.xml`, `strings.xml`; `app/src/main/AndroidManifest.xml`; `MainActivity.kt`; `app/build.gradle.kts`; `ui/navigation/AppNavHost.kt`; Settings/About UI.

**Create:** `app/src/main/res/drawable/ic_launcher_monochrome.xml`; `app/src/main/res/values-v31/themes.xml` for Android system splash; `app/src/androidTest/java/com/antigravity/bitcoinminingtycoon/IdentityUpdateTest.kt`.

- [x] Replace robot/grid with original graphite/copper silicon/hash-channel vector: 108dp adaptive layers inside the 66dp safe area, round mask, dedicated single-color monochrome layer and all density fallback assets. XML contract tests pass; density assets were visually inspected. No referenced robot artwork remains.
- [x] Configure the Android 12+ platform splash through the existing application theme, graphite system and window backgrounds, and the original foreground icon. No splash Activity, launch delay or extra dependency was added. Manifest keeps portrait MainActivity, `allowBackup=false`, app label, applicationId, namespace and package; the existing DataStore filename is untouched. Live splash/white-flash observation remains pending below.
- [x] About reads real versionName/versionCode/build from PackageManager (wired in M5 and verified by compiled instrumentation source); the APK metadata now reports name `1.2.0` and code `2`. Save schema `2` and `balanceRulesVersion=1` remain independent.
- [ ] Reconcile code `2` against external distribution history. Tracked branches/tags contain no higher candidate, but GitHub release metadata returned `Forbidden`; candidates distributed outside repository history remain unknown.
- [x] Build the minified release with existing debug signing as a local sideload artifact. The actual `origin/v1.0` source snapshot was rebuilt from a temporary `git archive`; its release APK and this candidate report the same `apksigner` certificate fingerprint.
- [ ] Install `artifacts/m0/v1.0-release.apk`, seed a profile, then install `app/build/outputs/apk/release/app-release.apk` with `adb install -r`; verify settings/assets/claims/scene/discovery after relaunch and process death. ADB lists no device, so no install or retained-save result is claimed. Never uninstall or clear app data.
- [ ] Inspect launcher/app drawer, adaptive/round/themed icon, App Info, recents, cold/warm/task launch, splash, system bars and About on API31/API36. Inspect merged release manifest/permissions and R8/resources; source and package inspection shows no INTERNET permission, `allowBackup` remains false, and no test helpers/secrets ship in the release package. Emulator visual checks remain pending.

**Tests/commands:** `bash ./gradlew --no-daemon --console=plain testDebugUnitTest --rerun-tasks compileDebugAndroidTestKotlin lintDebug lintRelease assembleDebug assembleRelease` passed with 173 JVM tests and 0 failures; `IdentityUpdateTest` instrumentation source compiled. `/workspace/.android-sdk/build-tools/36.0.0/apksigner verify --print-certs app/build/outputs/apk/release/app-release.apk`, `/workspace/.android-sdk/build-tools/36.0.0/aapt dump badging app/build/outputs/apk/release/app-release.apk`, and `sha256sum app/build/outputs/apk/release/app-release.apk` passed. `bash ./gradlew --no-daemon --console=plain connectedDebugAndroidTest` failed with `DeviceException: No connected devices!`; no `adb install -r` command could be performed. The preserved `origin/v1.0` snapshot rebuilt via `git archive origin/v1.0` plus `bash ./gradlew --no-daemon --console=plain assembleRelease`; the APK is ignored at `artifacts/m0/v1.0-release.apk`.

**Observable acceptance:** partial. APK metadata, manifest, R8 package, icon/splash resource structure and certificate comparison are evidenced. Emulator screenshots, live splash, About, and same-certificate in-place update retaining state without clearing data remain unverified. The public distribution certificate is unknown; this candidate is debug-signed only.

## M7 — Full F01–F25 Release Audit and Evidence (all features)

**Modify:** `docs/CURRENT_STATE.md`, `CHANGELOG.md`, `docs/TEST_STRATEGY.md`, `docs/RELEASE_CHECKLIST.md` and only code files implicated by reproduced release blockers.

- [x] Run the complete JVM, simulation/content/migration suite, focused release-risk matrix, `lintDebug`, `lintRelease`, Android-test Kotlin compilation, `assembleDebug` and `assembleRelease`. M7 full run passed 173 tests; focused adversarial/migration/concurrency/pacing run passed 99 tests. Connected tests compiled/packaged, then reported no connected device.
- [ ] Run all nine documented journeys sequentially on API31 and API36. The AVD/Studio/emulator is unavailable. Journey 07 still needs a force-stop/relaunch; Journey 08 still needs same-package same-certificate install -r with retained data; no uninstall/data clear was used.
- [x] Build and statically inspect the minified APK, R8 mapping, package/version, manifest permissions, backup/icon references and certificate; release SHA-256 is recorded below.
- [ ] Install/launch on both emulator versions and repeat first-session, sale, machine, reward, prestige, settings, process-death and migration flows on the release artifact.
- [x] Recheck zero/huge values, clock rollback, 1–59s offline, event expiry, repeated claims, transaction races, legacy unlocks, future/corrupt recovery and bounded MAX using the 99-test targeted suite; accessibility/1.5-font/TalkBack/reduced-motion, rapid-tap rendering, launcher/splash and process-death UI source tests compile only. Android Studio Profiler and visual acceptance are unavailable; no real-phone performance claim is made.
- [x] Map F01–F25 to milestone source/tests/evidence and review all five Review Focus cases. Legacy migration, transaction/tick, time/offline/event, and economy pacing have software tests; migrated/accessibility/native identity runtime proof remains blocked. Read all nine journey instructions and keep each outcome explicit in `docs/TEST_STRATEGY.md` / `docs/RELEASE_CHECKLIST.md`.
- [x] Update CURRENT_STATE, TEST_STRATEGY, RELEASE_CHECKLIST and CHANGELOG with exact test/build/install attempts, no-device result, release APK SHA/certificate, package/schema/balance versions, current commits and outstanding limits. No screenshot, emulator/API execution or install result is invented.
- [ ] Commit/push scoped M7 documentation normally to v1.2, preserve main/v1.0/all historical refs, and perform a read-only final review. No force push, history rewrite, branch deletion, main merge or release tag. M7 release acceptance remains partial until emulator journeys and external version history are verified.

**Full release commands:**

~~~bash
bash ./gradlew testDebugUnitTest
bash ./gradlew lintDebug
bash ./gradlew lintRelease
bash ./gradlew connectedDebugAndroidTest
bash ./gradlew assembleDebug
bash ./gradlew assembleRelease
adb devices -l
adb install -r app/build/outputs/apk/release/app-release.apk
adb shell am start -n com.antigravity.bitcoinminingtycoon/.MainActivity
mkdir -p artifacts/m7
adb exec-out screencap -p > artifacts/m7/release-home.png
adb shell am force-stop com.antigravity.bitcoinminingtycoon
adb shell am start -n com.antigravity.bitcoinminingtycoon/.MainActivity
apksigner verify --print-certs app/build/outputs/apk/release/app-release.apk
aapt dump badging app/build/outputs/apk/release/app-release.apk
sha256sum app/build/outputs/apk/release/app-release.apk
git diff --check
git status --short --branch
~~~

**Observable acceptance:** partial. Offline/source/package evidence, APK checksum/certificate and scoped commit history are recorded. The complete nine-journey acceptance matrix, installed minified release flows, process-death behavior, and v1.0 in-place save retention remain blocked by missing emulator/device support; external distribution version history is also inaccessible. Physical vibration feel, device-manufacturer icon variants and real-phone performance remain explicitly unverified.

## F01–F25 Coverage

| Requirement | Milestone | Existing/new files and observable proof |
|---|---|---|
| F01 Teaching/discovery | M1, M3 | GameState teaching IDs; DiscoveryPresentation, TeachingCue, OnboardingDialog; FirstSessionLoopTest and Journey 01 prove loop and migrated disclosure. |
| F02 Tap/feedback | M2, M4 | GameEngine, CoreMineButton, MiningFeedbackLayer, Haptics; exact tap/critical RNG tests, reduced-motion and rapid-tap emulator proof. |
| F03 Passive production | M1, M2 | GameEngine, EconomyEngine, GameRepository; tick-rate equivalence/race tests and passive-over-manual minute-five report. |
| F04 Balances/format | M2, M3, M5 | BalanceConfig, GameNumber/NumberFormatter, MineScreen; zero/dust/huge-value and visible early-earnings tests. |
| F05 Market/sell | M1–M3, M5 | MarketEngine.previewProceeds, MarketCard, SaleQuote; Sell10/50/MAX preview equality, threshold and post-prestige unlock journey. |
| F06 All hardware tiers | M2, M4, M5 | Miners, ContentIntegrityTest, FacilityPresentation; 20 IDs, benefits/unlocks/power/heat and every scene mapping. |
| F07 Bulk purchase | M1, M2, M5 | FleetEngine/GameNumber/HardwareScreen; geometric costs, affordability edges, rapid atomic buys and bounded huge MAX. |
| F08 Power | M2, M5 | Infrastructure/PowerEngine/UpgradeScreen; ten stages, visible benefit, soft deficit and reachable remedy. |
| F09 Cooling | M2, M5 | Infrastructure/ThermalEngine/UpgradeScreen; seven stages, equilibrium, threshold, useful remedy, no loss. |
| F10 Ordinary upgrades | M2, M5 | Upgrades/UpgradeEngine/UpgradeScreen; 56 useful effects, grouping, prerequisites, valid targets and per-effect tests. |
| F11 Achievements | M2, M5 | Achievements/AchievementEngine/StatsScreen/AchievementBanner; 40+ retained, one-time rewards, accessible progress. |
| F12 Events/windfalls | M1, M2, M5 | EventEngine/Events/OfflineEngine; stacking, expiry, reachable collect, once-only claim and offline expiry. |
| F13 Prestige | M1, M2, M5 | PrestigeEngine/PrestigeSheet/GameRepository; preview=apply, large/legacy points, cancel, repeat and exact reset list. |
| F14 Satoshi tree | M2, M5 | PrestigeNodes/PrestigeEngine/SatoshiTreeScreen; 12 stable nodes, DAG, atomic spend and faster next run. |
| F15 Offline | M1, M5 | OfflineEngine/ClockProvider/pending summary; 0/rollback/1–59/60/11h59/12h/>12h, event split and exactly-once resume. |
| F16 Daily rewards | M1, M2, M5 | DailyRewards/DailyRewardSheet; 7-day, 20-hour, no missed-day reset, post-first-machine disclosure, capped assist, repeat/rollback. |
| F17 Statistics | M1, M5 | StatsState/StatsScreen; source counts, fractional time/energy, bounded history, lifetime preservation and legacy coverage label. |
| F18 Saves/migration | M0, M1, M6 | GameSave/SaveMigrations/SaveRecoveryCheckpoint/GameRepository; old serializer fixtures, schema2, durable update and safe recovery. |
| F19 Settings/recovery | M1, M5 | SettingsState/SettingsScreen/AboutScreen; persisted controls, recovery status, reset cancel/confirm and real build metadata. |
| F20 Sound/haptics | M4, M5 | SoundPlayer/Haptics; independent toggles, capability/rate/resource tests; emulator wiring only. |
| F21 Accessibility/native | M3–M6 | AppNavHost/screens/Activity/theme; 48dp, 1.5 font, TalkBack, compact/tall, insets, back and reduced motion. |
| F22 Facility scenes | M2, M4 | FacilityStage/Presentation/Scene and GameState discovery; ten scenes, all 20 tiers, capped density and prestige reset behavior. |
| F23 App identity | M0, M6 | icon vectors/adaptive resources/themes/manifest/MainActivity; launcher, masks, splash, App Info and recents proof. |
| F24 Version/package | M0, M6, M7 | VERSION_CONTROL/app/build.gradle/GameSave/manifest; protected refs, same package/storage/signature, versionName/code and APK record. |
| F25 Release quality | M0–M7 | tests, journeys, docs, release artifact; milestone gates, full matrix, minified smoke and truthful evidence. |

## Environment Blockers and Exact Validation Setup

Observed in this container:

- System java is 21, but a working JDK 17.0.20.1 is available at /workspace/.java17-root/usr/lib/jvm/java-17-openjdk-amd64. The wrapper is Gradle 9.1.0 and runs on JDK17 when JAVA_HOME is set. gradlew is not executable; invoke it through bash.
- SDK platform 36, build-tools 36.0.0 and platform-tools/ADB 37.0.1 are installed. Platform 31 and emulator package are absent. ADB starts and reports no devices. Android CLI emulator list returns no AVD; no /dev/kvm exists.
- No studio executable is on PATH and Android CLI `studio check` reports no running Android Studio. Android CLI 1.0.16457483 is installed at `/workspace/.android-user/bin/android-cli`; observed supported forms include `--no-metrics --sdk=... skills list --help`, `emulator list`, `emulator create --help`, `sdk list --all <pattern>` and `studio check`. `emulator create --help` accepts a device profile but exposes no API-level selection. The current catalog query shows no API31 platform/system image. Use Android Studio Device Manager/SDK Manager on an available host; if Studio is unavailable, do not invent an API-selection CLI flag. The deprecated `sdkmanager` shim directs users to Android CLI.
- Repo local skills present: feature-cycle, frontend-design, game-balance, product-copy, bug-hunt, release-audit, testing-setup, edge-to-edge and navigation-3. Android CLI lists adaptive, edge-to-edge, navigation-3 and testing-setup; no project-specific feature-cycle slash command is assumed. Read/use local skill files by name/path through the active harness. Codex /goal availability is surface-specific per docs/WORKFLOW.md; after approval use it only if present, otherwise use the same approved sequential prompt/ledger contract.
- Cloud onboarding saved a revised `start_skill` draft with the verified JDK/SDK exports, Gradle checks, Device Manager guidance and the no-worktree rule. The existing install script, network policy, secrets and environment-variable requirements were preserved. The draft is saved for user review; it has not been applied or published.
- Current local debug keystore exists outside the repo at `/workspace/.android-user/debug.keystore` and was created in this workspace on 2026-09-30. M6 rebuilt the archived `origin/v1.0` source into ignored `artifacts/m0/v1.0-release.apk`; its signing certificate matches the v1.2 candidate for local same-key sideload validation. This does not prove that the key matches any separately distributed install.
- Read and write access to `origin/v1.2` have been exercised by the scoped M1–M6 source and documentation pushes. `main`, `v1.0` and historical refs remain unchanged; M7 documentation push and final review are the remaining ledger actions.

The current container cannot yet satisfy Android Studio Device Manager, API31/API36 emulator, screenshots, TalkBack, connected instrumentation, process-death UI or signed in-place emulator proof. These checks can proceed on the documented Windows Android Studio host or after Android Studio, API31/API36 system images and emulator support are available. A physical USB phone is not needed. JVM/build work can continue independently; emulator evidence remains blocked until then.

**Linux shell environment and exact commands:**

~~~bash
export JAVA_HOME=/workspace/.java17-root/usr/lib/jvm/java-17-openjdk-amd64
export ANDROID_HOME=/workspace/.android-sdk
export ANDROID_SDK_ROOT=/workspace/.android-sdk
export ANDROID_USER_HOME=/workspace/.android-user
export GRADLE_USER_HOME=/workspace/.gradle
export PATH=$JAVA_HOME/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/build-tools/36.0.0:$PATH
bash ./gradlew --version
bash ./gradlew testDebugUnitTest
bash ./gradlew lintDebug
bash ./gradlew assembleDebug
adb devices -l
bash ./gradlew connectedDebugAndroidTest
~~~

For Windows Android Studio, use the documented wrapper commands in `SETUP_WINDOWS.md`:

~~~powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat lintDebug
.\gradlew.bat assembleDebug
.\gradlew.bat connectedDebugAndroidTest
~~~

Create API31/API36 AVDs in Device Manager and run them sequentially. With one running emulator, use `adb install -r`, `adb shell am start -n com.antigravity.bitcoinminingtycoon/.MainActivity`, `adb shell am force-stop com.antigravity.bitcoinminingtycoon` and `mkdir -p artifacts/m7 && adb exec-out screencap -p > artifacts/m7/release-home.png`. Keep screenshots, profiler traces and simulation reports under ignored `artifacts/`.

## Blocking Ambiguities

No product or architecture ambiguity remains that prevents implementation planning. Default normalization, 56-upgrade allocation, schema-2 recovery, reference policy, journey coverage and branch behavior are specified above and in the canonical docs. Current missing Android Studio/emulators are environment blockers for dependent evidence, not unresolved product questions. External signing compatibility remains conditional on possession of the original installed certificate, as SAVE_COMPATIBILITY requires.
