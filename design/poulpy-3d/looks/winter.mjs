// Winter Poulpy, one of his looks: happy, in fluffy ear muffs and the autumn look's striped scarf in winter colours, while
// snow falls around him and his breath shows as little puffs of mist in the cold air. Plain JavaScript with no dependency, built on the
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
import { SHAPE, FACES, buildPoulpy3d, egg, facing, join, linear, tube } from '../model.mjs'
import { AUTUMN, scarf } from './autumn.mjs'
import { createFollow } from '../motion.mjs'

const FROST = '#9CCFEE', MIST = '#F2F8FC', MIST_EDGE = '#A9CDE6'

export const WINTER = {
  // His face: the happy one, with cheeks rosier than usual from the cold.
  face: [...FACES.happy.filter(([kind]) => kind !== 'cheek'), ['cheek', 63, 112, 10, 5.5, '#F27C6B', 0.85], ['cheek', 137, 112, 10, 5.5, '#F27C6B', 0.85]],
  // The set's two colours: a warm cream wool and a deep berry. Never ember, his angry side, nor the autumn scarf's red; both
  // stand out against his blue, the cream by its lightness and the berry by its depth.
  wool: '#FBF4E6', berry: '#8C2F5E',
  // The scarf is exactly the autumn look's (autumn.mjs): the same shape, size and place, knot and ends, in the winter colours.
  scarf: { ...AUTUMN.scarf, wool: '#FBF4E6', stripe: '#8C2F5E' },
  // The ear muffs sit where the DJ's headset does (dj.mjs): each muff `down` from the top of his head (an angle), on his side,
  // over where his ears would be, `lift` off his skin. A thin berry `band` goes over the top `out` off his head. Each muff is a
  // ball `size` wide and `depth` deep, covered in `tufts` smaller balls `tuft` of its size, so its outline is furry.
  muffs: { down: 1.36, lift: 0.1, size: 0.34, depth: 0.22, tufts: 34, tuft: 0.36, band: { width: 0.045, out: 0.045 } },
  // His idle. He looks a little to one side, then to the other, every `seconds`, by `degrees`, on a spring as soft as
  // `body`. His head leans with him by `tilt` degrees.
  sway: { seconds: 2.6, degrees: 8, tilt: 4, body: { speed: 5.5, damping: 0.62 }, head: { speed: 6, damping: 0.55 } },
  // He breathes in and out every `seconds`: in, his head stretches by `stretch`; out, it squashes by `squash`.
  breath: { seconds: 3.4, inhale: 0.55, stretch: 0.018, squash: 0.03, speed: 9, damping: 0.7 },
  // Each breath out is `count` little puffs of mist, `apart` seconds apart, from just in front of his `mouth`. Each leaves
  // forwards at `speed`, `aside` of it towards the side he looks to so it clears his face, and floats up at `rise`, slowed
  // by `drag`; it grows from `grow[0]` to `grow[1]` and fades out over `life` seconds.
  puffs: { count: 3, apart: 0.16, life: 2.2, speed: 1.7, aside: 1.2, rise: 0.3, drag: 1.5, grow: [0.13, 0.46], opacity: 0.9, mouth: [0, 0.74, 1.2] },
  // The snow: `flakes` six-armed crystals, falling from `top` through a box `wide` to each side and from `back` to `front`,
  // never closer to his middle than `clear`. Each falls at `fall` units a second, swaying sideways by `sway` units, `size`
  // big from its middle to a tip, and melts on the floor in `melt` seconds.
  snow: { flakes: 26, top: 4.6, wide: 3.6, back: -3, front: 2.8, clear: 1.75, fall: [0.3, 0.5], sway: [0.08, 0.26], size: [0.09, 0.14], spin: 1.1, melt: 0.7 },
}

// --- small maths -----------------------------------------------------------------------------------

const TAU = Math.PI * 2
const { head } = SHAPE
// A point on his head, `out` off it: `down` is the angle from its top, `around` from his front towards his left (as in dj.mjs).
const onHead = (down, around, out = 0) => [0, 1, 2].map((k) => [0, head.centreY, 0][k] + head.radius[k] * (1 + out) * [Math.sin(down) * Math.sin(around), Math.cos(down), Math.sin(down) * Math.cos(around)][k])

// --- what he wears ---------------------------------------------------------------------------------

