// Winter Poulpy, one of his looks: happy, in a knitted beanie with a pompom and a striped scarf, while snow falls around
// him and his breath shows as little puffs of mist in the cold air. Plain JavaScript with no dependency, built on the
// shape and the faces of model.mjs and moving on the springs of motion.mjs, so anything can draw him from it; the
// workshop's Brand > Poulpy looks > Winter page draws it live (web/storybook/stories/brand/looks/).
//
// A look is kept in a file of its own, like this one, so looks never step on each other.
//   - What he wears is solid and part of him, as the poses' props are (poses.mjs): it turns, tilts and squashes with his head.
//   - What is around him (the snow, his breath) is flat and faces the viewer, as the comic effects do, but it is in the
//     room: a flake falls where it is, and his breath leaves his mouth and drifts off on its own.
//   - His idle is gentle: he sways from side to side on a spring, his arms trailing, and breathes; each breath out is a puff.
// He never lifts an arm (brand decision 8), and winter is the blue side: he is happy, never cold and miserable.
// To change the look, edit WINTER below and look at the page.
import { SHAPE, FACES, buildPoulpy3d, egg, join, linear, tube } from '../model.mjs'
import { createFollow } from '../motion.mjs'

const CREAM = '#FFF1E8', SNOW = '#FFFFFF', FROST = '#9CCFEE', MIST = '#F2F8FC', MIST_EDGE = '#A9CDE6'

export const WINTER = {
  // His face: the happy one, with cheeks rosier than usual from the cold.
  face: [...FACES.happy.filter(([kind]) => kind !== 'cheek'), ['cheek', 63, 112, 10, 5.5, '#F27C6B', 0.85], ['cheek', 137, 112, 10, 5.5, '#F27C6B', 0.85]],
  // The knit is Poulpy's ember, striped with cream: the beanie and the scarf are one set.
  knit: { stripe: CREAM },
  // The beanie: its crown covers his head down to `crown` (radians from the top) and stands off it, by `puff[0]` at the top
  // and `puff[1]` at its edge, so it is a dome; the folded cuff goes from there down to `cuff`, `fold` off his head and
  // hugging it: to `cuff[0]` in front, clear of his eyes, and pulled further down, to `cuff[1]`, at the back.
  // `ribs` is how many ribs the knit has around, `rib` how deep they are; `stripe` is where its cream stripe runs.
  beanie: { crown: 0.86, puff: [0.16, 0.05], cuff: [1.1, 1.36], fold: 0.07, ribs: 44, rib: 0.012, stripe: [0.5, 0.6], pompom: 0.25 },
  // The scarf: wrapped once around the base of his head, `y` high at the back and `dip` lower in front, so it clears his
  // smile. `size` is half its height and half its thickness. It is knotted `knot` radians round to his left, and its two
  // ends lie on his arms, each with two cream stripes at `ends` of its length.
  scarf: { y: 0.66, dip: 0.08, size: [0.16, 0.1], knot: 0.42, ends: [[0.62, 0.72], [0.8, 0.9]] },
  // His idle. He looks a little to one side, then to the other, every `seconds`, by `degrees`, on a spring as soft as
  // `body`. His head leans with him by `tilt` degrees.
  sway: { seconds: 2.6, degrees: 8, tilt: 4, body: { speed: 5.5, damping: 0.62 }, head: { speed: 6, damping: 0.55 } },
  // He breathes in and out every `seconds`: in, his head stretches by `stretch`; out, it squashes by `squash`.
  breath: { seconds: 3.4, inhale: 0.55, stretch: 0.018, squash: 0.03, speed: 9, damping: 0.7 },
  // Each breath out is `count` little puffs of mist, `apart` seconds apart, from just in front of his `mouth`. Each leaves
  // forwards at `speed`, `aside` of it towards the side he looks to so it clears his face, and floats up at `rise`, slowed
  // by `drag`; it grows from `grow[0]` to `grow[1]` and fades out over `life` seconds.
  puffs: { count: 3, apart: 0.16, life: 2.2, speed: 1.7, aside: 1.2, rise: 0.3, drag: 1.5, grow: [0.13, 0.46], opacity: 0.9, mouth: [0, 0.74, 1.2] },
  // The snow: `flakes` in all, `crystals` of them six-armed, the rest round. They fall from `top` through a box `wide` to
  // each side and from `back` to `front`, never closer to his middle than `clear`. Each falls at `fall` units a second,
  // swaying sideways by `sway` units, `size` big (a crystal `crystal` big), and melts on the floor in `melt` seconds.
  snow: { flakes: 90, crystals: 16, top: 4.6, wide: 3.6, back: -3, front: 2.8, clear: 1.75, fall: [0.32, 0.55], sway: [0.08, 0.26], size: [0.032, 0.06], crystal: [0.085, 0.12], spin: 1.1, melt: 0.7 },
}

