# Test Strategy — Production Gate

## Rule zero

Testing happens **after every implemented feature**, not only at the end. The `feature-cycle` skill defines the mandatory per-feature loop.

## Layers

### 1. Pure JVM unit tests — majority

Cover every game engine, formula, formatter, migration and state transition.
Targets include:

- economy;
- bulk pricing/MAX calculation;
- market state machine;
- power/heat factors;
- event stacking/expiry;
- achievements;
- prestige preview/apply equivalence;
- offline time clamp;
- daily reward idempotency;
- save migration;
- number formatting;
- ViewModel intent handling where practical.

### 2. Repository/persistence tests

- serialize → persist → load round trip;
- missing/default fields;
- migrations;
- malformed payload recovery;
- transaction save after prestige/purchase;
- lifecycle save behavior.

### 3. Compose behavior tests

Test what users can do:

- bottom tabs;
- MINE;
- sell buttons;
- hardware purchase/bulk modes;
- upgrades;
- dialogs/sheets;
- prestige confirmation;
- settings;
- offline reward collection;
- achievement/event UI.

Avoid brittle assertions on implementation details.

### 4. Screenshot/visual verification

Use Compose preview rendering and/or screenshot testing for:

- each primary screen;
- zero/normal/late-game states;
- locked/disabled/alert states;
- font scale 1.5;
- reduced motion where visual state differs;
- compact and tall phone dimensions.

Visual review checks hierarchy, clipping, overlap, spacing, contrast and anti-AI-slop design rules.

### 5. Device/emulator tests

Run the app on API 31/32 class emulator and a current API 37/current device when possible.
Inspect screen and layout tree. Verify system insets, keyboard if any, process lifecycle, app restore and performance.

### 6. Journeys / E2E

Natural-language Journeys cover critical player outcomes. See `journeys/`.

### 7. Release tests

- `test`;
- instrumented/Compose UI suite;
- lint;
- release assemble/bundle;
- R8 output sanity;
- install/launch release candidate;
- cold/warm startup sanity;
- repeated tap stress;
- 12h offline simulation;
- process-kill/restore;
- no network permission.

## Feature acceptance gate

For each feature, record:

- tests added;
- targeted tests passed;
- UI/preview reviewed if applicable;
- on-device Journey/manual flow passed;
- boundary/failure case passed;
- full regression suite passed.

Do not mark feature complete if any applicable item is missing.

## Bug taxonomy agents must search for

- logic/formula bugs;
- arithmetic overflow/rounding;
- concurrency/race issues;
- double actions;
- stale Compose state;
- recomposition/performance issues;
- persistence/data loss;
- clock/time anomalies;
- lifecycle/process death;
- navigation/back bugs;
- accessibility/semantics;
- touch-target/input bugs;
- clipping/insets/font scale;
- animation/reduced-motion bugs;
- audio/haptic lifecycle;
- UX confusion/missing feedback;
- balance softlocks/exploits;
- release/R8-only crashes;
- policy/privacy/permission issues.
