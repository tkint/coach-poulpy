// Poulpy's twelve poses in 3D, with no dependency: the poses of the 2D drawings (design/build-poulpy.mjs),
// built on the shape and the faces of model.mjs. The docs site's "Poulpy in 3D" page draws them live.
//
// A pose is a mood's shade, a face, and what the drawing adds:
//   `props`   solid things he wears or that lie by him: a sweatband, a nightcap, a medal, a dumbbell
//   `on`      marks lying on his head, in the drawing's coordinates: plasters, a vein; `at` is their middle
//   `around`  the comic effects in front of him: sparks, lettering, bubbles
//   `behind`  the same, behind him
// The effects are flat, as in the drawing, and whoever draws him keeps them facing the viewer.
// What is on him is part of his face: sweat, a tear. He never lifts an arm in any of them (brand decision 8).
// To change a pose, edit POSES below and look at the page; the rules are in web/storybook/stories/brand/Poulpy3d.mdx.
import { SHAPE, FACES, MOODS, buildPoulpy3d, egg, facing, join, linear, pathPoints, tube } from './model.mjs'

// The drawing's floor is y 165 and its middle x 100; 64 of its units are 1 in the model, for what stands around him.
const CARD = 64
const GOLD = '#F0B53A', STEEL = '#56687A', RUST = '#7A2C12', CLOUD = '#B6C4D0', NIGHT = '#1574B0', WHITE = '#FFFFFF'

// A water drop and a four-point spark, as the drawings draw them.
const drop = (x, y, k = 1) => `M${x} ${y - 9 * k} C${x + 6 * k} ${y - 2 * k} ${x + 6 * k} ${y + 5 * k} ${x} ${y + 5 * k} C${x - 6 * k} ${y + 5 * k} ${x - 6 * k} ${y - 2 * k} ${x} ${y - 9 * k} Z`
const spark = (x, y, r) => `M${x} ${y - r} L${x + r * 0.3} ${y - r * 0.3} L${x + r} ${y} L${x + r * 0.3} ${y + r * 0.3} L${x} ${y + r} L${x - r * 0.3} ${y + r * 0.3} L${x - r} ${y} L${x - r * 0.3} ${y - r * 0.3} Z`
// A box with round corners, turned by `turn` degrees around the point (cx, cy), as a path.
function box(x, y, w, h, r, turn = 0, cx = x, cy = y) {
  const ca = Math.cos((turn * Math.PI) / 180), sa = Math.sin((turn * Math.PI) / 180)
  const corners = [[x + w - r, y + r, -90], [x + w - r, y + h - r, 0], [x + r, y + h - r, 90], [x + r, y + r, 180]]
  const points = corners.flatMap(([px, py, from]) => Array.from({ length: 5 }, (_, i) => {
    const ang = ((from + (i * 90) / 4) * Math.PI) / 180, qx = px + r * Math.cos(ang) - cx, qy = py + r * Math.sin(ang) - cy
    return [cx + qx * ca - qy * sa, cy + qx * sa + qy * ca]
  }))
  return 'M' + points.map(([px, py]) => `${+px.toFixed(2)} ${+py.toFixed(2)}`).join(' L') + ' Z'
}
// Drops are on him, so they are features of his face: they stay on his skin when he turns.
const drops = (...at) => at.flatMap(([x, y, k], i) => [['shape', drop(x, y, k), '#A8DDF7', `drop_${i + 1}`], ['path', drop(x, y, k), 1.5, '#3AA6E0', `drop_${i + 1}_edge`]])
const sparks = (...at) => at.map(([x, y, r]) => ['shape', spark(x, y, r), GOLD])
const puff = (x, y) => [[0, 0, 12], [13, -6, 10], [-12, -4, 9], [3, -15, 9]].map((p) => ['oval', x + p[0], y + p[1], p[2], p[2], '#E6EEF4', { stroke: CLOUD, width: 1.5 }])
const plaster = (turn) => [['shape', box(112, 50, 40, 13, 5, turn, 132, 56), '#F5D7B5', 'plaster'], ['path', box(112, 50, 40, 13, 5, turn, 132, 56), 1.5, '#D9B48C', 'plaster_edge']]

// The champion's confetti: its colours ('ember' is Poulpy's) and the size of a piece, in the drawing's units.
// The pose draws it flat behind him; when he cheers it flies (CHEER in motion.mjs).
export const GOLDEN = GOLD
export const CONFETTI = { colours: [GOLD, 'ember', '#5BBEF0'], size: [9, 5] }

