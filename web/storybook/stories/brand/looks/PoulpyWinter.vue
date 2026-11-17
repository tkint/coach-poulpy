<script setup lang="ts">
// Winter Poulpy, live: his look from design/poulpy-3d/looks/winter.mjs, drawn by ../poulpy3dScene.ts, with the snow and his
// breath drawn next to him by winterEffects.ts. He sways and breathes on his own; the viewer can walk around him. He only
// moves while he is on screen, and stands still with reduced motion.
import { onBeforeUnmount, onMounted, shallowRef, useTemplateRef, watch } from 'vue'
import type { Winter } from '../../../../../design/poulpy-3d/looks/winter.mjs'
import type { createPoulpyScene } from '../poulpy3dScene'
import type { createWinterEffects } from './winterEffects'

const canvas = useTemplateRef<HTMLCanvasElement>('canvas')
const stage = useTemplateRef<HTMLDivElement>('stage')
const turn = shallowRef(0) // degrees the viewer has walked around him; 0 is face on
const tilt = shallowRef(14) // degrees above him
const distance = shallowRef(10.4)
const speed = shallowRef(1)
const playing = shallowRef(false)
const snow = shallowRef(true)
const breath = shallowRef(true)
const outline = shallowRef(false)
const failed = shallowRef(false)

let scene: ReturnType<typeof createPoulpyScene> | undefined
let effects: ReturnType<typeof createWinterEffects> | undefined
let move: Winter | undefined
let frame = 0
let last = 0
let onScreen = true
let watching: IntersectionObserver | undefined

const clamp = (value: number, low: number, high: number) => Math.min(high, Math.max(low, value))
const wrap = (degrees: number) => ((degrees % 360) + 360) % 360

function draw(seconds: number) {
  if (!scene || !move) return
  const pose = move.advance(seconds)
  effects?.update(pose, { snow: snow.value, breath: breath.value })
  scene.pose(pose)
}
function tick(now: number) {
  if (playing.value && onScreen) draw(((now - last) / 1000) * speed.value)
  last = now
  frame = requestAnimationFrame(tick)
}

// One finger or the mouse walks around him, two fingers or a pinch zoom, the arrow keys turn.
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
    tilt.value = clamp(tilt.value + (event.clientY - from[1]) * 0.45, -30, 89)
  } else if (fingers.size === 2) {
    const [a, b] = [...fingers.values()]
    const gap = Math.hypot(a[0] - b[0], a[1] - b[1])
    if (pinch) distance.value = clamp((distance.value * pinch) / gap, 5.5, 16)
    pinch = gap
  }
}
function release(event: PointerEvent) {
  fingers.delete(event.pointerId)
  pinch = 0
}
function zoom(event: WheelEvent) {
  if (!event.ctrlKey) return
  event.preventDefault()
  distance.value = clamp(distance.value * Math.exp(event.deltaY * 0.001), 5.5, 16)
}
function nudge(event: KeyboardEvent) {
  const step = ({ ArrowLeft: [-8, 0], ArrowRight: [8, 0], ArrowUp: [0, 6], ArrowDown: [0, -6] } as Record<string, number[]>)[event.key]
  if (!step) return
  event.preventDefault()
  turn.value = wrap(turn.value + step[0])
  tilt.value = clamp(tilt.value + step[1], -30, 89)
}

const look = () => scene?.look({ turn: -turn.value, tilt: tilt.value, distance: distance.value, centre: 1.45 })
let model: Parameters<ReturnType<typeof createPoulpyScene>['show']>[0] | undefined
const show = () => { if (model) scene?.show(model, outline.value) }
// The snow and his breath turn to face the viewer again, even while he stands still.
watch([turn, tilt, distance], () => { look(); draw(0) })
watch(outline, show)
// A switch flipped while he is still is seen at once.
watch([snow, breath], () => draw(0))

onMounted(async () => {
  // three.js is only fetched on this page, and only in the browser.
  try {
    const [{ createPoulpyScene }, { createWinterEffects }, { buildWinterPoulpy, createWinter, WINTER }, tokens] = await Promise.all([
      import('../poulpy3dScene'),
      import('./winterEffects'),
      import('../../../../../design/poulpy-3d/looks/winter.mjs'),
      import('../../../../../design/tokens.json'),
    ])
    if (!canvas.value) return
    scene = createPoulpyScene(canvas.value)
    model = { model: buildWinterPoulpy({ tones: tokens.default.mood, colours: tokens.default.color.poulpy }) }
    effects = createWinterEffects(scene.room, scene.camera, { flakes: WINTER.snow.flakes, puffs: WINTER.puffs.count * 3 })
    move = createWinter()
    // A few seconds in, so the snow is already falling and a breath is in the air.
    for (let i = 0; i < 59; i++) move.advance(0.1)
  } catch {
    failed.value = true
    return
  }
  look()
  show()
  draw(0)
  playing.value = !window.matchMedia('(prefers-reduced-motion: reduce)').matches
  if (stage.value) {
    watching = new IntersectionObserver(([entry]) => { onScreen = entry.isIntersecting })
    watching.observe(stage.value)
  }
  last = performance.now()
  frame = requestAnimationFrame(tick)
})

