// Generates the pictures of Poulpy in 3D: the six moods, the twelve poses, the loader and the app icons, painted from the model.
//   node design/build-poulpy-pictures.mjs          write the pictures
//   node design/build-poulpy-pictures.mjs --check  fail if a picture is out of date (used by CI)
//
//   design/poulpy-3d/pictures/<name>.png   one picture each, 744 by 678 pixels: the 3x of the drawings' 248 by 226 frame,
//                                          so a picture can take a drawing's place without moving anything
// The same pictures go to the apps, under the names their screens already use:
//   app-android/src/main/res/drawable-nodpi/poulpy_<name>.png                  Android, drawn at whatever size the screen gives
//   app-ios/CoachPoulpy/Assets.xcassets/Poulpy/Poulpy<Name>.imageset           iOS, a 3x image
// The apps and the website take these (see docs/poulpy-3d.md); paintPoulpy in poulpy-3d/picture.mjs paints them in plain
// JavaScript, so every machine writes the same pixels.
import { readFileSync, writeFileSync, mkdirSync, existsSync, readdirSync } from 'node:fs'
import { dirname, join } from 'node:path'
import { inflateSync } from 'node:zlib'
import { fileURLToPath } from 'node:url'
import { buildPoulpy3d, MOODS } from './poulpy-3d/model.mjs'
import { buildPoulpyPose, POSES } from './poulpy-3d/poses.mjs'
import { paintPoulpy, pngOf } from './poulpy-3d/picture.mjs'

const root = join(dirname(fileURLToPath(import.meta.url)), '..')
const tokens = JSON.parse(readFileSync(join(root, 'design/tokens.json'), 'utf8'))
const colours = tokens.color.poulpy
const hex = (c) => [1, 3, 5].map((i) => parseInt(c.slice(i, i + 2), 16))
const OUT = 'design/poulpy-3d/pictures'

// The frame of every picture, and where the viewer stands for all of them: close enough to fill it, looking at the
// height that keeps what rises above him (a nightcap, confetti) and the floor shadow both inside.
const FRAME = { width: 744, height: 678 }
const SHOT = { tilt: 8, distance: 9.4, centre: 1.35, shadow: tokens.color.lagoon[900] }
const snake = (name) => name.replace(/[A-Z]/g, (c) => `_${c.toLowerCase()}`)

const pictures = {}
for (const mood of Object.keys(MOODS)) pictures[`mood_${mood}`] = buildPoulpy3d({ mood, tone: tokens.mood[mood], colours })
for (const pose of Object.keys(POSES)) pictures[`pose_${snake(pose)}`] = buildPoulpyPose({ pose, tones: tokens.mood, colours })
const pascal = (name) => name.split('_').map((p) => p.charAt(0).toUpperCase() + p.slice(1)).join('')
const ANDROID = 'app-android/src/main/res/drawable-nodpi', IOS = 'app-ios/CoachPoulpy/Assets.xcassets/Poulpy'
const files = { [`${IOS}/Contents.json`]: JSON.stringify({ info: { author: 'xcode', version: 1 } }, null, 2) + '\n' }
for (const [name, model] of Object.entries(pictures)) {
  const png = pngOf(paintPoulpy(model, { ...FRAME, ...SHOT }), FRAME.width, FRAME.height), asset = `Poulpy${pascal(name)}`
  files[`${OUT}/${name}.png`] = png
  files[`${ANDROID}/poulpy_${name}.png`] = png
  files[`${IOS}/${asset}.imageset/${asset}.png`] = png
  files[`${IOS}/${asset}.imageset/Contents.json`] = JSON.stringify({ images: [{ filename: `${asset}.png`, idiom: 'universal', scale: '3x' }], info: { author: 'xcode', version: 1 } }, null, 2) + '\n'
}

