<script setup lang="ts">
// Drafts of how Home's Poulpy gets angry and calms down, to validate before anything is baked for the phones: after several taps he spins and turns
// annoyed, tapped again he spins and goes fuming, tapped while fuming he tucks and his fumes puff, then after a while he spins back to annoyed and
// to normal. Each step has a few variants to compare; PoulpyViewer.vue draws him and runs the clock (design/poulpy-3d/motion.mjs, poulpy3dScene.ts).
import { shallowRef, useTemplateRef } from 'vue'
import { spinning, tucking, type Move } from './moodMoves'
import PoulpyViewer, { type LivePoulpy, type PoulpyScene } from './PoulpyViewer.vue'

// `only` shows just some of the steps (the page of one mood's moves); with none, all five, as a sequence.
// `plain` leaves out the choice of variants: just the steps and the run.
const props = defineProps<{ only?: string[]; plain?: boolean }>()
type State = 'happy' | 'annoyed' | 'fuming'
// A move is anything that gives a pose for a time step; `swap` says when the face changes: when he is half way round, or as he starts.
interface Variant { id: string; label: string; note: string; make: (clockwise: boolean) => Move; seconds: number; swap: 'half' | 'start'; boost?: number }
interface Step { id: string; title: string; text: string; from: State; to: State; clockwise?: boolean; variants: Variant[] }

const SPINS: Pick<Variant, 'id' | 'label' | 'note' | 'make' | 'seconds' | 'swap'>[] = [
  { id: 'spot', label: 'On the spot', note: 'A quick turn, feet on the floor, arms trailing.', make: spinning(0, false), seconds: 1.5, swap: 'half' },
  { id: 'hop', label: 'With a hop', note: 'A small jump with a full turn in the air.', make: spinning(0.45, false), seconds: 1.7, swap: 'half' },
  { id: 'whip', label: 'Wind-up and whip', note: 'He leans back first, then whips round and rocks past the mark.', make: spinning(0, true), seconds: 2.1, swap: 'half' },
]
const TUCKS: Variant[] = [
  { id: 'small', label: 'Small tuck', note: 'He tucks a little and comes back up; the fumes puff twice as fast meanwhile.', make: tucking(0.12, 0.08, 0, 0), seconds: 1.8, swap: 'start', boost: 1 },
  { id: 'deep', label: 'Deep tuck, release', note: 'He squeezes down into his arms, then pops back up with a tiny hop.', make: tucking(0.22, 0.2, 0.08, 0), seconds: 2, swap: 'start', boost: 1.4 },
  { id: 'shudder', label: 'Shudder', note: 'A short tuck and a shake from side to side, fumes at full steam.', make: tucking(0.16, 0.12, 0.05, 9), seconds: 1.9, swap: 'start', boost: 2 },
]

// What an annoyed Poulpy does when he is tapped (not decided yet: for now a tap does nothing): short and grumpy, never a turn.
const HUFFS: Variant[] = [
  { id: 'shake', label: 'Shake', note: 'A quick shake of the head and body, side to side, without leaving the floor.', make: tucking(0.05, 0.03, 0, 12), seconds: 1.4, swap: 'start' },
  { id: 'stomp', label: 'Stomp', note: 'A small stamp: he drops a little and lands with a bounce.', make: tucking(0.1, 0.06, 0.04, 0), seconds: 1.4, swap: 'start' },
  { id: 'huff', label: 'Huff', note: 'He puffs up, then lets it out with a small lean.', make: tucking(0.14, 0.1, 0, 5), seconds: 1.6, swap: 'start' },
]

