// Poulpy the cook, a look: a tall white toque on his head, a curled French moustache under his eyes, and the face of
// someone who knows his sauce is the best. Built on the shape and the faces of model.mjs, like a pose of poses.mjs,
// with no dependency. The workshop's Brand > Poulpy looks > The cook page draws it live, and its idle (CHEF in motion.mjs)
// lifts his chin and twitches his moustache.
//
// His face, on the proud blue side: a wide, confident grin with a few clenched teeth, eyes wide open and both brows up.
// The moustache is solid, laid along a line in the drawing's coordinates and standing out from his face; it is part of his
// face, so it twitches with it and stays on him when he turns. The toque is solid too, sits on his head and goes wherever his head goes. He has no arms (brand decision 8).
// To change the look, edit TOQUE, MOUSTACHE, SMILE or CHEF_FACE below and look at the page.
import { SHAPE, buildPoulpy3d, linear, pathPoints, tube, join } from '../model.mjs'

// The toque, in units of the model (his head is about 1 from its middle). `profile` is its outline from the bottom of its
// band to the top of its puff, as [distance from its middle, height], turned around its middle; `pleats` puts that many soft
// folds around the puff, `fold` deep, and the band stays plain. It sits `at` on his head and leans `lean` degrees to his
// left and `back` degrees backwards: a cook who knows he is good wears it a little askew.
export const TOQUE = {
  at: [0, 1.9, -0.02],
  lean: 9,
  back: 5,
  band: { top: 0.42, line: 0.05 },
  profile: [[0.6, -0.16], [0.64, 0], [0.66, 0.42], [0.82, 0.52], [0.95, 0.72], [1.0, 0.95], [0.97, 1.15], [0.86, 1.32], [0.64, 1.45], [0.36, 1.52], [0, 1.55]],
  pleats: 9,
  fold: 0.05,
  colour: '#FFFFFF',
  // The faint line where the band meets the puff.
  seam: '#D7E2EA',
}


// The moustache: a solid handlebar standing out from his face, thick under his middle and tapering out to two tips that curl
// up and in. `path` is the line along its middle for his right wing (the viewer's left), in the drawing's coordinates, from
// his middle to the tip of the curl; his left wing is the same, swapped across his middle. It is scaled by `size`, across
// and up, around `middle`, and sits `drop` lower. `thick` is its radius at his middle and `tip` at the end of the curl, in
// units of the model, thinning by `taper` along the way. `twitch` lifts the tips by that many drawing units at the top
// of a twitch, and draws them in a little.
export const MOUSTACHE = {
  path: 'M100 106.5 C93 105 84 109.5 77.5 107.5 Q71 105.5 70.5 100.5 Q71 96.5 74.5 97 Q77 98 75.5 101',
  size: [1.3, 1.1],
  middle: [100, 108],
  drop: -0.5,
  thick: 0.112,
  tip: 0.03,
  taper: 1.1,
  twitch: 3.2,
  colour: '#262E3E', // a little lighter than his ink, so its shading shows
}

// Where a point of the drawing lands on the front of his head, and which way his head faces there: as his face is pressed
// onto it (buildPoulpy3d in model.mjs).
const U = 63
function onFace(x, y) {
  const { head, face } = SHAPE, [rx, ry, rz] = head.radius
  const px = ((x - 100) / U) * face.scale * face.spread, py = face.y + ((130 - y) / U) * face.scale, dy = py - head.centreY
  const pz = rz * Math.sqrt(Math.max(1 - (px / rx) ** 2 - (dy / ry) ** 2, 0))
  const n = [px / rx ** 2, dy / ry ** 2, pz / rz ** 2], l = Math.hypot(...n)
  return { p: [px, py, pz], n: n.map((v) => v / l) }
}

/**
 * The moustache as a solid, `t` into a twitch: 0 at rest, 1 with its tips up. Only the tips move; its middle stays put.
 * @returns {{ positions: number[], normals: number[], indices: number[] }} both wings, in place on his head at rest
 */
export function moustache(t = 0) {
  const { size, middle: [mx, my], thick, tip, taper } = MOUSTACHE
  // How far a point is from his middle, as a share of the moustache's half width, squared: the tips move, the middle does not.
  const lift = ([x, y]) => {
    const far = Math.min(Math.abs(x - 100) / 28, 1.2) ** 2, lx = x + 0.6 * t * far, ly = y - MOUSTACHE.twitch * t * far
    return [mx + (lx - mx) * size[0], my + (ly - my) * size[1] + MOUSTACHE.drop]
  }
  const line = pathPoints(MOUSTACHE.path)[0].map(lift)
  // Its share of the way along, so it thins evenly whatever the steps.
  const along = line.map((_, i) => line.slice(1, i + 1).reduce((sum, q, k) => sum + Math.hypot(q[0] - line[k][0], q[1] - line[k][1]), 0))
  const radii = along.map((a) => tip + (thick - tip) * (1 - a / along[along.length - 1]) ** taper)
  // Each point stands off his skin by most of its radius, so it is a volume on his face and not sunk into it.
  const wing = (side) => tube(line.map(([x, y], i) => { const { p, n } = onFace(100 + side * (x - 100), y); return p.map((v, k) => v + n[k] * radii[i] * 0.7) }), radii, 14)
  return join(wing(1), wing(-1))
}

