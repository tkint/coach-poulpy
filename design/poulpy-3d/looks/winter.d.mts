// Types for winter.mjs, for the docs site and any other TypeScript reader.
import type { Poulpy3d, PoulpyShape } from '../model.mjs'
import type { TurnPose } from '../motion.mjs'
import type { AUTUMN } from './autumn.mjs'

/**
 * A turn's pose and, for Winter Poulpy: `tilt`, how far his head leans to his right, in radians; `breathing`, how far he is
 * into a breath, from 0 to 1; `snow`, each flake's `[x, y, z, size, spin]` (size 0 when it is not there); `breath`, each puff's `[x, y, z, size, opacity, spin]` (size 0 when it is not there).
 */
export interface WinterPose extends TurnPose { tilt: number; breathing: number; snow: number[][]; breath: number[][] }
export interface Winter { advance(seconds: number): WinterPose; pose(): WinterPose }
export const WINTER: {
  face: (string | number)[][]
  wool: string; berry: string
  scarf: typeof AUTUMN.scarf
  muffs: { down: number; lift: number; size: number; depth: number; tufts: number; tuft: number; band: { width: number; out: number } }
  sway: { seconds: number; degrees: number; tilt: number; body: { speed: number; damping: number }; head: { speed: number; damping: number } }
  breath: { seconds: number; inhale: number; stretch: number; squash: number; speed: number; damping: number }
  puffs: { count: number; apart: number; life: number; speed: number; aside: number; rise: number; drag: number; grow: number[]; opacity: number; mouth: number[] }
  snow: { flakes: number; top: number; wide: number; back: number; front: number; clear: number; fall: number[]; sway: number[]; size: number[]; spin: number; melt: number }
}
export function buildWinterPoulpy(options: { tones: Record<string, string>; colours: { blue: string; ember: string; ink: string } }): Poulpy3d
export function winterShapes(): Record<'crystal' | 'puff', { shape: PoulpyShape; colour: string }[]>
export function createWinter(options?: { winter?: typeof WINTER }): Winter
