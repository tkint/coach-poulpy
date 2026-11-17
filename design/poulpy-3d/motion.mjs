// How Poulpy moves in 3D, with no dependency, next to the shape of model.mjs.
//   design/build-poulpy-3d.mjs bakes it into the animated glTF files; the docs site's "Poulpy in 3D" page runs it live.
//
// Four moves. His idle, TURN: he turns around on the spot in short steps, stops, and goes again. JUMP:
// little jumps on the spot, which can also turn him a step each. CHEER, the champion's jumps of joy:
// higher and closer together, his medal bouncing on its ribbon and confetti flying at each one. And WINK,
// the cocky one's: eyes wide open, then a wink and a smirk as stars pop up around him and twinkle. His arms are soft, so they trail behind
// a step and swing a little past it before settling, and they hang under him when he jumps. He never
// raises them himself (brand decisions 8 and 15): on the floor they slide, in the air they droop.
// The cook's idle, CHEF, puffs him up and twitches his moustache (looks/chef.mjs).
// To change a move, edit TURN, JUMP, CHEER, WINK or CHEF below, then run `node design/build-poulpy-3d.mjs`.
import { SHAPE, linear } from './model.mjs'
import { CONFETTI, medalHinge, medalReach, STARS } from './poses.mjs'

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

export const JUMP = {
  // Before a jump he tucks his head `depth` down into his arms and holds it for `seconds`. His arms stay
  // flat on the floor and widen: at the full depth each slides `spread` outwards.
  tuck: { depth: 0.16, seconds: 0.26, spread: 0.14 },
  // Then he goes up by `height` and comes back in `seconds`. His arms are part of him, so their base goes
  // up with his head; their tips hang lower. One unit is about his head's radius.
  air: { height: 0.34, seconds: 0.46 },
  // As he lands his head goes `depth` down into his arms for `seconds`, and they widen again, then he stands.
  landing: { depth: 0.1, seconds: 0.18 },
  // Seconds he stands still between two jumps, used in turn.
  pauses: [0.6, 0.45, 0.7, 0.45],
  // In the air he tilts `degrees` to one side, the other side at the next jump.
  lean: { degrees: 7, speed: 13, damping: 0.5 },
  // His head is pulled to where it should be by a spring, as his body is in TURN.
  head: { speed: 24, damping: 0.55 },
  // Up and down, each arm hangs at the same three places it bends at in TURN: a spring holds each length level
  // with the one before it, softer towards the tip, `weight` pulls it down and the floor stops it.
  // `limit` is the steepest a length of arm may hang, in degrees: nearly straight down when it has to.
  arms: { speed: [21, 16, 12.5], damping: [0.5, 0.45, 0.4], weight: 22, limit: 78 },
  // His head stretches by `stretch` of its height when it moves at `atSpeed` units a second, and squashes by
  // `depth` when it is `atDepth` down in his arms. It widens or thins to keep its volume.
  squash: { stretch: 0.05, atSpeed: 2.4, depth: 0.07, atDepth: 0.16, speed: 22, damping: 0.4 },
}

export const CHEER = {
  // The champion's jump is JUMP, higher, leaning more and with shorter pauses: he cannot hold still.
  jump: { ...JUMP, air: { height: 0.5, seconds: 0.5 }, pauses: [0.3, 0.2, 0.35, 0.2], lean: { ...JUMP.lean, degrees: 9 } },
  // His medal hangs on its ribbon and rests on his front arm. `gravity` pulls it down, in units a second squared:
  // when he drops faster than that it floats up, by `most` radians at the most, and falls back on his arm with a
  // `bounce` (the share of its speed it keeps). `damping` is the air and the ribbon calming it down.
  // Sideways it is a spring: it slides to the side he tilts to, by `slide` of his tilt, and wobbles back.
  medal: { gravity: 15, damping: 2.2, bounce: 0.35, most: 0.6, sway: { slide: 0.9, speed: 10, damping: 0.22, most: 0.22 } },
  // Confetti: `burst` pieces fly up at each jump, out of `pieces` in all, so the oldest on the floor go again.
  // They start `from` so far from his middle, at up to `high`, leave at `speed` units a second (lowest, highest),
  // mostly upwards and `out` of that sideways. `gravity` and `drag` make them fall slowly, `flutter` pushes them
  // from side to side as they fall and `spin` is how fast they tumble, in radians a second.
  confetti: { pieces: 72, burst: 24, from: [1.5, 2.5], high: 0.6, speed: [8, 13], out: 0.28, gravity: 5, drag: 2.8, flutter: 2.2, spin: 9 },
}

