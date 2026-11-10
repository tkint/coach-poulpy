<script setup lang="ts">
// One switch for every sample on the page: light or dark, remembered in its own cookie.
import { Moon, Sun } from '@lucide/vue'
import { useSampleTheme } from './useSampleTheme'

const { mode, toggle } = useSampleTheme()
</script>

<template>
  <div class="sample-switch">
    <button type="button" :aria-pressed="mode === 'dark'" @click="toggle">
      <Moon v-if="mode === 'dark'" :size="16" aria-hidden="true" />
      <Sun v-else :size="16" aria-hidden="true" />
      Samples in {{ mode }}
    </button>
  </div>
</template>

<style scoped>
.sample-switch { position: sticky; top: calc(var(--vp-nav-height) + 8px); z-index: 5; display: flex; justify-content: flex-end; margin: 8px 0; pointer-events: none; }
button {
  pointer-events: auto; display: inline-flex; align-items: center; gap: 6px; height: 32px; padding: 0 14px;
  border-radius: 999px; border: 1px solid var(--vp-c-divider); background: var(--vp-c-bg-soft);
  font-size: 14px; font-weight: 600; color: var(--vp-c-text-1); cursor: pointer;
}
button:hover { border-color: var(--vp-c-brand-1); color: var(--vp-c-brand-1); }
</style>
