<script setup lang="ts">
// Poulpy the DJ (design/poulpy-3d/looks/dj.mjs), live in ../PoulpyViewer.vue, playing his decks (DJ in motion.mjs): his head
// bobs to the beat, he scratches one record while the other turns, and notes float up. Still, he stands at the decks with no notes.
import PoulpyViewer, { type LivePoulpy, type PoulpyScene } from '../PoulpyViewer.vue'

async function start(scene: PoulpyScene): Promise<LivePoulpy> {
  const { createDj } = await import('../../../../../design/poulpy-3d/motion.mjs')
  const move = createDj()
  return {
    show: (outline) => scene.show({ look: 'dj' }, outline),
    advance: (seconds) => scene.pose(move.advance(seconds)),
  }
}
</script>

<template>
  <PoulpyViewer
    label="Poulpy the DJ, playing his decks and bobbing his head as music notes float up around him."
    :framing="{ tilt: 26, distance: 9.6 }"
    move-label="Play"
    :start="start"
    :values="() => ({ page: 'look: dj' })"
    :speeds="{ max: 5, step: 0.1 }"
  />
</template>
