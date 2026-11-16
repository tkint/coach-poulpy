<script setup lang="ts">
// Poulpy in 3D, to turn around: the shape of design/poulpy-3d/model.mjs, drawn by poulpy3dScene.ts.
// With `moods` the switch picks one of his six moods instead of a side of the plush, with `poses` one of his twelve poses.
// With `motion` he moves himself (design/poulpy-3d/motion.mjs) and the viewer stays put: "turn" is his idle,
// "jump" his little jumps on the spot, "jump-turn" the same jumps, each turning him a step, and "cheer" the
// champion's jumps of joy, with his medal and confetti. "wink" is the cocky one's wink, with his stars. He only moves while he is on screen.
import { RotateCcw, RotateCw } from '@lucide/vue'
import { onBeforeUnmount, onMounted, shallowRef, useTemplateRef, watch } from 'vue'
import type { PoulpyMood, PoulpySide } from '../../../../design/poulpy-3d/model.mjs'
import type { PoulpyPose } from '../../../../design/poulpy-3d/poses.mjs'
import type { Cheer, Jump, Turn, Wink } from '../../../../design/poulpy-3d/motion.mjs'
import type { createPoulpyScene } from './poulpy3dScene'

const { motion, moods, poses } = defineProps<{ motion?: 'turn' | 'jump' | 'jump-turn' | 'cheer' | 'wink'; moods?: boolean; poses?: boolean }>()
const cheering = motion === 'cheer'
const winking = motion === 'wink'
const posed = cheering || winking // he keeps one pose, so there is none to pick
const jumping = motion === 'jump' || motion === 'jump-turn' || cheering
const turning = motion !== 'jump' && !posed // he turns, or without `motion` the viewer walks around him

const canvas = useTemplateRef<HTMLCanvasElement>('canvas')
const stage = useTemplateRef<HTMLDivElement>('stage')
const side = shallowRef<PoulpySide | PoulpyMood | PoulpyPose>(cheering ? 'champion' : winking ? 'cocky' : poses ? 'cocky' : moods ? 'happy' : 'blue')
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
const wiggle = shallowRef(true) // on, each step differs a little as designed (20 to 30); off, every step is exactly `step`
// The jump's key values, in units (his head's radius is about 1): they start at the design, read from JUMP.
const height = shallowRef(0) // how far he goes up
const tuck = shallowRef(0) // how far his head tucks into his arms before the jump
const landing = shallowRef(0) // how far his head sinks into his arms as he lands
const spread = shallowRef(0) // how far his arms widen as his head tucks in
const hang = shallowRef(1) // how heavy his arms hang in the air: 1 is the design
const lean = shallowRef(0) // degrees he tilts to the side in the air
const pause = shallowRef(0) // seconds between two jumps, on average
const eye = shallowRef<'left' | 'right'>('right') // the eye he shuts when he winks, his own: the right one is the design
const failed = shallowRef(false)
// The values he is set to, as text to hand over: what the sliders say, for the move or the pose shown.
const exported = shallowRef('')
const copied = shallowRef(false)
function values() {
  const round = (n: number) => Math.round(n * 1000) / 1000
  return {
    page: motion ? `motion: ${motion}` : poses ? 'poses' : moods ? 'moods' : 'sides',
    ...(posed ? {} : { shown: side.value }),
    ...(winking ? { eye: eye.value } : {}),
    view: { turn: round(turn.value), tilt: round(tilt.value), distance: round(distance.value) },
    ...(jumping ? { jump: { height: round(height.value), tuck: round(tuck.value), landing: round(landing.value), spread: round(spread.value), hang: round(hang.value), lean: round(lean.value), pause: round(pause.value) } } : {}),
    ...(motion && turning ? { step: step.value, wiggle: wiggle.value, direction: direction.value } : {}),
    ...(motion && !winking ? { softArms: round(softness.value) } : {}),
    speed: round(speed.value),
  }
}
async function exportValues() {
  exported.value = JSON.stringify(values(), null, 2)
  try {
    await navigator.clipboard.writeText(exported.value)
    copied.value = true
    setTimeout(() => { copied.value = false }, 2000)
  } catch {
    copied.value = false // the text below is there to copy by hand
  }
}

let scene: ReturnType<typeof createPoulpyScene> | undefined
let frame = 0
let last = 0
let move: Turn | Jump | Cheer | Wink | undefined
let winkFace: ((t: number, side?: 'left' | 'right') => (string | number)[][]) | undefined
let onScreen = true
let watching: IntersectionObserver | undefined

