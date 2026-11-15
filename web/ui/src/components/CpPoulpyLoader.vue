<script setup lang="ts">
// The loading indicator: Poulpy's outline seen from above, swinging half a turn one way and then half a turn the other,
// forever, with each of his eight tentacles hanging loose, bending about its base behind the turn. It is one tentacle
// picture drawn eight times (the CSS of .cp-poulpy-loader; the phones' is LoaderSwing in the shared code, whose BASE the
// transform-origin repeats). `spin="clockwise"` only turns clockwise instead, a full turn every cycle. It is a status, so a screen reader hears `label`, which says "Loading" in the page's
// language unless the page says what is loading.
import { computed } from 'vue'
import { poulpyDrawings } from '../poulpy'
import { uiText } from '../i18n'

const props = withDefaults(defineProps<{ label?: string; size?: number; spin?: 'swing' | 'clockwise' }>(), { size: 48, spin: 'swing' })
const words = computed(() => props.label ?? uiText('ui.loading'))
</script>

<template>
  <span class="cp-poulpy-loader" :class="{ 'cp-poulpy-loader--clockwise': spin === 'clockwise' }" role="status" :aria-label="words" :style="{ '--cp-poulpy-loader-size': `${size}px` }" data-component="poulpy-loader">
    <span v-for="index in 8" :key="index" class="cp-poulpy-loader-tentacle" :style="{ '--cp-tentacle': index - 1 }">
      <img :src="poulpyDrawings.loader_tentacle" alt="" aria-hidden="true" />
    </span>
  </span>
</template>
