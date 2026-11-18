// Rodolphe: Poulpy dressed as a red-nosed reindeer, a look for the festive season. A draft, shown on its own page of the
// component workshop (Brand > Poulpy looks > Rodolphe); nothing in the apps uses it yet.
//
// A look is Poulpy on his blue side with a face of his own, what he wears, and an idle move. It is built like the poses
// of poses.mjs and moves like the moves of motion.mjs, and keeps to their rules: his arms stay on the floor, what he
// wears is solid and goes with his head, and he is shaded flat.
//   FACE       his happy face, a little higher so his nose fits between his eyes and his smile
//   COLOURS    the felt of the antlers, the band and the ears, the bells, the holly, the nose, the scarf's two stripes
//   IDLE       how he sways to a tune, his nose glowing on the beat; createRodolpheIdle runs it
// To change the look, edit FACE, COLOURS, the shapes in buildRodolphe or IDLE, then look at the page.
import { SHAPE, buildPoulpy3d, egg, facing, join, linear, tube } from '../model.mjs'
import { createFollow } from '../motion.mjs'

// His colours. The reds are a bright, warm cherry, lighter and yellower than the error crimson (#C8264A) and redder than
// any ember shade, and he wears them on his blue side only: Rodolphe is always happy.
export const COLOURS = {
  antler: '#5E3A22', // chunky dark-brown felt antlers
  band: '#9A6A42', // the flat felt headband they stand on
  bell: '#F0B53A', // the jingle bells on the antlers' tips, the gold of his medal
  slot: '#8A5A12', // the slot across each bell
  ear: '#C89A6A', // the outside of his felt ears
  inner: '#F5E6CC', // their cream inside
  holly: '#2E8B4E', // the holly leaves
  berry: '#E2373F', // the holly berries, the scarf's red
  nose: '#F2353A',
  lit: '#FF8C8C', // what his nose turns towards as it glows
  glow: '#FF5A4E', // the light his nose gives off, drawn by whoever makes it glow
  scarf: '#E2373F', // the scarf's red stripes; the others are white
  white: '#FFFFFF',
}

// His face, in the drawing's coordinates like FACES in model.mjs: the happy face, eyes 8 higher and a smaller smile,
// with room between them for the nose and above the scarf for the smile.
const cheeks = (y) => [['cheek', 64, y, 8, 4.5, '#F49A6B', 0.75], ['cheek', 136, y, 8, 4.5, '#F49A6B', 0.75]]
export const FACE = [['eye', 80, 88, 9, 10.5], ['eye', 120, 88, 9, 10.5], ['curve', 91, 111, 100, 120, 109, 111, 4.5, 'mouth'], ...cheeks(104)]
// Where his nose sits in the drawing, and its size: the drawing's units.
const NOSE = { x: 100, y: 98, r: 8.4 }

// --- small maths -----------------------------------------------------------------------------------

const { head, face, arms } = SHAPE
const C = [0, head.centreY, 0]
const add = (a, b, k = 1) => [a[0] + b[0] * k, a[1] + b[1] * k, a[2] + b[2] * k]
const norm = (v) => { const l = Math.hypot(...v) || 1; return v.map((x) => x / l) }
const cross = (a, b) => [a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0]]
// Where the head is met going out from its middle along `dir`, `out` off it, and which way it faces there.
function onHead(dir, out = 0) {
  const u = norm(dir), s = 1 / Math.hypot(u[0] / head.radius[0], u[1] / head.radius[1], u[2] / head.radius[2]), p = add(C, u, s)
  const n = norm([0, 1, 2].map((k) => (p[k] - C[k]) / head.radius[k] ** 2))
  return { p: add(p, n, out), n }
}
// A point of the drawing on the front of his head, as model.mjs presses his face.
function onFace(x, y) {
  const U = 63, px = ((x - 100) / U) * face.scale * face.spread, py = face.y + ((130 - y) / U) * face.scale, dy = py - C[1]
  const pz = head.radius[2] * Math.sqrt(Math.max(1 - (px / head.radius[0]) ** 2 - (dy / head.radius[1]) ** 2, 0))
  return { p: [px, py, pz], n: norm([px / head.radius[0] ** 2, dy / head.radius[1] ** 2, pz / head.radius[2] ** 2]) }
}
// Turns an egg's own axes onto `along` (its z), `up` (its y) and what is across both (its x).
function lying(along, up) {
  const z = norm(along), x = norm(cross(up, z)), y = cross(z, x)
  return ([a, b, c]) => [0, 1, 2].map((k) => x[k] * a + y[k] * b + z[k] * c)
}
// How high the top of an arm is, `r` from his middle along it.
const armTop = (r) => arms.radius[1] + arms.radius[1] * Math.sqrt(Math.max(1 - ((r - arms.ring) / arms.radius[2]) ** 2, 0))