export const WINK = {
  // He looks, eyes wide open, for `open` seconds, shuts one eye in `shut`, holds the wink for `hold` and opens it again in `reopen`.
  open: 1.3, shut: 0.13, hold: 1.7, reopen: 0.22,
  // With the wink his head leans `degrees` to the side of the eye he shuts, on a spring, and dips by `dip` of its height.
  head: { degrees: 5, dip: 0.035, speed: 16, damping: 0.4 },
  // The stars come with the wink, one every `apart` seconds: each grows on a spring that takes it a little past
  // its size and back, and leaves the same way when he opens his eye. While it is there it twinkles: it grows
  // and shrinks by `twinkle` of its size, `beats` times a second, turns on itself at `spin` radians a second and
  // rocks by `rock` radians. No two stars keep the same time.
  stars: { apart: 0.09, speed: 20, damping: 0.35, twinkle: 0.22, beats: 1.6, spin: 2.4, rock: 0.3 },
}

// The cook's idle (looks/chef.mjs): he stands still for `rest` seconds, then puffs himself up, sure of his sauce. His chin
// goes up by `degrees`, his head tipping back around the point `pivot` high above the floor, and his head grows `puff` taller,
// both on the `head` spring. He holds it for `hold` seconds, and `delay` into it his moustache twitches `count` times, each
// twitch taking `seconds`; then he settles back.
export const CHEF = {
  rest: 1.8, hold: 1.5,
  chin: { degrees: 6, pivot: 0.6 },
  puff: 0.035,
  head: { speed: 11, damping: 0.5 },
  twitch: { count: 2, seconds: 0.2, delay: 0.3 },
}

// Where an arm bends, as distances from his middle along the arm, and how wide each bend is spread.
export const RIG = { bends: [0.5, 0.86, 1.22], spread: 0.2 }

const RAD = Math.PI / 180
const SUBSTEP = 1 / 240 // seconds; the springs are always advanced by this much, so every run is the same

// The arms' sideways swing, shared by every move: each length of arm follows the one before it, and the
// floor holds it back. `tick` is given where the body faces, how fast it turns and how loose the arms are.
function armSwing(turn) {
  const arms = turn.armVariety.slice(0, SHAPE.arms.count)
  // Each arm's three lengths, by the way they point in the room (not yet relative to each other).
  const angle = arms.map(() => [0, 0, 0]), swing = arms.map(() => [0, 0, 0])
  return {
    /** Each arm's three bends, relative to each other, for a body facing `at`. */
    bends: (at) => angle.map((a) => [a[0] - at, a[1] - a[0], a[2] - a[1]]),
    tick(h, at, speed, softness) {
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
    },
  }
}

/** How long one full turn takes, in seconds. */
export function turnSeconds(turn = TURN) {
  return turn.steps.reduce((sum, _, i) => sum + turn.windUp.seconds + turn.stepSeconds + turn.pauses[i % turn.pauses.length], 0)
}

/**
 * Poulpy turning, as something to advance in time.
 * @param {{ softness?: number, stride?: number, wiggle?: boolean, turn?: typeof TURN }} [options] `softness` scales how loose
 *   the arms are: 1 is the design, 0 makes them stiff, 2 very floppy. `stride` scales how far each step
 *   turns: 1 is the design, 2 twice as far, and a negative one turns him the other way. `wiggle` (on unless told) keeps the
 *   steps a little different from each other, as designed; off, every step is exactly the same. All can be changed at any time.
 * @returns {{ softness: number, stride: number, wiggle: boolean, advance(seconds: number): TurnPose, pose(): TurnPose }}
 *   A pose is `{ yaw, squash, arms }`: the way he faces in radians (it keeps growing turn after turn), how
 *   much his head is squashed (negative) or stretched, and for each arm its three bends in radians.
 */
