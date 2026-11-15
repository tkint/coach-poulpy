// Poulpy in 3D: the one description of his shape, with no dependency, so anything can build him from it.
//   design/build-poulpy-3d.mjs writes it as glTF files; the docs site's "Poulpy in 3D" page draws it live.
//
// How he moves is next door, in motion.mjs.
// To change the design, edit SHAPE, FACES, MOODS or LOOK below, then run `node design/build-poulpy-3d.mjs`.
// Units: the head is about 1 wide from its centre. He stands on y = 0, y is up and his face looks along +z.
// The rules this shape follows are in docs/poulpy-3d.md (Brand > Poulpy in 3D in the component workshop) and in docs/brand-decisions.md, decision 14.

export const SHAPE = {
  // A bubble: slightly wider than tall, tucked into the arms so they wrap around its base.
  head: { radius: [1.12, 1.04, 1.1], centreY: 1.04 },
  // Eight puffy arms lying on the floor. `ring` is how far their centres sit from the middle, `radius` is
  // half their width, height and length. Poulpy never lifts them (brand decision 8).
  arms: { count: 8, radius: [0.37, 0.3, 0.58], ring: 0.98 },
  // Height of the line where the top colour meets the other side's colour, measured from the floor.
  // Everything under it is the lining, on the arms and on the head alike, so the underside is one colour.
  liningLine: 0.192,
  // The face is the 2D drawing's, pressed onto the head: `y` is where the drawing's baseline sits,
  // `scale` its size, `spread` how far apart the features sit and `eyes` an extra size for the eyes.
  face: { y: 0.57, scale: 1.1, spread: 1.1, eyes: 1.25 },
  // Where the drawing's baseline sits for whatever lies on his head by its outline (a plaster, a mark): the
  // height at which his arms hide his head.
  propLine: 0.45,
}

