// Types for motion.mjs, for the docs site and any other TypeScript reader.
/** `yaw`: the way he faces, in radians. `squash`: his head squashed (negative) or stretched. `arms`: each arm's three bends, in radians. */
export interface TurnPose { yaw: number; squash: number; arms: number[][] }
export interface Turn { softness: number; advance(seconds: number): TurnPose; pose(): TurnPose }
export interface ArmJoint { name: string; arm: number; bend: number; at: number[] }
export const TURN: {
  steps: number[]; pauses: number[]; windUp: { degrees: number; seconds: number }; stepSeconds: number
  body: { speed: number; damping: number }
  arms: { speed: number[]; damping: number[]; drag: number; limit: number }
  armVariety: number[]
  squash: { depth: number; atSpeed: number; speed: number; damping: number }
}
export const RIG: { bends: number[]; spread: number }
export function turnSeconds(turn?: typeof TURN): number
export function createTurn(options?: { softness?: number; turn?: typeof TURN }): Turn
export function bakeTurn(options?: { fps?: number; turn?: typeof TURN }): { seconds: number; times: number[]; yaw: number[]; squash: number[]; arms: number[][][] }
export function squashScale(squash: number): number[]
export function armJoints(): ArmJoint[]
export function armSkin(k: number, positions: number[]): { joints: number[]; weights: number[] }