// His face, without the moustache: a confident toothy grin, small and under the moustache, with a few top teeth and the
// bottom ones showing; eyes wide open and both brows up, set high to leave the moustache room. `smile` scales the grin
// around its middle. Left and right are Poulpy's own, as in FACES.
export const SMILE = { size: 0.8, middle: [100, 117] }
const smile = (d) => d.replace(/(-?\d*\.?\d+)\s+(-?\d*\.?\d+)/g, (_, x, y) => [SMILE.middle[0] + (x - SMILE.middle[0]) * SMILE.size, SMILE.middle[1] + (y - SMILE.middle[1]) * SMILE.size].map((v) => +v.toFixed(2)).join(' '))
const GRIN = 'M78 109 Q100 119 122 109 Q119 128 100 130 Q81 128 78 109 Z'
export const CHEF_FACE = [
  ['eye', 80, 85, 8.5, 10], ['eye', 120, 85, 8.5, 10],
  ['curve', 67, 69, 79, 61, 91, 66, 4.5, 'brow_right'], ['curve', 109, 66, 121, 61, 133, 69, 4.5, 'brow_left'],
  ['shape', smile(GRIN), '#FFFFFF', 'teeth'],
  ['path', smile(GRIN), 3.5, 'mouth'],
  ['path', smile('M80 118 Q100 126 120 118'), 2.5, 'teeth_line'],
  ['path', smile('M93 119 L93 129 M107 119 L107 129'), 2, 'teeth_gaps'],
  ['cheek', 60, 108, 7, 4.5, '#F49A6B', 0.75], ['cheek', 140, 108, 7, 4.5, '#F49A6B', 0.75],
]

/** The mood the cook is drawn in: proud, on the blue side. */
export const CHEF_MOOD = 'proud'


// --- the toque ---------------------------------------------------------------------------------------

const RAD = Math.PI / 180
// A shape turned around the y axis from an outline of [distance, height], `wobble(angle, k)` pushing point k of the outline
// out by that share. Its normals are worked out from its own surface.
function lathe(profile, around, wobble = () => 0) {
  const positions = [], normals = [], indices = [], rows = profile.length, at = (i, j) => {
    const a = (j / around) * Math.PI * 2, [r, y] = profile[i], k = r * (1 + wobble(a, i))
    return [k * Math.sin(a), y, k * Math.cos(a)]
  }
  for (let i = 0; i < rows; i++) for (let j = 0; j <= around; j++) {
    const p = at(i, j), up = at(Math.min(i + 1, rows - 1), j), down = at(Math.max(i - 1, 0), j), next = at(i, j + 1), prev = at(i, j - 1)
    const u = [0, 1, 2].map((k) => up[k] - down[k]), v = [0, 1, 2].map((k) => next[k] - prev[k])
    let n = [v[1] * u[2] - v[2] * u[1], v[2] * u[0] - v[0] * u[2], v[0] * u[1] - v[1] * u[0]]
    // At the very top the outline has no width: the normal points up.
    const l = Math.hypot(...n)
    n = l < 1e-9 ? [0, 1, 0] : n.map((x) => x / l)
    positions.push(...p); normals.push(...n)
  }
  for (let i = 0; i < rows - 1; i++) for (let j = 0; j < around; j++) {
    const a = i * (around + 1) + j, b = a + 1, c = a + around + 1, d = c + 1
    indices.push(a, b, c, b, d, c)
  }
  return { positions, normals, indices }
}

// Smooths an outline: each stretch between two of its points becomes `steps` shorter ones, along a round curve through them.
function smoothed(points, steps = 4) {
  const out = []
  for (let i = 0; i < points.length - 1; i++) {
    const p0 = points[Math.max(i - 1, 0)], p1 = points[i], p2 = points[i + 1], p3 = points[Math.min(i + 2, points.length - 1)]
    for (let s = 0; s < steps; s++) {
      const t = s / steps, t2 = t * t, t3 = t2 * t
      out.push([0, 1].map((k) => 0.5 * (2 * p1[k] + (-p0[k] + p2[k]) * t + (2 * p0[k] - 5 * p1[k] + 4 * p2[k] - p3[k]) * t2 + (-p0[k] + 3 * p1[k] - 3 * p2[k] + p3[k]) * t3)))
    }
  }
  out.push(points[points.length - 1])
  return out
}

