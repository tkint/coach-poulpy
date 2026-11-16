<script setup lang="ts">
// Poulpy as the champion: his still picture, and he jumps for joy three times, with his medal and confetti, in 3D, and settles back to the
// picture. A tap plays it again. On the end-of-session screen he starts when it opens and goes on and on (a rest between two rounds); on Home
// (`home`, a smaller picture in the ring) he only plays at a tap. With reduced motion he only stands there.
// The mockup plays it live in the workshop; the phones play it baked from the model, frame by frame (decision 16).
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { poulpyDrawings } from '@coach-poulpy/ui'

const props = withDefaults(defineProps<{ label: string; home?: boolean }>(), { home: false })
const PAD_PER_HEIGHT = 110 / 210 // how much room the confetti has around the picture, in picture heights
const SECONDS = 3.6
const FADE = 0.7 // the confetti shrinks away over the last of it
const REST_MILLIS = 1500 // on the end-of-session screen, how long he stands between two rounds
const picture = (poulpyDrawings as Record<string, string>).pose_champion
const PICTURE_HEIGHT = props.home ? 95 : 210 // the picture as the screen draws it
const PAD = PICTURE_HEIGHT * PAD_PER_HEIGHT
const playing = ref(false)
const canvas = ref<HTMLCanvasElement>()
let frame = 0
let timer: ReturnType<typeof setTimeout> | undefined
let stop = () => {}

async function play() {
  clearTimeout(timer)
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
      // The confetti shrinks away at the end, as on the phones, so he settles on his picture with none left.
      scene.pose({ ...cheer.advance(dt), confettiScale: Math.min(Math.max((SECONDS - clock) / FADE, 0), 1) } as never)
      if (clock > SECONDS) return finish()
      frame = requestAnimationFrame(tick)
    }
    const finish = () => {
      stop(); stop = () => {}; playing.value = false
      // On the end-of-session screen he goes on and on.
      if (!props.home) timer = setTimeout(play, REST_MILLIS)
    }
    stop = () => { cancelAnimationFrame(frame); scene.dispose() }
    frame = requestAnimationFrame(tick)
  } catch {
    playing.value = false // no 3D in this browser: he just stands there
  }
}
// On the end-of-session screen he starts when it opens; on Home only at a tap.
onMounted(() => { if (!props.home) play() })
onBeforeUnmount(() => { clearTimeout(timer); stop() })
</script>

<template>
  <button type="button" class="poulpy-cheer" :class="{ 'poulpy-cheer--home': home }" :aria-label="`${label}. Tap and he jumps`" data-component="poulpy-cheer" @click="play">
    <img :src="picture" alt="" :style="{ visibility: playing ? 'hidden' : 'visible' }" />
    <span v-if="playing" class="poulpy-cheer-stage" aria-hidden="true" :style="{ inset: `-${PAD}px` }"><canvas ref="canvas" /></span>
  </button>
</template>
