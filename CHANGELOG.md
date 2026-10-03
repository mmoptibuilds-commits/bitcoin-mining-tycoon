# Changelog

## v1.2 — implementation complete; acceptance partial (unreleased)

This is the implementation record for the preserved `v1.2` branch. The local minified sideload APK uses the existing debug certificate and is not a public or store release artifact.

### Implemented

- Deterministic offline economy, content balance and reference pacing: first prestige at 27m25s; Efficient Silicon follow-up at 8m05s in the fixed reference run.
- Schema-1/2 migration and recovery, serialized transactions, source ledgers, fractional play time, and bounded offline/event accounting.
- Facility-first Mine home and progressive Mine → Bitcoin → sell → cash → machine teaching; ten evolving scenes; bounded visual feedback and native Android haptics.
- All 20 hardware tiers, 56 grouped ordinary upgrades, corrected content targets, complete statistics presentation, settings, About metadata, and native reward/prestige flows.
- Custom adaptive, round and themed launcher icons, density fallbacks, system splash and package metadata `1.2.0` / versionCode `2`.

### Validation and remaining acceptance

The final JVM run passed 194 tests in 38 suites. The API31 connected Compose suite passed 26 tests on the x86_64 software TCG AVD. Lint, instrumentation Kotlin compilation, debug/release assembly, and minified release launch passed. A seeded schema-1 save was updated from the local v1.0 release to v1.2 with `adb install -r`; balances, machines, infrastructure, prestige, onboarding and settings remained, without uninstall or data clear. A force-stop/relaunch restored the offline report and persisted average hashrate. The final release APK SHA-256 is `376eb4241f878675a3972bd152e0e398b9e64fc866ccd37c6e1bfaa4453d1035`.

The M7 audit found seven defects and regression coverage for their fixes: inaccessible Auto-Sell threshold editing; out-of-range market values bypassing raw recovery checkpointing; rapid settings writes reverting earlier changes; wall-clock rollback extending bonuses; immediate writes lowering the logical save boundary; Auto-Sell missing the manual-sale multiplier; and sub-cent Auto-Sell proceeds discarding BTC. Schema 3 now queues sub-cent BTC safely, pays once proceeds reach one cent, returns held BTC when Auto-Sell is disabled, and includes it in prestige loss preview. It also accepts the current writer's explicit `pendingOfflineSummary: null` and preserves time-weighted offline hashrate in the return report.

Release acceptance remains partial. API36 framework startup did not finish under TCG without `/dev/kvm`; TalkBack, large font, tactile feel, launcher/App Info/recents and splash transition still need device inspection. External version/signing history is unknown because GitHub release lookup returned `Forbidden`. No tag was created. See `docs/CURRENT_STATE.md`, `docs/TEST_STRATEGY.md` and `docs/RELEASE_CHECKLIST.md` for exact commands, evidence and remaining checks.

## v1.0 — baseline

Baseline commit: `33787bc4fa61ea4f78de06b7b81dd4094371cf39`, archived on v1.0. Existing Kotlin/Compose local engine, mining hardware, market, infrastructure, upgrades, events, achievements, rewards, offline progress, prestige, stats/settings and procedural audio. VersionName 1.0 / versionCode 1; compile/target SDK 36. The presence of code/tests does not establish all runtime or visual requirements as verified.
