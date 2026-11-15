// Poulpy in 3D as a picture, with no dependency: draws what buildPoulpy3d (model.mjs) or buildPoulpyPose
// (poses.mjs) built, pixel by pixel, the way the docs site's "Poulpy in 3D" page draws it live. Plain
// JavaScript and plain arithmetic, so every machine paints the same picture.
//
// The look is LOOK in model.mjs: flat cartoon shading with one shadow band, a light that belongs to the room,
// the gleam where that light hits his head, and a soft round shadow on the floor.
// The rules are in docs/poulpy-3d.md.
import { deflateSync } from 'node:zlib'
import { LOOK, gleamPatch } from './model.mjs'

// How the picture is taken unless told otherwise: the viewer stands `distance` away, `tilt` degrees above him
// and `turn` degrees around him, and sees `field` degrees from top to bottom. The floor shadow is in `shadow`:
// a `#RRGGBB` colour, how wide it is and how strong at its middle.
export const SHOT = { turn: 0, tilt: 8, distance: 9.6, field: 28, shadow: { size: 4, strength: 0.38 } }
// The flat effects of a pose stand this far in front of his middle and behind it, around this height.
const CARD = { gap: 1.9, centre: 1.04 }

const sub = (a, b) => [a[0] - b[0], a[1] - b[1], a[2] - b[2]]
const dot = (a, b) => a[0] * b[0] + a[1] * b[1] + a[2] * b[2]
const cross = (a, b) => [a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0]]
const norm = (v) => { const l = Math.hypot(...v) || 1; return [v[0] / l, v[1] / l, v[2] / l] }
const RAD = Math.PI / 180
// Linear light to the 0 to 255 of a screen.
const screen = (c) => Math.round(255 * (c <= 0.0031308 ? 12.92 * c : 1.055 * c ** (1 / 2.4) - 0.055))

/**
 * Paints Poulpy.
 * @param model what buildPoulpy3d or buildPoulpyPose returned
 * @param {{ width: number, height: number, turn?: number, tilt?: number, distance?: number, centre?: number, shadow?: string | null, smooth?: number }} options
 *   `width` and `height` in pixels. `turn`, `tilt` and `distance` are where the viewer stands, as in SHOT, looking at
 *   the point `centre` high above the floor, half his height unless told otherwise.
 *   `shadow` is the floor shadow's `#RRGGBB` colour, or null for none. `smooth` is how many times finer than a
 *   pixel he is painted, each way, before the picture is brought down to size: 3 unless told otherwise.
 * @returns {Uint8Array} the picture as RGBA bytes, row by row from the top, see-through around him
 */
