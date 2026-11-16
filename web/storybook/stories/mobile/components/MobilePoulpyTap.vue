<script setup lang="ts">
// Poulpy on Home (decision 16, step 4): his still picture, which stays still. A tap plays one short move for his current mood,
// frame by frame from the 3D model (design/poulpy-3d/moves; PoulpyMoves in the shared code is the phones' version), and he settles
// back to the picture. Only moods that have a move respond: happy and proud jump, leaning to his left or his right at random; the
// others get theirs later. With reduced motion a tap does nothing.
import { computed, onBeforeUnmount, ref } from 'vue'
import { poulpyDrawings, poulpyMoves } from '@coach-poulpy/ui'

const props = defineProps<{ mood: 'proud' | 'happy' | 'recovering' | 'annoyed' | 'grumpy' | 'fuming'; label: string }>()
const MOVE_MILLIS = 1250
const picture = computed(() => (poulpyDrawings as Record<string, string>)[`mood_${props.mood}`])
const playing = ref<string>()
let timer: ReturnType<typeof setTimeout> | undefined

function play() {
  const sides = ['left', 'right']
  const sheet = poulpyMoves[`mood_${props.mood}_${sides[Math.random() < 0.5 ? 0 : 1]}`]
  if (playing.value || !sheet || window.matchMedia('(prefers-reduced-motion: reduce)').matches) return
  playing.value = sheet
  timer = setTimeout(() => { playing.value = undefined }, MOVE_MILLIS)
}
onBeforeUnmount(() => clearTimeout(timer))
</script>

<template>
  <button type="button" class="poulpy-tap" :aria-label="`${label}. Tap and he plays`" data-component="poulpy-tap" @click="play">
    <img :src="picture" alt="" :style="{ visibility: playing ? 'hidden' : 'visible' }" />
    <span v-if="playing" class="poulpy-tap-stage" aria-hidden="true"><span class="poulpy-tap-frames" :style="{ backgroundImage: `url(${playing})` }" /></span>
  </button>
</template>