export function createTurn({ softness = 1, stride = 1, wiggle = true, turn = TURN } = {}) {
  // Angles are kept from above, in radians: `at` is where the body faces, `to` where it should.
  let step = 0, phase = 0, clock = 0, base = 0, to = 0, at = 0, speed = 0, squash = 0, squashSpeed = 0
  const arms = armSwing(turn)

  function tick(h) {
    // The script: wind up, step, pause, next step.
    clock += h
    const lengths = [turn.windUp.seconds, turn.stepSeconds, turn.pauses[step % turn.pauses.length]]
    while (clock >= lengths[phase]) {
      clock -= lengths[phase]
      phase = (phase + 1) % 3
      if (phase === 0) { base = to; step = (step + 1) % turn.steps.length }
    }
    to = phase === 0 ? base - turn.windUp.degrees * RAD * (stride < 0 ? -1 : 1) : phase === 1 ? base + stepDegrees(turn, step, stride, wiggle) * RAD : to

    // The body follows.
    const b = turn.body
    speed += (b.speed * b.speed * (to - at) - 2 * b.damping * b.speed * speed) * h
    at += speed * h

    arms.tick(h, at, speed, softness)

    // The head dips with speed and springs back.
    const s = turn.squash, goal = -s.depth * Math.min(Math.abs(speed) / (s.atSpeed * RAD), 1)
    squashSpeed += (s.speed * s.speed * (goal - squash) - 2 * s.damping * s.speed * squashSpeed) * h
    squash += squashSpeed * h
  }

  let owed = 0
  const sim = {
    softness,
    stride,
    wiggle,
    pose: () => ({ yaw: at, squash, arms: arms.bends(at) }),
    advance(seconds) {
      softness = sim.softness
      stride = sim.stride
      wiggle = sim.wiggle
      owed += Math.min(Math.max(seconds, 0), 1)
      while (owed >= SUBSTEP) { tick(SUBSTEP); owed -= SUBSTEP }
      return sim.pose()
    },
  }
  return sim
}

/**
 * Poulpy turned by something else (the loader's swing), his arms following loose behind it as they do in createTurn.
 * @param {{ softness?: number, turn?: typeof TURN }} [options]
 * @returns {{ softness: number, advance(seconds: number, yaw: number): TurnPose }} give it the time since the last call and where he
 *   should face now, in radians seen from above; it works out how fast he is turning and lets each arm bend and trail.
 */
export function createFollow({ softness = 1, turn = TURN } = {}) {
  const arms = armSwing(turn)
  let at = null, owed = 0
  const sim = {
    softness,
    advance(seconds, yaw) {
      if (at === null) at = yaw
      const span = Math.min(Math.max(seconds, 0), 1), from = at, speed = span > 0 ? (yaw - from) / span : 0
      owed += span
      let done = 0
      while (owed >= SUBSTEP) {
        done += SUBSTEP
        arms.tick(SUBSTEP, from + (speed * done), speed, sim.softness)
        owed -= SUBSTEP
      }
      at = yaw
      return { yaw, squash: 0, arms: arms.bends(yaw) }
    },
  }
  return sim
}

// How far a step turns, in degrees: with `wiggle` it is the design's step (they differ a little, 20 to 30, so he does not look
// mechanical) times `stride`; without it every step is the average of them, times `stride`, so a step of 360° is exactly 360°.
const stepDegrees = (turn, index, stride, wiggle) => (wiggle ? turn.steps[index % turn.steps.length] : turn.steps.reduce((sum, d) => sum + d, 0) / turn.steps.length) * stride

/**
 * Poulpy jumping on the spot, as something to advance in time.
 * @param {{ softness?: number, stride?: number, wiggle?: boolean, jump?: typeof JUMP, turn?: typeof TURN }} [options] `softness`
 *   scales how loose the arms are, as in createTurn. `stride` makes each jump turn him too, by a step of
 *   `turn` scaled as in createTurn (and `wiggle` as there): 0, the default, jumps on the spot.
 * @returns The move. `softness`, `stride`, `wiggle` and the jump's key values (`height`, `tuck`, `landing`, `spread`,
 *   `hang`, which scales the arms' weight, `lean` in degrees and `pause` in seconds on average) can be changed
 *   at any time. A pose is a turn's pose and `{ lift, lean, head, spread, droop }`: how far he is off the
 *   floor, how far he tilts to his side in radians, how far his head is tucked into his arms (negative, never
 *   out of them), how far each arm has slid outwards, and for each arm how far each of its three bends hangs, in radians.
 */
