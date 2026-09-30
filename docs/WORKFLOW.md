# Codex execution and optional Antigravity verification

## Preparation and authority

The v1.2 branch has requirements and execution guidance; runtime redesign remains to be built. Read AGENTS, CURRENT_STATE, PRD, FEATURES and PROJECT_PLAN, then milestone-specific docs. Do not scaffold a new app or reread all vendored Android references on every turn.

Product requirements are canonical; plans select an implementation path; CURRENT_STATE is the progress/evidence ledger. Update the ledger after a meaningful milestone or blocker. Keep detailed logs/screenshots under ignored artifacts and put concise proof in the ledger.

## Codex owns implementation

1. Select the user-chosen GPT-6 Luna/Max in the harness.
2. Use P1 `/plan` to inspect live source/tooling and refine M0–M7 into executable tasks with exact files, meaningful tests and commands. Preserve locked decisions; raise only genuinely blocking gaps.
3. After user approves that implementation path, activate P2 `/goal`. Implement all F01–F25 and M0–M7; verify between milestones and push scoped commits to v1.2.
4. Use P3 to resume without redoing completed commits; P4 targets an individual milestone when needed.
5. Finish with P7's read-only audit, resolve confirmed findings in Codex and rerun affected checks. Report exact SHA, evidence, candidate APK and outstanding constraints. Stop writing before any optional handoff.

Goals require a supported Codex surface/version. Official guidance distinguishes `/plan` (reviewable path) from `/goal` (persistent outcome plus evidence/stopping conditions). Goals can stop at budget limits, interruption or blockers; they are not a promise of unlimited unattended runtime. If unavailable, use normal prompts with the same completion contract and explicit resumption ledger.

## Optional Antigravity verification

Codex can implement, verify and fix the complete game through M7. Recommended primary sequence: P1 → approved plan → P2 → P7 → fixes and affected rechecks as needed. Completion depends on the same recorded native test, simulation, emulator and minified-APK evidence, regardless of harness. Lack of Antigravity access is not a completion blocker.

For an optional second pass, use P5 `/plan` at a stable pushed SHA to reproduce Android flows on emulators. Its output is findings/evidence. Transfer writing ownership only after Codex stops and before P6 fixes verified bugs; Codex may instead retain ownership and fix reported findings. Antigravity can implement a milestone using P4/P6 semantics if chosen, following the same requirements and save/version contracts. P7 is the final read-only audit for either harness; use an independent reviewer when supported.

## Skills, commands and reviewers

Project skills in `.agents/skills`: feature-cycle, frontend-design, game-balance, product-copy, bug-hunt, release-audit; official reference skills: testing-setup, edge-to-edge, navigation-3. Ask by name/path; check harness discovery. Codex skill invocation syntax and Antigravity slash-command registration are surface-specific; the existence of a SKILL.md does not establish every command on every harness. Current Antigravity supports manual skill slash commands on supported surfaces. No new legacy workflow files are needed.

Official Android CLI/Studio tools support native analysis, previews, screen/layout and journeys where installed. They are optional helpers; Gradle/ADB/Compose tests and Device Manager are valid evidence paths. A test named Journey under src/test is still a JVM test, not device E2E.

Use one implementation writer. Read-only specialist review focuses on save/economy, UI/semantics or release risk; never have three competing writers or model voting. The main writer resolves findings with reproduction/tests. Preserve selected parent model; do not install random MCPs or credentials. GitHub is sufficient for repository work; no Supabase/Figma/browser-testing dependency for this native scope.

## Quality-gate policy

Per milestone: relevant logic/migration tests, Compose interactions, screenshot/semantics inspection, emulator journey, boundary/lifecycle check and affected regressions. Run the full suite at integration/release and whenever a shared change warrants it; do not repeatedly rerun unchanged unrelated suites only as ceremony. Do not weaken assertions or substitute 'looks fine' for proof.

The old `.agents/hooks.json`/stop script is removed: it was harness-specific and accepted only a debug build/test subset. This does not remove release requirements. Use explicit commands from TEST_STRATEGY/RELEASE_CHECKLIST and record actual evidence; only configure future hooks after verifying the installed harness schema and preventing recursive/stale-completion behavior.

## Blockers and autonomy

After plan approval, settle routine implementation choices and record consequential rulings; don't repeatedly ask to continue. Missing emulator/SDK/signing key/model access is a concrete blocker for dependent proof. Continue independent work, report exact failed command and next needed input, and keep blocked work unverified. Never pretend a phone test happened or uninstall to fix signing.

## Current official references

- [Codex goals](https://developers.openai.com/cookbook/examples/codex/using_goals_in_codex)
- [Codex long-running planning](https://developers.openai.com/blog/run-long-horizon-tasks-with-codex)
- [Antigravity plan](https://antigravity.google/docs/plan/)
- [Antigravity skills](https://antigravity.google/docs/skills/)
- [Android CLI](https://developer.android.com/tools/agents/android-cli)
