# Save compatibility and upgrade contract

Preservation is mandatory. The baseline is commit `33787bc4fa61ea4f78de06b7b81dd4094371cf39` on v1.0; schema version 1. No forced new game, app ID change, storage rename or uninstall workaround.

## Preserve valid player data

Bitcoin/Cash balances, miner IDs/counts, purchased ordinary upgrades, infrastructure tiers, achievements, Satoshi balance/nodes, cumulative prestige awards/lifetime statistics, daily day/cooldown, settings, onboarding completion, market and valid RNG state. Keep existing stable content IDs. Fix the upgrade's bad `gpu_6x_rig` target to the actual miner `gpu_rig_6x`; do not rename the saved upgrade ID `gpu_6x_custom_os` or legitimate miner IDs.

Future production rates/prices follow the new balance. Preserve assets, not faulty prior constants. Centralize manual baseline: detect/migrate the legacy 10/50,000 baseline representations while preserving purchased multipliers and permanent bonuses. Never arbitrarily shrink a legitimate asset balance because it exceeds new tuning targets. Saved custom/modified strengths need explicit safe handling, not a blind reset of every string.

## Schema-1 field-by-field migration contract

Apply these rules to valid schema-1 values. Migration is distinct from a deliberate player-confirmed prestige/reset. Validate malformed fields individually and retain the original recovery payload; a bad timer is not permission to reset known assets. Missing optional fields receive documented safe defaults.

| Persisted field | Upgrade rule |
|---|---|
| `schemaVersion` | Advance 1 to the new supported schema only after successful migration; reject unsupported future schemas without overwriting them |
| `btc`, `usd` | Preserve decimal balances exactly; do not clamp legitimate large assets to new tuning targets |
| `manualHashStrength` | Apply the documented 10/50,000 baseline reconciliation, preserving purchased/permanent multipliers; retain ambiguous custom values for safe explicit handling |
| `miners` | Preserve stable IDs and counts; quarantine unknown IDs for recovery rather than deleting known ownership |
| `purchasedUpgrades` | Preserve IDs/ownership, including `gpu_6x_custom_os`; correct its target, not its saved ID |
| `powerGridTier`, `coolingTier` | Preserve valid owned tiers; new formulas may change efficiency, not ownership |
| `marketPrice`, `marketTrend`, `marketHistory` | Preserve valid simulated price, trend and history; validate bounds without an arbitrary fresh-market reset |
| `marketTimerSeconds`, `eventTimerSeconds` | Preserve valid elapsed/countdown phase; if a revised interval requires adjustment, use an explicit bounded conversion and test it rather than restarting every save |
| `autoSellEnabled` | Preserve enabled/disabled state when its prerequisite upgrade is owned; disable execution safely when locked and explain why |
| `autoSellThresholdUsd` | Preserve the valid configured threshold across migration and prestige; new settings must not silently replace it |
| `activeEvents` | Preserve known events and their `eventId`, `expiresAtWallMillis`, `multiplier`, `powerModifier`, `heatModifier` and `isWindfall`; expire naturally by wall time, apply offline modifiers only until actual expiry, and never re-award an already-triggered windfall |
| `achievements` | Preserve earned IDs/awards; do not replay one-time rewards during migration |
| `satoshiPoints`, `purchasedPrestigeNodes` | Preserve spendable points and owned permanent nodes separately from cumulative earned points |
| `dailyRewardDay`, `lastDailyClaimWallMillis` | Preserve cycle position and claim cooldown; an update cannot make a claimed reward claimable again |
| `stats` | Preserve every existing lifetime counter and extremum listed below; initialize new counters with honest historical coverage |
| `onboardingCompleted` | Preserve completion; derive newly revealed systems from owned progress rather than forcing the old tutorial |
| `settings` | Preserve `soundEnabled`, `hapticsEnabled`, `reducedMotion` and `numberFormat`; apply defaults only for newly added settings |
| `lastSaveWallMillis` | Retain the old timestamp until bounded offline production has been computed and credited durably exactly once; do not replace it with migration time and discard the gap |
| `rngSeed` | Preserve valid persisted RNG state and verify deterministic continuation across save/reload; do not reseed every migrated session |

