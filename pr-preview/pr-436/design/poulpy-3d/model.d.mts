// Types for model.mjs, for the docs site and any other TypeScript reader.
export interface PoulpyShape { positions: number[]; normals: number[]; indices: number[] }
export interface PoulpyPart extends PoulpyShape { name: string; material: string }
export interface PoulpyMaterial { colour: number[]; flat?: boolean; outline?: number[] }
export interface Poulpy3d { parts: PoulpyPart[]; materials: Record<string, PoulpyMaterial>; height: number }
export type PoulpySide = 'blue' | 'ember'
export const SHAPE: { head: { radius: number[]; centreY: number }; arms: { count: number; radius: number[]; ring: number }; liningLine: number; face: { y: number; scale: number; spread: number; eyes: number } }
export const FACES: Record<PoulpySide, unknown>
export const LOOK: { light: number[]; gleam: { towards: number[]; size: number[]; tilt: number; white: number }; shade: number; outline: { width: number; ink: number }; cheek: Record<PoulpySide, { colour: string; strength: number }> }
export function linear(hex: string): number[]
export function gleamPatch(towards?: number[]): PoulpyShape
export function buildPoulpy3d(options: { side?: PoulpySide; colours: { blue: string; ember: string; ink: string } }): Poulpy3d
