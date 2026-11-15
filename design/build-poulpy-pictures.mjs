// Generates the pictures of Poulpy in 3D: the six moods and the twelve poses, painted from the model.
//   node design/build-poulpy-pictures.mjs          write the pictures
//   node design/build-poulpy-pictures.mjs --check  fail if a picture is out of date (used by CI)
//
//   design/poulpy-3d/pictures/<name>.png   one picture each, 744 by 678 pixels: the 3x of the drawings' 248 by 226 frame,
//                                          so a picture can take a drawing's place without moving anything
// The same pictures go to the apps, under the names their screens already use:
//   app-android/src/main/res/drawable-nodpi/poulpy_<name>.png                  Android, drawn at whatever size the screen gives
//   app-ios/CoachPoulpy/Assets.xcassets/Poulpy/Poulpy<Name>.imageset           iOS, a 3x image
// The apps and the website take these (see web/storybook/stories/brand/Poulpy3d.mdx); paintPoulpy in poulpy-3d/picture.mjs paints them in plain
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

// The app icons: the same pictures, fitted into the icon's square on the dark theme's background, as the owner asked.
//   iOS      AppIcon.appiconset, AppIcon<Id>.appiconset   1024 px, light, dark and tinted (the light and dark are alike)
//   Android  drawable-nodpi/ic_launcher_[<id>_]foreground.png   the 108 dp layer, with Poulpy inside the 66 dp circle that
//            every launcher shows; the background layer is the same dark colour (drawable/ic_launcher_background.xml, made
//            by build-poulpy.mjs) and the themed layer stays a one-colour silhouette drawn there.
const ICON_FACES = { happy: 'mood_happy', proud: 'mood_proud', annoyed: 'mood_annoyed', grumpy: 'mood_grumpy', champion: 'pose_champion', cocky: 'pose_cocky' }
const ICON_BACKGROUND = tokens.theme.dark.background
const hex = (c) => [1, 3, 5].map((i) => parseInt(c.slice(i, i + 2), 16))
const ICON_SIDE = 1024

// Poulpy as big as the icon needs, found by painting him once large and measuring what is covered.
function icon(model, size, { reach, fill, background }) {
  const big = 1024, src = paintPoulpy(model, { width: big, height: big, tilt: 8, distance: 14, centre: model.height / 2, smooth: 2 })
  let x0 = big, y0 = big, x1 = -1, y1 = -1
  for (let y = 0; y < big; y++) for (let x = 0; x < big; x++) if (src[(y * big + x) * 4 + 3] > 8) { x0 = Math.min(x0, x); x1 = Math.max(x1, x); y0 = Math.min(y0, y); y1 = Math.max(y1, y) }
  const cx = (x0 + x1 + 1) / 2, cy = (y0 + y1 + 1) / 2
  let far = 0
  if (reach) for (let y = y0; y <= y1; y++) for (let x = x0; x <= x1; x++) if (src[(y * big + x) * 4 + 3] > 8) far = Math.max(far, Math.hypot(x + 0.5 - cx, y + 0.5 - cy))
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
for (const [dir, keep] of [[OUT, (f) => f.endsWith('.png')], [ANDROID, (f) => /^poulpy_(mood|pose)_.*\.png$/.test(f)], [IOS, (f) => /^Poulpy(Mood|Pose).*\.imageset$/.test(f)]]) {
  for (const f of list(dir, keep)) if (!Object.keys(files).some((p) => p.startsWith(`${dir}/${f}`))) { console.error(`Not generated any more: ${dir}/${f}`); stale++ }
}
if (stale) { console.error('Run `node design/build-poulpy-pictures.mjs` and commit the result.'); process.exit(1) }
console.log(check ? 'Poulpy pictures are up to date.' : `Wrote ${Object.keys(files).length} pictures.`)
