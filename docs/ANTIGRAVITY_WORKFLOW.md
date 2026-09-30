# Antigravity Workflow

## Recommended command strategy

Do **not** paste a massive build request directly into `/goal` from an empty folder. Establish requirements and planning artifacts first.

### Phase 0 — Environment

1. Install/update Antigravity and Android CLI.
2. Select Gemini 3.8 Flash High.
3. Copy this pack into the project root.
4. Install official Android skills.
5. Ensure an Android 12+ emulator and a current emulator/device are available.
6. Optionally open the project in Android Studio for `android studio` semantic/Compose-preview tooling.

### Phase 1 — Requirement challenge (`/grill-me`)

Run once. The PRD is already detailed, so instruct it to ask only material unresolved questions and not repeat decisions.
Use the exact prompt in `PROMPTS.md`.

After answering, tell it to update `docs/DECISIONS.md` only for genuinely new decisions.

### Phase 2 — Visual product direction (`/frontend-design`)

This is a workspace skill supplied by this pack, not a built-in Antigravity command. Run it before implementation so design constraints inform the architecture/plan.

### Phase 3 — Implementation plan (`/plan`)

Ask Antigravity to inspect every product document and produce a phased plan with tests attached to every feature. Review the plan artifact. Do not allow “build everything then test at the end.”

### Phase 4 — Autonomous execution (`/goal`)

After the plan is approved, run the master `/goal` prompt. The agent implements sequentially. Every feature must use the `feature-cycle` skill before the next feature starts.

### Phase 5 — Deep debugging (`/boost` only when needed)

Do not spend multi-agent deep reasoning on ordinary compile errors. Use it for stubborn logic, lifecycle, concurrency, R8, or state-corruption problems.

### Phase 6 — Release audit (`/release-audit`)

Run the custom skill, then require the full release checklist. Fix every P0/P1/P2 release blocker it finds.

## Why `/teamwork-preview` is not the default

It is excellent for large multi-day/repo-scale projects, but this app is a single tightly coupled module. The recommended workflow instead uses one writer plus specialist verifier subagents. If `/goal` loses context or the project expands significantly, switch to Teamwork with the provided Teamwork prompt.

## `/browser`

Not useful for core native UI testing. Prefer Android CLI screen/layout/Journeys and Android Studio Compose rendering. Use browser only for live documentation/research when necessary.

## `/learn`

After the project is complete and you have corrected Antigravity repeatedly, run `/learn` only if you want those corrections distilled into persistent rules/skills. Review what it proposes before accepting; do not let it duplicate existing rules.
