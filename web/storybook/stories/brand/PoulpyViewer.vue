<script lang="ts">
import type { createPoulpyScene, PoulpyView } from './poulpy3dScene'

export type PoulpyScene = ReturnType<typeof createPoulpyScene>

/** One page's Poulpy, running live in the viewer's scene: what `start` hands back once it has built him. */
export interface LivePoulpy {
  /** Draws him, with or without the outline, at the point his motion has reached. */
  show(outline: boolean): void
  /**
   * Moves him on by this many seconds of his motion (already scaled by the speed). Only called while Move is on and he is on
   * screen. A page where the viewer walks around him instead hands back how many degrees to walk.
   */
  advance(seconds: number): number | void
  /** The viewer has moved: what faces the viewer (leaves, snow) turns to face them again, even while he stands still. */
  viewed?(view: PoulpyView): void
  /** Move was turned off. */
  stopped?(): void
  /** Removes and disposes of what the page added to the scene, before the scene goes. */
  dispose?(): void
}

/** How the viewer first looks at him; the tilt is in degrees above him. */
export interface PoulpyFraming { tilt: number; distance: number; centre?: number }

/** How far the viewer tilts: from straight above him (90°) to straight below him (-90°). */
export const TILT = 90
</script>

<script setup lang="ts">
// Poulpy in 3D, live: the canvas and the side panel every 3D page of the workshop shares (Poulpy in 3D, his moves and his
// looks). The page builds him in the scene (`start`) and moves him; the viewer owns the camera, the Move and Outline switches,
// the Turn, Tilt and Speed sliders, Export values, dragging, the arrow keys, pinching to zoom, reduced motion and stopping off
// screen. A page's own choices go in the `pickers` slot, its own switches in `toggles` and its own sliders in `sliders`, with
// the `sides` and `choice`, `toggle` and `slider` classes, and anything else at the end of the panel; a short press on him is a `tap`.
import { onBeforeUnmount, onMounted, shallowRef, useTemplateRef, watch } from 'vue'

const { label, framing, start, moveLabel = 'Move', values, stopOnGrab = false, speeds = { max: 3, step: 0.1 }, bare = false } = defineProps<{
  /** What the canvas shows, for a screen reader; how to look around him is added. */
  label: string
  framing: PoulpyFraming
  /** Builds him in the scene, fetching what it needs, and hands back how to run him. */
  start: (scene: PoulpyScene) => Promise<LivePoulpy>
  moveLabel?: string
  /** The page's own values, for Export values; the view and the speed are added. */
  values?: () => Record<string, unknown>
  /** Grabbing the view (a drag, an arrow key, the Turn slider) stops Move: for a page where Move walks the viewer around him. */
  stopOnGrab?: boolean
  speeds?: { max: number; step: number }
  /** Just him on a clear background, without the panel or the hint: for a page that embeds him (the docs home). */
  bare?: boolean
}>()
const emit = defineEmits<{ tap: [] }>()

const canvas = useTemplateRef<HTMLCanvasElement>('canvas')
const stage = useTemplateRef<HTMLDivElement>('stage')
const playing = shallowRef(false)
const outline = shallowRef(false)
const turn = shallowRef(0) // degrees the viewer has walked around him; 0 is face on
const tilt = shallowRef(framing.tilt)
const distance = shallowRef(framing.distance)
const speed = shallowRef(1)
const failed = shallowRef(false)
const exported = shallowRef('')
const copied = shallowRef(false)

let scene: PoulpyScene | undefined
let live: LivePoulpy | undefined
let frame = 0
let last = 0
let onScreen = true
let watching: IntersectionObserver | undefined

const clamp = (value: number, low: number, high: number) => Math.min(high, Math.max(low, value))
const wrap = (degrees: number) => ((degrees % 360) + 360) % 360
const near = Math.min(5.5, framing.distance * 0.53), far = Math.max(16, framing.distance * 1.54)
const decimals = speeds.step < 0.1 ? 2 : 1

function look() {
  if (!scene || !live) return
  // The scene wants where the viewer stands: walked around by -turn.
  const view: PoulpyView = { turn: -turn.value, tilt: tilt.value, distance: distance.value, centre: framing.centre }
  scene.look(view)
  live.viewed?.(view)
}
function step(now: number) {
  if (playing.value && onScreen && live) {
    const walk = live.advance(((now - last) / 1000) * speed.value)
    if (walk) turn.value = wrap(turn.value + walk)
  }
  last = now
  frame = requestAnimationFrame(step)
}
function grab() {
  if (stopOnGrab) playing.value = false
}

