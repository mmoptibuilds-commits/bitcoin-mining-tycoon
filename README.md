# Bitcoin Mining Tycoon — Antigravity Build Pack

This repository pack is the source-of-truth package for building **Bitcoin Mining Tycoon** as a native Android production-quality sideloadable build with Google Antigravity and Gemini 3.8 Flash High.

## Product in one sentence

A portrait-only, offline, Cookie Clicker-style Bitcoin mining tycoon where the player taps to bootstrap compute, buys increasingly absurd mining hardware, manages power/heat, trades a simulated BTC market, unlocks upgrades and achievements, earns offline progress, and prestiges for permanent Satoshi Point bonuses.

## Hard constraints

- Native Android only.
- Kotlin + Jetpack Compose.
- Android 12+ (`minSdk 31`).
- Compile/target Android 17 / API 37 where supported by the installed stable toolchain.
- Portrait-only gameplay.
- Offline/local save only; no backend, login, ads, IAP, analytics, wallet, real mining, real trading, or financial APIs.
- Maximum offline earnings window: 12 hours.
- Feature-rich one-shot V1; after completion only bug fixes and small changes are expected.
- Production-quality local release target: automated tests, on-device validation, accessibility, performance checks, R8/minified release verification, installable APK, and no known crash/data-loss defects in the tested scope.
- No AI-looking/generated artwork. UI must look intentionally designed by a human product team.

## Read order for agents

1. `AGENTS.md`
2. `docs/PRD.md`
3. `docs/FEATURES.md`
4. `docs/GAME_DESIGN.md`
5. `docs/ARCHITECTURE.md`
6. `docs/DESIGN_SYSTEM.md`
7. `docs/UX_SPEC.md`
8. `docs/TEST_STRATEGY.md`
9. `docs/IMPLEMENTATION_GUIDE.md`
10. `docs/ANTIGRAVITY_WORKFLOW.md`

## Build philosophy

Implement one coherent vertical slice at a time. **A feature is not done when it renders; it is done only after logic tests, UI/interaction tests, on-device validation, UX review, regression tests, and save/restore behavior relevant to that feature have passed.**

The main agent is the only general-purpose writer. Subagents are primarily independent critics/verifiers to reduce conflicting edits. See `docs/SUBAGENT_STRATEGY.md`.
