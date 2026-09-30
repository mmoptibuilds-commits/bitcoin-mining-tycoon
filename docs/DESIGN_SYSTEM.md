# Design system — graphite/copper mining game

## Direction

A tactile, readable mining game whose main screen is a growing facility. Graphite provides depth; copper highlights actions, machines and milestones. Use normal readable typography for titles, explanations and controls; tabular/monospace only for suitable numerical telemetry. The old operations-console appearance is superseded.

## Composition

Facility scene is the visual anchor. Balances and production have clear hierarchy; Mine and the next goal are obvious. Market/infrastructure appear as compact expandable tools. Use open sections, dividers and purposeful containers rather than equal-weight cards around everything. Hardware rows emphasize ownership, production and buy action. No fake graphs; a market chart encodes actual game history.

Use reusable color, spacing, typography, shape and motion tokens. Start from current AppColors/AppTypography; adjust coherently instead of adding a second theme. Graphite background/charcoal surfaces, copper/amber primary, restrained green/warning/red statuses paired with text/icons. 8–12dp container/button corners where appropriate; no giant rounded-card wall. Copper need not decorate every label.

## Illustrations and icons

Original Canvas/vector geometric scenes and a coherent small line-icon family. Fans, boards, cables, racks, buildings, energy sources and orbital structures must be recognisable without photorealism. Render representative counts, not one object per owned unit. App identity is specified in APP_IDENTITY.

No generated or stock crypto imagery, Android template robot, 3D floating coins, emoji-as-controls, purple/blue gradients, glassmorphism stacks, ambient glow blobs, random decorative particles or irrelevant hero marketing copy. Quantum/Dyson scenes are fictional game motifs, not technical claims.

## Motion and feedback

Tap press/release ~80–140ms, number delta ~300–500ms, scene purchase/milestone transition ~200–350ms. Tune by emulator inspection. Bound simultaneous effects (initial cap 24 particles); coalesce rapid taps rather than allocating unbounded animations. Shockwave and machine response explain an action. No continuous ambient flourish just to fill space.

Reduced motion disables particles, number flights, parallax and continuous fan/scene animation; keep static scene change, selected state and a readable delta. Battery-friendly animation reduces optional activity without changing production. Pause visual loops when backgrounded or scene hidden.

## Accessibility and content

48dp hit areas; font scale 1.0 and 1.5; icons always have meaningful semantics when interactive. No color-only warnings. Live counters do not spam TalkBack. Avoid width shifts as balances change. Use short labels: Mine, Sell, Buy, Mining speed, Power, Cooling, Collect. Secondary technical detail can use H/s/kW/°C.

## Review states

New/empty, early/mid/late, migrated, locked/unaffordable, deficit/heat, active event, daily claimed, offline return, prestige confirm, reduced motion and huge numbers. Inspect screenshots plus real interactions. Historic v1 screenshots are evidence of prior behavior, not a layout to reproduce.
