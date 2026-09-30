# Economy and balance contract — v1.2

## Source and numeric rules

Preserve engine boundaries and move duplicated balance values into one `content/BalanceConfig.kt` or an equivalent single tested configuration. Decimal magnitudes use existing bounded GameNumber/BigDecimal helpers. Doubles are for bounded ratios/temperature/animation, never an unchecked conversion of late-game prestige magnitude. Bulk costs are geometric; MAX estimation has bounded correction.

Initial values are tuning inputs, not requirements to preserve. Keep stable content IDs and player assets. At baseline GameState defaults manualHashStrength to 50,000, GameSave to 10, and prestige to 10; fresh loaded state and reset can therefore differ. Reproduce the real launch/reset paths before asserting a universal 50,000-hash tap economy.

## First-run targets

| Milestone | Reference active strategy target |
|---|---|
| Visible first Bitcoin | first valid tap |
| Understand loop | within 60 seconds |
| First sale / first machine | 30–90 seconds |
| Passive output exceeds manual contribution | by minute 5 |
| First GPU-class hardware | 3–6 minutes |
| Infrastructure first matters | 7–12 minutes |
| First ASIC-class hardware | 12–20 minutes |
| First prestige eligibility | 25–35 minutes |
| Second comparable prestige run | at least 25% faster after spending the first permanent bonus |

First run teaches CPU/GPU/ASIC and infrastructure; it need not reach all sci-fi tiers. Late tiers take multiple runs/sessions. Eligibility and perceived enjoyment are separate: do not claim fun from a simulation alone.

## Reproducible simulation

Use the actual production engines and injected clock/RNG, not a duplicate economy spreadsheet. Reference policy: 2 taps/second for first 90 seconds; then 5-second bursts at 2 taps/second once per minute through minute 10; no tapping after minute 10. Sell at least every 15 simulated seconds when cash is needed. Every 5 seconds choose the affordable unlocked purchase with best marginal effective-production/cost benefit, including power/cooling/ordinary upgrades. For unlocked alternatives with positive future benefit, allow bounded saving toward the next tier rather than buying a cheap tier forever. Record deterministic tie-breaking and the executed purchase log. This policy is a reference, not a secret player requirement.

Use fixed normal price and no random bonuses for the reference; also run at least five published RNG seeds with the real market/events. Record seeds, policy, config hash, milestone times, tap/passive contributions and choices. Zero daily rewards, rare windfalls or critical upgrades may be required for base success. Target 25–35 minutes on the reference; report seeded variance and investigate any 15-minute stall or unreachable milestone instead of claiming every strategy has the same timing.

Additional policies: low-tap beginner, frequent seller, market holder, infrastructure-aware buyer, delayed prestige, two/three consecutive prestiges, no daily reward, daily reward claimed, 12-hour offline return, legacy early/mid/late saves. A slow/idle strategy may exceed 35 minutes; it must still have reachable progress without compulsory tapping or a lucky event.

## Rewards and temporary modifiers

Reveal first daily reward after the first machine. Starter cash reward is at most 30% of initial machine cost; later cash/Bitcoin rewards scale toward 30–90 seconds of current effective production value with bounded stage-aware caps. Keep existing 7-day/20-hour eligibility and cumulative cycle; do not revoke rewards already claimed. A day-seven permanent-point reward must remain separately accounted for, so it cannot corrupt prestige-derived claim bookkeeping.

Critical-tap upgrade: 2% chance of 2× manual output, using persisted deterministic RNG. Cosmetic combo has no multiplier. Temporary boosts are one modifier source with explicit duration and no duplicate application. Offline intervals must split at boost/event expiry or use an equivalent mathematically correct integration; never keep an expired bonus for 12 hours.

## Required invariants

- Valid actions never produce negative balances, invalid prices or nonfinite magnitude.
- Fresh/load/reset/prestige share a documented baseline plus legitimate permanent bonuses.
- First hardware is worth buying; later tiers and infrastructure have useful marginal benefits.
- Existing owned assets still function after rebalance; no migration forced reset or inaccessible remedy.
- Prestige earned reward is monotonic, preview equals apply, repeated confirmation cannot award twice, spending does not reduce cumulative already-awarded history.
- Daily/event grants and production source stats have explicit attribution; no accidental prestige double count.
- No instantaneous self-financing loops or infinite MAX searches.
- Offline cap remains 43,200 seconds even with offline upgrades.
- Economy output is equivalent at different tick/frame rates within documented rounding tolerances.

## Tuning and evidence

Change configuration/data before architecture; keep the simulation policy stable. Save before/after reports under ignored `artifacts/`, summarize checked results in CURRENT_STATE, and commit reviewed config/tests together. If the reference misses targets, iterate until it passes or provide the exact remaining blocker; do not loosen the target simply to turn a report green.
