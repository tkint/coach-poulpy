// Rounded stroke lettering for the few words drawn around Poulpy, on a 1-unit em with the baseline at 0.
// One copy, read by the 2D drawings (build-poulpy.mjs) and by the 3D poses (poulpy-3d/poses.mjs).
// Each glyph is a path of M, L and Q commands, and `advance` is the width it takes, gap included.
export const GLYPHS = {
  h: { d: 'M0 -0.72 L0 0 M0 -0.3 Q0 -0.5 0.19 -0.5 Q0.38 -0.5 0.38 -0.3 L0.38 0', advance: 0.56 },
  m: { d: 'M0 -0.5 L0 0 M0 -0.32 Q0 -0.5 0.16 -0.5 Q0.3 -0.5 0.3 -0.32 L0.3 0 M0.3 -0.32 Q0.3 -0.5 0.46 -0.5 Q0.6 -0.5 0.6 -0.32 L0.6 0', advance: 0.78 },
  p: { d: 'M0 -0.5 L0 0.24 M0 -0.25 Q0 -0.5 0.19 -0.5 Q0.38 -0.5 0.38 -0.25 Q0.38 0 0.19 0 Q0 0 0 -0.25', advance: 0.56 },
  z: { d: 'M0 -0.5 L0.36 -0.5 L0 0 L0.36 0', advance: 0.5 },
  '?': { d: 'M0 -0.56 Q0 -0.76 0.2 -0.76 Q0.4 -0.76 0.4 -0.56 Q0.4 -0.42 0.2 -0.34 L0.2 -0.22 M0.2 -0.02 L0.2 0', advance: 0.44 },
  P: { d: 'M0 0 L0 -0.7 L0.2 -0.7 Q0.4 -0.7 0.4 -0.52 Q0.4 -0.34 0.2 -0.34 L0 -0.34', advance: 0.56 },
  R: { d: 'M0 0 L0 -0.7 L0.2 -0.7 Q0.4 -0.7 0.4 -0.52 Q0.4 -0.34 0.2 -0.34 L0 -0.34 M0.2 -0.34 L0.4 0', advance: 0.58 },
  '!': { d: 'M0 -0.7 L0 -0.24 M0 -0.02 L0 0', advance: 0.16 },
  '…': { d: 'M0 -0.02 L0 0 M0.26 -0.02 L0.26 0 M0.52 -0.02 L0.52 0', advance: 0.64 },
}
