// Poulpy's autumn look, with no dependency: happy Poulpy in a white-and-red striped scarf, with maple leaves
// falling around him. Built on the shape and faces of model.mjs, moving on the springs of motion.mjs.
// The workshop's "Poulpy looks > Autumn" page draws it live (web/storybook/stories/brand/looks/).
//
// A look is a whole Poulpy for a moment of the year: a mood, what he wears and what goes on around him.
//   AUTUMN          its numbers: his cheeks, the scarf, the leaves and his idle
//   buildAutumn()   Poulpy wearing the scarf, as buildPoulpy3d returns him
//   leafShape()     one maple leaf, flat, facing +z
//   createAutumn()  his cosy idle and the falling leaves, as something to advance in time
// He never lifts an arm (brand decision 8): the scarf sits on his head where his arms meet it, and its ends lie on them.
// To change it, edit AUTUMN below and look at the page.
import { SHAPE, FACES, buildPoulpy3d, egg, join, linear, tube } from '../model.mjs'
import { createFollow } from '../motion.mjs'

export const AUTUMN = {
  // Blue side, happy: a look is never a mood of its own.
  mood: 'happy',
  // Cheeks rosy from the cold: the happy face's blush, a little stronger and wider.
  cheeks: { colour: '#F49A6B', strength: 0.95, size: [9.5, 5.5] },
  scarf: {
    // Off-white wool and a poppy red: brighter and warmer than the error crimson, redder than the ember side.
    wool: '#FBF6EE', stripe: '#E5373A',
    // The roll around his head: the height of its middle, and half its thickness outwards and upwards. It sits just above where
    // his arms meet his head, low enough to leave his smile clear.
    // It sinks a little into the soft tops of his arms.
    height: 0.68, half: [0.075, 0.1],
    // How many stripes go around, red and white in turn (an even number), how far into his head the roll sinks, and how much each
    // stripe swells in its middle, the way chunky knitting does.
    stripes: 16, sink: 0.2, swell: 0.12,
    // The knot, on his left above his second arm: `around` from his front towards his left, in radians, and its size.
    knot: { around: Math.PI / 4, size: [0.15, 0.13, 0.12] },
    // The two ends, lying over that arm and hanging off its tip: how far to each side of the arm, in radians, how far out they
    // reach from his middle, how wide and thick they are, and how many stripes each has.
    ends: [{ aside: -0.1, reach: 1.8 }, { aside: 0.2, reach: 1.5 }], wide: 0.24, thick: 0.045, endStripes: 5,
    // The fringe at each end: how many threads, and how long.
    fringe: { threads: 4, length: 0.08 },
  },
  // Maple leaves falling around him. `count` are in the air or on the floor at once. Each starts `from` high, somewhere `around`
  // him (from his middle; `depth` squeezes the circle front to back so they stay in view), and falls at `fall` units a second on
  // average, swinging from side to side by `swing` units, `beat` swings a second, tilting by up to `roll` radians at each end of a
  // swing and half turning over (`flip`, radians at most, `turnover` times a second). On the floor it settles flat in `settle`
  // seconds, stays `rest` seconds and shrinks away in `fade`, then falls again from the top. Sizes are tip to stem.
  leaves: {
    count: 12, seed: 20261117, colours: ['#F28A24', '#F6A833', '#E8742A'], size: [0.3, 0.4],
    from: 4.4, around: [1.95, 2.75], depth: 0.75, fall: [0.42, 0.6], swing: [0.16, 0.3], beat: [0.32, 0.46],
    roll: 0.6, flip: 1.1, turnover: [0.12, 0.3], settle: 0.35, rest: [1.5, 4], fade: 0.6,
  },
  // His idle: he sways gently from side to side, once every `seconds`, turning by `sway` degrees, his head leaning by `lean` degrees
  // a moment later, and breathes: his head stretches by `breath.depth` of its height and back, once every `breath.seconds`.
  // His head follows on a spring, `head`, and his arms trail loose behind the sway on the springs of his turn.
  idle: { seconds: 6.4, sway: 5, lean: 3, breath: { seconds: 3.2, depth: 0.018 }, head: { speed: 5, damping: 0.8 } },
}

