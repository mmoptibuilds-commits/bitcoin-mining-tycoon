# v1.2 feature catalogue and acceptance contract

All F01–F25 are in scope. Existing systems must be retained and corrected rather than reconstructed unnecessarily. Each row maps to a milestone in PROJECT_PLAN and proof in TEST_STRATEGY.

## F01 — Progressive teaching and discovery

Teach Mine → Bitcoin → Sell → Cash → first machine → passive income through brief contextual cues. Initial screen emphasizes action; no forced slide deck. Persist dismissed/completed cues. Returning users retain onboarding completion and reveal systems implied by their existing assets. Power/cooling appears as demand becomes relevant; prestige guidance appears near eligibility. Locked controls always explain the next requirement.

## F02 — Manual mining and feedback

Immediate valid tap output; coherent defaults after fresh launch, load, reset and prestige. Press compression, number delta, bounded shockwave/particles and machine reaction; sound/haptics obey independent settings. Combo indication is cosmetic, never requires an exhausting tap streak. A later critical-tap upgrade may enable a deterministic 2% chance of 2× manual output; apply exactly once, persist RNG state, account for source statistics and simulate it separately. The base pacing never depends on critical taps.

## F03 — Passive production

Aggregate owned hardware and all documented modifiers once. Delta-time production is independent of screen refresh, recomposition and reduced-motion mode. Serialized actions cannot overwrite a simultaneous tick. Automation becomes meaningful within minutes; no background service.

## F04 — Bitcoin/cash balances and formatting

Bitcoin is mined; cash purchases hardware and upgrades. Keep simulated USD internally and label it Cash with $ in ordinary UI. Decimal-safe arithmetic, positive prices and exact affordability. Small early earnings remain visible rather than rounding to a misleading zero; late-game values stay legible in compact/scientific formats with stable number widths.

## F05 — Simulated market and selling

Positive bounded local price, persisted trend/history/RNG, useful game-state sparkline in an expandable market panel. Sell 10%, 50%, MAX with an accurate proceeds preview and immediate feedback. Auto-sell unlock, threshold setting and disabled/locked explanations. Auto-sell cannot operate without its required upgrade after prestige. No real exchange jargon, WebSocket claims or finance integration.

## F06 — All 20 mining tiers

Retain stable IDs for Ancient CPU, Gaming CPU, Gaming GPU, Dual GPU Rig, 6× GPU Rig, Entry ASIC, Industrial ASIC, ASIC Rack, Server Room, Mining Warehouse, Mining Farm, Hydro Mining Facility, Geothermal Complex, Nuclear Mining Campus, Immersion Megafarm, Fusion Complex, Orbital Solar Miner, Lunar Mining Array, Quantum Hash Facility and Dyson Hash Swarm. Each has readable benefit, costs, owned count, production and power/heat implications. Nearby locked tiers show requirements; distant tiers can be collapsed without hiding progression permanently.

## F07 — Bulk purchase

x1/x10/x25/MAX, correct geometric costs and affordable counts, one atomic update, bounded MAX computation at extreme magnitudes. Rapid repeated purchases cannot overspend. Power/cooling deficits reduce efficiency rather than introducing an unexplained hard purchase block.

## F08 — Power infrastructure

Retain ten ordered power stages. Demand/capacity/effect are clear when relevant; capacity upgrades have a visible production benefit. Soft efficiency constraint, no permanent loss or unrecoverable zero-output trap.

## F09 — Cooling infrastructure

Retain seven stages. Preserve deterministic equilibrium unless evidence justifies changing it. Show current heat, efficiency impact and the useful next remedy, without needing an engineering glossary. No hardware destruction.

## F10 — Organized upgrades

50–60 meaningful data-defined ordinary upgrades; target 56 if each has a tested effect. Group under Tapping, Compute, Infrastructure and Automation, with power/cooling subgroups. Show prerequisites, effect, price and purchased/available/locked states. No duplicate filler just to meet a count. Retain existing IDs and ownership. Prestige nodes are additional and are not counted toward this target. Offline/late-game improvements must stay inside the offline cap and economy contract.

## F11 — Achievements

Retain at least the existing 40, with one-time unlock/reward semantics, coherent descriptions and accessible progress. Non-blocking milestone banner; browsing in the stats/achievements utility. Add only meaningful gaps implied by new facility/content progression.

## F12 — Events and windfalls

Retain the ten existing events, explicit stacking/expiration rules and clear effects/duration. Negative effects reduce output/price temporarily without deleting assets. Interactive windfalls have reachable targets and expiry feedback. A saved temporary boost cannot apply over an entire offline window after expiry.

## F13 — Prestige

Preview and execution share one decimal-safe reward calculation. Show exact reward and reset/preserve lists; cancel is mutation-free; repeated confirmation cannot double-award. Eligibility targets 25–35 minutes in reference first run. Preserve cumulative award bookkeeping so changing the curve or spending points cannot duplicate awards. Disable locked automation on reset; support replay immediately.

