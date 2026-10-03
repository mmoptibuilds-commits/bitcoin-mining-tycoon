# v1.2 product requirements

Status: approved; v1.2 implementation is integrated into `main` and retained on `v1.2`; public release acceptance remains partial. Updated 2026-10-03. See `CURRENT_STATE.md` for tested behavior and remaining release gates.

## Product

A single-player portrait Android idle/clicker game about turning a salvaged computer into an absurd sci-fi mining empire. Every purchase should make that empire feel larger. The first minute teaches the game loop through doing, not cryptocurrency lessons.

## Goals

1. Understand tap → earn Bitcoin → sell for cash → buy a machine → automatic income within one minute.
2. Make automation visibly valuable; tapping bootstraps growth and becomes optional support.
3. Replace the monitoring-console feel with an evolving facility, readable controls and purposeful feedback.
4. Reach the first prestige around 30 minutes under the reproducible reference strategy; retain multi-session late-game discovery.
5. Preserve current player progress through a normal app update.
6. Ship a polished, minified, emulator-verified sideload APK with an original launcher icon and coherent launch experience.

## Locked constraints

Native Kotlin/Compose; minSdk 31; portrait; one module; DataStore local save; no network/backend/accounts/ads/IAP/analytics/real crypto; 12-hour offline cap; no background mining service; procedural sound effects without background music; original Canvas/vector visuals; graphite/copper identity. Android Studio emulators are the test surface; no phone USB debugging requirement.

Existing application ID, storage identity and compatible signing must remain. Current compile/target SDK is 36; toolchain upgrades are not a redesign prerequisite. App release target is versionName `1.2.0` with versionCode greater than every existing install; save schema version is independent.

## Player experience

Mine is the main facility/home. A clear Mine control, balances, production and next goal surround an evolving scene. Hardware and Upgrades are easy to visit and return from. Stats, achievements, settings and version/about information are utility destinations. Market, power, cooling, rewards, events and prestige appear when relevant.

Gameplay depth stays: 20 hardware tiers, bulk buying, simulated market/auto-sell, power/cooling, upgrades, events/windfalls, achievements, daily rewards, offline earnings and Satoshi prestige/tree. Detailed requirements and tests are in FEATURES and TEST_STRATEGY.

## Success evidence

- A clean-profile emulator flow performs mining, sale and first purchase without crypto knowledge or a tutorial wall.
- Reference simulation reaches first prestige in 25–35 minutes without daily rewards, rare events or nonstop tapping.
- Passive production overtakes manual production by minute 5 under that reference strategy.
- Legacy fixtures and an in-place v1.0 → v1.2 upgrade retain all documented player assets and permanent data.
- UI passes compact/tall layouts, 1.5 font scale, TalkBack and reduced motion; launcher/splash have no Android template artwork.
- Required JVM/Compose/emulator/release checks have command-output evidence. CURRENT_STATE distinguishes implemented, verified and blocked work.

## Scope exclusions

No multiplayer, leaderboards, cloud save, notifications, background service, real prices, account setup, store publishing, 3D engine or generated artwork. Google Play publishing and a user-facing save export/import feature require separate future scope. This redesign does not erase saves or rename the package for branding.

## Simulation wording

On first launch and in Settings, state concisely that this is a fictional game and cannot mine or trade cryptocurrency or generate real money. Keep the notice readable and calm; ordinary gameplay labels need no repeated legal paragraphs.
