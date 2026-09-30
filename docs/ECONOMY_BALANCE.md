# Economy and Balance Specification

## Numeric representation

Use `BigDecimal` (with a bounded `MathContext`, e.g. DECIMAL128) for economy magnitudes that can compound dramatically: BTC, USD, hashrate, prices, costs and production. Use primitive `Double` only for bounded presentation/physical ratios such as temperature, animation progress and percentages.

All economic helper functions must reject/normalize negative or invalid inputs.

## Miner cost curve

Default geometric curve:
`cost(n) = baseCost × growth^owned`

Bulk cost uses a geometric-series helper rather than looping thousands/millions of times. MAX purchase uses logarithmic/closed-form estimate plus bounded correction to avoid hangs.

Each miner can override growth if required for pacing, but overrides remain data, never conditional UI logic.

## Production

Each miner contributes:
`owned × baseHashrate × minerUpgradeMultiplier × milestoneMultiplier`

Global effective hashrate applies power, heat, event and prestige modifiers exactly once.

## Manual taps

Manual taps begin relevant and naturally fade. Tap strength may be a base value plus a small percentage of automated production unlocked later, so tapping never becomes completely meaningless if the player enjoys it.

## Market

Store:

- current price;
- trend state;
- volatility;
- RNG seed/state;
- recent display history;
- observed high/low stats.

Price update must enforce a positive configured floor and a sane configured ceiling. No real BTC API.

## Prestige

Recommended shape:
`points = floor((lifetimeBtc / prestigeBase) ^ prestigeExponent)`
where `0 < prestigeExponent < 1` initially.

Tune `prestigeBase` and `prestigeExponent` through simulation. Never duplicate this formula in UI.

## Balance simulation requirement

Before release, add deterministic JVM simulations that model representative strategies:

- tap-heavy beginner;
- balanced purchaser;
- hold-BTC market timer;
- frequent seller;
- early prestige;
- delayed prestige;
- 12-hour offline return.

Simulation checks:

- no progression dead-end;
- no single cheap miner dominates forever;
- no power/cooling upgrade becomes permanently useless;
- first prestige not absurdly early/late;
- prestige bonus actually accelerates rebuild;
- 12h offline reward is meaningful but does not skip the whole game;
- no unbounded instantaneous feedback loop.

## Economy invariants to test

- balance >= 0 after legal actions;
- purchase cost > 0;
- production >= 0;
- market price > 0;
- sale cannot sell more BTC than owned;
- bulk purchase cannot buy more than affordable;
- prestige reward monotonic with lifetime BTC;
- modifier application order deterministic;
- all persisted economy values deserialize safely.
