// Draws the body map figures (front and back, two builds) and writes them into the mockups.
//   node design/build-body-map.mjs          rewrite the figures
//   node design/build-body-map.mjs --check  fail if a mockup differs from what this draws
// The figure is a muscle chart: each muscle is one tappable region, the skin is left transparent, and there
// is no face, hair or anything that would suggest a skin tone. The regions are authored once for the half of
// the body on the right of the page (x > 60) and mirrored; a build only stretches the half sideways.
import { readFileSync, writeFileSync } from 'node:fs'
import { dirname, join } from 'node:path'
import { fileURLToPath } from 'node:url'

const root = join(dirname(fileURLToPath(import.meta.url)), '..')
const phone = join(root, 'design/mockups/agreed/phone')
const files = ['profile-pain-sheet.html', 'profile-body-figure-sheet.html']

const CX = 60

// Regions of the half drawn on the right of the page. Shared corners are written with the same numbers so
// neighbours touch like in a muscle chart. Each region is a closed polygon whose corners are rounded.
// A limb muscle from a centre line: [x, y, width towards the body, width away from it] at each step.
function limb(spine) {
  const left = []
  const right = []
  spine.forEach(([x, y, wIn, wOut], i) => {
    const [px, py] = spine[Math.max(i - 1, 0)]
    const [nx, ny] = spine[Math.min(i + 1, spine.length - 1)]
    const dx = nx - px
    const dy = ny - py
    const len = Math.hypot(dx, dy) || 1
    const normal = [dy / len, -dx / len]
    left.push([x - normal[0] * wIn, y - normal[1] * wIn])
    right.push([x + normal[0] * wOut, y + normal[1] * wOut])
  })
  return [...left, ...right.reverse()]
}
const oval = (cx, cy, rx, ry) => Array.from({ length: 8 }, (_, i) => [cx + rx * Math.cos((i * Math.PI) / 4), cy + ry * Math.sin((i * Math.PI) / 4)])

const ARM = {
  elbow: { name: 'elbow', pts: oval(92, 103, 5, 5) },
  forearm: { name: 'forearm', pts: limb([[92, 109, 5.5, 5.5], [95, 121, 5.5, 5.5], [99, 141, 3.2, 3.2]]) },
  hand: { name: 'hand', pts: limb([[100, 145, 3.5, 3.5], [102, 156, 4, 4], [104, 168, 2.5, 2.5]]) },
}
const LEG = {
  knee: { name: 'knee', pts: oval(71, 197, 5.5, 6.5) },
}

const FRONT = {
  neck: { name: 'neck', pts: [[61, 34], [66, 33], [68, 40], [66, 46], [62, 50], [61, 44]] },
  trapezius: { name: 'trapezius', pts: [[66, 46], [68, 41], [73, 45], [79, 48], [77, 53], [70, 53], [66, 51]] },
  deltoid: { name: 'deltoid', pts: [[78, 48], [84, 48], [89, 53], [91, 62], [90, 72], [86, 78], [82, 74], [79, 66], [77, 56]] },
  pectoral: { name: 'pectoral', pts: [[61, 53], [66, 51], [70, 53], [77, 53], [77, 61], [75, 67], [70, 72], [63, 73], [61, 68]] },
  serratus: { name: 'serratus', pts: [[70, 72], [75, 67], [78, 71], [77, 78], [75, 85], [71, 82], [70, 77]] },
  abs: { name: 'abs', pts: [[61, 74], [63, 73], [70, 72], [70, 78], [70, 92], [68, 104], [65, 114], [61, 118]] },
  obliques: { name: 'obliques', pts: [[70, 78], [71, 82], [75, 85], [75, 92], [72, 102], [72, 110], [68, 112], [65, 114], [68, 104], [70, 92]] },
  hip: { name: 'hip', pts: [[65, 114], [68, 112], [72, 110], [76, 114], [78, 121], [75, 128], [70, 130], [64, 124]] },
  biceps: { name: 'biceps', pts: limb([[85, 76, 4.5, 5], [87, 86, 5, 6], [90, 98, 4.5, 4.5]]) },
  ...ARM,
  adductors: { name: 'adductors', pts: limb([[64, 130, 3, 3], [67, 150, 3.5, 3], [68, 170, 3, 3], [68, 184, 2.5, 2.5]]) },
  quads: { name: 'quads', pts: limb([[75, 132, 7, 8], [76, 150, 7, 8.5], [74, 170, 6, 7], [72, 188, 4.5, 5]]) },
  ...LEG,
  shin: { name: 'shin', pts: limb([[71, 208, 4.5, 4.5], [72, 222, 5, 5], [71, 235, 3.5, 3.5], [70, 246, 3, 3]]) },
  foot: { name: 'foot', pts: [[65, 248], [72, 248], [75, 255], [80, 261], [76, 264], [66, 264], [63, 257]] },
}