/** The toque as parts, placed on his head: its puff and band, and the seam between them. */
export function toque() {
  const { at, lean, back, band, pleats, fold } = TOQUE
  // The band is a plain straight piece; above it the outline is smoothed into a round puff.
  const top = TOQUE.profile.findIndex(([, y]) => y >= band.top)
  const outline = [...TOQUE.profile.slice(0, top), ...smoothed(TOQUE.profile.slice(top), 5)]
  // Folds only on the puff, deepest at its widest, fading towards the band and the top.
  const widest = Math.max(...outline.map(([r]) => r))
  const wobble = (a, i) => { const [r, y] = outline[i]; return y <= band.top ? 0 : fold * (r / widest) ** 3 * (0.5 + 0.5 * Math.cos(a * pleats)) - fold / 2 }
  const turn = ([x, y, z]) => {
    // Backwards first (around x), then to his left (around z), then onto his head.
    const cb = Math.cos(-back * RAD), sb = Math.sin(-back * RAD), cl = Math.cos(-lean * RAD), sl = Math.sin(-lean * RAD)
    const [y1, z1] = [y * cb - z * sb, y * sb + z * cb], [x2, y2] = [x * cl - y1 * sl, x * sl + y1 * cl]
    return [x2, y2, z1]
  }
  const placed = (shape) => ({
    positions: shape.positions.reduce((out, _, i) => { if (i % 3 === 0) out.push(...turn(shape.positions.slice(i, i + 3)).map((v, k) => v + at[k])); return out }, []),
    normals: shape.normals.reduce((out, _, i) => { if (i % 3 === 0) out.push(...turn(shape.normals.slice(i, i + 3))); return out }, []),
    indices: shape.indices,
  })
  const puff = lathe(outline, 72, wobble)
  // A crease runs up each fold's valley, from the band to near the top: a thin line in the seam's colour, lying on the puff.
  const puffRows = outline.map(([r, y], i) => ({ r, y, i })).filter(({ y }) => y > band.top + 0.04 && y < outline[outline.length - 1][1] - 0.12)
  const creases = join(...Array.from({ length: pleats }, (_, k) => {
    const a = ((k + 0.5) / pleats) * Math.PI * 2
    return tube(puffRows.map(({ r, y, i }) => { const out = r * (1 + wobble(a, i)) + 0.008; return [out * Math.sin(a), y, out * Math.cos(a)] }), 0.014, 6)
  }))
  // The seam is a thin ring a hair proud of the band, just under the puff.
  const r = TOQUE.profile[top - 1][0] + 0.012, y0 = band.top - band.line
  const seam = lathe([[r, y0], [r + 0.004, y0 + band.line / 2], [r, band.top]], 72)
  const hat = placed(puff)
  return {
    materials: { toque: { colour: linear(TOQUE.colour) }, toque_seam: { colour: linear(TOQUE.seam) } },
    parts: [{ name: 'toque', material: 'toque', ...hat }, { name: 'toque_seam', material: 'toque_seam', ...placed(join(seam, creases)) }],
    // How high its top reaches, for whoever frames him.
    top: Math.max(...hat.positions.filter((_, i) => i % 3 === 1)),
  }
}

/**
 * Builds Poulpy the cook.
 * @param {{ face?: boolean, twitch?: number, tones: Record<string, string>, colours: { blue: string, ember: string, ink: string }, faceOnly?: boolean }} options
 *   `face: false` leaves his face out, for whoever draws it apart while he moves, and `twitch` is how far
 *   into a moustache twitch he is. `tones` are the `mood` tokens of design/tokens.json and `colours` its `color.poulpy`
 *   tokens. He is always proud, on the blue side (CHEF_MOOD). `faceOnly` builds his face alone, without his head or toque.
 * @returns What buildPoulpy3d returns, with the toque among his parts (it goes with his head) and `height` up to its top.
 */
export function buildChef({ face = true, twitch = 0, tones, colours, faceOnly = false }) {
  const model = buildPoulpy3d({ mood: CHEF_MOOD, tone: tones[CHEF_MOOD], features: face ? CHEF_FACE : [], colours, faceOnly })
  // The moustache belongs to his face: it is built with it, and twitches with it.
  if (face) {
    model.materials.moustache = { colour: linear(MOUSTACHE.colour) }
    model.parts.push({ name: 'moustache', material: 'moustache', ...moustache(twitch) })
  }
  if (faceOnly) return model
  const hat = toque()
  Object.assign(model.materials, hat.materials)
  model.parts.push(...hat.parts)
  return { ...model, height: Math.max(model.height, hat.top), headTilt: 0 }
}
