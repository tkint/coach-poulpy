// Poulpy the DJ, a look: a headset over his head and a set of decks on the floor in front of him, two turntables and a
// mixer, which he plays with the tips of his front arms lying on them. His eyes are shut and he smiles a little: he is
// into the music. Built on the shape and the faces of model.mjs, like a pose of poses.mjs, with no dependency. The
// workshop's Brand > Looks > The DJ page draws it live, and his idle (DJ in motion.mjs) bobs his head to the beat, scratches
// one record while the other turns, and lets music notes float up around him.
//
// The headset is solid and goes wherever his head goes; the decks lie on the floor and stay there. The notes are comic
// effects, flat and facing the viewer, like the poses' sparks. He never lifts an arm (brand decision 8): the tips of his
// front three lie on the decks as they lie on the floor, and the scratch is his arm sliding, as it slides when he turns.
// To change the look, edit DJ_KIT, DJ_FACE or DJ_NOTES below and look at the page.
import { SHAPE, FACES, buildPoulpy3d, facing, join, linear, pathPoints, tube } from '../model.mjs'

// The kit, in units of the model (his head is about 1 from its middle), and its colours.
// The decks' top is `top` off the floor: low enough that his arms lie over them and his face stays clear.
export const DJ_KIT = {
  colours: { kit: '#2B3A4A', groove: '#4A5A6C', metal: '#B6C4D0', gold: '#F0B53A', white: '#FFFFFF' },
  // The cups sit `down` from the top of his head (an angle), `back` behind his sides and `lift` off his skin; the band runs
  // over the top `out` off his head, `pad` thicker in its middle.
  headset: { band: { width: 0.06, pad: 0.5, out: 0.05 }, cup: { down: 1.36, back: 0, size: 0.31, depth: 0.24, lift: 0.11 } },
  // The console: its half width and half depth, its height and how far in front of his middle it lies.
  console: { half: [1.64, 0.54], top: 0.2, z: 1.36 },
  platter: 0.43,
  record: 0.38,
}
/** The middle of each record, on its top: his right one (the viewer's left) first. Each turns around the up line through it. */
export const DJ_PLATTERS = [-1, 1].map((side) => [side * 1.08, DJ_KIT.console.top + 0.075, 1.3])
/** Which arm lies on each record (as in motion.mjs, from 0, his front one first and on to his left): his right one, then his left one. */
export const DJ_ARMS = [7, 1]

// His face: eyes shut in two happy arcs and a small smile, a little lopsided, as he listens. His blush is the happy one's.
export const DJ_FACE = [
  ['curve', 71, 98, 80, 90, 89, 98, 4.5, 'eye_right'], ['curve', 111, 98, 120, 90, 129, 98, 4.5, 'eye_left'],
  ['curve', 90, 112, 101, 122, 111, 111, 4.5, 'mouth'], ...FACES.happy.filter(([kind]) => kind === 'cheek'),
]

// --- solid kit -------------------------------------------------------------------------------------

const { head } = SHAPE
// A point on his head, `out` off it: `down` is the angle from its top, `around` from his front towards his left.
const onHead = (down, around, out = 0) => [0, 1, 2].map((k) => [0, head.centreY, 0][k] + head.radius[k] * (1 + out) * [Math.sin(down) * Math.sin(around), Math.cos(down), Math.sin(down) * Math.cos(around)][k])

// A rounded block, `size` its half width, height and depth: a box with soft edges when `edge` and `plan` are small, round
// seen from above (a puck, a knob, a record) when `plan` is 1. `turn` and `at` place it, as for egg().
function block([a, b, c], { edge = 0.2, plan = edge, around = 32, down = 16, turn = (p) => p, at = [0, 0, 0] } = {}) {
  const pow = (v, m) => Math.sign(v) * Math.abs(v) ** m, positions = [], normals = [], indices = []
  for (let i = 0; i <= down; i++) {
    const th = (Math.PI * i) / down, st = Math.sin(th), ct = Math.cos(th)
    for (let j = 0; j <= around; j++) {
      const ph = (j / around) * Math.PI * 2, sp = Math.sin(ph), cp = Math.cos(ph)
      const p = turn([a * pow(st, edge) * pow(sp, plan), b * pow(ct, edge), c * pow(st, edge) * pow(cp, plan)])
      positions.push(p[0] + at[0], p[1] + at[1], p[2] + at[2])
      const n = turn([(pow(st, 2 - edge) * pow(sp, 2 - plan)) / a, pow(ct, 2 - edge) / b, (pow(st, 2 - edge) * pow(cp, 2 - plan)) / c]), l = Math.hypot(...n) || 1
      normals.push(n[0] / l, n[1] / l, n[2] / l)
    }
  }
  for (let i = 0; i < down; i++) for (let j = 0; j < around; j++) {
    const p = i * (around + 1) + j, q = p + 1, r = p + around + 1, t = r + 1
    indices.push(p, r, q, q, r, t)
  }
  return { positions, normals, indices }
}
const puck = (radius, half, at, edge = 0.5) => block([radius, half, radius], { plan: 1, edge, at })

