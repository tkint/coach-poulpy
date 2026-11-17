// Types for looks/chef.mjs, for the docs site and any other TypeScript reader.
import type { Poulpy3d } from '../model.mjs'
export type ChefFace = 'cocky' | 'grin'
export const TOQUE: { at: number[]; lean: number; back: number; band: { top: number; line: number }; profile: number[][]; pleats: number; fold: number; colour: string; seam: string }
export const MOUSTACHE: { wing: string; curl: string; curlWidth: number; twitch: number; drop: number; colour: string | null }
export const CHEF_FACES: Record<ChefFace, (string | number)[][]>
export const CHEF_MOOD: 'proud'
/** The moustache as features, `t` into a twitch: 0 at rest, 1 with its tips up. */
export function moustache(t?: number): (string | number)[][]
/** His face as a cook, `twitch` into a moustache twitch: one of CHEF_FACES and the moustache over it. */
export function chefFace(face?: ChefFace, twitch?: number): (string | number)[][]
export function toque(): { materials: Poulpy3d['materials']; parts: Poulpy3d['parts']; top: number }
/** Poulpy the cook, with his toque; `face: null` leaves his face out, `faceOnly` builds it alone. `headTilt` is always 0. */
export function buildChef(options: { face?: ChefFace | null; twitch?: number; tones: Record<string, string>; colours: { blue: string; ember: string; ink: string }; faceOnly?: boolean }): Poulpy3d & { headTilt: number }
