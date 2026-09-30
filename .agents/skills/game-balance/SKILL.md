---
name: game-balance
description: Audits and tunes Bitcoin Mining Tycoon progression, miner cost curves, power/heat constraints, market behavior, offline rewards, and prestige using deterministic simulation rather than intuition alone.
---

# Game Balance

1. Read `GAME_DESIGN.md` and `ECONOMY_BALANCE.md`.
2. Extract all current balance constants and formulas.
3. Run deterministic simulations for beginner, balanced, hold/sell, early/late prestige and 12h offline strategies.
4. Check dead ends, dominant strategies, useless upgrades, sudden walls, runaway loops and prestige pacing.
5. Change data/config constants before changing architecture.
6. Add regression assertions for discovered balance invariants.
7. Re-run simulations and summarize before/after milestone pacing.
