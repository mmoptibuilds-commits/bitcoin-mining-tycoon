# Preserved version branches

User-approved branch policy, 2026-09-30.

| Ref | Purpose | Policy |
|---|---|---|
| main | existing shared baseline | unchanged by this preparation; no automatic merge |
| v1.0 | archive of baseline commit 33787bc4fa61ea4f78de06b7b81dd4094371cf39 | preserve; no writes, delete or force-push |
| v1.2 | approved redesign and execution pack | active work; scoped commits and normal fast-forward pushes |
| future v1.3, v1.4… | next versions | create from last accepted release commit; retain earlier versions |

No v1.1 branch/release is invented. Historical branches are preserved by project policy; this document does not claim server-enforced branch protection.

## Work and handoff

Fetch, inspect status and switch to v1.2 before work. If user changes are present, preserve them and use an isolated worktree rather than resetting. Check whether v1.2 already exists before creating it. Codex and Antigravity never write the same checkout concurrently; hand off a pushed commit SHA and the current evidence ledger. Use a feature-specific worktree/branch only if needed, then integrate into v1.2 without losing version history.

```powershell
git fetch origin
git status --short
git switch v1.2
git pull --ff-only origin v1.2
git diff --check
# Stage named files; inspect the staged diff before committing.
git commit -m "feat: describe the verified milestone"
git push origin v1.2
```

If no local v1.2 exists, use `git switch --track origin/v1.2`. Never force a pull/reset to erase work. Non-fast-forward push means inspect/fetch and reconcile legitimately, not force-push.

## Release identity

Branch v1.2 is mutable while development is active. After RELEASE_CHECKLIST passes, record the accepted commit with a version tag such as `v1.2.0`; do not move/delete that tag. Create the next version branch from that commit. Keep v1.2 as the completed version line; later fixes, if needed, use clearly recorded patch releases and increasing versionCodes.

App `versionName = 1.2.0`; `versionCode >` every prior installed candidate. Current baseline code is 1, so 2 is the minimum first candidate unless a higher code has already been distributed. Save schema is separately versioned. Docs preparation does not change runtime version or create a release tag.

## Release record

Keep app version/code, commit/tag, save schema/balance rules version, APK SHA-256, public signing certificate fingerprint, toolchain/API/emulator and checks performed. Never commit private signing material. Main promotion and external distribution remain separate explicit actions; completion does not merge v1.2 into main.

## Source and data rollback

Old branches preserve source. They do not roll back a player's migrated data automatically. Follow SAVE_COMPATIBILITY before downgrade; use forward fixes for production migration issues. No history rewrite or destructive cleanup commands.
