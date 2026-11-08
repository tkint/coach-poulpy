<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { withBase } from 'vitepress'
import type { Mockup } from '../mockups.mts'
import { useMockupWidth } from './useMockupWidth'

const props = defineProps<{ mockup: Mockup }>()
// The mockups' own theme, apart from the docs site's: shared/mockup-theme.js in every mockup reads
// the same key, so one switch changes them all. With no choice, they follow the system setting.
const themeKey = 'coach-poulpy-mockup-theme'
const isDark = ref(false)
function readTheme() {
  let choice: string | null = null
  try {
    choice = localStorage.getItem(themeKey)
  } catch {}
  isDark.value = choice ? choice === 'dark' : matchMedia('(prefers-color-scheme: dark)').matches
}
function toggleTheme() {
  isDark.value = !isDark.value
  try {
    localStorage.setItem(themeKey, isDark.value ? 'dark' : 'light')
  } catch {}
}
const onStorage = (event: StorageEvent) => {
  if (event.key === themeKey || event.key === null) readTheme()
}
// Changing the key remounts the iframe: the mockup starts over without reloading the docs page.
const run = ref(0)
const src = () => withBase('/' + props.mockup.path)

// The frame is drawn at its real size and scaled down to fit the column, so a screen never
// scrolls inside its frame, even on a narrow screen.
const isWeb = computed(() => props.mockup.device === 'web')
// A web page is drawn at a browser width chosen in the toolbar or by dragging the frame's edge,
// shared by every frame, and is as tall as its content at that width. Full is the column's width.
const sizes = [
  { name: 'Phone', width: 390 },
  { name: 'Medium', width: 768 },
  { name: 'Large', width: 1280 },
]
const minWidth = 320
const maxWidth = 1600
const { width: chosen, set: choose } = useMockupWidth()
const box = ref<HTMLElement>()
const boxWidth = ref(438)
// Full leaves room for the resize handle.
const viewport = computed(() => (chosen.value === 'full' ? Math.max(minWidth, Math.floor(boxWidth.value - 16)) : chosen.value))
const pageHeight = ref(800)
const width = computed(() => (isWeb.value ? viewport.value : 438))
// A page that fills the window (`mockup:viewport`) gets a window of that height and scrolls inside it, since
// sizing the frame to a page that is as tall as the frame would make it grow forever.
const height = computed(() => (isWeb.value ? props.mockup.viewport || pageHeight.value : props.mockup.height + 48))
const scale = computed(() => Math.min(1, boxWidth.value / width.value))

const frame = ref<HTMLIFrameElement>()
let pageObserver: ResizeObserver | undefined
// The bottom of what the page draws, not the document's height, which is at least the frame's
// own height with a full-screen body and so would never shrink.
function measure() {
  const body = frame.value?.contentDocument?.body
  if (!body) return
  const bottom = Math.max(0, ...[...body.children].map((child) => child.getBoundingClientRect().bottom))
  pageHeight.value = Math.max(480, Math.ceil(bottom) + 24)
}
function onLoad() {
  if (!isWeb.value || props.mockup.viewport) return
  pageObserver?.disconnect()
  const body = frame.value?.contentDocument?.body
  if (!body) return
  measure()
  pageObserver = new ResizeObserver(measure)
  // The body follows the frame's width even when a centred column inside it keeps its size.
  for (const element of [body, ...body.children]) pageObserver.observe(element)
}

let start: { x: number; width: number } | undefined
function startResize(event: PointerEvent) {
  start = { x: event.clientX, width: viewport.value }
  ;(event.currentTarget as HTMLElement).setPointerCapture(event.pointerId)
}
function resize(event: PointerEvent) {
  if (!start) return
  const next = start.width + (event.clientX - start.x) / scale.value
  choose(Math.round(Math.min(maxWidth, Math.max(minWidth, next))))
}
function stopResize() {
  start = undefined
}
function resizeByKey(event: KeyboardEvent) {
  const step = event.shiftKey ? 100 : 10
  if (event.key === 'ArrowLeft') choose(Math.max(minWidth, viewport.value - step))
  else if (event.key === 'ArrowRight') choose(Math.min(maxWidth, viewport.value + step))
  else return
  event.preventDefault()
}