// Faces, written as in the 2D drawings (design/build-poulpy.mjs) and in their coordinates, so the two can be
// read side by side: x grows to the viewer's right, y grows downwards, his head spans x 37 to 163 and sits
// on y 130. Each feature is a small list:
//   ['eye', x, y, rx, ry, dot]       a round eye with its white dot, `dot` to the side of the eye's middle (-3 if left out)
//   ['half', x, y, rx, ry]           the lower half of an eye, for a heavy lid (`ry` is `rx` if left out)
//   ['lid', x1, y1, x2, y2, width]   a straight line that belongs to an eye, and grows with it
//   ['line', x1, y1, x2, y2, width]  a straight line: a brow, a flat mouth
//   ['curve', x1, y1, cx, cy, x2, y2, width]  a curved line: start, pull, end
//   ['path', d, width, colour]       any line, as an SVG path (M L Q T C, either case); ink if no colour
//   ['shape', d, colour]             a closed SVG path, filled; ink if no colour
//   ['oval', x, y, rx, ry, colour, turn]      a filled oval, turned by `turn` degrees; ink if no colour
//   ['teeth', x, y, width, height, corner, gaps]  clenched teeth, with `gaps` lines between them
//   ['cheek', x, y, rx, ry, colour, strength]     a blushing cheek, the colour mixed into his own
// The last entry of a feature may be the name of its part; left and right are Poulpy's own.
const blush = (y, colour, strength, rx = 8, ry = 4.5) => [['cheek', 64, y, rx, ry, colour, strength], ['cheek', 136, y, rx, ry, colour, strength]]
const BLUSH = blush(112, '#F49A6B', 0.75), EMBER_BLUSH = blush(114, '#8E2414', 0.4)
export const FACES = {
  happy: [['eye', 80, 96, 9, 10.5], ['eye', 120, 96, 9, 10.5], ['curve', 90, 112, 100, 123, 110, 112, 4.5, 'mouth'], ...BLUSH],
  calm: [['curve', 71, 97, 80, 90, 89, 97, 4.5, 'eye_right'], ['curve', 111, 97, 120, 90, 129, 97, 4.5, 'eye_left'], ['curve', 92, 113, 100, 119, 108, 113, 4, 'mouth'], ...BLUSH],
  cocky: [
    ['half', 80, 97, 9], ['lid', 68, 95, 92, 93, 4, 'lid_right'], ['eye', 120, 96, 9, 10.5],
    ['curve', 109, 79, 120, 70, 132, 77, 4.5, 'brow_left'], ['curve', 88, 114, 102, 123, 115, 108, 4.5, 'mouth'], ...BLUSH,
  ],
  proud: [['path', 'M70 99 Q80 86 90 99', 4.5, 'eye_right'], ['path', 'M110 99 Q120 86 130 99', 4.5, 'eye_left'], ['shape', 'M87 109 Q100 132 113 109 Z', 'mouth'], ...BLUSH],
  tired: [
    ['half', 80, 97, 9], ['half', 120, 97, 9], ['lid', 69, 98, 91, 95, 4, 'lid_right'], ['lid', 109, 95, 131, 98, 4, 'lid_left'],
    ['path', 'M73 110 Q80 114 87 110 M113 110 Q120 114 127 110', 2.5, '#4B6274', 'eye_bags'],
    ['oval', 100, 121, 8, 6.5, 'mouth'], ['oval', 100, 125, 5, 2.6, '#F07A7A', 'tongue'],
  ],
  hurt: [
    ['eye', 80, 97, 9, 10.5], ['eye', 120, 97, 9, 10.5], ['oval', 84, 101, 2, 2, '#FFFFFF', 'eye_right_tear'], ['oval', 124, 101, 2, 2, '#FFFFFF', 'eye_left_tear'],
    ['path', 'M68 82 Q78 76 88 80', 4, 'brow_right'], ['path', 'M132 82 Q122 76 112 80', 4, 'brow_left'], ['path', 'M91 117 q3 -3 6 0 t6 0 t6 0', 3.5, 'mouth'], ...BLUSH,
  ],
  straining: [['path', 'M70 90 L87 96 L70 102', 4.5, 'eye_right'], ['path', 'M130 90 L113 96 L130 102', 4.5, 'eye_left'], ['teeth', 87, 110, 26, 12, 5, 0], ...BLUSH],
  pouting: [
    ['eye', 84, 94, 8.5, 9.5, -5], ['eye', 124, 94, 8.5, 9.5, -5], ['line', 68, 79, 92, 85, 5, 'brow_right'], ['line', 134, 76, 110, 85, 5, 'brow_left'],
    ['curve', 90, 110, 98, 104, 106, 110, 4.5, 'mouth'], ...EMBER_BLUSH,
  ],
  glance: [
    ['half', 97.5, 99, 8.5], ['half', 130.5, 98, 6.5, 7.5], ['lid', 87, 98, 108, 96, 3.5, 'lid_right'], ['lid', 122, 96, 139, 97, 3.5, 'lid_left'],
    ['curve', 108, 117, 115, 120, 122, 117, 4, 'mouth'], ['cheek', 86, 112, 7, 4, '#F49A6B', 0.7], ['cheek', 144, 111, 5, 3.5, '#F49A6B', 0.7],
  ],
  asleep: [['path', 'M71 96 Q80 104 89 96', 4.5, 'eye_right'], ['path', 'M111 96 Q120 104 129 96', 4.5, 'eye_left'], ['oval', 100, 117, 4, 4, 'mouth'], ...BLUSH],
  shy: [
    ['path', 'M70 100 Q80 92 90 100', 4.5, 'eye_right'], ['path', 'M110 100 Q120 92 130 100', 4.5, 'eye_left'], ['curve', 93, 116, 100, 121, 107, 116, 4, 'mouth'],
    ...blush(112, '#F49A6B', 0.9, 12, 6), ['path', 'M60 110 l4 4 M66 109 l4 4 M128 109 l4 4 M134 110 l4 4', 2, '#C96A3E', 'blush_lines'],
  ],
  thinking: [
    ['eye', 80, 97, 8, 9.5, 0], ['eye', 120, 95, 9.5, 11, 0], ['line', 66, 84, 90, 86, 4, 'brow_right'], ['path', 'M110 78 Q120 70 132 76', 4, 'brow_left'],
    ['path', 'M90 116 q5 -4 10 0 t10 0', 4, 'mouth'], ...BLUSH,
  ],
  annoyed: [
    ['half', 80, 97, 9], ['half', 120, 97, 9], ['lid', 69, 96, 91, 96, 4, 'lid_right'], ['lid', 109, 96, 131, 96, 4, 'lid_left'],
    ['line', 110, 82, 130, 78, 4, 'brow_left'], ['line', 91, 116, 109, 116, 4.5, 'mouth'], ...EMBER_BLUSH,
  ],
  grumpy: [
    ['eye', 80, 98, 8.5, 9], ['eye', 120, 98, 8.5, 9], ['line', 66, 80, 92, 89, 5, 'brow_right'], ['line', 134, 80, 108, 89, 5, 'brow_left'],
    ['curve', 89, 121, 100, 110, 111, 121, 4.5, 'mouth'], ...EMBER_BLUSH,
  ],
  fuming: [
    ['eye', 81, 100, 7, 7.5], ['eye', 119, 100, 7, 7.5], ['line', 64, 82, 93, 93, 5.5, 'brow_right'], ['line', 136, 82, 107, 93, 5.5, 'brow_left'],
    ['teeth', 86, 111, 28, 13, 5, 2], ...EMBER_BLUSH,
  ],
}

