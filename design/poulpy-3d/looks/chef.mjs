// Poulpy the cook, a look: a tall white toque on his head, a curled French moustache under his eyes, and the face of
// someone who knows his sauce is the best. Built on the shape and the faces of model.mjs, like a pose of poses.mjs,
// with no dependency. The workshop's Brand > Looks > The cook page draws it live, and its idle (CHEF in motion.mjs)
// lifts his chin and twitches his moustache.
//
// Two faces to choose from, both on the proud blue side:
//   cocky  the cocky face of FACES, one lid down and one brow up, with the smirk moved under the moustache
//   grin   a very wide, clenched, toothy grin from cheek to cheek, eyes wide open and both brows up
// The moustache is part of his face, written in the drawing's coordinates like every feature, so it stays on his skin
// when he turns; the toque is solid, sits on his head and goes wherever his head goes. He has no arms (brand decision 8).
// To change the look, edit TOQUE, MOUSTACHE or CHEF_FACES below and look at the page.
import { FACES, buildPoulpy3d, linear, tube, join } from '../model.mjs'

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

// The moustache: a handlebar, thick under his middle and thinning out to two tips that curl up and in. Written as the
// drawing's paths, for his right side (the viewer's left); his left side is the same, swapped across his middle.
// `twitch` lifts the tips by that many drawing units at the top of a twitch, and curls them a little tighter.
export const MOUSTACHE = {
  wing: 'M100 105 C94 102.5 85 102.5 79 106 C76.5 107.5 74.5 107.5 72.5 105.5 C73.5 110.5 79 114 86.5 114 C92.5 114 97 112.5 100 111.5',
  curl: 'M73.5 106.5 Q67.5 104.5 68.5 99 Q70 94.5 74.5 96 Q77 97.5 75 100.5',
  curlWidth: 3.4,
  twitch: 3.2,
  // How far the whole moustache sits below where its paths are written.
  drop: 2,
  colour: null, // ink, like his eyes and brows
}

const mirror = (d) => d.replace(/(-?\d*\.?\d+)\s+(-?\d*\.?\d+)/g, (_, x, y) => `${+(200 - x).toFixed(2)} ${y}`)
// Moves every point of a path drawn with absolute commands only: `to(x, y)` gives where the point goes.
const reshape = (d, to) => d.replace(/(-?\d*\.?\d+)\s+(-?\d*\.?\d+)/g, (_, x, y) => to(+x, +y).map((v) => +v.toFixed(2)).join(' '))

/**
 * The moustache as features, `t` into a twitch: 0 at rest, 1 with its tips up. Only the tips move; its middle stays put.
 * @returns {(string | number)[][]} features of FACES
 */
export function moustache(t = 0) {
  // How far a point is from his middle, as a share of the moustache's half width, squared: the tips move, the middle does not.
  const lift = (x, y) => { const far = Math.min(Math.abs(x - 100) / 28, 1.2) ** 2; return [x + Math.sign(100 - x) * 0.6 * t * far, y + MOUSTACHE.drop - MOUSTACHE.twitch * t * far] }
  const right = reshape(MOUSTACHE.wing, lift), curl = reshape(MOUSTACHE.curl, lift)
  // Both wings are one shape: his right wing out from his middle and back, then his left one, walked the other way.
  const colour = MOUSTACHE.colour ? [MOUSTACHE.colour] : []
  return [
    ['shape', `${right} ${reverseCubic(mirror(right))} Z`, ...colour, 'moustache'],
    ['path', `${curl} ${mirror(curl)}`, MOUSTACHE.curlWidth, ...colour, 'moustache_curls'],
  ]
}

// A path of one M and cubic curves, walked the other way, without its M: so a second wing can follow the first.
function reverseCubic(d) {
  const numbers = d.match(/-?\d*\.?\d+/g).map(Number), points = []
  for (let i = 0; i < numbers.length; i += 2) points.push([numbers[i], numbers[i + 1]])
  // points: start, then (c1, c2, end) per curve.
  const curves = []
  for (let i = 1; i < points.length; i += 3) curves.push([points[i - 1], points[i], points[i + 1], points[i + 2]])
  const at = (p) => `${p[0]} ${p[1]}`
  return curves.reverse().map(([s, c1, c2]) => `C${at(c2)} ${at(c1)} ${at(s)}`).join(' ')
}

