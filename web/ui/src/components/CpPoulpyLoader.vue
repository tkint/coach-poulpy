<script setup lang="ts">
// The loading indicator: Poulpy's outline seen from underneath, drawn frame by frame from the 3D model with his tentacles loose,
// swinging half a turn one way and then half a turn the other, forever. `spin="clockwise"` only turns clockwise instead. The
// frames are one picture of 48 played in turn by the CSS of .cp-poulpy-loader (the phones' is LoaderSwing in the shared code).
// It is a status, so a screen reader hears `label`, which says "Loading" in the page's language unless the page says what is loading.
import { computed } from 'vue'
import { poulpyDrawings } from '../poulpy'
import { uiText } from '../i18n'

const props = withDefaults(defineProps<{ label?: string; size?: number; spin?: 'swing' | 'clockwise' }>(), { size: 48, spin: 'swing' })
const words = computed(() => props.label ?? uiText('ui.loading'))
const frames = computed(() => `url(${props.spin === 'clockwise' ? poulpyDrawings.loader_clockwise : poulpyDrawings.loader_swing})`)
</script>

<template>
  <span
    class="cp-poulpy-loader"
    :class="{ 'cp-poulpy-loader--clockwise': spin === 'clockwise' }"
    role="status"
    :aria-label="words"
    :style="{ '--cp-poulpy-loader-size': `${size}px`, '--cp-poulpy-loader-frames': frames }"
    data-component="poulpy-loader"
  >
    <span class="cp-poulpy-loader-frames" aria-hidden="true" />
  </span>
</template>
