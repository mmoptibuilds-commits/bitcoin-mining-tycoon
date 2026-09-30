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
- After approval, commit and push this plan as a docs-only commit to `v1.2`, then begin M0. Keep plan approval separate from implementation and do not commit or push this review artifact before approval.
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
- The plan is saved as an uncommitted review artifact. No production source, test source or app runtime configuration is changed in this planning pass.

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

- [ ] Recheck v1.2 cleanliness/history and all three version refs; record starting SHA, local/remote refs, remote URL and worktree status. Run `git status --short --branch`, `git branch --all --verbose --no-abbrev`, `git log --graph --decorate --oneline -25`, `git remote -v`, `git ls-remote --heads origin` and `git diff --check`. Confirm v1.2 is the only write target.
- [ ] Confirm wrapper/JDK/SDK and registered Gradle tasks. Run `bash ./gradlew --version` and `bash ./gradlew tasks --all` with JDK 17 and the SDK environment below. Check observed Android CLI forms with `/workspace/.android-user/bin/android-cli --no-metrics --sdk=/workspace/.android-sdk skills list --help`, `/workspace/.android-user/bin/android-cli --no-metrics --sdk=/workspace/.android-sdk emulator list`, `/workspace/.android-user/bin/android-cli --no-metrics --sdk=/workspace/.android-sdk emulator create --help`, `/workspace/.android-user/bin/android-cli --no-metrics --sdk=/workspace/.android-sdk sdk list --all 'platforms;android-31'` and `/workspace/.android-user/bin/android-cli --no-metrics --sdk=/workspace/.android-sdk studio check`. Use Android Studio `Device Manager` and `SDK Manager` for API31/API36 AVD/system-image setup; the CLI `emulator create` help exposes device profiles but no API selector, so do not invent one.
- [ ] Re-run the unchanged baseline suites/build after plan approval with task execution forced: `bash ./gradlew testDebugUnitTest --rerun-tasks`, `bash ./gradlew lintDebug --rerun-tasks`, `bash ./gradlew assembleDebug --rerun-tasks` and `bash ./gradlew assembleRelease --rerun-tasks`. Record outputs, baseline APK SHA and package/version. The onboarding checks below are preliminary evidence from the same source SHA; M0 re-records them in its evidence ledger. No connected result is claimed until an emulator actually runs.
- [ ] Build the archived v1.0 app from `origin/v1.0` in a unique `/tmp/bitcoin-mining-tycoon-v1.0.XXXXXX` directory using the same local signing key. Produce `artifacts/m0/v1.0-release.apk` and capture its public certificate fingerprint with `apksigner`. Add a temporary `app/src/test/java/com/antigravity/bitcoinminingtycoon/data/LegacyFixtureWriterTest.kt` only in that source snapshot; encode deterministic early, mid, late, prestiged, daily-claimed, settings-disabled, missing-optional and large-value schema-1 payloads with the v1.0 `GameSave` serializer into the main checkout's `app/src/test/resources/saves/v1/`. Run only that fixture-writer test, then delete the temporary test and source snapshot. Keep only serializer-produced fixtures in the v1.2 test resources; never add the writer to the production source set.
- [ ] In Android Studio Device Manager create API31 and API36 portrait phone AVDs, run one at a time, and seed a dedicated preservation profile following `journeys/08-save-upgrade.md`. Capture baseline Mine/Hardware/Settings screens and the v1.0 asset/settings/claim snapshot. Do not uninstall or clear data during the upgrade profile.
- [ ] Update the evidence ledger with exact commands/results, device/API, screenshots, APK/certificate facts and blockers; commit/push only scoped M0 evidence to v1.2 after approval.

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

