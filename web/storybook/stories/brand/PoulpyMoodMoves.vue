<script setup lang="ts">
// Drafts of how Home's Poulpy gets angry and calms down, to validate before anything is baked for the phones: after several taps he spins and turns
// annoyed, tapped again he spins and goes fuming, tapped while fuming he tucks and his fumes puff, then after a while he spins back to annoyed and
// to normal. Each step has a few variants to compare; one scene plays them (design/poulpy-3d/motion.mjs, poulpy3dScene.ts).
import { onBeforeUnmount, onMounted, shallowRef, useTemplateRef } from 'vue'
import { TURN, createJump, createTurn } from '../../../../design/poulpy-3d/motion.mjs'
import type { createPoulpyScene } from './poulpy3dScene'

type State = 'happy' | 'annoyed' | 'fuming'
// A move is anything that gives a pose for a time step; `swap` says when the face changes: when he is half way round, or as he starts.
interface Move { advance: (seconds: number) => unknown }
interface Variant { id: string; label: string; note: string; make: (clockwise: boolean) => Move; seconds: number; swap: 'half' | 'start'; boost?: number }
interface Step { id: string; title: string; text: string; from: State; to: State; clockwise?: boolean; variants: Variant[] }

// The three ways to spin, shared by every spin: exactly 360 degrees (no wiggle).
const spinning = (hop: number, windUp: boolean): ((clockwise: boolean) => Move) => (clockwise) => {
  const stride = (clockwise ? -360 : 360) / 24
  if (hop) { const move = createJump({ stride, wiggle: false }); move.height = hop; move.hang = 3; return move }
  const turn = windUp ? { ...TURN, stepSeconds: 0.6, windUp: { degrees: 14, seconds: 0.3 }, body: { speed: 12, damping: 0.5 } } : TURN
  return createTurn({ stride, wiggle: false, turn } as never)
}
const SPINS: Pick<Variant, 'id' | 'label' | 'note' | 'make' | 'seconds' | 'swap'>[] = [
  { id: 'spot', label: 'On the spot', note: 'A quick turn, feet on the floor, arms trailing.', make: spinning(0, false), seconds: 1.5, swap: 'half' },
  { id: 'hop', label: 'With a hop', note: 'A small jump with a full turn in the air.', make: spinning(0.45, false), seconds: 1.7, swap: 'half' },
  { id: 'whip', label: 'Wind-up and whip', note: 'He leans back first, then whips round and rocks past the mark.', make: spinning(0, true), seconds: 2.1, swap: 'half' },
]
// Tucks while he fumes: he stays on the floor and comes back to standing.
const tucking = (tuck: number, spread: number, hop: number, lean: number): (() => Move) => () => {
  const move = createJump()
  Object.assign(move, { tuck, height: hop, landing: 0.05, spread, lean, pause: 99 })
  return move
}
const TUCKS: Variant[] = [
  { id: 'small', label: 'Small tuck', note: 'He tucks a little and comes back up; the fumes puff twice as fast meanwhile.', make: tucking(0.12, 0.08, 0, 0), seconds: 1.8, swap: 'start', boost: 1 },
  { id: 'deep', label: 'Deep tuck, release', note: 'He squeezes down into his arms, then pops back up with a tiny hop.', make: tucking(0.22, 0.2, 0.08, 0), seconds: 2, swap: 'start', boost: 1.4 },
  { id: 'shudder', label: 'Shudder', note: 'A short tuck and a shake from side to side, fumes at full steam.', make: tucking(0.16, 0.12, 0.05, 9), seconds: 1.9, swap: 'start', boost: 2 },
]