// The two faces, without the moustache. Left and right are Poulpy's own, as in FACES.
const BLUSH = FACES.cocky.filter(([kind]) => kind === 'cheek')
export const CHEF_FACES = {
  // FACES.cocky with its eyes a little higher, to make room, and its smirk moved down under the moustache.
  cocky: [
    ...FACES.cocky.filter(([kind, ...v]) => kind !== 'cheek' && v[v.length - 1] !== 'mouth').map(([kind, ...v]) => [kind, ...v.map((value, i) => (typeof value === 'number' && (kind === 'curve' || kind === 'lid' ? i % 2 === 1 && i < (kind === 'curve' ? 6 : 4) : i === 1) ? value - 5 : value))]),
    ['curve', 89, 122, 103, 129, 115, 117, 4.5, 'mouth'],
    ...BLUSH,
  ],
  // A grin from cheek to cheek, teeth clenched, eyes wide and both brows up: nothing can go wrong in his kitchen.
  grin: [
    ['eye', 80, 91, 8.5, 10], ['eye', 120, 91, 8.5, 10],
    ['curve', 67, 75, 79, 67, 91, 72, 4.5, 'brow_right'], ['curve', 109, 72, 121, 67, 133, 75, 4.5, 'brow_left'],
    ['shape', 'M70 108 Q100 119 130 108 Q127 129 100 131 Q73 129 70 108 Z', '#FFFFFF', 'teeth'],
    ['path', 'M70 108 Q100 119 130 108 Q127 129 100 131 Q73 129 70 108 Z', 3.5, 'mouth'],
    ['path', 'M72 116 Q100 126 128 116', 2.5, 'teeth_line'],
    ['path', 'M86 114.5 L86 129 M100 116.5 L100 131 M114 114.5 L114 129', 2, 'teeth_gaps'],
    ['cheek', 61, 106, 7, 4.5, '#F49A6B', 0.75], ['cheek', 139, 106, 7, 4.5, '#F49A6B', 0.75],
  ],
}

/** The mood the cook is drawn in: proud, on the blue side. */
export const CHEF_MOOD = 'proud'

/** His face as a cook, `twitch` into a moustache twitch (0 at rest, 1 tips up): one of CHEF_FACES and the moustache over it. */
export const chefFace = (face = 'cocky', twitch = 0) => [...CHEF_FACES[face], ...moustache(twitch)]

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
 * @param {{ face?: 'cocky' | 'grin' | null, twitch?: number, tones: Record<string, string>, colours: { blue: string, ember: string, ink: string }, faceOnly?: boolean }} options
 *   `face` picks one of CHEF_FACES (null for none, for whoever draws his face apart while he moves) and `twitch` how far
 *   into a moustache twitch he is. `tones` are the `mood` tokens of design/tokens.json and `colours` its `color.poulpy`
 *   tokens. He is always proud, on the blue side (CHEF_MOOD). `faceOnly` builds his face alone, without his head or toque.
 * @returns What buildPoulpy3d returns, with the toque among his parts (it goes with his head) and `height` up to its top.
 */
export function buildChef({ face = 'cocky', twitch = 0, tones, colours, faceOnly = false }) {
  const model = buildPoulpy3d({ mood: CHEF_MOOD, tone: tones[CHEF_MOOD], features: face ? chefFace(face, twitch) : [], colours, faceOnly })
  // The moustache lies over his mouth, so it is lifted a hair further off his skin than the teeth under it.
  for (const part of model.parts) if (part.name === 'moustache') part.positions = part.positions.map((v, i) => v + part.normals[i] * 0.008)
  if (faceOnly) return model
  const hat = toque()
  Object.assign(model.materials, hat.materials)
  model.parts.push(...hat.parts)
  return { ...model, height: Math.max(model.height, hat.top), headTilt: 0 }
}
