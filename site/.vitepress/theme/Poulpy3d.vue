<script setup lang="ts">
// Poulpy in 3D, to turn around: the shape of design/poulpy-3d/model.mjs, drawn by poulpy3dScene.ts.
// With `motion="turn"` he does his idle move himself (design/poulpy-3d/motion.mjs) and the viewer stays put.
import { onBeforeUnmount, onMounted, shallowRef, useTemplateRef, watch } from 'vue'
import type { PoulpySide } from '../../../design/poulpy-3d/model.mjs'
import type { Turn } from '../../../design/poulpy-3d/motion.mjs'
import type { createPoulpyScene } from './poulpy3dScene'

const { motion } = defineProps<{ motion?: 'turn' }>()

const canvas = useTemplateRef<HTMLCanvasElement>('canvas')
const side = shallowRef<PoulpySide>('blue')
const outline = shallowRef(false)
const turn = shallowRef(0) // degrees he has turned for the viewer, anticlockwise seen from above; 0 is face on
const tilt = shallowRef(motion ? 26 : 17) // degrees above him; negative looks from below
const distance = shallowRef(9.6)
const speed = shallowRef(1)
const spinning = shallowRef(false) // the viewer walks around him, or with `motion` he moves
const softness = shallowRef(1) // how loose his arms are: 1 is the design
// Seen from above, and named for him: without `motion` it is the viewer who walks around, the other way.
const direction = shallowRef<'clockwise' | 'anticlockwise'>('anticlockwise')
const step = shallowRef(24) // degrees he turns at each step, on average: 24 is the design
const failed = shallowRef(false)

let scene: ReturnType<typeof createPoulpyScene> | undefined
let frame = 0
let last = 0
let move: Turn | undefined

const sides: { id: PoulpySide; label: string }[] = [
  { id: 'blue', label: 'Blue side' },
  { id: 'ember', label: 'Grumpy side' },
]
const directions = [
  { id: 'anticlockwise', label: 'Anticlockwise' },
  { id: 'clockwise', label: 'Clockwise' },
] as const
const clamp = (value: number, low: number, high: number) => Math.min(high, Math.max(low, value))
const wrap = (degrees: number) => ((degrees % 360) + 360) % 360

// A full turn of the viewer takes about 13 s at speed 1. With `motion`, Poulpy moves instead.
function spin(now: number) {
  if (spinning.value && move) scene?.pose(move.advance(((now - last) / 1000) * speed.value))
  else if (spinning.value) turn.value = wrap(turn.value + ((now - last) / 1000) * 28 * speed.value * (direction.value === 'anticlockwise' ? 1 : -1))
  last = now
  frame = requestAnimationFrame(spin)
}

