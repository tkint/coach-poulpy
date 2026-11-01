// Draws the body map figures (front and back, two builds) and writes them into the mockups and the apps' data.
//   node design/build-body-map.mjs          rewrite the figures
//   node design/build-body-map.mjs --check  fail if a file differs from what this draws
// The muscle outlines come from design/body-map-source/parts.json (see NOTICE.md there). Each muscle is one
// tappable region; the skin is left transparent and there is no face or hair, so no skin tone is implied.
// A build only stretches the figure sideways, at the shoulders or at the hips.
import { readFileSync, writeFileSync } from 'node:fs'
import { dirname, join } from 'node:path'
import { fileURLToPath } from 'node:url'

const root = join(dirname(fileURLToPath(import.meta.url)), '..')
const phone = join(root, 'design/mockups/agreed/phone')
const files = ['profile-pain-sheet.html', 'profile-body-figure-sheet.html']
const source = JSON.parse(readFileSync(join(root, 'design/body-map-source/parts.json'), 'utf8'))

const SRC_W = 375.42
const SCALE = 120 / SRC_W // the figure fills the 120 wide grid; its height is 266 of 268
const CX = SRC_W / 2

// The source's French abbreviations, as our area slug and English name. The last letter is D (right) or G (left).
const NAMES = {
  cou: ['neck', 'neck'], trap: ['trapezius', 'trapezius'], ep: ['deltoid', 'deltoid'], bi: ['biceps', 'biceps'],
  coud: ['elbow', 'elbow'], avb: ['forearm', 'forearm'], poi: ['wrist', 'wrist'], main: ['hand', 'hand'],
  pec: ['pectoral', 'pectoral'], cot: ['serratus', 'ribs'], abd: ['abs', 'abs'], obl: ['obliques', 'obliques'],
  han: ['hip', 'hip'], quad: ['quads', 'quads'], add: ['adductors', 'adductors'], gen: ['knee', 'knee'],
  tib: ['shin', 'shin'], che: ['ankle', 'ankle'], pie: ['foot', 'foot'],
  tri: ['triceps', 'triceps'], sca: ['scapula', 'shoulder blade'], dos: ['back', 'back'], lomb: ['lower-back', 'lower back'],
  fes: ['glutes', 'glutes'], isc: ['hamstrings', 'hamstrings'], mol: ['calves', 'calves'], col: ['spine', 'spine'],
}
// On the back view, the source calls the elbow "cou" and the neck "trap" is the neck-side trapezius; the back has
// no separate neck, so its "cou" is the elbow.
const BACK_NAMES = { ...NAMES, cou: ['elbow', 'elbow'] }

// ---------- path data to absolute cubic curves ----------

function parse(d) {
  let i = 0
  const skip = () => { while (i < d.length && /[\s,]/.test(d[i])) i++ }
  const num = () => {
    skip()
    const m = /^-?(?:\d*\.\d+|\d+\.?)(?:e[-+]?\d+)?/i.exec(d.slice(i))
    i += m[0].length
    return parseFloat(m[0])
  }
  const flag = () => { skip(); return d[i++] === '1' ? 1 : 0 }
  const more = () => { skip(); return i < d.length && !/[A-Za-z]/.test(d[i]) }
  const out = []
  let cmd = ''
  let x = 0, y = 0, sx = 0, sy = 0
  let cubic = null // second control point of the last cubic, for S
  let quad = null // control point of the last quadratic, for T
  while (true) {
    skip()
    if (i >= d.length) break
    if (/[A-Za-z]/.test(d[i])) cmd = d[i++]
    const rel = cmd === cmd.toLowerCase()
    const C = cmd.toUpperCase()
      if (C === 'Z') {
      if (x !== sx || y !== sy) out.push(line(x, y, sx, sy))
      out.push('Z'); x = sx; y = sy; cubic = quad = null
      continue
    }
    do {
      const px = rel ? x : 0
      const py = rel ? y : 0
      let nextCubic = null
      let nextQuad = null
      if (C === 'M') {
        x = num() + px; y = num() + py; sx = x; sy = y; out.push(['M', x, y])
        cmd = rel ? 'l' : 'L'
        cubic = quad = null
        break
      } else if (C === 'L') { const nx = num() + px, ny = num() + py; out.push(line(x, y, nx, ny)); x = nx; y = ny }
      else if (C === 'H') { const nx = num() + px; out.push(line(x, y, nx, y)); x = nx }
      else if (C === 'V') { const ny = num() + py; out.push(line(x, y, x, ny)); y = ny }
      else if (C === 'C' || C === 'S') {
        const c1 = C === 'C' ? [num() + px, num() + py] : cubic ? [2 * x - cubic[0], 2 * y - cubic[1]] : [x, y]
        const c2 = [num() + px, num() + py]
        const e = [num() + px, num() + py]
        out.push(['C', c1[0], c1[1], c2[0], c2[1], e[0], e[1]])
        nextCubic = c2; x = e[0]; y = e[1]
      } else if (C === 'Q' || C === 'T') {
        const q = C === 'Q' ? [num() + px, num() + py] : quad ? [2 * x - quad[0], 2 * y - quad[1]] : [x, y]
        const nx = num() + px, ny = num() + py
        out.push(['C', x + (2 / 3) * (q[0] - x), y + (2 / 3) * (q[1] - y), nx + (2 / 3) * (q[0] - nx), ny + (2 / 3) * (q[1] - ny), nx, ny])
        nextQuad = q; x = nx; y = ny
      } else if (C === 'A') {
        const rx = num(), ry = num(), rot = num(), fa = flag(), fs = flag()
        const nx = num() + px, ny = num() + py
        out.push(...arc(x, y, rx, ry, rot, fa, fs, nx, ny))
        x = nx; y = ny
      } else throw new Error(`Unsupported path command ${cmd}`)
      cubic = nextCubic
      quad = nextQuad
    } while (more())
  }
  return out
}

