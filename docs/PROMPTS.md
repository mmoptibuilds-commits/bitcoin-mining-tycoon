# Copy-paste prompts — v1.2

Use the repository checkout on branch v1.2. Select GPT-6 Luna/Max in Codex before starting. These prompts do not configure a model or bypass harness permissions. Product docs contain the complete F01–F25 acceptance contract, so they remain authoritative through long sessions and compaction.

Recommended Codex sequence: **P1 → approve plan → P2 → P7 → fix findings and rerun affected checks**. Codex owns full implementation, verification and fixes. P3 resumes interrupted work; P4 limits a session to a milestone. Optional Antigravity second pass: **P5 → approve verification/fix path → P6 → P7** at a stable pushed commit, after Codex stops and writing ownership transfers. Both paths have the same completion evidence; Antigravity access is not required. Read WORKFLOW and SETUP_WINDOWS first.

## P1 — Codex planning (`/plan`)

```text
/plan Plan the complete Bitcoin Mining Tycoon v1.2 redesign in this existing repository. Read AGENTS.md, docs/CURRENT_STATE.md, docs/PRD.md, docs/FEATURES.md, docs/PROJECT_PLAN.md and all relevant design/economy/save/test/version docs before proposing work. Inspect live source, Git branch/history/dirty state, installed skills, Gradle/JDK/SDK and Android Studio/emulator tooling. Work is on v1.2; preserve main, v1.0 and all historical branches. Do not scaffold a new app.

Create an executable plan for every F01–F25 requirement, refining M0–M7 with exact existing/new files, component/state interfaces, migration steps, meaningful tests, commands and observable acceptance evidence. Retain Kotlin/Compose, the existing deterministic engines, BigDecimal helpers, DataStore, one app module, offline-only behavior and current working stable toolchain. Preserve app/package/storage/signing identity and all valid old saves. Reference first prestige is 25–35 minutes per ECONOMY_BALANCE, with a demonstrably faster next run.

Include the full visual/gameplay overhaul: understandable Mine→Bitcoin→sell→cash→machine loop; facility home rather than dashboard; progressive disclosure; evolving PC/rig/industrial/orbital/lunar/Dyson scenes; all 20 existing hardware tiers and systems; 50–60 meaningful grouped upgrades; corrected content targets/defaults; real haptics and purposeful bounded feedback; complete honest statistics; all native accessibility/settings/reward/prestige flows; custom adaptive/themed icon, system splash, launcher/App Info/recents/About/version packaging. No generated artwork, backend, real crypto, web wrapper or parallel writers.

Plan old-schema fixtures and signed in-place emulator update without uninstall/data clear. Cover transaction/tick races, repeated reward claims, fractional time, event expiry, extreme values, unsupported/corrupt save recovery, legacy unlocks and prestige bookkeeping. Tests/visual/emulator proof belong to each milestone, not one final afterthought. Use Android Studio Device Manager + Gradle/ADB; physical USB phone is not required. Discover supported CLI/skill syntax and use native fallbacks; don't invent commands or test results.

Do not change production files yet. Raise only implementation-blocking ambiguities not answered by the docs. Present the plan with requirements coverage, environment blockers and exact validation commands, then wait for my approval. After approval the intended execution is autonomous sequential implementation with scoped commits/pushes to v1.2.
```

## P2 — Codex master execution goal (`/goal`, after approval)