// His six moods (docs/brand.md): the side of the plush, the face he makes and the `mood` token that tints him.
export const MOODS = {
  proud: { side: 'blue', face: 'cocky' },
  happy: { side: 'blue', face: 'happy' },
  recovering: { side: 'blue', face: 'calm' },
  annoyed: { side: 'ember', face: 'annoyed' },
  grumpy: { side: 'ember', face: 'grumpy' },
  fuming: { side: 'ember', face: 'fuming' },
}

// How he is lit and shaded, shared by every renderer so he looks the same everywhere.
export const LOOK = {
  // Directions as seen by the viewer (x right, y up, z towards the viewer): the light belongs to the
  // room, never to Poulpy, so shade and gleam slide over him when he turns.
  light: [-0.45, 0.8, 0.55],
  gleam: { towards: [-0.38, 0.55, 0.74], size: [0.25, 0.13], tilt: 0.35, white: 0.15 },
  // Flat cartoon shading: one shadow band at this share of the lit colour, no gloss, no reflections.
  shade: 0.76,
  // The optional outline: how far it sits off the surface and how much ink is mixed into the colour.
  outline: { width: 0.035, ink: 0.62 },
}

// --- small maths -----------------------------------------------------------------------------------

const TAU = Math.PI * 2
const norm = ([x, y, z]) => { const l = Math.hypot(x, y, z) || 1; return [x / l, y / l, z / l] }
const cross = (a, b) => [a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0]]
const add = (a, b, k = 1) => [a[0] + b[0] * k, a[1] + b[1] * k, a[2] + b[2] * k]

/** A colour as linear RGB (what glTF and 3D renderers mix in), from a `#RRGGBB` token. */
export function linear(hex) {
  return [1, 3, 5].map((i) => {
    const c = parseInt(hex.slice(i, i + 2), 16) / 255
    return c <= 0.04045 ? c / 12.92 : ((c + 0.055) / 1.055) ** 2.4
  })
}
const mix = (a, b, t) => a.map((v, i) => v + (b[i] - v) * t)

// The rotation that turns "straight ahead" (+z) onto the direction n, as a function on vectors.
export function facing(n) {
  const v = [-n[1], n[0], 0], k = 1 / (1 + n[2])
  return ([x, y, z]) => {
    const w = cross(v, [x, y, z]), ww = cross(v, w)
    return [x + w[0] + ww[0] * k, y + w[1] + ww[1] * k, z + w[2] + ww[2] * k]
  }
}

// A slice of an egg shape between two heights (theta 0 is the top, PI the bottom), already placed:
// `turn` rotates each point and normal, `at` moves it.
export function egg(radius, { from = 0, to = Math.PI, around = 32, down = 16, turn = (p) => p, at = [0, 0, 0] } = {}) {
  const positions = [], normals = [], indices = []
  for (let i = 0; i <= down; i++) {
    const th = from + ((to - from) * i) / down
    for (let j = 0; j <= around; j++) {
      const ph = (j / around) * TAU
      const u = [Math.sin(th) * Math.sin(ph), Math.cos(th), Math.sin(th) * Math.cos(ph)]
      positions.push(...add(at, turn([u[0] * radius[0], u[1] * radius[1], u[2] * radius[2]])))
      normals.push(...turn(norm([u[0] / radius[0], u[1] / radius[1], u[2] / radius[2]])))
    }
  }
  for (let i = 0; i < down; i++) for (let j = 0; j < around; j++) {
    const a = i * (around + 1) + j, b = a + 1, c = a + around + 1, d = c + 1
    indices.push(a, c, b, b, c, d)
  }
  return { positions, normals, indices }
}

