---
trigger: always_on
description: Deterministic economy, migration and transaction invariants.
---

# economy-safety

Read ECONOMY_BALANCE and SAVE_COMPATIBILITY for engine/model/content/data changes. Centralize balance values; use bounded decimal economy magnitudes, bounded MAX, stable IDs, versioned migration and serial critical transactions. Test legacy saves, RNG/time equivalence, repeated claims, prestige cumulative accounting, fractional time, event expiry and huge values. Offline max43,200 seconds. No assets lost to tuning or silent valid-save fallback.
