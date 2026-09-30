---
name: feature-cycle
description: Implements or verifies one Bitcoin Mining Tycoon feature end-to-end and blocks progression until logic, persistence, UI, UX, accessibility, device behavior, failure cases, and regressions pass.
---

# Feature Cycle

## Inputs

Feature ID/name and relevant requirements from `docs/FEATURES.md`.

## Cycle

1. Read requirements and identify state/formula/persistence/UI surfaces affected.
2. Define objective acceptance checks.
3. Add or update pure unit tests first where logic exists.
4. Implement the smallest coherent production slice.
5. Run targeted unit tests.
6. Run persistence/migration tests if the save model changed.
7. Add/run Compose interaction tests if user-visible.
8. Analyze changed Kotlin with Android Studio tooling when available.
9. Render/inspect previews and semantics for changed UI.
10. Build/deploy/run with Android CLI.
11. Run/update the relevant Journey.
12. Inspect screenshot and layout tree for visual/UX/accessibility defects.
13. Exercise at least one boundary/failure/lifecycle case.
14. Invoke an independent verifier subagent when material.
15. Fix all defects found.
16. Run full existing regression suite.
17. Record the feature complete only after green results.

## Never accept

- “Build succeeds” as sufficient verification.
- Screenshot-only verification for logic.
- Unit-only verification for interaction UX.
- deleting a failing valid test.
- moving on while a known defect remains in the current feature.
