// The moves of Poulpy's moods, shared by the page of drafts (PoulpyMoodMoves.vue) and the Home mockup (MobilePoulpyMood.vue):
// the turns between moods and the little moves with which he answers a tap. A move is anything that gives a pose for a time step.
import { TURN, createJump, createTurn } from '../../../../design/poulpy-3d/motion.mjs'

export interface Move { advance: (seconds: number) => unknown }

// The three ways to spin, shared by every spin: exactly 360 degrees (no wiggle), and once: the pause after it is far longer than the move, so he
// does not start a second one.
export const spinning = (hop: number, windUp: boolean): ((clockwise: boolean) => Move) => (clockwise) => {
  const stride = (clockwise ? -360 : 360) / 24
  if (hop) { const move = createJump({ stride, wiggle: false }); Object.assign(move, { height: hop, hang: 3, pause: 99 }); return move }
  const turn = { ...TURN, pauses: [99], ...(windUp ? { stepSeconds: 0.6, windUp: { degrees: 14, seconds: 0.3 }, body: { speed: 12, damping: 0.5 } } : {}) }
  return createTurn({ stride, wiggle: false, turn } as never)
}

// Tucks while he fumes: he stays on the floor and comes back to standing.
export const tucking = (tuck: number, spread: number, hop: number, lean: number): (() => Move) => () => {
  const move = createJump()
  Object.assign(move, { tuck, height: hop, landing: 0.05, spread, lean, pause: 99 })
  return move
}
