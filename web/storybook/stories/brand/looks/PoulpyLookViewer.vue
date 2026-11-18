<script lang="ts">
import type { createPoulpyScene, PoulpyView } from '../poulpy3dScene'

export type PoulpyScene = ReturnType<typeof createPoulpyScene>

/** One look, running live in the viewer's scene: what `start` hands back once it has built him. */
export interface LiveLook {
  /** Draws him in the look, with or without the outline, at the point his motion has reached. */
  show(outline: boolean): void
  /** Moves him on by this many seconds of his motion (already scaled by the speed). Only called while Move is on and he is on screen. */
  advance(seconds: number): void
  /** The viewer has moved: what faces the viewer (leaves, snow) turns to face them again, even while he stands still. */
  viewed?(view: PoulpyView): void
  /** Move was turned off. */
  stopped?(): void
  /** Removes and disposes of what the look added to the scene, before the scene goes. */
  dispose?(): void
}

/** How the viewer first looks at him; the tilt is in degrees above him. */
export interface LookFraming { tilt: number; distance: number; centre?: number }

/** How far the viewer tilts: from straight above him (90°) to straight below him (-90°). */
export const TILT = 90
</script>

<script setup lang="ts">
// A Poulpy look, live (web/storybook/stories/brand/looks/): the canvas and the side controls every look page shares. The look
// builds him in the scene (`start`) and moves him; the viewer owns the camera, the Move and Outline switches, the Tilt and Speed
// sliders, dragging, the arrow keys, pinching to zoom, reduced motion and stopping off screen. A look's own switches go in the
// `toggles` slot, its own sliders in `sliders`, with the `toggle` and `slider` classes.
import { onBeforeUnmount, onMounted, shallowRef, useTemplateRef, watch } from 'vue'

const { label, framing, start, moveLabel = 'Move' } = defineProps<{
  /** What the canvas shows, for a screen reader; how to look around him is added. */
  label: string
  framing: LookFraming
  /** Builds the look in the scene, fetching what it needs, and hands back how to run it. */
  start: (scene: PoulpyScene) => Promise<LiveLook>
  moveLabel?: string
}>()

const canvas = useTemplateRef<HTMLCanvasElement>('canvas')
const stage = useTemplateRef<HTMLDivElement>('stage')
const playing = shallowRef(false)
const outline = shallowRef(false)
const turn = shallowRef(0) // degrees the viewer has walked around him; 0 is face on
const tilt = shallowRef(framing.tilt)
const distance = shallowRef(framing.distance)
const speed = shallowRef(1)
const failed = shallowRef(false)

let scene: PoulpyScene | undefined
let live: LiveLook | undefined
let frame = 0
let last = 0
let onScreen = true
let watching: IntersectionObserver | undefined

const clamp = (value: number, low: number, high: number) => Math.min(high, Math.max(low, value))
const wrap = (degrees: number) => ((degrees % 360) + 360) % 360
const near = framing.distance * 0.53, far = framing.distance * 1.54

function look() {
  if (!scene || !live) return
  // The scene wants where the viewer stands: walked around by -turn.
  const view: PoulpyView = { turn: -turn.value, tilt: tilt.value, distance: distance.value, centre: framing.centre }
  scene.look(view)
  live.viewed?.(view)
}
function step(now: number) {
  if (playing.value && onScreen) live?.advance(((now - last) / 1000) * speed.value)
  last = now
  frame = requestAnimationFrame(step)
}

// One finger or the mouse walks around him and tilts, two fingers or a pinch zoom, the arrow keys turn and tilt.
const fingers = new Map<number, [number, number]>()
let pinch = 0
function press(event: PointerEvent) {
  canvas.value?.setPointerCapture(event.pointerId)
  fingers.set(event.pointerId, [event.clientX, event.clientY])
}
function drag(event: PointerEvent) {
  const from = fingers.get(event.pointerId)
  if (!from) return
  fingers.set(event.pointerId, [event.clientX, event.clientY])
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
  turn.value = wrap(turn.value + by[0])
  tilt.value = clamp(tilt.value + by[1], -TILT, TILT)
}