// --- what he wears ---------------------------------------------------------------------------------

// The headband: a flat, broad felt strip over the top of his head, leaning a little back. Seen from above it is wide
// front to back; seen from the front it is a solid felt band. It stops well above his sides.
const BAND = { back: 0.3, reach: 1.02, width: 0.1, thick: 0.05 }
const bandAt = (t, out = BAND.thick) => onHead([Math.sin(t), Math.cos(t) * Math.cos(BAND.back), -Math.cos(t) * Math.sin(BAND.back)], out)
// Along the band at `t`: where it is, the way it runs, the way it faces and what is across it (towards his front).
function bandFrame(t, out) {
  const { p, n } = bandAt(t, out), ahead = bandAt(t + 0.01, out).p, along = norm(ahead.map((v, k) => v - p[k]))
  return { p, n, along, across: norm(cross(along, n)) }
}
// A flat strip with round edges: an oval section, `width` across and `thick` through, swept along the band.
function bandShape() {
  const steps = 40, sides = 20, positions = [], normals = [], indices = []
  for (let i = 0; i <= steps; i++) {
    const { p, n, across } = bandFrame(-BAND.reach + (2 * BAND.reach * i) / steps, BAND.thick)
    for (let j = 0; j < sides; j++) {
      const a = (j / sides) * Math.PI * 2, c = Math.cos(a), sn = Math.sin(a)
      positions.push(...add(add(p, across, c * BAND.width), n, sn * BAND.thick))
      normals.push(...norm(add(across.map((v) => (v * c) / BAND.width), n, sn / BAND.thick)))
    }
  }
  for (let i = 0; i < steps; i++) for (let j = 0; j < sides; j++) {
    const a = i * sides + j, b = i * sides + ((j + 1) % sides), c = a + sides, d = b + sides
    indices.push(a, c, b, b, c, d)
  }
  // Each end is a flat round cap.
  const cap = (t) => { const { p, n, along } = bandFrame(t, BAND.thick); return egg([BAND.width, BAND.thick, BAND.thick * 1.6], { around: 20, down: 10, turn: lying(along, n), at: p }) }
  return join({ positions, normals, indices }, cap(-BAND.reach), cap(BAND.reach))
}

// The antlers sit low on the band, `at` from the top: chunky dark felt, a fat beam that rises and bows out with two
// round tines branching outwards, and a little gold jingle bell on every tip. Nothing has a point.
const ANTLER = { at: 0.5, size: 1.4, tall: 1.4, beam: [0.1, 0.13, 0.125, 0.115, 0.105], tine: 0.09, bell: 0.058 }

function antlerParts(sign) {
  const { p, n } = bandAt(sign * ANTLER.at, 0), out = [sign, 0, 0], up = [0, 1, 0]
  // Measured from where the antler meets the band: `a` along his head's normal, `b` outwards, `c` up, `d` forwards, all but `a` times `size`, and `c` times `tall` too.
  const go = (a, b, c, d = 0) => add(add(add(add(p, n, a), out, b * ANTLER.size), up, c * ANTLER.size * ANTLER.tall), [0, 0, 1], d * ANTLER.size)
  const beam = [go(0.02, 0, 0), go(0.04, 0.04, 0.14), go(0.04, 0.1, 0.28), go(0.04, 0.12, 0.42, -0.02), go(0.04, 0.09, 0.53, -0.03)]
  const tines = [
    [go(0.04, 0.05, 0.15), go(0.04, 0.16, 0.22, 0.03), go(0.04, 0.28, 0.25, 0.05)],
    [go(0.04, 0.11, 0.33), go(0.04, 0.2, 0.41, 0.03), go(0.04, 0.29, 0.46, 0.05)],
  ]
  // A bell hangs just past each tip, the way the tip points, with a short dark slot across its front.
  const tips = [beam, ...tines].map((line) => {
    const end = line[line.length - 1], before = line[line.length - 2], way = norm(end.map((v, k) => v - before[k]))
    return add(end, way, (line === beam ? ANTLER.beam[ANTLER.beam.length - 1] : ANTLER.tine) + ANTLER.bell * 0.7)
  })
  const front = norm([0, -0.25, 0.97])
  return {
    felt: join(tube(beam, ANTLER.beam, 18), ...tines.map((tine) => tube(tine, ANTLER.tine, 16))),
    bells: join(...tips.map((at) => egg([ANTLER.bell, ANTLER.bell, ANTLER.bell], { around: 18, down: 12, at }))),
    slots: join(...tips.map((at) => egg([ANTLER.bell * 0.7, ANTLER.bell * 0.16, ANTLER.bell * 0.3], { around: 12, down: 6, at: add(at, front, ANTLER.bell * 0.9), turn: facing(front) }))),
  }
}

