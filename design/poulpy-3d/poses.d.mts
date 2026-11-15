// Types for poses.mjs, for the docs site and any other TypeScript reader.
import type { Poulpy3d, PoulpyMood, PoulpySide } from './model.mjs'
export type PoulpyPose = 'cocky' | 'straining' | 'champion' | 'standingTall' | 'exhausted' | 'sideGlance' | 'sleeping' | 'patchedUp' | 'shy' | 'thinking' | 'pouting' | 'fuming'
export const POSES: Record<PoulpyPose, { mood: PoulpyMood; face: string | (string | number)[][]; on?: { at: number[]; marks: (string | number)[][] }; stretch?: number; headTilt?: number; props?: string[]; around?: unknown[][]; behind?: unknown[][] }>
export const CONFETTI: { colours: string[]; size: number[] }
/** The medal's hinge, `[y, z]`: it swings up and down around the level line through it. */
export const medalHinge: number[]
export const medalReach: { length: number; rest: number }
/** `headTilt`: how far his head leans to his right, in radians. `effects: false` leaves out the flat effects around him; `face` gives him another face than the pose's. */
/** His face as the cocky one winks, from 0 (eyes wide open) to 1 (the wink and the smirk), as features for buildPoulpy3d. `side` is the eye he shuts, his own: 'right' if left out. */
export function winkFace(t: number, side?: 'left' | 'right'): (string | number)[][]
/** The stars around the cocky one when he winks: where each is in the room and its size. */
export const STARS: { at: number[]; size: number }[]
/** The stars' gold. */
export const GOLDEN: string
export function starShape(): { positions: number[]; normals: number[]; indices: number[] }
export function buildPoulpyPose(options: { pose: PoulpyPose; effects?: boolean; face?: string | (string | number)[][]; tones: Record<PoulpyMood, string>; colours: { blue: string; ember: string; ink: string } }): Poulpy3d & { headTilt: number }
export function poseMood(pose: PoulpyPose): { mood: PoulpyMood; side: PoulpySide }
