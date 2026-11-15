// Generates the pictures of Poulpy in 3D: the six moods and the twelve poses, painted from the model.
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

// The unfiltered rows of a PNG written by pngOf, or null for any other PNG.
function rows(buf) {
  let at = 8, data = []
  while (at < buf.length) {
    const length = buf.readUInt32BE(at), type = buf.toString('latin1', at + 4, at + 8)
    if (type === 'IDAT') data.push(buf.subarray(at + 8, at + 8 + length))
    at += 12 + length
  }
  const raw = inflateSync(Buffer.concat(data)), stride = FRAME.width * 4 + 1
  return raw.length === stride * FRAME.height ? raw : null
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
