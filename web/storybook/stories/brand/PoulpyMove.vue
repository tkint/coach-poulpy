<script setup lang="ts">
// One of Poulpy's moves (design/poulpy-3d/motion.mjs), live in PoulpyViewer.vue, with the controls to try other feels: "turn"
// is his idle, "jump" his little jumps on the spot, "jump-turn" the same jumps, each turning him a step, and "cheer" the
// champion's jumps of joy, with his medal and confetti. "wink" is the cocky one's wink, with his stars. He moves himself and the
// viewer stays put.
import { RotateCcw, RotateCw } from '@lucide/vue'
import { shallowRef, watch } from 'vue'
import type { Cheer, Jump, Turn, Wink } from '../../../../design/poulpy-3d/motion.mjs'
import PoulpyViewer, { type LivePoulpy, type PoulpyScene } from './PoulpyViewer.vue'

const { motion } = defineProps<{ motion: 'turn' | 'jump' | 'jump-turn' | 'cheer' | 'wink' }>()
const cheering = motion === 'cheer'
const winking = motion === 'wink'
const posed = cheering || winking // he keeps one pose
const jumping = motion === 'jump' || motion === 'jump-turn' || cheering
const turning = motion !== 'jump' && !posed // each step turns him

const softness = shallowRef(1) // how loose his arms are: 1 is the design
// Seen from above, and named for him.
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

// Named for him: his right eye is on the viewer's left.
const eyes = [{ id: 'right', label: 'His right eye' }, { id: 'left', label: 'His left eye' }] as const
const directions = [
  { id: 'anticlockwise', label: 'Anticlockwise', icon: RotateCcw },
  { id: 'clockwise', label: 'Clockwise', icon: RotateCw },
] as const
const moves = { turn: 'turning around in short steps', jump: 'doing little jumps on the spot', 'jump-turn': 'doing little jumps that turn him around', cheer: 'the champion, jumping for joy with his medal while confetti flies', wink: 'the cocky one, winking as stars pop up around him' }

let move: Turn | Jump | Cheer | Wink | undefined
let redraw: (() => void) | undefined
watch(eye, (value) => { if (move && 'side' in move) move.side = value; redraw?.() })
watch(softness, (value) => { if (move && 'softness' in move) move.softness = value })
const stride = () => (!turning ? 0 : (step.value / 24) * (direction.value === 'anticlockwise' ? 1 : -1))
watch([step, direction], () => { if (move && 'stride' in move) move.stride = stride() })
watch(wiggle, (value) => { if (move && 'wiggle' in move) move.wiggle = value })
watch([height, tuck, landing, spread, hang, lean, pause], (values) => {
  if (move && 'height' in move) [move.height, move.tuck, move.landing, move.spread, move.hang, move.lean, move.pause] = values
})

async function start(scene: PoulpyScene): Promise<LivePoulpy> {
  const { createCheer, createJump, createTurn, createWink } = await import('../../../../design/poulpy-3d/motion.mjs')
  let winkFace: ((t: number, side?: 'left' | 'right') => (string | number)[][]) | undefined
  if (winking) {
    winkFace = (await import('../../../../design/poulpy-3d/poses.mjs')).winkFace
    move = createWink()
  } else if (jumping) {
    const jump = (cheering ? createCheer : createJump)({ softness: softness.value, stride: stride(), wiggle: wiggle.value })
    ;[height.value, tuck.value, landing.value, spread.value, lean.value, pause.value] = [jump.height, jump.tuck, jump.landing, jump.spread, jump.lean, jump.pause]
    move = jump
  } else move = createTurn({ softness: softness.value })
  const running = move
  let outlined = false
  const show = () => scene.show(cheering ? { pose: 'champion', effects: false } : winking ? { pose: 'cocky', effects: false, faces: (t: number) => winkFace!(t, eye.value) } : 'blue', outlined)
  redraw = show
  return {
    show(outline) { outlined = outline; show() },
    advance: (seconds) => scene.pose(running.advance(seconds)),
    dispose() { move = undefined; redraw = undefined },
  }
}

function values() {
  const round = (n: number) => Math.round(n * 1000) / 1000
  return {
    page: `motion: ${motion}`,
    ...(winking ? { eye: eye.value } : {}),
    ...(jumping ? { jump: { height: round(height.value), tuck: round(tuck.value), landing: round(landing.value), spread: round(spread.value), hang: round(hang.value), lean: round(lean.value), pause: round(pause.value) } } : {}),
    ...(turning ? { step: step.value, wiggle: wiggle.value, direction: direction.value } : {}),
    ...(!winking ? { softArms: round(softness.value) } : {}),
  }
}
</script>

<template>
  <PoulpyViewer
    :label="`Poulpy in 3D, ${moves[motion]}.`"
    :framing="{ tilt: 26, distance: 9.6 }"
    :move-label="winking ? 'Wink' : jumping ? 'Jump' : 'Turn'"
    :speeds="{ max: 5, step: 0.1 }"
    :start="start"
    :values="values"
  >
    <template #pickers>
      <div v-if="winking" class="sides" role="group" aria-label="The eye he shuts, his own">
        <button v-for="e in eyes" :key="e.id" type="button" class="choice" :aria-pressed="eye === e.id" @click="eye = e.id">{{ e.label }}</button>
      </div>
      <div v-if="turning" class="sides" role="group" aria-label="Direction, seen from above">
        <button v-for="d in directions" :key="d.id" type="button" class="choice icon" :aria-pressed="direction === d.id" :aria-label="d.label" :title="d.label" @click="direction = d.id"><component :is="d.icon" :size="18" aria-hidden="true" /></button>
      </div>
    </template>
    <template v-if="turning" #toggles>
      <button type="button" class="toggle" :aria-pressed="wiggle" title="On, each step differs a little (20° to 30° around 24°). Off, every step is exactly the Step." @click="wiggle = !wiggle">Wiggle</button>
    </template>
    <template #sliders>
      <template v-if="jumping">
        <label class="slider"><span>Height</span><input v-model.number="height" type="range" min="0" max="1" step="0.01" /><output>{{ height.toFixed(2) }}</output></label>
        <label class="slider"><span>Tuck</span><input v-model.number="tuck" type="range" min="0" max="0.5" step="0.01" /><output>{{ tuck.toFixed(2) }}</output></label>
        <label class="slider"><span>Landing</span><input v-model.number="landing" type="range" min="0" max="0.5" step="0.01" /><output>{{ landing.toFixed(2) }}</output></label>
        <label class="slider"><span>Spread</span><input v-model.number="spread" type="range" min="0" max="0.5" step="0.01" /><output>{{ spread.toFixed(2) }}</output></label>
        <label class="slider"><span>Hang</span><input v-model.number="hang" type="range" min="0" max="4" step="0.05" /><output>{{ hang.toFixed(2) }}</output></label>
        <label class="slider"><span>Lean</span><input v-model.number="lean" type="range" min="0" max="30" step="0.5" /><output>{{ lean.toFixed(1) }}°</output></label>
        <label class="slider"><span>Pause</span><input v-model.number="pause" type="range" min="0" max="3" step="0.05" /><output>{{ pause.toFixed(2) }} s</output></label>
      </template>
      <label v-if="turning" class="slider"><span>Step</span><input v-model.number="step" type="range" min="1" :max="jumping ? 720 : 180" step="1" /><output>{{ step }}°</output></label>
      <label v-if="!winking" class="slider"><span>Soft arms</span><input v-model.number="softness" type="range" min="0" max="4" step="0.05" /><output>{{ softness.toFixed(2) }}</output></label>
    </template>
  </PoulpyViewer>
</template>
