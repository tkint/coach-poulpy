// Poulpy with a jetpack, a look: a draft for the owner to watch, not in the apps. He wears a little rocket pack on his back and zooms from
// side to side, grinning, while his tentacles hang and trail under him. Built on the shape and the faces of model.mjs; how he flies is
// ZOOM in motion.mjs. The workshop's Brand > Poulpy looks > Jetpack page draws him live.
//
// The pack is solid, part of him: two little rockets on a plate across the back of his head, a nose, a stripe and a fin on each and a
// nozzle under each. The flames and the smoke are comic effects: flat, and kept facing the viewer by whoever draws him. He never lifts a
// tentacle (brand decision 8): flying, they only hang and trail.
// To change it, edit JETPACK below (and ZOOM in motion.mjs) and look at the page.
import { buildPoulpy3d, egg, join, linear, tube } from '../model.mjs'

const NIGHT = '#1574B0', STEEL = '#56687A'

export const JETPACK = {
  // He is happy, on the blue side: flying is fun.
  mood: 'happy',
  // His grin, which moves (see jetpackFace below), in the drawings' coordinates (see FACES in model.mjs). Each `[hovering, dashing]` pair
  // is blended by how hard he pushes: `smile` is the open smile's half width and depth, `corners` how high its corners sit, `eyes` the
  // eyes' height (they narrow as his cheeks push up), `cheeks` the cheeks' height and half width.
  grin: { smile: [[12, 12], [15.5, 15.5]], corners: [107, 104.5], eyes: [11, 8], cheeks: [[112, 8], [108, 10]] },
  // The two tanks, his right one at -x: where the foot of each stands (out, up, back), how long and thick it is, how tall its round nose
  // is, and how far it leans in at the top, in degrees, so its nozzle and its flame point down and out, clear of his tentacles.
  tank: { x: 1.1, y: 0.56, z: -0.55, length: 0.78, radius: 0.3, nose: 0.28, splay: 22 },
  // Colours: white tanks on a white plate across his back, deep blue noses, stripes and fins, steel nozzles and straps.
  colours: { tank: '#F2F6FA', trim: NIGHT, nozzle: STEEL },
  // The flames: three flat layers from the nozzle out, outermost first: colour, length and half width.
  flames: [['#FF9F2E', 1.3, 0.26], ['#FFD34D', 0.9, 0.175], ['#FFF6D6', 0.48, 0.095]],
  // The smoke: puffs of the fuming pose's cloud, with its edge.
  smoke: { colour: '#E6EEF4', edge: '#B6C4D0', width: 0.035 },
}

const { tank } = JETPACK
const RAD = Math.PI / 180

const between = (a, b, t) => a + (b - a) * t
/**
 * His face as he flies, as features for buildPoulpy3d. From `t` 0 to 1 he goes from hovering to dashing: the smile opens wider and its
 * corners rise, his cheeks push up and his eyes narrow above them. He does not blink yet: a natural, rare blink may come later.
 */
export function jetpackFace(t) {
  const { grin } = JETPACK, push = Math.max(0, Math.min(t, 1))
  const at = ([a, b]) => between(a, b, push)
  const w = at(grin.smile.map((s) => s[0])), d = at(grin.smile.map((s) => s[1])), y = at(grin.corners)
  const tall = at(grin.eyes), [cheekY, cheekX] = [at(grin.cheeks.map((c) => c[0])), at(grin.cheeks.map((c) => c[1]))]
  return [
    ['eye', 80, 94 - push, 9.5, tall], ['eye', 120, 94 - push, 9.5, tall],
    ['shape', `M${100 - w} ${y} Q100 ${y + 4} ${100 + w} ${y} Q${100 + w - 2} ${y + d} 100 ${y + d} Q${100 - w + 2} ${y + d} ${100 - w} ${y} Z`, 'mouth'],
    ['oval', 100, y + d - 4, w * 0.55, 3 + push, '#F07A7A', 'tongue'],
    ['cheek', 64, cheekY, cheekX, 4.5 + push, '#F49A6B', 0.75], ['cheek', 136, cheekY, cheekX, 4.5 + push, '#F49A6B', 0.75],
  ]
}
/**
 * Where each flame comes out, his right then his left, as he stands: `at`, the middle of the end of the nozzle, and `way`, the way the
 * flame points from there, down and a little outwards.
 */
export const NOZZLES = [-1, 1].map((sgn) => {
  const way = [sgn * Math.sin(tank.splay * RAD), -Math.cos(tank.splay * RAD), 0]
  return { at: [sgn * tank.x + way[0] * 0.26, tank.y + way[1] * 0.26, tank.z], way }
})

