<script setup lang="ts">
// A workout's stats and the muscles it works: the left column of the workout page on a wide screen, the sheet on a phone.
// The stats and the body map are two blocks of their own, side by side in the column, never grouped in one card.
import { computed } from 'vue'
import { CpBodyMap, CpCard, CpStat, CpStats } from '@coach-poulpy/ui'
import type { Workout } from '../workouts'
import { bodyMapFigures } from '../bodyMap'
import { formatDay } from '../dates'
import { t } from '../i18n'

const props = defineProps<{ workout: Workout; card?: boolean }>()

const never = computed(() => t('app.statNever'))
const lastTime = computed(() => (props.workout.history.lastDoneAt ? formatDay(props.workout.history.lastDoneAt) : never.value))
const figures = computed(() => bodyMapFigures(props.workout.text.heat, { front: t('app.bodyFront'), back: t('app.bodyBack') }))
const description = computed(() => t('app.musclesAlt', { list: props.workout.text.muscles.map((muscle) => muscle.label).join(', ') }))
</script>

<template>
  <CpStats vertical class="w-full">
    <CpStat :title="t('app.statDone')" :value="workout.text.timesDone || never" />
    <CpStat :title="t('app.statLast')" :value="lastTime" />
    <CpStat :title="t('app.statUsual')" :value="workout.text.usual || never" />
  </CpStats>
  <CpCard v-if="card" :title="t('app.musclesTitle')">
    <CpBodyMap :figures="figures" :description="description" :less="t('app.heatLess')" :more="t('app.heatMore')" />
  </CpCard>
  <CpBodyMap v-else :figures="figures" :description="description" :less="t('app.heatLess')" :more="t('app.heatMore')" />
</template>
