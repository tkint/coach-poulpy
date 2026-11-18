<script setup lang="ts">
// Poulpy with a jetpack, zooming from side to side (design/poulpy-3d/looks/jetpack.mjs, ZOOM in motion.mjs), in
// ../PoulpyViewer.vue. Its own slider loosens his tentacles.
import { shallowRef, watch } from 'vue'
import type { Zoom } from '../../../../../design/poulpy-3d/motion.mjs'
import PoulpyViewer, { type LivePoulpy, type PoulpyScene } from '../PoulpyViewer.vue'

const softness = shallowRef(1) // how loose his tentacles are: 1 is the design
let move: Zoom | undefined
watch(softness, (value) => { if (move) move.softness = value })

async function start(scene: PoulpyScene): Promise<LivePoulpy> {
  const [{ createZoom }, { JETPACK, NOZZLES }] = await Promise.all([
    import('../../../../../design/poulpy-3d/motion.mjs'), import('../../../../../design/poulpy-3d/looks/jetpack.mjs'),
  ])
  const zoom = createZoom({ softness: softness.value, nozzles: NOZZLES, flame: JETPACK.flames[0][1] })
  move = zoom
  return {
    show(outline) {
      scene.show({ look: 'jetpack' }, outline)
      scene.pose(zoom.pose())
    },
    advance: (seconds) => scene.pose(zoom.advance(seconds)),
    dispose() { move = undefined },
  }
}
</script>

<template>
  <PoulpyViewer
    label="Poulpy in 3D with a jetpack, zooming from side to side with a wide grin, flames and smoke under him and his tentacles trailing."
    :framing="{ tilt: 10, distance: 13.5, centre: 1.85 }"
    move-label="Fly"
    :start="start"
    :values="() => ({ page: 'look: jetpack', softArms: Math.round(softness * 1000) / 1000 })"
    :speeds="{ max: 2, step: 0.05 }"
  >
    <template #sliders>
      <label class="slider"><span>Soft arms</span><input v-model.number="softness" type="range" min="0" max="3" step="0.05" /><output>{{ softness.toFixed(2) }}</output></label>
    </template>
  </PoulpyViewer>
</template>