export const join = (...shapes) => {
  const out = { positions: [], normals: [], indices: [] }
  for (const s of shapes) {
    const base = out.positions.length / 3
    out.positions.push(...s.positions); out.normals.push(...s.normals); out.indices.push(...s.indices.map((i) => i + base))
  }
  return out
}

/**
 * The points of an SVG path, one list per stroke of the pen. Reads M, L, Q, T, C and Z, in either case;
 * a list that Z closed has `closed` set. Curves become short straight steps.
 */
export function pathPoints(d) {
  const tokens = d.match(/[a-zA-Z]|-?\d*\.?\d+/g), out = []
  let i = 0, cmd = '', cur = [], x = 0, y = 0, qx = 0, qy = 0
  const num = () => +tokens[i++]
  const bend = (c, e, steps = 12) => {
    for (let s = 1; s <= steps; s++) { const t = s / steps, u = 1 - t; cur.push([u * u * x + 2 * u * t * c[0] + t * t * e[0], u * u * y + 2 * u * t * c[1] + t * t * e[1]]) }
    ;[qx, qy, x, y] = [c[0], c[1], e[0], e[1]]
  }
  while (i < tokens.length) {
    if (/[a-zA-Z]/.test(tokens[i])) cmd = tokens[i++]
    const rel = cmd === cmd.toLowerCase(), ox = rel ? x : 0, oy = rel ? y : 0, kind = cmd.toUpperCase()
    if (kind === 'M') { x = ox + num(); y = oy + num(); cur = [[x, y]]; out.push(cur); cmd = rel ? 'l' : 'L' }
    else if (kind === 'L') { x = ox + num(); y = oy + num(); cur.push([x, y]) }
    else if (kind === 'Q') bend([ox + num(), oy + num()], [ox + num(), oy + num()])
    else if (kind === 'T') bend([2 * x - qx, 2 * y - qy], [ox + num(), oy + num()])
    else if (kind === 'C') {
      const a = [ox + num(), oy + num()], b = [ox + num(), oy + num()], e = [ox + num(), oy + num()]
      for (let s = 1; s <= 16; s++) { const t = s / 16, u = 1 - t; cur.push([0, 1].map((k) => u * u * u * [x, y][k] + 3 * u * u * t * a[k] + 3 * u * t * t * b[k] + t * t * t * e[k])) }
      ;[x, y] = e
    } else if (kind === 'Z') { cur.closed = true; [x, y] = cur[0] }
    else throw new Error(`pathPoints cannot read "${cmd}" in "${d}"`)
    if (kind !== 'Q' && kind !== 'T') { qx = x; qy = y }
  }
  return out
}

/** A round tube through points in space, with round ends. `radius` is one number or one per point. */
export function tube(points, radius, sides = 10) {
  const r = (i) => (Array.isArray(radius) ? radius[i] : radius), positions = [], normals = [], indices = []
  let up = null
  points.forEach((p, i) => {
    const a = points[Math.max(i - 1, 0)], b = points[Math.min(i + 1, points.length - 1)], t = norm([b[0] - a[0], b[1] - a[1], b[2] - a[2]])
    up ??= Math.abs(t[1]) < 0.9 ? [0, 1, 0] : [1, 0, 0]
    const bn = norm(cross(t, up))
    up = cross(bn, t)
    for (let j = 0; j < sides; j++) {
      const ang = (j / sides) * TAU, dir = add([0, 0, 0].map((_, k) => bn[k] * Math.cos(ang)), up, Math.sin(ang))
      positions.push(...add(p, dir, r(i))); normals.push(...dir)
    }
  })
  for (let i = 0; i < points.length - 1; i++) for (let j = 0; j < sides; j++) {
    const a = i * sides + j, b = i * sides + ((j + 1) % sides), c = a + sides, d = b + sides
    indices.push(a, c, b, b, c, d)
  }
  const cap = (i) => egg([r(i), r(i), r(i)], { around: 10, down: 6, at: points[i] })
  return join({ positions, normals, indices }, cap(0), cap(points.length - 1))
}