// The cocky one's sparks, as the drawing has them: x, y and size. Still, they are flat; when he winks they are solid stars (STARS).
const SPARKS = [[6, 52, 10], [196, 40, 8], [176, 4, 5], [24, 10, 5]]

// Effects are lists like the faces' features, in the drawing's coordinates:
//   ['shape', d, colour, { stroke, width, opacity }]   a closed SVG path, filled, with an optional edge
//   ['oval', x, y, rx, ry, colour, { stroke, width, opacity }]
//   ['stroke', d, width, colour]                        a line, as an SVG path
//   ['text', x, y, size, colour, words, 'middle']       stroked lettering; 'middle' centres it on x
// A colour of 'ink' is Poulpy's ink.
export const POSES = {
  // The drawing's halo and "PR!" badge are left out in 3D: the owner dropped them when he asked for the wink.
  cocky: { mood: 'proud', face: 'cocky', around: sparks(...SPARKS) },
  straining: {
    mood: 'happy', face: [...FACES.straining, ...drops([137, 68, 0.9], [60, 74, 0.7])], props: ['dumbbell'],
    around: [['stroke', 'M12 70 l-12 -4 M10 86 l-14 0 M188 70 l12 -4 M190 86 l14 0', 3, STEEL]],
  },
  champion: {
    mood: 'proud', face: 'proud', props: ['medal'],
    behind: [[-8, 40, 0, 20], [20, 0, 1, -30], [180, 4, 2, 40], [204, 50, 0, -15], [150, -14, 1, 10], [46, -12, 2, 60], [100, -24, 0, 30]]
      .map(([x, y, colour, turn]) => ['shape', box(x, y, ...CONFETTI.size, 1.5, turn), CONFETTI.colours[colour]]),
  },
  standingTall: {
    mood: 'proud', face: 'proud', stretch: 1.4,
    around: [['stroke', 'M-4 70 l0 -16 M-10 60 l6 -6 6 6 M204 70 l0 -16 M198 60 l6 -6 6 6', 3, STEEL]],
  },
  exhausted: {
    mood: 'recovering', face: [...FACES.tired, ...drops([56, 96, 1.1], [145, 92, 1], [140, 116, 0.7], [62, 118, 0.8])], props: ['sweatband'],
  },
  sideGlance: { mood: 'recovering', face: 'glance', headTilt: 6, around: [['text', 160, 40, 22, STEEL, '…']] },
  sleeping: { mood: 'recovering', face: 'asleep', props: ['nightcap'], around: [['text', 160, 70, 24, NIGHT, 'z'], ['text', 178, 52, 17, NIGHT, 'z']] },
  patchedUp: {
    mood: 'recovering', on: { at: [132, 56], marks: [...plaster(35), ...plaster(-35)] },
    // The tear is part of his face, so it sits on his cheek.
    face: [...FACES.hurt, ['shape', drop(66, 112, 0.55), '#A8DDF7', 'tear'], ['path', drop(66, 112, 0.55), 1.5, '#3AA6E0', 'tear_edge']],
  },
  shy: { mood: 'happy', face: [...FACES.shy, ...drops([136, 76, 0.7])] },
  thinking: {
    mood: 'happy', face: 'thinking',
    around: [['oval', 160, 44, 4, 4, CLOUD], ['oval', 172, 28, 6, 6, CLOUD], ['oval', 196, -4, 24, 18, WHITE, { stroke: CLOUD, width: 2 }], ['text', 196, 6, 26, NIGHT, '?', 'middle']],
  },
  pouting: {
    mood: 'grumpy', face: 'pouting',
    around: [['stroke', 'M196 150 q6 -6 12 0 M200 140 q8 -8 16 0', 3, RUST], ['text', -16, 34, 22, RUST, 'hmph']],
  },
  fuming: {
    mood: 'fuming', face: 'fuming',
    on: { at: [146, 56], marks: [['path', 'M140 50 q6 6 0 12 M152 50 q-6 6 0 12 M140 50 q6 -6 12 0 M140 62 q6 6 12 0', 3.5, '#FFDCC7', 'vein']] },
    behind: [...puff(52, 6), ...puff(150, -2), ['stroke', 'M70 30 L64 18 M132 28 L140 14', 4, CLOUD]],
    around: [['stroke', 'M14 80 l-12 -4 M12 96 l-14 0 M186 80 l12 -4 M188 96 l14 0', 3, RUST]],
  },
}

