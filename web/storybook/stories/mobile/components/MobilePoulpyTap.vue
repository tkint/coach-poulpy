<script setup lang="ts">
// Poulpy on Home (decision 16, step 4): his still picture, which stays still. A tap plays one short animation for his
// current mood, in 3D, and he settles back to the picture. Only moods that have an animation respond: happy and proud
// jump; the others get theirs later. With reduced motion a tap does nothing. The apps do the same with their own 3D.
import { computed, onBeforeUnmount, ref } from 'vue'
import { poulpyDrawings } from '@coach-poulpy/ui'

const props = defineProps<{ mood: 'proud' | 'happy' | 'recovering' | 'annoyed' | 'grumpy' | 'fuming'; label: string }>()
const ANIMATED = ['proud', 'happy']
const picture = computed(() => (poulpyDrawings as Record<string, string>)[`mood_${props.mood}`])
const playing = ref(false)
const stage = ref<HTMLElement>()
const canvas = ref<HTMLCanvasElement>()
let frame = 0
let stop = () => {}

async function play() {
  if (playing.value || !ANIMATED.includes(props.mood) || window.matchMedia('(prefers-reduced-motion: reduce)').matches) return
  playing.value = true
  await Promise.resolve()
  if (!canvas.value) return
  const { createPoulpyScene } = await import('../../brand/poulpy3dScene')
  const { createJump } = await import('../../../../../design/poulpy-3d/motion.mjs')
  const scene = createPoulpyScene(canvas.value)
  scene.show(props.mood, false)
  scene.look({ turn: 0, tilt: 8, distance: 14.5 })
  const jump = createJump()
  let last = performance.now(), clock = 0, high = false
  const tick = (now: number) => {
    const dt = (now - last) / 1000
    last = now
    clock += dt
    const pose = jump.advance(dt)
    scene.pose(pose)
    if (pose.lift > 0.05) high = true
    // One jump: up, down, settled; or give up after a few seconds.
    if ((high && pose.lift < 0.004 && clock > 0.9) || clock > 3) return finish()
    frame = requestAnimationFrame(tick)
  }
  const finish = () => { stop(); stop = () => {}; playing.value = false }
  stop = () => { cancelAnimationFrame(frame); scene.dispose() }
  frame = requestAnimationFrame(tick)
}
onBeforeUnmount(() => stop())
</script>

<template>
  <button type="button" class="poulpy-tap" :aria-label="`${label}. Tap and he plays`" data-component="poulpy-tap" @click="play">
    <img :src="picture" alt="" :style="{ visibility: playing ? 'hidden' : 'visible' }" />
    <span v-if="playing" ref="stage" class="poulpy-tap-stage" aria-hidden="true"><canvas ref="canvas" /></span>
  </button>
</template>
