# UX Specification

## UX principles

- The player should always know: **what am I producing, how fast, what can I buy next, and why can't I buy something?**
- The first screen prioritizes action, not explanation.
- Failures explain how to recover.
- Destructive operations require confirmation; normal repetitive purchases do not.

## Mine screen hierarchy

1. BTC balance and USD balance.
2. Simulated BTC price/trend and quick sell.
3. Hashrate + BTC/sec.
4. Primary MINE control.
5. Power / heat / efficiency status.
6. Active event/boost.
7. Next meaningful unlock or recommendation (deterministic, not AI-generated).

## Hardware screen

- Group by tier/unlock progression.
- Each row/card: name, owned, production, cost, power/heat, affordance.
- Locked items explain unlock requirement.
- Bulk selector remains sticky or easily reachable.
- Disabled purchase explains insufficient USD/power if needed.

## Upgrades screen

Segmented into focused operational tracks:
- Power Grid Infrastructure (10 progressive stages from House Outlet to Dyson Network);
- Thermal Cooling Infrastructure (7 progressive stages from Desk Fan to Quantum Thermal);
- Hardware Family & Global Upgrades;
- Automation & Market mechanics;
- Satoshi Prestige Portal entry (navigates to full-screen Satoshi Tree destination).

Clearly distinguish available, purchased, locked, and permanent states. Never show dozens of equal-weight cards without grouping.

## Stats screen

Prioritize meaningful summary. Use disclosure/sections for long stats instead of one enormous wall.

## Feedback patterns

- Purchase: immediate visual state + short sound/haptic.
- Invalid purchase: no destructive toast spam; brief inline/snackbar explanation.
- Achievement: non-blocking banner.
- Event: banner with timer and effect.
- Prestige: full confirmation dialog/sheet with exact reset/preserve list.
- Offline return: summary sheet once, idempotent collection.

## First-run UX

No more than 30–45 seconds of forced teaching. Prefer coach marks that appear when a feature first unlocks. Persist each coach mark so it does not repeat.

## Accessibility

- 48dp minimum target.
- Screen reader labels describe actions (`Sell 50 percent of BTC`) not icon shapes.
- Changing values should not continuously spam accessibility announcements.
- Important event/achievement announcements use appropriate live-region behavior sparingly.
- At 1.5 font scale, primary actions and critical values remain usable without clipping.

## Back behavior

- Root tabs do not create endless duplicate back-stack entries.
- Back from secondary/settings returns predictably.
- Back from root follows Android conventions; do not trap the user.

## Error/edge states to design explicitly

- new game/zero balance;
- no affordable miner;
- power deficit;
- high heat;
- market crash;
- no active event;
- prestige unavailable;
- daily reward already claimed;
- offline duration zero;
- corrupted save recovered;
- sound/haptic unavailable on device.