export function createJump({ softness = 1, stride = 0, wiggle = true, jump = JUMP, turn = TURN } = {}) {
  const count = SHAPE.arms.count, variety = turn.armVariety.slice(0, count)
  const rest = jump.pauses.reduce((sum, p) => sum + p, 0) / jump.pauses.length
  // Where each length of arm ends, from his middle, and how long it is.
  const ends = [...RIG.bends.slice(1), SHAPE.arms.ring + SHAPE.arms.radius[2]], lengths = ends.map((r, i) => r - RIG.bends[i])
  const side = variety.map((_, k) => Math.sin((k * Math.PI * 2) / count)) // how far to his left each arm points
  let hop = 0, phase = 0, clock = 0
  // Heights are from where he stands. His arms hold on to his head: their base, `body`, goes up with it,
  // and stays on the floor when it tucks in.
  let head = 0, headSpeed = 0, body = 0, lean = 0, leanSpeed = 0, squash = 0, squashSpeed = 0
  let base = 0, to = 0, at = 0, speed = 0
  const swing = armSwing(turn)
  // The end of each length of arm: its height in the room, its speed, and where it was asked to be last time.
  const high = variety.map(() => [0, 0, 0]), rise = variety.map(() => [0, 0, 0]), asked = variety.map(() => [0, 0, 0])
  const spring = (s, goal, value, rate) => s.speed * s.speed * (goal - value) - 2 * s.damping * s.speed * rate

  function tick(h) {
    // The script: tuck, jump, land, pause, next jump.
    clock += h
    const seconds = [jump.tuck.seconds, jump.air.seconds, jump.landing.seconds, (jump.pauses[hop % jump.pauses.length] * sim.pause) / rest]
    while (clock >= seconds[phase]) {
      clock -= seconds[phase]
      phase = (phase + 1) % 4
      if (phase === 0) { base = to; hop++ }
    }
    const through = clock / seconds[phase], turning = stride !== 0
    const goal = phase === 0 ? -sim.tuck : phase === 1 ? sim.height * 4 * through * (1 - through) : phase === 2 ? -sim.landing : 0
    // He turns for as long as he is in the air: from his wind-up to the full step, eased, so he lands as the turn ends.
    const wind = base - turn.windUp.degrees * RAD * (stride < 0 ? -1 : 1), full = base + stepDegrees(turn, hop, stride, wiggle) * RAD
    if (turning) to = phase === 0 ? wind : phase === 1 ? wind + (full - wind) * through * through * (3 - 2 * through) : full
    else to = base

    // His head follows the script and takes the base of his arms up with it.
    headSpeed += spring(jump.head, goal, head, headSpeed) * h
    head += headSpeed * h
    leanSpeed += spring(jump.lean, phase === 1 ? sim.lean * RAD * (hop % 2 ? -1 : 1) : 0, lean, leanSpeed) * h
    lean += leanSpeed * h
    body = Math.max(head, 0)
    const tilt = Math.sin(lean)

    // Turning, when he does, is the same as in createTurn.
    speed += (turn.body.speed * turn.body.speed * (to - at) - 2 * turn.body.damping * turn.body.speed * speed) * h
    at += speed * h
    swing.tick(h, at, speed, softness)

    // Each length of arm hangs from the one before it, and the floor holds it up.
    const loose = Math.max(softness, 0), limit = Math.sin(jump.arms.limit * RAD), weight = jump.arms.weight * sim.hang
    variety.forEach((v, k) => {
      let off = 0 // how far the length before this one hangs off the straight arm
      for (let i = 0; i < 3; i++) {
        const straight = body + tilt * side[k] * ends[i], want = straight + off, wantSpeed = (want - asked[k][i]) / h
        asked[k][i] = want
        if (loose < 0.02) { high[k][i] = want; rise[k][i] = wantSpeed } else {
          const w = (jump.arms.speed[i] * v) / loose, d = jump.arms.damping[i]
          rise[k][i] += (w * w * (want - high[k][i]) + 2 * d * w * (wantSpeed - rise[k][i]) - weight) * h
          high[k][i] += rise[k][i] * h
        }
        // A length only hangs so steep, and nothing goes through the floor.
        const most = lengths[i] * limit, bend = high[k][i] - want
        if (bend > most || bend < -most) { high[k][i] = want + (bend > 0 ? most : -most); rise[k][i] = wantSpeed }
        if (high[k][i] < 0) { high[k][i] = 0; rise[k][i] = Math.max(rise[k][i], 0) }
        off = high[k][i] - straight
      }
    })

    // The head stretches as it moves and squashes when it is down in the arms.
    const s = jump.squash, deep = Math.min(Math.max((body - head) / s.atDepth, 0), 1)
    squashSpeed += spring(s, s.stretch * Math.min(Math.abs(headSpeed) / s.atSpeed, 1) - s.depth * deep, squash, squashSpeed) * h
    squash += squashSpeed * h
  }

  // How far each bend hangs: the slope of its length of arm, less the slope of the one before.
  const droop = () => high.map((ys, k) => {
    let before = 0, slope = 0
    return ys.map((y, i) => {
      const off = y - body - Math.sin(lean) * side[k] * ends[i], next = Math.asin(Math.min(Math.max((before - off) / lengths[i], -1), 1)), bend = next - slope
      before = off; slope = next
      return bend
    })
  })

  let owed = 0
  const sim = {
    softness,
    stride,
    wiggle,
    height: jump.air.height,
    tuck: jump.tuck.depth,
    landing: jump.landing.depth,
    spread: jump.tuck.spread,
    hang: 1,
    lean: jump.lean.degrees,
    pause: rest,
    pose: () => ({ yaw: at, squash, arms: swing.bends(at), lift: body, lean, head: head - body, spread: sim.tuck > 0 ? sim.spread * Math.min(Math.max((body - head) / sim.tuck, 0), 1.5) : 0, droop: droop() }),
    advance(seconds) {
      softness = sim.softness
      stride = sim.stride
      wiggle = sim.wiggle
      owed += Math.min(Math.max(seconds, 0), 1)
      while (owed >= SUBSTEP) { tick(SUBSTEP); owed -= SUBSTEP }
      return sim.pose()
    },
  }
  return sim
}

