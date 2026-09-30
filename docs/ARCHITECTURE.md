# Android Architecture

## Stack

- Kotlin.
- Single `app` module.
- Jetpack Compose + stable Compose BOM.
- Material 3 primitives with custom tokens/components.
- Navigation 3 stable is permitted if navigation complexity warrants it; for four persistent root tabs prefer the simplest stable state/back-stack implementation that preserves tab state and predictive-back correctness.
- Lifecycle/ViewModel + StateFlow.
- Coroutines.
- DataStore for atomic local persistence.
- Kotlin serialization for versioned save payload.
- JUnit + Compose testing + UI Automator/Journeys for higher-level flows.

## Suggested packages

```text
com.<owner>.bitcoinminingtycoon
├── MainActivity.kt
├── app/
│   ├── App.kt
│   └── AppState.kt
├── engine/
│   ├── GameEngine.kt
│   ├── EconomyEngine.kt
│   ├── MarketEngine.kt
│   ├── PowerEngine.kt
│   ├── ThermalEngine.kt
│   ├── PrestigeEngine.kt
│   ├── EventEngine.kt
│   ├── AchievementEngine.kt
│   └── OfflineEngine.kt
├── model/
├── content/
│   ├── Miners.kt
│   ├── Upgrades.kt
│   ├── Achievements.kt
│   ├── Events.kt
│   └── PrestigeNodes.kt
├── data/
│   ├── GameRepository.kt
│   ├── SaveDataSource.kt
│   ├── GameSave.kt
│   └── migrations/
├── ui/
│   ├── navigation/
│   ├── screens/
│   ├── components/
│   ├── theme/
│   └── previews/
├── viewmodel/
├── platform/
│   ├── ClockProvider.kt
│   ├── Haptics.kt
│   └── SoundPlayer.kt
└── util/
    ├── GameNumber.kt
    ├── NumberFormatter.kt
    └── ResultExt.kt
```

## State ownership

One immutable `GameState` is the gameplay source of truth. UI observes a presentation state derived by the ViewModel. User intents are sent to the ViewModel/repository/engine; composables do not mutate persistent state directly.

## Game loop

A coroutine ticker emits delta time while foregrounded. The engine applies deterministic `tick(state, delta, clock, rng)` logic and returns new state. Coalesce persistence so a 10Hz ticker does not write storage 10 times/sec.

Critical transactions (purchase, sell, prestige, reward claim, settings change) schedule/perform an immediate persisted save.

## Clock abstraction

- `monotonicNow()` for foreground deltas.
- `wallNow()` only for offline/daily calculations.
- tests inject fake clock.

## Persistence

Store a versioned serialized payload in DataStore. Include:

- `schemaVersion`;
- balances;
- hardware ownership;
- upgrades;
- achievements;
- market/RNG state;
- prestige;
- stats;
- settings;
- timestamps.

Migrations are pure transformations from old payload → current payload. If an optional field is absent, default safely. If the payload is unrecoverably corrupted, preserve a diagnostic marker and fall back to a safe new state rather than crash-looping.

## No backend

No Retrofit/OkHttp/Firebase/Supabase. No network permission.

## Dependency rule

Prefer AndroidX/Kotlin standard libraries. Every third-party dependency must justify itself in `docs/DECISIONS.md` and have an active maintenance/license check.