```text
/goal Deliver the full approved Bitcoin Mining Tycoon v1.2 redesign on branch v1.2, satisfying every F01–F25 acceptance criterion and M0–M7 in docs/FEATURES.md and docs/PROJECT_PLAN.md. Read AGENTS.md, the approved implementation plan, docs/CURRENT_STATE.md and the relevant canonical docs; inspect actual source/history before changing it. Continue implementing, reproducing defects, testing and fixing until the verified emulator/local APK release contract in docs/RELEASE_CHECKLIST.md is satisfied, or a concrete blocker prevents further authorized progress.

Required outcome: beginner-friendly Mine→Bitcoin→sell→cash→machine→automation; facility-centered home with progressive teaching/disclosure; 20 retained hardware tiers; ten evolving original Canvas/vector facility scenes through Dyson; 50–60 useful grouped upgrades; preserved market/sell/auto-sell, power/cooling, events/windfalls, achievements, 7-day rewards, 12h offline progress, prestige and permanent tree; corrected IDs/defaults/RNG/time/state bugs; complete source/power/thermal/playtime stats with honest migrated coverage; real sound/haptics/settings and bounded tap/combo/critical/purchase/milestone feedback; accessibility/reduced-motion/battery behavior; original adaptive/round/themed icon, coherent system splash, native launcher/App Info/recents, About/changelog and versioned APK packaging.

Preserve every valid schema-1 field according to docs/SAVE_COMPATIBILITY.md, including old balances, hardware, upgrades, achievements, prestige history/nodes, rewards, settings, onboarding, auto-sell threshold/valid enablement, active events/expiry, market/event timers, last-save timestamp and RNG state. Keep applicationId com.antigravity.bitcoinminingtycoon, storage identity and compatible signing. Production rates may rebalance, assets may not be wiped. Preserve unsupported/corrupt raw payloads before recovery. Never uninstall or clear old app data to bypass update/signing/migration issues. Keep local/offline-only architecture, minSdk31, no INTERNET/backend/ads/analytics/real crypto/background service. Retain working stable dependencies unless a documented requirement justifies change. No generated artwork, 3D engine, web rewrite or concurrent production writers.

Verification: actual-engine deterministic simulations with the fixed reference policy reach first prestige in 25–35 minutes without rewards/luck, passive dominates by minute5 and next comparable run is at least25% faster; include seeded variance/low-tap/legacy/offline cases. Required JVM/content/migration/Compose/lint suites pass; old-schema fields and signed in-place update preserve data; emulator API31/current configured target journeys verify all core flows, compact/tall/font1.5/TalkBack/reduced motion, icon/splash surfaces and process death. Build/install/run the actual minified APK and rerun critical release flows. Emulator proof does not establish physical haptic feel or phone performance.

Follow the project feature-cycle and relevant design/game-balance/product-copy/bug-hunt/release-audit skills. One writer; read-only independent review for material risks when supported. Use Android Studio/emulator/Gradle/ADB and optional supported Android CLI helpers. After each milestone update CURRENT_STATE with commit, feature IDs, actual commands/results, fixture/seed/emulator, screenshots/reports, decisions and blockers. Keep detailed generated artifacts ignored. Fix failures at cause; never weaken valid tests, fabricate evidence or mark code-only work verified. Choose the next action from measured failures and unfinished acceptance criteria; avoid repeating completed milestones after compaction.

Commit scoped verified changes and push normally to v1.2; preserve main, v1.0 and all historical refs. No force-push, branch deletion, shared-history rewrite or main merge. Don't tag a release merely because docs or a debug build passed. Finish P7's read-only audit in Codex, resolve confirmed findings and rerun affected checks; use an independent reviewer when supported. Record a stable SHA, APK checksum/certificate fingerprint and remaining checks. Prepare an Antigravity handoff only if a second pass is chosen; lack of that harness is not a blocker. If tooling, signing, access or budget blocks a required proof, continue independent work, then stop with exact attempted command/evidence, incomplete requirement, blocker and the next input needed. Final report must distinguish delivered code, verified behavior and unverified checks.
```

If `/goal` is absent in the installed Codex surface, paste the same text without its first `/goal` token as a normal execution prompt. Require the same ledger and completion conditions; use P3 when continuation is needed. Native goals have budget/interruption/blocker stop conditions, not guaranteed unlimited runtime.

## P3 — Resume without repeating work

```text
Resume Bitcoin Mining Tycoon v1.2 from the committed state. Read AGENTS.md, docs/CURRENT_STATE.md, the approved plan and Git log/status on v1.2. Reconcile the ledger with actual commits and test artifacts. Do not recreate completed work, reset user edits, scaffold or change historical branches. Continue from the first unfinished acceptance criterion using the full P2 contract in docs/PROMPTS.md. Reproduce any uncertain previous claim before trusting it. Preserve saves/signing and emulator-only scope. Update the ledger and commit/push scoped work; stop only at completion or a concrete dependent blocker with evidence.
```

In the same supported Codex thread, use `/goal` to inspect state and `/goal resume` to resume an existing paused goal. Do not create a competing goal or assume a new thread inherits the old goal; the repository ledger travels across threads.

## P4 — One milestone or controlled implementation session

```text
Implement the first unfinished milestone in docs/PROJECT_PLAN.md on v1.2, unless I named a particular milestone in this message. Read AGENTS.md, CURRENT_STATE and that milestone's canonical requirements. Inspect the live checkout and relevant project skills. First present a concise exact-file/test plan if this milestone has not already been approved; otherwise execute the approved path without repeating planning.

Deliver the whole milestone, including logic/state/UI, migration where needed, content integrity, targeted regressions, Compose/screenshot/semantics proof, emulator journey and boundary/lifecycle check. Preserve old saves/package/signing and all historical branches. Use the same architecture/product constraints and truthful evidence policy as P2. Fix confirmed failures, update CURRENT_STATE, commit/push scoped work to v1.2 and leave a concrete next milestone handoff. Do not expand into unrelated features or mark blocked device proof complete.
```

