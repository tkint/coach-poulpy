// Types for model.mjs, for the docs site and any other TypeScript reader.
export interface PoulpyShape { positions: number[]; normals: number[]; indices: number[] }
/** A part with `card` set is a flat effect of a pose (poses.mjs), to keep facing the viewer in front of him or behind. */
export interface PoulpyPart extends PoulpyShape { name: string; material: string; card?: 'front' | 'back' }
export interface PoulpyMaterial { colour: number[]; flat?: boolean; outline?: number[]; opacity?: number }
/** `headLift`: how far to lift his head and face, which are built where they sit at rest. */
export interface Poulpy3d { parts: PoulpyPart[]; materials: Record<string, PoulpyMaterial>; height: number; headLift: number }
export type PoulpySide = 'blue' | 'ember'
export const SHAPE: { head: { radius: number[]; centreY: number }; arms: { count: number; radius: number[]; ring: number }; liningLine: number; propLine: number; face: { y: number; scale: number; spread: number; eyes: number } }
export type PoulpyMood = 'proud' | 'happy' | 'recovering' | 'annoyed' | 'grumpy' | 'fuming'
export const FACES: Record<string, (string | number)[][]>
export const MOODS: Record<PoulpyMood, { side: PoulpySide; face: string }>
export const LOOK: { light: number[]; gleam: { towards: number[]; size: number[]; tilt: number; white: number }; shade: number; outline: { width: number; ink: number } }
export function linear(hex: string): number[]
export function gleamPatch(towards?: number[]): PoulpyShape
export function buildPoulpy3d(options: { side?: PoulpySide; mood?: PoulpyMood; tone?: string; face?: string; features?: (string | number)[][]; on?: { at: number[]; marks: (string | number)[][] }; stretch?: number; colours: { blue: string; ember: string; ink: string } }): Poulpy3d
export function egg(radius: number[], options?: { from?: number; to?: number; around?: number; down?: number; turn?: (p: number[]) => number[]; at?: number[] }): PoulpyShape
export function join(...shapes: PoulpyShape[]): PoulpyShape
export function facing(n: number[]): (p: number[]) => number[]
export function pathPoints(d: string): (number[][] & { closed?: boolean })[]
export function tube(points: number[][], radius: number | number[], sides?: number): PoulpyShape