let observer: ResizeObserver | undefined
onMounted(() => {
  readTheme()
  addEventListener('storage', onStorage)
  observer = new ResizeObserver(([entry]) => (boxWidth.value = entry.contentRect.width))
  observer.observe(box.value!)
  // The server-rendered frame may have loaded before this page came alive and missed its load event.
  if (frame.value?.contentDocument?.readyState === 'complete') onLoad()
})
onBeforeUnmount(() => {
  observer?.disconnect()
  pageObserver?.disconnect()
  removeEventListener('storage', onStorage)
})
</script>

<template>
  <div class="mockup-frame" :class="{ web: isWeb }">
    <div class="mockup-toolbar">
      <div v-if="isWeb" class="mockup-sizes" role="group" aria-label="Browser width">
        <button v-for="size in sizes" :key="size.name" type="button" :aria-pressed="chosen === size.width" @click="choose(size.width)">
          {{ size.name }}
        </button>
        <button type="button" :aria-pressed="chosen === 'full'" @click="choose('full')">Full</button>
        <span class="mockup-width">{{ viewport }} px</span>
      </div>
      <button type="button" @click="run++">
        <svg width="16" height="16" viewBox="0 0 24 24" aria-hidden="true" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 12a9 9 0 1 0 3-6.7"/><path d="M3 4v5h5"/></svg>
        Reset
      </button>
      <button type="button" :aria-pressed="isDark" @click="toggleTheme">
        <svg v-if="isDark" width="16" height="16" viewBox="0 0 24 24" aria-hidden="true" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 14.5A8 8 0 1 1 9.5 4a6.5 6.5 0 0 0 10.5 10.5z"/></svg>
        <svg v-else width="16" height="16" viewBox="0 0 24 24" aria-hidden="true" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round"><circle cx="12" cy="12" r="4"/><path d="M12 2v2M12 20v2M4.9 4.9l1.4 1.4M17.7 17.7l1.4 1.4M2 12h2M20 12h2M4.9 19.1l1.4-1.4M17.7 6.3l1.4-1.4"/></svg>
        {{ isDark ? 'Dark' : 'Light' }}
      </button>
      <a :href="src()" target="_blank" rel="noopener">Open on its own</a>
      <a :href="`https://github.com/tkint/coach-poulpy/blob/main/${mockup.path}`">Source</a>
      <a v-if="mockup.issue" :href="`https://github.com/tkint/coach-poulpy/issues/${mockup.issue}`">#{{ mockup.issue }}</a>
    </div>
    <div ref="box" class="mockup-box" :style="{ height: height * scale + 'px' }">
      <iframe
        ref="frame"
        :key="run"
        :src="src()"
        :title="mockup.title"
        scrolling="no"
        :style="{ width: width + 'px', height: height + 'px', transform: `scale(${scale})`, colorScheme: isDark ? 'dark' : 'light' }"
        @load="onLoad"
      ></iframe>
      <div
        v-if="isWeb"
        class="mockup-handle"
        :style="{ left: width * scale + 'px' }"
        role="slider"
        tabindex="0"
        aria-label="Browser width"
        :aria-valuemin="minWidth"
        :aria-valuemax="maxWidth"
        :aria-valuenow="viewport"
        @pointerdown="startResize"
        @pointermove="resize"
        @pointerup="stopResize"
        @pointercancel="stopResize"
        @keydown="resizeByKey"
      ></div>
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
.mockup-toolbar button[aria-pressed='true'] { border-color: var(--vp-c-brand-1); color: var(--vp-c-brand-1); background: var(--vp-c-brand-soft); }
.mockup-box { width: 100%; overflow: hidden; position: relative; }
iframe { border: 0; display: block; transform-origin: top left; }
/* A web page takes the whole column, framed like a browser window, with a handle on its right edge. */
.mockup-frame.web { width: 100%; }
.mockup-frame.web .mockup-box { overflow: visible; padding-right: 16px; }
.mockup-frame.web iframe { outline: 1px solid var(--vp-c-divider); border-radius: 0; }
.mockup-sizes { display: inline-flex; flex-wrap: wrap; align-items: center; gap: 8px; }
.mockup-width { color: var(--vp-c-text-2); font-variant-numeric: tabular-nums; min-width: 64px; }
.mockup-handle {
  position: absolute; top: 0; bottom: 0; width: 14px; cursor: ew-resize; touch-action: none;
  display: flex; align-items: center; justify-content: center;
}
.mockup-handle::after { content: ''; width: 4px; height: 48px; border-radius: 2px; background: var(--vp-c-divider); }
.mockup-handle:hover::after, .mockup-handle:focus-visible::after { background: var(--vp-c-brand-1); }
</style>
