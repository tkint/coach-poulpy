<script setup lang="ts">
// Poulpy as the champion on the end-of-session screen: his still picture, and when the screen opens he jumps for joy twice, with
// his medal and confetti, in 3D, and settles back to the picture. A tap plays it again. With reduced motion he only stands there.
// The mockup plays it live in the workshop; the phones play it baked from the model, frame by frame, as on Home (decision 16).
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { poulpyDrawings } from '@coach-poulpy/ui'

defineProps<{ label: string }>()
const PICTURE_HEIGHT = 155 // the picture as the screen draws it, 170 wide
const PAD = 90 // how much room the confetti has around it, in px
const SECONDS = 2.6
const picture = (poulpyDrawings as Record<string, string>).pose_champion
const playing = ref(false)
const canvas = ref<HTMLCanvasElement>()
let frame = 0
let stop = () => {}

async function play() {
  if (playing.value || window.matchMedia('(prefers-reduced-motion: reduce)').matches) return
  playing.value = true
  await Promise.resolve()
  if (!canvas.value) return
  try {
    const { createPoulpyScene } = await import('../../brand/poulpy3dScene')
    const { createCheer } = await import('../../../../../design/poulpy-3d/motion.mjs')
    const scene = createPoulpyScene(canvas.value)
    scene.show({ pose: 'champion', effects: false }, false)
    // The same shot as his still picture (design/build-poulpy-pictures.mjs), on a canvas that reaches PAD px further each way.
    scene.look({ turn: 0, tilt: 8, distance: 9.4, centre: 1.35, field: (2 * Math.atan(((PICTURE_HEIGHT + 2 * PAD) / PICTURE_HEIGHT) * Math.tan((14 * Math.PI) / 180)) * 180) / Math.PI })
    const cheer = createCheer()
    let last = performance.now(), clock = 0
    const tick = (now: number) => {
      const dt = (now - last) / 1000
      last = now
      clock += dt
      scene.pose(cheer.advance(dt))
      if (clock > SECONDS) return finish()
      frame = requestAnimationFrame(tick)
    }
    const finish = () => { stop(); stop = () => {}; playing.value = false }
    stop = () => { cancelAnimationFrame(frame); scene.dispose() }
    frame = requestAnimationFrame(tick)
  } catch {
    playing.value = false // no 3D in this browser: he just stands there
  }
}
onMounted(play)
onBeforeUnmount(() => stop())
</script>

<template>
  <button type="button" class="poulpy-cheer" :aria-label="`${label}. Tap and he jumps again`" data-component="poulpy-cheer" @click="play">
    <img :src="picture" alt="" :style="{ visibility: playing ? 'hidden' : 'visible' }" />
    <span v-if="playing" class="poulpy-cheer-stage" aria-hidden="true" :style="{ inset: `-${PAD}px` }"><canvas ref="canvas" /></span>
  </button>
</template>
