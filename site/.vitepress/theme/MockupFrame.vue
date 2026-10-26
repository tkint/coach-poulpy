<script setup lang="ts">
import { ref } from 'vue'
import { withBase } from 'vitepress'
import type { Mockup } from '../mockups.mts'

const props = defineProps<{ mockup: Mockup }>()
// Changing the key remounts the iframe: the mockup starts over without reloading the docs page.
const run = ref(0)
const src = () => withBase('/' + props.mockup.path)
</script>

<template>
  <div class="mockup-frame">
    <div class="mockup-toolbar">
      <button type="button" @click="run++">
        <svg width="16" height="16" viewBox="0 0 24 24" aria-hidden="true" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 12a9 9 0 1 0 3-6.7"/><path d="M3 4v5h5"/></svg>
        Reset
      </button>
      <a :href="src()" target="_blank" rel="noopener">Open on its own</a>
      <a :href="`https://github.com/tkint/coach-poulpy/blob/main/${mockup.path}`">Source</a>
      <a v-if="mockup.issue" :href="`https://github.com/tkint/coach-poulpy/issues/${mockup.issue}`">#{{ mockup.issue }}</a>
    </div>
    <iframe :key="run" :src="src()" :title="mockup.title" :style="{ height: mockup.height + 48 + 'px' }"></iframe>
  </div>
</template>

<style scoped>
.mockup-frame { width: 438px; max-width: 100%; }
.mockup-toolbar { display: flex; flex-wrap: wrap; align-items: center; gap: 8px 16px; margin-bottom: 12px; font-size: 14px; }
.mockup-toolbar button {
  display: inline-flex; align-items: center; gap: 6px; height: 32px; padding: 0 14px;
  border-radius: 999px; border: 1px solid var(--vp-c-divider); background: var(--vp-c-bg-soft);
  font-weight: 600; color: var(--vp-c-text-1);
}
.mockup-toolbar button:hover { border-color: var(--vp-c-brand-1); color: var(--vp-c-brand-1); }
.mockup-toolbar a { color: var(--vp-c-brand-1); font-weight: 500; }
iframe { width: 438px; max-width: 100%; border: 0; border-radius: 24px; display: block; }
</style>
