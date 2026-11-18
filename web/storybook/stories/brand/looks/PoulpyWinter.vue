<script setup lang="ts">
// Winter Poulpy, live: his look from design/poulpy-3d/looks/winter.mjs, in ../PoulpyViewer.vue, with the snow and his breath
// drawn next to him by winterEffects.ts. He sways and breathes on his own; the viewer can walk around him.
import { shallowRef, watch } from 'vue'
import PoulpyViewer, { type LivePoulpy, type PoulpyScene } from '../PoulpyViewer.vue'

const snow = shallowRef(true)
const breath = shallowRef(true)
let draw: ((seconds: number) => void) | undefined
// A switch flipped while he is still is seen at once.
watch([snow, breath], () => draw?.(0))

async function start(scene: PoulpyScene): Promise<LivePoulpy> {
  const [{ createWinterEffects }, { buildWinterPoulpy, createWinter, WINTER }, tokens] = await Promise.all([
    import('./winterEffects'),
    import('../../../../../design/poulpy-3d/looks/winter.mjs'),
    import('../../../../../design/tokens.json'),
  ])
  const model = { model: buildWinterPoulpy({ tones: tokens.default.mood, colours: tokens.default.color.poulpy }) }
  const effects = createWinterEffects(scene.room, scene.camera, { flakes: WINTER.snow.flakes, puffs: WINTER.puffs.count * 3 })
  const move = createWinter()
  // A few seconds in, so the snow is already falling and a breath is in the air.
  for (let i = 0; i < 59; i++) move.advance(0.1)
  draw = (seconds) => {
    const pose = move.advance(seconds)
    effects.update(pose, { snow: snow.value, breath: breath.value })
    scene.pose(pose)
  }
  return {
    show: (outline) => scene.show(model, outline),
    advance: (seconds) => draw?.(seconds),
    // The snow and his breath turn to face the viewer again, even while he stands still.
    viewed: () => draw?.(0),
    dispose() {
      draw = undefined
      effects.dispose()
    },
  }
}
</script>

<template>
  <PoulpyViewer
    label="Winter Poulpy in 3D: happy in fluffy ear muffs and a scarf, swaying gently while snow falls and his breath puffs out as mist."
    :framing="{ tilt: 14, distance: 10.4, centre: 1.45 }"
    :start="start"
    :values="() => ({ page: 'look: winter', snow, breath })"
  >
    <template #toggles>
      <button type="button" class="toggle" :aria-pressed="snow" @click="snow = !snow">Snow</button>
      <button type="button" class="toggle" :aria-pressed="breath" @click="breath = !breath">Breath</button>
    </template>
  </PoulpyViewer>
</template>
