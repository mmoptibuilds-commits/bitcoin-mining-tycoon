# Journey: Durable actions and process death

Make sales/purchases/upgrades/reward/prestige in a dedicated profile and capture expected state. Background/kill process immediately after confirmed durable action and relaunch without clearing data. Verify retained assets/settings/RNG and no duplicate offline/reward/prestige. Exercise action concurrent with tick/flush and repeat lifecycle loops. Inspect logcat for crash/recovery messages without leaked raw save data.

Record exact commit, emulator/API, fixture, actions and expected/actual values, screenshots/logs and pass/fail/blocker. Test helpers stay debug/test-only and are absent from release. Natural-language descriptions are specifications, not proof these flows ran.
