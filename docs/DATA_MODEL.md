# Data Model

## GameState (conceptual)

```text
GameState
- schemaVersion
- economy
  - btc
  - usd
  - manualHashStrength
  - currentBtcPrice
- miners: Map<MinerId, OwnedMinerState>
- purchasedUpgrades: Set<UpgradeId>
- powerState
- thermalState
- marketState
- activeEvents
- achievements
- prestigeState
- dailyRewardState
- stats
- onboardingState
- settings
- timing
- rngState
```

## Content definitions

Definitions are immutable app content, not stored redundantly in saves:

- `MinerDefinition`
- `UpgradeDefinition`
- `AchievementDefinition`
- `GameEventDefinition`
- `PrestigeNodeDefinition`

Persist stable IDs only. Never persist UI display strings as identity.

## Save compatibility

If a content item disappears in a later build, unknown IDs must be ignored safely, logged in debug builds, and never crash deserialization.

## Transaction semantics

All player economic actions validate preconditions then apply one atomic state transition:

- buy miner(s);
- buy upgrade;
- sell BTC;
- claim daily reward;
- collect offline reward;
- prestige;
- buy prestige node;
- reset game.
