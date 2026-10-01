# Changelog

## v1.2 — implementation in progress (unreleased)

This changelog separates implemented source from acceptance that still requires an Android emulator. A local minified v1.2 sideload APK exists, signed with the existing debug key; it is not a public or store release artifact.

### Implemented on the v1.2 branch

- Deterministic offline economy/content balance and reference policy: first prestige at 27m25s; Efficient Silicon follow-up at 8m05s in the fixed reference run.
- Save migration/recovery, old-schema fixtures, serialized transactions, source ledgers, fractional play time and bounded offline/event accounting.
- Facility-first Mine home, progressive Mine → Bitcoin → sell → cash → machine teaching, ten evolving facility scenes, bounded visual feedback and Android haptics.
- All 20 hardware tiers, 56 grouped ordinary upgrades, corrected content targets, complete statistics presentation, battery-friendly settings, About metadata and native reward/prestige flows.
- Original graphite/copper adaptive, round and themed launcher icon, Android 12 system splash, and package metadata `1.2.0` / versionCode `2`; the minified release package builds successfully.

Through M6, 173 JVM tests pass, Android instrumentation sources compile, debug/release lint passes with warnings, and minified debug/release APKs build. The local v1.0 release snapshot and v1.2 APK use the same debug certificate. Compose UI assertions, launcher/splash inspection and in-place save-retention update have not run because the current workspace has no Android Studio, emulator image or connected ADB device.

### Remaining before release

- Reconcile version code 2 against external distribution history; the repository has no version tags, but GitHub release metadata returned `Forbidden` here.
- Execute Compose journeys, inspect launcher/App Info/recents/splash screenshots and accessibility/font scaling on API31/API36, and run the same-certificate in-place v1.0 data-retention update without uninstall or data clear.
- Complete the nine-journey F01–F25 release audit on the minified artifact. No public signing key or store release is configured.

## v1.0 — baseline

Baseline commit: `33787bc4fa61ea4f78de06b7b81dd4094371cf39`, archived on v1.0. Existing Kotlin/Compose local engine, mining hardware, market, infrastructure, upgrades, events, achievements, rewards, offline progress, prestige, stats/settings and procedural audio. VersionName 1.0 / versionCode 1; compile/target SDK 36. The presence of code/tests does not establish all runtime or visual requirements as verified.
