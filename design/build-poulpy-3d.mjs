// Generates Poulpy's 3D model as glTF files, from the shape described in design/poulpy-3d/model.mjs.
//   node design/build-poulpy-3d.mjs          write the generated files
//   node design/build-poulpy-3d.mjs --check  fail if a generated file is out of date (used by CI)
//
// One binary glTF (.glb) per side of the plush, in design/poulpy-3d/:
//   poulpy_blue.glb   the blue, smiling side
//   poulpy_ember.glb  the ember, grumpy side
// and the same two again with his idle move baked in (design/poulpy-3d/motion.mjs):
//   poulpy_blue_turn.glb, poulpy_ember_turn.glb  turning around in short steps, as the animation `turn`
// Each part of him (head, arms, eyes, mouth...) is a named mesh, standing on y = 0 and facing +z, with
// plain matte colours. The cartoon shading, the moving gleam and the outline belong to whoever draws
// him: LOOK in model.mjs gives their values and the docs site's Poulpy3d component shows how.
// The file is written here by hand, with no library, so every machine writes the same bytes.
import { readFileSync, writeFileSync, existsSync } from 'node:fs'
import { dirname, join } from 'node:path'
import { fileURLToPath } from 'node:url'
import { buildPoulpy3d } from './poulpy-3d/model.mjs'
import { armJoints, armSkin, bakeTurn, squashScale } from './poulpy-3d/motion.mjs'

const root = join(dirname(fileURLToPath(import.meta.url)), '..')
const tokens = JSON.parse(readFileSync(join(root, 'design/tokens.json'), 'utf8'))
const check = process.argv.includes('--check')

