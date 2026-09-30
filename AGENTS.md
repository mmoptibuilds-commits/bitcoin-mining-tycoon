# AGENTS.md — Bitcoin Mining Tycoon

This file is always-on project guidance for Antigravity agents.

## Mission

Build and maintain a production-ready native Android game named **Bitcoin Mining Tycoon** according to the documents in `docs/`. Treat those documents as requirements, not inspiration.

## Required stack

- Kotlin.
- Jetpack Compose with the current stable Compose BOM.
- Material 3 as primitives, with a bespoke project design system.
- Single Android application module unless a second module becomes objectively necessary for release verification.
- MVVM/UDF-style state ownership with StateFlow.
- Coroutines.
- Versioned local persistence using DataStore + Kotlin serialization.
- Stable dependencies only unless a requirement is impossible without a pre-release artifact and the user explicitly approves it.
- No DI framework by default. Use constructor injection/manual fakes; do not add Hilt/Koin solely because a generic testing skill recommends DI.
- minSdk 31; compile/target the newest stable SDK supported by the stable build toolchain, currently expected to be API 37.

## Architectural invariants

1. UI composables never contain economy formulas, market simulation, prestige formulas, save migration logic, or wall-clock calculations.
2. Game calculations are deterministic when supplied the same state, clock, and RNG seed.
3. Monetary/game magnitude arithmetic must never emit NaN or Infinity. Use `BigDecimal`/bounded numeric helpers for economy values and explicit formatting helpers.
4. Runtime progression uses a monotonic clock. Offline progression may use wall clock only through a defensive abstraction that clamps negative time to zero and positive time to 12 hours.
5. The app must survive process death without losing previously committed purchases/progression.
6. Every persistent schema change increments a save version and has a migration or safe default.
7. No network access is required. Do not add `INTERNET` permission.
8. Do not add analytics, ads, billing, authentication, cryptocurrency wallet/trading/mining libraries, or remote configuration.
9. No generated AI artwork or stock “crypto” imagery. Use Compose primitives, Canvas, VectorDrawable, typography, iconography, and original geometric motifs.
10. Keep one clear source of truth for every gameplay constant. No magic balance numbers duplicated across UI files.

## Feature completion gate — mandatory after EVERY feature

Never start the next feature until the current feature passes all applicable checks:

1. Write/extend unit tests before or alongside the implementation.
2. Run targeted unit tests for the affected engine/repository/ViewModel code.
3. Run static/IDE analysis for changed Kotlin files when Android Studio integration is available.
4. Run Compose behavior tests for user-visible interactions.
5. Render or inspect Compose preview for changed screens/components and inspect semantics.
6. Build and deploy to an emulator/device.
7. Run the relevant Journey(s) or equivalent on-device flow.
8. Inspect screenshot/screen output and layout tree for clipping, overlap, incorrect touch targets, broken hierarchy, stale data, inaccessible controls, and visual slop.
9. Exercise at least one failure/boundary case for the feature.
10. Re-run the existing regression suite.
11. Record the feature as complete only when all failures are fixed.

Apply the `feature-cycle` skill for each feature.

## UX quality gate

Do not approve a UI simply because it compiles. Verify:

- information hierarchy;
- tap target sizing;
- one-handed portrait usability;
- clear labels and feedback;
- loading/disabled/empty/error states where applicable;
- scroll behavior and bottom-navigation interaction;
- system insets and edge-to-edge behavior;
- text scale 1.0 and 1.5;
- TalkBack semantics for interactive controls;
- no information conveyed by color alone;
- reduced-motion behavior;
- back handling/predictive back where relevant;
- no accidental double-purchase from rapid taps.

## Anti-AI-slop rule

Read `.agents/rules/ui-no-ai-slop.md` before any visual work. Never use generic AI-generated dashboard aesthetics: purple/blue gradients, glassmorphism everywhere, huge rounded cards, decorative glowing blobs, random sparkles, 3D floating crypto coins, fake charts with meaningless data, generic hero layouts, excessive shadows, emoji-as-interface, or generated illustrations.

## Testing discipline

- Tests are product requirements, not cleanup.
- Fix code rather than weakening assertions.
- A flaky test is a bug.
- Never delete a valid failing test merely to make CI green.
- Run release-path tests before declaring done.
- On any crash, capture logs, reproduce, add a regression test where feasible, fix, and rerun the relevant suite.

## Git discipline

- Keep commits scoped to a feature/fix when Git is configured.
- Never rewrite user work without inspecting it first.
- Do not run destructive Git commands unless explicitly requested.
- Do not make unrelated refactors during a bug fix.

## Completion definition

Do not say the project is complete until the **current local-release scope** in `docs/RELEASE_CHECKLIST.md` and `docs/PRODUCTION_READINESS.md` is satisfied and the full quality gate succeeds. Google Play publishing work is optional future scope.