- [ ] Write fixture-based migration tests before changing the serializer. Give every schema-1 field distinctive values: balances, custom/baseline tap strength, stable and unknown miner/upgrade IDs, power/cooling, market price/trend/history/timers, auto-sell flag/threshold, every ActiveEventState field, achievements, spendable points/nodes, day/cooldown, all legacy stats, onboarding, all settings, lastSaveWallMillis and rngSeed. Assert exact preserved values, schema 2 only after durable commit, missing optional defaults and round-trip/idempotence.
- [ ] Implement schema-1→2 migration and checkpointing in `data/SaveDataSource.kt`, `data/migrations/SaveMigrations.kt`, `data/SaveRecoveryCheckpoint.kt` and `app/BitcoinMiningTycoonApp.kt`. Validate fields individually; keep known assets if a timer/optional value is invalid; preserve unknown IDs but exclude them from calculations. Checkpoint one raw payload (maximum 4 MiB) atomically under `filesDir/save_recovery/` before recovery; if it is larger or checkpoint writing fails, leave the original DataStore untouched and block writes/reset. Never log payload bytes. Unsupported future schema cannot enter a mutating game.
- [ ] Add schema-2 defaults: balanceRulesVersion independent of schema/app version; completedTeachingCueIds; highestDiscoveredFacilityStage derived from current old ownership; batteryFriendlyAnimations; source-attribution counters; playtimeFractionalSeconds; bounded power/energy history; peakTemperatureC; prestige point counters; pending offline summary. Preserve all existing stats. Set `prestigePointsBaselineV2 = min(old lifetimeSatoshiPointsEarned, floorSqrt(old lifetimeBtcMined))`, `prestigePointsEarnedSinceV2 = 0` and `dailyPointsEarnedSinceV2 = 0`; retain the old aggregate unchanged. New prestige eligibility is `max(0, floorSqrt(lifetimeBtcMined) - prestigePointsBaselineV2 - prestigePointsEarnedSinceV2)`. New daily points update the legacy aggregate and `dailyPointsEarnedSinceV2`, never this eligibility baseline. This conservatively avoids replaying legacy awards without letting daily points permanently stall prestige. Mark old source attribution unavailable.
- [ ] Implement mutateLatest with one repository mutex used by ticks, actions, immediate DataStore commits and flush. Publish an immediate mutation only after durable update succeeds. Route all GameViewModel actions through reducers over the latest GameState; remove stale read/compute/save races. A failed commit gives no success sound/haptic or visible success.
- [ ] Correct subsecond playtime using persisted fractional remainder; bound/validate elapsed time, timers, counts and numeric values. Persist RNG after every random-consuming tick/critical tap and prove identical continuation after save/reload.
- [ ] Make offline production credit every positive interval, including 1–59 seconds; keep the 60-second rule summary-only if retained. Integrate at active-event/boost expiry boundaries, expire only the elapsed event contribution, cap at 43,200 seconds, and atomically persist BTC, source stats, pending summary and timestamp during startup initialization. The return sheet only acknowledges or dismisses the already credited summary; process death after commit may show the summary again but cannot apply BTC again.
- [ ] Fix prestige bookkeeping in `engine/PrestigeEngine.kt` and `util/GameNumber.kt`: preview and apply call one exact integer-square-root calculation over nonnegative BigDecimal/BigInteger values, saturating at Long.MAX_VALUE; never convert economic magnitudes to Double. Persist `prestigePointsEarnedSinceV2` separately from spendable `satoshiPoints`, legacy combined `lifetimeSatoshiPointsEarned` and `dailyPointsEarnedSinceV2`. A successful award increments the spendable balance, the legacy combined aggregate and post-v1.2 prestige counter atomically. Test old daily-heavy saves can earn again when eligible lifetime BTC grows. Reset only temporary assets/events/enabled auto-sell; preserve threshold, permanent data, settings, claims and discovery.
- [ ] Add tests for tick↔purchase/sell/claim/prestige/flush interleavings, two concurrent MAX buys, durable-write failure, fractional ticks, RNG reload, event expiry mid-foreground/offline tick, repeated windfall/daily/offline/prestige, wall rollback/extreme clocks, short/12h/>12h gaps, huge decimal prestige, corrupt/future checkpoint and legacy unlocks. Include process death immediately after durable transactions.
- [ ] Run targeted JVM regression suites, Compose migration/recovery interaction tests and Journey 07 plus Journey 08 on the API31/API36 emulator when available. Capture migrated early/late/recovery screens before moving on.

**Tests/commands:** `bash ./gradlew testDebugUnitTest --tests '*SaveMigrationsTest' --tests '*GameRepositoryTest' --tests '*GameViewModelTest' --tests '*OfflineDailyTest' --tests '*EventAchievementTest' --tests '*PrestigeEngineTest' --tests '*GameEngineTest' --tests '*AdversarialBugHuntTest' --tests '*ContentIntegrityTest'`; `bash ./gradlew connectedDebugAndroidTest`.

