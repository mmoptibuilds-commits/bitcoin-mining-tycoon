# Existing architecture and v1.2 boundaries

## Actual project

`app/src/main/java/com/antigravity/bitcoinminingtycoon/` contains immutable model/GameState, data/GameSave + SaveDataSource + GameRepository, data/migrations/SaveMigrations, content definitions, pure engine objects, platform clocks/audio, GameViewModel and Compose screens/navigation/theme. Keep this arrangement; add focused units rather than an unrelated rewrite.

Stack: Kotlin, Compose/Material 3, Navigation 3, StateFlow/ViewModel, coroutines, DataStore and Kotlin serialization; one app module and manual dependency construction. Current build uses minSdk 31/compileSdk 36/targetSdk 36/JDK 17, AGP 9.0.1 and Compose BOM 2026.03.01. These are observed baseline values, not claims about the latest releases.

## Boundaries

- `content/BalanceConfig.kt`: centralized tuneable defaults/curves; stable content definitions reference it. No UI constants controlling money.
- `engine/`: deterministic economy, purchases, market, event/achievement, power/thermal, offline and prestige transitions. Preserve existing APIs where possible; test changed behavior.
- `model/`: persistent game facts and bounded statistics/discovery; serialize monetary magnitudes as decimal strings.
- `data/`: migration, schema DTO/serializer, storage and durable commit ordering; no UI concepts.
- `viewmodel/`: serial intents/ticker/lifecycle handling and derived presentation state. Derive visible systems, current facility stage and next goal from authoritative state, not duplicate balances.
- `ui/facility/`: state-derived scene model/renderer, capped visual density and deterministic geometry. Animations never produce gameplay output.
- `platform/Haptics.kt`: capability-aware haptic interface/implementation; settings/rate limiting applied consistently; injectable fake for verification.

## Existing interfaces to reuse

`GameEngine.tick(state, deltaSeconds, wallMillis, rng)` and `performManualTap(state)`; `EconomyEngine.calculateManualTapOutput`, `calculateEffectiveHashrate`, `calculateMinedBtc`; `PrestigeEngine.previewPrestige/applyPrestige`; `OfflineEngine.calculateOfflineProgress/applyOfflineReward`; `SaveMigrations.migrate(rawJson)`; `GameSave.fromGameState/toGameState`; `SaveDataSource.update`; `GameRepository.initialize/saveImmediate/flush`; `ClockProvider.monotonicNanos/wallMillis`.

Inspect actual signatures before extending them. Do not add a parallel simulator economy; deterministic simulations drive the real engine APIs. New pure helpers should have narrow state inputs and testable outputs. UI intents must go through one serialized action path so tick/purchase/claim/flush cannot race and overwrite committed progress.

## Persistence

Keep existing DataStore name/location and package. Increment save schema for newly persisted fields; default and migrate through SAVE_COMPATIBILITY. Derive scene state where possible; store only discovery/teaching facts that cannot be reconstructed. Distinguish app version, schema version and balance rules version. Bound market/power history and effect queues.

## Lifecycle and performance

Foreground economy delta is monotonic; visual FPS is independent. Coalesce passive saves and flush critical actions/background state. Stop visual loops when backgrounded; calculate offline rewards once on resume using defensive wall time and actual event expiry. Preserve fractional playtime/energy rather than truncating every small tick. Sound resources are released correctly.

## Dependencies and privacy

No network stack/backend/analytics/ads/auth/wallet. No unnecessary permissions; Android backup remains disabled. Keep stable existing libraries unless evidence justifies a scoped change recorded in DECISIONS. Do not import web architecture/CSS/GSAP/React guidance into Compose. No Hilt/Koin requirement from a general testing reference.
