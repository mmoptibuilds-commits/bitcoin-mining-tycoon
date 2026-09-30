# v1.2 local APK release checklist

Scope: emulator-verified sideload/test APK. No Play Store publishing requirement. Documentation preparation is not a release candidate.

## Product and data

- [ ] F01–F25 acceptance matrix satisfied with actual evidence.
- [ ] Reference pacing/seeded variance/two-run acceleration verified.
- [ ] No known release-blocking crash, data loss, incorrect economy or inaccessible core action.
- [ ] Valid v1 saves preserved; unsupported/corrupt raw data retained; no migration reset workaround.
- [ ] Signed v1→v1.2 in-place emulator upgrade uses same certificate and retains asset/settings snapshots.
- [ ] Critical transactions/repeated claims/prestige/process death verified.

## Native experience

- [ ] First-time and migrated flows, all hardware/upgrade/system journeys.
- [ ] Facility stages and purchase/prestige/reset transitions verified.
- [ ] Compact/tall font1.0/1.5, TalkBack, 48dp targets, reduced motion, back/insets.
- [ ] Real haptics API/settings/capability handling; no claim of physical tactile testing.
- [ ] Custom adaptive/round/themed icon and all applicable fallback assets; no Android robot.
- [ ] Coherent system splash, launcher/app drawer/App Info/recents labels, cold/warm launch with no duplicate splash/white flash.
- [ ] Settings/About actual version/build/changelog, accurate simulation copy, reset/cancel usable.

## Tests and release artifact

- [ ] Full JVM/migration/content/simulation suites, relevant Compose instrumentation and lintDebug/lintRelease green.
- [ ] All nine journeys on API31 and API36 configured target class (or new target if changed) with evidence; unsupported check documented as blocker.
- [ ] Minification/resource shrinking enabled; actual minified APK installed/launched and critical release flows rerun.
- [ ] Merged manifest has no INTERNET/unnecessary permissions; backup disabled, no debug menus/logged save contents/secrets.
- [ ] Release serializers/resources/icon layers survive R8; no template/stub settings.
- [ ] Rapid tapping/large scenes/background/lifecycle checked; profiler evidence for unresolved jank/memory concern.
- [ ] VersionName1.2.0; versionCode greater than old distributed candidates; unchanged package/storage/signing identity.
- [ ] Record commit, schema/balance rules version, toolchain/API/emulator, APK SHA-256 and public certificate fingerprint.

## Audit and version history

- [ ] Scoped commits pushed to v1.2, main/v1.0/all historical refs preserved; no force push.
- [ ] P7 read-only audit records the exact stable commit; confirmed findings are fixed with rerun evidence. Use an independent reviewer when supported; optional Antigravity verification is recorded separately and is not required for completion.
- [ ] CURRENT_STATE and CHANGELOG describe delivered work/remaining constraints accurately.
- [ ] Tag only the accepted verified release commit when release-tag scope is authorized; do not tag this docs preparation.
- [ ] Future version branches begin from accepted release commit and retain earlier versions.

Physical vibration feel, phone launcher variations and real-phone performance remain unverified under the chosen emulator-only setup. If the original installed APK certificate is unavailable, the user's real update compatibility remains unverified even when same-key emulator proof passes. Do not call those checks complete.

Google Play assets, store policy, AAB and publishing are a separate future project. Source rollback is not a guaranteed save downgrade; follow SAVE_COMPATIBILITY.