**Observable acceptance:** all schema-1 fields survive fixtures, unknown/future/corrupt data cannot silently overwrite originals, repeated claims/tick races do not duplicate or lose state, and emulator relaunch/process death retains the credited state. Missing emulator proof stays blocked.

## M2 — Balance, Content and Reference Pacing (F02–F16/F22)

**Create:** `content/BalanceConfig.kt`; `engine/ReferencePolicySimulator.kt`; `engine/ReferencePacingSimulationTest.kt`. `content/ContentIntegrityTest.kt` is created in M1.

**Modify:** `content/Miners.kt`, `Upgrades.kt`, `Infrastructure.kt`, `DailyRewards.kt`, `Events.kt`, `PrestigeNodes.kt`; `engine/EconomyEngine.kt`, `GameEngine.kt`, `MarketEngine.kt`, `FleetEngine.kt`, `UpgradeEngine.kt`, `PowerEngine.kt`, `ThermalEngine.kt`, `EventEngine.kt`, `AchievementEngine.kt`, `OfflineEngine.kt`, `PrestigeEngine.kt`; `util/GameNumber.kt`; the corresponding existing engine test files listed in the test command below.

- [ ] Put initial cash `0`, manual baseline `50,000`, opening market price `$50,000`, current first-miner cost `$10`, rates, growth curves, power/cooling effects, reward scaling, prestige curve, offline cap and all tuning constants in one tested BalanceConfig. Keep BTC/USD magnitudes decimal-safe. Match fresh/load/reset/prestige defaults; normalize only recognized legacy manual baselines 10/50,000 and leave custom values intact. The onboarding cash grant is zero; day-one cash reward is at most `$3` and remains undisclosed until the first machine is owned.
- [ ] Preserve all 20 stable miner IDs and all 10 power/7 cooling stages. Tune cost, unlocks, hashrate, power draw and heat without deleting owned assets or introducing hard purchase blocks. Correct gpu_6x_custom_os target to gpu_rig_6x without renaming its saved ID.
- [ ] Retain the 32 existing ordinary upgrade IDs and add 24 meaningful definitions for 56 total: Tapping 9, Compute 22, Power 8, Cooling 8, Automation 9. The 24 new stable IDs are `precision_actuation`, `input_pipeline`, `quantum_fingerprints`; `asic_firmware_2`, `rack_thermal_routing`, `warehouse_scheduler`, `hydro_fluid_cooling`, `geothermal_hash_mesh`, `nuclear_fpga_farm`, `fusion_batcher`, `orbital_shard_cache`, `lunar_quantum_scheduler`; `demand_response_grid`, `solar_tracking_inverters`, `fusion_load_balancer`; `cold_plate_lattice`, `immersion_circulation`, `cryogenic_heat_recovery`; `auto_sell_controller`, `fleet_scheduler`, `market_spread_router`, `energy_aware_dispatch`, `offline_mining_buffer`, `event_response_automation`. Assign three to Tapping, nine to Compute, three to Power, three to Cooling and six to Automation in that order. Add critical-tap 2%/2× as an optional effect only after the no-crit reference passes; implement auto-sell/offline effects in their named Automation upgrades only with engine-backed tests. Never count prestige nodes toward 56.
- [ ] Keep all existing achievements/events/tree/reward definitions and stable IDs. `ContentIntegrityTest` asserts the 20 miner IDs, ten power stages, seven cooling stages, 56 upgrade IDs/category counts, valid miner targets, unique IDs, acyclic prerequisites and a tested engine handler for each special effect. Test one-time rewards, event stacking and expiry, 7-day cumulative reward cycle, 20-hour cooldown and daily point separation. Day-one cash is at most 30% of first miner cost and appears only after that machine; later awards scale to 30–90 seconds of current production with bounded stage-aware caps.
- [ ] Build ReferencePolicySimulator from real GameEngine, MarketEngine, FleetEngine, UpgradeEngine and PrestigeEngine transitions with an injected deterministic clock/RNG. Fixed reference price is $50,000, no rewards/events/critical taps, 2 taps/sec for 90 seconds, then five-second bursts once/minute through minute 10, then no taps; sell every 15 simulated seconds when cash is needed; every five seconds buy the best affordable marginal effective-production/cost option while allowing bounded saving for a useful unlocked tier. Record deterministic tie breaks, config hash, milestone times and purchase log.
- [ ] Assert first sale/first machine 30–90s; GPU 3–6m; first infrastructure 7–12m; ASIC 12–20m; passive contribution > manual by minute 5; first prestige 25–35m; and a second comparable run after buying efficient_silicon is ≥25% faster. Run seeds 7, 42, 1337, 2026 and 8675309 for real market/events and publish variance; also run low-tap, no-daily, daily, market-holder, infrastructure-aware, delayed-prestige, multi-prestige, legacy and 12-hour-offline policies.
- [ ] Verify geometric x1/x10/x25/MAX totals, exact affordability/one-below, huge MAX boundedness, each upgrade's one-time effect, no modifier duplication, no NaN/Infinity/negative balance and no unrecoverable power/cooling stall. Preserve deterministic equilibrium unless simulation evidence requires a recorded decision.
- [ ] Run engine/content/simulation tests; on emulator capture first sale, first machine, GPU/ASIC hardware and the organized upgrade/infrastructure states. Execute Journey 02 and 03. These are M2 evidence, not deferred to M7.

