# Visual Design System — Industrial, Human, Non-AI-Slop

## Direction

A compact industrial control-panel aesthetic: dark graphite surfaces, warm copper/amber accent, crisp numeric hierarchy, subtle mechanical motifs, and restrained motion. It should feel like a purpose-built mining operations console, not a generic SaaS dashboard or crypto exchange.

## Explicitly banned aesthetics

- purple-to-blue gradients;
- glassmorphism/frosted cards everywhere;
- neon cyan/purple glow;
- giant 24–32dp rounded rectangles for every container;
- decorative floating orbs/blobs;
- 3D Bitcoin coins, server-rack renders, robots, space art or other generated imagery;
- random sparkles/particles used as decoration;
- emoji as production icons;
- huge marketing-style hero headline on a game screen;
- fake charts that do not encode real in-game data;
- excessive drop shadows;
- arbitrary pills for normal text;
- five accent colors competing for attention.

## Imagery policy

Do not call image-generation tools for the app UI. Build visuals from:

- Compose Canvas;
- VectorDrawable;
- simple original geometric line icons;
- Material Symbols only where they fit semantically;
- typography and data visualization generated from real game state.

The mining rig illustration, if present, should be a simple programmatic schematic (fan circles, rack grid, activity bars), not a bitmap illustration.

## Color

Create accessible tokens rather than hardcoding random colors. Suggested character:

- background: near-black graphite, not absolute black everywhere;
- surface: slightly lifted charcoal;
- primary accent: warm copper/amber;
- positive: muted green;
- warning: amber/yellow distinct from primary by luminance/shape/text;
- destructive: restrained red;
- text: high/medium/disabled neutrals.

Do not convey market up/down or thermal warnings by color alone; pair with icon/label/sign.

## Shape

- Cards/panels: 8–12dp corner radius.
- Buttons: 8–12dp radius; primary MINE control may be distinctive but not bubbly.
- Thin borders/dividers are preferred over giant shadows.

## Typography

Use Android-system-friendly fonts unless a bundled open font is genuinely needed. Distinguish:

- display number;
- section title;
- body;
- label;
- dense numeric/technical label.

Use tabular figures for frequently changing numeric fields where available. Monospace may be used sparingly for technical values, not the entire interface.

## Layout

- Dense but breathable.
- Primary data above the fold: BTC, USD, price, hashrate, production.
- Mine control is dominant but not half the screen.
- Power/heat/efficiency are secondary operational data.
- Bottom nav remains stable.
- Avoid nested cards inside cards unless hierarchy requires it.

## Motion

Motion explains state changes:

- tap compression;
- value delta rise/fade;
- purchase confirmation;
- achievement reveal;
- prestige transition;
- graph movement.

No ambient meaningless movement. Reduced Motion removes number flights, parallax, continuous fan flourish etc. Essential state changes remain visible.

## App icon

Original vector mark combining a simplified ASIC/rack grid with a mining/hash motif. Avoid copying official Bitcoin artwork as the whole icon and avoid generated images.