const line = (x0, y0, x1, y1) => ['C', x0 + (x1 - x0) / 3, y0 + (y1 - y0) / 3, x0 + (2 * (x1 - x0)) / 3, y0 + (2 * (y1 - y0)) / 3, x1, y1]

function arc(x1, y1, rx, ry, rotDeg, fa, fs, x2, y2) {
  if (rx === 0 || ry === 0) return [line(x1, y1, x2, y2)]
  const phi = (rotDeg * Math.PI) / 180
  const cos = Math.cos(phi), sin = Math.sin(phi)
  const dx = (x1 - x2) / 2, dy = (y1 - y2) / 2
  const x1p = cos * dx + sin * dy, y1p = -sin * dx + cos * dy
  rx = Math.abs(rx); ry = Math.abs(ry)
  const lam = (x1p * x1p) / (rx * rx) + (y1p * y1p) / (ry * ry)
  if (lam > 1) { rx *= Math.sqrt(lam); ry *= Math.sqrt(lam) }
  const num = rx * rx * ry * ry - rx * rx * y1p * y1p - ry * ry * x1p * x1p
  const den = rx * rx * y1p * y1p + ry * ry * x1p * x1p
  const co = (fa === fs ? -1 : 1) * Math.sqrt(Math.max(0, num / den))
  const cxp = (co * rx * y1p) / ry, cyp = (-co * ry * x1p) / rx
  const cx = cos * cxp - sin * cyp + (x1 + x2) / 2, cy = sin * cxp + cos * cyp + (y1 + y2) / 2
  const ang = (ux, uy, vx, vy) => Math.atan2(ux * vy - uy * vx, ux * vx + uy * vy)
  const t1 = ang(1, 0, (x1p - cxp) / rx, (y1p - cyp) / ry)
  let dt = ang((x1p - cxp) / rx, (y1p - cyp) / ry, (-x1p - cxp) / rx, (-y1p - cyp) / ry)
  if (!fs && dt > 0) dt -= 2 * Math.PI
  if (fs && dt < 0) dt += 2 * Math.PI
  const n = Math.max(1, Math.ceil(Math.abs(dt) / (Math.PI / 2)))
  const step = dt / n
  const k = (4 / 3) * Math.tan(step / 4)
  const out = []
  const at = (t) => [cos * rx * Math.cos(t) - sin * ry * Math.sin(t) + cx, sin * rx * Math.cos(t) + cos * ry * Math.sin(t) + cy]
  const dv = (t) => [-cos * rx * Math.sin(t) - sin * ry * Math.cos(t), -sin * rx * Math.sin(t) + cos * ry * Math.cos(t)]
  for (let s = 0; s < n; s++) {
    const a = t1 + s * step, b = a + step
    const p0 = at(a), p3 = at(b), d0 = dv(a), d3 = dv(b)
    out.push(['C', p0[0] + k * d0[0], p0[1] + k * d0[1], p3[0] - k * d3[0], p3[1] - k * d3[1], s === n - 1 ? x2 : p3[0], s === n - 1 ? y2 : p3[1]])
  }
  return out
}

// ---------- builds ----------

