---
name: bug-hunt
description: Performs an adversarial cross-domain bug hunt covering Android lifecycle, Compose state/UI, economy, timing, persistence, interaction, accessibility, UX, performance and release-only behavior.
---

# Bug Hunt

Use `docs/BUG_HUNT_CHECKLIST.md` and `docs/FEATURE_TEST_MATRIX.md`.

Do not only review code. Reproduce through tests/device runs where possible. For each confirmed bug:

1. record reproduction;
2. identify root cause;
3. add/strengthen a regression test where feasible;
4. fix the root cause;
5. run targeted checks;
6. run affected Journey;
7. run regression suite.

Continue until no P0/P1/P2 release-blocking findings remain.
