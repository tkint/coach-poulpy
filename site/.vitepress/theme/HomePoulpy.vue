<script setup lang="ts">
// Poulpy on the docs home, as on the app's Home: tap him and he jumps, tap him again and again and he turns annoyed, then fuming,
// and left alone he calms down. It is the workshop's own component (the Home mockup's), not a copy of it, so its moves and
// tap counts stay the ones the mockup agreed; its look comes from the rules below, which the mockup keeps under `.cp-screen`.
import { MobilePoulpyMood } from '../../../web/storybook/stories/mobile/components'
</script>

<template>
  <div class="home-poulpy">
    <MobilePoulpyMood label="Poulpy" :size="300" />
    <p class="hint">Tap him</p>
  </div>
</template>

<style scoped>
.home-poulpy { display: flex; flex-direction: column; align-items: center; gap: 8px; }
.hint { margin: 0; font-size: 14px; color: var(--vp-c-text-2); }
.home-poulpy :deep(.poulpy-cheer) { position: relative; display: block; padding: 0; border: 0; background: none; cursor: pointer; }
.home-poulpy :deep(.poulpy-cheer img) { display: block; width: 100%; height: 100%; }
.home-poulpy :deep(.poulpy-cheer:focus-visible) { outline: 3px solid var(--vp-c-brand-1); outline-offset: 4px; border-radius: 24px; }
.home-poulpy :deep(.poulpy-cheer-stage) { position: absolute; pointer-events: none; }
.home-poulpy :deep(.poulpy-cheer-stage canvas) { display: block; width: 100%; height: 100%; }
</style>