// One finger or the mouse walks around him and tilts, two fingers or a pinch zoom, the arrow keys turn and tilt.
const fingers = new Map<number, [number, number]>()
let pinch = 0
let pressed: { x: number; y: number; at: number; moved: boolean } | undefined
function press(event: PointerEvent) {
  canvas.value?.setPointerCapture(event.pointerId)
  fingers.set(event.pointerId, [event.clientX, event.clientY])
  pressed = fingers.size === 1 ? { x: event.clientX, y: event.clientY, at: performance.now(), moved: false } : undefined
  grab()
}
function drag(event: PointerEvent) {
  const from = fingers.get(event.pointerId)
  if (!from) return
  fingers.set(event.pointerId, [event.clientX, event.clientY])
  if (pressed && Math.hypot(event.clientX - pressed.x, event.clientY - pressed.y) > 6) pressed.moved = true
  if (fingers.size === 1) {
    turn.value = wrap(turn.value + (event.clientX - from[0]) * 0.6)
    tilt.value = clamp(tilt.value + (event.clientY - from[1]) * 0.45, -TILT, TILT)
  } else if (fingers.size === 2) {
    const [a, b] = [...fingers.values()]
    const gap = Math.hypot(a[0] - b[0], a[1] - b[1])
    if (pinch) distance.value = clamp((distance.value * pinch) / gap, near, far)
    pinch = gap
  }
}
function release(event: PointerEvent) {
  fingers.delete(event.pointerId)
  pinch = 0
  // A short press that hardly moved is a tap on him.
  if (event.type === 'pointerup' && pressed && !pressed.moved && performance.now() - pressed.at < 500) emit('tap')
  pressed = undefined
}
// The plain wheel keeps scrolling the page; a trackpad pinch (or Ctrl and the wheel) zooms.
function zoom(event: WheelEvent) {
  if (!event.ctrlKey) return
  event.preventDefault()
  distance.value = clamp(distance.value * Math.exp(event.deltaY * 0.001), near, far)
}
function nudge(event: KeyboardEvent) {
  const by = ({ ArrowLeft: [-8, 0], ArrowRight: [8, 0], ArrowUp: [0, 6], ArrowDown: [0, -6] } as Record<string, number[]>)[event.key]
  if (!by) return
  event.preventDefault()
  grab()
  turn.value = wrap(turn.value + by[0])
  tilt.value = clamp(tilt.value + by[1], -TILT, TILT)
}

// The values the page is set to, as text to hand over: its own, then the view and the speed.
async function exportValues() {
  const round = (n: number) => Math.round(n * 1000) / 1000
  exported.value = JSON.stringify({ ...values?.(), view: { turn: round(turn.value), tilt: round(tilt.value), distance: round(distance.value) }, speed: round(speed.value) }, null, 2)
  try {
    await navigator.clipboard.writeText(exported.value)
    copied.value = true
    setTimeout(() => { copied.value = false }, 2000)
  } catch {
    copied.value = false // the text below is there to copy by hand
  }
}

// A page that starts something on a click (a variant to compare) turns Move back on, so it is seen.
defineExpose({ play: () => { playing.value = true } })

watch([turn, tilt, distance], look)
watch(outline, (on) => live?.show(on))
watch(playing, (on) => { if (!on) live?.stopped?.() })

onMounted(async () => {
  // three.js is only fetched on this page, and only in the browser.
  try {
    const { createPoulpyScene } = await import('./poulpy3dScene')
    if (!canvas.value) return
    scene = createPoulpyScene(canvas.value)
    live = await start(scene)
  } catch {
    failed.value = true
    return
  }
  look()
  live.show(outline.value)
  playing.value = !window.matchMedia('(prefers-reduced-motion: reduce)').matches
  // Off screen he waits, so several of him on a page cost no more than the one in view.
  if (stage.value) {
    watching = new IntersectionObserver(([entry]) => { onScreen = entry.isIntersecting })
    watching.observe(stage.value)
  }
  last = performance.now()
  frame = requestAnimationFrame(step)
})

onBeforeUnmount(() => {
  cancelAnimationFrame(frame)
  watching?.disconnect()
  live?.dispose?.()
  scene?.dispose()
})
</script>

<template>
  <div class="frame"><div class="poulpy-viewer" :class="{ bare }">
    <div ref="stage" class="stage">
      <canvas
        ref="canvas"
        tabindex="0"
        role="img"
        :aria-label="`${label} Drag, or use the arrow keys, to look around him.`"
        @pointerdown="press"
        @pointermove="drag"
        @pointerup="release"
        @pointercancel="release"
        @wheel="zoom"
        @keydown="nudge"
      />
      <p v-if="failed" class="failed">The 3D preview could not start in this browser.</p>
      <p v-else-if="!bare" class="hint">Drag to look around him</p>
    </div>
    <div v-if="!bare" class="controls">
      <slot name="pickers" />
      <div class="toggles">
        <button type="button" class="toggle" :aria-pressed="playing" @click="playing = !playing">{{ moveLabel }}</button>
        <slot name="toggles" />
        <button type="button" class="toggle" :aria-pressed="outline" @click="outline = !outline">Outline</button>
        <button type="button" class="toggle export" @click="exportValues">{{ copied ? 'Copied' : 'Export values' }}</button>
      </div>
      <label class="slider"><span>Turn</span><input v-model.number="turn" type="range" min="0" max="360" step="1" @input="grab" /><output>{{ Math.round(turn) }}°</output></label>
      <label class="slider"><span>Tilt</span><input v-model.number="tilt" type="range" :min="-TILT" :max="TILT" step="1" /><output>{{ Math.round(tilt) }}°</output></label>
      <slot name="sliders" />
      <label class="slider"><span>Speed</span><input v-model.number="speed" type="range" min="0.1" :max="speeds.max" :step="speeds.step" @input="playing = true" /><output>{{ speed.toFixed(decimals) }}×</output></label>
      <slot />
      <label v-if="exported" class="exported"><span>Values to hand over</span><textarea readonly rows="8" :value="exported" @focus="($event.target as HTMLTextAreaElement).select()" /></label>
    </div>
  </div></div>