watch([turn, tilt, distance], look)
watch(outline, (on) => live?.show(on))
watch(playing, (on) => { if (!on) live?.stopped?.() })

onMounted(async () => {
  // three.js is only fetched on this page, and only in the browser.
  try {
    const { createPoulpyScene } = await import('../poulpy3dScene')
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
  // Off screen he waits, so the page costs nothing while it is scrolled away.
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
  <div class="frame"><div class="poulpy-look">
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
      <p v-else class="hint">Drag to look around him</p>
    </div>
    <div class="controls">
      <div class="toggles">
        <button type="button" class="toggle" :aria-pressed="playing" @click="playing = !playing">{{ moveLabel }}</button>
        <slot name="toggles" />
        <button type="button" class="toggle" :aria-pressed="outline" @click="outline = !outline">Outline</button>
      </div>
      <label class="slider"><span>Tilt</span><input v-model.number="tilt" type="range" :min="-TILT" :max="TILT" step="1" /><output>{{ Math.round(tilt) }}°</output></label>
      <slot name="sliders" />
      <label class="slider"><span>Speed</span><input v-model.number="speed" type="range" min="0.1" max="3" step="0.1" @input="playing = true" /><output>{{ speed.toFixed(1) }}×</output></label>
    </div>
  </div></div>
</template>

<style scoped>
.frame { container-type: inline-size; }
.poulpy-look { display: flex; flex-direction: column; gap: 12px; margin: 16px 0; }
@container (min-width: 760px) {
  .poulpy-look { display: grid; grid-template-columns: minmax(0, 1fr) 280px; align-items: start; gap: 24px; }
  .controls { padding: 16px; border-radius: 24px; background: var(--cp-surface-variant); }
}
.stage { position: relative; aspect-ratio: 4 / 3; max-height: 70vh; border-radius: 24px; background: var(--cp-surface-variant); overflow: hidden; }
.stage canvas { position: absolute; inset: 0; width: 100%; height: 100%; touch-action: none; cursor: grab; border-radius: 24px; }
.stage canvas:active { cursor: grabbing; }
.stage canvas:focus-visible { outline: 3px solid var(--cp-primary); outline-offset: -3px; }
.hint, .failed { position: absolute; right: 16px; bottom: 10px; margin: 0; font-size: 13px; color: var(--cp-on-surface-variant); pointer-events: none; }
.failed { inset: 0; display: grid; place-items: center; padding: 24px; text-align: center; }
.controls { display: flex; flex-wrap: wrap; gap: 12px; align-items: center; }
.toggles { display: inline-flex; flex-wrap: wrap; gap: 8px; }
/* A look's own switches and sliders come in through the slots, so these reach into them. */
.controls :deep(.toggle) { height: 36px; padding: 0 16px; border-radius: 999px; border: 1px solid var(--cp-outline); background: none; font-size: 14px; font-weight: 700; color: var(--cp-on-surface); cursor: pointer; }
.controls :deep(.toggle[aria-pressed='true']) { border-color: var(--cp-primary); color: var(--cp-on-primary-container); background: var(--cp-primary-container); }
.controls :deep(.toggle:focus-visible) { outline: 3px solid var(--cp-primary); outline-offset: 2px; }
.controls :deep(.slider) { flex: 1 1 100%; display: flex; align-items: center; gap: 10px; min-width: 0; font-size: 14px; font-weight: 700; color: var(--cp-on-surface); }
.controls :deep(.slider span) { flex: none; width: 5.5em; }
.controls :deep(.slider output) { min-width: 3.6em; text-align: right; font-variant-numeric: tabular-nums; }
.controls :deep(.slider input) { flex: 1; min-width: 0; height: 36px; accent-color: var(--cp-primary); }
</style>