// The loader: just his outline seen from straight above, as the old loader was: the flower of his eight loose tentacles round
// the head, with no line inside, in the brand blue, see-through, inside the circle he turns in. The edge is rounded (no points)
// and the line is bold enough for the bases of neighbouring tentacles to touch. The picture is ONE tentacle, standing up from
// the middle of the square, because the apps draw eight of them and let each swing from its base as he turns (LoaderSwing).
const LOADER_SIDE = 600, LOADER_LINE = 0.06 * LOADER_SIDE
// How far each pixel inside a shape is from outside it (Felzenszwalb and Huttenlocher's distance transform, in pixels).
function inside(shape, size) {
  const far = 1e20, f = Float64Array.from(shape, (v) => (v ? far : 0)), out = new Float64Array(size * size), d = new Float64Array(size), z = new Float64Array(size + 1), v = new Int32Array(size)
  const line = (get, set) => {
    let k = 0; v[0] = 0; z[0] = -far; z[1] = far
    for (let q = 1; q < size; q++) {
      let s
      for (;;) { s = ((get(q) + q * q) - (get(v[k]) + v[k] * v[k])) / (2 * q - 2 * v[k]); if (s > z[k]) break; k-- }
      k++; v[k] = q; z[k] = s; z[k + 1] = far
    }
    k = 0
    for (let q = 0; q < size; q++) { while (z[k + 1] < q) k++; set(q, (q - v[k]) ** 2 + get(v[k])) }
  }
  for (let x = 0; x < size; x++) { line((y) => f[y * size + x], (y, value) => { out[y * size + x] = value }) }
  for (let y = 0; y < size; y++) { line((x) => out[y * size + x], (x, value) => { d[x] = value }); for (let x = 0; x < size; x++) out[y * size + x] = Math.sqrt(d[x]) }
  return out
}
// Shapes are true where they are, round their points with a disk: grow, shrink, and the two in the other order.
const grow = (shape, by, size) => Uint8Array.from(inside(shape.map((v) => (v ? 0 : 1)), size), (d) => (d <= by ? 1 : 0))
const shrink = (shape, by, size) => Uint8Array.from(inside(shape, size), (d) => (d > by ? 1 : 0))
const rounded = (shape, { convex, concave }, size) => grow(shrink(concave ? shrink(grow(shape, concave, size), concave, size) : shape, convex, size), convex, size)
// The tentacle at the top of his outline, and how far from the middle its base is, as a share of the square.
function loader(model) {
  const [r, g, b] = hex(colours.blue), N = LOADER_SIDE
  // His whole shape from above, head and tentacles as one.
  const px = icon(model, N, { reach: 0.46, tilt: 89.9, only: (name) => name === 'head' || name === 'head_lining' || name.startsWith('arm_'), like: model })
  const flower = rounded(Uint8Array.from({ length: N * N }, (_, i) => (px[i * 4 + 3] >= 128 ? 1 : 0)), { convex: 0.02 * N, concave: 0.05 * N }, N)
  // The hollow he is outlined round: the flower shrunk by the line, its inner points rounded as well.
  const hollow = rounded(shrink(flower, LOADER_LINE, N), { convex: 0.025 * N, concave: 0.025 * N }, N)
  // The line round one tentacle: the eighth of the turn round the top, and its two ends rounded off like the old loader's tails.
  const half = N / 2, ring = Uint8Array.from({ length: N * N }, (_, i) => {
    const x = i % N, y = Math.floor(i / N), angle = Math.atan2(x + 0.5 - half, half - y - 0.5)
    return flower[i] && !hollow[i] && Math.abs(angle) <= Math.PI / 8 + 0.02 ? 1 : 0
  })
  const capped = rounded(ring, { convex: 0.45 * LOADER_LINE }, N), edge = inside(capped, N)
  const out = new Uint8Array(N * N * 4)
  for (let i = 0; i < N * N; i++) { out[i * 4] = r; out[i * 4 + 1] = g; out[i * 4 + 2] = b; out[i * 4 + 3] = Math.round(255 * Math.min(Math.max(edge[i] - 0.5, 0), 1)) }
  // Where the line between two tentacles meets the head: along the eighth turn away from the top, out to where the flower ends.
  let valley = 0
  for (let d = 0; d < half; d += 0.25) { const x = Math.round(half + d * Math.sin(Math.PI / 8)), y = Math.round(half - d * Math.cos(Math.PI / 8)); if (flower[y * N + x]) valley = d }
  // The tentacle bends where its two ends meet the head, halfway between them.
  return { pixels: out, base: 0.5 - (valley * Math.cos(Math.PI / 8)) / N }
}
let loaderBase = 0
{
  const { pixels, base } = loader(pictures.mood_happy)
  loaderBase = base
  const png = pngOf(pixels, LOADER_SIDE, LOADER_SIDE)
  files[`${OUT}/loader_tentacle.png`] = png
  files[`${ANDROID}/poulpy_loader_tentacle.png`] = png
  files[`${IOS}/PoulpyLoaderTentacle.imageset/PoulpyLoaderTentacle.png`] = png
  files[`${IOS}/PoulpyLoaderTentacle.imageset/Contents.json`] = JSON.stringify({ images: [{ filename: 'PoulpyLoaderTentacle.png', idiom: 'universal', scale: '3x' }], info: { author: 'xcode', version: 1 } }, null, 2) + '\n'
}

