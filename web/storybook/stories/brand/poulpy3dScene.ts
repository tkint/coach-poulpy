// Draws the Poulpy of design/poulpy-3d/model.mjs with three.js: flat cartoon shading, a light that stays
// in the room while he turns, the gleam that follows it and the optional outline. He is built on the
// joints of design/poulpy-3d/motion.mjs, so pose() can turn him, lift him, bend his arms, swing his medal, throw confetti, change his face and light his stars,
// and play the DJ's records and notes (design/poulpy-3d/looks/dj.mjs). Poulpy3d.vue owns
// the controls; this file owns the canvas and everything WebGL, and gives it all back on dispose().
import * as THREE from 'three'
import tokens from '../../../../design/tokens.json'
import { buildPoulpy3d, gleamPatch, LOOK, type Poulpy3d, type PoulpyMaterial, type PoulpyMood, type PoulpyPart, type PoulpySide } from '../../../../design/poulpy-3d/model.mjs'
import { buildPoulpyPose, CONFETTI, GOLDEN, medalHinge, poseMood, STARS, starShape, type PoulpyPose } from '../../../../design/poulpy-3d/poses.mjs'
import { armJoints, armSkin, squashScale, type CheerPose, type DjPose, type JumpPose, type TurnPose, type WinkPose } from '../../../../design/poulpy-3d/motion.mjs'
import { buildDj, DJ_KIT, DJ_NOTES, DJ_PLATTERS, noteShape } from '../../../../design/poulpy-3d/looks/dj.mjs'

/** Where the viewer stands: degrees around Poulpy, degrees above (or below) him, and how far away. */
export interface PoulpyView { turn: number; tilt: number; distance: number; /** The height he is looked at, half his height unless told. */ centre?: number; /** The view's height in degrees, 28 unless told. */ field?: number }

