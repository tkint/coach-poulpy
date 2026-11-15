// Types for motion.mjs, for the docs site and any other TypeScript reader.
/** `yaw`: the way he faces, in radians. `squash`: his head squashed (negative) or stretched. `arms`: each arm's three bends, in radians. */
export interface TurnPose { yaw: number; squash: number; arms: number[][] }
export interface Turn { softness: number; stride: number; advance(seconds: number): TurnPose; pose(): TurnPose }
/** A turn's pose and, for a jump: `lift`, how far the base of his arms is off the floor; `lean`, his tilt to his side in radians; `head`, how far his head is out of his arms (negative is tucked in); `droop`, how far each arm's three bends hang, in radians. */
export interface JumpPose extends TurnPose { lift: number; lean: number; head: number; droop: number[][] }
/** The jump's key values can be changed while it runs: heights in units (his head's radius is about 1), `lean` in degrees, `pause` in seconds on average. */
export interface Jump { softness: number; stride: number; height: number; lift: number; tuck: number; landing: number; lean: number; pause: number; advance(seconds: number): JumpPose; pose(): JumpPose }
export interface ArmJoint { name: string; arm: number; bend: number; at: number[] }
export const TURN: {
  steps: number[]; pauses: number[]; windUp: { degrees: number; seconds: number }; stepSeconds: number
  body: { speed: number; damping: number }
  arms: { speed: number[]; damping: number[]; drag: number; limit: number }
  armVariety: number[]
  squash: { depth: number; atSpeed: number; speed: number; damping: number }
}
export const JUMP: {
  tuck: { depth: number; seconds: number }; air: { height: number; lift: number; seconds: number }; landing: { depth: number; seconds: number }
  pauses: number[]; lean: { degrees: number; speed: number; damping: number }
  head: { speed: number; damping: number }; body: { speed: number; damping: number }
  arms: { speed: number[]; damping: number[]; weight: number; limit: number }
  squash: { stretch: number; atSpeed: number; depth: number; atDepth: number; speed: number; damping: number }
}
export const RIG: { bends: number[]; spread: number }
export function turnSeconds(turn?: typeof TURN): number
export function createTurn(options?: { softness?: number; stride?: number; turn?: typeof TURN }): Turn
export function createJump(options?: { softness?: number; stride?: number; jump?: typeof JUMP; turn?: typeof TURN }): Jump
export function bakeTurn(options?: { fps?: number; turn?: typeof TURN }): { seconds: number; times: number[]; yaw: number[]; squash: number[]; arms: number[][][] }
export function squashScale(squash: number): number[]
export function armJoints(): ArmJoint[]
export function armSkin(k: number, positions: number[]): { joints: number[]; weights: number[] }
