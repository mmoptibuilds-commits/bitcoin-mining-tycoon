# Windows Setup Cheat Sheet

```powershell
# Android CLI
winget install --id Google.AndroidCLI
android update
android init
android info

# inspect official skills
android skills list --long

# project-scoped relevant skills
android skills add testing-setup --project=.
android skills add edge-to-edge --project=.
android skills add navigation-3 --project=.
# add during local release hardening
android skills add r8-analyzer --project=.

# add only if profiling is needed
android skills add android-profiler --project=.

# if Android Studio Quail 2+ is running with the project open
android studio check
```

If any skill ID is no longer present, run `android skills find <keyword>` or `android skills list --long` and use the current official ID.

Then in Antigravity:

1. select Gemini 3.8 Flash High;
2. run the `/grill-me` prompt from `docs/PROMPTS.md`;
3. run `/frontend-design`;
4. run `/plan`;
5. review/approve the plan;
6. run the master `/goal` prompt;
7. use `/boost` only for stubborn blockers;
8. finish with `/bug-hunt` and `/release-audit`.
