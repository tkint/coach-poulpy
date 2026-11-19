<script setup lang="ts">
import MobileIconButton from './MobileIconButton.vue'

// The one top bar of every phone screen: 44 under the status bar, the title in `headline` (21), never larger.
// A screen reached from another starts with `leading` (a back chevron, or a close cross for a screen that
// ends a task); a tab's root screen has none. The trailing actions go in the default slot, at most two,
// and an action is 40 tall (44 to touch). Anything else a screen needs sits below the bar, never in it.
defineProps<{ title: string; leading?: 'back' | 'close' }>()
</script>

<template>
  <header class="app-bar" data-component="app-bar">
    <MobileIconButton v-if="leading === 'back'" icon="chevron-left" :icon-size="22" aria-label="Back" />
    <MobileIconButton v-else-if="leading === 'close'" icon="x" :icon-size="22" aria-label="Close" />
    <h1>{{ title }}</h1>
    <div class="app-bar-actions"><slot /></div>
  </header>
</template>
