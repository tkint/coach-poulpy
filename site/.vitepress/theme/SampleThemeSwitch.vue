<script setup lang="ts">
// One switch for every sample on the page: light or dark, remembered in its own cookie.
import { useSampleTheme } from './useSampleTheme'

const { mode, toggle } = useSampleTheme()
</script>

<template>
  <div class="sample-switch">
    <button type="button" :aria-pressed="mode === 'dark'" @click="toggle">
      <svg v-if="mode === 'dark'" width="16" height="16" viewBox="0 0 24 24" aria-hidden="true" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 14.5A8 8 0 1 1 9.5 4a6.5 6.5 0 0 0 10.5 10.5z" /></svg>
      <svg v-else width="16" height="16" viewBox="0 0 24 24" aria-hidden="true" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round"><circle cx="12" cy="12" r="4" /><path d="M12 2v2M12 20v2M4.9 4.9l1.4 1.4M17.7 17.7l1.4 1.4M2 12h2M20 12h2M4.9 19.1l1.4-1.4M17.7 6.3l1.4-1.4" /></svg>
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