/**
 * Poulpy the champion jumping for joy, as something to advance in time: createJump with the jump of `cheer`,
 * his medal and the confetti. Every run is the same: the confetti's chances are drawn from a fixed list.
 * @param {{ softness?: number, stride?: number, cheer?: typeof CHEER, turn?: typeof TURN, cycle?: number }} [options] as for createJump. With `cycle`, the
 *   confetti's chances start again every `cycle` jumps (and `cheer.confetti.pieces` is `cycle` bursts of pieces), so that once he has jumped
 *   that many times to the pauses of his jump he repeats himself exactly: what a loop made of those jumps needs.
 * @returns The jump, whose poses also have `medal`, `{ lift, sway }`: how far it has swung up off his arm around
 *   its hinge (medalHinge in poses.mjs) and how far it has slid to his left around him, in radians; and `confetti`,
 *   for each piece `[x, y, z, turnX, turnY, turnZ]`: where it is in the room and how it is turned, in radians.
 *   A piece that has not flown yet is under the floor.
 */
export function createCheer({ cheer = CHEER, cycle = 0, ...options } = {}) {
  const jump = createJump({ ...options, jump: cheer.jump })
  const { advance: hop, pose: stand } = jump
  // A fixed list of chances (mulberry32), so no two pieces are alike and every run is the same.
  const SEED = 20261115
  let seed = SEED, bursts = 0
  const chance = () => {
    seed = (seed + 0x6d2b79f5) | 0
    let t = Math.imul(seed ^ (seed >>> 15), 1 | seed)
    t = (t + Math.imul(t ^ (t >>> 7), 61 | t)) ^ t
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296
  }
  const between = ([low, high]) => low + (high - low) * chance()
  const m = cheer.medal, c = cheer.confetti
  // The medal: `below` is how far under level it hangs from its hinge, `sway` how far it has slid sideways.
  let below = medalReach.rest, fall = 0, sway = 0, swaySpeed = 0, height = 0, climb = 0
  const pieces = Array.from({ length: c.pieces }, () => ({ at: [0, -1, 0], speed: [0, 0, 0], turn: [0, 0, 0], spin: [0, 0, 0], beat: 0, flying: false }))
  let next = 0, up = false

  function tick(h) {
    const pose = hop(SUBSTEP), was = climb
    climb = (pose.lift + pose.head - height) / h
    height = pose.lift + pose.head
    // The hinge goes up and down with his head: what the medal feels is gravity and that.
    fall += (((m.gravity + (climb - was) / h) / medalReach.length) * Math.cos(below) - m.damping * fall) * h
    below += fall * h
    if (below > medalReach.rest) { below = medalReach.rest; fall = fall > 0.6 ? -fall * m.bounce : Math.min(fall, 0) }
    if (below < medalReach.rest - m.most) { below = medalReach.rest - m.most; fall = Math.max(fall, 0) }
    swaySpeed += (m.sway.speed * m.sway.speed * (m.sway.slide * pose.lean - sway) - 2 * m.sway.damping * m.sway.speed * swaySpeed) * h
    sway = Math.min(Math.max(sway + swaySpeed * h, -m.sway.most), m.sway.most)

    // A burst as he leaves the floor.
    if (pose.lift > 0.02 && !up) {
      if (cycle && bursts % cycle === 0) seed = SEED
      bursts++
      for (let i = 0; i < c.burst; i++) {
        const piece = pieces[next], around = chance() * Math.PI * 2, far = between(c.from), speed = between(c.speed), out = (chance() * 2 - 1) * Math.PI
        next = (next + 1) % pieces.length
        piece.at = [Math.sin(around) * far, chance() * c.high, Math.cos(around) * far]
        piece.speed = [Math.sin(out) * speed * c.out, speed, Math.cos(out) * speed * c.out]
        piece.turn = [0, 1, 2].map(() => chance() * Math.PI * 2)
        piece.spin = [0, 1, 2].map(() => (chance() * 2 - 1) * c.spin)
        piece.beat = chance() * Math.PI * 2
        piece.flying = true
      }
    }
    up = pose.lift > 0.02
    for (const piece of pieces) {
      if (!piece.flying) continue
      // Falling, a piece is pushed from side to side, across the way it is turned.
      piece.beat += 6 * h
      const push = piece.speed[1] < 0 ? c.flutter * Math.sin(piece.beat) : 0
      piece.speed[0] += (push * Math.cos(piece.turn[1]) - c.drag * piece.speed[0]) * h
      piece.speed[1] += (-c.gravity - c.drag * piece.speed[1]) * h
      piece.speed[2] += (-push * Math.sin(piece.turn[1]) - c.drag * piece.speed[2]) * h
      for (let k = 0; k < 3; k++) { piece.at[k] += piece.speed[k] * h; piece.turn[k] += piece.spin[k] * h }
      // On the floor it lies flat and stays there until it flies again.
      if (piece.at[1] <= 0.012 && piece.speed[1] < 0) { piece.at[1] = 0.012; piece.turn[0] = Math.PI / 2; piece.turn[2] = 0; piece.flying = false }
    }
  }

  let owed = 0
  jump.pose = () => ({ ...stand(), medal: { lift: medalReach.rest - below, sway }, confetti: pieces.map((piece) => [...piece.at, ...piece.turn]) })
  jump.advance = (seconds) => {
    owed += Math.min(Math.max(seconds, 0), 1)
    while (owed >= SUBSTEP) { tick(SUBSTEP); owed -= SUBSTEP }
    return jump.pose()
  }
  return jump
}

