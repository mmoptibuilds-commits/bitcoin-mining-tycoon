# Changelog

## v1.2 — implementation in progress (unreleased)

This changelog separates implemented source from acceptance that still requires an Android emulator. No v1.2 release APK has been produced.

### Implemented on the v1.2 branch

- Deterministic offline economy/content balance and reference policy: first prestige at 27m25s; Efficient Silicon follow-up at 8m05s in the fixed reference run.
- Save migration/recovery, old-schema fixtures, serialized transactions, source ledgers, fractional play time and bounded offline/event accounting.
- Facility-first Mine home, progressive Mine → Bitcoin → sell → cash → machine teaching, ten evolving facility scenes, bounded visual feedback and Android haptics.
- All 20 hardware tiers, 56 grouped ordinary upgrades, corrected content targets, complete statistics presentation, battery-friendly settings, About metadata and native reward/prestige flows.

JVM tests, Android instrumentation source compilation, lint and debug packaging pass through milestone M5. Compose UI assertions have not run because the current workspace has no Android Studio, emulator image or connected ADB device.

### Remaining before release

- Original adaptive/round/themed launcher icon, Android system splash, App Info/recents identity surfaces and version 1.2.0/code update.
- Execute Compose journeys, inspect screenshots/accessibility/font scaling on API31/API36, and run the signed in-place v1.0 data-retention update without uninstall or data clear.
- Build and inspect the minified release APK, verify signing identity and complete the F01–F25 release audit. The changelog and verification status must be revised again after those milestones.

## v1.0 — baseline

Baseline commit: `33787bc4fa61ea4f78de06b7b81dd4094371cf39`, archived on v1.0. Existing Kotlin/Compose local engine, mining hardware, market, infrastructure, upgrades, events, achievements, rewards, offline progress, prestige, stats/settings and procedural audio. VersionName 1.0 / versionCode 1; compile/target SDK 36. The presence of code/tests does not establish all runtime or visual requirements as verified.
