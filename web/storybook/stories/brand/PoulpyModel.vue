<script setup lang="ts">
// Poulpy in 3D, to turn around (the Poulpy in 3D page): the shape of design/poulpy-3d/model.mjs in PoulpyViewer.vue. The switch
// picks a side of the plush; with `moods`, one of his six moods, with `poses` one of his twelve poses. Spin walks the viewer
// around him; grabbing the view stops it.
import { RotateCcw, RotateCw } from '@lucide/vue'
import { shallowRef, watch } from 'vue'
import type { PoulpyMood, PoulpySide } from '../../../../design/poulpy-3d/model.mjs'
import type { PoulpyPose } from '../../../../design/poulpy-3d/poses.mjs'
import PoulpyViewer, { type LivePoulpy, type PoulpyScene } from './PoulpyViewer.vue'

const { moods, poses } = defineProps<{ moods?: boolean; poses?: boolean }>()

const side = shallowRef<PoulpySide | PoulpyMood | PoulpyPose>(poses ? 'cocky' : moods ? 'happy' : 'blue')
// Seen from above, and named for him: it is the viewer who walks around, the other way.
const direction = shallowRef<'clockwise' | 'anticlockwise'>('anticlockwise')

const sides: { id: PoulpySide | PoulpyMood | PoulpyPose; label: string; suffix?: string }[] = poses
  ? [
      { id: 'cocky', label: 'Cocky' }, { id: 'straining', label: 'Straining' }, { id: 'champion', label: 'Champion' }, { id: 'standingTall', label: 'Standing tall' },
      { id: 'exhausted', label: 'Exhausted' }, { id: 'sideGlance', label: 'Side glance' }, { id: 'sleeping', label: 'Sleeping' }, { id: 'patchedUp', label: 'Patched up' },
      { id: 'shy', label: 'Shy' }, { id: 'thinking', label: 'Thinking' }, { id: 'pouting', label: 'Pouting' }, { id: 'fuming', label: 'Fuming' },
    ]
  : moods
  ? [{ id: 'proud', label: 'Proud' }, { id: 'happy', label: 'Happy' }, { id: 'recovering', label: 'Recovering' }, { id: 'annoyed', label: 'Annoyed' }, { id: 'grumpy', label: 'Grumpy' }, { id: 'fuming', label: 'Fuming' }]
  : [{ id: 'blue', label: 'Blue', suffix: 'side' }, { id: 'ember', label: 'Grumpy', suffix: 'side' }]
const directions = [
  { id: 'anticlockwise', label: 'Anticlockwise', icon: RotateCcw },
  { id: 'clockwise', label: 'Clockwise', icon: RotateCw },
] as const

let redraw: (() => void) | undefined
watch(side, () => redraw?.())

async function start(scene: PoulpyScene): Promise<LivePoulpy> {
  let outlined = false
  const show = () => scene.show(poses ? { pose: side.value as PoulpyPose } : (side.value as PoulpySide | PoulpyMood), outlined)
  redraw = show
  return {
    show(outline) { outlined = outline; show() },
    // A full turn of the viewer takes about 13 s at speed 1.
    advance: (seconds) => seconds * 28 * (direction.value === 'anticlockwise' ? 1 : -1),
    dispose() { redraw = undefined },
  }
}
const values = () => ({ page: poses ? 'poses' : moods ? 'moods' : 'sides', shown: side.value })
</script>

<template>
  <PoulpyViewer
    label="Poulpy in 3D."
    :framing="{ tilt: 17, distance: 9.6 }"
    move-label="Spin"
    stop-on-grab
    :speeds="{ max: 5, step: 0.1 }"
    :start="start"
    :values="values"
  >
    <template #pickers>
      <div class="sides" role="group" :aria-label="poses ? 'Pose' : moods ? 'Mood' : 'Side'">
        <button v-for="s in sides" :key="s.id" type="button" class="choice" :aria-pressed="side === s.id" :aria-label="s.suffix ? `${s.label} ${s.suffix}` : s.label" @click="side = s.id">{{ s.label }}<span v-if="s.suffix" class="wide">&nbsp;{{ s.suffix }}</span></button>
      </div>
      <div class="sides" role="group" aria-label="Direction, seen from above">
        <button v-for="d in directions" :key="d.id" type="button" class="choice icon" :aria-pressed="direction === d.id" :aria-label="d.label" :title="d.label" @click="direction = d.id"><component :is="d.icon" :size="18" aria-hidden="true" /></button>
      </div>
    </template>
  </PoulpyViewer>
</template>