/**
 * The cocky one winking, as something to advance in time.
 * @param {{ side?: 'left' | 'right', wink?: typeof WINK }} [options] `side` is the eye he shuts, his own; his head leans that way.
 * @returns The move; `side` can be changed at any time. A pose is a turn's pose, standing still, and `{ wink, tilt, stars }`: how far he is into
 *   the wink, from 0 (eyes wide open) to 1, for winkFace in poses.mjs; how far his head leans to his right, in
 *   radians (to his left when negative); and for each star of STARS in poses.mjs `{ size, spin, rock }`: its size as a share of its own
 *   (0 when it is not there), and how far it has turned on itself and rocked, in radians.
 */
export function createWink({ side = 'right', wink = WINK } = {}) {
  const arms = Array.from({ length: SHAPE.arms.count }, () => [0, 0, 0]), ease = (t) => t * t * (3 - 2 * t)
  const seconds = [wink.open, wink.shut, wink.hold, wink.reopen]
  let phase = 0, clock = 0, time = 0, shut = 0, tilt = 0, tiltSpeed = 0, squash = 0, squashSpeed = 0
  const stars = STARS.map(() => ({ size: 0, growth: 0 }))
  const spring = (s, goal, value, rate) => s.speed * s.speed * (goal - value) - 2 * s.damping * s.speed * rate

  function tick(h) {
    clock += h
    time += h
    while (clock >= seconds[phase]) { clock -= seconds[phase]; phase = (phase + 1) % 4 }
    const through = clock / seconds[phase]
    shut = phase === 0 ? 0 : phase === 1 ? ease(through) : phase === 2 ? 1 : 1 - ease(through)
    const winking = phase === 1 || phase === 2
    tiltSpeed += spring(wink.head, winking ? wink.head.degrees * RAD * (sim.side === 'left' ? -1 : 1) : 0, tilt, tiltSpeed) * h
    tilt += tiltSpeed * h
    squashSpeed += spring(wink.head, winking ? -wink.head.dip : 0, squash, squashSpeed) * h
    squash += squashSpeed * h
    // Each star waits its turn after the eye has shut.
    stars.forEach((star, i) => {
      const there = phase === 2 && clock >= i * wink.stars.apart
      star.growth += spring(wink.stars, there ? 1 : 0, star.size, star.growth) * h
      star.size = Math.max(star.size + star.growth * h, 0)
    })
  }

  let owed = 0
  const sim = {
    side,
    pose: () => ({
      yaw: 0, squash, arms, wink: shut, tilt,
      stars: stars.map((star, i) => {
        // Each star keeps its own time: a little faster or slower, and starting elsewhere in its beat.
        const own = 1 + 0.17 * ((i * 5) % 4 - 1.5), beat = time * own + i * 1.9
        return { size: star.size * (1 + wink.stars.twinkle * Math.sin(beat * wink.stars.beats * Math.PI * 2)), spin: beat * wink.stars.spin * (i % 2 ? -1 : 1), rock: wink.stars.rock * Math.sin(beat * 2.3) }
      }),
    }),
    advance(seconds) {
      owed += Math.min(Math.max(seconds, 0), 1)
      while (owed >= SUBSTEP) { tick(SUBSTEP); owed -= SUBSTEP }
      return sim.pose()
    },
  }
  return sim
}

