---
name: release-audit
description: Runs the final Bitcoin Mining Tycoon production-readiness audit across tests, UX, accessibility, performance, release/R8, manifest/privacy, and installable local-release behavior.
---

# Release Audit

Read `docs/PRODUCTION_READINESS.md` and `docs/RELEASE_CHECKLIST.md`.

Verify, do not assume:

- full test suites;
- core Journeys;
- API 31/32 and current Android smoke;
- minified release build install/launch;
- process-death restore;
- 12h offline clamp;
- rapid-tap stress;
- accessibility/1.5 font/reduced motion;
- no INTERNET/unnecessary permissions;
- R8/resource shrink sanity;
- no generated imagery/AI-slop regression;
- no release-blocking known defects.

Return a severity-ranked finding list. The main writer fixes findings; rerun until green.