**Tests/commands:** bash ./gradlew testDebugUnitTest --tests '*ReferencePacingSimulationTest' --tests '*ContentIntegrityTest' --tests '*EconomyEngineTest' --tests '*GameEngineTest' --tests '*MarketEngineTest' --tests '*BulkPurchaseTest' --tests '*UpgradeEngineTest' --tests '*PowerThermalTest' --tests '*OfflineDailyTest' --tests '*PrestigeEngineTest'; bash ./gradlew connectedDebugAndroidTest.

**Observable acceptance:** deterministic report shows every locked target with seed/config/purchase trace; five live RNG seeds document variance; all content integrity checks pass; emulator sale/purchase and power/cooling remedy flows match those same engine values.

## M3 — Facility Home, Progressive Teaching and Navigation (F01/F04/F05/F19/F21)

**Modify:** ui/navigation/AppNavHost.kt, NavigationKeys.kt; ui/screens/mine/MineScreen.kt, MarketCard.kt, EnvironmentalPanel.kt, onboarding/OnboardingDialog.kt; ui/theme/AppColors.kt, AppTypography.kt, Theme.kt; viewmodel/GameViewModel.kt and GameUiState.

**Create:** `ui/presentation/DiscoveryPresentation.kt`; `ui/components/TeachingCue.kt`; `app/src/test/java/com/antigravity/bitcoinminingtycoon/ui/presentation/DiscoveryPresentationTest.kt`; `app/src/androidTest/java/com/antigravity/bitcoinminingtycoon/ui/FirstSessionLoopTest.kt`; `app/src/androidTest/java/com/antigravity/bitcoinminingtycoon/ui/NavigationAccessibilityTest.kt`.

- [ ] Replace the equal Mine/Hardware/Upgrades/Stats dashboard hierarchy with Mine home, reachable Hardware/Upgrades destinations and utility Stats/Settings/About. Use pinned Navigation 3 APIs for saveable route/back state; keep bulk selection/list position stable. Add deterministic BackHandler/predictive-back behavior without a second navigation library.
- [ ] Make the facility scene the home anchor; balance Bitcoin/Cash, Mine, Mining speed, sale controls and one next goal around it. Use readable graphite/copper typography, open sections and dividers. Remove v1.0 console copy, fake telemetry emphasis and equal-weight card wall.
- [ ] Implement DiscoveryPresentation from GameState and completedTeachingCueIds. Teach tap→Bitcoin→Sell→Cash→first machine→passive income through short persisted cues; no forced slide deck and no $15 onboarding grant. First CPU is reachable before ownership. Persist cue completion; preserve onboardingCompleted; reveal old systems from existing assets. Keep the fictional-simulation notice concise on first launch/About.
- [ ] Show a small Sell10/50/MAX cash quote using MarketEngine.previewProceeds(state, percentage); quote and transaction use the same market multiplier/rounding. Expand actual market history/automation controls only when useful. Show deficits with an accessible remedy; never hide the fix behind itself.
- [ ] Add Compose semantics for all core actions, error/locked reasons, 48dp targets and no continuous balance announcements. Respect compact/tall content, safe/cutout insets, keyboard resize and predictable back.
- [ ] Add first-session and migrated-user Compose tests. Assert cues persist, action leads to sale, displayed proceeds match state, first machine is reachable and buys, passive output then appears, no crypto glossary or blocked remedy exists. Capture clean and migrated screens and run Journey 01/06 on emulator; test font scale 1.5 and TalkBack.

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