export function paintPoulpy(model, { width, height, turn = SHOT.turn, tilt = SHOT.tilt, distance = SHOT.distance, centre = model.height / 2, shadow = null, smooth = 3 }) {
  const W = width * smooth, H = height * smooth
  // Where the viewer stands and what is right, up and ahead for them.
  const eye = [distance * Math.sin(turn * RAD) * Math.cos(tilt * RAD), centre + distance * Math.sin(tilt * RAD), distance * Math.cos(turn * RAD) * Math.cos(tilt * RAD)]
  const ahead = norm(sub([0, centre, 0], eye)), right = norm(cross(ahead, [0, 1, 0])), up = cross(right, ahead)
  const reach = Math.tan((SHOT.field / 2) * RAD)
  // A direction the viewer sees (x right, y up, z towards them), in the room.
  const inRoom = ([x, y, z]) => norm([0, 1, 2].map((k) => right[k] * x + up[k] * y - ahead[k] * z))
  const light = inRoom(LOOK.light)

  const colour = new Float32Array(W * H * 3), cover = new Float32Array(W * H), depth = new Float32Array(W * H).fill(Infinity)
  // A point of the room on the picture: across, down, and how far ahead of the viewer it is.
  const see = (p) => {
    const q = sub(p, eye), d = dot(q, ahead)
    return [(dot(q, right) / d / (reach * (W / H)) + 1) * 0.5 * W, (1 - dot(q, up) / d / reach) * 0.5 * H, d]
  }
  // Fills one triangle. `paint(a, b, c)` gives the colour and strength at a point from its share of each corner.
  const fill = (corners, paint, solid = true) => {
    const [A, B, C] = corners.map(see)
    const area = (B[0] - A[0]) * (C[1] - A[1]) - (B[1] - A[1]) * (C[0] - A[0])
    if (area === 0) return
    const x0 = Math.max(Math.floor(Math.min(A[0], B[0], C[0])), 0), x1 = Math.min(Math.ceil(Math.max(A[0], B[0], C[0])), W - 1)
    const y0 = Math.max(Math.floor(Math.min(A[1], B[1], C[1])), 0), y1 = Math.min(Math.ceil(Math.max(A[1], B[1], C[1])), H - 1)
    for (let y = y0; y <= y1; y++) for (let x = x0; x <= x1; x++) {
      const px = x + 0.5, py = y + 0.5
      const a = ((B[0] - px) * (C[1] - py) - (B[1] - py) * (C[0] - px)) / area, b = ((C[0] - px) * (A[1] - py) - (C[1] - py) * (A[0] - px)) / area, c = 1 - a - b
      if (a < 0 || b < 0 || c < 0) continue
      // Shares are weighed by nearness, so what is painted is right in the room, not only on the picture.
      const wa = a / A[2], wb = b / B[2], wc = c / C[2], w = wa + wb + wc, d = 1 / w, at = y * W + x
      if (d >= depth[at]) continue
      const [r, g, bl, strength] = paint(wa / w, wb / w, wc / w)
      if (solid) { depth[at] = d; colour[at * 3] = r; colour[at * 3 + 1] = g; colour[at * 3 + 2] = bl; cover[at] = 1; continue }
      // What is see-through is laid over what is there, and hides nothing behind it.
      colour[at * 3] = r * strength + colour[at * 3] * cover[at] * (1 - strength)
      colour[at * 3 + 1] = g * strength + colour[at * 3 + 1] * cover[at] * (1 - strength)
      colour[at * 3 + 2] = bl * strength + colour[at * 3 + 2] * cover[at] * (1 - strength)
      cover[at] = strength + cover[at] * (1 - strength)
      for (let k = 0; k < 3; k++) colour[at * 3 + k] /= cover[at] || 1
    }
  }

  // His head and what he wears are lifted and leant with it; the flat effects stand across the line of sight.
  const lift = model.headLift ?? 0, lean = model.headTilt ?? 0, cl = Math.cos(lean), sl = Math.sin(lean)
  const towards = norm(sub(eye, [0, centre, 0]))
  const place = (part) => {
    if (part.card) {
      const way = part.card === 'front' ? 1 : -1, size = (distance - way * CARD.gap) / distance
      const middle = [0, 1, 2].map((k) => [0, CARD.centre, 0][k] + towards[k] * way * CARD.gap)
      return { point: ([x, y, z]) => [0, 1, 2].map((k) => middle[k] + (right[k] * x + up[k] * (y - CARD.centre) - ahead[k] * z) * size), way: (n) => inRoom(n) }
    }
    if (/^(arm_|dumbbell|medal)/.test(part.name)) return { point: (p) => p, way: (n) => n }
    return { point: ([x, y, z]) => [x * cl - y * sl, x * sl + y * cl + lift, z], way: ([x, y, z]) => [x * cl - y * sl, x * sl + y * cl, z] }
  }
  // The gleam follows the light, so it is laid again for where the viewer stands.
  const gleam = gleamPatch(inRoom(LOOK.gleam.towards))
  for (const part of model.parts) {
    const { positions, normals, indices } = part.name === 'gleam' ? gleam : part, material = model.materials[part.material], { point, way } = place(part)
    const [r, g, b] = material.colour, at = (i) => point([positions[i * 3], positions[i * 3 + 1], positions[i * 3 + 2]])
    // One shadow band: a point is lit when it faces the light, and LOOK.shade of its colour otherwise.
    const lit = (i) => dot(way([normals[i * 3], normals[i * 3 + 1], normals[i * 3 + 2]]), light)
    for (let t = 0; t < indices.length; t += 3) {
      const [i, j, k] = [indices[t], indices[t + 1], indices[t + 2]]
      if (material.flat) { fill([at(i), at(j), at(k)], () => [r, g, b, 1]); continue }
      const [li, lj, lk] = [lit(i), lit(j), lit(k)]
      fill([at(i), at(j), at(k)], (a, bb, c) => { const s = a * li + bb * lj + c * lk > 0 ? 1 : LOOK.shade; return [r * s, g * s, b * s, 1] })
    }
  }
  if (shadow) {
    // A soft round shadow on the floor, strongest under him and gone at its edge.
    const tint = [1, 3, 5].map((i) => { const c = parseInt(shadow.slice(i, i + 2), 16) / 255; return c <= 0.04045 ? c / 12.92 : ((c + 0.055) / 1.055) ** 2.4 })
    const half = SHOT.shadow.size / 2, corners = [[-half, 0.005, -half], [half, 0.005, -half], [half, 0.005, half], [-half, 0.005, half]]
    const fade = (far) => (far < 0.55 ? SHOT.shadow.strength + (0.16 - SHOT.shadow.strength) * (far / 0.55) : far < 1 ? 0.16 * (1 - (far - 0.55) / 0.45) : 0)
    for (const [i, j, k] of [[0, 1, 2], [0, 2, 3]]) {
      fill([corners[i], corners[j], corners[k]], (a, b, c) => {
        const x = a * corners[i][0] + b * corners[j][0] + c * corners[k][0], z = a * corners[i][2] + b * corners[j][2] + c * corners[k][2]
        return [...tint, fade(Math.hypot(x, z) / half)]
      }, false)
    }
  }

  // Down to size: each pixel is the average of the finer ones under it.
  const out = new Uint8Array(width * height * 4), n = smooth * smooth
  for (let y = 0; y < height; y++) for (let x = 0; x < width; x++) {
    let r = 0, g = 0, b = 0, a = 0
    for (let dy = 0; dy < smooth; dy++) for (let dx = 0; dx < smooth; dx++) {
      const at = (y * smooth + dy) * W + x * smooth + dx
      r += colour[at * 3] * cover[at]; g += colour[at * 3 + 1] * cover[at]; b += colour[at * 3 + 2] * cover[at]; a += cover[at]
    }
    const o = (y * width + x) * 4
    if (a > 0) { out[o] = screen(r / a); out[o + 1] = screen(g / a); out[o + 2] = screen(b / a) }
    out[o + 3] = Math.round((255 * a) / n)
  }
  return out
}