// --- small maths -----------------------------------------------------------------------------------

const TAU = Math.PI * 2
const norm = ([x, y, z]) => { const l = Math.hypot(x, y, z) || 1; return [x / l, y / l, z / l] }
const cross = (a, b) => [a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0]]
const { head, arms } = SHAPE
const C = [0, head.centreY, 0]

/**
 * A soft band swept along `centres`: at each point it is an oval, `half[0]` across `out` and `half[1]` along `up`.
 * `colour(i)` names the part each stretch between point i and i + 1 belongs to; returns one shape per name.
 */
function sweep(centres, outs, ups, half, colour = () => 'main', sides = 16) {
  const positions = [], normals = []
  centres.forEach((c, i) => {
    const o = outs[i], u = ups[i], h = Array.isArray(half[0]) ? half[i] : half
    for (let j = 0; j < sides; j++) {
      const b = (j / sides) * TAU, cb = Math.cos(b), sb = Math.sin(b)
      positions.push(...[0, 1, 2].map((k) => c[k] + o[k] * h[0] * cb + u[k] * h[1] * sb))
      normals.push(...norm([0, 1, 2].map((k) => (o[k] * cb) / h[0] + (u[k] * sb) / h[1])))
    }
  })
  const shapes = {}
  for (let i = 0; i < centres.length - 1; i++) {
    const name = colour(i), s = (shapes[name] ??= { indices: [] })
    for (let j = 0; j < sides; j++) {
      const a = i * sides + j, b = i * sides + ((j + 1) % sides), c = a + sides, d = b + sides
      s.indices.push(a, b, c, b, d, c)
    }
  }
  // Every shape keeps all the points and only its own triangles; fine for a few hundred points.
  return Object.fromEntries(Object.entries(shapes).map(([name, { indices }]) => [name, { positions, normals, indices }]))
}

// How wide his head is, as a share of its full width, at height y.
const headWidth = (y) => Math.sqrt(Math.max(1 - ((y - C[1]) / head.radius[1]) ** 2, 0))

// How high the tops of his arms are at (x, z): the floor, or the highest arm there.
function armTop(x, z) {
  let top = 0
  for (let k = 0; k < arms.count; k++) {
    const a = (k * TAU) / arms.count, c = Math.cos(a), s = Math.sin(a), dx = x - s * arms.ring, dz = z - c * arms.ring
    // Into the arm's own frame: across it, and along it.
    const across = dx * c - dz * s, along = dx * s + dz * c
    const inside = 1 - (across / arms.radius[0]) ** 2 - (along / arms.radius[2]) ** 2
    if (inside > 0) top = Math.max(top, arms.radius[1] * (1 + Math.sqrt(inside)))
  }
  return top
}

// --- what he wears ---------------------------------------------------------------------------------

