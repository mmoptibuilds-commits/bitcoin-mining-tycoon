---
trigger: always_on
description: Production Android implementation constraints for Bitcoin Mining Tycoon.
---

# Android production rules

- Native Kotlin + Jetpack Compose only.
- Stable dependencies only.
- minSdk 31; current stable compile/target SDK supported by stable toolchain.
- Single app module by default.
- No network/backend/ads/IAP/analytics.
- No INTERNET permission.
- UI logic separated from engine/persistence.
- No unbounded per-frame persistence writes.
- No TODO placeholders in release paths.
- Every user-visible feature must have applicable tests and on-device verification before completion.