const sides: { id: PoulpySide | PoulpyMood | PoulpyPose; label: string; suffix?: string }[] = poses
  ? [
      { id: 'cocky', label: 'Cocky' }, { id: 'straining', label: 'Straining' }, { id: 'champion', label: 'Champion' }, { id: 'standingTall', label: 'Standing tall' },
      { id: 'exhausted', label: 'Exhausted' }, { id: 'sideGlance', label: 'Side glance' }, { id: 'sleeping', label: 'Sleeping' }, { id: 'patchedUp', label: 'Patched up' },
      { id: 'shy', label: 'Shy' }, { id: 'thinking', label: 'Thinking' }, { id: 'pouting', label: 'Pouting' }, { id: 'fuming', label: 'Fuming' },
    ]
  : moods
  ? [{ id: 'proud', label: 'Proud' }, { id: 'happy', label: 'Happy' }, { id: 'recovering', label: 'Recovering' }, { id: 'annoyed', label: 'Annoyed' }, { id: 'grumpy', label: 'Grumpy' }, { id: 'fuming', label: 'Fuming' }]
  : [{ id: 'blue', label: 'Blue', suffix: 'side' }, { id: 'ember', label: 'Grumpy', suffix: 'side' }]
// Named for him: his right eye is on the viewer's left.
const eyes = [{ id: 'right', label: 'His right eye' }, { id: 'left', label: 'His left eye' }] as const
const directions = [
  { id: 'anticlockwise', label: 'Anticlockwise', icon: RotateCcw },
  { id: 'clockwise', label: 'Clockwise', icon: RotateCw },
] as const
const moves = { turn: 'turning around in short steps', jump: 'doing little jumps on the spot', 'jump-turn': 'doing little jumps that turn him around', cheer: 'the champion, jumping for joy with his medal while confetti flies', wink: 'the cocky one, winking as stars pop up around him' }
const clamp = (value: number, low: number, high: number) => Math.min(high, Math.max(low, value))
const wrap = (degrees: number) => ((degrees % 360) + 360) % 360