// --- the head's surface ----------------------------------------------------------------------------

const headCentre = () => [0, SHAPE.head.centreY, 0]

// Where a point of the flat drawing lands on the front of the head, and which way the head faces there.
function press(x, y) {
  const [rx, ry, rz] = SHAPE.head.radius, dy = y - SHAPE.head.centreY
  const z = rz * Math.sqrt(Math.max(1 - (x / rx) ** 2 - (dy / ry) ** 2, 0))
  return { p: [x, y, z], n: norm([x / rx ** 2, dy / ry ** 2, z / rz ** 2]) }
}

// Where something travelling from `o` along `d` first meets the head, and which way the head faces there.
// What would miss the head lands on its edge.
function strike(o, d) {
  const R = SHAPE.head.radius, C = headCentre()
  const q = [0, 1, 2].map((k) => (o[k] - C[k]) / R[k]), e = [0, 1, 2].map((k) => d[k] / R[k])
  const A = e[0] ** 2 + e[1] ** 2 + e[2] ** 2, B = 2 * (q[0] * e[0] + q[1] * e[1] + q[2] * e[2]), K = q[0] ** 2 + q[1] ** 2 + q[2] ** 2 - 1
  const p = add(o, d, (-B - Math.sqrt(Math.max(B * B - 4 * A * K, 0))) / (2 * A))
  return { p, n: norm([0, 1, 2].map((k) => (p[k] - C[k]) / R[k] ** 2)) }
}

/**
 * The gleam: a soft patch lying on the head where the light hits it. `towards` is the direction from the
 * head to the highlight, in Poulpy's own space; a renderer calls this again whenever he or the camera turns.
 */
export function gleamPatch(towards = LOOK.gleam.towards) {
  const rings = 6, around = 28, d = norm(towards), C = headCentre()
  let t1 = [d[2], 0, -d[0]]
  t1 = Math.hypot(...t1) < 1e-6 ? [1, 0, 0] : norm(t1)
  const t2 = cross(d, t1), ca = Math.cos(LOOK.gleam.tilt), sa = Math.sin(LOOK.gleam.tilt)
  const positions = [], normals = [], indices = []
  for (let i = 0; i <= rings; i++) for (let j = 0; j < around; j++) {
    const t = (j / around) * TAU, u = LOOK.gleam.size[0] * (i / rings) * Math.cos(t), v = LOOK.gleam.size[1] * (i / rings) * Math.sin(t)
    // Start outside the head, in front of the highlight, and travel back along `d` until the head is hit.
    const { p, n } = strike(add(add(add(C, t1, u * ca - v * sa), t2, u * sa + v * ca), d, 5), d.map((v) => -v))
    positions.push(...add(p, n, 0.006)); normals.push(...n)
  }
  for (let i = 0; i < rings; i++) for (let j = 0; j < around; j++) {
    const a = i * around + j, b = i * around + ((j + 1) % around), c = a + around, e2 = b + around
    indices.push(a, c, b, b, c, e2)
  }
  return { positions, normals, indices }
}

// --- Poulpy ----------------------------------------------------------------------------------------

/**
 * Builds Poulpy for one side of the plush, or in one of his moods.
 * @param {{ side?: 'blue' | 'ember', mood?: keyof typeof MOODS, tone?: string, face?: keyof typeof FACES, features?: (string | number)[][], on?: { at: number[], marks: (string | number)[][] }, stretch?: number,
 *   colours: { blue: string, ember: string, ink: string } }} options
 *   `colours` are the `color.poulpy` tokens of design/tokens.json. With a `mood`, he takes its side and face,
 *   and `tone` (that mood's `mood` token) tints him; without one he is the plain side, happy or grumpy.
 *   `face` gives him another face of FACES, or `features` a face written out. `on` is marks laid on his head by its outline in the
 *   drawing and not by his face, a plaster or a vein: `at` is their middle in the drawing and `marks` their features. `stretch` makes his arms that many times taller, and lifts his head with them.
 * @returns {{ parts: { name: string, material: string, positions: number[], normals: number[], indices: number[] }[],
 *   materials: Record<string, { colour: number[], flat?: boolean, outline?: number[] }>, height: number, headLift: number }}
 *   Triangle meshes, already in place, and their colours as linear RGB. `flat` materials take no shading.
 *   His head and face are built where they sit at rest: whoever draws him lifts them by `headLift`.
 */
