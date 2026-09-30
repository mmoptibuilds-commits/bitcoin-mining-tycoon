# Skills and Tooling Selection

## Official Android skills

Use Android CLI and install project-scoped skills where possible. Start with the first three; install local release/profiling skills when their phase arrives:

```powershell
android init
android skills list --long
android skills add testing-setup --project=.
android skills add edge-to-edge --project=.
android skills add navigation-3 --project=.
# local-release-hardening phase
android skills add r8-analyzer --project=.

# only if profiling evidence is needed
android skills add android-profiler --project=.
```

If an ID has changed, use `android skills find <keyword>` / `android skills list --long` and install the current equivalent rather than guessing.

### Why these

- `testing-setup`: unit, Compose UI, screenshot, E2E strategy.
- `edge-to-edge`: system bar/inset correctness.
- `navigation-3`: current Compose-native navigation guidance; use only if navigation implementation needs it.
- `r8-analyzer`: release shrink/keep-rule audit.
- `android-profiler`: install/use only if performance, jank or memory evidence requires profiling.

## Optional future publishing skill

Do **not** install or run Play Store policy tooling for today's build. If you later decide to publish on Google Play, first run `android skills find play` / `android skills list --long`, then install the current official Play-policy skill and follow `docs/OPTIONAL_PLAY_STORE_FUTURE.md`.

## Workspace skills included in this pack

- `/frontend-design`: Android-specific visual/UX design and anti-slop audit.
- `/feature-cycle`: mandatory implement → test → device/UX verify loop.
- `/game-balance`: balance formulas and simulation checks.
- `/bug-hunt`: broad bug taxonomy investigation.
- `/release-audit`: final production gate.
- `/product-copy`: concise in-game text without generic AI copy.

## Skills from earlier web work: what to carry forward

### Carry principles, not web implementation

- UI UX Pro Max: keep information architecture, user journeys, accessibility and anti-pattern review.
- Taste / Frontend Design Taste: keep hierarchy, density, originality, restraint, typography and motion taste.
- Design & Motion: keep purpose-driven motion and reduced-motion discipline.
- Stop Slop Writing: use for onboarding, achievements, event text and settings copy.

These principles are baked into the workspace skills so the agent receives Android-native instructions rather than React/CSS assumptions.

### Do NOT install/use for this app

- Scroll Craft.
- GSAP/ScrollTrigger skills.
- Vercel web-interface-guidelines as an implementation rule.
- web accessibility/ARIA skills.
- generic Tailwind/shadcn design skills.

They target browser/web behavior and can actively mislead Compose implementation.

## MCPs

Keep MCP surface minimal.

- GitHub MCP/plugin: optional, useful after repo creation for commits/issues/remote operations.
- No Supabase MCP: no backend.
- No Figma MCP unless you later create an actual Figma design.
- No Chrome DevTools MCP for native UI testing.
- No generic Android build MCP required initially; official Android CLI already covers build/run/install/screen/layout/Journeys/Android Studio integration.

## Android Studio integration

If Android Studio Quail 2+ is available and project is open:

```powershell
android studio check
```

Then let the agent use `android studio analyze-file` and `android studio render-compose-preview` for semantic and visual verification.

## Explicitly skip the official `styles` skill for V1

The Compose Styles API is still experimental and may pull pre-release Compose artifacts. This project requires stable production dependencies, so use ordinary Material 3 theming, custom tokens, modifiers and reusable composables instead.
