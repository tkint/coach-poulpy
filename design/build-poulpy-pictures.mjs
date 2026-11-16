// Generates the pictures of Poulpy in 3D: the six moods, the twelve poses, the loader and the app icons, painted from the model.
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
import { buildPoulpyPose, CONFETTI, POSES } from './poulpy-3d/poses.mjs'
import { paintPoulpy, pngOf } from './poulpy-3d/picture.mjs'
import { armJoints, armSkin, createCheer, createFollow, createJump, posePoulpy } from './poulpy-3d/motion.mjs'

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

// The loader, frame by frame: Poulpy seen from underneath, each tentacle wider and shorter than in the other pictures so that
// neighbours touch at the base, loose on the arm springs of motion.mjs, and only his outline drawn, in the brand blue and
// see-through. There are two versions, and each is FRAMES pictures of one cycle: how the tentacles hang at that moment, relative
// to his body. The apps turn the body themselves (LoaderSwing) and show the picture of the moment, so his turning stays smooth.
//   swing      he swings from -180 to 180 degrees and back, once per cycle: all the frames in one picture, FRAMES in columns by rows
//   clockwise  he only turns clockwise, steadily, so his tentacles trail the same way all the time: one picture
// Both are in design/poulpy-3d/loader, the one place every platform reads them from (the website, Android's assets and iOS's bundle).
const LOADER_DIR = 'design/poulpy-3d/loader'
const LOADER = { frames: 48, side: 240, columns: 8, line: 0.045, arms: [0.44, 0.3, 0.5], softness: 2.4, versions: ['swing', 'clockwise'] }
// Where a point of an arm goes when its bends are `angles` (each relative to the one before), as armSkin shares it out.
const spin = ([x, y, z], [cx, cz], a) => { const dx = x - cx, dz = z - cz, c = Math.cos(a), s = Math.sin(a); return [cx + dx * c + dz * s, y, cz - dx * s + dz * c] }
function bent(part, arm, angles) {
  const joints = armJoints(), skin = armSkin(arm, part.positions), positions = new Array(part.positions.length), normals = new Array(part.normals.length)
  for (let p = 0; p < part.positions.length / 3; p++) {
    const v = [0, 1, 2].map((k) => part.positions[p * 3 + k]), n = [0, 1, 2].map((k) => part.normals[p * 3 + k])
    const at = [0, 0, 0], way = [0, 0, 0]
    for (let s = 0; s < 4; s++) {
      const w = skin.weights[p * 4 + s]
      if (!w) continue
      let q = v, d = n
      // Share s follows bends s, s - 1, ... 1: the outer one first.
      for (let i = s; i >= 1; i--) { const j = joints[arm * 3 + i - 1].at; q = spin(q, [j[0], j[2]], angles[i - 1]); d = spin(d, [0, 0], angles[i - 1]) }
      for (let k = 0; k < 3; k++) { at[k] += q[k] * w; way[k] += d[k] * w }
    }
    for (let k = 0; k < 3; k++) { positions[p * 3 + k] = at[k]; normals[p * 3 + k] = way[k] }
  }
  return { ...part, positions, normals }
}
// His outline in the given pose: the shape seen from below, rounded, as a line of the brand blue.
function loaderFrame(model, pose) {
  const N = LOADER.side, [r, g, b] = hex(colours.blue)
  const parts = model.parts.filter((p) => /^(head|arm_)/.test(p.name)).map((p) => { const m = /^arm_(\d+)/.exec(p.name); return m ? bent(p, +m[1] - 1, pose.arms[+m[1] - 1]) : p })
  const px = paintPoulpy({ ...model, parts }, { width: N, height: N, turn: 0, tilt: -89.9, distance: 7.5, centre: 0.2, smooth: 4 })
  const shape = rounded(Uint8Array.from({ length: N * N }, (_, i) => (px[i * 4 + 3] >= 128 ? 1 : 0)), { convex: 0.012 * N, concave: 0.02 * N }, N)
  const hollow = shrink(shape, LOADER.line * N, N), line = Uint8Array.from(shape, (v, i) => (v && !hollow[i] ? 1 : 0)), edge = inside(line, N)
  const out = new Uint8Array(N * N * 4)
  for (let i = 0; i < N * N; i++) { out[i * 4] = r; out[i * 4 + 1] = g; out[i * 4 + 2] = b; out[i * 4 + 3] = Math.round(255 * Math.min(Math.max(edge[i] - 0.5, 0), 1)) }
  return out
}
// One cycle of a version, sampled: how the tentacles hang at each of the FRAMES moments. The springs go round three times first, so they have found their rhythm.
function loaderPoses(version) {
  const follow = createFollow({ softness: LOADER.softness }), step = 1 / LOADER.frames, ease = (x) => x * x * (3 - 2 * x), poses = []
  for (let i = -3 * LOADER.frames; i < LOADER.frames; i++) {
    const phase = (((i % LOADER.frames) + LOADER.frames) % LOADER.frames) / LOADER.frames, cycle = Math.floor(i / LOADER.frames)
    // The cycle is one second long here: only how the tentacles hang matters, and that does not change with the time it takes.
    const yaw = version === 'swing' ? Math.PI * (2 * ease(1 - Math.abs(2 * phase - 1)) - 1) : 2 * Math.PI * (cycle + phase)
    const pose = follow.advance(step * LOADER_SECONDS, yaw)
    if (i >= 0) poses.push(pose)
  }
  return poses
}
const LOADER_SECONDS = 3.6
{
  const model = buildPoulpy3d({ mood: 'happy', tone: tokens.mood.happy, colours, armSize: LOADER.arms }), N = LOADER.side
  const rows = Math.ceil(LOADER.frames / LOADER.columns), sheet = new Uint8Array(LOADER.columns * N * rows * N * 4)
  loaderPoses('swing').forEach((pose, i) => {
    const pixels = loaderFrame(model, pose), left = (i % LOADER.columns) * N, top = Math.floor(i / LOADER.columns) * N
    for (let y = 0; y < N; y++) sheet.set(pixels.subarray(y * N * 4, (y + 1) * N * 4), ((top + y) * LOADER.columns * N + left) * 4)
  })
  files[`${LOADER_DIR}/swing.png`] = pngOf(sheet, LOADER.columns * N, rows * N)
  // Turning steadily he holds one shape: the tentacles trail as far as the springs let them, and stay there.
  files[`${LOADER_DIR}/clockwise.png`] = pngOf(loaderFrame(model, loaderPoses('clockwise')[0]), N, N)
}

