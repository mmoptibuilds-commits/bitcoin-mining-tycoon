# Exact Antigravity Prompts

## 1. `/grill-me`

Paste:

```text
/grill-me Read @AGENTS.md and every Markdown file under @docs before asking anything. We are building Bitcoin Mining Tycoon, a native Android 12+ portrait-only offline clicker/idle game in Kotlin + Jetpack Compose. The existing documents contain intentionally locked decisions. Do not re-ask questions already answered there.

Your job is to find ONLY implementation-critical ambiguities that could materially change architecture, game rules, persistence, UX, testing, accessibility, or production local-release behavior. Ask at most 10 high-value questions total, grouped logically. Do not ask about trivial colors, naming, library preferences, or details you can safely resolve from Android best practices and the existing design system. Challenge contradictions and hidden edge cases. Do not write code yet.
```

## 2. `/frontend-design`

This command exists because this repo includes `.agents/skills/frontend-design/SKILL.md`.

```text
/frontend-design Audit @docs/PRD.md @docs/FEATURES.md @docs/DESIGN_SYSTEM.md @docs/UX_SPEC.md and @AGENTS.md. Produce the final native Android Compose visual/interaction direction before implementation. Preserve the industrial graphite/copper control-panel identity and all anti-AI-slop constraints. No generated imagery, gradients, glassmorphism, crypto-bro visuals, giant rounded cards, generic SaaS dashboard patterns, emoji UI, or decorative nonsense.

Define the Mine, Hardware, Upgrades, Stats, Settings, offline-return, prestige and achievement/event experiences at component level. Verify one-handed portrait hierarchy, text scaling, 48dp targets, TalkBack semantics, reduced motion, system insets, disabled/locked/error states and late-game large-number layouts. Prefer reusable Compose components and design tokens. This is a design/spec pass only; do not implement production code yet. Update docs only if you find a contradiction or missing requirement.
```

## 3. `/plan`

```text
/plan Build Bitcoin Mining Tycoon exactly from @AGENTS.md and every file under @docs. First inspect all requirements, installed skills, Android tooling, SDK/toolchain and current repository state.

Create a reviewable implementation plan that is optimized for correctness, production readiness, and a one-shot feature-complete V1. Use Kotlin + Jetpack Compose, minSdk 31, current stable Android target/toolchain, local-only DataStore persistence, deterministic game engines, and a single app module unless there is a proven need otherwise.

CRITICAL: plan vertical feature slices. Every feature/milestone MUST include its tests and validation before the next feature begins: JVM logic tests, persistence tests where relevant, Compose interaction tests, Compose preview/screenshot+semantics review for UI, emulator/device run, Android CLI Journey or equivalent core flow, boundary/failure-case test, and regression suite. Do not schedule “testing” as one final phase.

Identify exact files/packages to create, state/data flow, save schema/versioning, clock/RNG abstractions, economy numeric type, bulk/MAX algorithm, market model, prestige model, accessibility plan, release/R8 plan and test strategy. Include objective acceptance criteria for every milestone. Use verifier subagents from .agents/agents for independent review but keep the main agent as production-code writer to avoid conflicting edits.

Do not begin implementation until the plan artifact is complete and internally checked against PRD/FEATURES/TEST_STRATEGY.
```

## 4. Main `/goal` — implementation

