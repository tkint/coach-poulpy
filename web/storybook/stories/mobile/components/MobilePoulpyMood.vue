<script setup lang="ts">
// Poulpy on Home as his moods change (draft, the moves validated on Brand/Poulpy moves): tapped again and again he loses patience.
//   happy     a tap makes him jump; the 5th tap in a row turns him round (one full turn) and he comes out annoyed
//   annoyed   a tap makes him shake; the 3rd in a row turns him round and he comes out fuming, his fumes puffing
//   fuming    a tap makes him shudder, the fumes puffing harder
// Left alone for 5 seconds he improves his mood, a step each time: fuming to annoyed, annoyed to happy, each by a full turn the other way round.
// The mockup plays the moves live in the workshop; the phones will play them baked from the model, frame by frame (decision 16).
// He is a still picture between moves. With reduced motion he does not move at all.
import { nextTick, onBeforeUnmount, ref } from 'vue'
import { poulpyDrawings } from '@coach-poulpy/ui'
import { MOOD_MOVES, tucking, type Move } from '../../brand/moodMoves'

defineProps<{ label: string }>()
type Mood = 'happy' | 'annoyed' | 'fuming'
const TAPS = { happy: 5, annoyed: 3 } // taps in a row that make him change mood
const CALM_MILLIS = 5000 // alone this long, he improves his mood by a step
const PAD_PER_HEIGHT = 110 / 210 // how much room a move has around the picture, in picture heights
const PICTURE_HEIGHT = 95
const PAD = PICTURE_HEIGHT * PAD_PER_HEIGHT

interface Plan { make: () => Move; seconds: number; swap: 'half' | 'start'; to: Mood; boost?: number }
// The phones' moves (design/poulpy-3d/moods.mjs), plus the happy jump, which on the phones is the jump already baked for the happy Poulpy.
const plans: Record<string, Plan> = {
  hop: { make: tucking(0.1, 0.1, 0.45, 6), seconds: 1.2, swap: 'start', to: 'happy' },
  ...(MOOD_MOVES as Record<string, Plan>),
}

const mood = ref<Mood>('happy')
const playing = ref(false)
const canvas = ref<HTMLCanvasElement>()
let taps = 0
let calm: ReturnType<typeof setTimeout> | undefined
let stop = () => {}

// His fuming picture is the pose with his fumes behind him, as the moves draw it.
const picture = (m: Mood) => (poulpyDrawings as Record<string, string>)[m === 'fuming' ? 'pose_fuming' : `mood_${m}`]
const reduced = () => window.matchMedia('(prefers-reduced-motion: reduce)').matches

// Plays one move from the mood he is in, and lands on the mood the move ends in.
async function perform(plan: Plan) {
  const from = mood.value
  playing.value = true
  await nextTick()
  if (!canvas.value) { playing.value = false; return }
  try {
    const { createPoulpyScene } = await import('../../brand/poulpy3dScene')
    const scene = createPoulpyScene(canvas.value)
    const showMood = (m: Mood) => {
      scene.show(m === 'fuming' ? { pose: 'fuming', effects: true } : m, false)
      // The same shot as his still picture, on a canvas that reaches PAD px further each way.
      scene.look({ turn: 0, tilt: 8, distance: 9.4, centre: 1.35, field: (2 * Math.atan(((PICTURE_HEIGHT + 2 * PAD) / PICTURE_HEIGHT) * Math.tan((14 * Math.PI) / 180)) * 180) / Math.PI })
    }
    showMood(from)
    const move = plan.make()
    let shown = from, last = performance.now(), elapsed = 0, frame = 0, clock = 0
    const swapAt = (pose: { yaw?: number }) => (plan.swap === 'start' ? elapsed > 0.05 : Math.abs(pose.yaw ?? 0) > Math.PI)
    const tick = (now: number) => {
      const dt = Math.min((now - last) / 1000, 0.05)
      last = now
      elapsed += dt
      clock += dt
      const pose = move.advance(dt) as { yaw?: number }
      scene.pose(pose as never)
      if (shown !== plan.to && swapAt(pose)) { shown = plan.to; showMood(shown); scene.pose(pose as never) }
      if (shown === 'fuming') scene.fumes(clock, plan.to === 'fuming' && plan.boost ? plan.boost * Math.max(0, Math.min(1, 1 - (elapsed - plan.seconds + 0.5) / 0.5)) : 0)
      if (elapsed < plan.seconds) { frame = requestAnimationFrame(tick); return }
      finish()
    }
    const finish = () => {
      stop(); stop = () => {}
      mood.value = plan.to
      playing.value = false
      wait()
    }
    stop = () => { cancelAnimationFrame(frame); scene.dispose() }
    frame = requestAnimationFrame(tick)
  } catch {
    playing.value = false // no 3D in this browser: he just stands there
  }
}

// Alone for a while he improves his mood by a step; in his normal mood the count of taps starts again.
function wait() {
  clearTimeout(calm)
  calm = setTimeout(async () => {
    if (playing.value) return
    taps = 0
    if (mood.value === 'fuming') await perform(plans.calmDown)
    else if (mood.value === 'annoyed') await perform(plans.backToNormal)
  }, CALM_MILLIS)
}

function tap() {
  if (playing.value || reduced()) return
  taps++
  if (mood.value === 'happy') {
    if (taps >= TAPS.happy) { taps = 0; perform(plans.toAnnoyed) } else perform(plans.hop)
  } else if (mood.value === 'annoyed') {
    if (taps >= TAPS.annoyed) { taps = 0; perform(plans.toFuming) } else perform(plans.shake)
  } else perform(plans.shudder)
}
onBeforeUnmount(() => { clearTimeout(calm); stop() })
</script>

<template>
  <button type="button" class="poulpy-cheer poulpy-cheer--home" :aria-label="`${label}. Tap and he reacts to his mood`" data-component="poulpy-mood" @click="tap">
    <img :src="picture(mood)" alt="" :style="{ visibility: playing ? 'hidden' : 'visible' }" />
    <span v-if="playing" class="poulpy-cheer-stage" aria-hidden="true" :style="{ inset: `-${PAD}px` }"><canvas ref="canvas" /></span>
  </button>
</template>
