# v1.2 local APK release checklist

Scope: local emulator validation only. No public distribution or store publishing is authorized by this checklist.

## Current disposition — 2026-10-03

**Implementation complete; public release acceptance remains partial.** The 194-test JVM suite and 26-test API31 connected Compose suite pass. A minified local APK launches on API31, and a same-certificate v1.0→v1.2 `adb install -r` update preserved a seeded schema-1 save without uninstall or data clear. The API31 TCG emulator is usable; API36 did not finish framework startup without KVM. TalkBack, large text, tactile feel, launcher/App Info/recents and splash-transition observation remain. External release history could not be checked (`Forbidden`), and the local APK is debug-key signed. Do not distribute it publicly.

The package is `com.antigravity.bitcoinminingtycoon`, versionName `1.2.0`/versionCode `2`, current save schema `3`, and balance rules `2`. Final local release APK SHA-256: `376eb4241f878675a3972bd152e0e398b9e64fc866ccd37c6e1bfaa4453d1035`. Signer SHA-256: `e8c7f74bc7c2e017e55880394510c4c2df728e6cb8e6cd45bb4835a89eb84932`. This matches the locally rebuilt v1.0 release snapshot, not a separately distributed user install. See `CURRENT_STATE.md` and `TEST_STRATEGY.md` for command output and journey evidence.

## Product and data

- [x] Deterministic reference pacing and next-run acceleration: first prestige at 27m25s; Efficient Silicon follow-up at 8m05s.
- [x] Schema-1/2 migration, unsupported/corrupt-save recovery, checkpointing, transaction ordering, reward-claim concurrency, offline boundaries, event expiry and prestige bookkeeping have JVM regression coverage.
- [x] Seeded schema-1 profile updated locally from v1.0 release to v1.2 release in place. Balances, miners, infrastructure, prestige points/nodes, onboarding and settings survived; no uninstall or data clear occurred.
- [x] A 70-second force-stop/relaunch restored the saved offline report, average hashrate and credited BTC on the same API31 profile.
- [ ] Complete a manual collect/relaunch/duplicate-claim lifecycle on the migrated profile; broad release journey acceptance remains partial.
- [ ] Verify the actual certificate/version history of any externally installed user copy. GitHub release lookup returned `Forbidden`.

## Native experience

- [x] API31 Compose coverage exercises the first Mine→Bitcoin→sell→cash→machine loop, facility navigation, all 20 hardware tiers, upgrade groups, power/cooling remedies, stats, settings, reward/event/prestige sheets and recovery UI.
- [x] Facility-first home, ten stage scenes, progressive disclosure, bounded feedback, and native haptics/settings are implemented; rapid-tap and all-scene Compose checks pass on API31.
- [x] Adaptive/round/themed icon resources, density fallbacks, API31 system-splash theme, package identity and About metadata are implemented and packaged.
- [ ] Inspect launcher masks, App Info, recents and cold/warm splash on a booted API36 target. Android Studio/Device Manager is absent; the API36 TCG framework did not boot.
- [ ] Observe TalkBack, 1.5 font scale, compact/tall layout, native insets/back, and physical vibration feel. These are not proven by source assertions or software TCG.

## Tests and artifact

- [x] `testDebugUnitTest --rerun-tasks`: 194 tests / 38 XML suites, 0 failures, errors or skips.
- [x] `connectedDebugAndroidTest`: 26 API31 tests, 0 failures, errors or skips (`emulator-5554`, x86_64 software TCG).
- [x] `lintDebug`, `lintRelease`, `assembleDebug`, `assembleRelease`, and `compileDebugAndroidTestKotlin` passed. Each lint task reports 12 warnings and 0 errors.
- [x] Minified release APK installs and launches on API31. Package manifest has no INTERNET permission; `allowBackup=false`; storage/package identity is preserved.
- [x] Signed same-key schema-1 update and schema-3 save inspection passed without uninstall/data clear. The 70-second offline return summary showed 188.376 seconds, 14,683,500 H/s average, and 0.1383009498 BTC.
- [ ] API36 app install/launch, full manual process-death collection, accessibility and launcher/splash observations remain.
- [ ] Resolve external distribution version/signing history and configure the production signing identity before any public release.

Exact final validation commands, device setup, fixture staging and journey disposition are in `TEST_STRATEGY.md`. The API31 AVD ran through the supported emulator/ADB CLI fallback because this Linux workspace has no Android Studio/Device Manager or `/dev/kvm`. Physical USB is not required. No public tag was created. Google Play assets, AAB and publishing are separate future work. Source rollback is not a guaranteed save downgrade; follow `SAVE_COMPATIBILITY.md`.
