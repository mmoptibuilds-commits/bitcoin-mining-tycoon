# Version branches and release workflow

Updated 2026-10-03. The user authorized integration of the completed v1.2 redesign into `main`; it was merged normally as `221c548`. The integration preserved the version branches and did not create a release tag.

| Ref | Purpose | Policy |
|---|---|---|
| `main` | Current integrated development/default branch; contains the v1.2 redesign | Make future changes here unless the user selects another target; use scoped commits and normal pushes |
| `v1.0` | Baseline archive at `33787bc4fa61ea4f78de06b7b81dd4094371cf39` | Preserve as historical source; do not delete, rewrite or update without explicit instruction |
| `v1.2` | Retained v1.2 version line | Preserve its commits and branch ref; update it only when the user requests version-line changes |
| Future version lines | Subsequent product versions | Create from the accepted source/release commit and retain earlier version refs |

The repository has `v1.0`, not a branch literally named `v1`. No v1.1 branch is invented. Historical branches are preserved by project policy; this document does not claim server-enforced branch protection.

## Work and integration

Fetch and inspect the checkout before changing it. Preserve dirty work; never use reset/force to make switching easier. The current integrated code is on `main`. Switch to `v1.2` only when the task targets that version line. Do not delete a branch after merging, and do not rewrite published history.

```powershell
git fetch origin
git status --short --branch
git switch main
git pull --ff-only origin main
git diff --check
# Stage named files and inspect the staged diff before committing.
git commit -m "docs: describe verified project state"
git push origin main
```

If a normal push is rejected because the remote advanced, fetch and inspect the new commits, then reconcile with a normal merge/rebase only when authorized. Never force-push.

## Release identity and publishing

The app currently reports versionName `1.2.0`, versionCode `2`, save schema `3`, and balance rules `2`. Version code must exceed every externally distributed install; repository branches/tags alone cannot prove that external history. Save schema and app version are independent.

Before publishing a GitHub Release or creating its tag, verify all items in `RELEASE_CHECKLIST.md`, the actual production signing identity, the externally used version code, and the exact APK checksum. Never publish a workspace debug-key-signed APK or commit signing secrets. On 2026-10-03 the user asked for a GitHub release if possible, but this environment could not publish: GitHub release lookup was `Forbidden`, `gh auth status` reported invalid authentication, and only a local debug signing identity was available. No public release or tag was created.

Keep app version/code, accepted commit and tag, save schema/balance rules, APK SHA-256, public signing certificate fingerprint, toolchain/API/emulator and checks performed. Never commit private signing material. Main integration is complete; external distribution remains a separate release gate.

## Source and save rollback

Old branches preserve source. They do not automatically roll back a player's migrated data. Follow `SAVE_COMPATIBILITY.md` before downgrade and use forward fixes for production migration issues. Do not rewrite history or remove historical branches.
