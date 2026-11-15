<script setup lang="ts">
// Poulpy, as the picture of poulpy.ts: painted from the 3D model, or the loader's drawing. Decorative unless a
// label is given, since the words around him carry the message.
import { computed } from 'vue'
import { poulpyDrawings, type PoulpyDrawing } from '../poulpy'

const props = withDefaults(defineProps<{ drawing?: PoulpyDrawing; label?: string; size?: number }>(), {
  drawing: 'mood_happy',
  size: 96,
})
const src = computed(() => poulpyDrawings[props.drawing])
</script>

<template>
  <img
    class="cp-poulpy"
    :src="src"
    :alt="label ?? ''"
    :aria-hidden="label ? undefined : 'true'"
    :style="{ '--cp-poulpy-size': `${size}px` }"
    data-component="poulpy"
  />
</template>