// A little felt reindeer ear on each side of the band, below and outside the antlers, pointing outwards and a little
// up: tan outside, cream inside, the inside facing forwards. Round, like a leaf of felt.
const EAR = { at: 0.88, size: [0.28, 0.14, 0.045], lift: 0.35 }
function ear(sign) {
  const { p, n } = bandFrame(sign * EAR.at, BAND.thick)
  const way = norm(add([sign, 0, 0], [0, 1, 0], EAR.lift)), at = add(add(p, way, EAR.size[0] * 0.85), n, 0.02), turn = lying(way, [0, 0, 1])
  return {
    outer: egg([EAR.size[1], EAR.size[2], EAR.size[0]], { around: 18, down: 10, turn, at }),
    inner: egg([EAR.size[1] * 0.55, EAR.size[2] * 0.6, EAR.size[0] * 0.66], { around: 16, down: 8, turn, at: add(add(at, [0, 0, 1], EAR.size[2] * 0.7), way, -EAR.size[0] * 0.08) }),
  }
}

// A full cluster of holly at the front of each antler's base: six round-ended leaves fanned out on the band, none
// pointing down towards his eyes, and five berries in the middle.
function holly(sign) {
  const { p, across } = bandFrame(sign * ANTLER.at, BAND.thick * 1.6), front = add(p, across, BAND.width * 0.75)
  const leaves = [-0.8, -0.15, 0.5, 1.15, 1.8, 2.45].map((angle, i) => {
    const way = norm(add([sign * Math.cos(angle), 0, 0], [0, 1, 0], Math.sin(angle) * 0.85))
    return egg([0.085, 0.024, 0.155], { around: 16, down: 8, turn: lying(way, across), at: add(add(front, way, 0.13), across, 0.012 * (i % 3)) })
  })
  const berries = [[0, 0.03], [0.065, 0.0], [-0.06, -0.01], [0.03, -0.055], [-0.03, -0.06]].map(([dx, dy]) => egg([0.048, 0.048, 0.048], { around: 14, down: 8, at: add(add(add(front, [1, 0, 0], dx * sign), [0, 1, 0], dy), across, 0.05) }))
  return { leaves: join(...leaves), berries: join(...berries) }
}

// The scarf: puffy knitted pieces in turn red and white, all around the base of his head, sitting on his arms. It is
// lower in front, under his smile, and higher behind. It is knotted over the arm at his front right, and its two
// ends lie along that arm towards the floor.
const SCARF = { pieces: 14, front: 0.62, back: 0.74, thick: [0.095, 0.105], out: 0.08, knot: -Math.PI / 4 }

function scarf() {
  const red = [], white = [], endRed = [], endWhite = [], { pieces, thick } = SCARF
  const ringAt = (a) => {
    const y = SCARF.front + (SCARF.back - SCARF.front) * (1 - Math.cos(a)) / 2, dy = (y - C[1]) / head.radius[1], k = Math.sqrt(Math.max(1 - dy * dy, 0))
    return [Math.sin(a) * (head.radius[0] * k + SCARF.out), y, Math.cos(a) * (head.radius[2] * k + SCARF.out)]
  }
  for (let i = 0; i < pieces; i++) {
    const a = SCARF.knot + ((i + 0.5) / pieces) * Math.PI * 2, at = ringAt(a), next = ringAt(a + 0.01)
    const along = norm(next.map((v, k) => v - at[k])), length = (Math.PI * 2 * 1.12) / pieces / 2 * 1.45
    ;(i % 2 ? white : red).push(egg([thick[0], thick[1], length], { around: 16, down: 10, turn: lying(along, [0, 1, 0]), at }))
  }
  // The two ends lie from the knot along the arm under it, flat on it, and stop before its tip.
  for (const [side, reach, count] of [[0.08, 0.4, 5], [-0.1, 0.32, 4]]) {
    for (let i = 0; i < count; i++) {
      const r = 1.12 + (reach * (i + 0.5)) / count, a = SCARF.knot + side / r, y = armTop(r) + 0.05
      const r2 = r + 0.02, y2 = armTop(r2) + 0.05, at = [Math.sin(a) * r, y, Math.cos(a) * r], ahead = [Math.sin(a) * r2, y2, Math.cos(a) * r2]
      const along = norm(ahead.map((v, k) => v - at[k]))
      ;(i % 2 ? endWhite : endRed).push(egg([0.15, 0.06, (reach / count) * 0.72], { around: 18, down: 10, turn: lying(along, [0, 1, 0]), at }))
    }
  }
  const knot = ringAt(SCARF.knot)
  red.push(egg([0.14, 0.13, 0.12], { around: 18, down: 12, at: add(knot, norm([Math.sin(SCARF.knot), 0, Math.cos(SCARF.knot)]), 0.05) }))
  return { red: join(...red), white: join(...white), endRed: join(...endRed), endWhite: join(...endWhite), knot }
}

