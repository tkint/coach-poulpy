<script setup lang="ts">
// Poulpy's autumn look, live: design/poulpy-3d/looks/autumn.mjs, in ../PoulpyViewer.vue. He wears his scarf and sways while
// maple leaves fall around him. The leaves are flat and face the viewer as they fall, like the comic effects of the poses; once on
// the floor they lie flat on it.
import * as THREE from 'three'
import { shallowRef, watch } from 'vue'
import tokens from '../../../../../design/tokens.json'
import { AUTUMN, buildAutumn, createAutumn, leafShape, type AutumnPose } from '../../../../../design/poulpy-3d/looks/autumn.mjs'
import type { PoulpyView } from '../poulpy3dScene'
import PoulpyViewer, { type LivePoulpy, type PoulpyScene } from '../PoulpyViewer.vue'

const leavesShown = shallowRef(true)
let placeLeaves: (() => void) | undefined
watch(leavesShown, () => placeLeaves?.())

async function start(scene: PoulpyScene): Promise<LivePoulpy> {
  const move = createAutumn()
  let posed: AutumnPose = move.pose()
  let view: PoulpyView = { turn: 0, tilt: 0, distance: 0 }
  const leaves = new THREE.InstancedMesh(
    new THREE.BufferGeometry()
      .setAttribute('position', new THREE.Float32BufferAttribute(leafShape().positions, 3))
      .setAttribute('normal', new THREE.Float32BufferAttribute(leafShape().normals, 3))
      .setIndex(leafShape().indices),
    new THREE.MeshBasicMaterial({ side: THREE.DoubleSide }),
    posed.leaves.length,
  )
  leaves.frustumCulled = false
  const colours = AUTUMN.leaves.colours.map((colour) => new THREE.Color(colour))
  posed.leaves.forEach((leaf, i) => leaves.setColorAt(i, colours[leaf[7]]))
  scene.room.add(leaves)

  // Each leaf, falling, stands across the line of sight, turned in its own plane and half over; on the floor it lies flat.
  const facing = new THREE.Quaternion(), standing = new THREE.Quaternion(), lying = new THREE.Quaternion(), own = new THREE.Quaternion()
  const flat = new THREE.Quaternion().setFromAxisAngle(new THREE.Vector3(1, 0, 0), -Math.PI / 2)
  const along = new THREE.Euler(), piece = new THREE.Object3D()
  placeLeaves = () => {
    // The way the viewer looks, as the scene places its camera: walked around by the view's turn, looking down by its tilt.
    facing.setFromEuler(along.set(THREE.MathUtils.degToRad(-view.tilt), THREE.MathUtils.degToRad(view.turn), 0, 'YXZ'))
    posed.leaves.forEach(([x, y, z, roll, flip, lie, size], i) => {
      standing.copy(facing).multiply(own.setFromEuler(along.set(0, flip, roll, 'ZYX')))
      lying.setFromAxisAngle(THREE.Object3D.DEFAULT_UP, roll).multiply(flat)
      piece.position.set(x, y, z)
      piece.quaternion.slerpQuaternions(standing, lying, lie)
      piece.scale.setScalar(leavesShown.value ? size : 0)
      piece.updateMatrix()
      leaves.setMatrixAt(i, piece.matrix)
    })
    leaves.instanceMatrix.needsUpdate = true
    scene.pose(posed)
  }

  return {
    show(outline) {
      scene.show({ model: buildAutumn({ tones: tokens.mood, colours: tokens.color.poulpy }) }, outline)
      scene.pose(posed)
    },
    advance(seconds) {
      posed = move.advance(seconds)
      placeLeaves!()
    },
    viewed(next) {
      view = next
      placeLeaves!()
    },
    dispose() {
      placeLeaves = undefined
      scene.room.remove(leaves)
      leaves.geometry.dispose()
      ;(leaves.material as THREE.Material).dispose()
      leaves.dispose()
    },
  }
}
</script>

<template>
  <PoulpyViewer
    label="Poulpy in his autumn look: happy, in a white and red striped scarf, swaying gently as maple leaves fall around him."
    :framing="{ tilt: 20, distance: 10.4, centre: 1.5 }"
    :start="start"
    :values="() => ({ page: 'look: autumn', leaves: leavesShown })"
  >
    <template #toggles>
      <button type="button" class="toggle" :aria-pressed="leavesShown" @click="leavesShown = !leavesShown">Leaves</button>
    </template>
  </PoulpyViewer>
</template>
