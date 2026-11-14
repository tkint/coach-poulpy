<script setup lang="ts">
// The workouts of the account (design/mockups/agreed/web/app-workouts.html): a searchable, paged table of the workouts the
// phone synced, read-only. Its state lives in the URL (route.ts): /workouts?q=squat&format=sets&page=2. The words come from the API.
import { computed, onBeforeUnmount, ref, watch } from 'vue'
import { List, Repeat } from '@lucide/vue'
import { CpBadge, CpCard, CpChip, CpErrorNote, CpPagination, CpPoulpyCard, CpSearchField, CpTable } from '@coach-poulpy/ui'
import { listWorkouts, type WorkoutFormat, type WorkoutList, type WorkoutsQuery } from '../workouts'
import { navigate, search, withParams } from '../route'
import { formatDay } from '../dates'
import { t } from '../i18n'

const PAGE_SIZE = 25
const BASE = '/workouts'
/** What each URL parameter is when it is absent; a default isn't written to the address. */
const DEFAULTS = { q: '', format: '', page: '1' }
type Param = keyof typeof DEFAULTS
const FORMATS = ['circuit', 'sets'] as const

const param = (name: Param) => search.value.get(name) ?? DEFAULTS[name]
const query = computed<WorkoutsQuery>(() => ({
  q: param('q'),
  format: FORMATS.find((format) => format === param('format')) ?? '',
  page: Math.max(1, Number.parseInt(param('page'), 10) || 1),
  size: PAGE_SIZE,
}))

/** Moves to the table with [patch] applied to the address; `replace` for typing, which shouldn't fill the history. */
function go(patch: Partial<Record<Param, string>>, mode: 'push' | 'replace' = 'push') {
  const current = Object.fromEntries(Object.keys(DEFAULTS).map((name) => [name, search.value.get(name) ?? ''])) as Record<Param, string>
  navigate(withParams(BASE, { ...current, ...patch }, DEFAULTS), mode)
}

const found = ref<WorkoutList>()
const state = ref<'loading' | 'ready' | 'failed'>('loading')
const pages = computed(() => Math.max(1, Math.ceil((found.value?.total ?? 0) / PAGE_SIZE)))
const page = computed({ get: () => query.value.page, set: (value: number) => go({ page: String(value) }) })
const filtering = computed(() => query.value.q !== '' || query.value.format !== '')

const formats = computed(() => [
  { value: '', label: t('app.formatAll') },
  { value: 'circuit', label: t('app.formatCircuit') },
  { value: 'sets', label: t('app.formatSets') },
])
const icon = (format: WorkoutFormat) => (format === 'CIRCUIT' ? Repeat : List)
const lastDone = (at: string | null) => (at ? formatDay(at) : t('app.notDoneYet'))
const href = (id: string) => `/workouts/${encodeURIComponent(id)}`

// The search is typed in a draft and reaches the address after a pause, replacing the entry.
const draft = ref(param('q'))
let pause: ReturnType<typeof setTimeout> | undefined
watch(draft, (text) => {
  clearTimeout(pause)
  pause = setTimeout(() => {
    if (text !== param('q')) go({ q: text, page: '' }, 'replace')
  }, 300)
})
watch(
  () => param('q'),
  (text) => {
    if (text !== draft.value) draft.value = text
  },
)
onBeforeUnmount(() => clearTimeout(pause))

let latest = 0
async function load() {
  const asked = ++latest
  const answer = await listWorkouts(query.value)
  if (asked !== latest) return // a newer question was asked meanwhile
  if (!answer) {
    state.value = 'failed'
    return
  }
  // A page past the end, from an old link or a list that shrank: go to the last one.
  if (answer.items.length === 0 && answer.total > 0 && query.value.page > 1) {
    go({ page: String(Math.ceil(answer.total / PAGE_SIZE)) }, 'replace')
    return
  }
  found.value = answer
  state.value = 'ready'
}
watch(() => JSON.stringify(query.value), load, { immediate: true })
</script>

<template>
  <main class="flex flex-col gap-6 p-4 md:p-8">
    <div class="flex flex-col gap-1">
      <h1 class="cp-type-display">{{ t('app.workoutsTitle') }}</h1>
      <p class="text-base-content/70">{{ t('app.workoutsIntro') }}</p>
    </div>
    <form class="flex flex-col gap-3 md:flex-row md:items-center" role="search" @submit.prevent>
      <CpSearchField v-model="draft" class="w-full md:max-w-sm" :label="t('app.workoutsSearch')" :clear-label="t('app.workoutsClearSearch')" :placeholder="t('app.workoutsSearchPlaceholder')" />
      <div class="flex gap-2" role="group" :aria-label="t('app.workoutsFormat')">
        <CpChip v-for="format in formats" :key="format.value" :model-value="query.format === format.value" @update:model-value="go({ format: format.value, page: '' })">{{ format.label }}</CpChip>
      </div>
    </form>
    <CpErrorNote v-if="state === 'failed'">{{ t('app.workoutsError') }}</CpErrorNote>
    <CpPoulpyCard v-else-if="state === 'ready' && found?.total === 0" :title="filtering ? t('app.workoutsNoMatch') : t('app.workoutsNone')">
      {{ filtering ? t('app.workoutsNoMatchHint') : t('app.workoutsNoneHint') }}
    </CpPoulpyCard>
    <CpCard v-else-if="state === 'ready' && found">
      <CpTable>
        <thead>
          <tr>
            <th>{{ t('app.colName') }}</th>
            <th>{{ t('app.colFormat') }}</th>
            <th>{{ t('app.colContent') }}</th>
            <th>{{ t('app.colLength') }}</th>
            <th>{{ t('app.colLastDone') }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="workout in found.items" :key="workout.id" class="cp-hover">
            <td><a :href="href(workout.id)" class="cp-link cp-link-hover font-bold" @click.prevent="navigate(href(workout.id))">{{ workout.name }}</a></td>
            <td>
              <CpBadge tone="primary" class="gap-1 whitespace-nowrap"><component :is="icon(workout.format)" class="size-4 shrink-0" aria-hidden="true" />{{ workout.text.format }}</CpBadge>
            </td>
            <td class="whitespace-nowrap">{{ workout.text.exercises }}</td>
            <td class="whitespace-nowrap">{{ workout.text.length }}</td>
            <td class="whitespace-nowrap text-base-content/70">{{ lastDone(workout.lastDoneAt) }}</td>
          </tr>
        </tbody>
      </CpTable>
      <div class="flex flex-col items-center justify-between gap-3 sm:flex-row">
        <p class="text-base-content/70" aria-live="polite">{{ found.text.count }}</p>
        <CpPagination v-if="pages > 1" v-model:page="page" :pages="pages" :nav-label="t('app.workoutsPages')" :previous-label="t('app.previousPage')" :next-label="t('app.nextPage')" />
      </div>
    </CpCard>
  </main>
</template>