/** Where the scarf is knotted: its ends swing around the upright line through it. */
export const scarfKnot = scarf().knot

// The nose: a round bulb half out of his face, with a white dot of light like his eyes have.
function nose() {
  const { p, n } = onFace(NOSE.x, NOSE.y), r = (NOSE.r / 63) * face.scale, at = add(p, n, r * 0.55)
  const towards = norm([-0.45, 0.55, 0.7]), dot = facing(towards)
  return {
    at,
    bulb: egg([r, r * 0.94, r], { around: 28, down: 16, at }),
    shine: egg([r * 0.3, r * 0.22, 0.012], { around: 14, down: 8, turn: dot, at: add(at, towards, r * 0.93) }),
  }
}
/** The middle of his nose and its size, for whoever makes it glow. */
export const noseBulb = { at: nose().at, radius: (NOSE.r / 63) * face.scale }

/**
 * Builds Rodolphe, standing still.
 * @param {{ colours: { blue: string, ember: string, ink: string }, tone?: string }} options `colours` are the `color.poulpy`
 *   tokens and `tone` the happy `mood` token.
 * @returns what buildPoulpy3d returns, with his headband, his nose and his scarf among the parts: `antler_band`, `antlers`,
 *   `antler_bells`, `antler_bell_slots`, `ears`, `ears_inner`, `holly`, `holly_berries`, `nose`, `nose_shine`, `scarf_red`, `scarf_white` and its ends `scarf_end_red`, `scarf_end_white`.
 *   They ride with his head.
 */
export function buildRodolphe({ colours, tone }) {
  const model = buildPoulpy3d({ mood: 'happy', tone, features: FACE, colours })
  const scarfShape = scarf(), noseShape = nose(), antlers = [antlerParts(1), antlerParts(-1)], ears = [ear(1), ear(-1)], sprigs = [holly(1), holly(-1)]
  Object.assign(model.materials, {
    antler: { colour: linear(COLOURS.antler) },
    antler_band: { colour: linear(COLOURS.band) },
    bell: { colour: linear(COLOURS.bell) },
    bell_slot: { colour: linear(COLOURS.slot), flat: true },
    ear: { colour: linear(COLOURS.ear) },
    ear_inner: { colour: linear(COLOURS.inner) },
    holly: { colour: linear(COLOURS.holly) },
    berry: { colour: linear(COLOURS.berry) },
    nose: { colour: linear(COLOURS.nose) },
    scarf_red: { colour: linear(COLOURS.scarf) },
    scarf_white: { colour: linear(COLOURS.white) },
  })
  // The gleam stays the last of his parts, as in model.mjs.
  const gleam = model.parts.pop()
  model.parts.push(
    { name: 'antler_band', material: 'antler_band', ...bandShape() },
    { name: 'antlers', material: 'antler', ...join(...antlers.map((a) => a.felt)) },
    { name: 'antler_bells', material: 'bell', ...join(...antlers.map((a) => a.bells)) },
    { name: 'antler_bell_slots', material: 'bell_slot', ...join(...antlers.map((a) => a.slots)) },
    { name: 'ears', material: 'ear', ...join(...ears.map((e) => e.outer)) },
    { name: 'ears_inner', material: 'ear_inner', ...join(...ears.map((e) => e.inner)) },
    { name: 'holly', material: 'holly', ...join(...sprigs.map((h) => h.leaves)) },
    { name: 'holly_berries', material: 'berry', ...join(...sprigs.map((h) => h.berries)) },
    { name: 'scarf_red', material: 'scarf_red', ...scarfShape.red },
    { name: 'scarf_white', material: 'scarf_white', ...scarfShape.white },
    { name: 'scarf_end_red', material: 'scarf_red', ...scarfShape.endRed },
    { name: 'scarf_end_white', material: 'scarf_white', ...scarfShape.endWhite },
    { name: 'nose', material: 'nose', ...noseShape.bulb },
    { name: 'nose_shine', material: 'shine', ...noseShape.shine },
    gleam,
  )
  // The antlers rise above his head.
  const top = join(antlers[0].felt, antlers[0].bells)
  for (let i = 1; i < top.positions.length; i += 3) model.height = Math.max(model.height, top.positions[i])
  return model
}

