# App identity and Android finishing details

F23/F24 are required release features, not optional decoration. The v1.0 baseline used Android's robot launcher foreground and green grid background. M6 replaced those resources with an original graphite/copper silicon/hash-channel mark, adaptive/round/themed icon layers and an Android 12+ system splash. Resource/package tests pass; launcher masks, App Info, recents and splash transition still need direct emulator inspection. See `CURRENT_STATE.md` for current evidence.

## Visual identity

Create an original graphite/copper silicon block/hash-channel mark: a compact chip/rack silhouette with a clear internal compute motif. Prefer a strong readable shape to miniature text or a Bitcoin coin. Use vector foreground/background; a clean single-alpha monochrome mark for themed icons. Keep the central mark readable in circular/squircle masks and at launcher size. Do not generate raster artwork or add a green Android placeholder.

Android's adaptive icon guidance uses 108×108dp layers and a central 66×66dp safe zone. Confirm current official guidance before implementation. Reuse the mark in splash/About where useful, not as a watermark across gameplay.

## Files and surfaces

- Replace `app/src/main/res/drawable/ic_launcher_foreground.xml` and `ic_launcher_background.xml`.
- Add a dedicated monochrome drawable if the color foreground is unsuitable as a mask.
- Inspect `mipmap-anydpi-v26/ic_launcher.xml` and `ic_launcher_round.xml`; all referenced layers must use the new mark. Inspect density fallback `mipmap-*` assets so no old robot remains on an applicable surface; regenerate or remove only demonstrably unused resources.
- Keep `android:icon`, `android:roundIcon`, `android:label` and package identity coherent in AndroidManifest.xml.
- Configure the existing Android 12+ system splash through themes/resources and MainActivity only as needed. Single opaque graphite launch background; correct icon scaling; no duplicate splash Activity or forced wait.
- About/build information shows Bitcoin Mining Tycoon, 1.2.0 and the actual versionCode/build identifier.

## Native finishing audit

Check home launcher, app drawer, themed icons where supported, App Info, recent-apps icon/title, cold/warm/relaunch, system bars/insets, bottom sheets/dialogs, disabled/locked states, loading during migration, empty states, reset wording and text-scale/RTL robustness. All production UI icons use a consistent vector/line family, meaningful semantics and 48dp hit areas.

## Packaging and save continuity

Package stays `com.antigravity.bitcoinminingtycoon`; retain storage path. Current app versionName is `1.2.0` and versionCode is `2`; verify it exceeds every external install before release. The local minified candidate uses the workspace debug signing configuration and is a test/sideload build, not a public release. Reuse the actual signing key/certificate of the old install to update it. Generating a different debug key on another machine does not preserve installation compatibility; never suggest uninstalling to bypass this.

Signing keys/passwords stay outside Git. Record public signing certificate fingerprint, APK SHA-256, commit, build commands and tested emulator/API. Store distribution APKs in release/artifact storage, not source control. A GitHub Release was requested if publishable, but the current debug-signed artifact and unavailable release access do not satisfy that gate. Do not introduce Play Store publication work.

## Acceptance

No template robot/grid remains in applicable icon resources; all layers resolve; color/monochrome masks are readable. API31 verifies About metadata and release launch; the signed in-place update test retains the seeded v1 save. Launcher/App Info/recents masks, splash transition, API36, physical phone launcher differences, actual haptic feel and phone performance remain unverified.

## Official references

- [Adaptive icons](https://developer.android.com/develop/ui/compose/system/icon_design_adaptive)
- [Android system splash screen](https://developer.android.com/develop/ui/views/launch/splash-screen)
- [App versioning](https://developer.android.com/studio/publish/versioning)