/**
 * The cook's idle, as something to advance in time.
 * @param {{ chef?: typeof CHEF }} [options]
 * @returns The move. A pose is a turn's pose, standing still, and `{ chin, twitch }`: how far his chin is up, in radians,
 *   his head tipping back around CHEF.chin.pivot; and how far into a moustache twitch he is, from 0 to 1, for chefFace in looks/chef.mjs.
 */
export function createChef({ chef = CHEF } = {}) {
  const arms = Array.from({ length: SHAPE.arms.count }, () => [0, 0, 0])
  const spring = (s, goal, value, rate) => s.speed * s.speed * (goal - value) - 2 * s.damping * s.speed * rate
  let clock = 0, chin = 0, chinSpeed = 0, squash = 0, squashSpeed = 0, twitch = 0

  function tick(h) {
    clock = (clock + h) % (chef.rest + chef.hold)
    const puffed = clock >= chef.rest, into = clock - chef.rest - chef.twitch.delay
    chinSpeed += spring(chef.head, puffed ? chef.chin.degrees * RAD : 0, chin, chinSpeed) * h
    chin += chinSpeed * h
    squashSpeed += spring(chef.head, puffed ? chef.puff : 0, squash, squashSpeed) * h
    squash += squashSpeed * h
    // Each twitch goes up and back down in one smooth bump.
    const which = Math.floor(into / chef.twitch.seconds)
    twitch = puffed && into >= 0 && which < chef.twitch.count ? Math.sin((Math.PI * (into - which * chef.twitch.seconds)) / chef.twitch.seconds) ** 2 : 0
  }

  let owed = 0
  const sim = {
    pose: () => ({ yaw: 0, squash, arms, chin, twitch }),
    advance(seconds) {
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

// ---- Posing him without a 3D engine ----
// 3 by 3 matrices (row by row) and an offset make a placement; enough to move him as the rig of the scene does.
const m3 = (a, b) => [0, 1, 2].flatMap((r) => [0, 1, 2].map((c) => a[r * 3] * b[c] + a[r * 3 + 1] * b[3 + c] + a[r * 3 + 2] * b[6 + c]))
const m3v = (m, v) => [0, 1, 2].map((r) => m[r * 3] * v[0] + m[r * 3 + 1] * v[1] + m[r * 3 + 2] * v[2])
const put = (a, b) => ({ m: m3(a.m, b.m), t: m3v(a.m, b.t).map((x, i) => x + a.t[i]) })
const MOVE = { still: { m: [1, 0, 0, 0, 1, 0, 0, 0, 1], t: [0, 0, 0] } }
const shift = (t) => ({ m: MOVE.still.m, t })
const aroundY = (a) => ({ m: [Math.cos(a), 0, Math.sin(a), 0, 1, 0, -Math.sin(a), 0, Math.cos(a)], t: [0, 0, 0] })
const aroundZ = (a) => ({ m: [Math.cos(a), -Math.sin(a), 0, Math.sin(a), Math.cos(a), 0, 0, 0, 1], t: [0, 0, 0] })
const stretched = ([x, y, z]) => ({ m: [x, 0, 0, 0, y, 0, 0, 0, z], t: [0, 0, 0] })
// Rodrigues: a turn of `a` around the unit `axis`.
function aroundAxis([x, y, z], a) {
  const c = Math.cos(a), s = Math.sin(a), k = 1 - c
  return { m: [c + x * x * k, x * y * k - z * s, x * z * k + y * s, y * x * k + z * s, c + y * y * k, y * z * k - x * s, z * x * k - y * s, z * y * k + x * s, c + z * z * k], t: [0, 0, 0] }
}
const unit = (v) => { const l = Math.hypot(...v) || 1; return v.map((x) => x / l) }

/**
 * Poulpy in a pose of a move (createTurn, createJump), for a painter with no 3D engine: what the scene's rig does with its bones,
 * written with plain matrices. His arms are bent into new points; his head and face are placed by the painter.
 * @param model what buildPoulpy3d returned
 * @param pose a pose of createTurn, createJump or createCheer. A cheer's medal swings on its hinge and slides around him, and its
 *   confetti flies: each piece is a small flat card in the room (`confettiScale`, 1 unless the pose says, shrinks them all, to fade them out).
 * @param {{ confetti?: string[] }} [options] `confetti` is the `#RRGGBB` of each colour of CONFETTI.colours, with Poulpy's ember in place of 'ember'.
 * @returns {{ model: object, placed: (part: object) => ({ point: Function, way: Function } | undefined) }} the model with
 *   his arms posed, and where the painter should put each of the other parts: pass both to paintPoulpy.
 */
export function posePoulpy(model, pose, { confetti = [] } = {}) {
  const body = put(shift([0, pose.lift ?? 0, 0]), put(aroundY(pose.yaw ?? 0), aroundZ(pose.lean ?? 0)))
  const head = put(body, put(shift([0, (model.headLift ?? 0) + (pose.head ?? 0), 0]), put(aroundZ(model.headTilt ?? 0), stretched(squashScale(pose.squash ?? 0)))))
  const joints = armJoints()
  const parts = model.parts.map((part) => {
    const arm = /^arm_(\d+)/.exec(part.name)
    if (!arm) return part
    const k = +arm[1] - 1, own = joints.slice(k * 3, k * 3 + 3), skin = armSkin(k, part.positions)
    // Each bend's placement in the room, as the rig's bones chain them, and what each does to a point of his arm at rest.
    let chain = body
    const moves = own.map((joint, i) => {
      const before = i ? own[i - 1].at : [0, 0, 0], local = joint.at.map((x, n) => x - before[n])
      if (i === 0) { const out = unit([joint.at[0], 0, joint.at[2]]); local.forEach((_, n) => { local[n] += out[n] * (pose.spread ?? 0) }) }
      const hang = pose.droop ? aroundAxis(unit([joint.at[2], 0, -joint.at[0]]), pose.droop[k][i]) : MOVE.still
      chain = put(chain, put(shift(local), put(aroundY(pose.arms[k][i]), hang)))
      return put(chain, shift(joint.at.map((x) => -x)))
    })
    const positions = new Array(part.positions.length), normals = new Array(part.normals.length)
    for (let p = 0; p < positions.length / 3; p++) {
      const v = [0, 1, 2].map((n) => part.positions[p * 3 + n]), d = [0, 1, 2].map((n) => part.normals[p * 3 + n])
      const at = [0, 0, 0], way = [0, 0, 0]
      for (let s = 0; s < 4; s++) {
        const w = skin.weights[p * 4 + s]
        if (!w) continue
        const move = s === 0 ? body : moves[s - 1], moved = m3v(move.m, v).map((x, n) => x + move.t[n]), turned = m3v(move.m, d)
        for (let n = 0; n < 3; n++) { at[n] += moved[n] * w; way[n] += turned[n] * w }
      }
      for (let n = 0; n < 3; n++) { positions[p * 3 + n] = at[n]; normals[p * 3 + n] = way[n] }
    }
    return { ...part, positions, normals }
  })
  const by = (at) => ({ point: (v) => m3v(at.m, v).map((x, n) => x + at.t[n]), way: (n) => unit(m3v(at.m, n)) })
  // His medal rides on his body, slides around it and swings up off his arm around its hinge (as the scene's rig does).
  const [hingeY, hingeZ] = medalHinge
  const medal = put(body, put(aroundY(pose.medal?.sway ?? 0), put(shift([0, hingeY, hingeZ + (pose.spread ?? 0)]), put(aroundAxis([1, 0, 0], -(pose.medal?.lift ?? 0)), shift([0, -hingeY, -hingeZ])))))
  // Confetti: a unit-sized card for each piece, turned the way the scene turns it (Y, then X, then Z) and put where it is in the room.
  const flying = pose.confetti ?? [], [wide, tall] = CONFETTI.size.map((size) => size / 64)
  const cards = flying.map(([x, y, z, turnX, turnY, turnZ], i) => ({
    at: put(shift([x, y, z]), put(aroundY(turnY), put(aroundAxis([1, 0, 0], turnX), put(aroundZ(turnZ), stretched(Array(3).fill(y < 0 ? 0 : pose.confettiScale ?? 1)))))),
    colour: `confetti_${i % confetti.length}`,
  }))
  const card = { positions: [-wide / 2, -tall / 2, 0, wide / 2, -tall / 2, 0, wide / 2, tall / 2, 0, -wide / 2, tall / 2, 0], normals: [0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1], indices: [0, 1, 2, 0, 2, 3] }
  const materials = { ...model.materials }
  confetti.forEach((hex, k) => { materials[`confetti_${k}`] = { colour: linear(hex), flat: true } })
  const confettiParts = confetti.length ? cards.map((c, i) => ({ name: `confetti_piece_${i}`, material: c.colour, ...card })) : []
  const placed = (part) => {
    if (/^arm_/.test(part.name)) return undefined
    const piece = /^confetti_piece_(\d+)/.exec(part.name)
    if (piece) return by(cards[+piece[1]].at)
    return by(part.name.startsWith('medal') ? medal : head)
  }
  return { model: { ...model, materials, parts: [...parts, ...confettiParts] }, placed }
}