const BACK = {
  neck: { name: 'neck', pts: [[61, 34], [66, 33], [68, 40], [66, 46], [61, 50]] },
  trapezius: { name: 'trapezius', pts: [[61, 50], [66, 46], [73, 45], [79, 49], [77, 56], [72, 64], [66, 82], [61, 94]] },
  rotator: { name: 'rotator cuff', pts: [[77, 56], [80, 60], [80, 70], [76, 77], [70, 78], [69, 72], [72, 64]] },
  deltoid: { name: 'deltoid', pts: [[78, 49], [84, 48], [89, 53], [91, 62], [90, 72], [86, 78], [82, 74], [80, 66], [80, 60], [77, 56]] },
  lats: { name: 'lats', pts: [[69, 78], [76, 77], [80, 70], [79, 84], [76, 96], [72, 106], [66, 102], [63, 100], [61, 94], [66, 82]] },
  triceps: { name: 'triceps', pts: limb([[85, 76, 4.5, 5], [87, 86, 5, 6], [90, 98, 4.5, 4.5]]) },
  ...ARM,
  lowerBack: { name: 'lower back', pts: [[61, 94], [63, 100], [66, 102], [72, 106], [71, 114], [66, 116], [61, 116]] },
  glutes: { name: 'glutes', pts: [[61, 116], [66, 116], [71, 114], [78, 117], [82, 125], [80, 136], [73, 143], [65, 143], [61, 137]] },
  hamstrings: { name: 'hamstrings', pts: limb([[71, 147, 9, 10], [72, 166, 8.5, 9], [71, 184, 6, 6], [71, 190, 5, 5]]) },
  ...LEG,
  calves: { name: 'calves', pts: limb([[70, 206, 4, 5.5], [72, 218, 5.5, 6.5], [71, 232, 4, 4.5], [70, 246, 3, 3]]) },
  foot: { name: 'foot', pts: [[65, 248], [72, 248], [75, 255], [76, 261], [73, 264], [66, 264], [63, 257]] },
}

// Fine lines inside a region (tendon bands of the abs, the spine) that are drawn but never tapped.
const FRONT_LINES = [
  [[61, 82], [70, 81]], [[61, 92], [71, 91]], [[61, 102], [70, 101]],
]
const BACK_LINES = [[[60, 52], [60, 118]]]

// A build only stretches the half sideways, more at the shoulders or at the hips.
const bump = (y, centre, spread) => Math.exp(-(((y - centre) / spread) ** 2))
const BUILDS = {
  shoulders: (y) => 1 + 0.1 * bump(y, 62, 22) - 0.05 * bump(y, 128, 22),
  hips: (y) => 1 - 0.04 * bump(y, 62, 22) + 0.13 * bump(y, 128, 24),
}

const num = (n) => String(Math.round(n * 10) / 10)

