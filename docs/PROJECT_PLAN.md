# Bitcoin Mining Tycoon v1.2 implementation plan

> For agentic workers: execute the approved plan milestone by milestone using the project feature-cycle skill. One writer; use a read-only independent reviewer for material risks where supported. This is an implementation roadmap to refine against the live checkout, not permission to recreate existing systems.

**Goal:** deliver the full F01–F25 game-experience redesign and emulator-verified minified APK on v1.2, preserving existing saves and version history.

**Architecture:** retain current deterministic engines, immutable state, decimal helpers, DataStore and ViewModel/Compose UI. Add centralized balance, pure presentation/discovery/facility helpers, bounded scene rendering, haptics and migration support at the existing boundaries.

**Stack:** Kotlin, Compose/Material 3, Navigation 3, StateFlow/coroutines, Kotlin serialization/DataStore, JDK 17, Gradle, Android Studio/ADB.

**Spec:** PRD + FEATURES; GAME_DESIGN/ECONOMY_BALANCE and DESIGN_SYSTEM/UX_SPEC/APP_IDENTITY define behavior. SAVE_COMPATIBILITY governs data. Current implementation status is CURRENT_STATE.

## Global constraints

minSdk 31; current compile/target 36 until justified; stable dependencies; portrait; applicationId `com.antigravity.bitcoinminingtycoon`; save preservation/signing continuity; no network/backend/real crypto; 12-hour offline cap; original Canvas/vector; graphite/copper; no phone USB requirement; branches preserved; work/push v1.2; no main merge/release tag before release evidence.

## Review focus

1. A real early/late old save must retain assets, onboarding and settings after update; unknown fields/version must not become a wipe.
2. Fast taps, purchase, reward claim and foreground tick/flush must not race or duplicate/lost-commit data.
3. Subsecond ticks, event expiry across offline gaps and clock extremes must conserve production/time without duplicate rewards.
4. Low-tap/poor-market play and legacy high balances must retain a useful reachable next action; base progression cannot depend on luck.
5. Compact/1.5-font/reduced-motion/migrated screens and launcher/splash must remain usable, with correct signing/install identity.

## M0 — Environment and baseline (F18/F21/F24/F25)

**Files:** inspect gradle/libs.versions.toml, app/build.gradle.kts, AndroidManifest.xml, CURRENT_STATE, VERSION_CONTROL, SETUP_WINDOWS; update only documented environment fixes/evidence. Create ignored artifacts for reports.

- [ ] Fetch/switch v1.2 and inspect dirty state/history. Preserve user edits and v1.0/main; record starting SHA.
- [ ] Verify JDK, wrapper, SDK36 and Android Studio emulator virtualization. Use one emulator at a time. Discover available task/CLI syntax rather than inventing commands.
- [ ] Run existing JVM tests, lintDebug and assembleDebug; launch baseline emulator and inspect actual UI, launcher and save flows.
- [ ] Build same-key baseline APK/save fixtures and record signing certificate fingerprint. Do not uninstall an existing user app to solve signing mismatch.
- [ ] Record reproducible C01–C08 findings as confirmed runtime/test failures or source-only concerns, with reproduction and baseline reports.

**Done:** working documented test surface, baseline evidence and safe old-save fixtures. Missing tooling is an explicit blocker, not a reason to invent a green baseline.

## M1 — Save safety and correctness (F03/F05/F07/F12–F20/F24)

**Modify:** model/GameState.kt, data/GameSave.kt, data/migrations/SaveMigrations.kt, SaveDataSource.kt, GameRepository.kt, viewmodel/GameViewModel.kt, relevant engines/content. **Create:** anonymized v1 JSON fixtures under app/src/test/resources/saves/v1/, ContentIntegrityTest.kt and targeted regression tests in existing data/engine/ViewModel test packages.

**Interfaces:** preserve SaveMigrations.migrate, GameSave conversions and SaveDataSource contract unless safer result/recovery metadata is necessary; record any signature change and update every consumer. Actions/ticks share one serial mutation path; save commit completes before durable-success feedback.

- [ ] Reproduce target mismatch, tap-default inconsistency, fractional playtime, repeated offline/daily claim, event expiry and critical transaction races with meaningful tests.
- [ ] Fix bad upgrade target without renaming saved IDs. Validate unique IDs, all targets/prerequisites, DAG cycles, positive prices, event modifiers and stage references.
- [ ] Implement schema-1 → schema-2 migration/new-field defaults/recovery checkpoint as additions require, with preservation assertions for every field in SAVE_COMPATIBILITY.
- [ ] Resolve prestige cumulative award accounting/large magnitudes and automation reset; verify preview equals apply.
- [ ] Resolve time/RNG/event/flush correctness at actual owning layer; inspect cause before changing architecture.
- [ ] Run data/engine/ViewModel suites and emulator process-death/reward flows. Check signing-compatible update using representative fixtures when candidate migration exists.