// Rounded stroke lettering for the few words drawn in the poses, on a 1-unit em, baseline at 0.
// CLEANUP: the letters still drawn here, copied from GLYPHS in design/build-poulpy.mjs; keep one copy once the pictures are
// rendered from this model and the 2D generator goes (brand decision 16).
const GLYPHS = {
  h: { d: 'M0 -0.72 L0 0 M0 -0.3 Q0 -0.5 0.19 -0.5 Q0.38 -0.5 0.38 -0.3 L0.38 0', advance: 0.56 },
  m: { d: 'M0 -0.5 L0 0 M0 -0.32 Q0 -0.5 0.16 -0.5 Q0.3 -0.5 0.3 -0.32 L0.3 0 M0.3 -0.32 Q0.3 -0.5 0.46 -0.5 Q0.6 -0.5 0.6 -0.32 L0.6 0', advance: 0.78 },
  p: { d: 'M0 -0.5 L0 0.24 M0 -0.25 Q0 -0.5 0.19 -0.5 Q0.38 -0.5 0.38 -0.25 Q0.38 0 0.19 0 Q0 0 0 -0.25', advance: 0.56 },
  z: { d: 'M0 -0.5 L0.36 -0.5 L0 0 L0.36 0', advance: 0.5 },
  '?': { d: 'M0 -0.56 Q0 -0.76 0.2 -0.76 Q0.4 -0.76 0.4 -0.56 Q0.4 -0.42 0.2 -0.34 L0.2 -0.22 M0.2 -0.02 L0.2 0', advance: 0.44 },
  '…': { d: 'M0 -0.02 L0 0 M0.26 -0.02 L0.26 0 M0.52 -0.02 L0.52 0', advance: 0.64 },
}
// The strokes of some words, as lists of points.
function lettering(x, y, size, words, anchor) {
  // The last letter's advance includes the gap before a next letter, which centring should ignore.
  const width = [...words].reduce((w, ch) => w + GLYPHS[ch].advance, 0) - 0.16
  let at = x - (anchor === 'middle' ? (width * size) / 2 : 0)
  return [...words].flatMap((ch) => {
    const strokes = pathPoints(GLYPHS[ch].d).map((points) => points.map(([px, py]) => [at + px * size, y + py * size]))
    at += GLYPHS[ch].advance * size
    return strokes
  })
}

// --- the cocky wink --------------------------------------------------------------------------------

const between = (a, b, t) => a + (b - a) * t
/**
 * His face as the cocky one winks: at 0 both eyes are wide open, at 1 his right eye is shut, his left brow has come up
 * and his mouth is the cocky smirk. In between, the eye narrows to a line, then the line bends into the wink.
 * The numbers are the drawing's, as in FACES; `[open, winking]` pairs are blended. With `side` 'left' it is
 * his left eye that shuts, and his whole face is the same the other way around.
 */
export function winkFace(t, side = 'right') {
  const at = (...pairs) => pairs.map(([open, winking]) => between(open, winking, t))
  const shut = Math.min(t / 0.7, 1), bent = Math.max((t - 0.7) / 0.3, 0)
  const face = [
    shut < 1 ? ['eye', 80, between(96, 97, shut), between(9.5, 10, shut), between(11.5, 2.2, shut)] : ['curve', 69, 97 + bent, 80, 97 - 9 * bent, 91, 97 - bent, 4.5, 'eye_right'],
    ['eye', 120, 96, ...at([9.5, 9], [11.5, 10.5])],
    // His brow is not drawn while his eyes are wide open: it grows in as it goes up.
    ...(t > 0 ? [['curve', ...at([109, 109], [80, 79], [120, 120], [75, 70], [131, 132], [80, 77]), 4.5 * Math.min(t / 0.4, 1), 'brow_left']] : []),
    ['curve', ...at([91, 88], [113, 114], [100, 102], [119, 123], [109, 115], [113, 108]), 4.5, 'mouth'],
    ...FACES.cocky.filter(([kind]) => kind === 'cheek'),
  ]
  if (side === 'right') return face
  // The other way around: every x is swapped across his middle, and so are the names of his sides.
  const other = { eye_right: 'eye_left', brow_left: 'brow_right' }
  return face.map(([kind, ...v]) => [kind, ...v.map((value, i) => (typeof value === 'string' ? other[value] ?? value : (kind === 'curve' ? i < 6 && i % 2 === 0 : i === 0) ? 200 - value : value))])
}