const bump = (y, centre, spread) => Math.exp(-(((y - centre) / spread) ** 2))
// The arms and hands keep their place, so only what is close to the middle is stretched.
const near = (dx) => Math.max(0, Math.min(1, (150 - Math.abs(dx)) / 70))
const BUILDS = {
  shoulders: (y) => 0.1 * bump(y, 175, 55) - 0.05 * bump(y, 420, 60),
  hips: (y) => -0.04 * bump(y, 175, 55) + 0.14 * bump(y, 420, 60),
}

const num = (n) => String(Math.round(n * 10) / 10)

function draw(path, build) {
  const move = (x, y) => {
    const dx = x - CX
    const stretched = CX + dx * (1 + BUILDS[build](y) * near(dx))
    return [stretched * SCALE, y * SCALE]
  }
  let d = ''
  for (const seg of path) {
    if (seg === 'Z') d += 'Z'
    else if (seg[0] === 'M') { const [x, y] = move(seg[1], seg[2]); d += `M${num(x)} ${num(y)}` }
    else {
      const [a, b] = move(seg[1], seg[2]), [c, e] = move(seg[3], seg[4]), [f, g] = move(seg[5], seg[6])
      d += `C${num(a)} ${num(b)} ${num(c)} ${num(e)} ${num(f)} ${num(g)}`
    }
  }
  return d
}

// ---------- figures ----------

const cap = (s) => s[0].toUpperCase() + s.slice(1)

function shapes(view, build) {
  const face = view === 'front' ? 'ant' : 'post'
  const names = view === 'front' ? NAMES : BACK_NAMES
  const out = []
  for (const part of source.filter((p) => p.face === face)) {
    const d = draw(parse(part.d), build)
    if (part.key === 'tete') { out.push({ key: 'head', name: 'head', d, skin: true }); continue }
    const side = part.key.endsWith('D') ? 'right' : part.key.endsWith('G') ? 'left' : ''
    const stem = side ? part.key.slice(0, -1) : part.key
    const entry = names[stem]
    if (!entry) throw new Error(`Unknown body part ${part.key}`)
    const [slug, name] = entry
    out.push({ key: side ? `${slug}-${side}` : slug, name: side ? `${cap(side)} ${name}` : cap(name), d, skin: false })
  }
  return out
}

function figure(view, build, { interactive, checked = [] }) {
  let body = ''
  for (const s of shapes(view, build)) {
    if (s.skin) body += `<path class="body-outline" d="${s.d}"/>`
    else if (interactive) body += `<path class="body-area" role="checkbox" tabindex="0" aria-checked="${checked.includes(s.key)}" aria-label="${s.name}" data-area="${s.key}" d="${s.d}"/>`
    else body += `<path class="body-part" d="${s.d}"/>`
  }
  const attrs = interactive ? `role="group" aria-label="${cap(view)} of the body"` : 'aria-hidden="true"'
  return `<svg viewBox="0 0 120 268" ${attrs}>${body}</svg>`
}

const CHECKED = { front: ['hip-left', 'knee-left'], back: ['knee-left'] }

function rewrite(text) {
  return text.replace(/<figure data-figure="(\w+)" class="body-view"><svg viewBox="0 0 120 268"(?: [^>]*aria-label="(Front|Back) of the body")?[^>]*>.*?<\/svg>/g,
    (_, build, label) => {
      const view = label ? label.toLowerCase() : 'front'
      return `<figure data-figure="${build}" class="body-view">${figure(view, build, { interactive: Boolean(label), checked: CHECKED[view] })}`
    })
}

// The same shapes as data for the apps: design/body-map.json, per build and view, a list of
// { key, d, skin } on the 120 x 268 grid with paths of M, C and Z only. skin is true for what is drawn but
// never tapped (the head outline).
const json = `${JSON.stringify(Object.fromEntries(Object.keys(BUILDS).map((b) => [b, Object.fromEntries(['front', 'back'].map((v) => [v, shapes(v, b).map(({ key, d, skin }) => ({ key, d, skin }))]))])), null, 2)}\n`
const jsonPath = join(root, 'design/body-map.json')

const check = process.argv.includes('--check')
let stale = false
for (const f of files) {
  const path = join(phone, f)
  const before = readFileSync(path, 'utf8')
  const after = rewrite(before)
  if (after !== before) {
    if (check) { console.error(`${f} does not match design/build-body-map.mjs; run it and commit`); stale = true } else writeFileSync(path, after)
  }
}
let current = ''
try { current = readFileSync(jsonPath, 'utf8') } catch {}
if (current !== json) {
  if (check) { console.error('design/body-map.json does not match design/build-body-map.mjs; run it and commit'); stale = true } else writeFileSync(jsonPath, json)
}
if (stale) process.exit(1)
