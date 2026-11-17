// Types for jetpack.mjs, for the docs site and any other TypeScript reader.
import type { Poulpy3d, PoulpyMood, PoulpyShape } from '../model.mjs'
export const JETPACK: {
  mood: PoulpyMood
  grin: { smile: number[][]; corners: number[]; eyes: number[]; cheeks: number[][] }
  tank: { x: number; y: number; z: number; length: number; radius: number; nose: number; splay: number }
  colours: { tank: string; trim: string; nozzle: string }
  /** Each layer of a flame, outermost first: colour, length and half width. */
  flames: [string, number, number][]
  smoke: { colour: string; edge: string; width: number }
}
/** Where each flame comes out, his right then his left, as he stands, and the way it points. */
export const NOZZLES: { at: number[]; way: number[] }[]
/** His face as he flies: from hovering (0) to dashing (1). */
export function jetpackFace(t: number): (string | number)[][]
export function flameShape(length: number, width: number, z?: number, n?: number): PoulpyShape
export function puffShape(edge?: number, n?: number): PoulpyShape
/** Poulpy with his jetpack; with `flames` (on unless told) the flat flames `jetpack_flame_<nozzle>_<layer>`, drawn hanging from (0, 0) and facing +z. */
export function buildPoulpyJetpack(options: { tones: Record<PoulpyMood, string>; colours: { blue: string; ember: string; ink: string }; flames?: boolean; face?: (string | number)[][] }): Poulpy3d
