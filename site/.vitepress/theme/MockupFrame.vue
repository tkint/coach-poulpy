<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { withBase } from 'vitepress'
import type { Mockup } from '../mockups.mts'

const props = defineProps<{ mockup: Mockup }>()
// Changing the key remounts the iframe: the mockup starts over without reloading the docs page.
const run = ref(0)
const src = () => withBase('/' + props.mockup.path)

// The frame is drawn at its real size and scaled down to fit the column, so a phone screen never
// scrolls inside its frame, even on a narrow screen.
const width = 438
const height = () => props.mockup.height + 48
const box = ref<HTMLElement>()
const scale = ref(1)
let observer: ResizeObserver | undefined
onMounted(() => {
  observer = new ResizeObserver(([entry]) => (scale.value = Math.min(1, entry.contentRect.width / width)))
  observer.observe(box.value!)
})
onBeforeUnmount(() => observer?.disconnect())
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
    <div ref="box" class="mockup-box" :style="{ height: height() * scale + 'px' }">
      <iframe
        :key="run"
        :src="src()"
        :title="mockup.title"
        scrolling="no"
        :style="{ width: width + 'px', height: height() + 'px', transform: `scale(${scale})` }"
      ></iframe>
    </div>
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
.mockup-box { width: 100%; overflow: hidden; }
iframe { border: 0; display: block; transform-origin: top left; }
</style>
