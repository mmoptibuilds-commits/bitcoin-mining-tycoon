# Bitcoin Mining Tycoon — shared agent instructions

## Mission and authority

Implement the approved v1.2 redesign on `v1.2`, preserving the working local engine and existing player saves. This is an existing Android project, not a scaffold/build-pack task. Read `docs/CURRENT_STATE.md`, `docs/PRD.md`, `docs/FEATURES.md`, `docs/PROJECT_PLAN.md` and the documents relevant to the current milestone.

Product rules live in the product docs; implementation order in PROJECT_PLAN; evidence in CURRENT_STATE. If a plan contradicts a requirement, satisfy the requirement and record the ruling in DECISIONS. Do not repeat already answered questions. The user approves the implementation plan once; then complete its authorized milestones without routine permission requests.

## Stack and invariants

- Kotlin + Jetpack Compose + Material 3 primitives; one app module, ViewModel/StateFlow, coroutines and versioned DataStore/Kotlin serialization.
- Keep applicationId/namespace `com.antigravity.bitcoinminingtycoon`, save filename/location and signing continuity. Follow SAVE_COMPATIBILITY before any state/content change.
- minSdk 31; current compile/target SDK 36. Keep the working toolchain unless a verified stable dependency/platform requirement justifies a focused change. Do not infer versions from old docs or upgrade everything automatically.
- Stable dependencies only. Manual constructor injection; no new DI framework, backend, web wrapper or module split without demonstrated need.
- UI contains no economy, prestige, migration or clock formulas. Use one centralized source of balance constants.
- Use bounded decimal helpers for compounding economy values; reject invalid inputs, negative balances, NaN/Infinity and unbounded MAX loops.
- Foreground production uses monotonic elapsed time, independent of animation FPS. Defensive offline wall time is clamped to 0–43,200 seconds. Persist RNG state and verify determinism across reload.
- Serialize economic transactions and await durable commits for purchases, sales, reward claims, prestige and settings. Do not claim persistence merely because the screen changed.
- Migrate valid old saves; never silently replace a valid save to solve a decode issue. Retain unsupported/corrupt payloads locally for recovery before overwriting. New statistics must label unavailable historical attribution.
- No INTERNET permission, network dependency, ads, analytics, real mining/trading, wallets, accounts, remote configuration or background mining service.

## Product and visual constraints

- Beginner-friendly language: Bitcoin, Cash, Mining speed, Power, Cooling. Technical units are supporting information.
- Mine is the player's facility/home; Hardware and Upgrades are secondary destinations; Stats/Settings are utilities.
- Preserve all existing systems and 20 hardware tiers; implement the facility stages, 50–60 meaningful upgrades, complete statistics, real haptics, feedback, progressive teaching, icon and launch polish in FEATURES.
- Read DESIGN_SYSTEM, UX_SPEC, APP_IDENTITY and `.agents/rules/ui-no-ai-slop.md` before visual changes.
- Original Compose Canvas/VectorDrawable imagery only. No image generation, stock crypto art, emoji controls, generic dashboard card walls or decorative glow. State-driven tap particles are allowed, bounded and disabled by reduced motion.
- Design zero/locked/unaffordable/purchased/overheated/offline/migrated/huge-value states. 48dp targets, font scale 1.0/1.5, TalkBack, safe insets, predictable back and independent sound/haptics controls.

## Workflow and verification

Use the project `feature-cycle` skill for each gameplay milestone. Relevant local skills: frontend-design, game-balance, product-copy, bug-hunt and release-audit. Ask the harness to load them by name/path; do not assume a skill is a portable slash command. Official Android tools are optional helpers; Gradle, Android Studio and ADB remain valid fallbacks.

Before moving to the next milestone:

1. Add meaningful logic/migration tests for changed behavior and reproduce the original defect where applicable.
2. Run targeted tests; fix the cause without weakening valid assertions.
3. Add/run Compose interaction tests for changed user flows.
4. Render/inspect screenshots and semantics for changed UI.
5. Build and run on an emulator; execute the relevant journey and a boundary/lifecycle case.
6. Run the affected regression suites, inspect actual output and record command/result/commit evidence.

Documentation-only maintenance uses link/consistency/diff validation and does not require an APK build. If emulator/tooling is unavailable, continue independent work, report the exact blocked checks and do not mark the affected milestone verified. Never fabricate test results.

One writer owns production changes at a time. Independent reviewers are read-only; use them for material save/economy/UI/release risks when supported, not as competing implementations. Model selection is made in the harness; the approved Codex choice is GPT-6 Luna Max. Do not silently substitute a model or install credentials.

## Git and completion

Follow VERSION_CONTROL. Preserve `main`, `v1.0` and all historical version branches. Work on `v1.2`; no force push, branch deletion, rebase of published history or merge into main. Make scoped commits and push authorized work to v1.2. Do not tag a release before RELEASE_CHECKLIST passes.

Keep CURRENT_STATE accurate after each milestone, including blockers and unverified checks. Complete only when all feature acceptance checks and the emulator/local APK release gates pass. Physical vibration feel and real-phone performance remain unverified under the approved emulator-only setup; do not make claims about them.