```text
/goal Execute the approved implementation plan for Bitcoin Mining Tycoon to completion. Read @AGENTS.md and all @docs first and treat them as binding requirements. Use Gemini 3.8 Flash High reasoning rigor throughout.

Build a production-quality native Android game and sideloadable release APK, not a prototype. Kotlin + Jetpack Compose; Android 12+; portrait; completely local/offline; no backend, accounts, ads, IAP, analytics, network permission, real crypto mining, real BTC feed, wallet or financial integration.

Implement sequential vertical slices. BEFORE starting the next feature, apply the feature-cycle skill to the current feature and fix every failure. This is mandatory. Test logic, persistence, UI interaction, accessibility/semantics, visual hierarchy, UX behavior, device/emulator behavior, lifecycle/process death where relevant, error/boundary cases, performance/recomposition, and regression impact. A feature is not complete because it compiles or looks correct.

Use the provided custom verifier subagents for independent audits after major features. The main agent owns production edits; verifier subagents should report defects rather than independently rewriting shared production files.

Use official Android CLI tooling for build/run/install/screen/layout/Journeys and Android Studio tooling for analyze-file/render-compose-preview when available. Keep stable dependencies only. Follow the anti-AI-slop design rule strictly: no generated imagery, purple/blue gradients, glass UI, oversized rounded-card soup, 3D coins, generic dashboard slop, emoji icons, meaningless charts or decorative AI visuals. Build original vector/Canvas UI.

Continuously maintain tests and docs as architecture actually lands. Never weaken valid tests just to pass. On a crash or serious bug: reproduce, capture logs, add a regression test where feasible, fix root cause, rerun targeted and regression suites.

Do not stop at debug success. Finish release hardening: unit/UI/Journey suites, lint, Android Studio analysis, minified release build, R8/resource shrinking, release install/launch smoke test, API 31+ compatibility check, current Android check, process-death/save-restore test, 12-hour offline test, rapid-tap stress test, accessibility check, manifest/privacy audit and all current-scope items in @docs/RELEASE_CHECKLIST.md and @docs/PRODUCTION_READINESS.md.

If a normal debugging loop cannot resolve a genuinely difficult defect, use /boost for that defect, then continue the goal. Do not remove features to make the build green unless a documented requirement is technically impossible; in that case implement the closest robust equivalent and document the exact reason.

Final handoff must include: exact test/build commands run and results, remaining known issues grouped by severity (target: none release-blocking), installable APK path plus any optional local release artifacts, toolchain/dependency versions, save-schema version, and a concise walkthrough of the finished app.
```

## 5. `/boost` prompt for a stubborn bug

```text
/boost Investigate and fix this release-blocking Bitcoin Mining Tycoon defect: <DESCRIBE DEFECT>. Read the relevant docs, implementation and tests. Use independent hypotheses and verification loops. Reproduce before modifying code, identify root cause, add/strengthen a regression test, implement the smallest correct fix, then run targeted tests plus the affected core Journey and full regression suite. Do not paper over the symptom or weaken assertions.
```

## 6. `/bug-hunt`

```text
/bug-hunt Perform a broad adversarial bug hunt over the current implementation. Cover game logic, economy precision, bulk/MAX purchases, event stacking, prestige, offline/daily clocks, persistence/migrations, process death, Compose stale state/recomposition, rapid interaction, navigation/back, accessibility, system insets, font scale, reduced motion, audio/haptics, UX feedback, balance exploits/softlocks and release/R8-only behavior. Reproduce and fix defects through the feature-cycle gate; do not just produce a report.
```

## 7. `/release-audit`

```text
/release-audit Treat @docs/PRODUCTION_READINESS.md and @docs/RELEASE_CHECKLIST.md as the release contract. Independently audit the entire project, run all applicable automated/device tests, inspect manifest/dependencies/release config, verify local-only privacy constraints, install and smoke-test the minified release build, and find UX/accessibility/logic/persistence/performance/manifest/privacy defects. Fix release blockers through the normal main-agent workflow, rerun the complete gate, and only then declare release readiness.
```

## 8. Optional `/teamwork-preview` fallback

Use only if the one-agent `/goal` run loses context or the project becomes much larger:

```text
/teamwork-preview Build and independently verify Bitcoin Mining Tycoon from @AGENTS.md and @docs as a production-quality Android sideloadable build. Preserve a single source of truth and avoid concurrent edits to the same shared production files. Milestones must be independently verified with unit/UI/device/Journey/release checks before handoff. Use specialized tracks for game engine/economy, Android UI/UX, persistence/lifecycle, QA/accessibility/performance and release audit, with objective acceptance checks from TEST_STRATEGY and PRODUCTION_READINESS.
```