// The headset: a padded band over the top of his head from one cup to the other, and a round cup over each side of it.
function headset({ ember, ink }) {
  const { band, cup } = DJ_KIT.headset, around = (side) => side * (Math.PI / 2 + cup.back)
  // From the cup on his right, over the top, to the cup on his left; thicker where it is padded.
  const over = Array.from({ length: 25 }, (_, i) => { const t = -1 + i / 12; return onHead(Math.abs(t) * cup.down, around(t < 0 ? -1 : 1), band.out) })
  const thick = over.map((_, i) => band.width * (1 + band.pad * Math.max(0, 1 - Math.abs(i - 12) / 7)))
  const cups = [-1, 1].map((side) => {
    const at = (lift) => onHead(cup.down, around(side), lift), n = at(1).map((v, k) => v - at(0)[k]), l = Math.hypot(...n), way = facing(n.map((v) => v / l))
    // A puck is round around y: it is stood up along z, then z is turned onto the way his head faces there.
    const turn = ([x, y, z]) => way([x, -z, y]), round = (r, half, edge, lift) => block([r, half, r], { plan: 1, edge, turn, at: at(lift) })
    return { cushion: round(cup.size, cup.depth * 0.3, 0.6, cup.lift * 0.4), shell: round(cup.size * 0.95, cup.depth * 0.35, 0.4, cup.lift), cap: round(cup.size * 0.5, 0.03, 0.5, cup.lift + cup.depth * 0.33) }
  })
  return {
    materials: { headset: { colour: linear(DJ_KIT.colours.kit) }, headset_cushion: { colour: linear(ink) }, headset_shell: { colour: linear(ember) }, headset_cap: { colour: linear(DJ_KIT.colours.gold) } },
    parts: [
      { name: 'headset_band', material: 'headset', ...tube(over, thick, 14) },
      { name: 'headset_cushions', material: 'headset_cushion', ...join(...cups.map((c) => c.cushion)) },
      { name: 'headset_cups', material: 'headset_shell', ...join(...cups.map((c) => c.shell)) },
      { name: 'headset_caps', material: 'headset_cap', ...join(...cups.map((c) => c.cap)) },
    ],
  }
}

// The decks, on the floor in front of him: one console, a turntable each side and the mixer in the middle. Each record is
// its own parts, `decks_record_N...`, so it can turn around its middle (DJ_PLATTERS) while the rest stays put.
function decks({ ember, ink }) {
  const { console: c, platter, record, colours } = DJ_KIT, top = c.top
  const parts = [
    { name: 'decks', material: 'decks', ...block([c.half[0], top / 2, c.half[1]], { edge: 0.3, plan: 0.22, at: [0, top / 2, c.z] }) },
    { name: 'decks_platters', material: 'decks_metal', ...join(...DJ_PLATTERS.map(([x, , z]) => puck(platter, 0.025, [x, top + 0.02, z], 0.4))) },
  ]
  DJ_PLATTERS.forEach(([x, y, z], i) => {
    const n = i + 1, ring = Array.from({ length: 41 }, (_, k) => [x + record * 0.7 * Math.sin((k / 40) * Math.PI * 2), y + 0.008, z + record * 0.7 * Math.cos((k / 40) * Math.PI * 2)])
    parts.push(
      { name: `decks_record_${n}`, material: 'decks_vinyl', ...puck(record, 0.02, [x, y - 0.01, z]) },
      { name: `decks_record_${n}_groove`, material: 'decks_groove', ...tube(ring, 0.01, 6) },
      { name: `decks_record_${n}_label`, material: i ? 'decks_gold' : 'decks_ember', ...puck(record * 0.36, 0.012, [x, y + 0.008, z]) },
      // A white dot off the middle of the label, so the record is seen to turn.
      { name: `decks_record_${n}_mark`, material: 'decks_white', ...puck(0.035, 0.01, [x + record * 0.2 * Math.sign(x), y + 0.018, z + record * 0.08]) },
    )
  })
  // The tonearms rest across the outer side of each record, from a post at the console's back corner.
  const tonearms = DJ_PLATTERS.flatMap(([x, y, z]) => {
    const side = Math.sign(x), post = [x + side * (record + 0.09), z - record * 0.72]
    return [puck(0.055, 0.06, [post[0], top + 0.06, post[1]]), tube([[post[0], y + 0.11, post[1]], [post[0], y + 0.11, z + record * 0.1], [x + side * record * 0.55, y + 0.06, z + record * 0.45]], 0.02, 8)]
  })
  parts.push({ name: 'decks_tonearms', material: 'decks_metal', ...join(...tonearms) })
  // The mixer: two knobs a side, a fader a side, and the crossfader at the front.
  const knob = (x, z) => puck(0.055, 0.04, [x, top + 0.04, z], 0.6)
  const slot = (half, x, z) => block([half[0], 0.006, half[1]], { edge: 0.5, plan: 0.3, at: [x, top + 0.004, z] })
  const fader = (half, x, z) => block([half[0], 0.03, half[1]], { edge: 0.4, plan: 0.3, at: [x, top + 0.03, z] })
  parts.push(
    { name: 'decks_knobs', material: 'decks_gold', ...join(knob(-0.36, c.z - 0.22), knob(0.36, c.z - 0.22), knob(-0.36, c.z), knob(0.36, c.z)) },
    { name: 'decks_slots', material: 'decks_vinyl', ...join(slot([0.02, 0.12], -0.36, c.z + 0.27), slot([0.02, 0.12], 0.36, c.z + 0.27), slot([0.2, 0.022], 0, c.z + 0.42)) },
    { name: 'decks_faders', material: 'decks_white', ...join(fader([0.045, 0.026], -0.36, c.z + 0.22), fader([0.045, 0.026], 0.36, c.z + 0.32), fader([0.032, 0.045], 0.06, c.z + 0.42)) },
  )
  return {
    materials: {
      decks: { colour: linear(colours.kit) }, decks_metal: { colour: linear(colours.metal) }, decks_vinyl: { colour: linear(ink) }, decks_groove: { colour: linear(colours.groove), flat: true },
      decks_gold: { colour: linear(colours.gold) }, decks_ember: { colour: linear(ember) }, decks_white: { colour: linear(colours.white) },
    },
    parts,
  }
}