// The ear muffs: a thin band over his head from one muff to the other, and a fluffy muff on each side, where the DJ's headset is.
function muffs() {
  const m = WINTER.muffs, around = (side) => (side * Math.PI) / 2
  const over = Array.from({ length: 25 }, (_, i) => { const t = -1 + i / 12; return onHead(Math.abs(t) * m.down, around(t < 0 ? -1 : 1), m.band.out) })
  const fluff = [-1, 1].map((side) => {
    const at = (lift) => onHead(m.down, around(side), lift), n = at(1).map((v, k) => v - at(0)[k]), l = Math.hypot(...n), way = facing(n.map((v) => v / l))
    // The muff's middle; in its own frame x and y run across his side and z out of it.
    const centre = at(m.lift), r = m.size, d = m.depth
    const ball = egg([r * 0.86, r * 0.86, d * 0.86], { around: 24, down: 14, turn: way, at: centre })
    // Tufts all over its outer side and its rim, spread evenly (a Fibonacci sphere), none on the side against his head.
    const tufts = []
    for (let i = 0; tufts.length < m.tufts; i++) {
      const z = 1 - (2 * (i + 0.5)) / (m.tufts * 1.6), ring = Math.sqrt(Math.max(1 - z * z, 0)), a = i * 2.39996
      if (z < -0.25) break
      const p = way([Math.cos(a) * ring * r * 0.82, Math.sin(a) * ring * r * 0.82, z * d * 0.82])
      tufts.push(egg([r * m.tuft, r * m.tuft, r * m.tuft], { around: 12, down: 8, at: p.map((v, k) => v + centre[k]) }))
    }
    return join(ball, ...tufts)
  })
  return {
    materials: { muff_band: { colour: linear(WINTER.berry) }, muff_fur: { colour: linear(WINTER.wool) } },
    parts: [
      { name: 'muffs_band', material: 'muff_band', ...tube(over, m.band.width, 12) },
      { name: 'muffs', material: 'muff_fur', ...join(...fluff) },
    ],
  }
}

/**
 * Builds Winter Poulpy: happy, with his ear muffs and his scarf, as buildPoulpy3d builds him.
 * @param {{ tones: Record<string, string>, colours: { blue: string, ember: string, ink: string } }} options the `mood` and
 *   `color.poulpy` tokens of design/tokens.json.
 * @returns what buildPoulpy3d returns, with his ear muffs and scarf as parts of his head (they turn, lean and squash with it).
 */
export function buildWinterPoulpy({ tones, colours }) {
  const model = buildPoulpy3d({ mood: 'happy', tone: tones.happy, features: WINTER.face, colours })
  for (const made of [muffs(), scarf(WINTER.scarf)]) {
    Object.assign(model.materials, made.materials)
    model.parts.push(...made.parts)
  }
  return { ...model, headTilt: 0 }
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
 * side by side: `crystal` a six-armed snowflake in frost blue, which shows on a light page and a dark one, and `puff` a
 * little cloud of his breath, a pale mist with a soft blue edge.
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
  const mist = ringed(cloud, 0.1)
  return {
    crystal: [{ shape: join(...[0, 1, 2, 3, 4, 5].map((i) => arm((i * TAU) / 6 + Math.PI / 2)), fan(circle(0.2, 12))), colour: FROST }],
    puff: [{ shape: mist.fill, colour: MIST }, { shape: mist.edge, colour: MIST_EDGE }],
  }
}

// --- his idle --------------------------------------------------------------------------------------

/**
 * Winter Poulpy's idle, as something to advance in time: he sways, breathes and puffs mist, while snow falls. Every run is
 * the same: the snow's chances come from a fixed list, as the champion's confetti's do.
 * @returns {{ advance(seconds: number): WinterPose, pose(): WinterPose }} A pose is a turn's pose and `{ tilt, snow, breath }`:
 *   how far his head leans to his right, in radians (as the wink's); for each flake `[x, y, z, size, spin]`, where it
 *   is in the room, its size (0 when it is not there) and how far it has turned on itself; for each puff of
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
    flake.size = between(sn.size)
  }
  const flakes = Array.from({ length: sn.flakes }, () => { const flake = {}; place(flake, true); return flake })

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
      snow: flakes.map((f) => [f.x + f.sway * Math.sin(f.phase + time * f.beat * TAU * 0.5), f.y, f.z, f.size * (f.melting > 0 ? Math.max(1 - f.melting / sn.melt, 0) : 1), f.spin]),
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