**Done:** preserved data, deterministic valid state and verified known correctness fixes. Mark source-only concerns resolved or explicitly still open.

## M2 — Economy, content and pacing (F02–F16/F22)

**Create:** content/BalanceConfig.kt, test-side simulation harness/reference policies and pacing/content tests. **Modify:** Miners.kt, Upgrades.kt, DailyRewards.kt, Infrastructure.kt, PrestigeNodes.kt and relevant economy/purchase/power/thermal/market/prestige engines as needed.

**Interfaces:** production engines consume centralized config; simulator calls real engine transitions with clock/RNG. Do not maintain independent 'simulation formulas'. Retain saved content IDs and purchased effects.

- [ ] Implement reference and alternative policies from ECONOMY_BALANCE before tuning. Capture baseline times and source-production ratios.
- [ ] Tune taps/miners/costs/unlocks/infrastructure/ordinary upgrades/daily/prestige together. Correct fresh/load/reset/prestige baseline consistently through migration.
- [ ] Add 50–60 meaningful ordinary upgrades; grouped paths and explicit effects. Keep target 56 only if all effects are useful/tested.
- [ ] Add critical upgrade only after base pacing succeeds; deterministic 2%/2× effect. Cosmetic combo changes no economy.
- [ ] Run no-reward/no-luck first run, five published real-market/event seeds, low-tap, consecutive prestige and 12h offline/legacy strategies. Reference first prestige 25–35 min, passive dominance by minute 5, second run ≥25% faster.
- [ ] Test affordability/huge MAX/soft constraints/point grants/modifier application once and no dominant dead-end strategy. Commit config, content and tests with report summary.

**Done:** actual simulation evidence meets reference goals; variation and legacy behavior are documented. Keep visual/gamefeel evaluation for later emulator milestones.

## M3 — Home, navigation and teaching (F01/F04/F05/F19/F21)

**Modify:** ui/navigation/AppNavHost.kt, NavigationKeys.kt, screens/mine/MineScreen.kt, MarketCard.kt, EnvironmentalPanel.kt, onboarding/OnboardingDialog.kt, theme/AppColors.kt/AppTypography.kt and GameViewModel presentation. **Create:** focused discovery/next-goal presentation helpers and Compose first-session/navigation tests under androidTest.

- [ ] Sketch component hierarchy using the existing UI/screens before implementation; use current/zero/legacy/large-number fixtures.
- [ ] Replace equal four-tab dashboard hierarchy with Mine home, reachable Hardware/Upgrades and Stats/Settings utilities while retaining existing Navigation 3 behavior.
- [ ] Teach Mine → sale → first machine without crypto terms or a forced slide deck. Persist cues and infer migrated unlocks. First purchase is reachable before hardware ownership.
- [ ] Make early BTC deltas/sell proceeds readable; expand advanced market/infrastructure controls only when relevant; show deficits/remedies.
- [ ] Run clean-profile and migrated-user Compose/emulator journeys, back/list preservation, insets, compact/tall and font1.5 checks.

**Done:** novice can perform the complete bootstrap loop and returning user can use preserved systems. No blocker hidden by progressive disclosure.

## M4 — Facility and interaction (F02/F20/F21/F22)

**Create:** ui/facility/FacilityStage.kt, FacilityPresentation.kt, FacilityScene.kt (or equivalent focused files); platform/Haptics.kt; deterministic stage tests and scene previews/interaction tests. **Modify:** CoreMineButton.kt, SoundPlayer.kt only where needed, mine screen/ViewModel/settings presentation.

- [ ] Map all tiers to the ten staged scenes from GAME_DESIGN; derive current scene from owned assets and keep discovered milestone facts separately.
- [ ] Add capped density/details and recognisable original vector/Canvas scenes. Preview every stage using fake state; no full 3D/rendered imagery.
- [ ] Add press, number delta, bounded shockwave/particles, cosmetic combo, critical/milestone feedback and machine response. Do not let animation own currency/RNG logic.
- [ ] Implement actual haptic API, independent setting, rate limits/capability fallback and testable fake; keep procedural sound lifecycle safe.
- [ ] Add reduced-motion/battery-friendly options; pause hidden/backgrounded scene animation; production unchanged by modes/FPS.
- [ ] Verify stage change on purchase/prestige/migrated save; stress rapid taps and transitions, inspect allocation/frame traces if jank appears.