- [ ] Hardware rows show all 20 stable IDs, readable benefit, price, owned count, current output and power/heat impact. Nearby locked rows explain requirement; distant progression can collapse but remains navigable. Sticky x1/x10/x25/MAX shows exact cost/count. A deficit warns and offers remedy without blocking purchase.
- [ ] Present exactly 56 ordinary upgrades under Tapping, Compute, Infrastructure (Power/Cooling subgroups), Automation. Show effect, price, prerequisites and purchased/available/locked state. Keep the 12-node Satoshi tree separate and preserve all IDs.
- [ ] Keep 40+ achievements accessible in Stats with one-time reward semantics/progress. Events retain ten definitions, visible effect/duration/expiry and reachable windfalls. Daily stays seven-day cumulative/20-hour cooldown, appears after first machine and keeps old claim state. Offline is concise raw-BTC duration/earnings once.
- [ ] Complete stats: current/lifetime BTC and Cash, current/peak Mining speed, manual taps/tap BTC, foreground passive BTC, offline BTC, reward/event BTC, hardware/upgrades, BTC sold, price extremes, event/achievement/prestige/point counts, fractional active playtime, peak temperature and bounded power/energy history. Show legacy lifetime aggregates unchanged and mark source breakdown as tracked since v1.2.
- [ ] Complete independent sound/haptics, reduced motion, battery-friendly animation, number format, About actual package version/build/changelog/simulation statement and reset confirmation. `MainActivity.kt` reads `PackageInfo` and passes `AppVersionInfo(versionName, versionCode, buildType)` into `AppNavHost`; the About destination displays those values and the fictional/offline simulation statement. Reset cancel preserves all data; confirm names the reset fields and leaves the app usable.
- [ ] Add semantics/state tests for zero/dust/huge values, affordability, all track states, 48dp actions, achievement browsing, reward/prestige cancellation and duplicate clicks, old coverage label, independent settings, reset cancel/confirm, font1.5 and large text. Capture early/mid/late/migrated/locked/deficit screens. Run Journeys 02–06 on emulator.

**Tests/commands:** bash ./gradlew testDebugUnitTest --tests '*HardwareBulkJourneyTest' --tests '*MarketSellJourneyTest' --tests '*OfflineDailyJourneyTest' --tests '*EventsAndAchievementsJourneyTest' --tests '*PrestigeJourneyTest' --tests '*SettingsAndStatsJourneyTest' --tests '*NumberFormatterTest'; bash ./gradlew connectedDebugAndroidTest.

**Observable acceptance:** no retained system is hidden or stubbed; settings change actual behavior; source/lifetime stats do not claim guessed legacy attribution; player can reach every core action at compact and large text sizes.

## M6 — Native Identity, Version and Signed Update (F18/F23–F25)

**Modify:** `app/src/main/res/drawable/ic_launcher_foreground.xml`, `ic_launcher_background.xml`; `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml` and `ic_launcher_round.xml`; `app/src/main/res/mipmap-{mdpi,hdpi,xhdpi,xxhdpi,xxxhdpi}/ic_launcher.webp` and `ic_launcher_round.webp`; `app/src/main/res/values/themes.xml`, `strings.xml`; `app/src/main/AndroidManifest.xml`; `MainActivity.kt`; `app/build.gradle.kts`; `ui/navigation/AppNavHost.kt`; Settings/About UI.

**Create:** `app/src/main/res/drawable/ic_launcher_monochrome.xml`; `app/src/main/res/values-v31/themes.xml` for Android system splash; `app/src/androidTest/java/com/antigravity/bitcoinminingtycoon/IdentityUpdateTest.kt`.

