<script setup lang="ts">
// Poulpy with a jetpack, zooming from side to side (design/poulpy-3d/looks/jetpack.mjs, ZOOM in motion.mjs), drawn by poulpy3dScene.ts. A draft look:
// he flies while he is on screen, and stands still with reduced motion. Drag to look around him; the controls loosen his tentacles and
// change the speed.
import { onBeforeUnmount, onMounted, shallowRef, useTemplateRef, watch } from 'vue'
import type { Zoom } from '../../../../../design/poulpy-3d/motion.mjs'
import type { createPoulpyScene } from '../poulpy3dScene'

const canvas = useTemplateRef<HTMLCanvasElement>('canvas')
const stage = useTemplateRef<HTMLDivElement>('stage')
const flying = shallowRef(false)
const outline = shallowRef(false)
const softness = shallowRef(1) // how loose his tentacles are: 1 is the design
const speed = shallowRef(1)
const turn = shallowRef(0) // degrees the viewer has walked around him; 0 is face on
const tilt = shallowRef(10) // degrees above him
const failed = shallowRef(false)

let scene: ReturnType<typeof createPoulpyScene> | undefined
let move: Zoom | undefined
let frame = 0
let last = 0
let onScreen = true
let watching: IntersectionObserver | undefined

const clamp = (value: number, low: number, high: number) => Math.min(high, Math.max(low, value))
const look = () => scene?.look({ turn: turn.value, tilt: tilt.value, distance: 13.5, centre: 1.85 })
watch([turn, tilt], look)
watch(outline, () => { scene?.show({ look: 'jetpack' }, outline.value); if (move) scene?.pose(move.pose()) })
watch(softness, (value) => { if (move) move.softness = value })

// Off screen he waits, so the page costs nothing while it is scrolled away.
function fly(now: number) {
  if (flying.value && onScreen && move) scene?.pose(move.advance(((now - last) / 1000) * speed.value))
  last = now
  frame = requestAnimationFrame(fly)
}

// One finger or the mouse looks around him, the arrow keys too.
let from: [number, number] | undefined
function press(event: PointerEvent) {
  canvas.value?.setPointerCapture(event.pointerId)
  from = [event.clientX, event.clientY]
}
function drag(event: PointerEvent) {
  if (!from) return
  turn.value = (turn.value - (event.clientX - from[0]) * 0.6) % 360
  tilt.value = clamp(tilt.value + (event.clientY - from[1]) * 0.45, -60, 80)
  from = [event.clientX, event.clientY]
}
function release() { from = undefined }
function nudge(event: KeyboardEvent) {
  const step = ({ ArrowLeft: [8, 0], ArrowRight: [-8, 0], ArrowUp: [0, 6], ArrowDown: [0, -6] } as Record<string, number[]>)[event.key]
  if (!step) return
  event.preventDefault()
  turn.value = (turn.value + step[0]) % 360
  tilt.value = clamp(tilt.value + step[1], -60, 80)
}

onMounted(async () => {
  // three.js is only fetched on this page, and only in the browser.
  try {
    const [{ createPoulpyScene }, { createZoom }, { JETPACK, NOZZLES }] = await Promise.all([
      import('../poulpy3dScene'), import('../../../../../design/poulpy-3d/motion.mjs'), import('../../../../../design/poulpy-3d/looks/jetpack.mjs'),
    ])
    if (!canvas.value) return
    move = createZoom({ softness: softness.value, nozzles: NOZZLES, flame: JETPACK.flames[0][1] })
    scene = createPoulpyScene(canvas.value)
  } catch {
    failed.value = true
    return
  }
  look()
  scene.show({ look: 'jetpack' }, outline.value)
  scene.pose(move.pose())
  flying.value = !window.matchMedia('(prefers-reduced-motion: reduce)').matches
  if (stage.value) {
    watching = new IntersectionObserver(([entry]) => { onScreen = entry.isIntersecting })
    watching.observe(stage.value)
  }
  last = performance.now()
  frame = requestAnimationFrame(fly)
})