// A slice of a ribbed knit around his head, from `from` to `to` radians below the top (numbers, or functions of the angle
// around), `out` off it (a number, or one for each angle from the top): the ribs run
// from top to bottom, `ribs` of them, standing `rib` out. Like egg() in model.mjs, with the ribs.
function knit(from, to, out, ribs, rib, around = 96, down = 10) {
  const per = (v) => (typeof v === 'function' ? v : () => v), off = per(out), start = per(from), end = per(to)
  const positions = [], normals = [], indices = []
  for (let i = 0; i <= down; i++) {
    for (let j = 0; j <= around; j++) {
      const ph = (j / around) * TAU, th = start(ph) + ((end(ph) - start(ph)) * i) / down, k = 1 + off(th) + rib * Math.cos(ribs * ph)
      const u = [Math.sin(th) * Math.sin(ph), Math.cos(th), Math.sin(th) * Math.cos(ph)]
      positions.push(C[0] + u[0] * head.radius[0] * k, C[1] + u[1] * head.radius[1] * k, C[2] + u[2] * head.radius[2] * k)
      // The rib tips the surface sideways: its slope around is added to the plain normal.
      const n = norm([u[0] / head.radius[0], u[1] / head.radius[1], u[2] / head.radius[2]]), tip = -rib * ribs * Math.sin(ribs * ph) * 0.9
      normals.push(...norm([n[0] + tip * Math.cos(ph), n[1], n[2] - tip * Math.sin(ph)]))
    }
  }
  for (let i = 0; i < down; i++) for (let j = 0; j < around; j++) {
    const a = i * (around + 1) + j, b = a + 1, c = a + around + 1, d = c + 1
    indices.push(a, c, b, b, c, d)
  }
  return { positions, normals, indices }
}

// A ring around his head, `at` radians from the top (a number, or one for each angle around) and `out` off it, as points.
const ringAt = (at, out, n = 64) => Array.from({ length: n + 1 }, (_, j) => {
  const ph = (j / n) * TAU, down = typeof at === 'function' ? at(ph) : at
  return [C[0] + Math.sin(down) * Math.sin(ph) * head.radius[0] * (1 + out), C[1] + Math.cos(down) * head.radius[1] * (1 + out), C[2] + Math.sin(down) * Math.cos(ph) * head.radius[2] * (1 + out)]
})

function beanie() {
  const b = WINTER.beanie
  // A dome: further off his head at the top than at its edge.
  const puff = (th) => b.puff[1] + (b.puff[0] - b.puff[1]) * Math.cos(Math.min(th / b.crown, 1) * Math.PI / 2) ** 2
  const top = C[1] + head.radius[1] * (1 + b.puff[0])
  // The pompom: a ball with smaller balls all over it, so its outline is fluffy. The points are spread evenly (a Fibonacci sphere).
  const at = [0.02, top + b.pompom * 0.7, -0.02], tufts = 26
  const pompom = join(egg([b.pompom * 0.86, b.pompom * 0.82, b.pompom * 0.86], { at }), ...Array.from({ length: tufts }, (_, i) => {
    const y = 1 - (2 * (i + 0.5)) / tufts, r = Math.sqrt(1 - y * y), a = i * 2.39996
    return egg([b.pompom * 0.34, b.pompom * 0.34, b.pompom * 0.34], { around: 12, down: 8, at: [at[0] + Math.cos(a) * r * b.pompom * 0.72, at[1] + y * b.pompom * 0.72, at[2] + Math.sin(a) * r * b.pompom * 0.72] })
  }))
  // The cuff is folded up: a band a little proud of the crown, with a rolled edge top and bottom.
  const edge = (ph) => b.cuff[0] + (b.cuff[1] - b.cuff[0]) * (0.5 - 0.5 * Math.cos(ph)), crown = (ph) => b.crown + (edge(ph) - b.cuff[0])
  const cuffTop = ringAt(crown, b.fold), cuffBottom = ringAt(edge, b.fold)
  return {
    parts: [
      { name: 'beanie', material: 'knit', ...join(knit(0, b.stripe[0], puff, b.ribs, b.rib * 0.5, 96, 10), knit(b.stripe[1], (ph) => crown(ph) + 0.04, puff, b.ribs, b.rib * 0.5, 96, 6)) },
      { name: 'beanie_stripe', material: 'knit_stripe', ...knit(b.stripe[0], b.stripe[1], (th) => puff(th) + 0.004, b.ribs, b.rib * 0.5, 96, 3) },
      { name: 'beanie_cuff', material: 'knit', ...join(knit(crown, edge, b.fold, b.ribs, b.rib, 96, 8), tube(cuffTop, 0.03, 10), tube(cuffBottom, 0.04, 10)) },
      { name: 'beanie_pompom', material: 'knit_stripe', ...pompom },
    ],
  }
}