// --- his idle --------------------------------------------------------------------------------------

export const IDLE = {
  // He sways to a tune: a step to his left, a step to his right, `beat` seconds each. His body turns by `degrees`
  // towards the side, on a spring, and his arms trail behind it as they do in TURN.
  beat: 0.6,
  sway: { degrees: 8, speed: 9, damping: 0.5 },
  // With each step his head leans that way by `degrees`, a little after his body.
  lean: { degrees: 4, speed: 8, damping: 0.45 },
  // On each beat his head dips by `depth` of its height for `seconds`, and bounces back: the bob of a little dance.
  bob: { depth: 0.045, seconds: 0.12, speed: 20, damping: 0.32 },
  // His nose glows on every `every` beats: it lights up to full at `rise` a second and fades back to `rest` on a spring.
  glow: { every: 2, rest: 0.2, rise: 9, speed: 5, damping: 0.7 },
  // The scarf's ends trail the sway around their knot: a spring, softer than the arms, `most` radians at the most.
  ends: { speed: 7, damping: 0.3, most: 0.35 },
  // Every `rest` beats he holds still for one tune's worth: two beats with no sway, only the glow.
  rest: 8,
}

const RAD = Math.PI / 180, SUBSTEP = 1 / 240

/**
 * Rodolphe swaying to a tune, as something to advance in time.
 * @param {{ softness?: number, idle?: typeof IDLE }} [options] `softness` is how loose his arms are, as in createTurn.
 * @returns the move. A pose is a turn's pose and `{ tilt, glow, ends }`: how far his head leans to his right in radians
 *   (to his left when negative), how bright his nose is from 0 to 1, and how far the scarf's ends have swung, in radians.
 */
export function createRodolpheIdle({ softness = 1, idle = IDLE } = {}) {
  const follow = createFollow({ softness })
  const spring = (s, goal, value, rate) => s.speed * s.speed * (goal - value) - 2 * s.damping * s.speed * rate
  let clock = 0, beat = 0, yaw = 0, yawSpeed = 0, tilt = 0, tiltSpeed = 0, squash = 0, squashSpeed = 0
  let glow = idle.glow.rest, glowSpeed = 0, lit = false, ends = 0, endsSpeed = 0
  let pose = follow.advance(0, 0)

  function tick(h) {
    clock += h
    while (clock >= idle.beat) { clock -= idle.beat; beat++ }
    // Two beats out of every `rest` are still; the others sway to one side, then the other.
    const still = beat % idle.rest >= idle.rest - 2, side = still ? 0 : beat % 2 ? -1 : 1
    yawSpeed += spring(idle.sway, side * idle.sway.degrees * RAD, yaw, yawSpeed) * h
    yaw += yawSpeed * h
    tiltSpeed += spring(idle.lean, -side * idle.lean.degrees * RAD, tilt, tiltSpeed) * h
    tilt += tiltSpeed * h
    const dipping = !still && clock < idle.bob.seconds
    squashSpeed += spring(idle.bob, dipping ? -idle.bob.depth : 0, squash, squashSpeed) * h
    squash += squashSpeed * h
    // The glow lights up on its beat, quickly, then a spring brings it back down.
    lit = beat % idle.glow.every === 0 && clock < idle.beat / 2
    if (lit) { glow = Math.min(glow + idle.glow.rise * h, 1); glowSpeed = 0 } else {
      glowSpeed += spring(idle.glow, idle.glow.rest, glow, glowSpeed) * h
      glow = Math.min(Math.max(glow + glowSpeed * h, 0), 1)
    }
    // The ends lag behind the way he turns.
    endsSpeed += (spring(idle.ends, 0, ends, endsSpeed) - yawSpeed * idle.ends.speed) * h
    ends = Math.min(Math.max(ends + endsSpeed * h, -idle.ends.most), idle.ends.most)
    pose = follow.advance(h, yaw)
  }

  let owed = 0
  const sim = {
    softness,
    pose: () => ({ ...pose, squash, tilt, glow, ends }),
    advance(seconds) {
      follow.softness = sim.softness
      owed += Math.min(Math.max(seconds, 0), 1)
      while (owed >= SUBSTEP) { tick(SUBSTEP); owed -= SUBSTEP }
      return sim.pose()
    },
  }
  return sim
}
