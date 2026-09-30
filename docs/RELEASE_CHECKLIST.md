# Release Checklist

1. Freeze features.
2. Run `/release-audit` skill.
3. Run full JVM + UI/instrumented tests.
4. Run all critical Journeys.
5. Run lint and Android Studio analysis on changed production files.
6. Build the minified release APK. An AAB is not required for the current scope.
7. Install release candidate on a physical Android 16/17 device if available.
8. Smoke: first launch → mine → sell → buy → upgrade → event → background/return → prestige preview.
9. Kill process, relaunch, verify state.
10. Test a fixture save from previous schema version if one exists.
11. Verify manifest contains no unnecessary dangerous/network permissions.
12. Check app label, icon, splash, version, simulation disclaimer and reset flow.
13. Verify no unnecessary permissions, secrets, debug endpoints, or test-only menus are present in the release build.
14. Archive test reports/screenshots and final commit hash/tag if Git is configured.

> Play Store publishing is intentionally not a release gate. If that becomes a goal later, use `OPTIONAL_PLAY_STORE_FUTURE.md` as a separate publishing checklist.
