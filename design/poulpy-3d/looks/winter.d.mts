// Types for winter.mjs, for the docs site and any other TypeScript reader.
import type { Poulpy3d, PoulpyShape } from '../model.mjs'
import type { TurnPose } from '../motion.mjs'

/**
 * A turn's pose and, for Winter Poulpy: `tilt`, how far his head leans to his right, in radians; `breathing`, how far he is
 * into a breath, from 0 to 1; `snow`, each flake's `[x, y, z, size, spin, crystal]` (size 0 when it is not there, crystal 1
 * for a six-armed one); `breath`, each puff's `[x, y, z, size, opacity, spin]` (size 0 when it is not there).
 */
export interface WinterPose extends TurnPose { tilt: number; breathing: number; snow: number[][]; breath: number[][] }
export interface Winter { advance(seconds: number): WinterPose; pose(): WinterPose }
export const WINTER: {
  face: (string | number)[][]
  knit: { stripe: string }
  beanie: { crown: number; puff: number[]; cuff: number[]; fold: number; ribs: number; rib: number; stripe: number[]; pompom: number }
  scarf: { y: number; dip: number; size: number[]; knot: number; ends: number[][] }
  sway: { seconds: number; degrees: number; tilt: number; body: { speed: number; damping: number }; head: { speed: number; damping: number } }
  breath: { seconds: number; inhale: number; stretch: number; squash: number; speed: number; damping: number }
  puffs: { count: number; apart: number; life: number; speed: number; aside: number; rise: number; drag: number; grow: number[]; opacity: number; mouth: number[] }
  snow: { flakes: number; crystals: number; top: number; wide: number; back: number; front: number; clear: number; fall: number[]; sway: number[]; size: number[]; crystal: number[]; spin: number; melt: number }
}
export function buildWinterPoulpy(options: { tones: Record<string, string>; colours: { blue: string; ember: string; ink: string } }): Poulpy3d
export function winterShapes(): Record<'flake' | 'crystal' | 'puff', { shape: PoulpyShape; colour: string }[]>
export function createWinter(options?: { winter?: typeof WINTER }): Winter
