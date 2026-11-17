// Types for looks/dj.mjs, for the docs site and any other TypeScript reader.
import type { Poulpy3d, PoulpyMood, PoulpyShape } from '../model.mjs'
export const DJ_KIT: {
  colours: Record<'kit' | 'groove' | 'metal' | 'gold' | 'white', string>
  headset: { band: { width: number; pad: number; out: number }; cup: { down: number; back: number; size: number; depth: number; lift: number } }
  console: { half: number[]; top: number; z: number }
  platter: number
  record: number
}
/** The middle of each record, on its top: his right one (the viewer's left) first. */
export const DJ_PLATTERS: number[][]
/** Which arm lies on each record, from 0: his right one, then his left one. */
export const DJ_ARMS: number[]
export const DJ_FACE: (string | number)[][]
export const DJ_NOTES: { kind: 'one' | 'two'; colour: string; size: number; from: number[]; still: number[] }[]
export const DJ_MOOD: PoulpyMood
/** A note's outlines in the drawing's coordinates, as lists of points. */
export function note(x: number, y: number, k?: number, kind?: 'one' | 'two'): number[][][]
/** A note as flat shapes facing +z, 1 tall and its middle at 0: `fill` in its colour, `edge` behind it in white. */
export function noteShape(kind?: 'one' | 'two'): { fill: PoulpyShape; edge: PoulpyShape }
/** `effects: false` leaves out the still notes. */
export function buildDj(options: { tones: Record<PoulpyMood, string>; colours: { blue: string; ember: string; ink: string }; effects?: boolean }): Poulpy3d & { headTilt: number }