const allSteps: Step[] = [
  { id: 'annoyed', title: '1. Tapped a few times: he spins and turns annoyed', text: 'From his normal self, a full turn, and his face changes half way round.', from: 'happy', to: 'annoyed', variants: SPINS.map((v) => ({ ...v })) },
  { id: 'fuming', title: '2. Tapped again: he spins and goes fuming', text: 'The same turn, to the fuming one, with his fumes starting up behind him.', from: 'annoyed', to: 'fuming', variants: SPINS.map((v) => ({ ...v })) },
  { id: 'tuck', title: '3. Tapped while fuming: he tucks and the fumes move', text: 'He stays fuming and goes back to standing, the fumes puffing harder while he tucks.', from: 'fuming', to: 'fuming', variants: TUCKS },
  { id: 'huff', title: 'Tapped while annoyed: he huffs', text: 'He stays annoyed and answers the tap with a short grumpy move.', from: 'annoyed', to: 'annoyed', variants: HUFFS },
  { id: 'calm', title: '4. After a while: he spins back to annoyed', text: 'The turn the other way, clockwise, and the fumes go.', from: 'fuming', to: 'annoyed', clockwise: true, variants: SPINS.map((v) => ({ ...v })) },
  { id: 'normal', title: '5. Later still: he spins back to normal', text: 'The same turn back, clockwise, to his normal self.', from: 'annoyed', to: 'happy', clockwise: true, variants: SPINS.map((v) => ({ ...v })) },
]
const steps = props.only ? allSteps.filter((one) => props.only!.includes(one.id)) : allSteps.filter((one) => one.id !== 'huff')
// What he does while one is chosen, to compare: the picked variant of each step, and the whole run.
// The owner's pick of each step to start with: on the spot, on the spot, shudder, on the spot, with a hop.
const picked = shallowRef<Record<string, string>>({ annoyed: 'spot', fuming: 'spot', tuck: 'shudder', huff: 'shake', calm: 'spot', normal: 'hop' })
const playing = shallowRef('')
const viewer = useTemplateRef<InstanceType<typeof PoulpyViewer>>('viewer')

// What he does next, in order, each timed by the viewer's clock: a variant of a step, or a wait (with his fumes drifting, or not).
type Action =
  | { kind: 'perform'; step: Step; variant: Variant; move?: Move; elapsed: number; swapped: boolean }
  | { kind: 'wait'; seconds: number; fumes: boolean }
let queue: Action[] = []
let scene: PoulpyScene | undefined
let outlined = false
let state: State = 'happy'
let clock = 0
let boost = 0

const show = (to: State) => {
  state = to
  scene?.show(to === 'fuming' ? { pose: 'fuming', effects: true } : to, outlined)
}

// One step of the action at the head of the queue; true once it is over.
function perform(action: Extract<Action, { kind: 'perform' }>, seconds: number) {
  const { step, variant } = action
  if (!action.move) {
    action.move = variant.make(!!step.clockwise)
    show(step.from)
  }
  const dt = Math.min(seconds, 0.05)
  action.elapsed += dt
  const pose = action.move.advance(dt) as { yaw?: number }
  scene?.pose(pose as never)
  if (!action.swapped && (variant.swap === 'start' ? action.elapsed > 0.05 : Math.abs(pose.yaw ?? 0) > Math.PI)) {
    action.swapped = true
    if (step.from !== step.to) show(step.to)
    scene?.pose(pose as never)
  }
  boost = step.to === 'fuming' && action.swapped ? (variant.boost ?? 0.4) * Math.max(0, Math.min(1, 1 - (action.elapsed - variant.seconds + 0.5) / 0.5)) : 0
  scene?.fumes(clock, boost)
  if (action.elapsed < variant.seconds) return false
  if (step.from !== step.to && !action.swapped) show(step.to)
  scene?.pose(undefined)
  boost = 0
  return true
}

function advance(seconds: number) {
  clock += seconds
  const action = queue[0]
  // Between moves he stands, and when he is fuming his fumes drift.
  if (!action) { if (state === 'fuming') scene?.fumes(clock, 0); return }
  let done: boolean
  if (action.kind === 'perform') done = perform(action, seconds)
  else {
    action.seconds -= seconds
    if (action.fumes) scene?.fumes(clock, 0)
    done = action.seconds <= 0
  }
  if (!done) return
  queue.shift()
  if (!queue.length) playing.value = ''
}