/** The stars around the cocky one when he winks: where each is in the room, and its size from its middle to a tip. */
export const STARS = SPARKS.map(([x, y, r], i) => ({ at: [(x - 100) / CARD, (165 - y) / CARD, [0.5, 0.35, -0.1, 0.1][i]], size: (r / CARD) * 1.5 }))
/** A solid four-point star of size 1, cut like a gem so each face catches the light on its own. It faces +z. */
export function starShape() {
  const rim = Array.from({ length: 8 }, (_, i) => { const r = i % 2 ? 0.34 : 1, a = (i / 8) * Math.PI * 2; return [r * Math.sin(a), r * Math.cos(a), 0] })
  const positions = [], normals = [], indices = []
  rim.forEach((p, i) => {
    const q = rim[(i + 1) % 8]
    for (const [peak, a, b] of [[[0, 0, 0.3], q, p], [[0, 0, -0.3], p, q]]) {
      const u = a.map((v, k) => v - peak[k]), w = b.map((v, k) => v - peak[k])
      const n = [u[1] * w[2] - u[2] * w[1], u[2] * w[0] - u[0] * w[2], u[0] * w[1] - u[1] * w[0]], l = Math.hypot(...n)
      indices.push(...[0, 1, 2].map((k) => positions.length / 3 + k))
      positions.push(...peak, ...a, ...b)
      for (let k = 0; k < 3; k++) normals.push(...n.map((v) => v / l))
    }
  })
  return { positions, normals, indices }
}

// --- flat effects ----------------------------------------------------------------------------------

// An effect of the drawing as flat shapes facing +z.
const onCard = ([x, y]) => [(x - 100) / CARD, (165 - y) / CARD]
// A filled outline, as a fan of triangles from its middle: `flat` takes points already on the card, `fan` the drawing's.
function flat(points, z) {
  let around = points
  if (around.reduce((sum, [x, y], i) => { const [nx, ny] = around[(i + 1) % around.length]; return sum + x * ny - nx * y }, 0) < 0) around = around.reverse()
  const mid = [0, 1].map((k) => around.reduce((sum, q) => sum + q[k], 0) / around.length)
  const all = [mid, ...around]
  return { positions: all.flatMap(([x, y]) => [x, y, z]), normals: all.flatMap(() => [0, 0, 1]), indices: around.flatMap((_, i) => [0, 1 + i, 1 + ((i + 1) % around.length)]) }
}
const fan = (points, z) => flat(points.map(onCard), z)
const round = (x, y, rx, ry = rx, n = 28) => Array.from({ length: n }, (_, i) => [x + rx * Math.cos((i / n) * Math.PI * 2), y + ry * Math.sin((i / n) * Math.PI * 2)])
// A line with round ends and corners: a strip along each stretch and a dot at each point.
function ribbon(points, width, z) {
  const strips = points.slice(1).map((b, i) => {
    const a = points[i], l = Math.hypot(b[0] - a[0], b[1] - a[1]) || 1, nx = (-(b[1] - a[1]) / l) * (width / 2), ny = ((b[0] - a[0]) / l) * (width / 2)
    return flat([[a[0] + nx, a[1] + ny], [b[0] + nx, b[1] + ny], [b[0] - nx, b[1] - ny], [a[0] - nx, a[1] - ny]], z)
  })
  return join(...strips, ...points.map(([x, y]) => flat(round(x, y, width / 2, width / 2, 12), z)))
}

// --- solid props -----------------------------------------------------------------------------------

const { head } = SHAPE
// A point on his head, `out` off it: `down` is the angle from its top, `around` from his front towards his left.
const onHead = (down, around, out = 0) => [0, 1, 2].map((k) => [0, head.centreY, 0][k] + head.radius[k] * (1 + out) * [Math.sin(down) * Math.sin(around), Math.cos(down), Math.sin(down) * Math.cos(around)][k])
// A band around his head, `width` wide (as an angle), sitting `down` from the top and `tip` higher in front.
function band(down, tip, width, out, n = 48) {
  const positions = [], normals = [], indices = []
  for (let i = 0; i <= 2; i++) for (let j = 0; j <= n; j++) {
    const around = (j / n) * Math.PI * 2, at = down - tip * Math.cos(around) + ((i - 1) * width) / 2, p = onHead(at, around, out)
    positions.push(...p)
    const nrm = [0, 1, 2].map((k) => (p[k] - [0, head.centreY, 0][k]) / head.radius[k] ** 2), l = Math.hypot(...nrm)
    normals.push(...nrm.map((v) => v / l))
  }
  for (let i = 0; i < 2; i++) for (let j = 0; j < n; j++) {
    const a = i * (n + 1) + j, b = a + 1, c = a + n + 1, d = c + 1
    indices.push(a, c, b, b, c, d)
  }
  return { positions, normals, indices }
}