function scarf() {
  const s = WINTER.scarf, n = 96
  // Around his head: high at the back, lower in front, lying against him.
  const centres = [], outs = [], ups = [], halves = []
  for (let j = 0; j <= n; j++) {
    const ph = (j / n) * TAU, y = s.y - s.dip * (0.5 + 0.5 * Math.cos(ph)), w = headWidth(y)
    const o = norm([Math.sin(ph) / head.radius[0], 0, Math.cos(ph) / head.radius[2]])
    centres.push([Math.sin(ph) * head.radius[0] * w + o[0] * s.size[1] * 0.75, y, Math.cos(ph) * head.radius[2] * w + o[2] * s.size[1] * 0.75])
    outs.push(o); ups.push([0, 1, 0])
    // Soft folds: it is a little thicker and taller in places, as wrapped wool is.
    halves.push([s.size[1] * (1 + 0.16 * Math.sin(7 * ph + 1)), s.size[0] * (1 + 0.08 * Math.sin(7 * ph))])
  }
  const wrap = sweep(centres, outs, ups, halves)

  // The knot, at his front on his left (the viewer's right), and two tails that lie over his front arm and down to the floor.
  const ka = s.knot, ky = s.y - s.dip * (0.5 + 0.5 * Math.cos(ka)), kw = headWidth(ky)
  const knotAt = [Math.sin(ka) * (head.radius[0] * kw + 0.12), ky - 0.02, Math.cos(ka) * (head.radius[2] * kw + 0.12)]
  const knot = egg([0.15, 0.13, 0.11], { around: 20, down: 12, at: knotAt, turn: ([x, y, z]) => [x * Math.cos(ka) + z * Math.sin(ka), y, -x * Math.sin(ka) + z * Math.cos(ka)] })
  const tail = (to, steps, width) => {
    // It leaves the knot and lies on whatever is under it, a little above. Where what is under it drops away it hangs down
    // as cloth does, no steeper than `drape`: each point is as high as the highest thing near it, less the slope to there.
    const along = Array.from({ length: steps * 4 + 1 }, (_, i) => { const t = i / (steps * 4); return [knotAt[0] + (to[0] - knotAt[0]) * t, knotAt[2] + (to[1] - knotAt[2]) * t] })
    const length = Math.hypot(to[0] - knotAt[0], to[1] - knotAt[2]), drape = 2.4, from = knotAt[1] - 0.04
    const points = Array.from({ length: steps + 1 }, (_, i) => {
      const t = i / steps, [x, z] = along[i * 4]
      const lying = Math.max(...along.map(([ax, az], j) => armTop(ax, az) - drape * length * Math.abs(j / (steps * 4) - t)), 0) + 0.045
      return [x, Math.max(lying, from - drape * length * t * 0.5), z]
    })
    // Across the tail is level, its thickness at right angles to the way it runs.
    const outsT = [], upsT = []
    points.forEach((p, i) => {
      const a = points[Math.max(i - 1, 0)], b = points[Math.min(i + 1, points.length - 1)], t = norm([b[0] - a[0], b[1] - a[1], b[2] - a[2]])
      const across = norm(cross(t, [0, 1, 0])), up = norm(cross(across, t))
      outsT.push(up); upsT.push(across)
    })
    const halves = points.map(() => [0.04, width])
    // Two cream stripes near its end, as on a knitted scarf.
    const shapes = sweep(points, outsT, upsT, halves, (i) => (WINTER.scarf.ends.some(([from, to]) => i / steps >= from && i / steps < to) ? 'stripe' : 'main'), 16)
    // Its end is cut straight: a flat oval closes it.
    const end = points[steps], ring = shapes.main.positions.slice(steps * 16 * 3, (steps + 1) * 16 * 3), way = norm([0, 1, 2].map((k) => end[k] - points[steps - 1][k]))
    const cap = { positions: [...end, ...ring], normals: Array.from({ length: 17 }, () => way).flat(), indices: Array.from({ length: 16 }, (_, j) => [0, 1 + j, 1 + ((j + 1) % 16)]).flat() }
    return { main: join(shapes.main, cap), stripe: shapes.stripe }
  }
  const long = tail([-0.08, 1.86], 20, 0.18), short = tail([1.0, 1.12], 14, 0.16)
  return {
    parts: [
      { name: 'scarf', material: 'knit', ...join(wrap.main, knot, long.main, short.main) },
      { name: 'scarf_stripe', material: 'knit_stripe', ...join(long.stripe, short.stripe) },
    ],
  }
}

