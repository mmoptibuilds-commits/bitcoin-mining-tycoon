# Exact Implementation Guide

## Stage 1 — Workstation setup (Windows)

1. Install/update Antigravity and select **Gemini 3.8 Flash High**.
2. Install Android Studio current stable (Quail 2+ if using Android Studio bridge).
3. Install Android CLI:

   ```powershell
   winget install --id Google.AndroidCLI
   android update
   android init
   android info
   ```

4. Install SDK/platform/API 31 and current API 37/toolchain as reported by Android CLI/Android Studio.
5. Create an emulator for an Android 12/12L API and a current Android 17 API if machine resources allow. A physical current Android phone is valuable for final haptic/performance checks.
6. Run:

   ```powershell
   android skills list --long
   ```

   Then install the skills listed in `docs/SKILLS.md`.

## Stage 2 — Create project/repository

1. Create a clean project folder/repo.
2. Copy this pack to the root so `AGENTS.md`, `.agents/`, `docs/` and `journeys/` are present before implementation.
3. Initialize Git and make a baseline docs commit if desired.
4. Ask Antigravity to scaffold a native Compose project with minSdk 31 and application ID you choose.
5. Keep secrets/signing files ignored.

## Stage 3 — Validate tooling before feature work

Run/ask agent to run:

```powershell
android info
android studio check
```

`android studio check` is optional if Android Studio is not open/compatible.

Build the empty scaffold once and launch it on the emulator/device. Fix environment/toolchain problems before adding gameplay.

## Stage 4 — Requirements + design

1. Run the `/grill-me` prompt from `PROMPTS.md`.
2. Answer only remaining material questions.
3. Run `/frontend-design`.
4. Review the resulting design/spec artifact against `DESIGN_SYSTEM.md` and `UX_SPEC.md`.
5. Reject any generated images/glass/gradient/generic dashboard treatment.

## Stage 5 — Plan

Run the `/plan` prompt. Ensure the plan contains these milestones in roughly this dependency order:

1. scaffold/design tokens/test infrastructure;
2. numeric helpers + core GameState + clock/RNG;
3. save repository + migrations;
4. manual/passive mining vertical slice;
5. market + sell flow;
6. hardware + bulk purchase;
7. power + heat/cooling;
8. upgrades;
9. events + achievements;
10. offline + daily reward;
11. prestige + Satoshi tree;
12. stats/settings/audio/haptics;
13. onboarding/polish;
14. adversarial bug hunt;
15. local release hardening.

Every milestone must include tests before the next milestone.

## Stage 6 — Execute `/goal`

Run the exact master prompt. Let Antigravity work autonomously but inspect plan/diff artifacts when it surfaces them.

### Mandatory feature cycle

For feature N:

1. Main agent reads requirements.
2. Add tests/fixtures.
3. Implement logic/state.
4. Implement UI.
5. Run targeted tests.
6. Analyze changed Kotlin files.
7. Render Compose preview + semantics where applicable.
8. Build/run on emulator/device.
9. Run relevant Journey.
10. Inspect screen/layout hierarchy.
11. Invoke verifier subagent.
12. Fix findings.
13. Run regression suite.
14. Only then move to N+1.

## Stage 7 — Android CLI validation examples

Let the agent discover current syntax with `android <command> -h` rather than inventing flags. Typical capabilities to use:

- `android run` — build/deploy/launch.
- `android install` — install artifact.
- `android screen` — capture live screen.
- `android layout` — inspect interactive/full hierarchy.
- `android emulator` — manage AVDs.
- `android studio analyze-file` — IDE inspection.
- `android studio render-compose-preview` — render preview and semantics.

## Stage 8 — Test on both compatibility edges

At minimum:

- API 31/32 emulator smoke/core journeys;
- current API 37 emulator/device smoke/core journeys.

The primary target phone may be Android 16; include a physical run there if available.

## Stage 9 — Local release hardening

1. Run `/bug-hunt`.
2. Resolve all high-severity findings.
3. Run `/release-audit`.
4. Run the R8 analyzer.
5. Enable minification/resource shrinking for the release variant.
6. Build the local release APK. If signing is needed for installation, use a local test/release key kept outside Git.
7. Install the release APK on at least one physical device or emulator and run the critical Journeys.
8. Verify there is no release-only serialization, shrinking, startup, or restore crash.
9. Finish `PRODUCTION_READINESS.md`.
10. Do **not** block today's build on Play Store policy work or an AAB. If publishing is planned later, use `OPTIONAL_PLAY_STORE_FUTURE.md` then.

## Stage 10 — Final handoff

Require:

- installable APK path and any optional local release artifact paths;
- test results;
- screenshots/recordings of core flows;
- save schema version;
- dependency/toolchain summary;
- known issues (ideally none blocking);
- build instructions in repository README.