// Where the medal lies at rest, and how far down his head its ribbon starts.
const MEDAL = { at: [0, 0.43, 1.5], down: 1.95 }
/** The medal's hinge, `[y, z]`: it swings up and down around the level line through it, where its ribbon leaves his head. */
export const medalHinge = onHead(MEDAL.down, 0.55, 0.02).slice(1)
/** How far the medal's middle is from its hinge, and how far below level it lies at rest, in radians. */
export const medalReach = { length: Math.hypot(MEDAL.at[1] - medalHinge[0], MEDAL.at[2] - medalHinge[1]), rest: Math.atan2(medalHinge[0] - MEDAL.at[1], MEDAL.at[2] - medalHinge[1]) }

// Each prop gives its parts and the colours they need; `colours` are the `color.poulpy` tokens. Matte, shaded like him.
const PROPS = {
  // A white towelling band with an ember stripe, knotted on his left with two loose ends.
  sweatband: ({ ember }) => {
    const knot = onHead(0.95, 1.75, 0.03), end = (dx, dy, dz) => [knot[0] + dx, knot[1] + dy, knot[2] + dz]
    return {
      materials: { towel: { colour: linear(WHITE) }, stripe: { colour: linear(ember) } },
      parts: [
        { name: 'sweatband', material: 'towel', ...join(band(0.95, 0.1, 0.26, 0.02), tube([knot, end(0.2, 0.02, 0), end(0.36, -0.14, 0.02)], 0.05), tube([knot, end(0.12, -0.16, 0.04), end(0.1, -0.36, 0.06)], 0.05)) },
        { name: 'sweatband_stripe', material: 'stripe', ...band(0.95, 0.1, 0.07, 0.027) },
      ],
    }
  },
  // A short upright nightcap: a deep blue cone that leans to his left, a white brim and a white bobble.
  nightcap: () => {
    const brim = Array.from({ length: 41 }, (_, i) => onHead(0.6 - 0.06 * Math.cos((i / 40) * Math.PI * 2), (i / 40) * Math.PI * 2, 0.03))
    const spine = [[0, 1.86, 0], [0.04, 2.14, -0.02], [0.1, 2.4, -0.04], [0.17, 2.6, -0.06], [0.22, 2.74, -0.08]]
    return {
      materials: { cap: { colour: linear(NIGHT) }, wool: { colour: linear(WHITE) } },
      parts: [
        { name: 'nightcap', material: 'cap', ...join(egg(head.radius.map((r) => r * 1.025), { to: 0.62, around: 40, down: 10, at: [0, head.centreY, 0] }), tube(spine, [0.62, 0.46, 0.3, 0.17, 0.08], 24)) },
        { name: 'nightcap_brim', material: 'wool', ...join(tube(brim, 0.075), egg([0.13, 0.13, 0.13], { at: [0.24, 2.84, -0.09] })) },
      ],
    }
  },
  // A gold medal on an ember ribbon, resting on his front arm. It hangs from where the ribbon leaves his head.
  medal: ({ ember }) => {
    const face = facing([0, 0.34, 0.94]), at = MEDAL.at
    return {
      materials: { ribbon: { colour: linear(ember) }, gold: { colour: linear(GOLD) }, gold_rim: { colour: linear('#C98A0C') }, gold_shine: { colour: linear('#FFF3C4') } },
      parts: [
        { name: 'medal_ribbon', material: 'ribbon', ...join(...[-1, 1].map((sgn) => tube([onHead(MEDAL.down, sgn * 0.55, 0.02), onHead(2.25, sgn * 0.3, 0.03), [sgn * 0.12, 0.6, 1.2], [sgn * 0.05, 0.56, 1.4]], 0.055))) },
        { name: 'medal_rim', material: 'gold_rim', ...egg([0.25, 0.25, 0.045], { around: 28, turn: face, at }) },
        { name: 'medal', material: 'gold', ...egg([0.2, 0.2, 0.06], { around: 28, turn: face, at: face([0, 0, 0.012]).map((v, k) => v + at[k]) }) },
        { name: 'medal_shine', material: 'gold_shine', ...egg([0.09, 0.09, 0.02], { around: 16, down: 8, turn: face, at: face([0, 0, 0.062]).map((v, k) => v + at[k]) }) },
      ],
    }
  },
  // A dumbbell lying on the floor by his left side.
  dumbbell: ({ ink }) => {
    const a = [1.5, 0.21, 1.3], b = [2.02, 0.21, 0.98], along = facing([(b[0] - a[0]) / Math.hypot(b[0] - a[0], b[2] - a[2]), 0, (b[2] - a[2]) / Math.hypot(b[0] - a[0], b[2] - a[2])])
    return {
      materials: { steel: { colour: linear(STEEL) }, weight: { colour: linear(ink) } },
      parts: [
        { name: 'dumbbell_bar', material: 'steel', ...tube([a, b], 0.045) },
        { name: 'dumbbell_weights', material: 'weight', ...join(...[a, b].map((at) => egg([0.21, 0.21, 0.085], { around: 24, turn: along, at }))) },
      ],
    }
  },
}