// A full turn of the viewer takes about 13 s at speed 1. With `motion`, Poulpy moves instead.
function spin(now: number) {
  // Off screen he waits, so several of him on a page cost no more than the one in view.
  const moving = spinning.value && onScreen
  if (moving && move) scene?.pose(move.advance(((now - last) / 1000) * speed.value))
  else if (moving) turn.value = wrap(turn.value + ((now - last) / 1000) * 28 * speed.value * (direction.value === 'anticlockwise' ? 1 : -1))
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

const show = () => scene?.show(cheering ? { pose: 'champion', effects: false } : winking ? { pose: 'cocky', effects: false, faces: winkFace && ((t: number) => winkFace!(t, eye.value)) } : poses ? { pose: side.value as PoulpyPose } : (side.value as PoulpySide | PoulpyMood), outline.value)
// The scene wants where the viewer stands: the other way around from how far he has turned.
const look = () => scene?.look({ turn: -turn.value, tilt: tilt.value, distance: distance.value })
watch([side, outline, eye], show)
watch(eye, (value) => { if (move && 'side' in move) move.side = value })
watch([turn, tilt, distance], look)
watch(softness, (value) => { if (move && 'softness' in move) move.softness = value })
const stride = () => (!turning ? 0 : (step.value / 24) * (direction.value === 'anticlockwise' ? 1 : -1))
watch([step, direction], () => { if (move && 'stride' in move) move.stride = stride() })
watch(wiggle, (value) => { if (move && 'wiggle' in move) move.wiggle = value })
watch([height, tuck, landing, spread, hang, lean, pause], (values) => {
  if (move && 'height' in move) [move.height, move.tuck, move.landing, move.spread, move.hang, move.lean, move.pause] = values
})

onMounted(async () => {
  // three.js is only fetched on this page, and only in the browser.
  try {
    const { createPoulpyScene } = await import('./poulpy3dScene')
    if (motion) {
      const { createCheer, createJump, createTurn, createWink } = await import('../../../../design/poulpy-3d/motion.mjs')
      if (winking) {
        winkFace = (await import('../../../../design/poulpy-3d/poses.mjs')).winkFace
        move = createWink()
      } else if (jumping) {
        const jump = (cheering ? createCheer : createJump)({ softness: softness.value, stride: stride(), wiggle: wiggle.value })
        ;[height.value, tuck.value, landing.value, spread.value, lean.value, pause.value] = [jump.height, jump.tuck, jump.landing, jump.spread, jump.lean, jump.pause]
        move = jump
      } else move = createTurn({ softness: softness.value })
    }
    if (!canvas.value) return
    scene = createPoulpyScene(canvas.value)
  } catch {
    failed.value = true
    return
  }
  look()
  show()
  spinning.value = !window.matchMedia('(prefers-reduced-motion: reduce)').matches
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
        :aria-label="motion ? `Poulpy in 3D, ${moves[motion]}. Drag, or use the arrow keys, to look around him.` : 'Poulpy in 3D. Drag, or use the arrow keys, to turn him.'"
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
      <div v-if="!posed" class="sides" :class="{ moods: moods || poses }" role="group" :aria-label="poses ? 'Pose' : moods ? 'Mood' : 'Side'">
        <button v-for="s in sides" :key="s.id" type="button" class="choice" :aria-pressed="side === s.id" :aria-label="s.suffix ? `${s.label} ${s.suffix}` : s.label" @click="side = s.id">{{ s.label }}<span v-if="s.suffix" class="wide">&nbsp;{{ s.suffix }}</span></button>
      </div>
      <div v-if="winking" class="sides" role="group" aria-label="The eye he shuts, his own">
        <button v-for="e in eyes" :key="e.id" type="button" class="choice" :aria-pressed="eye === e.id" @click="eye = e.id">{{ e.label }}</button>
      </div>
      <div v-if="turning" class="sides" role="group" aria-label="Direction, seen from above">
        <button v-for="d in directions" :key="d.id" type="button" class="choice icon" :aria-pressed="direction === d.id" :aria-label="d.label" :title="d.label" @click="direction = d.id"><component :is="d.icon" :size="18" aria-hidden="true" /></button>
      </div>
      <div class="toggles">
        <button type="button" class="toggle" :aria-pressed="outline" @click="outline = !outline">Outline</button>
        <button type="button" class="toggle" :aria-pressed="spinning" @click="spinning = !spinning">{{ winking ? 'Wink' : jumping ? 'Jump' : motion ? 'Turn' : 'Spin' }}</button>
        <button v-if="motion && turning" type="button" class="toggle" :aria-pressed="wiggle" title="On, each step differs a little (20° to 30° around 24°). Off, every step is exactly the Step." @click="wiggle = !wiggle">Wiggle</button>
        <button type="button" class="toggle export" @click="exportValues">{{ copied ? 'Copied' : 'Export values' }}</button>
      </div>
      <label v-if="!motion" class="slider"><span>Turn</span><input v-model.number="turn" type="range" min="0" max="360" step="1" @input="spinning = false" /><output>{{ Math.round(turn) }}°</output></label>
      <label class="slider"><span>Tilt</span><input v-model.number="tilt" type="range" min="-89" max="89" step="1" /><output>{{ Math.round(tilt) }}°</output></label>
      <template v-if="jumping">
        <label class="slider"><span>Height</span><input v-model.number="height" type="range" min="0" max="1" step="0.01" /><output>{{ height.toFixed(2) }}</output></label>
        <label class="slider"><span>Tuck</span><input v-model.number="tuck" type="range" min="0" max="0.5" step="0.01" /><output>{{ tuck.toFixed(2) }}</output></label>
        <label class="slider"><span>Landing</span><input v-model.number="landing" type="range" min="0" max="0.5" step="0.01" /><output>{{ landing.toFixed(2) }}</output></label>
        <label class="slider"><span>Spread</span><input v-model.number="spread" type="range" min="0" max="0.5" step="0.01" /><output>{{ spread.toFixed(2) }}</output></label>
        <label class="slider"><span>Hang</span><input v-model.number="hang" type="range" min="0" max="4" step="0.05" /><output>{{ hang.toFixed(2) }}</output></label>
        <label class="slider"><span>Lean</span><input v-model.number="lean" type="range" min="0" max="30" step="0.5" /><output>{{ lean.toFixed(1) }}°</output></label>
        <label class="slider"><span>Pause</span><input v-model.number="pause" type="range" min="0" max="3" step="0.05" /><output>{{ pause.toFixed(2) }} s</output></label>
      </template>
      <label v-if="motion && turning" class="slider"><span>Step</span><input v-model.number="step" type="range" min="1" :max="jumping ? 720 : 180" step="1" /><output>{{ step }}°</output></label>
      <label v-if="motion && !winking" class="slider"><span>Soft arms</span><input v-model.number="softness" type="range" min="0" max="4" step="0.05" /><output>{{ softness.toFixed(2) }}</output></label>
      <label class="slider"><span>Speed</span><input v-model.number="speed" type="range" min="0.1" max="5" step="0.1" @input="spinning = true" /><output>{{ speed.toFixed(1) }}×</output></label>
      <label v-if="exported" class="exported"><span>Values to hand over</span><textarea readonly rows="8" :value="exported" @focus="($event.target as HTMLTextAreaElement).select()" /></label>
    </div>
  </div></div>
</template>

<style scoped>
.frame { container-type: inline-size; }
.poulpy-3d { display: flex; flex-direction: column; gap: 12px; margin: 16px 0; }
.exported { flex: 1 1 100%; display: flex; flex-direction: column; gap: 6px; font-size: 14px; font-weight: 700; }
.exported textarea { width: 100%; box-sizing: border-box; padding: 8px 10px; border-radius: 12px; border: 1px solid var(--cp-outline); background: var(--cp-surface); color: var(--cp-on-surface); font: 12px/1.4 ui-monospace, monospace; resize: vertical; }
/* Wide enough, the controls are a panel beside him, and the sliders stay in view as the page scrolls. */
@container (min-width: 760px) {
  .poulpy-3d { display: grid; grid-template-columns: minmax(0, 1fr) 320px; align-items: start; gap: 24px; }
  .controls { position: sticky; top: 16px; max-height: calc(100vh - 32px); overflow-y: auto; padding: 16px; border-radius: 24px; background: var(--cp-surface-variant); }
  .controls .sides { background: var(--cp-surface); }
  .stage { position: sticky; top: 16px; }
}
.stage { position: relative; aspect-ratio: 4 / 3; max-height: 70vh; border-radius: 24px; background: var(--cp-surface-variant); overflow: hidden; }
.stage canvas { position: absolute; inset: 0; width: 100%; height: 100%; touch-action: none; cursor: grab; border-radius: 24px; }
.stage canvas:active { cursor: grabbing; }
.stage canvas:focus-visible { outline: 3px solid var(--cp-primary); outline-offset: -3px; }
.hint, .failed { position: absolute; right: 16px; bottom: 10px; margin: 0; font-size: 13px; color: var(--cp-on-surface-variant); pointer-events: none; }
.failed { inset: 0; display: grid; place-items: center; padding: 24px; text-align: center; }
.controls { display: flex; flex-wrap: wrap; gap: 12px; align-items: center; }
.sides { display: inline-flex; gap: 4px; padding: 4px; border-radius: 999px; background: var(--cp-surface-variant); }
.sides.moods { flex-wrap: wrap; border-radius: 22px; }
.toggles { display: inline-flex; gap: 8px; }
.export { white-space: nowrap; }
.choice, .toggle { height: 36px; padding: 0 16px; border-radius: 999px; border: 1px solid transparent; font-size: 14px; font-weight: 700; color: var(--cp-on-surface); cursor: pointer; }
.choice.icon { display: inline-grid; place-items: center; width: 40px; padding: 0; }
.choice[aria-pressed='true'] { background: var(--cp-primary); color: var(--cp-on-primary); }
.toggle { border-color: var(--cp-outline); }
.toggle[aria-pressed='true'] { border-color: var(--cp-primary); color: var(--cp-on-primary-container); background: var(--cp-primary-container); }
.choice:focus-visible, .toggle:focus-visible { outline: 3px solid var(--cp-primary); outline-offset: 2px; }
/* On a phone the switches still share one row. */
@media (max-width: 480px) {
  .controls { column-gap: 4px; }
  .sides { gap: 2px; padding: 3px; }
  .choice, .toggle { padding: 0 8px; font-size: 13px; }
  .wide { display: none; }
  .choice.icon { width: 32px; }
  .toggles { gap: 4px; }
}
.slider { flex: 1 1 100%; display: flex; align-items: center; gap: 10px; min-width: 0; font-size: 14px; font-weight: 700; }
.slider span { flex: none; width: 5.5em; }
.slider output { min-width: 3.6em; text-align: right; font-variant-numeric: tabular-nums; }
.slider input { flex: 1; min-width: 0; height: 36px; accent-color: var(--cp-primary); }
</style>