const variantOf = (step: Step) => step.variants.find((v) => v.id === picked.value[step.id]) ?? step.variants[0]

function play(step: Step, variant: Variant) {
  picked.value = { ...picked.value, [step.id]: variant.id }
  playing.value = `${step.id}/${variant.id}`
  queue = [{ kind: 'perform', step, variant, elapsed: 0, swapped: false }]
  viewer.value?.play()
}

// Tapping him while he is fuming plays the tuck chosen, with its fumes; while he is annoyed, the huff chosen. Only on a page that shows them.
function tap() {
  const step = steps.find((one) => one.id === (state === 'fuming' ? 'tuck' : state === 'annoyed' ? 'huff' : ''))
  if (step && !playing.value) play(step, variantOf(step))
}

function playAll() {
  playing.value = 'all'
  show(steps[0].from)
  // A short pause before each step; the tuck is followed by a few seconds of fumes before he calms down.
  queue = steps.flatMap((step): Action[] => [
    { kind: 'wait', seconds: 0.9, fumes: false },
    { kind: 'perform', step, variant: variantOf(step), elapsed: 0, swapped: false },
    ...(step.id === 'tuck' ? [{ kind: 'wait', seconds: 2.5, fumes: true } as const] : []),
  ])
  viewer.value?.play()
}

async function start(next: PoulpyScene): Promise<LivePoulpy> {
  scene = next
  return {
    show(outline) {
      outlined = outline
      show(state)
    },
    advance,
    dispose() { queue = []; scene = undefined },
  }
}
// Starts as the first step does.
state = steps[0].from
const values = () => ({ page: `mood moves: ${steps.map((step) => step.id).join(', ')}`, ...(props.plain ? {} : { picked: Object.fromEntries(steps.map((step) => [step.id, picked.value[step.id]])) }) })
</script>

<template>
  <PoulpyViewer
    ref="viewer"
    label="Poulpy in 3D, in his moods from normal to annoyed and fuming. Tap him and he reacts to his mood."
    :framing="{ tilt: 8, distance: 9.4, centre: 1.35 }"
    :start="start"
    :values="values"
    @tap="tap"
  >
    <div class="steps">
      <button v-if="steps.length > 1" type="button" class="play-all" :aria-pressed="playing === 'all'" @click="playAll">{{ only ? 'Play them one after the other' : 'Play the whole sequence' }}</button>
      <section v-for="step in steps" :key="step.id" class="step">
        <h3>{{ only ? step.title.replace(/^\d\. /, '') : step.title }}</h3>
        <p>{{ step.text }}</p>
        <div v-if="!plain" class="sides" role="group" :aria-label="step.title">
          <button v-for="variant in step.variants" :key="variant.id" type="button" class="choice" :aria-pressed="picked[step.id] === variant.id" :title="variant.note" @click="play(step, variant)">{{ variant.label }}</button>
        </div>
        <p v-if="!plain" class="note">{{ step.variants.find((v) => v.id === picked[step.id])?.note }}</p>
      </section>
    </div>
  </PoulpyViewer>
</template>

<style scoped>
.steps { flex: 1 1 100%; display: flex; flex-direction: column; gap: 14px; }
.step { display: flex; flex-direction: column; gap: 6px; padding: 14px 16px; border-radius: 20px; background: var(--cp-surface); }
.step h3 { margin: 0; font-size: 16px; }
.step p { margin: 0; font-size: 14px; color: var(--cp-on-surface-variant); }
.step .note { font-size: 13px; }
.steps .step .sides { background: var(--cp-surface-variant); }
.play-all { height: 36px; padding: 0 16px; border-radius: 999px; border: 1px solid var(--cp-primary); background: var(--cp-primary-container); color: var(--cp-on-primary-container); font-size: 14px; font-weight: 700; cursor: pointer; align-self: flex-start; }
.play-all:focus-visible { outline: 3px solid var(--cp-primary); outline-offset: 2px; }
</style>
