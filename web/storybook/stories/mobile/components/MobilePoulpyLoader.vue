<script setup lang="ts">
// The loading indicator on a phone: Poulpy's outline seen from above, swinging half a turn one way and then half a turn the
// other, forever, each of his eight tentacles hanging loose and bending about its base behind the turn. The app's PoulpyLoader
// does the same with LoaderSwing of the shared code. `size` is its width; `label` is what a screen reader hears (what is
// loading).
import { poulpyDrawings } from '@coach-poulpy/ui'

withDefaults(defineProps<{ size?: number; label?: string; spin?: 'swing' | 'clockwise' }>(), { size: 48, label: 'Loading', spin: 'swing' })
</script>

<template>
  <span class="poulpy-loader" :class="{ 'poulpy-loader--clockwise': spin === 'clockwise' }" role="status" :aria-label="label" :style="{ width: `${size}px`, height: `${size}px` }" data-component="poulpy-loader">
    <span v-for="index in 8" :key="index" class="poulpy-loader-tentacle" :style="{ '--tentacle': index - 1 }">
      <img :src="poulpyDrawings.loader_tentacle" alt="" aria-hidden="true">
    </span>
  </span>
</template>