const steps: Step[] = [
  { id: 'annoyed', title: '1. Tapped a few times: he spins and turns annoyed', text: 'From his normal self, a full turn, and his face changes half way round.', from: 'happy', to: 'annoyed', variants: SPINS.map((v) => ({ ...v })) },
  { id: 'fuming', title: '2. Tapped again: he spins and goes fuming', text: 'The same turn, to the fuming one, with his fumes starting up behind him.', from: 'annoyed', to: 'fuming', variants: SPINS.map((v) => ({ ...v })) },
  { id: 'tuck', title: '3. Tapped while fuming: he tucks and the fumes move', text: 'He stays fuming and goes back to standing, the fumes puffing harder while he tucks.', from: 'fuming', to: 'fuming', variants: TUCKS },
  { id: 'calm', title: '4. After a while: he spins back to annoyed', text: 'The turn the other way, clockwise, and the fumes go.', from: 'fuming', to: 'annoyed', clockwise: true, variants: SPINS.map((v) => ({ ...v })) },
  { id: 'normal', title: '5. Later still: he spins back to normal', text: 'The same turn back, clockwise, to his normal self.', from: 'annoyed', to: 'happy', clockwise: true, variants: SPINS.map((v) => ({ ...v })) },
]
// What he does while one is chosen, to compare: the picked variant of each step, and the whole run.
const picked = shallowRef<Record<string, string>>(Object.fromEntries(steps.map((s) => [s.id, s.variants[0].id])))
const playing = shallowRef('')
const failed = shallowRef(false)

const canvas = useTemplateRef<HTMLCanvasElement>('canvas')
const stage = useTemplateRef<HTMLDivElement>('stage')
let scene: ReturnType<typeof createPoulpyScene> | undefined
let frame = 0
let state: State = 'happy'
let visible = true
let observer: IntersectionObserver | undefined
let clock = 0
let boost = 0
let run = 0 // each move gets a number; a new one stops the one before
let sequence = 0 // the same for the whole sequence

const show = (to: State) => {
  state = to
  scene?.show(to === 'fuming' ? { pose: 'fuming', effects: true } : to, false)
  scene?.look({ turn: 0, tilt: 8, distance: 9.4, centre: 1.35 })
}

function perform(step: Step, variant: Variant): Promise<void> {
  const mine = ++run
  const move = variant.make(!!step.clockwise)
  const fromState = step.from
  show(fromState)
  return new Promise((resolve) => {
    let last = performance.now(), elapsed = 0, swapped = false
    const tick = (now: number) => {
      if (mine !== run) return resolve()
      const dt = Math.min((now - last) / 1000, 0.05)
      last = now
      elapsed += dt
      clock += dt
      const pose = move.advance(dt) as { yaw?: number }
      scene?.pose(pose as never)
      if (!swapped && (variant.swap === 'start' ? elapsed > 0.05 : Math.abs(pose.yaw ?? 0) > Math.PI)) { swapped = true; if (step.from !== step.to) show(step.to); scene?.pose(pose as never) }
      boost = step.to === 'fuming' && swapped ? (variant.boost ?? 0.4) * Math.max(0, Math.min(1, 1 - (elapsed - variant.seconds + 0.5) / 0.5)) : 0
      scene?.fumes(clock, boost)
      if (elapsed >= variant.seconds) {
        if (step.from !== step.to && !swapped) show(step.to)
        scene?.pose(undefined)
        boost = 0
        return resolve()
      }
      frame = requestAnimationFrame(tick)
    }
    frame = requestAnimationFrame(tick)
  })
}

// Between moves he stands, and when he is fuming his fumes drift.
function idle(now: number) {
  if (visible) { clock = now / 1000; if (state === 'fuming') scene?.fumes(clock, 0) }
  frame = requestAnimationFrame(idle)
}

async function play(step: Step, variant: Variant) {
  sequence++
  cancelAnimationFrame(frame)
  picked.value = { ...picked.value, [step.id]: variant.id }
  playing.value = `${step.id}/${variant.id}`
  await perform(step, variant)
  if (playing.value === `${step.id}/${variant.id}`) { playing.value = ''; frame = requestAnimationFrame(idle) }
}