Existing `stats` fields are `lifetimeBtcMined`, `lifetimeUsdEarned`, `peakHashrate`, `totalManualTaps`, `totalMinersPurchased`, `totalUpgradesPurchased`, `totalBtcSold`, `highestPriceObserved`, `lowestPriceObserved`, `totalPrestiges`, `lifetimeSatoshiPointsEarned`, `totalPlaytimeSeconds` and `totalEventsTriggered`. Preserve recorded values even when old code undercounted playtime; do not invent lost historical seconds.

Clamp negative/future/invalid offline gaps defensively to the documented 0–43,200 seconds and retain recovery evidence for invalid clocks. Any positive eligible gap produces earnings, including gaps below the 60-second summary threshold. Advance the timestamp with the credited save atomically so reload or interruption cannot duplicate or lose the claim. Expired events may leave the active list after their valid contribution is integrated; unexpired events keep their remaining duration.

Prestige deliberately disables enabled auto-sell while its ordinary prerequisite is reset, but preserves the configured threshold. These reset semantics never justify clearing valid auto-sell configuration or event state during an app upgrade.

## Schema and new fields

First v1.2 persisted additions use schema 2 unless a newer schema already exists at implementation time. Migration is idempotent and pure until the durable write succeeds. App version 1.2.0 does not imply schema 1.2. New stats/discovery/teaching fields have safe defaults; derive unlocks from preserved assets. Mark new source attribution as tracked since v1.2; lifetime totals must not be reconstructed from guesswork.

No actual pre-v1.2 source attribution/playtime can be inferred from current balances. Keep legacy totals and represent unavailable historical coverage honestly. The new per-source counters need not sum to lifetime totals for migrated saves; explain this in Stats.

## Recovery

Before replacing an unsupported future-schema or genuinely corrupt payload, retain raw bytes in a bounded app-private recovery checkpoint and report a clear recovery state. Never log save contents into release logs. A valid old payload must migrate, not silently fall into new defaults. Unsupported future schema is not authorization to rewrite it as schema 2; refuse mutation safely and preserve original data. If old storage contains unknown content IDs, retain them for recovery and exclude them safely from calculations; do not delete valid known assets.

## Fixtures and tests

Capture real schema-1 payload shape through the old serializer; keep anonymized deterministic fixtures for new/early/mid/late/prestiged saves, claimed daily reward, disabled sound/haptics, missing optional fields and large values. No personal data or signing keys in fixtures.

Assert every table field and nested stats/settings/event field using distinctive fixture values; test valid enabled and disabled auto-sell, custom thresholds, fractional timer phases, unexpired/expired events, already-awarded windfalls, non-default RNG and short/long offline gaps. Test migration twice; encode/decode round trip; unchanged balances/counts; purchased GPU upgrade now affecting its real target; correct derived facility/unlocks; unsupported version/corruption checkpoint; interruption before/after save commit; repeated offline/daily/prestige confirmation; old content ownership plus new upgrades; app process death during critical transactions.

Prestige/award migration must preserve cumulative point bookkeeping independently of currently spendable Satoshi Points. Daily/event point grants must not make the new prestige curve double-award or irreversibly stall progress; test both legacy and new cases.

## In-place APK proof

On an emulator, install the v1.0 baseline APK using the same signing certificate as the candidate, build a meaningful saved profile, then update with `adb install -r <candidate.apk>`. Never clear app data or uninstall during this preservation journey. Verify before/after asset snapshots, teaching state, settings, relaunch and process death.

If the original user's installed APK signing key is unavailable, internal fixture and same-key emulator update tests can pass but the user's real in-place update remains blocked. Report that exact constraint; do not generate a new key and claim old install compatibility. Physical phone testing is not required for this project.

## Rollback

Source rollback uses preserved version branches. Installing old code over a migrated schema is not automatically safe. Prefer a forward fix; old app versions must not overwrite a newer unsupported payload. Keep a migration checkpoint locally; never promise a downgrade restores schema-1 data without an explicitly verified restoration path.