</template>

<style scoped>
.frame { container-type: inline-size; }
.poulpy-viewer { display: flex; flex-direction: column; gap: 12px; margin: 16px 0; }
/* Wide enough, the controls are a panel beside him, and both stay in view as the page scrolls. */
@container (min-width: 760px) {
  .poulpy-viewer { display: grid; grid-template-columns: minmax(0, 1fr) 300px; align-items: start; gap: 24px; }
  .controls { position: sticky; top: 16px; max-height: calc(100vh - 32px); overflow-y: auto; padding: 16px; border-radius: 24px; background: var(--cp-surface-variant); }
  .controls :deep(.sides) { background: var(--cp-surface); }
  .stage { position: sticky; top: 16px; }
}
.stage { position: relative; aspect-ratio: 4 / 3; max-height: 70vh; border-radius: 24px; background: var(--cp-surface-variant); overflow: hidden; }
.poulpy-viewer.bare { margin: 0; display: block; }
.poulpy-viewer.bare .stage { position: relative; top: auto; max-height: none; background: none; }
.stage canvas { position: absolute; inset: 0; width: 100%; height: 100%; touch-action: none; cursor: grab; border-radius: 24px; }
.stage canvas:active { cursor: grabbing; }
.stage canvas:focus-visible { outline: 3px solid var(--cp-primary); outline-offset: -3px; }
.hint, .failed { position: absolute; right: 16px; bottom: 10px; margin: 0; font-size: 13px; color: var(--cp-on-surface-variant); pointer-events: none; }
.failed { inset: 0; display: grid; place-items: center; padding: 24px; text-align: center; }
.controls { display: flex; flex-wrap: wrap; gap: 12px; align-items: center; color: var(--cp-on-surface); }
.toggles { display: inline-flex; flex-wrap: wrap; gap: 8px; }
.export { white-space: nowrap; }
.exported { flex: 1 1 100%; display: flex; flex-direction: column; gap: 6px; font-size: 14px; font-weight: 700; }
.exported textarea { width: 100%; box-sizing: border-box; padding: 8px 10px; border-radius: 12px; border: 1px solid var(--cp-outline); background: var(--cp-surface); color: var(--cp-on-surface); font: 12px/1.4 ui-monospace, monospace; resize: vertical; }
/* A page's own choices, switches and sliders come in through the slots, so these reach into them. */
.controls :deep(.sides) { display: inline-flex; flex-wrap: wrap; gap: 4px; padding: 4px; border-radius: 22px; background: var(--cp-surface-variant); }
.controls :deep(.choice), .controls :deep(.toggle) { height: 36px; padding: 0 16px; border-radius: 999px; border: 1px solid transparent; background: none; font-size: 14px; font-weight: 700; color: var(--cp-on-surface); cursor: pointer; }
.controls :deep(.choice.icon) { display: inline-grid; place-items: center; width: 40px; padding: 0; }
.controls :deep(.choice[aria-pressed='true']) { background: var(--cp-primary); color: var(--cp-on-primary); }
.controls :deep(.toggle) { border-color: var(--cp-outline); }
.controls :deep(.toggle[aria-pressed='true']) { border-color: var(--cp-primary); color: var(--cp-on-primary-container); background: var(--cp-primary-container); }
.controls :deep(.choice:focus-visible), .controls :deep(.toggle:focus-visible) { outline: 3px solid var(--cp-primary); outline-offset: 2px; }
.controls :deep(.slider) { flex: 1 1 100%; display: flex; align-items: center; gap: 10px; min-width: 0; font-size: 14px; font-weight: 700; }
.controls :deep(.slider span) { flex: none; width: 5.5em; }
.controls :deep(.slider output) { min-width: 3.6em; text-align: right; font-variant-numeric: tabular-nums; }
.controls :deep(.slider input) { flex: 1; min-width: 0; height: 36px; accent-color: var(--cp-primary); }
/* On a phone the switches still share one row. */
@media (max-width: 480px) {
  .controls { column-gap: 4px; }
  .controls :deep(.sides) { gap: 2px; padding: 3px; }
  .controls :deep(.choice), .controls :deep(.toggle) { padding: 0 8px; font-size: 13px; }
  .controls :deep(.wide) { display: none; }
  .controls :deep(.choice.icon) { width: 32px; }
  .toggles { gap: 4px; }
}
</style>