export function buildPoulpy3d({ side = 'blue', mood, tone, face: faceName, features, on, stretch = 1, colours }) {
  if (mood) side = MOODS[mood].side
  const ember = side === 'ember', F = features ?? FACES[faceName ?? (mood ? MOODS[mood].face : ember ? 'grumpy' : 'happy')], { head, arms, face } = SHAPE
  const body = linear(tone ?? (ember ? colours.ember : colours.blue)), lining = linear(ember ? colours.blue : colours.ember), ink = linear(colours.ink)
  const cheek = F.find((feature) => feature[0] === 'cheek')
  const outline = (c) => mix(c, ink, LOOK.outline.ink)
  const materials = {
    body: { colour: body, outline: outline(body) },
    lining: { colour: lining, outline: outline(lining) },
    ink: { colour: ink, flat: true },
    shine: { colour: [1, 1, 1], flat: true },
    ...(cheek && { cheek: { colour: mix(body, linear(cheek[5]), cheek[6]) } }),
    gleam: { colour: mix(body, [1, 1, 1], LOOK.gleam.white) },
  }
  // A plain colour of its own, for a feature that is neither ink nor white.
  const paint = (colour) => {
    if (!colour) return 'ink'
    materials[`paint_${colour.slice(1).toLowerCase()}`] ??= { colour: linear(colour), flat: true }
    return `paint_${colour.slice(1).toLowerCase()}`
  }
  const parts = []
  const part = (name, material, shape) => parts.push({ name, material, ...shape })

  // Head and arms are each one smooth shape, sliced at the lining line and coloured in two parts.
  const headCut = Math.acos((SHAPE.liningLine - head.centreY) / head.radius[1])
  part('head', 'body', egg(head.radius, { to: headCut, around: 48, down: 26, at: headCentre() }))
  part('head_lining', 'lining', egg(head.radius, { from: headCut, around: 48, down: 6, at: headCentre() }))
  const armRadius = [arms.radius[0], arms.radius[1] * stretch, arms.radius[2]]
  const armY = armRadius[1], armCut = Math.acos((SHAPE.liningLine - armY) / armRadius[1])
  for (let k = 0; k < arms.count; k++) {
    const a = (k * TAU) / arms.count, c = Math.cos(a), s = Math.sin(a)
    const placed = { around: 28, turn: ([x, y, z]) => [x * c + z * s, y, -x * s + z * c], at: [s * arms.ring, armY, c * arms.ring] }
    part(`arm_${k + 1}`, 'body', egg(armRadius, { ...placed, to: armCut, down: 14 }))
    part(`arm_${k + 1}_lining`, 'lining', egg(armRadius, { ...placed, from: armCut, down: 5 }))
  }

  // Every feature is in the flat drawing's coordinates, then pressed onto the head.
  // One unit of the model is the drawing's head radius, 63, and the drawing's middle is x 100, its baseline y 130.
  const U = 63
  // Left and right are Poulpy's own: his left is the viewer's right.
  const which = (x) => (x > 100 ? 'left' : 'right')
  // `lay` says where a point of the drawing lands and which way the head faces there, and `size` how much bigger things are drawn.
  const draw = (features, lay, size) => {
    const put = (x, y) => lay((x - 100) / U, (130 - y) / U)
    const sticker = (x, y, rx, ry, depth, detail, turnBy = 0) => {
      const { p, n } = put(x, y), onto = facing(n), ca = Math.cos(turnBy), sa = Math.sin(turnBy)
      const turn = ([px, py, pz]) => onto([px * ca - py * sa, px * sa + py * ca, pz]), centre = add(p, n, -depth * 0.25)
      return { shape: egg([(rx / U) * size, (ry / U) * size, depth], { ...detail, turn, at: centre }), turn, at: centre }
    }
    // A round line following the head, with rounded ends.
    const line = (points, w) => {
      const r = (w / 2 / U) * size
      const on = points.map(([x, y]) => { const { p, n } = put(x, y); return { p: add(p, n, r * 0.25), n } })
      const sides = 10, positions = [], normals = [], indices = []
      on.forEach(({ p, n }, i) => {
        const a = on[Math.max(i - 1, 0)].p, b = on[Math.min(i + 1, on.length - 1)].p
        const t = norm([b[0] - a[0], b[1] - a[1], b[2] - a[2]]), bn = norm(cross(t, n)), up = cross(bn, t)
        for (let j = 0; j < sides; j++) {
          const ang = (j / sides) * TAU, dir = add([0, 0, 0].map((_, k) => bn[k] * Math.cos(ang)), up, Math.sin(ang))
          positions.push(...add(p, dir, r)); normals.push(...dir)
        }
      })
      for (let i = 0; i < on.length - 1; i++) for (let j = 0; j < sides; j++) {
        const a = i * sides + j, b = i * sides + ((j + 1) % sides), c = a + sides, d = b + sides
        indices.push(a, c, b, b, c, d)
      }
      const cap = (centre) => egg([r, r, r], { around: 10, down: 6, at: centre })
      return join({ positions, normals, indices }, cap(on[0].p), cap(on[on.length - 1].p))
    }
    const straight = (x1, y1, x2, y2, n = 10) => Array.from({ length: n + 1 }, (_, i) => [x1 + ((x2 - x1) * i) / n, y1 + ((y2 - y1) * i) / n])
    // Straight stretches are cut into short steps too, so they follow the head.
    const steps = (points) => points.slice(1).flatMap((q, i) => straight(...points[i], ...q, Math.max(1, Math.ceil(Math.hypot(q[0] - points[i][0], q[1] - points[i][1]) / 4))).slice(i ? 1 : 0))
    // A flat shape lying on the head, `lift` off it: rings of triangles from its middle to its outline, close
    // enough to follow the head however big the shape is.
    const patch = (outlinePoints, lift) => {
      let around = steps([...outlinePoints, outlinePoints[0]]).slice(0, -1)
      // Triangles face the viewer when the outline runs clockwise in the drawing, where y grows downwards.
      if (around.reduce((sum, [x, y], i) => { const [nx, ny] = around[(i + 1) % around.length]; return sum + x * ny - nx * y }, 0) > 0) around = around.reverse()
      const mid = [0, 1].map((k) => around.reduce((sum, q) => sum + q[k], 0) / around.length), rings = 4, n = around.length
      const positions = [], normals = [], indices = []
      const lie = (x, y) => { const on = put(x, y); positions.push(...add(on.p, on.n, lift)); normals.push(...on.n) }
      lie(...mid)
      for (let ring = 1; ring <= rings; ring++) for (const [x, y] of around) lie(mid[0] + ((x - mid[0]) * ring) / rings, mid[1] + ((y - mid[1]) * ring) / rings)
      for (let i = 0; i < n; i++) {
        const next = (i + 1) % n
        indices.push(0, 1 + i, 1 + next)
        for (let ring = 1; ring < rings; ring++) {
          const a = 1 + (ring - 1) * n + i, b = 1 + (ring - 1) * n + next, c = a + n, d = b + n
          indices.push(a, c, b, b, c, d)
        }
      }
      return { positions, normals, indices }
    }
    const es = face.eyes
    // Where each eye sits, so a lid can grow with its eye.
    const eyes = features.filter((feature) => feature[0] === 'eye' || feature[0] === 'half').map((feature) => [feature[1], feature[2]])
    for (const feature of features) {
      const [kind, ...v] = feature, named = typeof v[v.length - 1] === 'string' && !/^#/.test(v[v.length - 1]) && kind !== 'path' && kind !== 'shape' ? v.pop() : null
      if (kind === 'eye') {
        const [x, y, rx, ry, dot = -3] = v, k = es * size
        const eye = sticker(x, y, rx * es, ry * es, 0.06, { around: 24, down: 12 })
        part(`eye_${which(x)}`, 'ink', eye.shape)
        // The white dot sits on the eye, up and to the viewer's left, as in the drawing.
        part(`eye_${which(x)}_shine`, 'shine', egg([0.044 * k, 0.044 * k, 0.02], { around: 12, down: 8, turn: eye.turn, at: add(eye.at, eye.turn([(dot / U) * k, 0.066 * k, 0.047])) }))
      } else if (kind === 'half') {
        const [x, y, rx, ry = rx] = v
        part(`eye_${which(x)}`, 'ink', sticker(x, y, rx * es, ry * es, 0.06, { from: Math.PI / 2, around: 24, down: 8 }).shape)
      } else if (kind === 'lid') {
        const [x1, y1, x2, y2, w] = v, mx = (x1 + x2) / 2
        const [ex, ey] = eyes.reduce((best, e) => (Math.abs(e[0] - mx) < Math.abs(best[0] - mx) ? e : best))
        part(named, 'ink', line(straight(ex + (x1 - ex) * es, ey + (y1 - ey) * es, ex + (x2 - ex) * es, ey + (y2 - ey) * es), w))
      } else if (kind === 'line') {
        const [x1, y1, x2, y2, w] = v
        part(named, 'ink', line(straight(x1, y1, x2, y2), w))
      } else if (kind === 'curve') {
        const [x1, y1, cx, cy, x2, y2, w] = v
        part(named, 'ink', line(pathPoints(`M${x1} ${y1} Q${cx} ${cy} ${x2} ${y2}`)[0], w))
      } else if (kind === 'path') {
        const [d, w, ...rest] = v, name = rest.pop(), colour = rest[0]
        part(name, paint(colour), join(...pathPoints(d).map((points) => line(steps(points.closed ? [...points, points[0]] : points), w))))
      } else if (kind === 'shape') {
        const [d, ...rest] = v, name = rest.pop(), colour = rest[0]
        part(name, paint(colour), join(...pathPoints(d).map((points) => patch(points, 0.012))))
      } else if (kind === 'oval') {
        const [x, y, rx, ry, colour, turnBy = 0] = v
        part(named, paint(colour), sticker(x, y, rx, ry, 0.03, { around: 16, down: 8 }, (-turnBy * Math.PI) / 180).shape)
      } else if (kind === 'teeth') {
        const [x, y, w, h, r, gaps] = v
        // A rounded box, corner by corner.
        const corners = [[x + w - r, y + r, -90], [x + w - r, y + h - r, 0], [x + r, y + h - r, 90], [x + r, y + r, 180]]
        const box = corners.flatMap(([cx, cy, from]) => Array.from({ length: 5 }, (_, i) => {
          const ang = ((from + (i * 90) / 4) * Math.PI) / 180
          return [cx + r * Math.cos(ang), cy + r * Math.sin(ang)]
        }))
        part('teeth', 'shine', patch(box, 0.012))
        part('mouth', 'ink', join(
          line([...box, box[0]], 3.5),
          line(straight(x, y + h / 2, x + w, y + h / 2), 2.5),
          ...Array.from({ length: gaps }, (_, i) => line(straight(x + (w * (i + 1)) / (gaps + 1), y, x + (w * (i + 1)) / (gaps + 1), y + h, 4), 2)),
        ))
      } else if (kind === 'cheek') {
        const [x, y, rx, ry] = v
        part(`cheek_${which(x)}`, 'cheek', sticker(x, y, rx, ry, 0.03, { around: 16, down: 8 }).shape)
      } else throw new Error(`No way to draw a "${kind}"`)
    }
  }
  // His face is the drawing's, a little bigger and wider apart. A mark lies where the drawing's outline puts
  // it: the drawing's head is 63 wide from its middle and 100 tall above the line where the arms hide it.
  draw(F, (x, y) => press(x * face.scale * face.spread, face.y + y * face.scale), face.scale)
  if (on) {
    // A mark is laid flat against his head where its middle touches, then wrapped onto it, so it keeps its shape
    // and never leaves his head however near the edge it is.
    const outline = (x, y) => [x * head.radius[0], SHAPE.propLine + y * ((head.centreY + head.radius[1] - SHAPE.propLine) * U) / 100]
    const middle = outline((on.at[0] - 100) / U, (130 - on.at[1]) / U), touch = press(...middle)
    const across = norm(cross([0, 1, 0], touch.n)), up = cross(touch.n, across)
    draw(on.marks, (x, y) => { const [px, py] = outline(x, y); return strike(add(add(add(touch.p, across, px - middle[0]), up, py - middle[1]), touch.n, 2), touch.n.map((v) => -v)) }, 1.05)
  }
  part('gleam', 'gleam', gleamPatch())

  return { parts, materials, height: head.centreY + head.radius[1] + 2 * arms.radius[1] * (stretch - 1), headLift: 2 * arms.radius[1] * (stretch - 1) }
}