// With `moving`, he is rigged and carries the animation `turn`: the node `turn` faces him, `squash` under it
// holds his head and face, and each arm is skinned to `turn` and its own three `arm_N_bend_N` joints.
function glb({ parts, materials }, name, moving = false) {
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
  // Which arm a part belongs to, from 0, when he is rigged; -1 for his head and face.
  const armOf = (part) => { const arm = /^arm_(\d+)/.exec(part.name); return moving && arm ? arm[1] - 1 : -1 }
  const meshes = parts.map((part) => {
    const positions = new Float32Array(part.positions), count = positions.length / 3
    const skin = armOf(part) < 0 ? null : armSkin(armOf(part), part.positions)
    const bounds = [0, 1, 2].map((k) => { let lo = Infinity, hi = -Infinity; for (let i = k; i < positions.length; i += 3) { lo = Math.min(lo, positions[i]); hi = Math.max(hi, positions[i]) } return [lo, hi] })
    return {
      name: part.name,
      primitives: [{
        attributes: {
          POSITION: store(positions, 34962, { componentType: 5126, count, type: 'VEC3', min: bounds.map((b) => b[0]), max: bounds.map((b) => b[1]) }),
          NORMAL: store(new Float32Array(part.normals), 34962, { componentType: 5126, count, type: 'VEC3' }),
          ...(skin && {
            JOINTS_0: store(new Uint8Array(skin.joints), 34962, { componentType: 5121, count, type: 'VEC4' }),
            WEIGHTS_0: store(new Float32Array(skin.weights), 34962, { componentType: 5126, count, type: 'VEC4' }),
          }),
        },
        indices: store(new Uint16Array(part.indices), 34963, { componentType: 5123, count: part.indices.length, type: 'SCALAR' }),
        material: materialNames.indexOf(part.material),
      }],
    }
  })
  // Standing still, every part hangs from one node. Moving, the parts keep their numbers (1 to parts.length)
  // and the nodes that move them come after: `turn`, `squash`, then the arms' joints.
  const nodes = [{ name, children: parts.map((_, i) => i + 1) }, ...parts.map((part, i) => ({ name: part.name, mesh: i }))]
  const roots = [0]
  let rig = {}
  if (moving) {
    const turn = nodes.length, squash = turn + 1, firstJoint = turn + 2, joints = armJoints(), baked = bakeTurn()
    const still = parts.flatMap((part, i) => (armOf(part) < 0 ? [i + 1] : []))
    parts.forEach((part, i) => { if (armOf(part) >= 0) { nodes[i + 1].skin = 0; roots.push(i + 1) } })
    nodes[0].children = [turn]
    nodes.push({ name: 'turn', children: [squash, ...joints.flatMap((j, i) => (j.bend === 0 ? [firstJoint + i] : []))] }, { name: 'squash', children: still })
    joints.forEach((j, i) => {
      const from = j.bend === 0 ? [0, 0, 0] : joints[i - 1].at
      nodes.push({ name: j.name, translation: j.at.map((v, k) => +(v - from[k]).toFixed(6)), ...(j.bend < 2 && { children: [firstJoint + i + 1] }) })
    })
    // A skin needs, for each joint, the way back from the model to the joint: here only a move.
    const back = [[0, 0, 0], ...joints.map((j) => j.at)].flatMap(([x, y, z]) => [1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, -x, -y, -z, 1])
    const skins = [{ name: 'arms', skeleton: turn, joints: [turn, ...joints.map((_, i) => firstJoint + i)], inverseBindMatrices: store(new Float32Array(back), undefined, { componentType: 5126, count: joints.length + 1, type: 'MAT4' }) }]
    // The move: every track shares one clock. Turns are stored as 16-bit numbers, which is plenty.
    const frames = baked.times.length
    const input = store(new Float32Array(baked.times), undefined, { componentType: 5126, count: frames, type: 'SCALAR', min: [0], max: [Math.fround(baked.seconds)] })
    const aroundY = (angles) => store(new Int16Array(angles.flatMap((a) => [0, Math.round(Math.sin(a / 2) * 32767), 0, Math.round(Math.cos(a / 2) * 32767)])), undefined, { componentType: 5122, normalized: true, count: frames, type: 'VEC4' })
    const tracks = [
      { node: turn, path: 'rotation', output: aroundY(baked.yaw) },
      { node: squash, path: 'scale', output: store(new Float32Array(baked.squash.flatMap(squashScale)), undefined, { componentType: 5126, count: frames, type: 'VEC3' }) },
      ...joints.map((j, i) => ({ node: firstJoint + i, path: 'rotation', output: aroundY(baked.arms[j.arm][j.bend]) })),
    ]
    rig = {
      skins,
      animations: [{
        name: 'turn',
        samplers: tracks.map((t) => ({ input, output: t.output, interpolation: 'LINEAR' })),
        channels: tracks.map((t, i) => ({ sampler: i, target: { node: t.node, path: t.path } })),
      }],
    }
  }
  const json = {
    asset: { version: '2.0', generator: 'design/build-poulpy-3d.mjs' },
    scene: 0,
    scenes: [{ name, nodes: roots }],
    nodes,
    meshes,
    materials: materialNames.map((id) => ({
      name: id,
      pbrMetallicRoughness: { baseColorFactor: [...materials[id].colour.map((c) => +c.toFixed(6)), 1], metallicFactor: 0, roughnessFactor: 1 },
    })),
    ...rig,
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
for (const [side, moving] of [['blue', false], ['ember', false], ['blue', true], ['ember', true]]) {
  const path = `design/poulpy-3d/poulpy_${side}${moving ? '_turn' : ''}.glb`, file = join(root, path)
  const bytes = glb(buildPoulpy3d({ side, colours: tokens.color.poulpy }), `Poulpy_${side}`, moving)
  if (check) {
    if (!existsSync(file) || !readFileSync(file).equals(bytes)) { console.error(`Out of date: ${path}`); stale++ }
  } else {
    writeFileSync(file, bytes)
    console.log(`Wrote ${path} (${Math.round(bytes.length / 1024)} KB)`)
  }
}
if (stale) { console.error('Run `node design/build-poulpy-3d.mjs` and commit the result.'); process.exit(1) }
if (check) console.log('Poulpy 3D models are up to date.')
