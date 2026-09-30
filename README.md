# Bitcoin Mining Tycoon — v1.2

A native Android idle game: tap to earn fictional Bitcoin, sell it for cash, buy machines, and grow from a salvaged PC into a sci-fi mining empire.

**Branch status:** `v1.2` contains the approved redesign requirements and execution pack. Gameplay changes are pending implementation; this documentation commit is not a finished v1.2 APK.

## Start here

1. Read [AGENTS.md](AGENTS.md) for shared agent constraints.
2. Read [Current state](docs/CURRENT_STATE.md) and [Product requirements](docs/PRD.md).
3. Follow [Windows setup](SETUP_WINDOWS.md) and [Workflow](docs/WORKFLOW.md).
4. Copy the Codex planning prompt from [Prompts](docs/PROMPTS.md), approve its plan, then use the master goal.
5. Follow [Project plan](docs/PROJECT_PLAN.md) milestone by milestone and finish with P7 in Codex. Optionally use Antigravity P5/P6 for a second verification and fix pass.

## Locked direction

- Kotlin, Jetpack Compose, one app module, DataStore, deterministic local engines.
- Android 12+ / minSdk 31; portrait phone gameplay. Current implementation compiles/targets API 36; an SDK upgrade requires a documented stable-toolchain reason.
- Graphite/copper identity, readable typography and a visibly evolving facility. Original Canvas/vector visuals; no generated crypto artwork.
- Beginner understands tap → Bitcoin → sell → cash → buy → automatic income without crypto knowledge.
- First prestige target: 25–35 minutes for the reference active strategy. Later runs accelerate into orbital, lunar and Dyson tiers.
- Preserve existing saves and install identity. See [Save compatibility](docs/SAVE_COMPATIBILITY.md).
- Fully offline; no backend, account, ads, billing, analytics, wallet, real trading/mining, or network permission.
- Offline earnings cap: 12 hours. No background mining service.
- Emulator-based validation through Android Studio, Gradle and ADB; physical phone connection is not required.

## Requirements and evidence

| Document | Authority |
|---|---|
| [PRD](docs/PRD.md), [Features](docs/FEATURES.md) | What v1.2 must do |
| [Game design](docs/GAME_DESIGN.md), [Economy](docs/ECONOMY_BALANCE.md) | Rules and measurable pacing |
| [Design system](docs/DESIGN_SYSTEM.md), [UX](docs/UX_SPEC.md), [App identity](docs/APP_IDENTITY.md) | Screens, interaction and Android packaging |
| [Architecture](docs/ARCHITECTURE.md), [Save compatibility](docs/SAVE_COMPATIBILITY.md) | Implementation boundaries and migration contract |
| [Test strategy](docs/TEST_STRATEGY.md), [Release checklist](docs/RELEASE_CHECKLIST.md) | Completion evidence |
| [Version control](docs/VERSION_CONTROL.md), [Changelog](CHANGELOG.md) | Preserved version branches and release history |

Historical screenshots are in `docs/screenshots/v1/`; they show the old implementation, not approved v1.2 visuals. Third-party Android skill references are reference material, not product requirements.