const TAU = Math.PI * 2, RAD = Math.PI / 180
const SUBSTEP = 1 / 240 // seconds, as in motion.mjs: every run is the same
const norm = (v) => { const l = Math.hypot(...v) || 1; return v.map((x) => x / l) }
const cross = (a, b) => [a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0]]

// A soft roll swept along `points`: its cross-section is an oval, `half[0]` along `side(i)` and `half[1]` along the third way;
// `half` may also be a function of the point's number, for a roll that swells and narrows.
// `closed` joins the last point to the first; otherwise each end gets a flat cap.
function sweep(points, side, half, { closed = false, sides = 14 } = {}) {
  const positions = [], normals = [], indices = [], n = points.length, size = typeof half === 'function' ? half : () => half
  points.forEach((p, i) => {
    const [ha, hb] = size(i)
    const a = points[closed ? (i - 1 + n) % n : Math.max(i - 1, 0)], b = points[closed ? (i + 1) % n : Math.min(i + 1, n - 1)]
    const along = norm([0, 1, 2].map((k) => b[k] - a[k])), s = norm(side(i, along)), o = norm(cross(s, along))
    for (let j = 0; j < sides; j++) {
      const t = (j / sides) * TAU, c = Math.cos(t), sn = Math.sin(t)
      positions.push(...[0, 1, 2].map((k) => p[k] + s[k] * ha * c + o[k] * hb * sn))
      normals.push(...norm([0, 1, 2].map((k) => (s[k] * c) / ha + (o[k] * sn) / hb)))
    }
  })
  const rings = closed ? n : n - 1
  for (let i = 0; i < rings; i++) for (let j = 0; j < sides; j++) {
    const a = i * sides + j, b = i * sides + ((j + 1) % sides), c = ((i + 1) % n) * sides + j, d = ((i + 1) % n) * sides + ((j + 1) % sides)
    indices.push(a, c, b, b, c, d)
  }
  if (!closed) for (const [ring, way] of [[0, -1], [n - 1, 1]]) {
    // A cap: the end's ring, fanned from its middle, facing on along the roll.
    const p = points[ring], q = points[ring - way], out = norm([0, 1, 2].map((k) => p[k] - q[k])), mid = positions.length / 3
    positions.push(...p); normals.push(...out)
    for (let j = 0; j < sides; j++) { positions.push(...positions.slice((ring * sides + j) * 3, (ring * sides + j) * 3 + 3)); normals.push(...out) }
    for (let j = 0; j < sides; j++) indices.push(...(way > 0 ? [mid, mid + 1 + ((j + 1) % sides), mid + 1 + j] : [mid, mid + 1 + j, mid + 1 + ((j + 1) % sides)]))
  }
  return { positions, normals, indices }
}

// The head's outline at a height: half its width (x) and depth (z).
function headAt(y) {
  const { radius, centreY } = SHAPE.head, k = Math.sqrt(Math.max(1 - ((y - centreY) / radius[1]) ** 2, 0))
  return [radius[0] * k, radius[2] * k]
}
// The top of an arm's surface above a point of the floor, or 0 off the arm.
function armTop(x, z) {
  const { count, radius, ring } = SHAPE.arms
  let top = 0
  for (let k = 0; k < count; k++) {
    const a = (k * TAU) / count, along = x * Math.sin(a) + z * Math.cos(a) - ring, across = x * Math.cos(a) - z * Math.sin(a)
    const inside = 1 - (across / radius[0]) ** 2 - (along / radius[2]) ** 2
    if (inside > 0) top = Math.max(top, radius[1] + radius[1] * Math.sqrt(inside))
  }
  return top
}

