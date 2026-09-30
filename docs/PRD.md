# Product Requirements Document — Bitcoin Mining Tycoon

## 1. Product summary

Bitcoin Mining Tycoon is a single-player native Android idle/clicker game inspired by the clarity and compounding progression of Cookie Clicker. The player starts with negligible compute, taps to generate manual hashing power, buys automated mining hardware, manages power and heat, mines fictional in-game BTC, sells it into a simulated local market for USD, buys upgrades, completes achievements, earns offline progress, and eventually prestiges into permanent Satoshi Point bonuses.

## 2. Product goals

- Be understandable within 30 seconds without a tutorial wall.
- Make every early purchase visibly change production.
- Provide enough interconnected systems for long-term progression without turning into spreadsheet maintenance.
- Feel polished and native on Android 12+.
- Work completely offline and resume safely after process death.
- Be playable for minutes at a time or left idle for up to 12 hours.
- Reach release-candidate quality in the initial one-shot build.

## 3. Non-goals

- Real cryptocurrency mining.
- Real BTC price feeds.
- Wallet creation/import.
- Blockchain interaction.
- Real trading or financial advice.
- Multiplayer/leaderboards.
- Accounts/cloud sync.
- Ads/IAP/subscriptions.
- Backend services.
- Landscape gameplay.

## 4. Target audience

Casual clicker/idle-game players and tech enthusiasts who enjoy escalating numbers, hardware progression, upgrade trees, optimization, achievements, and prestige loops.

## 5. Platform

- Android 12+ (API 31+).
- Portrait phone experience is primary.
- Must behave correctly on common compact/large phones and display cutouts.
- Target current production Android SDK supported by stable tooling.

## 6. Core player loop

1. Tap `MINE` to bootstrap manual hashes.
2. Convert hash production into simulated BTC over time.
3. Hold or sell BTC into a fictional USD market.
4. Spend USD on miners, power, cooling, and upgrades.
5. Increase automated hashrate and efficiency.
6. Respond to random market/operations events.
7. Complete achievements and milestone bonuses.
8. Prestige when worthwhile to earn Satoshi Points.
9. Rebuild faster with permanent bonuses.

## 7. Primary screens

### Mine

Live BTC/USD balance, market price, total hashrate, BTC/sec, primary mine interaction, power, heat, efficiency, active event/boost, quick sell controls, and concise next-goal cues.

### Hardware

Data-driven catalogue of miners grouped by progression tier with owned count, per-unit output, total output, price, power/heat footprint, unlock state, and bulk purchase controls `x1`, `x10`, `x25`, `MAX`.

### Upgrades

Global upgrades, miner-specific upgrades, power/cooling improvements, automation, and prestige tree entry points.

### Stats

Current/lifetime production, purchases, market stats, events, taps, offline earnings, achievements, prestige history, and playtime.

### Settings

Sound, haptics, reduced motion, number formatting, performance mode, legal/game disclaimer, version, reset progress.

## 8. Key product requirements

- Passive income continues while app is active.
- Offline earnings are calculated on next launch/resume and capped at 12 hours.
- Save writes are atomic via DataStore and resilient to corruption/defaulting.
- All gameplay is local.
- Fake market affects the USD received when BTC is sold.
- Power/cooling constrain growth but do not permanently destroy hardware.
- Prestige resets temporary progression while preserving documented permanent data.
- Achievements are deterministic and cannot repeatedly grant one-time rewards.
- Daily reward is local and robust against simple date anomalies; it is not a security boundary.
- Sound and haptics can be disabled independently.
- Reduced motion affects non-essential animations.

## 9. Safety/clarity requirements

Show this message during onboarding and in Settings:

> Bitcoin Mining Tycoon is a fictional simulation game. It does not mine cryptocurrency, connect to a wallet or blockchain, execute trades, or provide financial services or advice.

Do not display copy that implies guaranteed profit or real-world earnings.

## 10. Success criteria for V1

- New install reaches first automated miner within a short first session.
- The user can understand which resource buys what without external documentation.
- No known crash, save-loss, negative-balance, NaN/Infinity, impossible-purchase, or prestige corruption defects.
- Core flows pass unit, Compose UI, and on-device Journey tests.
- Release build completes with minification/resource shrinking enabled.
- App remains responsive during rapid tapping and long idle sessions.