async function playAll() {
  cancelAnimationFrame(frame)
  playing.value = 'all'
  const mine = ++sequence
  show('happy')
  for (const step of steps) {
    await new Promise((resolve) => setTimeout(resolve, 900))
    if (mine !== sequence) return
    const variant = step.variants.find((v) => v.id === picked.value[step.id]) ?? step.variants[0]
    await perform(step, variant)
    if (mine !== sequence) return
    // The tuck is followed by a few seconds of fumes before he calms down.
    if (step.id === 'tuck') await new Promise((resolve) => { const end = performance.now() + 2500; const wait = (now: number) => { clock = now / 1000; scene?.fumes(clock, 0); now < end ? requestAnimationFrame(wait) : resolve(undefined) }; requestAnimationFrame(wait) })
  }
  if (mine !== sequence) return
  playing.value = ''
  frame = requestAnimationFrame(idle)
}

onMounted(async () => {
  try {
    const { createPoulpyScene } = await import('./poulpy3dScene')
    scene = createPoulpyScene(canvas.value!)
    show('happy')
    observer = new IntersectionObserver(([entry]) => { visible = entry.isIntersecting })
    observer.observe(stage.value!)
    frame = requestAnimationFrame(idle)
  } catch {
    failed.value = true
  }
})
onBeforeUnmount(() => {
  run++
  sequence++
  cancelAnimationFrame(frame)
  observer?.disconnect()
  scene?.dispose()
})
</script>

<template>
  <div class="frame"><div class="mood-moves">
    <div ref="stage" class="stage">
      <canvas ref="canvas" role="img" aria-label="Poulpy in 3D, going from his normal self to annoyed and fuming and back" />
      <p v-if="failed" class="failed">The 3D preview could not start in this browser.</p>
    </div>
    <div class="steps">
      <button type="button" class="play-all" :aria-pressed="playing === 'all'" @click="playAll">Play the whole sequence</button>
      <section v-for="step in steps" :key="step.id" class="step">
        <h3>{{ step.title }}</h3>
        <p>{{ step.text }}</p>
        <div class="sides" role="group" :aria-label="step.title">
          <button v-for="variant in step.variants" :key="variant.id" type="button" class="choice" :aria-pressed="picked[step.id] === variant.id" :title="variant.note" @click="play(step, variant)">{{ variant.label }}</button>
        </div>
        <p class="note">{{ step.variants.find((v) => v.id === picked[step.id])?.note }}</p>
      </section>
    </div>
  </div></div>
</template>

<style scoped>
.frame { container-type: inline-size; }
.mood-moves { display: flex; flex-direction: column; gap: 16px; margin: 16px 0; }
@container (min-width: 760px) {
  .mood-moves { display: grid; grid-template-columns: minmax(0, 1fr) 360px; align-items: start; gap: 24px; }
  .stage { position: sticky; top: 16px; }
}
.stage { position: relative; aspect-ratio: 4 / 3; max-height: 70vh; border-radius: 24px; background: var(--cp-surface-variant); overflow: hidden; }
.stage canvas { position: absolute; inset: 0; width: 100%; height: 100%; border-radius: 24px; }
.failed { position: absolute; inset: 0; margin: 0; display: grid; place-items: center; padding: 24px; text-align: center; font-size: 13px; color: var(--cp-on-surface-variant); }
.steps { display: flex; flex-direction: column; gap: 14px; }
.step { display: flex; flex-direction: column; gap: 6px; padding: 14px 16px; border-radius: 20px; background: var(--cp-surface-variant); }
.step h3 { margin: 0; font-size: 16px; }
.step p { margin: 0; font-size: 14px; color: var(--cp-on-surface-variant); }
.step .note { font-size: 13px; }
.sides { display: flex; flex-wrap: wrap; gap: 4px; padding: 4px; border-radius: 22px; background: var(--cp-surface); }
.choice, .play-all { height: 36px; padding: 0 16px; border-radius: 999px; border: 1px solid transparent; font-size: 14px; font-weight: 700; color: var(--cp-on-surface); cursor: pointer; }
.choice[aria-pressed='true'] { background: var(--cp-primary); color: var(--cp-on-primary); }
.play-all { border-color: var(--cp-primary); background: var(--cp-primary-container); color: var(--cp-on-primary-container); align-self: flex-start; }
.choice:focus-visible, .play-all:focus-visible { outline: 3px solid var(--cp-primary); outline-offset: 2px; }
</style>