- [ ] Replace robot/grid with original graphite/copper silicon/hash-channel vector: 108dp adaptive layers, recognizable within 66dp safe area, round mask and dedicated single-alpha monochrome layer. Replace every applicable fallback asset; no robot remains in any referenced resource.
- [ ] Configure one Android 12+ system splash through the existing application theme, opaque graphite background and icon. No splash Activity, artificial delay or white flash. Preserve portrait MainActivity, allowBackup=false, app label, applicationId, namespace, DataStore filename and package.
- [ ] About reads real versionName/versionCode/build from PackageManager. Set versionName 1.2.0 and versionCode 2 only after confirming no higher shipped candidate; schema 2 and balanceRulesVersion 1 remain independent.
- [ ] Build minified release with existing debug signing only as an honestly labelled sideload artifact. Compare v1.0 baseline/candidate certificate with apksigner. Install `artifacts/m0/v1.0-release.apk`, seed profile, then install `app/build/outputs/apk/release/app-release.apk` with `adb install -r`; verify settings/assets/claims/scene/discovery after relaunch and process death. Never uninstall or clear app data.
- [ ] Inspect launcher/app drawer, adaptive/round/themed icon, App Info, recents, cold/warm/task launch, splash, system bars and About on API31/API36. Inspect merged release manifest/permissions and R8/resources; no INTERNET, backup enablement, test helpers or secrets.

**Tests/commands:** `bash ./gradlew lintRelease assembleRelease`; `adb install -r artifacts/m0/v1.0-release.apk`; `adb install -r app/build/outputs/apk/release/app-release.apk`; `apksigner verify --print-certs app/build/outputs/apk/release/app-release.apk`; `aapt dump badging app/build/outputs/apk/release/app-release.apk`; `sha256sum app/build/outputs/apk/release/app-release.apk`; `bash ./gradlew connectedDebugAndroidTest`.

**Observable acceptance:** screenshots show original identity across launcher/App Info/recents/splash; release metadata says 1.2.0 with higher code; same-certificate in-place update retains state without clearing data. If the original user-installed certificate is unavailable, report external-install compatibility as blocked even if same-key emulator update passes.

## M7 — Full F01–F25 Release Audit and Evidence (all features)

**Modify:** `docs/CURRENT_STATE.md`, `CHANGELOG.md`, `docs/TEST_STRATEGY.md`, `docs/RELEASE_CHECKLIST.md` and only code files implicated by reproduced release blockers.

- [ ] Run complete JVM suite, simulation/content/migration suites, lintDebug/lintRelease, connected Compose tests and all nine documented journeys sequentially on API31 and API36. Journey 07 performs force-stop/relaunch after committed actions. Journey 08 performs same-package same-certificate install -r; no uninstall/data clear.
- [ ] Install and launch the actual minified app-release.apk on both emulators and repeat first-session, sale, machine, reward, prestige, settings, process-death and migration smoke flows on the release artifact, not only debug.
- [ ] Recheck zero/huge values, clock rollback, 1–59s offline, event expiry, double claims, transaction races, legacy unlocks, future/corrupt recovery, font1.5/TalkBack/reduced motion, icon/splash/R8/merged manifest, fast taps and bounded scenes. Use Android Studio Profiler only for observed emulator performance concerns; do not infer real-phone performance.
- [ ] Verify exact F01–F25 evidence, five Review Focus cases, all nine journeys and RELEASE_CHECKLIST. Fix at cause, never weaken valid assertions, rerun affected tests and capture new proof. Release blockers or unavailable device/signing gates stay explicit.
- [ ] Update CURRENT_STATE with commit, feature IDs, exact test/build/install commands and results, API/emulator, fixtures/seeds/config hash, screenshot/trace paths, public certificate fingerprint, APK SHA-256 and outstanding limits. Update CHANGELOG to distinguish delivered from planned.
- [ ] Commit and push scoped, reviewed milestone changes normally to v1.2. Preserve main, v1.0, work and every historical ref. No force push, history rewrite, branch deletion, main merge or automatic release tag. Do not mark a milestone complete without its own logic, Compose, visual and emulator evidence.

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

**Observable acceptance:** complete acceptance matrix and nine journeys agree with actual output; minified release installs, launches, survives process death and updates v1.0 data in place; APK checksum/certificate and exact commit are recorded. Physical vibration feel, device-manufacturer icon variants and real-phone performance remain explicitly unverified.

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
- Current local debug keystore exists outside the repo at `/workspace/.android-user/debug.keystore` and was created in this workspace on 2026-09-30. There is a debug APK but no archived v1.0 APK or evidence this generated certificate matches an external user's installed app. A same-key v1.0/candidate emulator upgrade is possible; compatibility with a separately installed old key is blocked unless that original certificate/key is available.
- Read-only access to `origin` succeeded; write access has not been probed because this planning pass creates no commit or push. The first user-approved milestone push is the correct time to test destination authorization.

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