## P5 — Optional Antigravity verification plan (`/plan`, stable commit)

```text
/plan Prepare the Android verification and bug-fix handoff for this existing Bitcoin Mining Tycoon v1.2 checkout. Codex is the prior implementer; first inspect branch/status/log and record the exact pushed commit to verify. If another writer is active, do not edit shared files. Read AGENTS.md, CURRENT_STATE, FEATURES, TEST_STRATEGY, RELEASE_CHECKLIST, APP_IDENTITY and SAVE_COMPATIBILITY. Do not redesign or rebuild the app from scratch.

Plan emulator verification on API31 and the configured target API using Android Studio Device Manager, Gradle/ADB and installed native Android helpers. No USB phone is required. Cover all F01–F25, especially novice/migrated first session, visible facility stages, sales/bulk/infrastructure/upgrades, events/rewards/offline/prestige, settings/stats, save migration/in-place signed update, process death, font1.5/TalkBack/reduced motion, launcher/themed icon/App Info/recents/splash and minified release behavior. Distinguish JVM journeys from real UI flows and emulator wiring from physical tactile proof.

Return a reviewable verification/fix plan with exact commands/fixtures/emulator config, existing proof to reproduce, unavailable checks and severity criteria. Do not claim planned checks passed. Wait for approval/writing ownership before fixes; then P6 defines execution.
```

## P6 — Optional Antigravity full verification and fixes (normal prompt)

```text
Execute the approved verification/fix plan for Bitcoin Mining Tycoon v1.2. You now own writing after Codex has stopped. Record starting SHA and clean/dirty state, read AGENTS.md plus the full P2 completion contract and current ledger, and verify the actual app rather than assuming the prior writer's report is correct.

Build/install/run on Android Studio emulators, run relevant JVM/Compose/lint/simulation checks and all journeys, inspect screenshots/layout/semantics and actual minified APK behavior. Cover every F01–F25 criterion and release checklist; verify old saves through same-package/same-certificate in-place update without uninstall/data clear. Check novice clarity, automation pacing, facility purchase/milestone/reset changes, every existing system, grouped upgrades, complete/migrated statistics, settings/haptics/reduced motion, native accessibility/insets/back and app icon/splash/launcher/App Info/recents/About/version details.

For each finding separate confirmed reproduction from suspicion, rank by actual user impact, identify the root cause, add a meaningful regression where feasible, fix it and rerun affected tests/journey plus justified shared regressions. Do not weaken tests or redesign already approved product choices. Keep Kotlin/Compose/local engines, stable content IDs and assets/signing/storage, 12h offline cap, no network/backend/real crypto, original Canvas/vector visuals and preserved version branches. No concurrent writers, fake test evidence or claim of physical phone/haptic validation.

Continue until all emulator/local-release acceptance gates pass or a concrete blocker prevents proof. Update CURRENT_STATE/CHANGELOG with exact SHA/commands/results/fixtures/seeds/emulators/reports, commit and push scoped fixes normally to v1.2. Preserve main/v1.0; no force push, branch deletion or main merge. Return the verified commit, APK/checksum/public certificate fingerprint, confirmed remaining blockers and unverified checks. Never call the app complete solely because a debug build or unit suite passed.
```

## P7 — Final independent audit (either harness, read-only)

```text
Audit the current v1.2 commit against AGENTS.md and every F01–F25 criterion, the five PROJECT_PLAN review-focus cases, SAVE_COMPATIBILITY, APP_IDENTITY and RELEASE_CHECKLIST. Read CURRENT_STATE but independently reproduce its material claims with existing tests, artifacts and emulator/release flows where tooling permits. Do not edit production files or reset app data/history.

Find requirements omissions, false completion evidence, save/signing/update risks, state/RNG/clock/precision errors, balance softlocks, inaccessible or confusing native flows, unimplemented settings/stats, visual regression, template icon/splash resources, release-only crashes and version-history violations. Return confirmed findings with severity, exact file/feature, reproduction and expected/actual result. List unavailable checks separately; do not turn lack of proof into a pass. Identify the exact verified SHA and whether the emulator/local APK contract is satisfied. Corrections are implemented by the Codex main writer's next focused fix session, or through P6 after an optional ownership transfer. Antigravity access is not required to satisfy this audit.
```

## Prompt format and finish line

Every handoff records branch/SHA, relevant requirement IDs, scope, constraints, evidence/commands, iteration policy and blocker condition. Long runs use the committed docs as shared memory. Neither harness silently drops a feature, resets a valid save or changes version history to finish faster.
