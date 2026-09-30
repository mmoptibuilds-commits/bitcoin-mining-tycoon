# Decision Log

## D001 Native Android

Kotlin + Compose chosen for native performance, direct Android tooling, haptics/audio/lifecycle control and strongest Antigravity/Android CLI support.

## D002 Single app module

Feature coupling is high and scope is one game. Multiple Gradle modules would increase one-shot build/configuration risk without current payoff.

## D003 Local only

No backend/account/ads/IAP/analytics. No INTERNET permission.

## D004 BTC + USD

Mining produces fictional BTC; selling converts it to USD used for purchases. This gives the simulated market meaningful gameplay.

## D005 Dashboard-style Mine screen

Chosen over a giant Cookie-Clicker-only surface. Preserves the clicker loop while surfacing market, power, heat and efficiency.

## D006 Bottom navigation

Mine / Hardware / Upgrades / Stats. Settings via gear. Fewer nested screens means fewer navigation bugs.

## D007 12-hour offline cap

Balances idle reward with return incentive and bounds time manipulation.

## D008 No background mining service

Offline progress is computed on resume/launch. Avoids battery drain, foreground-service complexity and platform restrictions.

## D009 No background music

Sound effects + haptics only in V1. Reduces lifecycle/audio complexity and package size.

## D010 No generated imagery

Use Compose/Canvas/vector assets. This is both a design requirement and anti-slop constraint.

## D011 Main writer + verifier subagents

Avoid simultaneous writers in the same coupled module. Subagents independently audit/test and return findings; primary agent owns production edits.

## D012 Power & cooling infrastructure in Upgrades screen

Power capacity and cooling dissipation are purchased as dedicated infrastructure upgrade tracks in the Upgrades screen for USD. Flat kW capacity and cooling dissipation ratings are provided per tier.

## D013 Instantaneous thermal equilibrium model

Temperature is evaluated deterministically per tick based on instantaneous heat generation vs cooling dissipation (`temp = ambient + factor * max(0, heat - cooling)`). No dynamic heat accumulation buffer or differential cooling cooldown curves; 100% deterministic and stateless across offline gaps.

## D014 Offline earnings snapshot & BTC accumulation

Offline progression accumulates raw BTC based on the player's effective hashrate snapshot at background time (active events expire naturally according to their remaining duration). No market trading or auto-sell occurs during offline time; player collects raw BTC on return and trades manually.

## D015 Full economy prestige reset

Prestige executes a hard economy reset: BTC, USD, owned miners, standard upgrades, and power/cooling levels reset to baseline. Persisted across prestige: Satoshi Points, unlocked Satoshi Tree permanent nodes, lifetime statistics, achievements, daily reward streak, onboarding state, and settings.

## D016 Hybrid random event triggering

Ambient market and grid events (Bull Run, Market Crash, Heat Wave) trigger automatically via pseudo-random seeded timers with an active dashboard banner. Windfall events (Lucky Block, ASIC Breakthrough) spawn interactive clickable badges on the Mine screen to reward active play.

## D017 Configurable threshold auto-sell

When the auto-sell upgrade is unlocked, players configure a threshold rule in the Market card (e.g. sell when price exceeds target or during Bull trend) to automatically convert incoming mined BTC to USD.

## D018 Non-punishing cumulative 7-day daily reward

Daily rewards unlock at least 20 hours after the previous claim (or next calendar day). Missed days do not reset the streak; players advance continuously along the 7-day cycle.

## D019 Procedural audio synthesis via AudioTrack

Sound effects (tap, buy, invalid, achievement, event, prestige, daily reward) are synthesized programmatically in code using Android `AudioTrack`. Zero external audio assets, zero bundle bloat, zero licensing ambiguity.

## D020 Clean hybrid navigation architecture

The 4 root tabs (Mine, Hardware, Upgrades, Stats) persist at the base. Settings and the Satoshi Prestige Tree navigate as full-screen destinations with top app bars and predictive back support. Prestige confirmation and Offline return use modal bottom sheets.

