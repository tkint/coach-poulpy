// Types for poses.mjs, for the docs site and any other TypeScript reader.
import type { Poulpy3d, PoulpyMood, PoulpySide } from './model.mjs'
export type PoulpyPose = 'cocky' | 'straining' | 'champion' | 'standingTall' | 'exhausted' | 'sideGlance' | 'sleeping' | 'patchedUp' | 'shy' | 'thinking' | 'pouting' | 'fuming'
export const POSES: Record<PoulpyPose, { mood: PoulpyMood; face: string | (string | number)[][]; on?: (string | number)[][]; stretch?: number; headTilt?: number; props?: string[]; around?: unknown[][]; behind?: unknown[][] }>
/** `headTilt`: how far his head leans to his right, in radians. */
export function buildPoulpyPose(options: { pose: PoulpyPose; tones: Record<PoulpyMood, string>; colours: { blue: string; ember: string; ink: string } }): Poulpy3d & { headTilt: number }
export function poseMood(pose: PoulpyPose): { mood: PoulpyMood; side: PoulpySide }
