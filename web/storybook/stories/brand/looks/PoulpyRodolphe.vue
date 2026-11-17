<script setup lang="ts">
// Rodolphe, Poulpy's red-nosed reindeer look (design/poulpy-3d/looks/rodolphe.mjs), drawn by poulpy3dScene.ts: he sways to a
// tune, his nose glowing on the beat, and the viewer can walk around him. He only moves while he is on screen.
import { onBeforeUnmount, onMounted, shallowRef, useTemplateRef, watch } from 'vue'
import type * as Three from 'three'
import tokens from '../../../../../design/tokens.json'
import { buildRodolphe, COLOURS, createRodolpheIdle, noseBulb, scarfKnot, type RodolpheIdle } from '../../../../../design/poulpy-3d/looks/rodolphe.mjs'
import type { createPoulpyScene } from '../poulpy3dScene'

const canvas = useTemplateRef<HTMLCanvasElement>('canvas')
const stage = useTemplateRef<HTMLDivElement>('stage')
const outline = shallowRef(false)
const dancing = shallowRef(false)
const turn = shallowRef(0) // degrees the viewer has walked around him; 0 is face on
const tilt = shallowRef(18) // degrees above him
const distance = shallowRef(9.6)
const speed = shallowRef(1)
const failed = shallowRef(false)

let scene: ReturnType<typeof createPoulpyScene> | undefined
let move: RodolpheIdle | undefined
let three: typeof Three | undefined
let frame = 0
let last = 0
let onScreen = true
let watching: IntersectionObserver | undefined
// What the idle moves on its own: the glow in front of his nose, his nose, and the scarf's ends around their knot.
let glow: Three.Sprite | undefined
let nose: Three.Mesh[] = []
let bulb: { material: Three.MeshToonMaterial; colour: Three.Color; lit: Three.Color } | undefined
let ends: Three.Mesh[] = []

const clamp = (value: number, low: number, high: number) => Math.min(high, Math.max(low, value))
const wrap = (degrees: number) => ((degrees % 360) + 360) % 360

// A soft round glow, warm in the middle and gone at its edge: the light of his nose on the air in front of his face.
function glowTexture() {
  const size = 128, paint = document.createElement('canvas')
  paint.width = paint.height = size
  const ink = paint.getContext('2d')!, fade = ink.createRadialGradient(size / 2, size / 2, 0, size / 2, size / 2, size / 2)
  const [r, g, b] = [1, 3, 5].map((i) => parseInt(COLOURS.glow.slice(i, i + 2), 16))
  fade.addColorStop(0, `rgba(${r},${g},${b},0.8)`)
  fade.addColorStop(0.3, `rgba(${r},${g},${b},0.35)`)
  fade.addColorStop(0.65, `rgba(${r},${g},${b},0.08)`)
  fade.addColorStop(1, `rgba(${r},${g},${b},0)`)
  ink.fillStyle = fade
  ink.fillRect(0, 0, size, size)
  return new three!.CanvasTexture(paint)
}

function show() {
  if (!scene || !three) return
  glow?.material.map?.dispose()
  glow?.material.dispose()
  scene.show({ model: buildRodolphe({ colours: tokens.color.poulpy, tone: tokens.mood.happy }) }, outline.value)
  // His nose swells a little as it glows, around its own middle, and lights up towards a paler red.
  const middle = new three.Vector3().fromArray(noseBulb.at)
  nose = scene.parts('nose')
  for (const part of nose) { part.geometry.translate(-middle.x, -middle.y, -middle.z); part.position.copy(middle) }
  const material = nose.find((part) => part.name === 'nose')?.material as Three.MeshToonMaterial | undefined
  bulb = material && { material, colour: material.emissive.clone(), lit: new three.Color(COLOURS.lit) }
  // The ends swing around the upright line through the knot: each is moved so the knot is where it turns.
  const knot = new three.Vector3().fromArray(scarfKnot)
  ends = scene.parts('scarf_end')
  for (const end of ends) { end.geometry.translate(-knot.x, -knot.y, -knot.z); end.position.copy(knot) }
  // The glow rides with his head, a little in front of his nose.
  glow = new three.Sprite(new three.SpriteMaterial({ map: glowTexture(), transparent: true, depthWrite: false }))
  glow.position.copy(middle).add(new three.Vector3(0, 0, noseBulb.radius * 1.2))
  glow.renderOrder = 1
  nose[0]?.parent?.add(glow)
  pose()
}