/**
 * The scarf's parts and their colours, built where his head sits at rest, from numbers shaped like AUTUMN.scarf. Other looks
 * wear the same scarf in their own colours (winter.mjs): they pass AUTUMN.scarf with other `wool` and `stripe`.
 */
export function scarf(s) {
  const [w, d] = headAt(s.height), out = (around) => norm([Math.sin(around) / w, 0, Math.cos(around) / d])
  // A point of the roll's middle line: on his head, then out by its thickness, less what sinks in.
  const middle = (around) => { const n = out(around); return [w * Math.sin(around) + n[0] * s.half[0] * (1 - s.sink), s.height, d * Math.cos(around) + n[2] * s.half[0] * (1 - s.sink)] }
  // The roll goes around in stripes; each stripe is its own short roll, so the colours meet sharply.
  const wool = [], stripes = [], steps = 6
  for (let i = 0; i < s.stripes; i++) {
    const from = (i / s.stripes) * TAU, points = Array.from({ length: steps + 1 }, (_, k) => middle(from + ((k / steps) * TAU) / s.stripes))
    const swell = (k) => s.half.map((r) => r * (1 + s.swell * Math.sin((Math.PI * k) / steps)))
    ;(i % 2 ? wool : stripes).push(sweep(points, (k) => out(from + ((k / steps) * TAU) / s.stripes), swell, { sides: 16 }))
  }
  // The knot sits on the roll, out from it, and the two ends leave it over his arm.
  const knotAt = middle(s.knot.around), n = out(s.knot.around)
  const knot = egg(s.knot.size, { around: 20, down: 12, at: [knotAt[0] + n[0] * s.half[0] * 0.75, knotAt[1] - 0.01, knotAt[2] + n[2] * s.half[0] * 0.75] })
  for (const end of s.ends) {
    const around = s.knot.around + end.aside, way = [Math.sin(around), 0, Math.cos(around)], sideways = [Math.cos(around), 0, -Math.sin(around)]
    // From the knot outwards, lying on the arm (or the floor) and hanging off its tip.
    const start = Math.hypot(knotAt[0], knotAt[2]) + 0.04, count = 18
    const line = Array.from({ length: count + 1 }, (_, k) => {
      const r = start + ((end.reach - start) * k) / count, x = way[0] * r, z = way[2] * r
      return [x, Math.max(armTop(x, z), 0) + s.thick / 2 + 0.012, z]
    })
    // The end starts in the knot, so it is lifted to it along the first stretch.
    line[0][1] = Math.max(line[0][1], knotAt[1] - 0.02)
    line[1][1] = Math.max(line[1][1], (line[0][1] + line[2][1]) / 2)
    // Stripes across the end, each its own piece; the side of the end is level, across the way it goes.
    const per = count / s.endStripes
    for (let i = 0; i < s.endStripes; i++) {
      const piece = line.slice(Math.round(i * per), Math.round((i + 1) * per) + 1)
      ;(i % 2 ? wool : stripes).push(sweep(piece, () => sideways, [s.wide / 2, s.thick / 2], { sides: 12 }))
    }
    // The fringe carries on from the end's last stretch, in its colour.
    const tip = line[count], before = line[count - 1], on = norm([0, 1, 2].map((k) => tip[k] - before[k]))
    const threads = Array.from({ length: s.fringe.threads }, (_, t) => {
      const across = (t / (s.fringe.threads - 1) - 0.5) * s.wide * 0.8, from = tip.map((v, k) => v + sideways[k] * across)
      return tube([from, from.map((v, k) => v + on[k] * s.fringe.length)], 0.017, 6)
    })
    ;((s.endStripes - 1) % 2 ? wool : stripes).push(...threads)
  }
  return {
    materials: { scarf_wool: { colour: linear(s.wool) }, scarf_stripe: { colour: linear(s.stripe) } },
    parts: [
      { name: 'scarf', material: 'scarf_wool', ...join(...wool) },
      { name: 'scarf_stripes', material: 'scarf_stripe', ...join(...stripes, knot) },
    ],
  }
}

