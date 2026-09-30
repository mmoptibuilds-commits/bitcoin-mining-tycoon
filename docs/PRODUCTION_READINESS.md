# Production Readiness Gates

All boxes in the **current local-release scope** must be satisfied before calling the app production-ready for sideloading/testing. Play Store publishing is intentionally out of scope for now.

## Functional

- [ ] All V1 features implemented.
- [ ] No P0/P1 known defects.
- [ ] No known save-loss path.
- [ ] No negative/NaN/Infinity economy state.
- [ ] Prestige and reset behavior verified.

## Tests

- [ ] Unit suite green.
- [ ] Compose UI suite green.
- [ ] Core Journeys green.
- [ ] API 31/32 compatibility smoke test green.
- [ ] Current Android device/API smoke test green.
- [ ] Release build smoke test green.
- [ ] Feature test matrix satisfied.

## UX/accessibility

- [ ] All primary flows reviewed on device.
- [ ] Font 1.5 pass.
- [ ] TalkBack core-flow pass.
- [ ] 48dp targets.
- [ ] Reduced Motion pass.
- [ ] No clipped content/system bar overlap.
- [ ] No AI-slop banned patterns.

## Performance

- [ ] Rapid tapping remains responsive.
- [ ] Idle tick does not cause excessive recomposition.
- [ ] No runaway background work.
- [ ] No continuous storage writes at tick frequency.
- [ ] Release R8/resource shrink enabled.
- [ ] Inspect profiler/trace if jank is observed; do not guess.

## Privacy and distribution

- [ ] No INTERNET permission.
- [ ] No analytics/ads/trackers.
- [ ] No real crypto/wallet/trading behavior.
- [ ] Simulation disclaimer present.

## Build/release

- [ ] Stable dependencies only.
- [ ] Lint green or documented intentional suppressions.
- [ ] Minified release APK builds successfully.
- [ ] Release APK installation path is verified using a local signing approach when required; signing secrets are not committed.
- [ ] Version code/name set.
- [ ] App icon/splash original and vector-based.
- [ ] No secrets/debug endpoints/test menus in release.