// --- a pose ----------------------------------------------------------------------------------------

/**
 * Builds Poulpy in one of his poses.
 * @param {{ pose: keyof typeof POSES, effects?: boolean, tones: Record<string, string>, colours: { blue: string, ember: string, ink: string } }} options
 *   `tones` are the `mood` tokens of design/tokens.json and `colours` its `color.poulpy` tokens.
 *   `effects: false` leaves out the flat effects around him, for when he moves, and `face` gives him another face
 *   than the pose's: a name in FACES or a list of features.
 * @returns What buildPoulpy3d returns, and more: `headTilt`, how far his head leans to his right, in radians,
 *   for whoever draws him to apply with `headLift`; parts with `card` set to 'front' or 'back', the flat effects,
 *   drawn facing +z around his middle and to be kept facing the viewer, in front of him or behind; and
 *   materials that may have an `opacity`.
 */
export function buildPoulpyPose({ pose, tones, colours, effects = true, face = POSES[pose].face }) {
  const { mood, on, stretch, headTilt = 0, props = [], around = [], behind = [] } = POSES[pose]
  const model = buildPoulpy3d({ mood, tone: tones[mood], face: typeof face === 'string' ? face : undefined, features: typeof face === 'string' ? undefined : face, on, stretch, colours })
  for (const prop of props) {
    const made = PROPS[prop](colours)
    Object.assign(model.materials, made.materials)
    model.parts.push(...made.parts)
  }
  const named = { ink: colours.ink, ember: colours.ember }
  const paint = (colour, opacity = 1) => {
    const hex = named[colour] ?? colour, name = `flat_${hex.slice(1).toLowerCase()}${opacity < 1 ? `_${Math.round(opacity * 100)}` : ''}`
    model.materials[name] ??= { colour: linear(hex), flat: true, ...(opacity < 1 && { opacity }) }
    return name
  }
  for (const [card, drawn] of effects ? [['back', behind], ['front', around]] : []) {
    drawn.forEach(([kind, ...v], i) => {
      // Later effects lie a hair in front of earlier ones.
      let z = i * 0.004
      const add = (material, shape) => { model.parts.push({ name: `effect_${card}_${i + 1}`, material, card, ...shape }); z += 0.001 }
      const edge = (points, look) => { if (look?.stroke) add(paint(look.stroke), ribbon([...points, points[0]].map(onCard), look.width / CARD, z)) }
      if (kind === 'shape') {
        const [d, colour, look] = v
        for (const points of pathPoints(d)) { add(paint(colour, look?.opacity), fan(points, z)); edge(points, look) }
      } else if (kind === 'oval') {
        const [x, y, rx, ry, colour, look] = v, points = round(x, y, rx, ry)
        add(paint(colour, look?.opacity), fan(points, z)); edge(points, look)
      } else if (kind === 'stroke') {
        const [d, width, colour] = v
        add(paint(colour), join(...pathPoints(d).map((points) => ribbon(points.map(onCard), width / CARD, z))))
      } else if (kind === 'text') {
        const [x, y, size, colour, words, anchor] = v
        add(paint(colour), join(...lettering(x, y, size, words, anchor).map((points) => ribbon(points.map(onCard), (size * 0.15) / CARD, z))))
      } else throw new Error(`No way to draw a "${kind}"`)
    })
  }
  return { ...model, headTilt: (headTilt * Math.PI) / 180 }
}

/** The mood a pose is drawn in: the side of the plush it shows and the `mood` token that tints it. */
export const poseMood = (pose) => ({ mood: POSES[pose].mood, side: MOODS[POSES[pose].mood].side })
