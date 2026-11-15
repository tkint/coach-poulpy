<script setup lang="ts">
// The loading indicator on a phone: Poulpy's outline seen from above, swinging half a turn one way and then half a turn the other, forever. The
// app's PoulpyLoader does the same with LoaderSwing of the shared code. `size` is its width; `label` is what a screen reader
// hears (what is loading).
import { poulpyDrawings } from '@coach-poulpy/ui'

withDefaults(defineProps<{ size?: number; label?: string }>(), { size: 48, label: 'Loading' })
</script>

<template>
  <span class="poulpy-loader" role="status" :aria-label="label" :style="{ width: `${size}px`, height: `${size}px` }" data-component="poulpy-loader">
    <img :src="poulpyDrawings.loader" alt="" aria-hidden="true">
  </span>
</template>