export function createPoulpyScene(canvas: HTMLCanvasElement) {
  const renderer = new THREE.WebGLRenderer({ canvas, antialias: true, alpha: true })
  renderer.setPixelRatio(Math.min(window.devicePixelRatio || 1, 2))
  const scene = new THREE.Scene()
  const camera = new THREE.PerspectiveCamera(28, 1, 0.1, 100)

  // One shadow band: the lit colour is the token colour, the shaded one is LOOK.shade of it.
  const ramp = new THREE.DataTexture(new Uint8Array([0, 0, 0, 255, 255, 255, 255, 255]), 2, 1, THREE.RGBAFormat)
  ramp.minFilter = ramp.magFilter = THREE.NearestFilter
  ramp.needsUpdate = true
  const light = new THREE.DirectionalLight(0xffffff, Math.PI * (1 - LOOK.shade))
  scene.add(light, light.target)

  // A soft round shadow on the floor, in the palette's deepest blue.
  const blot = document.createElement('canvas')
  blot.width = blot.height = 256
  const ink = blot.getContext('2d')!, fade = ink.createRadialGradient(128, 128, 0, 128, 128, 128)
  const [r, g, b] = [1, 3, 5].map((i) => parseInt(tokens.color.lagoon[900].slice(i, i + 2), 16))
  fade.addColorStop(0, `rgba(${r},${g},${b},0.38)`)
  fade.addColorStop(0.55, `rgba(${r},${g},${b},0.16)`)
  fade.addColorStop(1, `rgba(${r},${g},${b},0)`)
  ink.fillStyle = fade
  ink.fillRect(0, 0, 256, 256)
  const shadow = new THREE.Mesh(new THREE.PlaneGeometry(4, 4), new THREE.MeshBasicMaterial({ map: new THREE.CanvasTexture(blot), transparent: true, depthWrite: false }))
  shadow.rotation.x = -Math.PI / 2
  shadow.position.y = 0.005
  scene.add(shadow)

  let poulpy: THREE.Group | undefined
  let gleam: THREE.BufferGeometry | undefined
  // What moves him: `turn` faces, lifts and tilts him, `squash` holds his head and face, `bends` are the arms'
  // joints and `hinges` the line each one hangs around.
  // `slide` carries his medal around him and `medal` swings it up and down around its hinge.
  let rig: { turn: THREE.Bone; squash: THREE.Group; slide: THREE.Group; medal: THREE.Group; bends: THREE.Bone[]; hinges: THREE.Vector3[]; rest: THREE.Vector3[]; skeleton: THREE.Skeleton } | undefined
  let posed: TurnPose | JumpPose | CheerPose | WinkPose | DjPose | undefined
  // His face when it changes as he moves: the same face drawn at each step from 0 to 1, one shown at a time.
  let faces: THREE.Group[] = []
  const FACE_STEPS = 16
  // The cocky one's stars, made when he first winks: solid, in the room around him.
  let stars: THREE.Mesh[] = []
  // The champion's confetti, made when he first cheers: every piece is the same small card, in the room and not on him.
  let confetti: THREE.InstancedMesh | undefined
  // The DJ's records, each turning around its middle on the decks, and his notes, made when he first plays: flat, in the room, facing the viewer.
  let records: THREE.Group[] = []
  let notes: THREE.Group[] = []
  const piece = new THREE.Object3D()
  const back = new THREE.Quaternion(), hang = new THREE.Quaternion(), sideways = new THREE.Vector3(), spin = new THREE.Quaternion(), FORWARD = new THREE.Vector3(0, 0, 1)
  let centre = 1
  // How his head is held when he stands in a pose: lifted with his arms, or leaning.
  let stance = { headLift: 0, headTilt: 0 }
  // The flat effects of a pose, kept facing the viewer in front of him and behind.
  const CARD_GAP = 1.9, CARD_CENTRE = 1.04
  let cards: { front: THREE.Group; back: THREE.Group } | undefined
  let view: PoulpyView = { turn: 0, tilt: 17, distance: 9.6 }

  const toon = (tint: THREE.Color) => new THREE.MeshToonMaterial({ color: tint, emissive: tint.clone().multiplyScalar(LOOK.shade), gradientMap: ramp })
  const geometry = (shape: { positions: number[]; normals: number[]; indices: number[] }) => {
    const geo = new THREE.BufferGeometry()
    geo.setAttribute('position', new THREE.Float32BufferAttribute(shape.positions, 3))
    geo.setAttribute('normal', new THREE.Float32BufferAttribute(shape.normals, 3))
    geo.setIndex(shape.indices)
    return geo
  }
  const clear = () => {
    if (!poulpy) return
    rig?.skeleton.dispose()
    scene.remove(poulpy)
    if (cards) scene.remove(cards.front, cards.back)
    for (const group of [poulpy, ...(cards ? [cards.front, cards.back] : [])]) group.traverse((node) => {
      if (!(node instanceof THREE.Mesh)) return
      node.geometry.dispose()
      node.material.dispose()
    })
  }

  function render() {
    const here = view.centre ?? centre
    if (camera.fov !== (view.field ?? 28)) { camera.fov = view.field ?? 28; camera.updateProjectionMatrix() }
    const turn = THREE.MathUtils.degToRad(view.turn), tilt = THREE.MathUtils.degToRad(view.tilt)
    camera.position.set(view.distance * Math.sin(turn) * Math.cos(tilt), here + view.distance * Math.sin(tilt), view.distance * Math.cos(turn) * Math.cos(tilt))
    camera.lookAt(0, here, 0)
    camera.updateMatrixWorld()
    // The light and the gleam are placed from where the viewer stands, so they stay put in the room.
    if (cards) {
      // Each card stands across the line of sight, and is sized so its drawing looks as big as if it stood at his middle.
      const towards = camera.position.clone().sub(light.target.position.set(0, here, 0)).normalize()
      for (const [card, way] of [[cards.front, 1], [cards.back, -1]] as const) {
        card.quaternion.copy(camera.quaternion)
        card.position.set(0, CARD_CENTRE, 0).addScaledVector(towards, way * CARD_GAP)
        card.scale.setScalar((view.distance - way * CARD_GAP) / view.distance)
      }
    }
    // A note stands across the line of sight, then rocks in its own plane.
    for (const n of notes) n.quaternion.copy(camera.quaternion).multiply(spin.setFromAxisAngle(FORWARD, n.userData.rock ?? 0))
    light.target.position.set(0, here, 0)
    light.position.set(...(LOOK.light as [number, number, number])).normalize().transformDirection(camera.matrixWorld).multiplyScalar(10).add(light.target.position)
    if (gleam) {
      // gleamPatch works in Poulpy's own space, so the direction is turned back by however far he has turned and tilted.
      const towards = new THREE.Vector3(...(LOOK.gleam.towards as [number, number, number])).normalize().transformDirection(camera.matrixWorld)
      if (rig) towards.applyQuaternion(back.copy(rig.turn.quaternion).invert())
      const patch = gleamPatch(towards.toArray())
      gleam.setAttribute('position', new THREE.Float32BufferAttribute(patch.positions, 3))
      gleam.setAttribute('normal', new THREE.Float32BufferAttribute(patch.normals, 3))
    }
    renderer.render(scene, camera)
  }

  /**
   * Builds Poulpy again for a side of the plush, one of his moods or one of his poses, with or without the outline.
   * A pose with `effects: false` leaves out the flat effects around him, for when he moves, and one with `faces`
   * takes his face from it instead: the face at each step from 0 to 1, for pose() to pick from. `{ model }` shows a Poulpy built
   * elsewhere, a look (design/poulpy-3d/looks/): what he wears rides on his head. The DJ (`{ look: 'dj' }`) is built by its own
   * file, without its still effects: they move when he plays.
   */
  function show(as: PoulpySide | PoulpyMood | { pose: PoulpyPose; effects?: boolean; faces?: (t: number) => (string | number)[][] } | { model: Poulpy3d & { headTilt?: number } } | { look: 'dj' }, outline: boolean) {
    clear()
    const colours = tokens.color.poulpy
    const changing = typeof as === 'object' && 'pose' in as ? as.faces : undefined
    const inPose = typeof as !== 'object' ? undefined
      : 'model' in as ? as.model
      : 'look' in as ? buildDj({ tones: tokens.mood, colours, effects: false })
      : buildPoulpyPose({ pose: as.pose, effects: as.effects, ...(changing && { face: [] }), tones: tokens.mood, colours })
    const model = inPose ?? (as === 'blue' || as === 'ember' ? buildPoulpy3d({ side: as, colours }) : buildPoulpy3d({ mood: as as PoulpyMood, tone: tokens.mood[as as PoulpyMood], colours }))
    centre = model.height / 2
    stance = { headLift: model.headLift, headTilt: inPose?.headTilt ?? 0 }
    cards = inPose ? { front: new THREE.Group(), back: new THREE.Group() } : undefined
    poulpy = new THREE.Group()
    const turn = new THREE.Bone(), squash = new THREE.Group(), slide = new THREE.Group(), medal = new THREE.Group(), joints = armJoints(), bends: THREE.Bone[] = []
    joints.forEach((joint, i) => {
      const bone = new THREE.Bone(), parent = joint.bend === 0 ? turn : bends[i - 1]
      bone.position.fromArray(joint.at).sub(joint.bend === 0 ? new THREE.Vector3() : new THREE.Vector3().fromArray(joints[i - 1].at))
      parent.add(bone)
      bends.push(bone)
    })
    medal.position.set(0, medalHinge[0], medalHinge[1])
    slide.add(medal)
    turn.add(squash, slide)
    poulpy.add(turn)
    poulpy.updateMatrixWorld(true)
    // An arm hangs around the level line across it, which tips its far end down.
    const hinges = joints.map((joint) => new THREE.Vector3(joint.at[2], 0, -joint.at[0]).normalize())
    rig = { turn, squash, slide, medal, bends, hinges, rest: bends.map((bone) => bone.position.clone()), skeleton: new THREE.Skeleton([turn, ...bends]) }
    // The DJ's records turn around their middles; they lie on the floor with the decks, not on him.
    records = DJ_PLATTERS.map((at) => { const record = new THREE.Group(); record.position.fromArray(at); poulpy!.add(record); return record })
    // His head and face ride on `squash`; an arm is skinned, so each of its points follows its bends.
    const place = (part: PoulpyPart, geo: THREE.BufferGeometry, material: THREE.Material) => {
      const arm = /^arm_(\d+)/.exec(part.name)
      if (part.card && cards) {
        const mesh = new THREE.Mesh(geo, material)
        mesh.position.y = -CARD_CENTRE
        cards[part.card].add(mesh)
        return mesh
      }
      const record = /^decks_record_(\d+)/.exec(part.name)
      if (record) {
        const mesh = new THREE.Mesh(geo, material)
        mesh.position.fromArray(DJ_PLATTERS[+record[1] - 1]).negate()
        records[+record[1] - 1].add(mesh)
        return mesh
      }
      if (!arm) {
        const mesh = new THREE.Mesh(geo, material)
        // What he wears goes with his head, his medal hangs from its hinge and what lies by him stays on the floor.
        if (part.name.startsWith('medal')) mesh.position.copy(medal.position).negate()
        ;(part.name.startsWith('medal') ? medal : /^(dumbbell|decks)/.test(part.name) ? poulpy! : squash).add(mesh)
        return mesh
      }
      const skin = armSkin(+arm[1] - 1, part.positions)
      geo.setAttribute('skinIndex', new THREE.Uint16BufferAttribute(skin.joints, 4))
      geo.setAttribute('skinWeight', new THREE.Float32BufferAttribute(skin.weights, 4))
      const mesh = new THREE.SkinnedMesh(geo, material)
      mesh.frustumCulled = false
      poulpy!.add(mesh)
      mesh.bind(rig!.skeleton)
      return mesh
    }
    for (const part of model.parts) {
      const { colour, flat, outline: edge, opacity } = model.materials[part.material] as PoulpyMaterial
      const tint = new THREE.Color().setRGB(colour[0], colour[1], colour[2])
      const mesh = place(
        part,
        geometry(part),
        flat ? new THREE.MeshBasicMaterial({ color: tint, ...(opacity !== undefined && { transparent: true, opacity, depthWrite: false }) }) : toon(tint),
      )
      mesh.name = part.name
      if (part.name === 'gleam') gleam = mesh.geometry
      if (outline && edge) {
        // The outline is the same shape, a little bigger, seen from inside.
        const hull = geometry({ ...part, positions: part.positions.map((p, i) => p + part.normals[i] * LOOK.outline.width) })
        place(part, hull, new THREE.MeshBasicMaterial({ color: new THREE.Color().setRGB(edge[0], edge[1], edge[2]), side: THREE.BackSide }))
      }
    }
    faces = []
    if (changing && typeof as === 'object' && 'pose' in as) {
      const { mood } = poseMood(as.pose)
      for (let step = 0; step <= FACE_STEPS; step++) {
        const face = buildPoulpy3d({ mood, tone: tokens.mood[mood], features: changing(step / FACE_STEPS), faceOnly: true, colours }), group = new THREE.Group()
        for (const part of face.parts) {
          const { colour, flat } = face.materials[part.material] as PoulpyMaterial, tint = new THREE.Color().setRGB(colour[0], colour[1], colour[2])
          group.add(new THREE.Mesh(geometry(part), flat ? new THREE.MeshBasicMaterial({ color: tint }) : toon(tint)))
        }
        squash.add(group)
        faces.push(group)
      }
    }
    scene.add(poulpy)
    if (cards) scene.add(cards.front, cards.back)
    pose(posed)
  }

  /** Puts him in a pose of his turn, his jump, his cheer, his wink or the DJ's playing (design/poulpy-3d/motion.mjs); with none, he stands as modelled. */
  function pose(next?: TurnPose | JumpPose | CheerPose | WinkPose | DjPose) {
    posed = next
    if (rig) {
      const jump = next && 'droop' in next ? next : undefined, cheer = next && 'medal' in next ? next : undefined, wink = next && 'wink' in next ? next : undefined
      const dj = next && 'records' in next ? next : undefined
      // A pose with `tilt` leans his head: the wink's, the DJ's bob, or a look's idle.
      const leaning = next && 'tilt' in next ? next : undefined
      rig.turn.rotation.set(0, next?.yaw ?? 0, jump?.lean ?? 0, 'YXZ')
      rig.turn.position.y = jump?.lift ?? 0
      rig.squash.position.y = stance.headLift + (jump?.head ?? 0)
      rig.squash.rotation.z = stance.headTilt + (leaning?.tilt ?? 0)
      // The DJ nods around the floor under his middle, so his head bobs forward and his arms stay put.
      rig.squash.rotation.x = dj?.nod ?? 0
      records.forEach((record, i) => { record.rotation.y = dj?.records[i] ?? 0 })
      if (dj) play(dj.notes)
      rig.squash.scale.fromArray(squashScale(next?.squash ?? 0))
      // His medal rests on his arm, so it stays on it when his head tucks in and slides out with it.
      rig.medal.position.z = medalHinge[1] + (jump?.spread ?? 0)
      rig.medal.rotation.x = -(cheer?.medal.lift ?? 0)
      rig.slide.rotation.y = cheer?.medal.sway ?? 0
      if (cheer) fly(cheer.confetti, (cheer as { confettiScale?: number }).confettiScale ?? 1)
      faces.forEach((face, step) => { face.visible = step === Math.round((wink?.wink ?? 0) * FACE_STEPS) })
      if (wink) twinkle(wink.stars)
      rig.bends.forEach((bone, i) => {
        const arm = Math.floor(i / 3), bend = i % 3
        bone.quaternion.setFromAxisAngle(THREE.Object3D.DEFAULT_UP, next?.arms[arm][bend] ?? 0)
        if (jump) bone.quaternion.multiply(hang.setFromAxisAngle(rig!.hinges[i], jump.droop[arm][bend]))
        // Each arm slides outwards from where it joins him: its first joint is straight out from his middle.
        if (bend === 0) bone.position.copy(rig!.rest[i]).addScaledVector(sideways.set(rig!.rest[i].x, 0, rig!.rest[i].z).normalize(), jump?.spread ?? 0)
      })
      // His shadow gathers under him as he leaves the floor.
      shadow.scale.setScalar(1 - Math.min((jump?.lift ?? 0) * 0.4, 0.3))
    }
    render()
  }

  function fly(pieces: number[][], size = 1) {
    if (!confetti) {
      const [wide, tall] = CONFETTI.size.map((size) => size / 64)
      confetti = new THREE.InstancedMesh(new THREE.PlaneGeometry(wide, tall), new THREE.MeshBasicMaterial({ side: THREE.DoubleSide }), pieces.length)
      confetti.frustumCulled = false
      const colours = CONFETTI.colours.map((colour) => new THREE.Color(colour === 'ember' ? tokens.color.poulpy.ember : colour))
      pieces.forEach((_, i) => confetti!.setColorAt(i, colours[i % colours.length]))
      scene.add(confetti)
    }
    pieces.forEach(([x, y, z, turnX, turnY, turnZ], i) => {
      piece.position.set(x, y, z)
      piece.rotation.set(turnX, turnY, turnZ, 'YXZ')
      // A piece that has not flown yet is under the floor.
      piece.scale.setScalar(y < 0 ? 0 : size)
      piece.updateMatrix()
      confetti!.setMatrixAt(i, piece.matrix)
    })
    confetti.instanceMatrix.needsUpdate = true
  }

  function play(all: DjPose['notes']) {
    if (!notes.length) {
      notes = DJ_NOTES.map(({ kind, colour }) => {
        const shape = noteShape(kind), group = new THREE.Group()
        group.add(new THREE.Mesh(geometry(shape.edge), new THREE.MeshBasicMaterial({ color: new THREE.Color(DJ_KIT.colours.white) })))
        group.add(new THREE.Mesh(geometry(shape.fill), new THREE.MeshBasicMaterial({ color: new THREE.Color(colour) })))
        scene.add(group)
        return group
      })
    }
    all.forEach(({ at, size, rock }, i) => {
      notes[i].position.fromArray(at)
      notes[i].scale.setScalar(Math.max(size, 1e-4))
      notes[i].visible = size > 0.002
      notes[i].userData.rock = rock
    })
  }

  function twinkle(all: WinkPose['stars']) {
    if (!stars.length) {
      stars = STARS.map(({ at }) => {
        const star = new THREE.Mesh(geometry(starShape()), toon(new THREE.Color(GOLDEN)))
        star.position.fromArray(at)
        scene.add(star)
        return star
      })
    }
    all.forEach(({ size, spin, rock }, i) => {
      stars[i].scale.setScalar(STARS[i].size * size)
      stars[i].rotation.set(0, spin, rock, 'ZYX')
    })
  }

  /**
   * Moves the flat puffs of his fumes (the fuming pose's effects, behind him): they bob and drift, by `boost` more when he is angrier.
   * `seconds` is any clock that keeps running.
   */
  function fumes(seconds: number, boost = 0) {
    if (!cards) return
    cards.back.children.forEach((puff, i) => {
      // Each puff is four ovals, then the strokes: the ovals of one puff move together.
      const group = Math.floor(i / 4), t = seconds * (2.2 + boost * 2) + group * 1.7, sway = 0.05 + 0.1 * boost
      puff.position.set(Math.sin(t * 0.8) * sway * 0.6, -CARD_CENTRE + Math.sin(t) * sway + boost * 0.05, 0)
    })
    render()
  }

  function look(next: PoulpyView) {
    view = next
    render()
  }

  const fit = () => {
    const box = canvas.parentElement!.getBoundingClientRect()
    if (!box.width || !box.height) return
    renderer.setSize(box.width, box.height, false)
    camera.aspect = box.width / box.height
    camera.updateProjectionMatrix()
    render()
  }
  const resizing = new ResizeObserver(fit)
  resizing.observe(canvas.parentElement!)
  fit()

  function dispose() {
    resizing.disconnect()
    clear()
    for (const star of stars) { star.geometry.dispose(); (star.material as THREE.Material).dispose() }
    for (const n of notes) n.traverse((node) => { if (node instanceof THREE.Mesh) { node.geometry.dispose(); (node.material as THREE.Material).dispose() } })
    confetti?.geometry.dispose()
    ;(confetti?.material as THREE.Material | undefined)?.dispose()
    confetti?.dispose()
    shadow.geometry.dispose()
    shadow.material.map?.dispose()
    shadow.material.dispose()
    ramp.dispose()
    renderer.dispose()
  }

  // `room` is the three.js scene, for a look to add what goes on around him (falling leaves); it removes and disposes of it itself.
  return { show, look, pose, fumes, dispose, room: scene }
}
