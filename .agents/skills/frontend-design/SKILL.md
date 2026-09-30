---
name: frontend-design
description: Designs and audits the native Jetpack Compose UI/UX for Bitcoin Mining Tycoon. Use before visual implementation or when reviewing screens, hierarchy, accessibility, interaction, motion, anti-AI-slop quality, and large-number layouts.
---

# Native Android Frontend Design Skill

This is an Android Compose design skill despite the historical `frontend-design` name.

## Required references

Read `docs/DESIGN_SYSTEM.md`, `docs/UX_SPEC.md`, `docs/PRD.md`, and `.agents/rules/ui-no-ai-slop.md`.

## Procedure

1. Identify the player's primary action and information priority on the target screen.
2. Define component hierarchy before styling details.
3. Use reusable design tokens; no random per-screen colors/radii/spacing.
4. Design zero/normal/locked/disabled/error/late-game states.
5. Verify 48dp targets, semantics, 1.5 font scale, system insets and reduced motion.
6. Verify frequently changing numbers do not cause layout jumps/clipping.
7. Remove generic AI-dashboard patterns listed in the anti-slop rule.
8. For imagery, use Compose Canvas/vector primitives only.
9. Before approval, render/inspect the Compose preview and semantics when implementation exists.

## Taste test

A screen fails if it looks like a generic SaaS dashboard with a Bitcoin theme pasted on. It passes when the hierarchy and controls look purpose-built for an idle mining game.