// One finger or the mouse turns him any way, two fingers or the wheel zoom, the arrow keys turn.
const fingers = new Map<number, [number, number]>()
let pinch = 0
function press(event: PointerEvent) {
  canvas.value?.setPointerCapture(event.pointerId)
  fingers.set(event.pointerId, [event.clientX, event.clientY])
  if (!motion) spinning.value = false
}
function drag(event: PointerEvent) {
  const from = fingers.get(event.pointerId)
  if (!from) return
  fingers.set(event.pointerId, [event.clientX, event.clientY])
  if (fingers.size === 1) {
    turn.value = wrap(turn.value + (event.clientX - from[0]) * 0.6)
    tilt.value = clamp(tilt.value + (event.clientY - from[1]) * 0.45, -89, 89)
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
// The plain wheel keeps scrolling the page; a trackpad pinch (or Ctrl and the wheel) zooms.
function zoom(event: WheelEvent) {
  if (!event.ctrlKey) return
  event.preventDefault()
  distance.value = clamp(distance.value * Math.exp(event.deltaY * 0.001), 5.5, 16)
}
function nudge(event: KeyboardEvent) {
  const step = ({ ArrowLeft: [-8, 0], ArrowRight: [8, 0], ArrowUp: [0, 6], ArrowDown: [0, -6] } as Record<string, number[]>)[event.key]
  if (!step) return
  event.preventDefault()
  if (!motion) spinning.value = false
  turn.value = wrap(turn.value + step[0])
  tilt.value = clamp(tilt.value + step[1], -89, 89)
}

const show = () => scene?.show(side.value, outline.value)
// The scene wants where the viewer stands: the other way around from how far he has turned.
const look = () => scene?.look({ turn: -turn.value, tilt: tilt.value, distance: distance.value })
watch([side, outline], show)
watch([turn, tilt, distance], look)
watch(softness, (value) => { if (move) move.softness = value })
watch([step, direction], ([degrees, way]) => { if (move) move.stride = (degrees / 24) * (way === 'anticlockwise' ? 1 : -1) })

onMounted(async () => {
  // three.js is only fetched on this page, and only in the browser.
  try {
    const { createPoulpyScene } = await import('./poulpy3dScene')
    if (motion) move = (await import('../../../design/poulpy-3d/motion.mjs')).createTurn({ softness: softness.value })
    if (!canvas.value) return
    scene = createPoulpyScene(canvas.value)
  } catch {
    failed.value = true
    return
  }
  look()
  show()
  spinning.value = !window.matchMedia('(prefers-reduced-motion: reduce)').matches
  last = performance.now()
  frame = requestAnimationFrame(spin)
})

onBeforeUnmount(() => {
  cancelAnimationFrame(frame)
  scene?.dispose()
})
</script>

<template>
  <div class="poulpy-3d">
    <div class="stage">
      <canvas
        ref="canvas"
        tabindex="0"
        role="img"
        :aria-label="motion ? 'Poulpy in 3D, turning around in short steps. Drag, or use the arrow keys, to look around him.' : 'Poulpy in 3D. Drag, or use the arrow keys, to turn him.'"
        @pointerdown="press"
        @pointermove="drag"
        @pointerup="release"
        @pointercancel="release"
        @wheel="zoom"
        @keydown="nudge"
      />
      <p v-if="failed" class="failed">The 3D preview could not start in this browser.</p>
      <p v-else class="hint">Drag any way to turn, pinch to zoom</p>
    </div>
    <div class="controls">
      <div class="sides" role="group" aria-label="Side">
        <button v-for="s in sides" :key="s.id" type="button" class="choice" :aria-pressed="side === s.id" @click="side = s.id">{{ s.label }}</button>
      </div>
      <div class="sides" role="group" aria-label="Direction, seen from above">
        <button v-for="d in directions" :key="d.id" type="button" class="choice" :aria-pressed="direction === d.id" @click="direction = d.id">{{ d.label }}</button>
      </div>
      <div class="toggles">
        <button type="button" class="toggle" :aria-pressed="outline" @click="outline = !outline">Outline</button>
        <button type="button" class="toggle" :aria-pressed="spinning" @click="spinning = !spinning">{{ motion ? 'Turning' : 'Spin' }}</button>
      </div>
      <label v-if="!motion" class="slider">Turn <input v-model.number="turn" type="range" min="0" max="360" step="1" @input="spinning = false" /><output>{{ Math.round(turn) }}°</output></label>
      <label class="slider">Tilt <input v-model.number="tilt" type="range" min="-89" max="89" step="1" /><output>{{ Math.round(tilt) }}°</output></label>
      <label v-if="motion" class="slider">Step <input v-model.number="step" type="range" min="1" max="180" step="1" /><output>{{ step }}°</output></label>
      <label v-if="motion" class="slider">Soft arms <input v-model.number="softness" type="range" min="0" max="4" step="0.05" /><output>{{ softness.toFixed(2) }}</output></label>
      <label class="slider">Speed <input v-model.number="speed" type="range" min="0.1" max="5" step="0.1" @input="spinning = true" /><output>{{ speed.toFixed(1) }}×</output></label>
    </div>
  </div>
</template>

<style scoped>
.poulpy-3d { display: flex; flex-direction: column; gap: 12px; margin: 16px 0; }
.stage { position: relative; aspect-ratio: 4 / 3; max-height: 70vh; border-radius: 24px; background: var(--vp-c-bg-soft); overflow: hidden; }
.stage canvas { position: absolute; inset: 0; width: 100%; height: 100%; touch-action: none; cursor: grab; border-radius: 24px; }
.stage canvas:active { cursor: grabbing; }
.stage canvas:focus-visible { outline: 3px solid var(--vp-c-brand-1); outline-offset: -3px; }
.hint, .failed { position: absolute; right: 16px; bottom: 10px; margin: 0; font-size: 13px; color: var(--vp-c-text-2); pointer-events: none; }
.failed { inset: 0; display: grid; place-items: center; padding: 24px; text-align: center; }
.controls { display: flex; flex-wrap: wrap; gap: 12px; align-items: center; }
.sides { display: inline-flex; gap: 4px; padding: 4px; border-radius: 999px; background: var(--vp-c-bg-soft); }
.toggles { display: inline-flex; gap: 8px; }
.choice, .toggle { height: 36px; padding: 0 16px; border-radius: 999px; border: 1px solid transparent; font-size: 14px; font-weight: 700; color: var(--vp-c-text-1); cursor: pointer; }
.choice[aria-pressed='true'] { background: var(--vp-c-brand-1); color: var(--vp-c-bg); }
.toggle { border-color: var(--vp-c-divider); }
.toggle[aria-pressed='true'] { border-color: var(--vp-c-brand-1); color: var(--vp-c-brand-1); background: var(--vp-c-brand-soft); }
.choice:focus-visible, .toggle:focus-visible { outline: 3px solid var(--vp-c-brand-1); outline-offset: 2px; }
.slider { flex: 1 1 180px; display: flex; align-items: center; gap: 10px; min-width: 0; font-size: 14px; font-weight: 700; }
.slider output { min-width: 3em; text-align: right; font-variant-numeric: tabular-nums; }
.slider input { flex: 1; min-width: 0; height: 36px; accent-color: var(--vp-c-brand-1); }
</style>
