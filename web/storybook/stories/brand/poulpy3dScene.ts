// Draws the Poulpy of design/poulpy-3d/model.mjs with three.js: flat cartoon shading, a light that stays
// in the room while he turns, the gleam that follows it and the optional outline. He is built on the
// joints of design/poulpy-3d/motion.mjs, so pose() can turn him, lift him and bend his arms. Poulpy3d.vue owns
// the controls; this file owns the canvas and everything WebGL, and gives it all back on dispose().
import * as THREE from 'three'
import tokens from '../../../../design/tokens.json'
import { buildPoulpy3d, gleamPatch, LOOK, type PoulpyMaterial, type PoulpyMood, type PoulpyPart, type PoulpySide } from '../../../../design/poulpy-3d/model.mjs'
import { buildPoulpyPose, type PoulpyPose } from '../../../../design/poulpy-3d/poses.mjs'
import { armJoints, armSkin, squashScale, type JumpPose, type TurnPose } from '../../../../design/poulpy-3d/motion.mjs'

/** Where the viewer stands: degrees around Poulpy, degrees above (or below) him, and how far away. */
export interface PoulpyView { turn: number; tilt: number; distance: number }

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
  let rig: { turn: THREE.Bone; squash: THREE.Group; bends: THREE.Bone[]; hinges: THREE.Vector3[]; rest: THREE.Vector3[]; skeleton: THREE.Skeleton } | undefined
  let posed: TurnPose | JumpPose | undefined
  const back = new THREE.Quaternion(), hang = new THREE.Quaternion(), sideways = new THREE.Vector3()
  let centre = 1
  // How his head is held when he stands in a pose: lifted with his arms, or leaning.
  let stance = { headLift: 0, headTilt: 0 }
  // The flat effects of a pose, kept facing the viewer in front of him and behind.
  const CARD_GAP = 1.9, CARD_CENTRE = 1.04
  let cards: { front: THREE.Group; back: THREE.Group } | undefined
  let view: PoulpyView = { turn: 0, tilt: 17, distance: 9.6 }

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
    const turn = THREE.MathUtils.degToRad(view.turn), tilt = THREE.MathUtils.degToRad(view.tilt)
    camera.position.set(view.distance * Math.sin(turn) * Math.cos(tilt), centre + view.distance * Math.sin(tilt), view.distance * Math.cos(turn) * Math.cos(tilt))
    camera.lookAt(0, centre, 0)
    camera.updateMatrixWorld()
    // The light and the gleam are placed from where the viewer stands, so they stay put in the room.
    if (cards) {
      // Each card stands across the line of sight, and is sized so its drawing looks as big as if it stood at his middle.
      const towards = camera.position.clone().sub(light.target.position.set(0, centre, 0)).normalize()
      for (const [card, way] of [[cards.front, 1], [cards.back, -1]] as const) {
        card.quaternion.copy(camera.quaternion)
        card.position.set(0, CARD_CENTRE, 0).addScaledVector(towards, way * CARD_GAP)
        card.scale.setScalar((view.distance - way * CARD_GAP) / view.distance)
      }
    }
    light.target.position.set(0, centre, 0)
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

  /** Builds Poulpy again for a side of the plush, one of his moods or one of his poses, with or without the outline. */
  function show(as: PoulpySide | PoulpyMood | { pose: PoulpyPose }, outline: boolean) {
    clear()
    const colours = tokens.color.poulpy
    const inPose = typeof as === 'object' ? buildPoulpyPose({ pose: as.pose, tones: tokens.mood, colours }) : undefined
    const model = inPose ?? (as === 'blue' || as === 'ember' ? buildPoulpy3d({ side: as, colours }) : buildPoulpy3d({ mood: as as PoulpyMood, tone: tokens.mood[as as PoulpyMood], colours }))
    centre = model.height / 2
    stance = { headLift: model.headLift, headTilt: inPose?.headTilt ?? 0 }
    cards = inPose ? { front: new THREE.Group(), back: new THREE.Group() } : undefined
    poulpy = new THREE.Group()
    const turn = new THREE.Bone(), squash = new THREE.Group(), joints = armJoints(), bends: THREE.Bone[] = []
    joints.forEach((joint, i) => {
      const bone = new THREE.Bone(), parent = joint.bend === 0 ? turn : bends[i - 1]
      bone.position.fromArray(joint.at).sub(joint.bend === 0 ? new THREE.Vector3() : new THREE.Vector3().fromArray(joints[i - 1].at))
      parent.add(bone)
      bends.push(bone)
    })
    turn.add(squash)
    poulpy.add(turn)
    poulpy.updateMatrixWorld(true)
    // An arm hangs around the level line across it, which tips its far end down.
    const hinges = joints.map((joint) => new THREE.Vector3(joint.at[2], 0, -joint.at[0]).normalize())
    rig = { turn, squash, bends, hinges, rest: bends.map((bone) => bone.position.clone()), skeleton: new THREE.Skeleton([turn, ...bends]) }
    // His head and face ride on `squash`; an arm is skinned, so each of its points follows its bends.
    const place = (part: PoulpyPart, geo: THREE.BufferGeometry, material: THREE.Material) => {
      const arm = /^arm_(\d+)/.exec(part.name)
      if (part.card && cards) {
        const mesh = new THREE.Mesh(geo, material)
        mesh.position.y = -CARD_CENTRE
        cards[part.card].add(mesh)
        return mesh
      }
      if (!arm) {
        const mesh = new THREE.Mesh(geo, material)
        // What he wears goes with his head; what lies by him stays on the floor.
        ;(/^(dumbbell|medal)/.test(part.name) ? poulpy! : squash).add(mesh)
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
        flat ? new THREE.MeshBasicMaterial({ color: tint, ...(opacity !== undefined && { transparent: true, opacity, depthWrite: false }) }) : new THREE.MeshToonMaterial({ color: tint, emissive: tint.clone().multiplyScalar(LOOK.shade), gradientMap: ramp }),
      )
      mesh.name = part.name
      if (part.name === 'gleam') gleam = mesh.geometry
      if (outline && edge) {
        // The outline is the same shape, a little bigger, seen from inside.
        const hull = geometry({ ...part, positions: part.positions.map((p, i) => p + part.normals[i] * LOOK.outline.width) })
        place(part, hull, new THREE.MeshBasicMaterial({ color: new THREE.Color().setRGB(edge[0], edge[1], edge[2]), side: THREE.BackSide }))
      }
    }
    scene.add(poulpy)
    if (cards) scene.add(cards.front, cards.back)
    pose(posed)
  }

  /** Puts him in a pose of his turn or his jump (design/poulpy-3d/motion.mjs); with none, he stands as modelled. */
  function pose(next?: TurnPose | JumpPose) {
    posed = next
    if (rig) {
      const jump = next && 'droop' in next ? next : undefined
      rig.turn.rotation.set(0, next?.yaw ?? 0, jump?.lean ?? 0, 'YXZ')
      rig.turn.position.y = jump?.lift ?? 0
      rig.squash.position.y = stance.headLift + (jump?.head ?? 0)
      rig.squash.rotation.z = stance.headTilt
      rig.squash.scale.fromArray(squashScale(next?.squash ?? 0))
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
    shadow.geometry.dispose()
    shadow.material.map?.dispose()
    shadow.material.dispose()
    ramp.dispose()
    renderer.dispose()
  }

  return { show, look, pose, dispose }
}