/**
 * Builds Winter Poulpy: happy, with his beanie and his scarf, as buildPoulpy3d builds him.
 * @param {{ tones: Record<string, string>, colours: { blue: string, ember: string, ink: string } }} options the `mood` and
 *   `color.poulpy` tokens of design/tokens.json.
 * @returns what buildPoulpy3d returns, with his beanie and scarf as parts of his head (they turn, lean and squash with it).
 */
export function buildWinterPoulpy({ tones, colours }) {
  const model = buildPoulpy3d({ mood: 'happy', tone: tones.happy, features: WINTER.face, colours })
  const ink = linear(colours.ink), mix = (c, t) => c.map((v, i) => v + (ink[i] - v) * t)
  const knitColour = linear(colours.ember), stripeColour = linear(WINTER.knit.stripe)
  Object.assign(model.materials, { knit: { colour: knitColour, outline: mix(knitColour, 0.62) }, knit_stripe: { colour: stripeColour, outline: mix(stripeColour, 0.62) } })
  model.parts.push(...beanie().parts, ...scarf().parts)
  return { ...model, height: model.height + WINTER.beanie.puff + WINTER.beanie.pompom * 1.6 }
}

// --- what is around him ----------------------------------------------------------------------------

// A flat filled outline facing +z, as a fan from its middle.
const fan = (points, z = 0) => ({ positions: [0, 0, z, ...points.flatMap(([x, y]) => [x, y, z])], normals: [0, 0, 1, ...points.flatMap(() => [0, 0, 1])], indices: points.flatMap((_, i) => [0, 1 + i, 1 + ((i + 1) % points.length)]) })
// An outline around its middle as a filling and an edge `width` wide inside the outline, side by side so they never overlap.
function ringed(points, width) {
  const inner = points.map(([x, y]) => { const l = Math.hypot(x, y) || 1; return [x * (1 - width / l), y * (1 - width / l)] }), n = points.length
  const edge = {
    positions: [...points, ...inner].flatMap(([x, y]) => [x, y, 0]), normals: [...points, ...inner].flatMap(() => [0, 0, 1]),
    indices: points.flatMap((_, i) => { const j = (i + 1) % n; return [i, n + i, j, j, n + i, n + j] }),
  }
  return { fill: fan(inner), edge }
}
// A bar from a to b, `width` wide, flat.
const bar = ([ax, ay], [bx, by], width, z = 0) => {
  const l = Math.hypot(bx - ax, by - ay) || 1, nx = (-(by - ay) / l) * (width / 2), ny = ((bx - ax) / l) * (width / 2)
  return { positions: [ax + nx, ay + ny, z, bx + nx, by + ny, z, bx - nx, by - ny, z, ax - nx, ay - ny, z], normals: Array(12).fill(0).map((_, i) => (i % 3 === 2 ? 1 : 0)), indices: [0, 3, 1, 1, 3, 2] }
}
const circle = (r, n = 20) => Array.from({ length: n }, (_, i) => [r * Math.cos((i / n) * TAU), r * Math.sin((i / n) * TAU)])

/**
 * The flat shapes of what is around him, each of size 1 and facing +z, with their colours (`#RRGGBB`), in pieces that lie
 * side by side: `flake` a round snowflake, white with a frosty edge so it shows on a light background too, `crystal` a
 * six-armed one in frost blue, and `puff` a little cloud of his breath, a pale mist with a soft blue edge.
 */
