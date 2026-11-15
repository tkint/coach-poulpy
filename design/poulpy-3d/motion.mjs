// How Poulpy moves in 3D, with no dependency, next to the shape of model.mjs.
//   design/build-poulpy-3d.mjs bakes it into the animated glTF files; the docs site's "Poulpy in 3D" page runs it live.
//
// One move so far, his idle: he turns around on the spot in short steps, stops, and goes again. His arms
// are soft, so they trail behind each step and swing a little past it before settling. They only ever
// slide on the floor: Poulpy never lifts them (brand decision 8).
// To change the move, edit TURN below, then run `node design/build-poulpy-3d.mjs`.
import { SHAPE } from './model.mjs'

export const TURN = {
  // The size of each step, in degrees, seen from above and turning to his left. They add up to one full
  // turn, so the move loops; they differ a little so he never looks like clockwork.
  steps: [24, 28, 21, 26, 22, 30, 23, 27, 20, 25, 29, 21, 22, 20, 22],
  // Seconds he stands still after each step, used in turn along the steps.
  pauses: [0.6, 0.45, 0.75, 0.5, 0.65],
  // Before a step he leans back by `degrees` for `seconds`, the way anything winds up before it moves.
  windUp: { degrees: 3, seconds: 0.125 },
  // Seconds a step is given before the pause starts. His body gets there sooner and settles in the rest.
  stepSeconds: 0.425,
  // His body is pulled to where it should face by a spring: `speed` is how eagerly (higher is snappier)
  // and `damping` how soon it calms down (1 stops dead, lower lets him rock past the mark and back).
  body: { speed: 17, damping: 0.6 },
  // Each arm bends at three places, from the head outwards. A bend is a spring pulling that length of arm
  // back in line with the one before it: softer towards the tip, so the tip trails most and settles last.
  // `drag` is the floor holding the arm back, and `limit` the most one bend may open, in degrees.
  arms: { speed: [15, 11, 8.5], damping: [0.5, 0.45, 0.4], drag: 1.5, limit: 24 },
  // No two arms are quite alike: each one's springs are this much stiffer or softer, so they fall out of step.
  armVariety: [1, 0.9, 1.07, 0.86, 1.04, 0.94, 1.1, 0.88],
  // While he turns his head dips by `depth` of its height and widens to keep its volume, then bounces back.
  squash: { depth: 0.035, atSpeed: 160, speed: 19, damping: 0.3 },
}

// Where an arm bends, as distances from his middle along the arm, and how wide each bend is spread.
export const RIG = { bends: [0.5, 0.86, 1.22], spread: 0.2 }

const RAD = Math.PI / 180
const SUBSTEP = 1 / 240 // seconds; the springs are always advanced by this much, so every run is the same

/** How long one full turn takes, in seconds. */
export function turnSeconds(turn = TURN) {
  return turn.steps.reduce((sum, _, i) => sum + turn.windUp.seconds + turn.stepSeconds + turn.pauses[i % turn.pauses.length], 0)
}

/**
 * Poulpy turning, as something to advance in time.
 * @param {{ softness?: number, stride?: number, turn?: typeof TURN }} [options] `softness` scales how loose
 *   the arms are: 1 is the design, 0 makes them stiff, 2 very floppy. `stride` scales how far each step
 *   turns: 1 is the design, 2 twice as far. Both can be changed at any time.
 * @returns {{ softness: number, stride: number, advance(seconds: number): TurnPose, pose(): TurnPose }}
 *   A pose is `{ yaw, squash, arms }`: the way he faces in radians (it keeps growing turn after turn), how
 *   much his head is squashed (negative) or stretched, and for each arm its three bends in radians.
 */
