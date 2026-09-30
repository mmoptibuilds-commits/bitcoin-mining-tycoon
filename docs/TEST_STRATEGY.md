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
