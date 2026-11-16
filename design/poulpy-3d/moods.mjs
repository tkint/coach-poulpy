// How Home's Poulpy changes mood and answers a tap while he is annoyed or fuming (the moves validated on the workshop's "Poulpy moves" pages):
// the turns between moods and the little moves with which he answers a tap, and how his fumes drift. Plain JavaScript with no dependency: the workshop's
// pages and the Home mockup play these live, and design/build-poulpy-pictures.mjs bakes them into frames for the phones.
import { TURN, createJump, createTurn } from './motion.mjs'

// The three ways to spin, shared by every spin: exactly 360 degrees (no wiggle), and once: the pause after it is far longer than the move, so he
// does not start a second one.
export const spinning = (hop, windUp) => (clockwise) => {
  const stride = (clockwise ? -360 : 360) / 24
  if (hop) { const move = createJump({ stride, wiggle: false }); Object.assign(move, { height: hop, hang: 3, pause: 99 }); return move }
  const turn = { ...TURN, pauses: [99], ...(windUp ? { stepSeconds: 0.6, windUp: { degrees: 14, seconds: 0.3 }, body: { speed: 12, damping: 0.5 } } : {}) }
  return createTurn({ stride, wiggle: false, turn })
}

// Tucks while he is angry: he stays on the floor and comes back to standing.
export const tucking = (tuck, spread, hop, lean) => () => {
  const move = createJump()
  Object.assign(move, { tuck, height: hop, landing: 0.05, spread, lean, pause: 99 })
  return move
}

/**
 * The moves of the phones, by name. `from` and `to` are the moods he is in before and after (happy, annoyed or fuming), `swap` when his face changes
 * ('half': when he is half way round, 'start': it does not change), `seconds` how long the move lasts, `boost` how hard his fumes puff while he
 * ends up fuming, and `make()` gives the move to advance in time.
 */
export const MOOD_MOVES = {
  toAnnoyed: { from: 'happy', to: 'annoyed', swap: 'half', seconds: 1.5, make: () => spinning(0, false)(false) },
  toFuming: { from: 'annoyed', to: 'fuming', swap: 'half', seconds: 1.5, boost: 0.4, make: () => spinning(0, false)(false) },
  shake: { from: 'annoyed', to: 'annoyed', swap: 'start', seconds: 1.4, make: tucking(0.05, 0.03, 0, 12) },
  shudder: { from: 'fuming', to: 'fuming', swap: 'start', seconds: 1.9, boost: 2, make: tucking(0.16, 0.12, 0.05, 9) },
  calmDown: { from: 'fuming', to: 'annoyed', swap: 'half', seconds: 1.5, make: () => spinning(0, false)(true) },
  backToNormal: { from: 'annoyed', to: 'happy', swap: 'half', seconds: 1.7, make: () => spinning(0.45, false)(true) },
}

/**
 * How a puff of his fumes has drifted from where it is drawn: `index` is the part's place among the flat effects behind him (each two parts are one
 * oval), `seconds` any clock that keeps running and `boost` how much harder they puff (0 when he is calm). Returns [x, y] in the card's units.
 */
export function fumeDrift(index, seconds, boost = 0) {
  const t = seconds * (2.2 + boost * 2) + Math.floor(index / 4) * 1.7, sway = 0.05 + 0.1 * boost
  return [Math.sin(t * 0.8) * sway * 0.6, Math.sin(t) * sway + boost * 0.05]
}

/** The boost of his fumes `elapsed` seconds into `move`: full while he fumes, easing off over the last half second. */
export const fumeBoost = (move, elapsed, fuming) => (fuming ? (move.boost ?? 0) * Math.max(0, Math.min(1, 1 - (elapsed - move.seconds + 0.5) / 0.5)) : 0)
