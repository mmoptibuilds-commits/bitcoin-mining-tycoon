# Changelog

## v1.2 — implementation in progress (unreleased)

This changelog separates implemented source from acceptance that still requires an Android emulator. A local minified v1.2 sideload APK exists, signed with the existing debug key; it is not a public or store release artifact.

### Implemented on the v1.2 branch

- Deterministic offline economy/content balance and reference policy: first prestige at 27m25s; Efficient Silicon follow-up at 8m05s in the fixed reference run.
- Save migration/recovery, old-schema fixtures, serialized transactions, source ledgers, fractional play time and bounded offline/event accounting.
- Facility-first Mine home, progressive Mine → Bitcoin → sell → cash → machine teaching, ten evolving facility scenes, bounded visual feedback and Android haptics.
- All 20 hardware tiers, 56 grouped ordinary upgrades, corrected content targets, complete statistics presentation, battery-friendly settings, About metadata and native reward/prestige flows.
- Original graphite/copper adaptive, round and themed launcher icon, Android 12 system splash, and package metadata `1.2.0` / versionCode `2`; the minified release package builds successfully.

The independent M7 audit found six software defects and the follow-up conservation review found a seventh: Auto-Sell threshold editing was unreachable; out-of-range saved market prices/history could bypass raw recovery checkpointing; rapid settings changes could overwrite one another; wall-clock rollback could extend foreground event bonuses; immediate writes could lower the logical save boundary; Auto-Sell skipped the existing sale multiplier; and sub-cent Auto-Sell payouts discarded mined Bitcoin. Fixes include a persisted schema-3 pending-BTC queue that pays once proceeds reach one cent, returns held BTC when disabled, and accounts for it during prestige. Market details also now has a 48dp minimum target with a Compose assertion. Schema-3 conservation fix commit `bd72fb7bc9afc9aec5c51b5d76be7179cb390d6c` is pushed to `v1.2` and passed a fresh independent read-only review with no actionable findings. Post-fix validation passed 187 JVM tests (38 suites) plus a focused 121-test release-risk matrix (17 suites); instrumentation source compilation, lintDebug/lintRelease, and debug/release assembly pass. Current saves use schema 3 / `balanceRulesVersion=2`, and valid schema-1/2 saves migrate while preserving prior fields. The minified APK SHA-256 is `337d43fc815ab1783e149cef053965b685728fd254007c6ca27d3a348511b9f5`. The local v1.0 release snapshot and v1.2 APK use the same debug certificate. The nine guided emulator journeys, Compose UI assertions, launcher/splash inspection and in-place save-retention update have not run because this workspace has no Android Studio, emulator image or connected ADB device. M7 acceptance remains partial.

### Remaining before release

- Reconcile version code 2 against external distribution history; the repository has no version tags, but GitHub release metadata returned `Forbidden` here.
- Execute Compose journeys, inspect launcher/App Info/recents/splash screenshots and accessibility/font scaling on API31/API36, and run the same-certificate in-place v1.0 data-retention update without uninstall or data clear.
- Complete the nine-journey F01–F25 release audit on the minified artifact. No public signing key or store release is configured.

## v1.0 — baseline

Baseline commit: `33787bc4fa61ea4f78de06b7b81dd4094371cf39`, archived on v1.0. Existing Kotlin/Compose local engine, mining hardware, market, infrastructure, upgrades, events, achievements, rewards, offline progress, prestige, stats/settings and procedural audio. VersionName 1.0 / versionCode 1; compile/target SDK 36. The presence of code/tests does not establish all runtime or visual requirements as verified.