// A closed smooth curve through the corners (Catmull-Rom turned into Béziers), so every muscle is round.
function rounded(pts) {
  const n = pts.length
  let d = `M${num(pts[0][0])} ${num(pts[0][1])}`
  for (let i = 0; i < n; i++) {
    const p0 = pts[(i + n - 1) % n]
    const p1 = pts[i]
    const p2 = pts[(i + 1) % n]
    const p3 = pts[(i + 2) % n]
    const c1 = [p1[0] + (p2[0] - p0[0]) / 6, p1[1] + (p2[1] - p0[1]) / 6]
    const c2 = [p2[0] - (p3[0] - p1[0]) / 6, p2[1] - (p3[1] - p1[1]) / 6]
    d += `C${num(c1[0])} ${num(c1[1])} ${num(c2[0])} ${num(c2[1])} ${num(p2[0])} ${num(p2[1])}`
  }
  return `${d}Z`
}

const place = (pts, build, mirror) => pts.map(([x, y]) => {
  const stretched = CX + (x - CX) * BUILDS[build](y)
  return [mirror ? 2 * CX - stretched : stretched, y]
})

const cap = (s) => s[0].toUpperCase() + s.slice(1)

function figure(view, build, { interactive, checked = [] }) {
  const set = view === 'front' ? FRONT : BACK
  const lines = view === 'front' ? FRONT_LINES : BACK_LINES
  // The half on the right of the page is the person's left on the front and their right on the back.
  const sides = view === 'front' ? [['right', true], ['left', false]] : [['left', true], ['right', false]]
  let body = '<ellipse class="body-outline" cx="60" cy="19" rx="11" ry="14"/>'
  for (const [side, mirror] of sides) {
    for (const [key, { name, pts }] of Object.entries(set)) {
      const slug = key.replace(/[A-Z]/g, (c) => `-${c.toLowerCase()}`)
      const d = rounded(place(pts, build, mirror))
      if (interactive) {
        const id = `${slug}-${side}`
        body += `<path class="body-area" role="checkbox" tabindex="0" aria-checked="${checked.includes(id)}" aria-label="${cap(side)} ${name}" data-area="${id}" d="${d}"/>`
      } else {
        body += `<path class="body-part" d="${d}"/>`
      }
    }
    for (const line of lines) {
      const [a, b] = place(line, build, mirror)
      body += `<path class="body-line" d="M${num(a[0])} ${num(a[1])}L${num(b[0])} ${num(b[1])}"/>`
    }
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
// never tapped (the head outline and the tendon lines).
function shapes(view, build) {
  const set = view === 'front' ? FRONT : BACK
  const lines = view === 'front' ? FRONT_LINES : BACK_LINES
  const sides = view === 'front' ? [['right', true], ['left', false]] : [['left', true], ['right', false]]
  const k = 0.5523
  const head = `M60 5C${num(60 + 11 * k)} 5 71 ${num(19 - 14 * k)} 71 19C71 ${num(19 + 14 * k)} ${num(60 + 11 * k)} 33 60 33C${num(60 - 11 * k)} 33 49 ${num(19 + 14 * k)} 49 19C49 ${num(19 - 14 * k)} ${num(60 - 11 * k)} 5 60 5Z`
  const out = [{ key: 'head', d: head, skin: true }]
  for (const [side, mirror] of sides) {
    for (const [key, { pts }] of Object.entries(set)) {
      const slug = key.replace(/[A-Z]/g, (c) => `-${c.toLowerCase()}`)
      out.push({ key: `${slug}-${side}`, d: rounded(place(pts, build, mirror)), skin: false })
    }
    lines.forEach((line, i) => {
      const [a, b] = place(line, build, mirror)
      out.push({ key: `line-${i + 1}-${side}`, d: `M${num(a[0])} ${num(a[1])}C${num(a[0])} ${num(a[1])} ${num(b[0])} ${num(b[1])} ${num(b[0])} ${num(b[1])}`, skin: true })
    })
  }
  return out
}
const json = `${JSON.stringify(Object.fromEntries(Object.keys(BUILDS).map((b) => [b, { front: shapes('front', b), back: shapes('back', b) }])), null, 2)}\n`
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