// The solid pack: its parts, all named `jetpack…`, and their colours. It rides on his back and turns with him.
function pack() {
  const c = JETPACK.colours, tanks = [], trims = [], nozzles = [], straps = []
  NOZZLES.forEach(({ way }, i) => {
    const sgn = i ? 1 : -1, foot = [sgn * tank.x, tank.y, tank.z]
    // A point `up` along the tank from its foot, moved by `by`.
    const at = (up, by = [0, 0, 0]) => foot.map((v, k) => v - way[k] * up + by[k])
    tanks.push(tube([at(0), at(tank.length)], tank.radius, 24))
    // A round nose on top, like a little rocket's, a stripe round the tank and a fin behind its foot.
    const top = tank.length, n = tank.nose
    trims.push(tube([at(top), at(top + n * 0.5), at(top + n * 0.85), at(top + n)], [tank.radius * 1.02, tank.radius * 0.82, tank.radius * 0.45, 0.06], 24))
    trims.push(tube([at(top * 0.45), at(top * 0.45 + 0.1)], tank.radius * 1.07, 24))
    trims.push(egg([0.035, 0.2, 0.16], { around: 16, down: 10, at: at(0.1, [0, 0, -(tank.radius + 0.07)]) }))
    // A nozzle that flares out under the tank, the way its flame goes.
    nozzles.push(tube([at(-0.02), at(-0.13), at(-0.26)], [0.15, 0.14, 0.19], 20))
    // A strap from the tank round to the plate on his back.
    straps.push(tube([at(0.45, [-sgn * 0.12, 0, -0.05]), [sgn * 0.72, 1.0, -1.02], [sgn * 0.3, 1.0, -1.2]], 0.07))
  })
  return {
    materials: { jetpack_tank: { colour: linear(c.tank) }, jetpack_trim: { colour: linear(c.trim) }, jetpack_nozzle: { colour: linear(c.nozzle) } },
    parts: [
      { name: 'jetpack_tanks', material: 'jetpack_tank', ...join(...tanks, egg([0.5, 0.42, 0.2], { around: 28, down: 14, at: [0, 0.98, -1.16] })) },
      { name: 'jetpack_trim', material: 'jetpack_trim', ...join(...trims) },
      { name: 'jetpack_nozzles', material: 'jetpack_nozzle', ...join(...nozzles, ...straps) },
    ],
  }
}

// --- flat effects ----------------------------------------------------------------------------------

// A filled outline as a fan of triangles from its middle, facing +z, `z` in front.
function flat(points, z = 0) {
  // Triangles face the viewer when the outline runs anticlockwise.
  if (points.reduce((sum, [x, y], i) => { const [nx, ny] = points[(i + 1) % points.length]; return sum + x * ny - nx * y }, 0) < 0) points = [...points].reverse()
  const mid = [0, 1].map((k) => points.reduce((sum, q) => sum + q[k], 0) / points.length), all = [mid, ...points]
  return { positions: all.flatMap(([x, y]) => [x, y, z]), normals: all.flatMap(() => [0, 0, 1]), indices: points.flatMap((_, i) => [0, 1 + i, 1 + ((i + 1) % points.length)]) }
}

/**
 * One flame, flat and facing +z: a drop hanging from the nozzle at (0, 0), pointing down, `length` long and `width` wide at its widest.
 * Its top is as wide as the nozzle, it swells a third of the way down and comes to a point.
 */
export function flameShape(length, width, z = 0, n = 24) {
  const side = Array.from({ length: n + 1 }, (_, i) => { const t = i / n; return [width * Math.sin(Math.PI * (0.25 + 0.75 * t)) ** 0.75, -length * t] })
  return flat([...side, ...side.slice(1, -1).reverse().map(([x, y]) => [-x, y])], z)
}
/** A round puff of smoke, flat and facing +z, of radius 1 (1 + `edge` for the edge drawn behind it). */
export function puffShape(edge = 0, n = 28) {
  return flat(Array.from({ length: n }, (_, i) => [(1 + edge) * Math.cos((i / n) * Math.PI * 2), (1 + edge) * Math.sin((i / n) * Math.PI * 2)]))
}

/**
 * Builds Poulpy with his jetpack: buildPoulpy3d in the look's mood and face, with the pack's parts (named `jetpack…`) and, unless `flames`
 * is false, one flat flame of each layer per nozzle (named `jetpack_flame_<nozzle>_<layer>`, both from 1), drawn hanging from (0, 0) and
 * facing +z: whoever draws him puts each at its nozzle, keeps it facing the viewer and pointing its nozzle's way, and stretches it as it flickers.
 * `face` gives him other features than his hovering grin, `[]` none, for whoever changes his face as he flies (jetpackFace).
 * @param {{ tones: Record<string, string>, colours: { blue: string, ember: string, ink: string }, flames?: boolean, face?: (string | number)[][] }} options
 */
export function buildPoulpyJetpack({ tones, colours, flames = true, face = jetpackFace(0) }) {
  const model = buildPoulpy3d({ mood: JETPACK.mood, tone: tones[JETPACK.mood], features: face, colours })
  const made = pack()
  Object.assign(model.materials, made.materials)
  model.parts.push(...made.parts)
  if (flames) {
    JETPACK.flames.forEach(([colour], layer) => { model.materials[`jetpack_flame_${layer + 1}`] = { colour: linear(colour), flat: true } })
    NOZZLES.forEach((_, nozzle) => JETPACK.flames.forEach(([, length, width], layer) => {
      model.parts.push({ name: `jetpack_flame_${nozzle + 1}_${layer + 1}`, material: `jetpack_flame_${layer + 1}`, ...flameShape(length, width, layer * 0.004) })
    }))
  }
  return model
}
