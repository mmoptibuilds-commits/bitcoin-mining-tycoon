# Local-only privacy and recovery

The game makes no network requests and includes no backend, account, ads, analytics, billing, wallet, real mining or trading. No INTERNET or unnecessary sensitive permission. Android cloud backup stays disabled; no new export/import UI in this scope.

Save data contains gameplay/settings facts. Migrations preserve valid assets; unsupported/corrupt raw payload checkpoints remain bounded and app-private. Do not log raw saves in release, upload them, or silently erase them. Test fixtures are deterministic/anonymized. Reset requires a clear destructive confirmation and cancellation test.

Signing keys/passwords remain outside Git. Public certificate fingerprints and APK checksums are safe release evidence. Same-certificate update plus same package/storage identity is required to preserve the old install; don't uninstall as a workaround.

Tooling can have its own telemetry/account requirements; those do not justify adding telemetry to the game. Android CLI docs offer a no-metrics option if desired. No cloud device reservation or paid service is needed for the approved emulator workflow.

The first launch and About screen describe a fictional simulation with no real earnings. Quantum/Dyson content is a game fantasy. Inspect final merged manifest/dependencies rather than assuming an unchanged permission list proves the final binary.
