---
name: release-audit
description: Audits the v1.2 emulator-local APK release, branding, migrations and version-history evidence.
---

# release-audit

Read RELEASE_CHECKLIST, TEST_STRATEGY, APP_IDENTITY, SAVE_COMPATIBILITY and VERSION_CONTROL. Verify exact SHA, suites/simulations/Compose, both API emulator journeys, minified APK installation/restore/in-place same-certificate upgrade and absence of default icon/stubs/unwanted permissions. Inspect accessibility/reduced motion, performance concerns and true release artifact. Check versionCode/name/schema separation, branch preservation and APK/certificate evidence. Distinguish planned/implemented/verified/blocked; physical haptic/phone performance is unverified. Read-only auditor reports findings; main writer fixes them and reruns affected proof. No tag from docs or debug checks alone.