export function winterShapes() {
  // The cloud: the outline of a few round bumps, seen from its middle, so it is one shape with no overlaps.
  const bumps = [[0, 0, 0.62], [0.5, 0.12, 0.46], [-0.5, 0.05, 0.44], [0.12, 0.48, 0.42], [-0.22, -0.36, 0.4], [0.3, -0.34, 0.38]]
  const cloud = Array.from({ length: 64 }, (_, i) => {
    const a = (i / 64) * TAU, d = [Math.cos(a), Math.sin(a)]
    // How far along this way the furthest bump reaches.
    const far = Math.max(...bumps.map(([x, y, r]) => { const b = x * d[0] + y * d[1], c = x * x + y * y - r * r, disc = b * b - c; return disc < 0 ? 0 : b + Math.sqrt(disc) }))
    return [d[0] * far * 0.9, d[1] * far * 0.9]
  })
  // A crystal's arm: a bar out from its middle with two little branches.
  const arm = (a) => {
    const d = [Math.cos(a), Math.sin(a)], at = (r) => [d[0] * r, d[1] * r]
    const branch = (turn) => { const p = at(0.55), b = a + turn; return bar(p, [p[0] + Math.cos(b) * 0.32, p[1] + Math.sin(b) * 0.32], 0.13) }
    return join(bar([0, 0], at(1), 0.16), branch(0.8), branch(-0.8))
  }
  const round = ringed(circle(1, 20), 0.24), mist = ringed(cloud, 0.1)
  return {
    flake: [{ shape: round.fill, colour: SNOW }, { shape: round.edge, colour: FROST }],
    crystal: [{ shape: join(...[0, 1, 2, 3, 4, 5].map((i) => arm((i * TAU) / 6 + Math.PI / 2)), fan(circle(0.2, 12))), colour: FROST }],
    puff: [{ shape: mist.fill, colour: MIST }, { shape: mist.edge, colour: MIST_EDGE }],
  }
}

// --- his idle --------------------------------------------------------------------------------------

/**
 * Winter Poulpy's idle, as something to advance in time: he sways, breathes and puffs mist, while snow falls. Every run is
 * the same: the snow's chances come from a fixed list, as the champion's confetti's do.
 * @returns {{ advance(seconds: number): WinterPose, pose(): WinterPose }} A pose is a turn's pose and `{ tilt, snow, breath }`:
 *   how far his head leans to his right, in radians (as the wink's); for each flake `[x, y, z, size, spin, crystal]`, where it
 *   is in the room, its size (0 when it is not there), how far it has turned on itself and 1 for a crystal; for each puff of
 *   breath `[x, y, z, size, opacity, spin]`, size 0 when it is not there.
 */