function pose() {
  const now = move?.pose()
  if (!scene || !now) return
  bulb?.material.emissive.copy(bulb.colour).lerp(bulb.lit, now.glow * 0.55)
  for (const part of nose) part.scale.setScalar(1 + 0.06 * now.glow)
  if (glow) {
    glow.scale.setScalar(noseBulb.radius * (3 + 2.2 * now.glow))
    glow.material.opacity = 0.25 + 0.75 * now.glow
  }
  for (const end of ends) end.rotation.y = now.ends
  scene.pose(now)
}

function spin(now: number) {
  if (dancing.value && onScreen && move) {
    move.advance(((now - last) / 1000) * speed.value)
    pose()
  }
  last = now
  frame = requestAnimationFrame(spin)
}

// One finger or the mouse walks around him, two fingers or a pinch zoom, the arrow keys walk.
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
  tilt.value = clamp(tilt.value + step[1], -89, 89)
}

const look = () => scene?.look({ turn: -turn.value, tilt: tilt.value, distance: distance.value })
watch(outline, show)
watch([turn, tilt, distance], look)

onMounted(async () => {
  // three.js is only fetched on this page, and only in the browser.
  try {
    const [{ createPoulpyScene }, threeModule] = await Promise.all([import('../poulpy3dScene'), import('three')])
    three = threeModule
    move = createRodolpheIdle()
    if (!canvas.value) return
    scene = createPoulpyScene(canvas.value)
  } catch {
    failed.value = true
    return
  }
  look()
  show()
  dancing.value = !window.matchMedia('(prefers-reduced-motion: reduce)').matches
  if (stage.value) {
    watching = new IntersectionObserver(([entry]) => { onScreen = entry.isIntersecting })
    watching.observe(stage.value)
  }
  last = performance.now()
  frame = requestAnimationFrame(spin)
})

onBeforeUnmount(() => {
  cancelAnimationFrame(frame)
  watching?.disconnect()
  glow?.material.map?.dispose()
  glow?.material.dispose()
  scene?.dispose()
})
</script>

<template>
  <div class="frame"><div class="poulpy-3d">
    <div ref="stage" class="stage">
      <canvas
        ref="canvas"
        tabindex="0"
        role="img"
        aria-label="Poulpy in 3D as Rodolphe, a red-nosed reindeer with antlers and a striped scarf, swaying to a tune as his nose glows. Drag, or use the arrow keys, to look around him."
        @pointerdown="press"
        @pointermove="drag"
        @pointerup="release"
        @pointercancel="release"
        @wheel="zoom"
        @keydown="nudge"
      />
      <p v-if="failed" class="failed">The 3D preview could not start in this browser.</p>
      <p v-else class="hint">Drag any way to look around, pinch to zoom</p>
    </div>
    <div class="controls">
      <div class="toggles">
        <button type="button" class="toggle" :aria-pressed="dancing" @click="dancing = !dancing">Dance</button>
        <button type="button" class="toggle" :aria-pressed="outline" @click="outline = !outline">Outline</button>
      </div>
      <label class="slider"><span>Tilt</span><input v-model.number="tilt" type="range" min="-89" max="89" step="1" /><output>{{ Math.round(tilt) }}°</output></label>
      <label class="slider"><span>Speed</span><input v-model.number="speed" type="range" min="0.1" max="3" step="0.1" @input="dancing = true" /><output>{{ speed.toFixed(1) }}×</output></label>
    </div>
  </div></div>
</template>

<style scoped>
.frame { container-type: inline-size; }
.poulpy-3d { display: flex; flex-direction: column; gap: 12px; margin: 16px 0; }
@container (min-width: 760px) {
  .poulpy-3d { display: grid; grid-template-columns: minmax(0, 1fr) 280px; align-items: start; gap: 24px; }
  .controls { position: sticky; top: 16px; padding: 16px; border-radius: 24px; background: var(--cp-surface-variant); }
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
.slider span { flex: none; width: 4.5em; }
.slider output { min-width: 3.6em; text-align: right; font-variant-numeric: tabular-nums; }
.slider input { flex: 1; min-width: 0; height: 36px; accent-color: var(--cp-primary); }
</style>
