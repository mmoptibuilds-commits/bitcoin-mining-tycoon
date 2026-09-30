---
name: bug-hunt
description: Reproduces v1.2 state, lifecycle, economy, save, UI and release defects with native evidence.
---

# bug-hunt

Read TEST_STRATEGY adversarial cases/matrix, CURRENT_STATE and relevant requirements. Separate source concerns from confirmed failures. Reproduce through meaningful tests/emulator flows, identify root cause, add a regression where feasible, fix in the single writer's scope and rerun affected checks/journey. Include legacy update/corruption, rapid-action races, fractional time/RNG/offline expiry, huge values, native/accessibility/icon/splash and minified release. Never clear old data or weaken tests to finish. Return severity-ranked findings with evidence; block completion on real crash/data loss/incorrect economy/unusable core flows.
