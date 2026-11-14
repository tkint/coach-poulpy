// Generates Poulpy's 3D model as glTF files, from the shape described in design/poulpy-3d/model.mjs.
//   node design/build-poulpy-3d.mjs          write the generated files
//   node design/build-poulpy-3d.mjs --check  fail if a generated file is out of date (used by CI)
//
// One binary glTF (.glb) per side of the plush, in design/poulpy-3d/:
//   poulpy_blue.glb   the blue, smiling side
//   poulpy_ember.glb  the ember, grumpy side
// Each part of him (head, arms, eyes, mouth...) is a named mesh, standing on y = 0 and facing +z, with
// plain matte colours. The cartoon shading, the moving gleam and the outline belong to whoever draws
// him: LOOK in model.mjs gives their values and the docs site's Poulpy3d component shows how.
// The file is written here by hand, with no library, so every machine writes the same bytes.
import { readFileSync, writeFileSync, existsSync } from 'node:fs'
import { dirname, join } from 'node:path'
import { fileURLToPath } from 'node:url'
import { buildPoulpy3d } from './poulpy-3d/model.mjs'

const root = join(dirname(fileURLToPath(import.meta.url)), '..')
const tokens = JSON.parse(readFileSync(join(root, 'design/tokens.json'), 'utf8'))
const check = process.argv.includes('--check')

function glb({ parts, materials }, name) {
  const materialNames = Object.keys(materials)
  const chunks = [], bufferViews = [], accessors = []
  let length = 0
  // Adds one typed array to the binary part, padded to 4 bytes, and returns its accessor's index.
  const store = (array, target, accessor) => {
    const bytes = Buffer.from(array.buffer, array.byteOffset, array.byteLength)
    const pad = (4 - (bytes.length % 4)) % 4
    bufferViews.push({ buffer: 0, byteOffset: length, byteLength: bytes.length, target })
    chunks.push(bytes, Buffer.alloc(pad))
    length += bytes.length + pad
    accessors.push({ bufferView: bufferViews.length - 1, ...accessor })
    return accessors.length - 1
  }
  const meshes = parts.map((part) => {
    const positions = new Float32Array(part.positions), count = positions.length / 3
    const bounds = [0, 1, 2].map((k) => { let lo = Infinity, hi = -Infinity; for (let i = k; i < positions.length; i += 3) { lo = Math.min(lo, positions[i]); hi = Math.max(hi, positions[i]) } return [lo, hi] })
    return {
      name: part.name,
      primitives: [{
        attributes: {
          POSITION: store(positions, 34962, { componentType: 5126, count, type: 'VEC3', min: bounds.map((b) => b[0]), max: bounds.map((b) => b[1]) }),
          NORMAL: store(new Float32Array(part.normals), 34962, { componentType: 5126, count, type: 'VEC3' }),
        },
        indices: store(new Uint16Array(part.indices), 34963, { componentType: 5123, count: part.indices.length, type: 'SCALAR' }),
        material: materialNames.indexOf(part.material),
      }],
    }
  })
  const json = {
    asset: { version: '2.0', generator: 'design/build-poulpy-3d.mjs' },
    scene: 0,
    scenes: [{ name, nodes: [0] }],
    nodes: [{ name, children: parts.map((_, i) => i + 1) }, ...parts.map((part, i) => ({ name: part.name, mesh: i }))],
    meshes,
    materials: materialNames.map((id) => ({
      name: id,
      pbrMetallicRoughness: { baseColorFactor: [...materials[id].colour.map((c) => +c.toFixed(6)), 1], metallicFactor: 0, roughnessFactor: 1 },
    })),
    accessors,
    bufferViews,
    buffers: [{ byteLength: length }],
  }
  let text = Buffer.from(JSON.stringify(json))
  text = Buffer.concat([text, Buffer.alloc((4 - (text.length % 4)) % 4, ' ')])
  const bin = Buffer.concat(chunks)
  const header = Buffer.alloc(12), jsonHead = Buffer.alloc(8), binHead = Buffer.alloc(8)
  header.writeUInt32LE(0x46546c67, 0); header.writeUInt32LE(2, 4); header.writeUInt32LE(12 + 8 + text.length + 8 + bin.length, 8)
  jsonHead.writeUInt32LE(text.length, 0); jsonHead.writeUInt32LE(0x4e4f534a, 4)
  binHead.writeUInt32LE(bin.length, 0); binHead.writeUInt32LE(0x004e4942, 4)
  return Buffer.concat([header, jsonHead, text, binHead, bin])
}

let stale = 0
for (const side of ['blue', 'ember']) {
  const path = `design/poulpy-3d/poulpy_${side}.glb`, file = join(root, path)
  const bytes = glb(buildPoulpy3d({ side, colours: tokens.color.poulpy }), `Poulpy_${side}`)
  if (check) {
    if (!existsSync(file) || !readFileSync(file).equals(bytes)) { console.error(`Out of date: ${path}`); stale++ }
  } else {
    writeFileSync(file, bytes)
    console.log(`Wrote ${path} (${Math.round(bytes.length / 1024)} KB)`)
  }
}
if (stale) { console.error('Run `node design/build-poulpy-3d.mjs` and commit the result.'); process.exit(1) }
if (check) console.log('Poulpy 3D models are up to date.')
