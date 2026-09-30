# Optional Future Google Play Preparation

> **Not part of the current build or release gate.** Use this document only when you decide to publish on Google Play.

## Positioning

Describe the product as an **idle simulation game**. Never imply real mining, investment returns, exchange functionality, or affiliation with Bitcoin developers/organizations.

## Store assets

Use human-designed/vector/gameplay-based assets. Do not use AI-generated promo imagery for this project unless the product decision is explicitly changed.

## Data safety intent

The architecture should collect/share no user data and use no backend/analytics/ads. Verify the final binary/dependencies before completing Play Console declarations.

## Target SDK

Use the newest stable target supported by the stable toolchain. The project design currently expects API 37 while retaining minSdk 31.

## Release artifact

Prefer signed Android App Bundle for Play distribution. Keep signing secrets outside Git. Add keystore properties to `.gitignore`.

## Policy audit

Run the official `play-policy-insights` Android skill before upload and address findings rather than blindly suppressing them.
