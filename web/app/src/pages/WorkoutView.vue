<script setup lang="ts">
// One workout (design/mockups/agreed/web/app-workout.html): its stats and the muscles it works on the left, its parts with
// their steps on the right, read-only. On a phone the stats and muscles open in a sheet, kept in the URL as ?overview=1.
// The address is /workouts/<id>. The words come from the API, the same as the phone's.
import { computed, ref, watch } from 'vue'
import { List, Moon, Repeat, Sun, Zap } from '@lucide/vue'
import { CpBadge, CpBodyMap, CpBreadcrumbs, CpButton, CpCard, CpErrorNote, CpPoulpyCard, CpSheet, CpStat, CpStats } from '@coach-poulpy/ui'
import { getWorkout, type PartText, type StepText, type Workout } from '../workouts'
import { bodyMapFigures } from '../bodyMap'
import { formatDay } from '../dates'
import { navigate, search } from '../route'
import { t } from '../i18n'

const props = defineProps<{ id: string }>()

const workout = ref<Workout>()
const state = ref<'loading' | 'ready' | 'missing' | 'failed'>('loading')

let latest = 0
async function load() {
  const asked = ++latest
  state.value = 'loading'
  const answer = await getWorkout(props.id)
  if (asked !== latest) return
  if (answer === 'missing') state.value = 'missing'
  else if (!answer) state.value = 'failed'
  else {
    workout.value = answer
    state.value = 'ready'
  }
}
watch(() => props.id, load, { immediate: true })

const overview = computed({
  get: () => search.value.get('overview') === '1',
  set: (open: boolean) => navigate(open ? `/workouts/${encodeURIComponent(props.id)}?overview=1` : `/workouts/${encodeURIComponent(props.id)}`, open ? 'push' : 'replace'),
})
const figures = computed(() => bodyMapFigures(workout.value?.text.heat ?? {}, { front: t('app.bodyFront'), back: t('app.bodyBack') }))
const musclesAlt = computed(() => t('app.musclesAlt', { list: (workout.value?.text.muscles ?? []).map((muscle) => muscle.label).join(', ') }))
const partIcons = { WARM_UP: Sun, WORK: Zap, COOL_DOWN: Moon }
const formatIcon = computed(() => (workout.value?.workout.format === 'CIRCUIT' ? Repeat : List))
const lastTime = computed(() => (workout.value?.history.lastDoneAt ? formatDay(workout.value.history.lastDoneAt) : t('app.statNever')))
const never = computed(() => t('app.statNever'))

type Line = { key: string; repeat?: string; row?: StepText; number?: number }

/** The exercises numbered one after the other across the parts; a rest has no number. */
const numbered = computed(() => {
  let count = 0
  return (workout.value?.text.parts ?? []).map((part: PartText) => ({
    part,
    lines: part.steps.flatMap((step) => {
      const rows = step.row ? [step.row] : (step.repeat?.rows ?? [])
      const header: Line[] = step.repeat ? [{ key: `${step.key}-repeat`, repeat: step.repeat.times }] : []
      return [...header, ...rows.map((row): Line => ({ key: row.key, row, number: row.isRest ? undefined : ++count }))]
    }),
  }))
})
const line = (row: { prescription: string; roleLabel: string | null }) => [row.prescription, row.roleLabel].filter(Boolean).join(' · ')
const list = '/workouts'
</script>

