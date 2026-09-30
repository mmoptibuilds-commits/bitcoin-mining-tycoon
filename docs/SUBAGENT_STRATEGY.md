# Antigravity Subagent Strategy

## Recommended model

Keep the parent conversation on **Gemini 3.8 Flash High**. Custom subagents use `model: inherit` so they follow the parent tier when supported.

## Why not let five agents edit at once?

This is a single-module, state-heavy game. Economy, state, ViewModel, persistence and UI touch shared types. Parallel writers increase merge/conflict/regression risk. Use subagents primarily as independent verifiers and specialists.

## Workspace agents provided

- `spec-critic`: checks implementation/plan against docs.
- `gameplay-auditor`: checks formulas, exploits, pacing, edge cases.
- `ui-ux-auditor`: checks layout, accessibility, anti-slop, screenshots/semantics.
- `feature-verifier`: runs targeted/full tests and on-device verification without modifying production code.
- `release-auditor`: final cross-domain release review.

## Delegation rule

After a feature is implemented, invoke the verifier most relevant to it. For complex features (prestige/offline/persistence), invoke both `feature-verifier` and `gameplay-auditor`. For visual screens, invoke `ui-ux-auditor`.

Subagents should not “vote” on arbitrary implementation styles. They verify against explicit requirements and tests.

## When to use `/boost`

Use for a stubborn race, state corruption, Gradle/R8-only failure, arithmetic/bulk-buy bug, or difficult lifecycle defect after ordinary debugging has failed.

## When to use `/teamwork-preview`

Not the default for this one-day build. Use only if the project grows beyond this scope or the main `/goal` run cannot maintain context. Teamwork is better for repo-scale/multi-day projects; it can introduce unnecessary coordination for this tightly coupled app.