/**
 * Poulpy in his autumn look: happy, rosy-cheeked and wearing the scarf.
 * @param {{ tones: Record<string, string>, colours: { blue: string, ember: string, ink: string }, autumn?: typeof AUTUMN }} options
 *   `tones` are the `mood` tokens of design/tokens.json and `colours` its `color.poulpy` tokens.
 * @returns What buildPoulpy3d returns, with the scarf's parts (`scarf`, `scarf_stripes`), which ride on his head, and `headTilt` 0.
 */
export function buildAutumn({ tones, colours, autumn = AUTUMN }) {
  const { colour, strength, size } = autumn.cheeks
  const face = FACES.happy.map((feature) => (feature[0] === 'cheek' ? ['cheek', feature[1], feature[2], ...size, colour, strength] : feature))
  const model = buildPoulpy3d({ mood: autumn.mood, tone: tones[autumn.mood], features: face, colours })
  const made = scarf(autumn.scarf)
  Object.assign(model.materials, made.materials)
  model.parts.push(...made.parts)
  return { ...model, headTilt: 0 }
}

/** One maple leaf, flat, facing +z: five pointed lobes and a stem, 1 from the stem's end to the top tip, its middle at 0. */
export function leafShape() {
  // Its right half, from the top tip down to the stem, in a box 1 tall; the left half is the same, mirrored.
  const right = [[0, 0.62], [0.09, 0.4], [0.2, 0.46], [0.17, 0.27], [0.47, 0.36], [0.4, 0.2], [0.47, 0.12], [0.26, 0.02], [0.3, -0.12], [0.11, -0.08], [0.03, -0.17], [0.025, -0.38]]
  const outline = [...right, ...right.slice().reverse().map(([x, y]) => [-x, y])]
  const mid = [0, 0.1], all = [mid, ...outline]
  return {
    positions: all.flatMap(([x, y]) => [x, y, 0]),
    normals: all.flatMap(() => [0, 0, 1]),
    indices: outline.flatMap((_, i) => [0, 1 + ((i + 1) % outline.length), 1 + i]),
  }
}

/**
 * Poulpy's autumn idle and the leaves falling around him, as something to advance in time. Every run is the same: the leaves'
 * chances are drawn from a fixed list, and the leaves have already been falling for a while when it starts.
 * @param {{ autumn?: typeof AUTUMN }} [options]
 * @returns {{ advance(seconds: number): object, pose(): object }} A pose is a turn's pose (`yaw`, `squash`, `arms`) and `tilt`, how far his
 *   head leans to his right in radians, and `leaves`: for each leaf `[x, y, z, roll, flip, lie, size, colour]`, where it is in the room,
 *   how far it has turned in its own plane and over (radians), how far it has settled flat on the floor (0 standing towards the viewer,
 *   1 flat), its size from stem to tip and which of AUTUMN.leaves.colours it is.
 */
