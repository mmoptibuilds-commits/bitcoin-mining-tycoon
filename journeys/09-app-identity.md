# Journey: Launcher, splash and release identity

Install actual minified candidate; inspect home launcher/app drawer, round mask and themed icon where supported, App Info and recents title/icon. No Android robot/green template grid. Cold/warm/task relaunch uses one coherent graphite system splash, no forced wait/white flash. Verify versionName/code/label, manifest/resources/R8 and About. Record APK checksum/public signing certificate/commit and emulator API; do not claim OEM phone behavior from emulator.

Record exact commit, emulator/API, fixture, actions and expected/actual values, screenshots/logs and pass/fail/blocker. Test helpers stay debug/test-only and are absent from release. Natural-language descriptions are specifications, not proof these flows ran.
