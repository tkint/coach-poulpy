// Generates Poulpy's 3D model as glTF files, from the shape described in design/poulpy-3d/model.mjs.
//   node design/build-poulpy-3d.mjs          write the generated files
//   node design/build-poulpy-3d.mjs --check  fail if a generated file is out of date (used by CI)
//
// One binary glTF (.glb) per side of the plush, in design/poulpy-3d/:
//   poulpy_blue.glb   the blue, smiling side
//   poulpy_ember.glb  the ember, grumpy side
// and the same two again with his idle move baked in (design/poulpy-3d/motion.mjs):
//   poulpy_blue_turn.glb, poulpy_ember_turn.glb  turning around in short steps, as the animation `turn`
// and the moves that need more than a turn, each in a file of its own, looping:
//   poulpy_blue_jump.glb, poulpy_ember_jump.glb  his little jumps on the spot, as the animation `jump`
//   poulpy_champion_cheer.glb  the champion jumping for joy with his medal swinging, as the animation `cheer`
// (the confetti and the flat comic effects belong to whoever draws him, as the look does).
// Each part of him (head, arms, eyes, mouth...) is a named mesh, standing on y = 0 and facing +z, with
// plain matte colours. The cartoon shading, the moving gleam and the outline belong to whoever draws
// him: LOOK in model.mjs gives their values and the docs site's Poulpy3d component shows how.
// The file is written here by hand, with no library, so every machine writes the same bytes.
import { readFileSync, writeFileSync, existsSync } from 'node:fs'
import { dirname, join } from 'node:path'
import { fileURLToPath } from 'node:url'
import { buildPoulpy3d } from './poulpy-3d/model.mjs'
import { buildPoulpyPose, medalHinge } from './poulpy-3d/poses.mjs'
import { CHEER, JUMP, armJoints, armSkin, bakeTurn, createCheer, createJump, squashScale } from './poulpy-3d/motion.mjs'

const root = join(dirname(fileURLToPath(import.meta.url)), '..')
const tokens = JSON.parse(readFileSync(join(root, 'design/tokens.json'), 'utf8'))
const check = process.argv.includes('--check')

// Quaternions as glTF writes them, [x, y, z, w]: a turn of `angle` around a unit `axis`, and one turn after another.
const unit = (v) => { const l = Math.hypot(...v) || 1; return v.map((x) => x / l) }
const quat = (axis, angle) => [...unit(axis).map((x) => x * Math.sin(angle / 2)), Math.cos(angle / 2)]
const qmul = ([ax, ay, az, aw], [bx, by, bz, bw]) => [
  aw * bx + ax * bw + ay * bz - az * by, aw * by - ax * bz + ay * bw + az * bx,
  aw * bz + ax * by - ay * bx + az * bw, aw * bw - ax * bx - ay * by - az * bz,
]

// One go of a jumping move, looping: the move runs three rounds first so its springs settle into their rhythm, then
// `seconds` are recorded at `fps`, and the last frame is the first so a player can loop it.
const FPS = 30
function bakeMove(name, sim, seconds) {
  const frames = Math.round(seconds * FPS), step = seconds / frames
  for (let round = 0; round < 3; round++) sim.advance(0)
  for (let t = 0; t < seconds * 3; t += step) sim.advance(step)
  const poses = []
  for (let f = 0; f < frames; f++) { poses.push(JSON.parse(JSON.stringify(sim.pose()))); sim.advance(step) }
  poses.push(poses[0])
  return { name, seconds, times: poses.map((_, f) => f * step), poses }
}
// How long a round of jumps takes: every jump of the move's script and the pause that follows it.
const round = (jump) => jump.pauses.reduce((sum, pause) => sum + jump.tuck.seconds + jump.air.seconds + jump.landing.seconds + pause, 0)

