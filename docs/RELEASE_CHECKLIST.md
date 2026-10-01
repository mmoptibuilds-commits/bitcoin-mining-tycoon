# v1.2 local APK release checklist

Scope: emulator-verified sideload/test APK. No Play Store publishing requirement. Documentation preparation is not a release candidate.

## Current disposition — 2026-10-01

**Not release-ready.** The local minified v1.2 APK is built and statically inspected, but it has not been installed or launched on an emulator. M7 reran 173 JVM tests; a focused 99-test release-risk set also passed; lintDebug/lintRelease and debug/release assembly passed. Android instrumentation sources compile, while `connectedDebugAndroidTest` fails at device selection with `No connected devices!`. There are no API31/API36 AVDs, Android Studio/Device Manager, emulator executable or `/dev/kvm`. No screenshot, TalkBack/font-scale observation, process-death flow or signed in-place save update was performed.

The package is `com.antigravity.bitcoinminingtycoon` versionName `1.2.0`/versionCode `2`, save schema `2`, balance rules `1`. The local release APK SHA-256 after M7 assembly is `d005bf3a34c85b5a1f79d4e98ac9ca84b937f37783a23e317624debc52905380`. Its debug-key certificate fingerprint matches a minified APK built from the preserved `origin/v1.0` source snapshot; this does not establish compatibility with a separately distributed user install. GitHub release-history lookup returned `Forbidden`, so an external higher version code remains unverified. See `CURRENT_STATE.md` and `TEST_STRATEGY.md` for command logs and journey-by-journey disposition.

## Product and data

- [ ] F01–F25 acceptance matrix satisfied with actual evidence.
- [x] Reference pacing/seeded variance/two-run acceleration verified by deterministic simulation: first prestige 27m25s and Efficient Silicon next run 8m05s.
- [ ] No known release-blocking crash, data loss, incorrect economy or inaccessible core action.
- [x] v1 serializer fixtures, supported migration fields, corrupt checkpoint and unsupported-schema read-only behavior pass JVM tests.
- [ ] Actual signed v1→v1.2 in-place emulator update preserves assets/settings/claim history; no `adb install -r` profile was run.
- [x] Critical transaction ordering, concurrent reward claims, prestige preview/apply and reset accounting have JVM coverage.
- [ ] Force-stop/relaunch process-death journey and UI duplicate-claim/prestige cancellation are not device-verified.

## Native experience

- [ ] First-time and migrated flows, all hardware/upgrade/system journeys.
- [ ] Facility stages and purchase/prestige/reset transitions verified.
- [ ] Compact/tall font1.0/1.5, TalkBack, 48dp targets, reduced motion, back/insets.
- [ ] Real haptics API/settings/capability handling; no claim of physical tactile testing.
- [x] Custom adaptive/round/themed icon and density fallbacks are implemented; no template robot remains in referenced resources.
- [ ] Emulator confirms launcher/app drawer/themed and round masks, App Info and recents identity.
- [x] API31 system-splash theme, icon and graphite window background are configured without another Activity or delay.
- [ ] Cold/warm launch proves there is no duplicate splash or white flash; unavailable without emulator.
- [x] About sources actual version/build metadata from PackageManager; accuracy/reset/cancel have source tests.
- [ ] Emulator confirms Settings/About behavior, font scaling, TalkBack and reset usability.

## Tests and release artifact

- [x] Full JVM/migration/content/simulation suite passed (173 tests, 0 failures/errors/skips); lintDebug/lintRelease pass with 12 warnings each and 0 errors.
- [x] Relevant Compose instrumentation sources compile, including identity, settings, hardware, statistics, reward/prestige and facility coverage.
- [ ] Connected Compose instrumentation is not executed: `connectedDebugAndroidTest` reports `DeviceException: No connected devices!`.
- [ ] All nine journeys on API31 and API36 configured target class (or new target if changed) with evidence; unsupported check documented as blocker.
- [x] Minification/resource shrinking enabled; minified release APK builds and R8 mapping/resources are present.
- [ ] Install/launch the minified APK on API31/API36 and rerun release flows; no device is available.
- [x] Merged manifest has no INTERNET permission, `allowBackup=false`, no debug menu or test helpers in the release APK; only VIBRATE plus the app-scoped receiver permission is present.
- [ ] Release serializer/data retention and launcher/splash behavior have not been smoke-tested after R8 on a device.
- [ ] Rapid tapping/large scenes/background/lifecycle checked; profiler evidence for unresolved jank/memory concern.
- [ ] VersionName `1.2.0`/code `2` is packaged and exceeds the tracked v1.0 code `1`; reconcile with external distribution history before release because GitHub release lookup is forbidden here.
- [x] Application/storage identity is unchanged; APK SHA-256, schema `2`, balance rules `1`, toolchain and public certificate fingerprint are recorded.
- [ ] Record target emulator/API and install/update results; no emulator/API runtime evidence exists.

## Audit and version history

- [ ] Scoped M1–M7 commits pushed normally to v1.2; main/v1.0 and historical refs remain unchanged; no force push.
- [ ] M7 read-only audit records the exact reviewed commit; confirmed findings are fixed with rerun evidence. Use an independent reviewer when supported; optional Antigravity verification is recorded separately and is not required for completion.
- [x] CURRENT_STATE, TEST_STRATEGY, CHANGELOG and this checklist record delivered work and remaining constraints accurately.
- [ ] Tag only the accepted verified release commit when release-tag scope is authorized; do not tag this docs preparation.
- [ ] Future version branches begin from accepted release commit and retain earlier versions.

Physical vibration feel, phone launcher variations and real-phone performance remain unverified under the chosen emulator-only setup. If the original installed APK certificate is unavailable, the user's real update compatibility remains unverified even when same-key emulator proof passes. Do not call those checks complete.

Google Play assets, store policy, AAB and publishing are a separate future project. Source rollback is not a guaranteed save downgrade; follow SAVE_COMPATIBILITY.