// --- notes -----------------------------------------------------------------------------------------

// A note, as closed outlines in the drawing's coordinates (x grows to the right, y downwards): 'one' a quaver, 'two' two
// quavers beamed together. (x, y) is the middle of its head, or of the first head, and `k` scales it: at 1 it is 28 tall.
const oval = (x, y, rx, ry, turn) => Array.from({ length: 20 }, (_, i) => {
  const t = (i / 20) * Math.PI * 2, px = rx * Math.cos(t), py = ry * Math.sin(t), c = Math.cos(turn), s = Math.sin(turn)
  return [x + px * c - py * s, y + px * s + py * c]
})
const bar = (x1, y1, x2, y2, w) => [[x1 - w / 2, y1], [x1 + w / 2, y1], [x2 + w / 2, y2], [x2 - w / 2, y2]]
export function note(x, y, k = 1, kind = 'one') {
  const head = (hx, hy) => oval(hx, hy, 6.2 * k, 4.6 * k, -0.38), stem = 3.6 * k, w = 2.6 * k, top = y - 26 * k
  if (kind === 'one') {
    const sx = x + stem + w / 2
    // The flag curls out and down from the top of the stem; it is kept convex, so it fills as one fan.
    const flag = pathPoints(`M${sx - w / 2} ${top} C${sx + 6 * k} ${top + 3 * k} ${sx + 11 * k} ${top + 9 * k} ${sx + 7 * k} ${top + 18 * k} L${sx - w / 2} ${top + 8 * k} Z`)[0]
    return [head(x, y), bar(x + stem, top, x + stem, y - k, w), flag]
  }
  const x2 = x + 16 * k, y2 = y - 3 * k, top2 = top - 3 * k
  return [head(x, y), head(x2, y2), bar(x + stem, top, x + stem, y - k, w), bar(x2 + stem, top2, x2 + stem, y2 - k, w),
    [[x + stem - w / 2, top], [x2 + stem + w / 2, top2], [x2 + stem + w / 2, top2 + 6 * k], [x + stem - w / 2, top + 6 * k]]]
}
// A filled outline as a fan of triangles from its middle, facing +z: its points are [x, y] with y up.
function fan(points, z) {
  let around = points
  if (around.reduce((sum, [x, y], i) => { const [nx, ny] = around[(i + 1) % around.length]; return sum + x * ny - nx * y }, 0) < 0) around = [...around].reverse()
  const mid = [0, 1].map((k) => around.reduce((sum, q) => sum + q[k], 0) / around.length), all = [mid, ...around]
  return { positions: all.flatMap(([x, y]) => [x, y, z]), normals: all.flatMap(() => [0, 0, 1]), indices: around.flatMap((_, i) => [0, 1 + i, 1 + ((i + 1) % around.length)]) }
}
// The same outline grown by `by` all around, for the white edge that keeps a note readable on any background.
function grown(points, by) {
  const mid = [0, 1].map((k) => points.reduce((sum, q) => sum + q[k], 0) / points.length)
  return points.map((p, i) => {
    const a = points[(i + points.length - 1) % points.length], b = points[(i + 1) % points.length], t = [b[0] - a[0], b[1] - a[1]], l = Math.hypot(...t) || 1
    let n = [t[1] / l, -t[0] / l]
    if ((p[0] - mid[0]) * n[0] + (p[1] - mid[1]) * n[1] < 0) n = [-n[0], -n[1]]
    return [p[0] + n[0] * by, p[1] + n[1] * by]
  })
}
/**
 * A note as flat shapes facing +z, 1 tall and its middle at 0, for whoever draws it in the room: `fill` in its colour and
 * `edge`, a little bigger and a hair behind, in white.
 */
