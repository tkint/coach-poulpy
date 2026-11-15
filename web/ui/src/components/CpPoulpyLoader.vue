<script setup lang="ts">
// The loading indicator: Poulpy's outline seen from above, swinging half a turn one way and then half a turn the other, forever (the
// swing is the CSS of .cp-poulpy-loader; the phones' is LoaderSwing in the shared code). It is a status, so a screen
// reader hears `label`, which says "Loading" in the page's language unless the page says what is loading.
import { computed } from 'vue'
import { poulpyDrawings } from '../poulpy'
import { uiText } from '../i18n'

const props = withDefaults(defineProps<{ label?: string; size?: number }>(), { size: 48 })
const words = computed(() => props.label ?? uiText('ui.loading'))
</script>

<template>
  <span class="cp-poulpy-loader" role="status" :aria-label="words" :style="{ '--cp-poulpy-loader-size': `${size}px` }" data-component="poulpy-loader">
    <img :src="poulpyDrawings.loader" alt="" aria-hidden="true" />
  </span>
</template>
