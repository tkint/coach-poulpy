---
status: accepted
date: 2026-10-24
---

# Design tokens are generated from one file

Colours, mood shades, type, spacing, radius and motion live in `design/tokens.json`. A script generates the Compose theme, the SwiftUI tokens and accent colour, and the website's CSS variables, and CI fails when a generated file no longer matches. Poulpy's drawings follow the same rule through `design/build-poulpy.mjs`. Nobody edits a generated file by hand, so the three platforms cannot drift apart.
