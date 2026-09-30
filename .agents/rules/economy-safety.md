---
trigger: glob
globs: "**/engine/**/*.kt, **/model/**/*.kt, **/content/**/*.kt, **/data/**/*.kt"
description: Protect idle-game economy precision, determinism and persistence invariants.
---

# Economy/state invariants

- Never allow NaN/Infinity/negative balances from legal actions.
- Centralize formulas/constants.
- Use stable decimal/big-number handling for compounding quantities.
- Bulk/MAX must be bounded and performant.
- Foreground time uses monotonic clock; offline wall time clamps to [0, 12h].
- Prestige preview and apply share one calculation source.
- Save changes are versioned/migrated.
- RNG-dependent systems persist enough RNG state to remain reproducible/debuggable.
