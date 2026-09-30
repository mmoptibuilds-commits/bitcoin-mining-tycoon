# Bug Hunt Checklist

Run after each milestone and again before release.

## Gameplay correctness

- Buy exactly at price boundary.
- Buy just below price boundary.
- Bulk MAX with tiny, medium and absurd balances.
- Sell 10/50/MAX with fractional BTC.
- Zero BTC sale.
- Multiple multipliers combined.
- Event expires mid-tick.
- Upgrade purchased during event.
- Prestige when barely eligible and extremely over-eligible.

## Time

- background 1 second;
- background 30 minutes;
- 11h59m;
- exactly 12h;
- >12h;
- wall clock moved backward;
- wall clock moved years forward;
- process killed while backgrounded.

## State/lifecycle

- rotate blocked by portrait policy where intended;
- task removed then relaunched;
- OS process kill;
- low-memory recreation where reproducible;
- rapid tab switching;
- app background during dialog;
- app background immediately after purchase/prestige.

## Interaction

- rapid 10+ taps/sec;
- double-tap purchase;
- press buy while insufficient;
- tap disabled/locked item;
- back during modal;
- repeated daily-reward tap;
- repeated offline-collect tap.

## UI/UX

- smallest supported phone viewport;
- tall phone;
- cutout/status/nav insets;
- font scale 1.5;
- TalkBack traversal;
- reduced motion;
- large late-game values;
- price negative trend vs positive trend without color reliance.

## Release-only

- minified release launch;
- serialized save works after R8;
- no missing reflection/serializer rules;
- app restart after upgrade from previous save schema fixture;