export function createTurn({ softness = 1, stride = 1, turn = TURN } = {}) {
  const arms = turn.armVariety.slice(0, SHAPE.arms.count)
  // Angles are kept from above, in radians: `at` is where the body faces, `to` where it should.
  let step = 0, phase = 0, clock = 0, base = 0, to = 0, at = 0, speed = 0, squash = 0, squashSpeed = 0
  // Each arm's three lengths, by the way they point in the room (not yet relative to each other).
  const angle = arms.map(() => [0, 0, 0]), swing = arms.map(() => [0, 0, 0])

  function tick(h) {
    // The script: wind up, step, pause, next step.
    clock += h
    const lengths = [turn.windUp.seconds, turn.stepSeconds, turn.pauses[step % turn.pauses.length]]
    while (clock >= lengths[phase]) {
      clock -= lengths[phase]
      phase = (phase + 1) % 3
      if (phase === 0) { base = to; step = (step + 1) % turn.steps.length }
    }
    to = phase === 0 ? base - turn.windUp.degrees * RAD : phase === 1 ? base + turn.steps[step] * RAD * stride : to

    // The body follows.
    const b = turn.body
    speed += (b.speed * b.speed * (to - at) - 2 * b.damping * b.speed * speed) * h
    at += speed * h

    // Each length of arm follows the one before it, and the floor holds it back.
    const loose = Math.max(softness, 0), limit = turn.arms.limit * RAD
    arms.forEach((variety, k) => {
      let parent = at, parentSwing = speed
      for (let i = 0; i < 3; i++) {
        if (loose < 0.02) { angle[k][i] = parent; swing[k][i] = parentSwing; continue }
        const w = (turn.arms.speed[i] * variety) / loose, d = turn.arms.damping[i]
        swing[k][i] += (w * w * (parent - angle[k][i]) + 2 * d * w * (parentSwing - swing[k][i]) - turn.arms.drag * loose * swing[k][i]) * h
        angle[k][i] += swing[k][i] * h
        // A bend only opens so far: past it the arm is carried along.
        const bend = angle[k][i] - parent
        if (bend > limit || bend < -limit) { angle[k][i] = parent + (bend > 0 ? limit : -limit); swing[k][i] = parentSwing }
        parent = angle[k][i]; parentSwing = swing[k][i]
      }
    })

    // The head dips with speed and springs back.
    const s = turn.squash, goal = -s.depth * Math.min(Math.abs(speed) / (s.atSpeed * RAD), 1)
    squashSpeed += (s.speed * s.speed * (goal - squash) - 2 * s.damping * s.speed * squashSpeed) * h
    squash += squashSpeed * h
  }

  let owed = 0
  const sim = {
    softness,
    stride,
    pose: () => ({ yaw: at, squash, arms: angle.map((a) => [a[0] - at, a[1] - a[0], a[2] - a[1]]) }),
    advance(seconds) {
      softness = sim.softness
      stride = sim.stride
      owed += Math.min(Math.max(seconds, 0), 1)
      while (owed >= SUBSTEP) { tick(SUBSTEP); owed -= SUBSTEP }
      return sim.pose()
    },
  }
  return sim
}

/**
 * One full turn, sampled: the same move as createTurn, written down so a file can carry it.
 * @returns {{ seconds: number, times: number[], yaw: number[], squash: number[], arms: number[][][] }}
 *   `arms[k][i]` is the list of bend `i` of arm `k` over time. The last sample is the first one a turn later.
 */
export function bakeTurn({ fps = 30, turn = TURN } = {}) {
  const seconds = turnSeconds(turn), frames = Math.round(seconds * fps), per = Math.round(1 / fps / SUBSTEP)
  const sim = createTurn({ turn })
  // Go around once first, so the springs have found their rhythm and the end meets the start.
  sim.advance(0)
  for (let i = 0; i < Math.round(seconds / SUBSTEP); i++) sim.advance(SUBSTEP)
  const start = sim.pose()
  const out = { seconds, times: [], yaw: [], squash: [], arms: start.arms.map((a) => a.map(() => [])) }
  for (let f = 0; f <= frames; f++) {
    const pose = f === frames ? { ...start, yaw: start.yaw + Math.PI * 2 } : sim.pose()
    out.times.push((f * seconds) / frames)
    out.yaw.push(pose.yaw - start.yaw)
    out.squash.push(pose.squash)
    pose.arms.forEach((a, k) => a.forEach((bend, i) => out.arms[k][i].push(bend)))
    for (let i = 0; i < per; i++) sim.advance(SUBSTEP)
  }
  return out
}

/** How a squash becomes a size: the head keeps its volume, so it widens as it dips. */
export const squashScale = (squash) => [1 - squash / 2, 1 + squash, 1 - squash / 2]

/**
 * The bones the arms bend with: for each arm, three joints in a chain from the head outwards.
 * @returns {{ name: string, arm: number, bend: number, at: number[] }[]} `at` is where the joint sits on him at rest.
 */
export function armJoints() {
  const { count, radius } = SHAPE.arms, joints = []
  for (let k = 0; k < count; k++) {
    const a = (k * Math.PI * 2) / count
    RIG.bends.forEach((r, i) => joints.push({ name: `arm_${k + 1}_bend_${i + 1}`, arm: k, bend: i, at: [Math.sin(a) * r, radius[1], Math.cos(a) * r] }))
  }
  return joints
}

/**
 * How much each point of an arm follows each of its bends, as glTF skin data.
 * @param {number} k the arm, from 0
 * @param {number[]} positions the arm's points
 * @returns {{ joints: number[], weights: number[] }} four of each per point. Joint 0 is his body; arm `k`'s
 *   bends are joints `1 + 3k` to `3 + 3k`, in the order of armJoints().
 */
export function armSkin(k, positions) {
  const a = (k * Math.PI * 2) / SHAPE.arms.count, s = Math.sin(a), c = Math.cos(a), joints = [], weights = []
  for (let p = 0; p < positions.length; p += 3) {
    const along = positions[p] * s + positions[p + 2] * c
    // `past[i]` is how far the point is past bend i: 0 before it, 1 after, eased in between.
    const past = RIG.bends.map((r) => { const t = Math.min(Math.max((along - r + RIG.spread) / (2 * RIG.spread), 0), 1); return t * t * (3 - 2 * t) })
    const share = [1 - past[0], past[0] - past[1], past[1] - past[2], past[2]]
    // glTF wants a joint that carries nothing to be written as joint 0.
    joints.push(...share.map((w, i) => (w > 0 && i > 0 ? i + 3 * k : 0)))
    weights.push(...share)
  }
  return { joints, weights }
}
