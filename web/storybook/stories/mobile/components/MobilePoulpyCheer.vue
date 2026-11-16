<script setup lang="ts">
// Poulpy as the champion: his still picture, and he jumps for joy three times, with his medal and confetti, in 3D, and settles back to the
// picture. A tap plays it again. On the end-of-session screen he starts when it opens and never stops: jump after jump, with no rest and no
// confetti fading; on Home (`home`, a smaller picture in the ring) one round of three jumps plays at a tap, then he settles. With reduced motion he only stands there.
// The mockup plays it live in the workshop; the phones play it baked from the model, frame by frame (decision 16).
import { nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { poulpyDrawings } from '@coach-poulpy/ui'

const props = withDefaults(defineProps<{ label: string; home?: boolean }>(), { home: false })
const PAD_PER_HEIGHT = 110 / 210 // how much room the confetti has around the picture, in picture heights
const SECONDS = 3.6
const FADE = 0.7 // the confetti shrinks away over the last of it
const PAUSES = [0.3, 0.2, 0.34, 0.2] // the finished screen's non-stop jumps: the pauses between them, as in the phones' loop (design/build-poulpy-pictures.mjs)
const picture = (poulpyDrawings as Record<string, string>).pose_champion
const PICTURE_HEIGHT = props.home ? 95 : 210 // the picture as the screen draws it
const PAD = PICTURE_HEIGHT * PAD_PER_HEIGHT
const playing = ref(false)
const canvas = ref<HTMLCanvasElement>()
let frame = 0
let stop = () => {}

async function play() {
  if (playing.value || window.matchMedia('(prefers-reduced-motion: reduce)').matches) return
  playing.value = true
  // The canvas is drawn once `playing` is on the page.
  await nextTick()
  if (!canvas.value) { playing.value = false; return }
  try {
    const { createPoulpyScene } = await import('../../brand/poulpy3dScene')
    const { CHEER, createCheer } = await import('../../../../../design/poulpy-3d/motion.mjs')
    const scene = createPoulpyScene(canvas.value)
    scene.show({ pose: 'champion', effects: false }, false)
    // The same shot as his still picture (design/build-poulpy-pictures.mjs), on a canvas that reaches PAD px further each way.
    scene.look({ turn: 0, tilt: 8, distance: 9.4, centre: 1.35, field: (2 * Math.atan(((PICTURE_HEIGHT + 2 * PAD) / PICTURE_HEIGHT) * Math.tan((14 * Math.PI) / 180)) * 180) / Math.PI })
    // On Home, three jumps then rest; on the finished screen the jumps follow each other for good.
    const cheer = props.home ? createCheer() : createCheer({ cheer: { ...CHEER, jump: { ...CHEER.jump, pauses: PAUSES } } })
    let last = performance.now(), clock = 0
    const tick = (now: number) => {
      const dt = (now - last) / 1000
      last = now
      clock += dt
      // On Home the confetti shrinks away at the end, as on the phones, so he settles on his picture with none left; the finished screen goes on.
      scene.pose({ ...cheer.advance(dt), confettiScale: props.home ? Math.min(Math.max((SECONDS - clock) / FADE, 0), 1) : 1 } as never)
      if (props.home && clock > SECONDS) return finish()
      frame = requestAnimationFrame(tick)
    }
    const finish = () => {
      stop(); stop = () => {}; playing.value = false
    }
    stop = () => { cancelAnimationFrame(frame); scene.dispose() }
    frame = requestAnimationFrame(tick)
  } catch {
    playing.value = false // no 3D in this browser: he just stands there
  }
}
// On the end-of-session screen he starts when it opens; on Home only at a tap.
onMounted(() => { if (!props.home) play() })
onBeforeUnmount(() => stop())
</script>

<template>
  <button type="button" class="poulpy-cheer" :class="{ 'poulpy-cheer--home': home }" :aria-label="`${label}. Tap and he jumps`" data-component="poulpy-cheer" @click="play">
    <img :src="picture" alt="" :style="{ visibility: playing ? 'hidden' : 'visible' }" />
    <span v-if="playing" class="poulpy-cheer-stage" aria-hidden="true" :style="{ inset: `-${PAD}px` }"><canvas ref="canvas" /></span>
  </button>
</template>