// The moves of Home: a tap on his picture plays his jump, leaning to one side or the other. Frame by frame, with the same shot as his
// picture, on a canvas that reaches `pad` further each way so the jump is not cut off. One sheet per mood and move, `columns` across, in
// design/poulpy-3d/moves. The numbers are also in PoulpyMoves.kt, which the build checks.
const MOVES = { dir: 'design/poulpy-3d/moves', frames: 30, fps: 24, columns: 6, picture: 64, pad: 24, scale: 3 }
// Which moves each mood has, and how each is made. `left` and `right` are his little jump leaning to that side (a positive lean takes his
// head to the right of the picture); `spin` is the high jump with a full turn anticlockwise, as the owner tuned it on the workshop's
// "Jumping around" page: height 1, heavy arms, one step of exactly 360 degrees (no wiggle).
const MOVE_KINDS = {
  left: { moods: ['happy', 'proud'], make: () => { const jump = createJump(); jump.lean = -Math.abs(jump.lean); return jump } },
  right: { moods: ['happy', 'proud'], make: () => { const jump = createJump(); jump.lean = Math.abs(jump.lean); return jump } },
  spin: { moods: ['happy'], make: () => { const jump = createJump({ stride: 360 / 24, wiggle: false }); jump.height = 1; jump.hang = 4; return jump } },
}
{
  const kotlin = readFileSync(join(root, 'shared/src/commonMain/kotlin/ch/coachpoulpy/shared/PoulpyMoves.kt'), 'utf8')
  const sizes = { width: Math.round(((MOVES.picture * FRAME.width) / FRAME.height + 2 * MOVES.pad) * MOVES.scale), height: (MOVES.picture + 2 * MOVES.pad) * MOVES.scale }
  for (const [name, value] of [['FRAMES', MOVES.frames], ['FPS', MOVES.fps], ['COLUMNS', MOVES.columns], ['FRAME_WIDTH', sizes.width], ['FRAME_HEIGHT', sizes.height], ['PICTURE_WIDTH', Math.round((MOVES.picture * FRAME.width) / FRAME.height * MOVES.scale)], ['PICTURE_HEIGHT', MOVES.picture * MOVES.scale]]) {
    if (!new RegExp(`const val ${name} = ${value}\\b`).test(kotlin)) { console.error(`PoulpyMoves.kt: ${name} must be ${value}, as in design/build-poulpy-pictures.mjs.`); process.exit(1) }
  }
  const rowsOf = Math.ceil(MOVES.frames / MOVES.columns), s = MOVES.scale
  const height = (MOVES.picture + 2 * MOVES.pad) * s, width = Math.round(((MOVES.picture * FRAME.width) / FRAME.height + 2 * MOVES.pad) * s)
  // Grown by the padding, the view shows the same size of him as the picture does (SHOT, picture's field of 28 degrees).
  const field = (2 * Math.atan(((MOVES.picture + 2 * MOVES.pad) / MOVES.picture) * Math.tan((14 * Math.PI) / 180)) * 180) / Math.PI
  for (const mood of ['happy', 'proud']) {
    const model = buildPoulpy3d({ mood, tone: tokens.mood[mood], colours })
    for (const [kind, { moods, make }] of Object.entries(MOVE_KINDS)) {
      if (!moods.includes(mood)) continue
      const jump = make(), sheet = new Uint8Array(MOVES.columns * width * rowsOf * height * 4)
      for (let i = 0; i < MOVES.frames; i++) {
        const pose = i ? jump.advance(1 / MOVES.fps) : jump.pose(), { model: posed, placed } = posePoulpy(model, pose)
        const pixels = paintPoulpy(posed, { width, height, ...SHOT, field, placed, gather: 1 - Math.min(pose.lift * 0.4, 0.3) })
        const left = (i % MOVES.columns) * width, top = Math.floor(i / MOVES.columns) * height
        for (let y = 0; y < height; y++) sheet.set(pixels.subarray(y * width * 4, (y + 1) * width * 4), ((top + y) * MOVES.columns * width + left) * 4)
      }
      files[`${MOVES.dir}/mood_${mood}_${kind}.png`] = pngOf(sheet, MOVES.columns * width, rowsOf * height)
    }
  }
}