const CRC = Array.from({ length: 256 }, (_, n) => { let c = n; for (let k = 0; k < 8; k++) c = c & 1 ? 0xedb88320 ^ (c >>> 1) : c >>> 1; return c >>> 0 })
const crc32 = (buf) => { let c = 0xffffffff; for (const b of buf) c = CRC[(c ^ b) & 0xff] ^ (c >>> 8); return (c ^ 0xffffffff) >>> 0 }
function chunk(type, data) {
  const out = Buffer.alloc(12 + data.length)
  out.writeUInt32BE(data.length, 0); out.write(type, 4, 'latin1'); data.copy(out, 8)
  out.writeUInt32BE(crc32(out.subarray(4, 8 + data.length)), 8 + data.length)
  return out
}
/** A picture of paintPoulpy as a PNG file, see-through around him. Every row is unfiltered, so a check can read the pixels back. */
export function pngOf(pixels, width, height) {
  const head = Buffer.alloc(13)
  head.writeUInt32BE(width, 0); head.writeUInt32BE(height, 4); head.set([8, 6, 0, 0, 0], 8)
  const rows = Buffer.alloc(height * (width * 4 + 1))
  for (let y = 0; y < height; y++) rows.set(pixels.subarray(y * width * 4, (y + 1) * width * 4), y * (width * 4 + 1) + 1)
  return Buffer.concat([Buffer.from([0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a]), chunk('IHDR', head), chunk('IDAT', deflateSync(rows, { level: 9 })), chunk('IEND', Buffer.alloc(0))])
}
