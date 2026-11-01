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
const FRONT = {
  neck: { name: 'neck', pts: [[61, 34], [66, 33], [68, 40], [66, 46], [62, 50], [61, 44]] },
  trapezius: { name: 'trapezius', pts: [[66, 46], [68, 41], [73, 45], [79, 48], [77, 52], [70, 53], [66, 51]] },
  deltoid: { name: 'deltoid', pts: [[79, 48], [85, 50], [89, 57], [91, 65], [90, 73], [87, 77], [82, 76], [78, 70], [76, 60], [77, 52]] },
  pectoral: { name: 'pectoral', pts: [[61, 53], [66, 51], [70, 53], [77, 52], [76, 60], [75, 66], [70, 71], [63, 72], [61, 67]] },
  serratus: { name: 'serratus', pts: [[70, 71], [75, 66], [78, 70], [79, 76], [78, 83], [73, 82], [71, 77]] },
  abs: { name: 'abs', pts: [[61, 73], [63, 72], [70, 71], [71, 77], [72, 90], [71, 102], [68, 110], [64, 121], [61, 124]] },
  obliques: { name: 'obliques', pts: [[71, 77], [73, 82], [78, 83], [78, 92], [76, 101], [76, 108], [72, 112], [68, 110], [71, 102], [72, 90]] },
  hip: { name: 'hip', pts: [[68, 110], [72, 112], [78, 114], [81, 122], [78, 128], [72, 131], [66, 131], [64, 121]] },
  biceps: { name: 'biceps', pts: [[82, 77], [87, 77], [91, 80], [93, 89], [92, 96], [88, 98], [84, 96], [82, 87]] },
  elbow: { name: 'elbow', pts: [[85, 99], [90, 98], [95, 100], [96, 105], [93, 109], [88, 109], [85, 105]] },
  forearm: { name: 'forearm', pts: [[85, 110], [93, 110], [97, 118], [100, 130], [99, 141], [94, 141], [91, 130], [87, 119]] },
  hand: { name: 'hand', pts: [[93, 143], [99, 143], [103, 152], [103, 162], [99, 166], [95, 161], [92, 152]] },
  adductors: { name: 'adductors', pts: [[61, 129], [66, 132], [66, 150], [66, 168], [65, 177], [63, 166], [62, 148]] },
  quads: { name: 'quads', pts: [[66, 132], [72, 132], [79, 129], [83, 136], [83, 150], [81, 165], [78, 180], [75, 190], [69, 190], [66, 182], [66, 168], [66, 150]] },
  knee: { name: 'knee', pts: [[65, 192], [70, 191], [76, 193], [77, 198], [74, 203], [68, 203], [65, 198]] },
  shin: { name: 'shin', pts: [[65, 205], [75, 205], [76, 216], [74, 232], [72, 244], [67, 244], [66, 232], [64, 216]] },
  foot: { name: 'foot', pts: [[64, 247], [73, 247], [75, 254], [79, 260], [75, 263], [66, 263], [63, 256]] },
}

const BACK = {
  neck: { name: 'neck', pts: [[61, 34], [66, 33], [68, 40], [66, 46], [61, 50]] },
  trapezius: { name: 'trapezius', pts: [[61, 50], [66, 46], [73, 45], [79, 49], [77, 56], [72, 64], [66, 82], [61, 94]] },
  rotator: { name: 'rotator cuff', pts: [[77, 56], [80, 60], [80, 70], [76, 77], [70, 78], [69, 72], [72, 64]] },
  deltoid: { name: 'deltoid', pts: [[79, 49], [85, 50], [89, 57], [91, 65], [90, 73], [87, 77], [82, 76], [80, 70], [80, 60], [77, 56]] },
  lats: { name: 'lats', pts: [[69, 78], [76, 77], [80, 70], [81, 84], [79, 96], [76, 106], [70, 104], [63, 100], [61, 94], [66, 82]] },
  triceps: { name: 'triceps', pts: [[82, 77], [87, 77], [91, 80], [93, 89], [92, 96], [88, 98], [84, 96], [82, 87]] },
  elbow: { name: 'elbow', pts: [[85, 99], [90, 98], [95, 100], [96, 105], [93, 109], [88, 109], [85, 105]] },
  forearm: { name: 'forearm', pts: [[85, 110], [93, 110], [97, 118], [100, 130], [99, 141], [94, 141], [91, 130], [87, 119]] },
  hand: { name: 'hand', pts: [[93, 143], [99, 143], [103, 152], [103, 162], [99, 166], [95, 161], [92, 152]] },
  lowerBack: { name: 'lower back', pts: [[61, 94], [63, 100], [70, 104], [76, 106], [75, 114], [70, 116], [61, 116]] },
  glutes: { name: 'glutes', pts: [[61, 116], [70, 116], [75, 114], [81, 119], [84, 127], [82, 137], [75, 143], [66, 143], [61, 137]] },
  hamstrings: { name: 'hamstrings', pts: [[62, 145], [68, 144], [76, 144], [81, 150], [80, 165], [78, 180], [75, 190], [69, 190], [66, 182], [64, 165], [62, 150]] },
  knee: { name: 'knee', pts: [[65, 192], [70, 191], [76, 193], [77, 198], [74, 203], [68, 203], [65, 198]] },
  calves: { name: 'calves', pts: [[64, 205], [75, 205], [78, 214], [77, 228], [72, 244], [67, 244], [64, 228], [63, 214]] },
  foot: { name: 'foot', pts: [[64, 247], [73, 247], [75, 254], [75, 260], [72, 263], [66, 263], [63, 256]] },
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
if (stale) process.exit(1)