// With `moving`, he is rigged and carries the animation `turn`: the node `turn` faces him, `squash` under it
// holds his head and face, and each arm is skinned to `turn` and its own three `arm_N_bend_N` joints.
// With `move` instead, he carries that move (see bakeMove) in place of the turn, and `headLift` and `headTilt` of his model
// are kept on `squash`; a model with a medal gets its hinge as nodes too: `medal_sway`, `medal_hinge` and `medal_rest`.
function glb({ parts, materials, headLift = 0, headTilt = 0 }, name, moving = false, move = null) {
  if (move) moving = true
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
  const isMedal = (part) => !!move && part.name.startsWith('medal')
  const nodes = [{ name, children: parts.map((_, i) => i + 1) }, ...parts.map((part, i) => ({ name: part.name, mesh: i }))]
  const roots = [0]
  let rig = {}
  if (moving) {
    const turn = nodes.length, squash = turn + 1, firstJoint = turn + 2, joints = armJoints(), baked = bakeTurn()
    const still = parts.flatMap((part, i) => (armOf(part) < 0 && !isMedal(part) ? [i + 1] : []))
    const medal = parts.flatMap((part, i) => (isMedal(part) ? [i + 1] : []))
    const hasMedal = medal.length > 0, medalSway = firstJoint + joints.length
    parts.forEach((part, i) => { if (armOf(part) >= 0) { nodes[i + 1].skin = 0; roots.push(i + 1) } })
    nodes[0].children = [turn]
    nodes.push(
      { name: 'turn', children: [squash, ...joints.flatMap((j, i) => (j.bend === 0 ? [firstJoint + i] : [])), ...(hasMedal ? [medalSway] : [])] },
      { name: 'squash', children: still, ...(move && headLift && { translation: [0, headLift, 0] }), ...(move && headTilt && { rotation: quat([0, 0, 1], headTilt) }) },
    )
    joints.forEach((j, i) => {
      const from = j.bend === 0 ? [0, 0, 0] : joints[i - 1].at
      nodes.push({ name: j.name, translation: j.at.map((v, k) => +(v - from[k]).toFixed(6)), ...(j.bend < 2 && { children: [firstJoint + i + 1] }) })
    })
    // The medal hangs from its hinge, which rides on his body, and the hinge swings it up; the sway slides it around him.
    if (hasMedal) {
      const [hy, hz] = medalHinge
      nodes.push({ name: 'medal_sway', children: [medalSway + 1] }, { name: 'medal_hinge', translation: [0, +hy.toFixed(6), +hz.toFixed(6)], children: [medalSway + 2] }, { name: 'medal_rest', translation: [0, +(-hy).toFixed(6), +(-hz).toFixed(6)], children: medal })
    }
    // A skin needs, for each joint, the way back from the model to the joint: here only a move.
    const back = [[0, 0, 0], ...joints.map((j) => j.at)].flatMap(([x, y, z]) => [1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, -x, -y, -z, 1])
    const skins = [{ name: 'arms', skeleton: turn, joints: [turn, ...joints.map((_, i) => firstJoint + i)], inverseBindMatrices: store(new Float32Array(back), undefined, { componentType: 5126, count: joints.length + 1, type: 'MAT4' }) }]
    // The move: every track shares one clock. Turns are stored as 16-bit numbers, which is plenty.
    if (move) baked.times = move.times
    const frames = baked.times.length
    const input = store(new Float32Array(baked.times), undefined, { componentType: 5126, count: frames, type: 'SCALAR', min: [0], max: [Math.fround(baked.seconds)] })
    const aroundY = (angles) => store(new Int16Array(angles.flatMap((a) => [0, Math.round(Math.sin(a / 2) * 32767), 0, Math.round(Math.cos(a / 2) * 32767)])), undefined, { componentType: 5122, normalized: true, count: frames, type: 'VEC4' })
    const vec = (rows, type) => store(new Float32Array(rows.flat()), undefined, { componentType: 5126, count: frames, type })
    const tracks = move ? [
      { node: turn, path: 'translation', output: vec(move.poses.map((p) => [p.shift ?? 0, p.lift ?? 0, 0]), 'VEC3') },
      { node: turn, path: 'rotation', output: vec(move.poses.map((p) => qmul(quat([0, 1, 0], p.yaw ?? 0), quat([0, 0, 1], p.lean ?? 0))), 'VEC4') },
      { node: squash, path: 'translation', output: vec(move.poses.map((p) => [0, headLift + (p.head ?? 0), 0]), 'VEC3') },
      { node: squash, path: 'scale', output: vec(move.poses.map((p) => squashScale(p.squash ?? 0)), 'VEC3') },
      ...joints.flatMap((j, i) => {
        const axis = unit([j.at[2], 0, -j.at[0]]), out = unit([j.at[0], 0, j.at[2]]), local = joints[i - 1]?.at ?? [0, 0, 0]
        return [
          { node: firstJoint + i, path: 'rotation', output: vec(move.poses.map((p) => qmul(quat([0, 1, 0], p.arms[j.arm][j.bend]), quat(axis, p.droop ? p.droop[j.arm][j.bend] : 0))), 'VEC4') },
          ...(j.bend === 0 ? [{ node: firstJoint + i, path: 'translation', output: vec(move.poses.map((p) => j.at.map((v, k) => v + out[k] * (p.spread ?? 0))), 'VEC3') }] : []),
        ]
      }),
      ...(hasMedal ? [
        { node: medalSway, path: 'rotation', output: vec(move.poses.map((p) => quat([0, 1, 0], p.medal?.sway ?? 0)), 'VEC4') },
        { node: medalSway + 1, path: 'rotation', output: vec(move.poses.map((p) => quat([1, 0, 0], -(p.medal?.lift ?? 0))), 'VEC4') },
        { node: medalSway + 1, path: 'translation', output: vec(move.poses.map((p) => [0, medalHinge[0], medalHinge[1] + (p.spread ?? 0)]), 'VEC3') },
      ] : []),
    ] : [
      { node: turn, path: 'rotation', output: aroundY(baked.yaw) },
      { node: squash, path: 'scale', output: store(new Float32Array(baked.squash.flatMap(squashScale)), undefined, { componentType: 5126, count: frames, type: 'VEC3' }) },
      ...joints.map((j, i) => ({ node: firstJoint + i, path: 'rotation', output: aroundY(baked.arms[j.arm][j.bend]) })),
    ]
    rig = {
      skins,
      animations: [{
        name: move ? move.name : 'turn',
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

const colours = tokens.color.poulpy
const files = [
  ...['blue', 'ember'].flatMap((side) => [
    { path: `poulpy_${side}.glb`, model: () => buildPoulpy3d({ side, colours }), name: `Poulpy_${side}` },
    { path: `poulpy_${side}_turn.glb`, model: () => buildPoulpy3d({ side, colours }), name: `Poulpy_${side}`, moving: true },
  ]),
  ...['blue', 'ember'].map((side) => ({ path: `poulpy_${side}_jump.glb`, model: () => buildPoulpy3d({ side, colours }), name: `Poulpy_${side}`, move: () => bakeMove('jump', createJump(), round(JUMP)) })),
  { path: 'poulpy_champion_cheer.glb', model: () => buildPoulpyPose({ pose: 'champion', effects: false, tones: tokens.mood, colours }), name: 'Poulpy_champion', move: () => bakeMove('cheer', createCheer(), round(CHEER.jump)) },
]
let stale = 0
for (const { path: name, model, name: title, moving = false, move } of files) {
  const path = `design/poulpy-3d/${name}`, file = join(root, path)
  const bytes = glb(model(), title, moving, move?.())
  if (check) {
    if (!existsSync(file) || !readFileSync(file).equals(bytes)) { console.error(`Out of date: ${path}`); stale++ }
  } else {
    writeFileSync(file, bytes)
    console.log(`Wrote ${path} (${Math.round(bytes.length / 1024)} KB)`)
  }
}
if (stale) { console.error('Run `node design/build-poulpy-3d.mjs` and commit the result.'); process.exit(1) }
if (check) console.log('Poulpy 3D models are up to date.')