// The app icons: the same pictures, fitted into the icon's square on the dark theme's background, as the owner asked.
//   iOS      AppIcon.appiconset, AppIcon<Id>.appiconset   1024 px, light, dark and tinted (the light and dark are alike)
//   Android  drawable-nodpi/ic_launcher_[<id>_]foreground.png   the 108 dp layer, with Poulpy inside the 66 dp circle that
//            every launcher shows; the background layer is the same dark colour (drawable/ic_launcher_background.xml, made
//            by build-poulpy.mjs) and the themed layer stays a one-colour silhouette drawn there.
const ICON_FACES = { happy: 'mood_happy', proud: 'mood_proud', annoyed: 'mood_annoyed', grumpy: 'mood_grumpy', champion: 'pose_champion', cocky: 'pose_cocky' }
const ICON_BACKGROUND = tokens.theme.dark.background
const ICON_SIDE = 1024

// Poulpy as big as the icon needs, found by painting him once large and measuring what is covered.
function icon(model, size, { reach, fill, background, tilt = 8, only, like = model }) {
  // `only` paints just some of his parts, framed as `like` (all of him) is, so the pieces of one picture line up.
  const big = 1024, shot = (m) => paintPoulpy(m, { width: big, height: big, tilt, distance: 14, centre: m.height / 2, smooth: 2 })
  const src = only ? shot({ ...model, parts: model.parts.filter((part) => only(part.name)) }) : shot(model), whole = only ? shot(like) : src
  let x0 = big, y0 = big, x1 = -1, y1 = -1
  for (let y = 0; y < big; y++) for (let x = 0; x < big; x++) if (whole[(y * big + x) * 4 + 3] > 8) { x0 = Math.min(x0, x); x1 = Math.max(x1, x); y0 = Math.min(y0, y); y1 = Math.max(y1, y) }
  const cx = (x0 + x1 + 1) / 2, cy = (y0 + y1 + 1) / 2
  let far = 0
  if (reach) for (let y = y0; y <= y1; y++) for (let x = x0; x <= x1; x++) if (whole[(y * big + x) * 4 + 3] > 8) far = Math.max(far, Math.hypot(x + 0.5 - cx, y + 0.5 - cy))
  // How many source pixels one icon pixel covers.
  const k = reach ? far / (reach * size) : Math.max(x1 - x0 + 1, y1 - y0 + 1) / (fill * size)
  const out = new Uint8Array(size * size * 4), bg = background && hex(background)
  const at = (x, y, c) => (x < 0 || y < 0 || x >= big || y >= big ? 0 : src[(y * big + x) * 4 + c])
  for (let y = 0; y < size; y++) for (let x = 0; x < size; x++) {
    // Bilinear, with colour weighed by how opaque each corner is.
    const sx = cx + (x + 0.5 - size / 2) * k - 0.5, sy = cy + (y + 0.5 - size / 2) * k - 0.5
    const ix = Math.floor(sx), iy = Math.floor(sy), fx = sx - ix, fy = sy - iy
    let r = 0, g = 0, b = 0, a = 0
    for (const [dx, dy, w] of [[0, 0, (1 - fx) * (1 - fy)], [1, 0, fx * (1 - fy)], [0, 1, (1 - fx) * fy], [1, 1, fx * fy]]) {
      const al = at(ix + dx, iy + dy, 3) / 255
      r += at(ix + dx, iy + dy, 0) * al * w; g += at(ix + dx, iy + dy, 1) * al * w; b += at(ix + dx, iy + dy, 2) * al * w; a += al * w
    }
    const o = (y * size + x) * 4
    if (bg) { out[o] = Math.round(r + bg[0] * (1 - a)); out[o + 1] = Math.round(g + bg[1] * (1 - a)); out[o + 2] = Math.round(b + bg[2] * (1 - a)); out[o + 3] = 255 }
    else if (a > 0) { out[o] = Math.round(r / a); out[o + 1] = Math.round(g / a); out[o + 2] = Math.round(b / a); out[o + 3] = Math.round(a * 255) }
  }
  return out
}
const grey = (px) => { const out = Uint8Array.from(px); for (let i = 0; i < out.length; i += 4) out[i] = out[i + 1] = out[i + 2] = Math.round(0.299 * px[i] + 0.587 * px[i + 1] + 0.114 * px[i + 2]); return out }
const appearance = (value) => ({ appearances: [{ appearance: 'luminosity', value }] })
for (const [id, name] of Object.entries(ICON_FACES)) {
  const model = pictures[name], standard = id === 'happy', stem = standard ? 'AppIcon' : `AppIcon${pascal(id)}`, set = `app-ios/CoachPoulpy/Assets.xcassets/${stem}.appiconset`
  const square = icon(model, ICON_SIDE, { fill: 0.8, background: ICON_BACKGROUND })
  files[`${set}/${stem}.png`] = pngOf(square, ICON_SIDE, ICON_SIDE)
  files[`${set}/${stem}Dark.png`] = pngOf(square, ICON_SIDE, ICON_SIDE)
  files[`${set}/${stem}Tinted.png`] = pngOf(grey(square), ICON_SIDE, ICON_SIDE)
  files[`${set}/Contents.json`] = JSON.stringify({
    images: [
      { filename: `${stem}.png`, idiom: 'universal', platform: 'ios', size: '1024x1024' },
      { ...appearance('dark'), filename: `${stem}Dark.png`, idiom: 'universal', platform: 'ios', size: '1024x1024' },
      { ...appearance('tinted'), filename: `${stem}Tinted.png`, idiom: 'universal', platform: 'ios', size: '1024x1024' },
    ],
    info: { author: 'xcode', version: 1 },
  }, null, 2) + '\n'
  // 432 px is the 108 dp layer at the finest density; the circle every launcher shows is 66 dp across.
  files[`${ANDROID}/ic_launcher_${standard ? '' : `${id}_`}foreground.png`] = pngOf(icon(model, 432, { reach: (33 / 108) * 0.97 }), 432, 432)
}