export function createWinter({ winter = WINTER } = {}) {
  const SEED = 20261217
  let seed = SEED
  const chance = () => {
    seed = (seed + 0x6d2b79f5) | 0
    let t = Math.imul(seed ^ (seed >>> 15), 1 | seed)
    t = (t + Math.imul(t ^ (t >>> 7), 61 | t)) ^ t
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296
  }
  const between = ([low, high]) => low + (high - low) * chance()
  const spring = (s, goal, value, rate) => s.speed * s.speed * (goal - value) - 2 * s.damping * s.speed * rate
  const RAD = Math.PI / 180, SUBSTEP = 1 / 240
  const sn = winter.snow, sw = winter.sway, br = winter.breath, pf = winter.puffs

  // The snow: each flake starts somewhere in the box, clear of him.
  const place = (flake, anywhere) => {
    let x, z
    do { x = (chance() * 2 - 1) * sn.wide; z = sn.back + chance() * (sn.front - sn.back) } while (Math.hypot(x, z) < sn.clear)
    Object.assign(flake, { x, z, y: anywhere ? chance() * sn.top : sn.top + chance() * 0.4, fall: between(sn.fall), sway: between(sn.sway), beat: 0.6 + chance() * 0.7, phase: chance() * TAU, spin: chance() * TAU, turning: (chance() * 2 - 1) * sn.spin, melting: 0 })
    flake.size = flake.crystal ? between(sn.crystal) : between(sn.size)
  }
  const flakes = Array.from({ length: sn.flakes }, (_, i) => { const flake = { crystal: i < sn.crystals }; place(flake, true); return flake })

  // Him: where he faces, how his head leans and how it breathes.
  const follow = createFollow()
  let time = 0, yaw = 0, yawSpeed = 0, tilt = 0, tiltSpeed = 0, squash = 0, squashSpeed = 0, arms = follow.advance(0, 0).arms
  let breathing = 0, lastBreath = -1
  const puffs = Array.from({ length: pf.count * 3 }, () => ({ age: Infinity, at: [0, 0, 0], speed: [0, 0, 0], spin: 0 }))
  let nextPuff = 0, owed = 0, pending = []

  function tick(h) {
    time += h
    // He looks one way, then the other; his head leans the way he looks.
    const side = Math.floor(time / sw.seconds) % 2 ? -1 : 1
    yawSpeed += spring(sw.body, side * sw.degrees * RAD, yaw, yawSpeed) * h
    yaw += yawSpeed * h
    tiltSpeed += spring(sw.head, -side * sw.tilt * RAD, tilt, tiltSpeed) * h
    tilt += tiltSpeed * h
    arms = follow.advance(h, yaw).arms

    // Breathing: in, then out. Each breath out starts a few puffs of mist.
    const into = (time % br.seconds) / br.seconds, breath = Math.floor(time / br.seconds), out = into >= br.inhale
    squashSpeed += spring(br, out ? -br.squash : br.stretch, squash, squashSpeed) * h
    squash += squashSpeed * h
    if (out && breath !== lastBreath) { lastBreath = breath; for (let i = 0; i < pf.count; i++) pending.push(time + i * pf.apart) }
    breathing = into
    pending = pending.filter((when) => {
      if (when > time) return true
      // From just in front of his mouth, as his head is turned and leant, then forwards the way he faces and a little up.
      const [mx, my, mz] = pf.mouth, lx = mx * Math.cos(tilt) - my * Math.sin(tilt), ly = mx * Math.sin(tilt) + my * Math.cos(tilt)
      const at = [lx * Math.cos(yaw) + mz * Math.sin(yaw), ly * (1 - squash * 0.5), -lx * Math.sin(yaw) + mz * Math.cos(yaw)]
      const puff = puffs[nextPuff]
      nextPuff = (nextPuff + 1) % puffs.length
      const way = yaw + side * pf.aside + (chance() * 2 - 1) * 0.15
      Object.assign(puff, { age: 0, at, speed: [Math.sin(way) * pf.speed, pf.rise, Math.cos(way) * pf.speed], spin: chance() * TAU })
      return false
    })
    for (const puff of puffs) {
      if (puff.age > pf.life) continue
      puff.age += h
      for (let k = 0; k < 3; k++) { puff.speed[k] -= (k === 1 ? 0.3 : pf.drag) * puff.speed[k] * h; puff.at[k] += puff.speed[k] * h }
      // Warm breath keeps floating up a little after it has slowed down.
      puff.at[1] += pf.rise * 0.35 * h
    }

    // The snow falls, sways, turns, and melts where it lands.
    for (const flake of flakes) {
      if (flake.melting > 0) { flake.melting += h; if (flake.melting > sn.melt) place(flake, false); continue }
      flake.y -= flake.fall * h
      flake.spin += flake.turning * h
      if (flake.y <= 0.01) { flake.y = 0.01; flake.melting = h }
    }
  }

  const sim = {
    pose: () => ({
      yaw, squash, arms, tilt, breathing,
      snow: flakes.map((f) => [f.x + f.sway * Math.sin(f.phase + time * f.beat * TAU * 0.5), f.y, f.z, f.size * (f.melting > 0 ? Math.max(1 - f.melting / sn.melt, 0) : 1), f.spin, f.crystal ? 1 : 0]),
      breath: puffs.map((p) => {
        if (p.age > pf.life) return [0, -1, 0, 0, 0, 0]
        const t = p.age / pf.life, grown = 1 - (1 - t) ** 2
        return [...p.at, pf.grow[0] + (pf.grow[1] - pf.grow[0]) * grown, pf.opacity * Math.min(p.age / 0.12, 1) * (1 - t) ** 1.4, p.spin]
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
