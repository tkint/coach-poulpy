<script setup lang="ts">
// Poulpy's autumn look, live: design/poulpy-3d/looks/autumn.mjs, drawn by poulpy3dScene.ts. He wears his scarf and sways while
// maple leaves fall around him. The leaves are flat and face the viewer as they fall, like the comic effects of the poses; once on
// the floor they lie flat on it. He only moves while he is on screen, and starts still for anyone who asked for less motion.
import * as THREE from 'three'
import { onBeforeUnmount, onMounted, shallowRef, useTemplateRef, watch } from 'vue'
import tokens from '../../../../../design/tokens.json'
import { AUTUMN, buildAutumn, createAutumn, leafShape, type Autumn, type AutumnPose } from '../../../../../design/poulpy-3d/looks/autumn.mjs'
import type { createPoulpyScene } from '../poulpy3dScene'

const canvas = useTemplateRef<HTMLCanvasElement>('canvas')
const stage = useTemplateRef<HTMLDivElement>('stage')
const playing = shallowRef(false)
const leavesShown = shallowRef(true)
const outline = shallowRef(false)
const turn = shallowRef(0) // degrees the viewer has walked around him; 0 is face on
const tilt = shallowRef(20) // degrees above him
const speed = shallowRef(1)
const failed = shallowRef(false)

let scene: ReturnType<typeof createPoulpyScene> | undefined
let move: Autumn | undefined
let posed: AutumnPose | undefined
let leaves: THREE.InstancedMesh | undefined
let frame = 0
let last = 0
let onScreen = true
let watching: IntersectionObserver | undefined

const clamp = (value: number, low: number, high: number) => Math.min(high, Math.max(low, value))
const wrap = (degrees: number) => ((degrees % 360) + 360) % 360

// Each leaf, falling, stands across the line of sight, turned in its own plane and half over; on the floor it lies flat.
const facing = new THREE.Quaternion(), standing = new THREE.Quaternion(), lying = new THREE.Quaternion(), own = new THREE.Quaternion()
const flat = new THREE.Quaternion().setFromAxisAngle(new THREE.Vector3(1, 0, 0), -Math.PI / 2)
const along = new THREE.Euler(), piece = new THREE.Object3D()
function placeLeaves() {
  if (!leaves || !posed) return
  // The way the viewer looks, as the scene places its camera: walked around by -turn, looking down by tilt.
  facing.setFromEuler(along.set(THREE.MathUtils.degToRad(-tilt.value), THREE.MathUtils.degToRad(-turn.value), 0, 'YXZ'))
  posed.leaves.forEach(([x, y, z, roll, flip, lie, size], i) => {
    standing.copy(facing).multiply(own.setFromEuler(along.set(0, flip, roll, 'ZYX')))
    lying.setFromAxisAngle(THREE.Object3D.DEFAULT_UP, roll).multiply(flat)
    piece.position.set(x, y, z)
    piece.quaternion.slerpQuaternions(standing, lying, lie)
    piece.scale.setScalar(leavesShown.value ? size : 0)
    piece.updateMatrix()
    leaves!.setMatrixAt(i, piece.matrix)
  })
  leaves.instanceMatrix.needsUpdate = true
}

function show() {
  scene?.show({ model: buildAutumn({ tones: tokens.mood, colours: tokens.color.poulpy }) }, outline.value)
}
function look() {
  placeLeaves()
  scene?.look({ turn: -turn.value, tilt: tilt.value, distance: 10.4, centre: 1.5 })
}
function step(now: number) {
  if (playing.value && onScreen && move) {
    posed = move.advance(((now - last) / 1000) * speed.value)
    placeLeaves()
    scene?.pose(posed)
  }
  last = now
  frame = requestAnimationFrame(step)
}