export function createAutumn({ autumn = AUTUMN } = {}) {
  const { idle, leaves: L } = autumn
  // A fixed list of chances (mulberry32), as for the champion's confetti.
  let seed = L.seed
  const chance = () => {
    seed = (seed + 0x6d2b79f5) | 0
    let t = Math.imul(seed ^ (seed >>> 15), 1 | seed)
    t = (t + Math.imul(t ^ (t >>> 7), 61 | t)) ^ t
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296
  }
  const between = ([low, high]) => low + (high - low) * chance()
  const spring = (s, goal, value, rate) => s.speed * s.speed * (goal - value) - 2 * s.damping * s.speed * rate
  const follow = createFollow()
  let time = 0, yaw = 0, tilt = 0, tiltSpeed = 0, squash = 0, squashSpeed = 0, arms = follow.advance(0, 0).arms

  // A leaf starts at the top, or at `high` to fill the sky at the start.
  const leaves = Array.from({ length: L.count }, (_, i) => ({ colour: i % L.colours.length }))
  const fall = (leaf, high = L.from) => {
    const around = chance() * TAU, far = between(L.around), swingWay = (chance() * 2 - 1) * 0.6
    Object.assign(leaf, {
      centre: [Math.sin(around) * far, Math.cos(around) * far * L.depth], way: [Math.cos(swingWay), Math.sin(swingWay)],
      y: high, speed: between(L.fall), swing: between(L.swing), beat: between(L.beat) * TAU, phase: chance() * TAU,
      turnover: between(L.turnover) * TAU, over: chance() * TAU, size: between(L.size), clock: 0, landed: -1, rest: between(L.rest),
      at: [0, 0, 0], roll: 0, flip: 0, lie: 0, shown: high < L.from ? 1 : 0,
    })
  }
  leaves.forEach((leaf, i) => fall(leaf, (L.from * (i + chance())) / L.count))

  function tick(h) {
    time += h
    // His sway and breath: targets that come and go, his head following them on a spring and his arms trailing the sway.
    const w = TAU / idle.seconds
    yaw = idle.sway * RAD * Math.sin(w * time)
    arms = follow.advance(h, yaw).arms
    tiltSpeed += spring(idle.head, idle.lean * RAD * Math.sin(w * time - 0.9), tilt, tiltSpeed) * h
    tilt += tiltSpeed * h
    squashSpeed += spring(idle.head, idle.breath.depth * Math.sin((TAU * time) / idle.breath.seconds), squash, squashSpeed) * h
    squash += squashSpeed * h

    for (const leaf of leaves) {
      leaf.clock += h
      if (leaf.landed < 0) {
        // In the air it swings like a pendulum: fastest and falling quickest through the middle, tilted up at each end.
        const s = leaf.phase + leaf.beat * leaf.clock, swing = leaf.swing * Math.sin(s)
        leaf.y -= h * leaf.speed * (0.6 + 0.8 * Math.cos(s) ** 2)
        leaf.at = [leaf.centre[0] + leaf.way[0] * swing, leaf.y, leaf.centre[1] + leaf.way[1] * swing]
        leaf.roll = L.roll * Math.sin(s) * leaf.way[0]
        leaf.flip = L.flip * Math.sin(leaf.over + leaf.turnover * leaf.clock)
        leaf.shown = Math.min(leaf.shown + h / 0.5, 1)
        if (leaf.y <= leaf.size * 0.5) { leaf.landed = 0; leaf.flip = 0 }
      } else {
        // On the floor it settles flat, stays a while and shrinks away, then falls again.
        leaf.landed += h
        const settled = Math.min(leaf.landed / L.settle, 1), ease = settled * settled * (3 - 2 * settled)
        leaf.lie = ease
        leaf.at[1] = 0.012 + (leaf.size * 0.5 - 0.012) * (1 - ease)
        const gone = (leaf.landed - L.settle - leaf.rest) / L.fade
        leaf.shown = 1 - Math.min(Math.max(gone, 0), 1)
        if (gone >= 1) fall(leaf)
      }
    }
  }

  // The leaves have been falling for a while before he is shown.
  for (let i = 0; i < 20 / SUBSTEP; i++) tick(SUBSTEP)
  let owed = 0
  const sim = {
    pose: () => ({
      yaw, squash, arms, tilt,
      leaves: leaves.map((leaf) => [...leaf.at, leaf.roll, leaf.flip, leaf.lie, leaf.size * leaf.shown, leaf.colour]),
    }),
    advance(seconds) {
      owed += Math.min(Math.max(seconds, 0), 1)
      while (owed >= SUBSTEP) { tick(SUBSTEP); owed -= SUBSTEP }
      return sim.pose()
    },
  }
  return sim
}
