// Types for motion.mjs, for the docs site and any other TypeScript reader.
/** `yaw`: the way he faces, in radians. `squash`: his head squashed (negative) or stretched. `arms`: each arm's three bends, in radians. */
export interface TurnPose { yaw: number; squash: number; arms: number[][] }
export interface Turn { softness: number; stride: number; advance(seconds: number): TurnPose; pose(): TurnPose }
/** A turn's pose and, for a jump: `lift`, how far he is off the floor; `lean`, his tilt to his side in radians; `head`, how far his head is tucked into his arms (negative, never out of them); `spread`, how far each arm has slid outwards; `droop`, how far each arm's three bends hang, in radians. */
export interface JumpPose extends TurnPose { lift: number; lean: number; head: number; spread: number; droop: number[][] }
/** The jump's key values can be changed while it runs: heights in units (his head's radius is about 1), `hang` scales the arms' weight, `lean` is in degrees, `pause` in seconds on average. */
export interface Jump { softness: number; stride: number; height: number; tuck: number; landing: number; spread: number; hang: number; lean: number; pause: number; advance(seconds: number): JumpPose; pose(): JumpPose }
/** A jump's pose and, for the champion: `medal`, how far it has swung up off his arm and slid to his left, in radians; `confetti`, each piece's `[x, y, z, turnX, turnY, turnZ]`, under the floor until it has flown. */
export interface CheerPose extends JumpPose { medal: { lift: number; sway: number }; confetti: number[][] }
export interface Cheer extends Jump { advance(seconds: number): CheerPose; pose(): CheerPose }
/** A turn's pose, standing still, and for the cocky wink: `wink`, from 0 (eyes wide open) to 1; `tilt`, how far his head leans to his right, in radians; `stars`, each star's size as a share of its own and how far it has turned on itself and rocked, in radians. */
export interface WinkPose extends TurnPose { wink: number; tilt: number; stars: { size: number; spin: number; rock: number }[] }
export interface Wink { advance(seconds: number): WinkPose; pose(): WinkPose }
export interface ArmJoint { name: string; arm: number; bend: number; at: number[] }
export const TURN: {
  steps: number[]; pauses: number[]; windUp: { degrees: number; seconds: number }; stepSeconds: number
  body: { speed: number; damping: number }
  arms: { speed: number[]; damping: number[]; drag: number; limit: number }
  armVariety: number[]
  squash: { depth: number; atSpeed: number; speed: number; damping: number }
}
export const JUMP: {
  tuck: { depth: number; seconds: number; spread: number }; air: { height: number; seconds: number }; landing: { depth: number; seconds: number }
  pauses: number[]; lean: { degrees: number; speed: number; damping: number }
  head: { speed: number; damping: number }
  arms: { speed: number[]; damping: number[]; weight: number; limit: number }
  squash: { stretch: number; atSpeed: number; depth: number; atDepth: number; speed: number; damping: number }
}
export const CHEER: {
  jump: typeof JUMP
  medal: { gravity: number; damping: number; bounce: number; most: number; sway: { slide: number; speed: number; damping: number; most: number } }
  confetti: { pieces: number; burst: number; from: number[]; high: number; speed: number[]; out: number; gravity: number; drag: number; flutter: number; spin: number }
}
export const WINK: {
  open: number; shut: number; hold: number; reopen: number
  head: { degrees: number; dip: number; speed: number; damping: number }
  stars: { apart: number; speed: number; damping: number; twinkle: number; beats: number; spin: number; rock: number }
}
export const RIG: { bends: number[]; spread: number }
export function turnSeconds(turn?: typeof TURN): number
export function createTurn(options?: { softness?: number; stride?: number; turn?: typeof TURN }): Turn
export function createJump(options?: { softness?: number; stride?: number; jump?: typeof JUMP; turn?: typeof TURN }): Jump
export function createCheer(options?: { softness?: number; stride?: number; cheer?: typeof CHEER; turn?: typeof TURN }): Cheer
export function createWink(options?: { wink?: typeof WINK }): Wink
export function bakeTurn(options?: { fps?: number; turn?: typeof TURN }): { seconds: number; times: number[]; yaw: number[]; squash: number[]; arms: number[][][] }
export function squashScale(squash: number): number[]
export function armJoints(): ArmJoint[]
export function armSkin(k: number, positions: number[]): { joints: number[]; weights: number[] }