<template>
  <main class="flex flex-col gap-6 p-4 md:p-8">
    <CpPoulpyCard v-if="state === 'missing'" :title="t('app.workoutMissing')">
      {{ t('app.workoutMissingHint') }}
      <template #actions><a :href="list" class="cp-btn cp-btn-primary" @click.prevent="navigate(list)">{{ t('app.backToWorkouts') }}</a></template>
    </CpPoulpyCard>
    <CpErrorNote v-else-if="state === 'failed'">{{ t('app.workoutsError') }}</CpErrorNote>
    <template v-else-if="state === 'ready' && workout">
      <CpBreadcrumbs :label="t('app.hereLabel')">
        <li><a :href="list" @click.prevent="navigate(list)">{{ t('app.workoutsTitle') }}</a></li>
        <li>{{ workout.workout.name }}</li>
      </CpBreadcrumbs>
      <div class="flex flex-col gap-2">
        <h1 class="cp-type-display">{{ workout.workout.name }}</h1>
        <p class="flex flex-wrap items-center gap-2 text-base-content/70">
          <CpBadge tone="primary" class="gap-1"><component :is="formatIcon" class="size-4 shrink-0" aria-hidden="true" />{{ workout.text.format }}</CpBadge>
          <span>{{ [workout.text.details, workout.text.equipment.join(', ')].filter(Boolean).join(' · ') }}</span>
        </p>
      </div>
      <a :href="`/workouts/${encodeURIComponent(id)}?overview=1`" class="cp-btn cp-btn-outline lg:hidden" aria-haspopup="dialog" @click.prevent="overview = true">{{ t('app.overviewButton') }}</a>
      <div class="grid items-start gap-6 lg:grid-cols-[22rem_minmax(0,1fr)]">
        <div class="hidden flex-col gap-6 lg:sticky lg:top-8 lg:flex" :aria-label="t('app.overviewLabel')">
          <CpStats vertical class="w-full">
            <CpStat :title="t('app.statDone')" :value="workout.text.timesDone || never" />
            <CpStat :title="t('app.statLast')" :value="lastTime" />
            <CpStat :title="t('app.statUsual')" :value="workout.text.usual || never" />
          </CpStats>
          <CpCard :title="t('app.musclesTitle')">
            <CpBodyMap :figures="figures" :description="musclesAlt" :less="t('app.heatLess')" :more="t('app.heatMore')" />
          </CpCard>
        </div>
        <div class="flex flex-col gap-6">
          <CpCard v-for="{ part, lines } in numbered" :key="part.key">
            <div class="flex items-center gap-3">
              <component :is="partIcons[part.kind]" class="size-6 shrink-0 text-primary" aria-hidden="true" />
              <div>
                <h2 class="cp-card-title">{{ part.name }}</h2>
                <p class="text-base-content/70">{{ [part.summary, part.time].filter(Boolean).join(' · ') }}</p>
              </div>
            </div>
            <div class="overflow-x-auto" data-component="table">
              <table class="cp-table">
                <tbody>
                  <template v-for="entry in lines" :key="entry.key">
                    <tr v-if="entry.repeat"><td colspan="3" class="text-base-content/70">{{ entry.repeat }}</td></tr>
                    <tr v-else-if="entry.row">
                      <td class="w-10 text-base-content/70">{{ entry.number }}</td>
                      <td><strong>{{ entry.row.name }}</strong></td>
                      <td class="text-base-content/70">{{ line(entry.row) }}</td>
                    </tr>
                  </template>
                </tbody>
              </table>
            </div>
          </CpCard>
        </div>
      </div>
      <CpCard :title="t('app.changeOnPhoneTitle')">
        <p class="text-base-content/70">{{ t('app.changeOnPhoneBody') }}</p>
      </CpCard>
      <CpSheet v-model:open="overview" :title="t('app.overviewButton')" :close-label="t('app.overviewClose')">
        <div class="flex flex-col gap-4 py-2">
          <CpStats vertical class="w-full">
            <CpStat :title="t('app.statDone')" :value="workout.text.timesDone || never" />
            <CpStat :title="t('app.statLast')" :value="lastTime" />
            <CpStat :title="t('app.statUsual')" :value="workout.text.usual || never" />
          </CpStats>
          <CpBodyMap :figures="figures" :description="musclesAlt" :less="t('app.heatLess')" :more="t('app.heatMore')" />
        </div>
      </CpSheet>
    </template>
  </main>
</template>