// The unfiltered rows of a PNG written by pngOf, or null for any other PNG.
function rows(buf) {
  let at = 8, data = []
  while (at < buf.length) {
    const length = buf.readUInt32BE(at), type = buf.toString('latin1', at + 4, at + 8)
    if (type === 'IDAT') data.push(buf.subarray(at + 8, at + 8 + length))
    at += 12 + length
  }
  const width = buf.readUInt32BE(16), height = buf.readUInt32BE(20)
  const raw = inflateSync(Buffer.concat(data)), stride = width * 4 + 1
  return raw.length === stride * height ? raw : null
}

// A picture is up to date when its pixels match, give or take rounding, whatever compressed them.
function upToDate(full, content) {
  if (!existsSync(full)) return false
  if (typeof content === 'string') return readFileSync(full, 'utf8') === content
  const want = rows(content), have = rows(readFileSync(full))
  return !!have && have.length === want.length && have.every((b, i) => Math.abs(b - want[i]) <= 2)
}

const check = process.argv.includes('--check')
let stale = 0
for (const [path, content] of Object.entries(files)) {
  const full = join(root, path)
  if (check) {
    if (!upToDate(full, content)) { console.error(`Out of date: ${path}`); stale++ }
  } else {
    mkdirSync(dirname(full), { recursive: true })
    writeFileSync(full, content)
  }
}
// A picture of something that is gone must go too.
const list = (dir, keep) => (existsSync(join(root, dir)) ? readdirSync(join(root, dir)).filter(keep) : [])
for (const [dir, keep] of [[OUT, (f) => f.endsWith('.png')], [ANDROID, (f) => /^poulpy_(mood|pose)_.*\.png$/.test(f)], [IOS, (f) => /^Poulpy(Mood|Pose|Loader).*\.imageset$/.test(f)]]) {
  for (const f of list(dir, keep)) if (!Object.keys(files).some((p) => p.startsWith(`${dir}/${f}`))) { console.error(`Not generated any more: ${dir}/${f}`); stale++ }
}
// Where a tentacle bends is a number the apps and the web repeat; they must say what the picture measures.
const said = (4 * Math.round(loaderBase * 10000)) / 4 / 100
for (const [file, pattern] of [['shared/src/commonMain/kotlin/ch/coachpoulpy/shared/LoaderSwing.kt', /BASE = ([\d.]+)f/], ['web/ui/src/theme.css', /transform-origin: 50% ([\d.]+)%/], ['web/storybook/stories/mobile/screens/screens.css', /transform-origin: 50% ([\d.]+)%/]]) {
  const found = readFileSync(join(root, file), 'utf8').match(pattern)?.[1]
  const want = pattern.source.includes('%') ? said.toFixed(2) : (loaderBase).toFixed(4)
  if (found !== want) { console.error(`${file} says the loader's tentacle bends at ${found}, the picture says ${want}.`); stale++ }
}
if (stale) { console.error('Run `node design/build-poulpy-pictures.mjs` and commit the result.'); process.exit(1) }
console.log(check ? 'Poulpy pictures are up to date.' : `Wrote ${Object.keys(files).length} pictures.`)