export function noteShape(kind = 'one') {
  const mid = kind === 'one' ? [4, -12] : [12, -14]
  const outlines = note(0, 0, 1, kind).map((points) => points.map(([x, y]) => [(x - mid[0]) / 28, -(y - mid[1]) / 28]))
  return { fill: join(...outlines.map((points) => fan(points, 0))), edge: join(...outlines.map((points) => fan(grown(points, DJ_NOTE_EDGE), -0.01))) }
}
// How wide the white edge of a note is, as a share of its height.
const DJ_NOTE_EDGE = 0.06

/**
 * The notes around him. Still, they stand where `still` puts them, in the drawing's coordinates (x, y and size), in front
 * of him. When he plays, each floats up from `from` (in the room) and is `size` tall; a new one starts every `every` beats,
 * in turn, and the move (DJ in motion.mjs) does the rest.
 */
export const DJ_NOTES = [
  { kind: 'one', colour: DJ_KIT.colours.gold, size: 0.44, from: [-1.5, 1.45, 0.25], still: [-4, 60, 0.95] },
  { kind: 'two', colour: '#1574B0', size: 0.48, from: [1.5, 1.6, 0.15], still: [178, 30, 1] },
  { kind: 'two', colour: '#1574B0', size: 0.38, from: [-1.25, 0.9, 1.5], still: [22, 8, 0.7] },
  { kind: 'one', colour: DJ_KIT.colours.gold, size: 0.4, from: [1.35, 0.95, 1.45], still: [196, 96, 0.6] },
]

// --- the DJ ----------------------------------------------------------------------------------------

/**
 * Builds Poulpy the DJ.
 * @param {{ tones: Record<string, string>, colours: { blue: string, ember: string, ink: string }, effects?: boolean }} options
 *   `tones` are the `mood` tokens of design/tokens.json and `colours` its `color.poulpy` tokens. He is always happy, on the
 *   blue side. `effects: false` leaves out the notes standing still around him, for when he plays and they float.
 * @returns What buildPoulpyPose returns: his parts, the headset with his head, the decks on the floor (records apart, as
 *   `decks_record_N...`), and the still notes as flat parts with `card` 'front'; `headTilt` is 0.
 */
export function buildDj({ tones, colours, effects = true }) {
  const model = buildPoulpy3d({ mood: DJ_MOOD, tone: tones[DJ_MOOD], features: DJ_FACE, colours })
  for (const kit of [headset(colours), decks(colours)]) {
    Object.assign(model.materials, kit.materials)
    model.parts.push(...kit.parts)
  }
  if (effects) {
    // A card's unit is 64 of the drawing's, its middle is x 100 and its floor y 165, as for the poses.
    const onCard = ([x, y]) => [(x - 100) / 64, (165 - y) / 64]
    model.materials.note_edge = { colour: linear(DJ_KIT.colours.white), flat: true }
    DJ_NOTES.forEach(({ kind, colour, still: [x, y, k] }, i) => {
      const material = `note_${colour.slice(1).toLowerCase()}`, outlines = note(x, y, k, kind).map((points) => points.map(onCard))
      model.materials[material] ??= { colour: linear(colour), flat: true }
      model.parts.push(
        { name: `effect_front_${i + 1}_edge`, material: 'note_edge', card: 'front', ...join(...outlines.map((points) => fan(grown(points, DJ_NOTE_EDGE * (28 * k) / 64), i * 0.004))) },
        { name: `effect_front_${i + 1}`, material, card: 'front', ...join(...outlines.map((points) => fan(points, i * 0.004 + 0.001))) },
      )
    })
  }
  return { ...model, headTilt: 0 }
}

/** The mood the DJ is drawn in. */
export const DJ_MOOD = 'happy'