onBeforeUnmount(() => {
  cancelAnimationFrame(frame)
  watching?.disconnect()
  effects?.dispose()
  scene?.dispose()
})
</script>

<template>
  <div class="frame"><div class="poulpy-winter">
    <div ref="stage" class="stage">
      <canvas
        ref="canvas"
        tabindex="0"
        role="img"
        aria-label="Winter Poulpy in 3D: happy in fluffy ear muffs and a scarf, swaying gently while snow falls and his breath puffs out as mist. Drag, or use the arrow keys, to look around him."
        @pointerdown="press"
        @pointermove="drag"
        @pointerup="release"
        @pointercancel="release"
        @wheel="zoom"
        @keydown="nudge"
      />
      <p v-if="failed" class="failed">The 3D preview could not start in this browser.</p>
      <p v-else class="hint">Drag any way to look around him</p>
    </div>
    <div class="controls">
      <div class="toggles">
        <button type="button" class="toggle" :aria-pressed="playing" @click="playing = !playing">Move</button>
        <button type="button" class="toggle" :aria-pressed="snow" @click="snow = !snow">Snow</button>
        <button type="button" class="toggle" :aria-pressed="breath" @click="breath = !breath">Breath</button>
        <button type="button" class="toggle" :aria-pressed="outline" @click="outline = !outline">Outline</button>
      </div>
      <label class="slider"><span>Tilt</span><input v-model.number="tilt" type="range" min="-30" max="89" step="1" /><output>{{ Math.round(tilt) }}°</output></label>
      <label class="slider"><span>Speed</span><input v-model.number="speed" type="range" min="0.1" max="3" step="0.1" @input="playing = true" /><output>{{ speed.toFixed(1) }}×</output></label>
    </div>
  </div></div>
</template>

<style scoped>
.frame { container-type: inline-size; }
.poulpy-winter { display: flex; flex-direction: column; gap: 12px; margin: 16px 0; }
@container (min-width: 760px) {
  .poulpy-winter { display: grid; grid-template-columns: minmax(0, 1fr) 280px; align-items: start; gap: 24px; }
  .controls { position: sticky; top: 16px; padding: 16px; border-radius: 24px; background: var(--cp-surface-variant); }
}
.stage { position: relative; aspect-ratio: 4 / 3; max-height: 70vh; border-radius: 24px; background: var(--cp-surface-variant); overflow: hidden; }
.stage canvas { position: absolute; inset: 0; width: 100%; height: 100%; touch-action: none; cursor: grab; border-radius: 24px; }
.stage canvas:active { cursor: grabbing; }
.stage canvas:focus-visible { outline: 3px solid var(--cp-primary); outline-offset: -3px; }
.hint, .failed { position: absolute; right: 16px; bottom: 10px; margin: 0; font-size: 13px; color: var(--cp-on-surface-variant); pointer-events: none; }
.failed { inset: 0; display: grid; place-items: center; padding: 24px; text-align: center; }
.controls { display: flex; flex-wrap: wrap; gap: 12px; align-items: center; }
.toggles { display: flex; flex-wrap: wrap; gap: 8px; }
.toggle { height: 36px; padding: 0 16px; border-radius: 999px; border: 1px solid var(--cp-outline); background: none; font-size: 14px; font-weight: 700; color: var(--cp-on-surface); cursor: pointer; }
.toggle[aria-pressed='true'] { border-color: var(--cp-primary); color: var(--cp-on-primary-container); background: var(--cp-primary-container); }
.toggle:focus-visible { outline: 3px solid var(--cp-primary); outline-offset: 2px; }
.slider { flex: 1 1 100%; display: flex; align-items: center; gap: 10px; min-width: 0; font-size: 14px; font-weight: 700; }
.slider span { flex: none; width: 4em; }
.slider output { min-width: 3.6em; text-align: right; font-variant-numeric: tabular-nums; }
.slider input { flex: 1; min-width: 0; height: 36px; accent-color: var(--cp-primary); }
</style>