// The champion's celebration at the end of a session: he jumps for joy three times with his medal and confetti, then settles on his still
// picture. The same shot as the still (pose_champion), on a canvas `pad` pixels bigger each way for the confetti, in sheets of `perSheet` frames
// (`columns` across) so no sheet is wider or taller than a phone's GPU takes. The last `fade` seconds the confetti shrinks away, and the last frame is his still.
// The numbers are also in PoulpyCheer.kt, which the build checks.
const CHEER = { frames: 72, fps: 20, columns: 6, perSheet: 24, picture: 252, pad: 110, fade: 0.7 }
{
  const kotlin = readFileSync(join(root, 'shared/src/commonMain/kotlin/ch/coachpoulpy/shared/PoulpyCheer.kt'), 'utf8')
  const picture = { width: Math.round((CHEER.picture * FRAME.width) / FRAME.height), height: CHEER.picture }
  const size = { width: picture.width + 2 * CHEER.pad, height: picture.height + 2 * CHEER.pad }
  for (const [name, value] of [['FRAMES', CHEER.frames], ['FPS', CHEER.fps], ['COLUMNS', CHEER.columns], ['SHEET_FRAMES', CHEER.perSheet], ['FRAME_WIDTH', size.width], ['FRAME_HEIGHT', size.height], ['PICTURE_WIDTH', picture.width], ['PICTURE_HEIGHT', picture.height]]) {
    if (!new RegExp(`const val ${name} = ${value}\\b`).test(kotlin)) { console.error(`PoulpyCheer.kt: ${name} must be ${value}, as in design/build-poulpy-pictures.mjs.`); process.exit(1) }
  }
  const field = (2 * Math.atan((size.height / picture.height) * Math.tan((14 * Math.PI) / 180)) * 180) / Math.PI
  const model = buildPoulpyPose({ pose: 'champion', effects: false, tones: tokens.mood, colours })
  const confetti = CONFETTI.colours.map((colour) => (colour === 'ember' ? colours.ember : colour))
  const cheer = createCheer(), rows = Math.ceil(CHEER.perSheet / CHEER.columns)
  const sheets = Array.from({ length: CHEER.frames / CHEER.perSheet }, () => new Uint8Array(CHEER.columns * size.width * rows * size.height * 4))
  for (let i = 0; i < CHEER.frames; i++) {
    const last = i === CHEER.frames - 1
    // The last frame is exactly his still picture: standing, the medal at rest, no confetti.
    const pose = last ? { ...createCheer().pose(), confetti: [] } : i ? cheer.advance(1 / CHEER.fps) : cheer.pose()
    pose.confettiScale = Math.min((CHEER.frames - 1 - i) / CHEER.fps / CHEER.fade, 1)
    const { model: posed, placed } = posePoulpy(model, pose, { confetti })
    const pixels = paintPoulpy(posed, { ...size, ...SHOT, field, placed, gather: 1 - Math.min((pose.lift ?? 0) * 0.4, 0.3) })
    const sheet = sheets[Math.floor(i / CHEER.perSheet)], at = i % CHEER.perSheet, x = (at % CHEER.columns) * size.width, y = Math.floor(at / CHEER.columns) * size.height
    for (let row = 0; row < size.height; row++) sheet.set(pixels.subarray(row * size.width * 4, (row + 1) * size.width * 4), ((y + row) * CHEER.columns * size.width + x) * 4)
  }
  sheets.forEach((sheet, n) => { files[`${MOVES.dir}/pose_champion_cheer_${n + 1}.png`] = pngOf(sheet, CHEER.columns * size.width, rows * size.height) })
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
for (const [dir, keep] of [[OUT, (f) => f.endsWith('.png')], [LOADER_DIR, (f) => f.endsWith('.png')], [ANDROID, (f) => /^poulpy_(mood|pose|loader)_.*\.png$/.test(f)], [IOS, (f) => /^Poulpy(Mood|Pose|Loader).*\.imageset$/.test(f)]]) {
  for (const f of list(dir, keep)) if (!Object.keys(files).some((p) => p.startsWith(`${dir}/${f}`))) { console.error(`Not generated any more: ${dir}/${f}`); stale++ }
}
if (stale) { console.error('Run `node design/build-poulpy-pictures.mjs` and commit the result.'); process.exit(1) }
console.log(check ? 'Poulpy pictures are up to date.' : `Wrote ${Object.keys(files).length} pictures.`)