// One finger or the mouse walks around him, the arrow keys too.
let dragging: [number, number] | undefined
function press(event: PointerEvent) {
  canvas.value?.setPointerCapture(event.pointerId)
  dragging = [event.clientX, event.clientY]
}
function drag(event: PointerEvent) {
  if (!dragging) return
  turn.value = wrap(turn.value + (event.clientX - dragging[0]) * 0.6)
  tilt.value = clamp(tilt.value + (event.clientY - dragging[1]) * 0.45, -10, 60)
  dragging = [event.clientX, event.clientY]
}
function release() {
  dragging = undefined
}
function nudge(event: KeyboardEvent) {
  const by = ({ ArrowLeft: [-8, 0], ArrowRight: [8, 0], ArrowUp: [0, 6], ArrowDown: [0, -6] } as Record<string, number[]>)[event.key]
  if (!by) return
  event.preventDefault()
  turn.value = wrap(turn.value + by[0])
  tilt.value = clamp(tilt.value + by[1], -10, 60)
}

watch(outline, show)
watch([turn, tilt, leavesShown], look)

onMounted(async () => {
  // three.js is only fetched on this page, and only in the browser.
  try {
    const { createPoulpyScene } = await import('../poulpy3dScene')
    if (!canvas.value) return
    scene = createPoulpyScene(canvas.value)
  } catch {
    failed.value = true
    return
  }
  move = createAutumn()
  posed = move.pose()
  leaves = new THREE.InstancedMesh(
    new THREE.BufferGeometry()
      .setAttribute('position', new THREE.Float32BufferAttribute(leafShape().positions, 3))
      .setAttribute('normal', new THREE.Float32BufferAttribute(leafShape().normals, 3))
      .setIndex(leafShape().indices),
    new THREE.MeshBasicMaterial({ side: THREE.DoubleSide }),
    posed.leaves.length,
  )
  leaves.frustumCulled = false
  const colours = AUTUMN.leaves.colours.map((colour) => new THREE.Color(colour))
  posed.leaves.forEach((leaf, i) => leaves!.setColorAt(i, colours[leaf[7]]))
  scene.room.add(leaves)
  show()
  look()
  scene.pose(posed)
  playing.value = !window.matchMedia('(prefers-reduced-motion: reduce)').matches
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
  if (leaves) {
    scene?.room.remove(leaves)
    leaves.geometry.dispose()
    ;(leaves.material as THREE.Material).dispose()
    leaves.dispose()
  }
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
        aria-label="Poulpy in his autumn look: happy, in a white and red striped scarf, swaying gently as maple leaves fall around him. Drag, or use the arrow keys, to look around him."
        @pointerdown="press"
        @pointermove="drag"
        @pointerup="release"
        @pointercancel="release"
        @keydown="nudge"
      />
      <p v-if="failed" class="failed">The 3D preview could not start in this browser.</p>
      <p v-else class="hint">Drag to look around him</p>
    </div>
    <div class="controls">
      <div class="toggles">
        <button type="button" class="toggle" :aria-pressed="playing" @click="playing = !playing">Move</button>
        <button type="button" class="toggle" :aria-pressed="leavesShown" @click="leavesShown = !leavesShown">Leaves</button>
        <button type="button" class="toggle" :aria-pressed="outline" @click="outline = !outline">Outline</button>
      </div>
      <label class="slider"><span>Tilt</span><input v-model.number="tilt" type="range" min="-10" max="60" step="1" /><output>{{ Math.round(tilt) }}°</output></label>
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
.toggle { height: 36px; padding: 0 16px; border-radius: 999px; border: 1px solid var(--cp-outline); background: none; font-size: 14px; font-weight: 700; color: var(--cp-on-surface); cursor: pointer; }
.toggle[aria-pressed='true'] { border-color: var(--cp-primary); color: var(--cp-on-primary-container); background: var(--cp-primary-container); }
.toggle:focus-visible { outline: 3px solid var(--cp-primary); outline-offset: 2px; }
.slider { flex: 1 1 100%; display: flex; align-items: center; gap: 10px; min-width: 0; font-size: 14px; font-weight: 700; }
.slider span { flex: none; width: 4em; }
.slider output { min-width: 3.2em; text-align: right; font-variant-numeric: tabular-nums; }
.slider input { flex: 1; min-width: 0; height: 36px; accent-color: var(--cp-primary); }
</style>
