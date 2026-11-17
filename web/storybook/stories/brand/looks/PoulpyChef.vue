<script setup lang="ts">
// Poulpy the cook (design/poulpy-3d/looks/chef.mjs), live, drawn by poulpy3dScene.ts, running the cook's idle (CHEF in
// motion.mjs): his chin goes up and his moustache twitches. Nothing moves off screen or with reduced motion.
import { onBeforeUnmount, onMounted, shallowRef, useTemplateRef, watch } from 'vue'
import type { Chef } from '../../../../../design/poulpy-3d/motion.mjs'
import type { createPoulpyScene } from '../poulpy3dScene'

const variants = [{ face: 'grin', about: 'Eyes wide, brows up and a small confident grin under a big, solid curled moustache.' }]

const stages = useTemplateRef<HTMLDivElement[]>('stages')
const canvases = useTemplateRef<HTMLCanvasElement[]>('canvases')
const turn = shallowRef(0) // degrees he has turned for the viewer, anticlockwise seen from above; 0 is face on
const tilt = shallowRef(14) // degrees above him
const outline = shallowRef(false)
const moving = shallowRef(false)
const failed = shallowRef(false)

let scenes: ReturnType<typeof createPoulpyScene>[] = []
let showAll: (() => void) | undefined
let idle: Chef | undefined
let frame = 0
let last = 0
let onScreen = true
let watching: IntersectionObserver | undefined

// Framed for his toque: looked at a little higher than his middle, from a little further away.
const look = () => scenes.forEach((scene) => scene.look({ turn: -turn.value, tilt: tilt.value, distance: 10.4, centre: 1.62 }))
watch([turn, tilt], look)
watch(outline, () => showAll?.())
watch(moving, (on) => { if (!on && idle) scenes.forEach((scene) => scene.pose(undefined)) })

function tick(now: number) {
  if (moving.value && onScreen && idle) {
    const pose = idle.advance((now - last) / 1000)
    scenes.forEach((scene) => scene.pose(pose))
  }
  last = now
  frame = requestAnimationFrame(tick)
}

onMounted(async () => {
  // three.js is only fetched on this page, and only in the browser.
  try {
    const [{ createPoulpyScene }, { buildChef }, { createChef }, tokens] = await Promise.all([
      import('../poulpy3dScene'),
      import('../../../../../design/poulpy-3d/looks/chef.mjs'),
      import('../../../../../design/poulpy-3d/motion.mjs'),
      import('../../../../../design/tokens.json').then((m) => m.default),
    ])
    if (!canvases.value?.length) return
    const colours = tokens.color.poulpy, tones = tokens.mood
    scenes = canvases.value.map((canvas) => createPoulpyScene(canvas))
    // His face is drawn apart, at each step of a twitch, so the idle can twitch his moustache.
    showAll = () => scenes.forEach((scene) => {
      scene.show({ model: buildChef({ face: false, tones, colours }), faces: (t) => buildChef({ twitch: t, tones, colours, faceOnly: true }) }, outline.value)
    })
    idle = createChef()
  } catch {
    failed.value = true
    return
  }
  look()
  showAll()
  moving.value = !window.matchMedia('(prefers-reduced-motion: reduce)').matches
  watching = new IntersectionObserver((entries) => { onScreen = entries.some((entry) => entry.isIntersecting) })
  stages.value?.forEach((stage) => watching!.observe(stage))
  last = performance.now()
  frame = requestAnimationFrame(tick)
})

onBeforeUnmount(() => {
  cancelAnimationFrame(frame)
  watching?.disconnect()
  scenes.forEach((scene) => scene.dispose())
})
</script>

<template>
  <div class="poulpy-chef">
    <div class="variants">
      <figure v-for="v in variants" :key="v.face" class="variant">
        <div ref="stages" class="stage">
          <canvas ref="canvases" role="img" :aria-label="`Poulpy the cook in a tall white toque and a curled moustache, ${v.about.toLowerCase()}`" />
          <p v-if="failed" class="failed">The 3D preview could not start in this browser.</p>
        </div>
        <figcaption>{{ v.about }}</figcaption>
      </figure>
    </div>
    <div class="controls">
      <div class="toggles">
        <button type="button" class="toggle" :aria-pressed="moving" @click="moving = !moving">Idle</button>
        <button type="button" class="toggle" :aria-pressed="outline" @click="outline = !outline">Outline</button>
      </div>
      <label class="slider"><span>Turn</span><input v-model.number="turn" type="range" min="-180" max="180" step="1" /><output>{{ turn }}°</output></label>
      <label class="slider"><span>Tilt</span><input v-model.number="tilt" type="range" min="-30" max="60" step="1" /><output>{{ tilt }}°</output></label>
    </div>
  </div>
</template>

<style scoped>
.poulpy-chef { display: flex; flex-direction: column; gap: 12px; margin: 16px 0; }
.variants { display: grid; max-width: 420px; gap: 16px; }
.variant { margin: 0; display: flex; flex-direction: column; gap: 8px; }
.stage { position: relative; aspect-ratio: 5 / 6; border-radius: 24px; background: var(--cp-surface-variant); overflow: hidden; }
.stage canvas { position: absolute; inset: 0; width: 100%; height: 100%; border-radius: 24px; }
figcaption { font-size: 14px; color: var(--cp-on-surface-variant); }
.failed { position: absolute; inset: 0; display: grid; place-items: center; margin: 0; padding: 24px; text-align: center; font-size: 13px; color: var(--cp-on-surface-variant); }
.controls { display: flex; flex-wrap: wrap; gap: 12px; align-items: center; }
.toggles { display: inline-flex; gap: 8px; }
.toggle { height: 36px; padding: 0 16px; border-radius: 999px; border: 1px solid var(--cp-outline); font-size: 14px; font-weight: 700; color: var(--cp-on-surface); cursor: pointer; }
.toggle[aria-pressed='true'] { border-color: var(--cp-primary); color: var(--cp-on-primary-container); background: var(--cp-primary-container); }
.toggle:focus-visible { outline: 3px solid var(--cp-primary); outline-offset: 2px; }
.slider { flex: 1 1 100%; display: flex; align-items: center; gap: 10px; min-width: 0; font-size: 14px; font-weight: 700; color: var(--cp-on-surface); }
.slider span { flex: none; width: 4em; }
.slider output { min-width: 3.6em; text-align: right; font-variant-numeric: tabular-nums; }
.slider input { flex: 1; min-width: 0; height: 36px; accent-color: var(--cp-primary); }
</style>