onBeforeUnmount(() => {
  cancelAnimationFrame(frame)
  watching?.disconnect()
  scene?.dispose()
})
</script>

<template>
  <div class="frame"><div class="jetpack">
    <div ref="stage" class="stage">
      <canvas
        ref="canvas"
        tabindex="0"
        role="img"
        aria-label="Poulpy in 3D with a jetpack, zooming from side to side with a wide grin, flames and smoke under him and his tentacles trailing. Drag, or use the arrow keys, to look around him."
        @pointerdown="press"
        @pointermove="drag"
        @pointerup="release"
        @pointercancel="release"
        @keydown="nudge"
      />
      <p v-if="failed" class="failed">The 3D preview could not start in this browser.</p>
      <p v-else class="hint">Drag any way to look around him</p>
    </div>
    <div class="controls">
      <div class="toggles">
        <button type="button" class="toggle" :aria-pressed="flying" @click="flying = !flying">Fly</button>
        <button type="button" class="toggle" :aria-pressed="outline" @click="outline = !outline">Outline</button>
      </div>
      <label class="slider"><span>Tilt</span><input v-model.number="tilt" type="range" min="-60" max="80" step="1" /><output>{{ Math.round(tilt) }}°</output></label>
      <label class="slider"><span>Soft arms</span><input v-model.number="softness" type="range" min="0" max="3" step="0.05" /><output>{{ softness.toFixed(2) }}</output></label>
      <label class="slider"><span>Speed</span><input v-model.number="speed" type="range" min="0.1" max="2" step="0.05" @input="flying = true" /><output>{{ speed.toFixed(2) }}×</output></label>
    </div>
  </div></div>
</template>

<style scoped>
.frame { container-type: inline-size; }
.jetpack { display: flex; flex-direction: column; gap: 12px; margin: 16px 0; }
@container (min-width: 760px) {
  .jetpack { display: grid; grid-template-columns: minmax(0, 1fr) 300px; align-items: start; gap: 24px; }
  .controls { position: sticky; top: 16px; padding: 16px; border-radius: 24px; background: var(--cp-surface-variant); }
  .stage { position: sticky; top: 16px; }
}
.stage { position: relative; aspect-ratio: 4 / 3; max-height: 70vh; border-radius: 24px; background: var(--cp-surface-variant); overflow: hidden; }
.stage canvas { position: absolute; inset: 0; width: 100%; height: 100%; touch-action: none; cursor: grab; border-radius: 24px; }
.stage canvas:active { cursor: grabbing; }
.stage canvas:focus-visible { outline: 3px solid var(--cp-primary); outline-offset: -3px; }
.hint, .failed { position: absolute; right: 16px; bottom: 10px; margin: 0; font-size: 13px; color: var(--cp-on-surface-variant); pointer-events: none; }
.failed { inset: 0; display: grid; place-items: center; padding: 24px; text-align: center; }
.controls { display: flex; flex-wrap: wrap; gap: 12px; align-items: center; }
.toggles { display: inline-flex; gap: 8px; }
.toggle { height: 36px; padding: 0 16px; border-radius: 999px; border: 1px solid var(--cp-outline); font-size: 14px; font-weight: 700; color: var(--cp-on-surface); cursor: pointer; }
.toggle[aria-pressed='true'] { border-color: var(--cp-primary); color: var(--cp-on-primary-container); background: var(--cp-primary-container); }
.toggle:focus-visible { outline: 3px solid var(--cp-primary); outline-offset: 2px; }
.slider { flex: 1 1 100%; display: flex; align-items: center; gap: 10px; min-width: 0; font-size: 14px; font-weight: 700; }
.slider span { flex: none; width: 5.5em; }
.slider output { min-width: 3.6em; text-align: right; font-variant-numeric: tabular-nums; }
.slider input { flex: 1; min-width: 0; height: 36px; accent-color: var(--cp-primary); }
</style>
