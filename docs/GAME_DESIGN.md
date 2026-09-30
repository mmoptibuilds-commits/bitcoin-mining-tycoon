# Game Design Document

## Design pillars

1. **Immediate causality:** every tap/purchase should visibly affect a number the player cares about.
2. **Automation fantasy:** manual tapping becomes progressively irrelevant as the mining empire scales.
3. **Meaningful decisions without punishment:** market timing, power/cooling choices and prestige timing matter, but mistakes do not permanently brick progression.
4. **Escalation:** bedroom PC → industrial facility → orbital/planetary absurdity.
5. **Readable complexity:** systems are deep but surface one clear next action.

## Resource model

- **BTC:** mined resource; can be held or sold.
- **USD:** primary purchasing currency.
- **Hashrate:** production capacity.
- **Power capacity/usage:** growth constraint.
- **Heat/thermal load:** efficiency constraint.
- **Satoshi Points:** permanent prestige currency.

## Tick model

Recommended engine tick: 4–10 logical updates/sec. UI animation may run independently. Production uses delta-time so low frame rate never reduces economy output.

## Production concept

`effectiveHashrate = rawHashrate × globalMultipliers × powerFactor × thermalFactor × eventFactor × prestigeFactor`

`btcMined = effectiveHashrate × btcPerHashCoefficient × deltaSeconds`

All constants live in balance configuration, not UI.

## Power behavior

Power is a soft constraint. If demand exceeds capacity, apply a transparent diminishing factor rather than silently turning random miners off. Example concept:
`powerFactor = min(1, capacity / demand)` with a floor only if required for game feel.

## Thermal behavior

Use bands or a smooth curve, with readable thresholds. Example initial bands:

- below 70°C equivalent: 100%
- 70–80: 90–100%
- 80–90: 75–90%
- 90+: 50–75%

No permanent damage.

## Market behavior

Use a saved seeded RNG and state machine:

- Neutral
- Bull
- Bear
- Volatile
- Crash/Pump event override

Keep the market fictional. The graph should be small and useful, not styled like a real exchange terminal.

## Prestige behavior

Prestige reward comes from lifetime BTC mined using a sublinear threshold curve. The engine must expose `previewPrestige(state)` and `applyPrestige(state)` where the latter consumes the preview result rather than duplicating the formula.

Prestige should feel worthwhile periodically, not every few minutes. Permanent points accelerate early/mid-game while preserving late-game progression.

## Pacing targets (tune with simulations)

- first purchase: tens of seconds, not minutes;
- first GPU-class automation: first short session;
- first ASIC: early meaningful milestone;
- first industrial site: later session/idle return;
- first prestige: after player has learned market/power/cooling;
- post-prestige rebuild: materially faster than first run.

Do not lock these as hard timings until automated economy simulations show sane progression.

## Event philosophy

Events create temporary optimization opportunities, not random game-over punishment.

- Positive events: clear benefit and duration.
- Negative events: reduced efficiency/price, never delete assets.
- Rare events: exciting enough to notice, not mandatory for progression.

## Number feedback

Early game can display exact-ish values. Mid/late game defaults to compact suffix/scientific formatting. Internally retain stable numeric precision separate from display formatting.
