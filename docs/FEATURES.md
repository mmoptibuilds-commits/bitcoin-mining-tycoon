# Feature Catalogue and Acceptance Criteria

Every feature below is in V1 unless explicitly marked optional. Implement in the order from `IMPLEMENTATION_GUIDE.md`.

## F01 — First launch and onboarding

- 2–4 concise pages max, or a single progressive overlay.
- Explains tap → mine → sell → buy → automate → prestige.
- Shows the fictional-simulation disclaimer.
- Can be skipped.
- Completion persisted.
- Never blocks returning users.

## F02 — Manual mining

- Large primary Mine control.
- Tap increases manual work contribution immediately.
- Visual number-pop feedback is restrained and can honor reduced motion.
- Haptic feedback respects settings.
- Rapid taps cannot cause state corruption or duplicate non-tap actions.

## F03 — Passive mining engine

- Aggregates owned hardware and modifiers into total hashrate.
- Converts hashrate to BTC production through a centralized formula.
- Uses elapsed delta time; no frame-count dependence.
- Continues correctly through UI recomposition.

## F04 — BTC and USD economy

- BTC is mined; USD is acquired by selling BTC.
- No real-money integration.
- Balances never become negative from valid actions.
- Formatting supports very large magnitudes.

## F05 — Simulated BTC market

- Local pseudo-random market with trend, volatility and events.
- Never produces invalid/negative prices.
- Price history is gameplay-only; avoid fake-real-world styling.
- Sell controls: 10%, 50%, MAX.
- Optional automation unlock can auto-sell according to a rule.

## F06 — Hardware catalogue

20 launch tiers:

1. Ancient CPU
2. Gaming CPU
3. Gaming GPU
4. Dual GPU Rig
5. 6× GPU Rig
6. Entry ASIC
7. Industrial ASIC
8. ASIC Rack
9. Server Room
10. Mining Warehouse
11. Mining Farm
12. Hydro Mining Facility
13. Geothermal Mining Complex
14. Nuclear Mining Campus
15. Immersion-Cooled Megafarm
16. Fusion Mining Complex
17. Orbital Solar Miner
18. Lunar Mining Array
19. Quantum Hash Facility
20. Dyson Hash Swarm

Each miner has ID, name, description, base cost, cost-growth rule, base hashrate, power, heat, unlock threshold, icon/vector motif, owned count, milestones, and upgrade hooks.

## F07 — Bulk purchase

- x1, x10, x25, MAX.
- Exact affordable count calculated before mutation.
- One atomic state update per purchase.
- MAX cannot hang on enormous balances.

## F08 — Power system

Progression examples: House Outlet → Commercial Grid → Industrial Grid → Solar Farm → Hydro → Geothermal → Nuclear → Fusion → Orbital Solar → Dyson Energy Network.

- Capacity gates expansion or scales efficiency.
- Never randomly destroys purchased hardware.
- Status clearly communicates capacity and deficit.

## F09 — Heat and cooling

Cooling progression: Desk Fan → AC → Industrial HVAC → Liquid → Immersion → Cryogenic → Quantum Thermal Management.

- Heat affects efficiency through a documented curve.
- Heat state is understandable from dashboard.
- No permanent hardware loss.

## F10 — Upgrade system

Target 40–60 data-driven upgrades across:

- tapping;
- global hashrate;
- hardware families;
- power efficiency;
- cooling;
- market automation;
- offline earnings;
- prestige acceleration.

Upgrades are purchased once unless explicitly repeatable.

## F11 — Achievements

Target 35–50 achievements.

- One-time unlock.
- Persistent.
- Toast/snackbar/banner is non-blocking.
- Some may provide small documented permanent bonuses.

## F12 — Random events

Positive, negative and rare events with durations and modifiers.
Examples: Bull Run, Cheap Electricity, Lucky Block, ASIC Breakthrough, Cooling Weather, Market Crash, Heat Wave, Grid Failure, Difficulty Spike, Perfect Block.

- Event stacking rules are explicit.
- Expiration is deterministic.
- No event can make core state invalid.

## F13 — Prestige

- Prestige reward based primarily on lifetime BTC mined.
- Preview shows exactly what resets/what persists/reward to receive.
- Confirmation required.
- Awards Satoshi Points.
- Permanent bonus tree persists.
- Preview and actual reward must share the exact same calculation function.

## F14 — Satoshi Point tree

Example nodes: Efficient Silicon, Diamond Hands, Cold Start, Cheap Energy, Automation, Industrial Memory, Quantum Legacy.

- Dependencies/prerequisites visible.
- Spending is atomic.
- Cannot buy locked/unaffordable node.

## F15 — Offline earnings

- Maximum 12 hours.
- Negative wall-clock delta = zero earnings.
- Extreme future clock = clamp to 12 hours.
- Offline summary shows duration, BTC earned and average effective hashrate.
- Collecting is idempotent.

## F16 — Daily reward

- 7-day local cycle.
- Reward claim is idempotent per eligible day.
- Clock anomalies never crash/corrupt state.
- No push notification requirement.

## F17 — Statistics

Track at minimum: current/lifetime BTC, current/lifetime USD, current/highest hashrate, taps, tap-derived BTC, passive BTC, miners purchased, upgrades, power, heat peak, playtime, offline earnings, BTC sold, observed price high/low, events, achievements, prestiges, lifetime Satoshi Points.

## F18 — Save/persistence

- Versioned game save.
- Atomic writes.
- Autosave after critical transactions + periodic coalesced save.
- Save on lifecycle background transition.
- Corrupt/missing optional fields recover to safe defaults.

## F19 — Settings

- Sound effects.
- Haptics.
- Reduced motion.
- Compact/scientific number preference.
- Balanced/battery-friendly animation mode if useful.
- Reset game with destructive confirmation.

## F20 — Audio/haptics

Original lightweight effects only: tap, buy, invalid, achievement, rare event, prestige, daily reward.
No background music in V1.

## F21 — Accessibility

- 48dp minimum interactive targets.
- Meaningful semantics/content descriptions.
- Logical TalkBack order.
- 1.5x font scale screenshots/inspection.
- Contrast not dependent on OLED black alone.
- Color never sole state channel.

## F22 — Release hardening

- R8 minification and resource shrinking on release.
- No INTERNET permission.
- App icon/splash original and vector-based.
- Version name/code.
- Signed release instructions.
- No debug logging/secrets.
- Data Safety compatible with local-only/no-data-collection design.
