// Types for autumn.mjs, for the docs site and any other TypeScript reader.
import type { Poulpy3d, PoulpyMood, PoulpyShape } from '../model.mjs'
import type { TurnPose } from '../motion.mjs'
export const AUTUMN: {
  mood: PoulpyMood
  cheeks: { colour: string; strength: number; size: number[] }
  scarf: {
    wool: string; stripe: string; height: number; half: number[]; stripes: number; sink: number; swell: number
    knot: { around: number; size: number[] }
    ends: { aside: number; reach: number }[]; wide: number; thick: number; endStripes: number
    fringe: { threads: number; length: number }
    /** Keeps the scarf clear of his arms however they trail (scarf() says how); the autumn look leaves it out. */
    fit?: { out: number; by: number; margin: number; slope: number }
  }
  leaves: {
    count: number; seed: number; colours: string[]; size: number[]; from: number; around: number[]; depth: number; fall: number[]
    swing: number[]; beat: number[]; roll: number; flip: number; turnover: number[]; settle: number; rest: number[]; fade: number
  }
  idle: { seconds: number; sway: number; lean: number; breath: { seconds: number; depth: number }; head: { speed: number; damping: number } }
}
/** A turn's pose, standing still, and for the autumn look: `tilt`, how far his head leans to his right, in radians; `leaves`, each leaf's
 *  `[x, y, z, roll, flip, lie, size, colour]`: where it is in the room, how far it has turned in its own plane and over, how far it has
 *  settled flat on the floor (0 to 1), its size from stem to tip (0 when it is not there) and which of AUTUMN.leaves.colours it is. */
export interface AutumnPose extends TurnPose { tilt: number; leaves: number[][] }
export interface Autumn { advance(seconds: number): AutumnPose; pose(): AutumnPose }
export function buildAutumn(options: { tones: Record<PoulpyMood, string>; colours: { blue: string; ember: string; ink: string }; autumn?: typeof AUTUMN }): Poulpy3d & { headTilt: number }
/** The scarf's parts and their materials (`scarf_wool`, `scarf_stripe`), from numbers shaped like AUTUMN.scarf, for any look that wears it. */
export function scarf(s: typeof AUTUMN.scarf): { materials: Poulpy3d['materials']; parts: Poulpy3d['parts'] }
export function leafShape(): PoulpyShape
export function createAutumn(options?: { autumn?: typeof AUTUMN }): Autumn
