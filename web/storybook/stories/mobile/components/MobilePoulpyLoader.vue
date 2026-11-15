<script setup lang="ts">
// The loading indicator on a phone: Poulpy's outline seen from underneath, drawn frame by frame from the 3D model with his tentacles
// loose, swinging half a turn one way and then half a turn the other, forever (`spin="clockwise"` only turns clockwise). The app's
// PoulpyLoader does the same with LoaderSwing of the shared code. `size` is its width; `label` is what a screen reader hears
// (what is loading).
import { computed } from 'vue'
import { poulpyDrawings } from '@coach-poulpy/ui'

const props = withDefaults(defineProps<{ size?: number; label?: string; spin?: 'swing' | 'clockwise' }>(), { size: 48, label: 'Loading', spin: 'swing' })
const frames = computed(() => `url(${props.spin === 'clockwise' ? poulpyDrawings.loader_clockwise : poulpyDrawings.loader_swing})`)
</script>

<template>
  <span
    class="poulpy-loader"
    :class="{ 'poulpy-loader--clockwise': spin === 'clockwise' }"
    role="status"
    :aria-label="label"
    :style="{ width: `${size}px`, height: `${size}px`, '--frames': frames }"
    data-component="poulpy-loader"
  >
    <span class="poulpy-loader-frames" aria-hidden="true" />
  </span>
</template>
