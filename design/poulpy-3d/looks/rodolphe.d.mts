// Types for rodolphe.mjs, for the docs site and any other TypeScript reader.
import type { Poulpy3d } from '../model.mjs'
import type { TurnPose } from '../motion.mjs'
export const COLOURS: { antler: string; band: string; nose: string; lit: string; glow: string; scarf: string; white: string }
export const FACE: (string | number)[][]
/** Where the scarf is knotted: its ends swing around the upright line through it. */
export const scarfKnot: number[]
/** The middle of his nose and its size, for whoever makes it glow. */
export const noseBulb: { at: number[]; radius: number }
export function buildRodolphe(options: { colours: { blue: string; ember: string; ink: string }; tone?: string }): Poulpy3d
export const IDLE: {
  beat: number
  sway: { degrees: number; speed: number; damping: number }
  lean: { degrees: number; speed: number; damping: number }
  bob: { depth: number; seconds: number; speed: number; damping: number }
  glow: { every: number; rest: number; rise: number; speed: number; damping: number }
  ends: { speed: number; damping: number; most: number }
  rest: number
}
/** A turn's pose and, for Rodolphe: `tilt`, how far his head leans to his right in radians; `glow`, how bright his nose is, 0 to 1; `ends`, how far the scarf's ends have swung, in radians. */
export interface RodolphePose extends TurnPose { tilt: number; glow: number; ends: number }
export interface RodolpheIdle { softness: number; advance(seconds: number): RodolphePose; pose(): RodolphePose }
export function createRodolpheIdle(options?: { softness?: number; idle?: typeof IDLE }): RodolpheIdle
