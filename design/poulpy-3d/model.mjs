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
}

// Faces, written as in the 2D drawings (design/build-poulpy.mjs) and in their coordinates, so the two can be
// read side by side: x grows to the viewer's right, y grows downwards, his head spans x 37 to 163 and sits
// on y 130. Each feature is a small list:
//   ['eye', x, y, rx, ry]            a round eye with its white dot
//   ['half', x, y, r]                the lower half of an eye, for a heavy lid
//   ['lid', x1, y1, x2, y2, width]   a straight line that belongs to an eye, and grows with it
//   ['line', x1, y1, x2, y2, width]  a straight line: a brow, a flat mouth
//   ['curve', x1, y1, cx, cy, x2, y2, width]  a curved line: start, pull, end
//   ['teeth', x, y, width, height, corner]    clenched teeth
//   ['blush', y, colour, strength]   the two cheeks
// The last entry of a feature may be the name of its part; left and right are Poulpy's own.
const BLUSH = ['blush', 112, '#F49A6B', 0.75], EMBER_BLUSH = ['blush', 114, '#8E2414', 0.4]
export const FACES = {
  happy: [['eye', 80, 96, 9, 10.5], ['eye', 120, 96, 9, 10.5], ['curve', 90, 112, 100, 123, 110, 112, 4.5, 'mouth'], BLUSH],
  calm: [['curve', 71, 97, 80, 90, 89, 97, 4.5, 'eye_right'], ['curve', 111, 97, 120, 90, 129, 97, 4.5, 'eye_left'], ['curve', 92, 113, 100, 119, 108, 113, 4, 'mouth'], BLUSH],
  cocky: [
    ['half', 80, 97, 9], ['lid', 68, 95, 92, 93, 4, 'lid_right'], ['eye', 120, 96, 9, 10.5],
    ['curve', 109, 79, 120, 70, 132, 77, 4.5, 'brow_left'], ['curve', 88, 114, 102, 123, 115, 108, 4.5, 'mouth'], BLUSH,
  ],
  annoyed: [
    ['half', 80, 97, 9], ['half', 120, 97, 9], ['lid', 69, 96, 91, 96, 4, 'lid_right'], ['lid', 109, 96, 131, 96, 4, 'lid_left'],
    ['line', 110, 82, 130, 78, 4, 'brow_left'], ['line', 91, 116, 109, 116, 4.5, 'mouth'], EMBER_BLUSH,
  ],
  grumpy: [
    ['eye', 80, 98, 8.5, 9], ['eye', 120, 98, 8.5, 9], ['line', 66, 80, 92, 89, 5, 'brow_right'], ['line', 134, 80, 108, 89, 5, 'brow_left'],
    ['curve', 89, 121, 100, 110, 111, 121, 4.5, 'mouth'], EMBER_BLUSH,
  ],
  fuming: [
    ['eye', 81, 100, 7, 7.5], ['eye', 119, 100, 7, 7.5], ['line', 64, 82, 93, 93, 5.5, 'brow_right'], ['line', 136, 82, 107, 93, 5.5, 'brow_left'],
    ['teeth', 86, 111, 28, 13, 5], EMBER_BLUSH,
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
function facing(n) {
  const v = [-n[1], n[0], 0], k = 1 / (1 + n[2])
  return ([x, y, z]) => {
    const w = cross(v, [x, y, z]), ww = cross(v, w)
    return [x + w[0] + ww[0] * k, y + w[1] + ww[1] * k, z + w[2] + ww[2] * k]
  }
}

// A slice of an egg shape between two heights (theta 0 is the top, PI the bottom), already placed:
// `turn` rotates each point and normal, `at` moves it.
function egg(radius, { from = 0, to = Math.PI, around = 32, down = 16, turn = (p) => p, at = [0, 0, 0] } = {}) {
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

const join = (...shapes) => {
  const out = { positions: [], normals: [], indices: [] }
  for (const s of shapes) {
    const base = out.positions.length / 3
    out.positions.push(...s.positions); out.normals.push(...s.normals); out.indices.push(...s.indices.map((i) => i + base))
  }
  return out
}

// --- the head's surface ----------------------------------------------------------------------------

const headCentre = () => [0, SHAPE.head.centreY, 0]

// Where a point of the flat drawing lands on the front of the head, and which way the head faces there.
function press(x, y) {
  const [rx, ry, rz] = SHAPE.head.radius, dy = y - SHAPE.head.centreY
  const z = rz * Math.sqrt(Math.max(1 - (x / rx) ** 2 - (dy / ry) ** 2, 0))
  return { p: [x, y, z], n: norm([x / rx ** 2, dy / ry ** 2, z / rz ** 2]) }
}

/**
 * The gleam: a soft patch lying on the head where the light hits it. `towards` is the direction from the
 * head to the highlight, in Poulpy's own space; a renderer calls this again whenever he or the camera turns.
 */
export function gleamPatch(towards = LOOK.gleam.towards) {
  const rings = 6, around = 28, d = norm(towards), R = SHAPE.head.radius, C = headCentre()
  let t1 = [d[2], 0, -d[0]]
  t1 = Math.hypot(...t1) < 1e-6 ? [1, 0, 0] : norm(t1)
  const t2 = cross(d, t1), ca = Math.cos(LOOK.gleam.tilt), sa = Math.sin(LOOK.gleam.tilt)
  const positions = [], normals = [], indices = []
  for (let i = 0; i <= rings; i++) for (let j = 0; j < around; j++) {
    const t = (j / around) * TAU, u = LOOK.gleam.size[0] * (i / rings) * Math.cos(t), v = LOOK.gleam.size[1] * (i / rings) * Math.sin(t)
    // Start outside the head, in front of the highlight, and travel back along `d` until the head is hit.
    const o = add(add(add(C, t1, u * ca - v * sa), t2, u * sa + v * ca), d, 5)
    const q = [0, 1, 2].map((k) => (o[k] - C[k]) / R[k]), e = [0, 1, 2].map((k) => -d[k] / R[k])
    const A = e[0] ** 2 + e[1] ** 2 + e[2] ** 2, B = 2 * (q[0] * e[0] + q[1] * e[1] + q[2] * e[2]), K = q[0] ** 2 + q[1] ** 2 + q[2] ** 2 - 1
    const hit = add(o, d, -(-B - Math.sqrt(Math.max(B * B - 4 * A * K, 0))) / (2 * A))
    const n = norm([0, 1, 2].map((k) => (hit[k] - C[k]) / R[k] ** 2))
    positions.push(...add(hit, n, 0.006)); normals.push(...n)
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
 * @param {{ side?: 'blue' | 'ember', mood?: keyof typeof MOODS, tone?: string, colours: { blue: string, ember: string, ink: string } }} options
 *   `colours` are the `color.poulpy` tokens of design/tokens.json. With a `mood`, he takes its side and face,
 *   and `tone` (that mood's `mood` token) tints him; without one he is the plain side, happy or grumpy.
 * @returns {{ parts: { name: string, material: string, positions: number[], normals: number[], indices: number[] }[],
 *   materials: Record<string, { colour: number[], flat?: boolean, outline?: number[] }>, height: number }}
 *   Triangle meshes, already in place, and their colours as linear RGB. `flat` materials take no shading.
 */
export function buildPoulpy3d({ side = 'blue', mood, tone, colours }) {
  if (mood) side = MOODS[mood].side
  const ember = side === 'ember', F = FACES[mood ? MOODS[mood].face : ember ? 'grumpy' : 'happy'], { head, arms, face } = SHAPE
  const body = linear(tone ?? (ember ? colours.ember : colours.blue)), lining = linear(ember ? colours.blue : colours.ember), ink = linear(colours.ink)
  const blush = F.find((feature) => feature[0] === 'blush')
  const outline = (c) => mix(c, ink, LOOK.outline.ink)
  const materials = {
    body: { colour: body, outline: outline(body) },
    lining: { colour: lining, outline: outline(lining) },
    ink: { colour: ink, flat: true },
    shine: { colour: [1, 1, 1], flat: true },
    cheek: { colour: mix(body, linear(blush[2]), blush[3]) },
    gleam: { colour: mix(body, [1, 1, 1], LOOK.gleam.white) },
  }
  const parts = []
  const part = (name, material, shape) => parts.push({ name, material, ...shape })

  // Head and arms are each one smooth shape, sliced at the lining line and coloured in two parts.
  const headCut = Math.acos((SHAPE.liningLine - head.centreY) / head.radius[1])
  part('head', 'body', egg(head.radius, { to: headCut, around: 48, down: 26, at: headCentre() }))
  part('head_lining', 'lining', egg(head.radius, { from: headCut, around: 48, down: 6, at: headCentre() }))
  const armY = arms.radius[1], armCut = Math.acos((SHAPE.liningLine - armY) / arms.radius[1])
  for (let k = 0; k < arms.count; k++) {
    const a = (k * TAU) / arms.count, c = Math.cos(a), s = Math.sin(a)
    const placed = { around: 28, turn: ([x, y, z]) => [x * c + z * s, y, -x * s + z * c], at: [s * arms.ring, armY, c * arms.ring] }
    part(`arm_${k + 1}`, 'body', egg(arms.radius, { ...placed, to: armCut, down: 14 }))
    part(`arm_${k + 1}_lining`, 'lining', egg(arms.radius, { ...placed, from: armCut, down: 5 }))
  }

  // The face: every feature is in the flat drawing's coordinates, then pressed onto the head.
  // One unit of the model is the drawing's head radius, 63, and the drawing's middle is x 100, its baseline y 130.
  const U = 63, dx = (x) => (x - 100) / U, dy = (y) => (130 - y) / U
  const fx = (x) => x * face.scale * face.spread, fy = (y) => face.y + y * face.scale
  const sticker = (x, y, rx, ry, depth, detail) => {
    const { p, n } = press(fx(x), fy(y)), turn = facing(n), at = add(p, n, -depth * 0.25)
    return { shape: egg([rx * face.scale, ry * face.scale, depth], { ...detail, turn, at }), turn, at }
  }
  // A round line following the head, with rounded ends.
  const line = (points, r) => {
    const on = points.map(([x, y]) => { const { p, n } = press(fx(x), fy(y)); return { p: add(p, n, r * 0.25), n } })
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
    const cap = (at) => egg([r, r, r], { around: 10, down: 6, at })
    return join({ positions, normals, indices }, cap(on[0].p), cap(on[on.length - 1].p))
  }
  // A flat shape lying on the head, `lift` off it: a fan of triangles from its middle to its outline.
  const patch = (outlinePoints, lift) => {
    // Triangles face the viewer when the outline runs anticlockwise.
    if (outlinePoints.reduce((sum, [x, y], i) => { const [nx, ny] = outlinePoints[(i + 1) % outlinePoints.length]; return sum + x * ny - nx * y }, 0) < 0) outlinePoints = [...outlinePoints].reverse()
    const mid = [0, 1].map((k) => outlinePoints.reduce((sum, q) => sum + q[k], 0) / outlinePoints.length)
    const positions = [], normals = [], indices = []
    for (const [x, y] of [mid, ...outlinePoints]) { const { p, n } = press(fx(x), fy(y)); positions.push(...add(p, n, lift)); normals.push(...n) }
    outlinePoints.forEach((_, i) => indices.push(0, 1 + i, 1 + ((i + 1) % outlinePoints.length)))
    return { positions, normals, indices }
  }
  const curve = ([a, c, b], n = 18) => Array.from({ length: n + 1 }, (_, i) => {
    const t = i / n, u = 1 - t
    return [u * u * a[0] + 2 * u * t * c[0] + t * t * b[0], u * u * a[1] + 2 * u * t * c[1] + t * t * b[1]]
  })
  const straight = ([a, b], n = 10) => Array.from({ length: n + 1 }, (_, i) => [a[0] + ((b[0] - a[0]) * i) / n, a[1] + ((b[1] - a[1]) * i) / n])
  const width = (w) => (w / 2 / U) * face.scale
  // Left and right are Poulpy's own: his left is the viewer's right.
  const which = (x) => (x > 100 ? 'left' : 'right')
  const es = face.eyes, k = es * face.scale
  // Where each eye sits, so a lid can grow with its eye.
  const eyes = F.filter((feature) => feature[0] === 'eye' || feature[0] === 'half').map((feature) => [feature[1], feature[2]])
  for (const feature of F) {
    const [kind, ...v] = feature, named = typeof v[v.length - 1] === 'string' ? v.pop() : null
    if (kind === 'eye') {
      const [x, y, rx, ry] = v
      const eye = sticker(dx(x), dy(y), (rx / U) * es, (ry / U) * es, 0.06, { around: 24, down: 12 })
      part(`eye_${which(x)}`, 'ink', eye.shape)
      // The white dot sits on the eye, up and to the viewer's left, as in the drawing.
      part(`eye_${which(x)}_shine`, 'shine', egg([0.044 * k, 0.044 * k, 0.02], { around: 12, down: 8, turn: eye.turn, at: add(eye.at, eye.turn([-0.048 * k, 0.066 * k, 0.047])) }))
    } else if (kind === 'half') {
      const [x, y, r] = v
      part(`eye_${which(x)}`, 'ink', sticker(dx(x), dy(y), (r / U) * es, (r / U) * es, 0.06, { from: Math.PI / 2, around: 24, down: 8 }).shape)
    } else if (kind === 'lid') {
      const [x1, y1, x2, y2, w] = v, mx = (x1 + x2) / 2
      const [ex, ey] = eyes.reduce((best, e) => (Math.abs(e[0] - mx) < Math.abs(best[0] - mx) ? e : best))
      const grown = (x, y) => [dx(ex + (x - ex) * es), dy(ey + (y - ey) * es)]
      part(named, 'ink', line(straight([grown(x1, y1), grown(x2, y2)]), width(w)))
    } else if (kind === 'line') {
      const [x1, y1, x2, y2, w] = v
      part(named, 'ink', line(straight([[dx(x1), dy(y1)], [dx(x2), dy(y2)]]), width(w)))
    } else if (kind === 'curve') {
      const [x1, y1, cx, cy, x2, y2, w] = v
      part(named, 'ink', line(curve([[dx(x1), dy(y1)], [dx(cx), dy(cy)], [dx(x2), dy(y2)]]), width(w)))
    } else if (kind === 'teeth') {
      const [x, y, w, h, r] = v
      // A rounded box, corner by corner.
      const corners = [[x + w - r, y + r, -90], [x + w - r, y + h - r, 0], [x + r, y + h - r, 90], [x + r, y + r, 180]]
      const box = corners.flatMap(([cx, cy, from]) => Array.from({ length: 5 }, (_, i) => {
        const ang = ((from + (i * 90) / 4) * Math.PI) / 180
        return [dx(cx + r * Math.cos(ang)), dy(cy + r * Math.sin(ang))]
      }))
      part('teeth', 'shine', patch(box, 0.012))
      part('mouth', 'ink', join(
        line([...box, box[0]], width(3.5)),
        line(straight([[dx(x), dy(y + h / 2)], [dx(x + w), dy(y + h / 2)]]), width(2.5)),
        ...[1, 2].map((i) => line(straight([[dx(x + (w * i) / 3), dy(y)], [dx(x + (w * i) / 3), dy(y + h)]], 4), width(2))),
      ))
    } else if (kind === 'blush') {
      for (const x of [136, 64]) part(`cheek_${which(x)}`, 'cheek', sticker(dx(x), dy(v[0]), 8 / U, 4.5 / U, 0.03, { around: 16, down: 8 }).shape)
    }
  }
  part('gleam', 'gleam', gleamPatch())

  return { parts, materials, height: head.centreY + head.radius[1] }
}