**Done:** visible progression and responsive feedback on emulator; no unbounded effects/recompositions/background loops. Physical tactile quality is explicitly unverified.

## M5 — Secondary screens and complete tracking (F06–F19/F21)

**Modify:** hardware/HardwareScreen.kt/HardwareCard.kt, upgrades/UpgradesScreen.kt, stats/StatsScreen.kt, settings/SettingsScreen.kt, prestige/PrestigeSheet.kt/SatoshiTreeScreen.kt, daily/offline sheets and UI components; model/stats/schema/ViewModel/engines for new tracking. **Create:** focused group/row/track components and meaningful Compose/persistence tests.

- [ ] Present hardware/bulk controls with clear owned/output/cost/locked state. Organize ordinary upgrades under four tracks; make infrastructure and permanent tree accessible.
- [ ] Finish usable event/windfall/achievement/daily/offline/prestige journeys with concise exact feedback and no obstructing overlays.
- [ ] Add source stats, fractional playtime, peak thermal and bounded energy/power history. Correct update owners and no double counting; label migrated coverage.
- [ ] Complete Settings/About/changelog/build display, battery/reduced motion, independent haptics/audio, number preference and destructive reset/cancel semantics.
- [ ] Run all core flows with early/late/migrated fixtures, huge values and font1.5/TalkBack; regression on saved settings and transaction outcomes.

**Done:** every existing system remains playable and F17–F19 are complete; no cosmetic setting or stat without real implementation.

## M6 — Android app identity and update packaging (F18/F23–F25)

**Modify:** res/drawable launcher layers, mipmap adaptive/round/fallback assets, values/themes.xml/strings.xml, AndroidManifest.xml, MainActivity.kt and app/build.gradle.kts as needed. **Create:** monochrome icon drawable/preview resources and emulator identity/update tests.

- [ ] Design original silicon/hash mark, consistent with graphite/copper and readable within adaptive masks. Replace all applicable default robot assets.
- [ ] Configure coherent Android system splash with no duplicate Activity, artificial delay or white flash. Inspect cold/warm/task launch.
- [ ] Verify launcher/app drawer, themed icon where supported, App Info/recents and app labels.
- [ ] Set versionName1.2.0 and higher versionCode; preserve package/storage/signing. Install old/candidate with same certificate using -r and verify old data.
- [ ] Inspect merged release manifest, icon resources, R8/serialization and absence of debug menus/unwanted permissions.

**Done:** F23 surfaces verified on emulator and signed update continuity documented. No release tag yet.

## M7 — Full verification and local APK (all features)

**Update:** TEST_STRATEGY acceptance matrix/evidence, CURRENT_STATE, CHANGELOG and release record. **Outputs:** ignored reports/screenshots/traces, installable minified APK with SHA-256 and public certificate fingerprint.

- [ ] Run full JVM, simulations, lintDebug/lintRelease, Compose instrumentation and all emulator journeys on API31 and current configured target class (API36 baseline), sequentially.
- [ ] Install/run minified candidate APK and execute release smoke, process death and in-place save upgrade. Unit-only journeys are insufficient.
- [ ] Audit all F01–F25, the five review-focus cases and release checklist; fix confirmed release blockers and rerun affected checks.
- [ ] Push scoped commits to v1.2; update ledger with exact SHA/commands/results and any unavailable checks. Keep main/v1.0 untouched.
- [ ] Run P7's read-only audit in Codex, using an independent reviewer when supported; resolve confirmed findings and rerun affected checks. An optional Antigravity P5/P6 second pass uses the same gates at a stable pushed SHA; transfer writing ownership only after Codex stops. Antigravity access is not required for completion.
- [ ] When all emulator/local-release gates actually pass, record accepted commit/version/artifact. Create version tag only under authorized release scope; keep old branches. Physical phone limitations remain explicit.

**Done:** requirements and emulator-local APK evidence agree; no known release-blocking crash, data loss, incorrect economy, inaccessible core action or default app artwork.

## Per-milestone record

Checked feature IDs, commits, test command/result, emulator/API, fixture/seed, report/screenshot locations, fixed findings, blocker and next action. Planning may refine implementation details after reading the live repo; it may not silently drop features, reset saves or change approved constraints.