## F14 — Satoshi tree

Retain the 12 existing permanent nodes and stable IDs. Dependencies, cost, effects and owned states are legible; spending is atomic. Permanent bonuses genuinely accelerate subsequent runs. A tree is appropriate here because it is small; ordinary upgrades use grouped paths.

## F15 — Offline earnings

Clamp elapsed time to 0–12 hours; preserve event expiry over the interval and compute only documented production. Every positive elapsed interval earns the appropriate production; a 60-second threshold may suppress the summary but must not discard short-absence earnings. Raw BTC accumulation, no simulated offline selling. Apply/claim exactly once even after process death or reopening a sheet. Returning users get a concise duration/earnings summary. Time is injected for tests; no 12-hour sleep.

## F16 — Daily rewards

Retain seven-day cumulative cycle and 20-hour cooldown; missed days do not reset it. Reveal after learning the first-machine loop, so Day 1 cannot replace the first purchase. Starter cash assist ≤30% of first miner cost; later rewards scale with progression and are simulated to avoid skipping a whole stage. No compulsory login or prestige dependence. Preserve already claimed rewards/cooldown and handle rollback/extreme dates safely.

## F17 — Complete statistics

Track current/lifetime BTC and USD, peak/current speed, taps, tap BTC, foreground passive BTC, offline BTC, reward/event BTC attribution, purchases/upgrades, BTC sold, price extremes, events, achievements, prestige/points, active playtime, peak temperature and bounded power/energy history. Define source attribution once, never double-count offline as foreground passive. Newly added counters on migrated saves start with explicit 'tracked since v1.2' coverage; lifetime totals remain unchanged. Subsecond ticks must accumulate playtime and energy correctly.

## F18 — Persistence and migration

Versioned save, actual v1 schema fixtures, migration/defaulting, durable critical transactions and coalesced periodic/background saving. Preserve valid balances/assets/settings and content identities. Retain raw unsupported/corrupt data before recovery; no silent valid-save wipe. Save version, balance version and app version are separate concepts. Follow SAVE_COMPATIBILITY.

## F19 — Settings and recovery controls

Independent sound/haptics, reduced motion, number format and battery-friendly animation setting. Persist changes. Accessible reset confirmation lists data lost; cancel preserves everything. About shows app version/build, short changelog and simulation explanation. No hidden release cheat/test menus.

## F20 — Audio and haptics

Retain lightweight original procedural tap/buy/invalid/achievement/event/prestige/daily sounds; no background music. Implement actual Android haptic feedback with rate limits and capability fallback. Disabled settings suppress every relevant path; lifecycle cleanup prevents leaked AudioTrack/resources. Emulator proves wiring and settings, not physical vibration feel.

## F21 — Accessibility and native behavior

48dp targets, font scale 1.0/1.5, meaningful TalkBack labels/traversal, no continuous announcements for changing counters, color-independent statuses, correct edge-to-edge/cutout insets and predictable system/predictive back. Compact and tall portrait layouts; no trapping overlays or unreachable bottom actions.

## F22 — Evolving facility

Original deterministic scene derived from owned progression, with visible purchased capacity and a capped rendering budget. Stages: salvaged PC, GPU bench, rig workshop, ASIC room, warehouse, energy campus, fusion megafarm, orbital array, lunar/quantum base, Dyson swarm. All 20 tiers influence the scene; no 1:1 rendering of millions of miners. Stage transitions explain a purchase/milestone. Pause visual activity while hidden/backgrounded; reduced motion retains the scene and meaningful static changes. Discovery survives prestige while the current scene reflects the reset operation.

## F23 — App identity and launch surfaces

Replace template Android robot artwork with a graphite/copper silicon/hash mark: adaptive foreground/background, round-mask behavior, themed monochrome layer, matching fallback assets where referenced, and coherent Android system splash. Verify launcher/app drawer, App Info, recents and cold/warm launch. Correct app label; no duplicate splash Activity, artificial launch delay or white flash. APP_IDENTITY owns exact checks.

## F24 — Version history and packaging

Preserve existing branches; `v1.0` is the baseline archive and `v1.2` remains the version line. The completed v1.2 redesign was integrated into `main` on 2026-10-03. App versionName is `1.2.0` and versionCode is `2`; verify no higher code has been distributed before release. Keep package/storage/signing identity and valid saves. Record the release artifact checksum, commit, schema and signing certificate fingerprint without secrets. Create a release tag only after `RELEASE_CHECKLIST.md` passes and a production signing identity is available.

## F25 — Release and repository quality

Unit/migration/simulation/Compose tests, emulator journeys, accessibility, profiler evidence where needed and minified APK smoke. Clean tracked generated files and machine paths; preserve useful old screenshots as historical. No TODO/stub controls, fake enabled settings, dead targets, default artwork, unbounded histories/effects, false test claims or known release-blocking crash/data-loss defects.
