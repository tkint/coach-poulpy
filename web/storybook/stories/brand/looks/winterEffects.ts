// What is around Winter Poulpy, drawn with three.js next to him (poulpy3dScene.ts draws him): the falling snow and the puffs
// of his breath, from design/poulpy-3d/looks/winter.mjs. They are flat, as the comic effects are, and always face the viewer,
// but they are in the room: a flake falls where it is and a puff drifts off on its own.
import * as THREE from 'three'
import type { PoulpyShape } from '../../../../../design/poulpy-3d/model.mjs'
import { winterShapes, type WinterPose } from '../../../../../design/poulpy-3d/looks/winter.mjs'

// One geometry from shapes lying side by side, each in its colour.
function painted(pieces: { shape: PoulpyShape; colour: string }[]) {
  const positions: number[] = [], colours: number[] = [], indices: number[] = []
  for (const { shape, colour } of pieces) {
    const base = positions.length / 3, tint = new THREE.Color(colour)
    positions.push(...shape.positions)
    for (let i = 0; i < shape.positions.length / 3; i++) colours.push(tint.r, tint.g, tint.b)
    indices.push(...shape.indices.map((i) => i + base))
  }
  const geo = new THREE.BufferGeometry()
  geo.setAttribute('position', new THREE.Float32BufferAttribute(positions, 3))
  geo.setAttribute('color', new THREE.Float32BufferAttribute(colours, 3))
  geo.setIndex(indices)
  return geo
}

export function createWinterEffects(scene: THREE.Scene, camera: THREE.Camera, sizes: { flakes: number; puffs: number }) {
  const shapes = winterShapes()
  const flat = new THREE.MeshBasicMaterial({ vertexColors: true, side: THREE.DoubleSide })
  // Every flake is a six-armed crystal, all of them one mesh.
  const snow = new THREE.InstancedMesh(painted(shapes.crystal), flat, sizes.flakes)
  snow.frustumCulled = false
  scene.add(snow)
  // Each puff has its own material, so it fades on its own; it never hides what is behind it.
  const mist = painted(shapes.puff)
  const puffs = Array.from({ length: sizes.puffs }, () => {
    const mesh = new THREE.Mesh(mist, new THREE.MeshBasicMaterial({ vertexColors: true, transparent: true, depthWrite: false, side: THREE.DoubleSide }))
    mesh.renderOrder = 2
    mesh.visible = false
    scene.add(mesh)
    return mesh
  })
  const piece = new THREE.Object3D(), spin = new THREE.Quaternion(), forward = new THREE.Vector3(0, 0, 1)

  // Turns a piece to face the viewer, turned on itself by `angle`.
  const facingViewer = (angle: number) => piece.quaternion.copy(camera.quaternion).multiply(spin.setFromAxisAngle(forward, angle))

  /** Places the snow and the puffs of a pose of createWinter(); `snow` and `breath` show or hide each. */
  function update(pose: WinterPose, show: { snow: boolean; breath: boolean }) {
    pose.snow.forEach(([x, y, z, size, angle], i) => {
      piece.position.set(x, y, z)
      facingViewer(angle)
      piece.scale.setScalar(show.snow ? size : 0)
      piece.updateMatrix()
      snow.setMatrixAt(i, piece.matrix)
    })
    snow.instanceMatrix.needsUpdate = true
    pose.breath.forEach(([x, y, z, size, opacity, angle], i) => {
      const mesh = puffs[i]
      if (!mesh) return
      mesh.visible = show.breath && size > 0 && opacity > 0.01
      if (!mesh.visible) return
      mesh.position.set(x, y, z)
      mesh.quaternion.copy(facingViewer(angle))
      mesh.scale.setScalar(size)
      ;(mesh.material as THREE.MeshBasicMaterial).opacity = opacity
    })
  }

  function dispose() {
    scene.remove(snow); snow.geometry.dispose(); snow.dispose()
    for (const mesh of puffs) { scene.remove(mesh); (mesh.material as THREE.Material).dispose() }
    mist.dispose()
    flat.dispose()
  }

  return { update, dispose }
}
