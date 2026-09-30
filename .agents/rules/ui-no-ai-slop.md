---
trigger: glob
globs: "**/*.kt, **/*.xml"
description: Enforce the project's human-designed Android visual language and reject generic AI-looking UI.
---

# No AI-slop UI

Never introduce:

- purple/blue gradients;
- glassmorphism/frosted card stacks;
- giant rounded-card soup;
- glowing blobs/orbs;
- generated/stock crypto illustrations or 3D coins;
- emoji used as interface icons;
- meaningless decorative charts;
- oversized hero marketing layouts;
- arbitrary pill labels everywhere;
- excessive shadows and animation.

Use the design tokens/components in `docs/DESIGN_SYSTEM.md`. Prefer graphite surfaces, restrained copper/amber accent, thin borders, 8–12dp radii, clear hierarchy, dense useful data and original Compose Canvas/vector motifs.

Do not call image generation for production app imagery.
