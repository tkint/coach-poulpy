<script setup lang="ts">
// The catalog screen of the admin (design/mockups/agreed/web/admin-catalog*.html, #387): the exercises of the
// catalog draft in a paged table that is searched, filtered and sorted on the server, with rows picked for the
// actions on several exercises. Content (what changed since the latest version), translations and lifecycle are
// three separate columns and filters. The table's state lives in the URL (route.ts):
// /admin/catalog?q=squat&pattern=squat&muscles=glutes,quadriceps&translation=incomplete&sort=name&order=desc&page=2.
import { computed, onBeforeUnmount, ref, watch } from 'vue'
import { ChevronDown, ChevronUp, ChevronsUpDown, EllipsisVertical, SlidersHorizontal } from '@lucide/vue'
import { CpBadge, CpButton, CpCard, CpCheckbox, CpErrorNote, CpMultiSelectField, CpPagination, CpPoulpyCard, CpSearchField, CpSelectField, CpSheet, CpTable } from '@coach-poulpy/ui'
import { listCatalogExercises, type CatalogExercise, type CatalogQuery, type CatalogSort } from '../session'
import { domainLabel, domainOptions } from '../catalogText'
import { navigate, search, withParams } from '../route'
import { t } from '../i18n'

const PAGE_SIZE = 20
const MAX_PAGE_SIZE = 100
const BASE = '/admin/catalog'
const SORTS: CatalogSort[] = ['catalog', 'name', 'pattern', 'languages', 'change', 'status']
/** What each URL parameter is when it is absent; a default isn't written to the address. */
const DEFAULTS = { q: '', pattern: '', muscles: '', positions: '', equipment: '', translation: '', content: '', lifecycle: '', sort: 'catalog', order: 'asc', page: '1' }
type Param = keyof typeof DEFAULTS
/** The parameters that narrow the list: a change to one of them starts a new selection. */
const FILTERS: Param[] = ['q', 'pattern', 'muscles', 'positions', 'equipment', 'translation', 'content', 'lifecycle']

const param = (name: Param) => search.value.get(name) ?? DEFAULTS[name]
const list = (name: Param) => param(name).split(',').filter(Boolean)
const query = computed<CatalogQuery>(() => ({
  q: param('q'),
  pattern: param('pattern'),
  muscles: param('muscles'),
  positions: param('positions'),
  equipment: param('equipment'),
  translation: param('translation'),
  content: param('content'),
  lifecycle: param('lifecycle'),
  sort: SORTS.find((sort) => sort === param('sort')) ?? 'catalog',
  order: param('order') === 'desc' ? 'desc' : 'asc',
  page: Math.max(1, Number.parseInt(param('page'), 10) || 1),
  size: PAGE_SIZE,
}))

/** Moves to the table with [patch] applied to the address; `replace` for typing, which shouldn't fill the history. */
function go(patch: Partial<Record<Param, string>>, mode: 'push' | 'replace' = 'push') {
  const current = Object.fromEntries(Object.keys(DEFAULTS).map((name) => [name, search.value.get(name) ?? ''])) as Record<Param, string>
  navigate(withParams(BASE, { ...current, ...patch }, DEFAULTS), mode)
}

const exercises = ref<CatalogExercise[]>([])
const total = ref(0)
const state = ref<'loading' | 'ready' | 'failed'>('loading')
const filtering = ref(false)
const picked = ref(new Set<string>())
const menu = ref<HTMLDetailsElement>()

const pages = computed(() => Math.max(1, Math.ceil(total.value / PAGE_SIZE)))
const showing = computed(() =>
  t('admin.catalogShowing', {
    from: String((query.value.page - 1) * PAGE_SIZE + 1),
    to: String(Math.min(query.value.page * PAGE_SIZE, total.value)),
    total: String(total.value),
  }),
)
const page = computed({ get: () => query.value.page, set: (value: number) => go({ page: String(value) }) })

const columns: { sort: CatalogSort; label: string }[] = [
  { sort: 'name', label: 'admin.catalogColExercise' },
  { sort: 'pattern', label: 'admin.catalogColPattern' },
  { sort: 'languages', label: 'admin.catalogColLanguages' },
  { sort: 'change', label: 'admin.catalogColContent' },
  { sort: 'status', label: 'admin.catalogColLifecycle' },
]
const ariaSort = (sort: CatalogSort) => (query.value.sort !== sort ? 'none' : query.value.order === 'asc' ? 'ascending' : 'descending')

/** A new column sorts ascending first; the same column flips its direction. */
function sortBy(sort: CatalogSort) {
  go({ sort, order: query.value.sort === sort && query.value.order === 'asc' ? 'desc' : 'asc', page: '' })
}

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

// The filters. A select or a multi-select reads and writes its parameter; any change goes back to the first page.
const single = (name: 'pattern' | 'translation' | 'content' | 'lifecycle') =>
  computed({ get: () => param(name), set: (value: string) => go({ [name]: value, page: '' }) })
const multiple = (name: 'muscles' | 'positions' | 'equipment') =>
  computed({ get: () => list(name), set: (values: string[]) => go({ [name]: values.join(','), page: '' }) })
const pattern = single('pattern')
const translation = single('translation')
const content = single('content')
const lifecycle = single('lifecycle')
const muscles = multiple('muscles')
const positions = multiple('positions')
const equipment = multiple('equipment')

/** The quick views of "Show" are shortcuts for the content and translation filters; any other mix reads as all. */
const SHOWS: Record<string, Partial<Record<Param, string>>> = {
  all: { content: '', translation: '' },
  pending: { content: 'pending', translation: '' },
  draft: { content: 'draft', translation: '' },
  published: { content: 'published', translation: '' },
  incomplete: { content: '', translation: 'incomplete' },
}
const show = computed({
  get: () => Object.keys(SHOWS).find((name) => Object.entries(SHOWS[name]).every(([key, value]) => param(key as Param) === value)) ?? 'all',
  set: (name: string) => go({ ...SHOWS[name], page: '' }),
})

const options = (keys: Record<string, string>) => [{ value: '', text: t('admin.filterAll') }, ...Object.entries(keys).map(([value, label]) => ({ value, text: t(label) }))]
const showOptions = computed(() =>
  Object.entries({ all: 'showAll', pending: 'showPending', draft: 'showDrafts', published: 'showPublished', incomplete: 'showIncomplete' }).map(([value, label]) => ({ value, text: t(`admin.${label}`) })),
)
const patternOptions = computed(() => [{ value: '', text: t('admin.patternAll') }, ...domainOptions('pattern')])
const translationOptions = computed(() => options({ complete: 'admin.translationComplete', incomplete: 'admin.translationIncomplete' }))
const contentOptions = computed(() => options({ draft: 'admin.contentDraft', modified: 'admin.contentModified', published: 'admin.contentPublished', pending: 'admin.contentPending' }))
const lifecycleOptions = computed(() => options({ active: 'admin.catalogLifeActive', retiring: 'admin.catalogLifeRetiring', retired: 'admin.catalogLifeRetired' }))
const muscleOptions = computed(() => domainOptions('muscle'))
const positionOptions = computed(() => domainOptions('position'))
const equipmentOptions = computed(() => domainOptions('equipment'))

const textOf = (values: { value: string; text: string }[], value: string) => values.find((option) => option.value === value)?.text ?? value
/** The active filters as chips (the search isn't one: it has its own field). */
const chips = computed(() => {
  const found: string[] = []
  const add = (label: string, value: string) => found.push(t(label, { value }))
  if (pattern.value) add('admin.chipPattern', textOf(patternOptions.value, pattern.value))
  if (muscles.value.length) add('admin.chipMuscles', muscles.value.map((value) => textOf(muscleOptions.value, value)).join(', '))
  if (positions.value.length) add('admin.chipPositions', positions.value.map((value) => textOf(positionOptions.value, value)).join(', '))
  if (equipment.value.length) add('admin.chipEquipment', equipment.value.map((value) => textOf(equipmentOptions.value, value)).join(', '))
  if (translation.value) add('admin.chipTranslation', textOf(translationOptions.value, translation.value))
  if (content.value) add('admin.chipContent', textOf(contentOptions.value, content.value))
  if (lifecycle.value) add('admin.chipLifecycle', textOf(lifecycleOptions.value, lifecycle.value))
  return found
})
const activeFilters = computed(() => chips.value.length)
const clearFilters = () => go({ pattern: '', muscles: '', positions: '', equipment: '', translation: '', content: '', lifecycle: '', page: '' })

// Row labels. The title is the English name, the other line the French one and the slug.
const english = (row: CatalogExercise) => row.exercise.name.en ?? t('admin.catalogNoEnglish')
const french = (row: CatalogExercise) => row.exercise.name.fr ?? t('admin.catalogNoFrench')
const languageNames: Record<string, string> = { en: 'English', fr: 'Français' }
const missingText = (row: CatalogExercise) => t('admin.catalogLanguagesMissing', { languages: row.missing.map((code) => languageNames[code] ?? code).join(', ') })
const contentLabel = (change: CatalogExercise['change']) =>
  ({ added: 'admin.catalogContentNew', edited: 'admin.catalogContentEdited', offered: 'admin.catalogContentOfferedAgain' } as Record<string, string>)[change ?? '']
/** Retired in the draft but still offered by the latest version: the retirement waits for a publication. */
const lifecycleOf = (row: CatalogExercise) => (!row.exercise.retired ? 'active' : row.change === 'retired' ? 'retiring' : 'retired')
const lifecycleLabel = { active: 'admin.catalogLifeActive', retiring: 'admin.catalogLifeRetiring', retired: 'admin.catalogLifeRetired' }

// The selection holds slugs across pages. A new search or filter starts a new one, since a selection must never
// reach rows the person can no longer see; the page checkbox stands for this page, and "Select all" for the
// whole list the filters match, whose size the menu states.
const onPage = computed(() => exercises.value.map((row) => row.exercise.slug))
const allOnPage = computed(() => onPage.value.length > 0 && onPage.value.every((slug) => picked.value.has(slug)))
const someOnPage = computed(() => onPage.value.some((slug) => picked.value.has(slug)))
const selectedCount = computed(() => picked.value.size)
const selectedText = computed(() => (selectedCount.value === 1 ? t('admin.catalogSelectedOne') : t('admin.catalogSelectedMany', { count: String(selectedCount.value) })))
const pageBox = computed({
  get: () => allOnPage.value,
  set: (on: boolean) => {
    const next = new Set(picked.value)
    for (const slug of onPage.value) if (on) next.add(slug)
    else next.delete(slug)
    picked.value = next
  },
})
const rowBox = (slug: string) =>
  computed({
    get: () => picked.value.has(slug),
    set: (on: boolean) => {
      const next = new Set(picked.value)
      if (on) next.add(slug)
      else next.delete(slug)
      picked.value = next
    },
  })
const rowBoxes = computed(() => Object.fromEntries(onPage.value.map((slug) => [slug, rowBox(slug)])))
const clearSelection = () => {
  picked.value = new Set()
  if (menu.value) menu.value.open = false
}

/** Every slug the filters match, a page of the largest size at a time. */
async function selectAllMatching() {
  const found = new Set<string>()
  for (let at = 1; found.size < total.value; at++) {
    const result = await listCatalogExercises({ ...query.value, page: at, size: MAX_PAGE_SIZE })
    if (!result || result.exercises.length === 0) break
    for (const row of result.exercises) found.add(row.exercise.slug)
  }
  picked.value = found
  if (menu.value) menu.value.open = false
}

let latest = 0
async function load() {
  const asked = ++latest
  const found = await listCatalogExercises(query.value)
  if (asked !== latest) return // a newer question was asked meanwhile
  if (!found) {
    state.value = 'failed'
    return
  }
  // The last page of a list that shrank (a filter) is gone: go to the new last one.
  if (found.exercises.length === 0 && found.total > 0 && query.value.page > 1) {
    go({ page: String(Math.ceil(found.total / PAGE_SIZE)) }, 'replace')
    return
  }
  exercises.value = found.exercises
  total.value = found.total
  state.value = 'ready'
}

watch(() => JSON.stringify(query.value), load, { immediate: true })
watch(
  () => JSON.stringify(FILTERS.map(param)),
  () => (picked.value = new Set()),
)
</script>

<template>
  <main class="flex min-w-0 flex-col gap-6 p-4 md:p-8">
    <div class="flex flex-col gap-1">
      <h1 class="cp-type-display">{{ t('admin.catalogTitle') }}</h1>
      <p class="text-base-content/70">{{ t('admin.catalogIntro') }}</p>
    </div>
    <CpCard>
      <div class="flex flex-wrap items-center justify-between gap-4">
        <CpSearchField v-model="draft" class="w-64 max-w-full" :label="t('admin.catalogSearchLabel')" :clear-label="t('admin.catalogSearchClear')" :placeholder="t('admin.catalogSearch')" />
        <CpButton variant="outline" size="md" class="ms-auto gap-2" :aria-label="activeFilters ? `${t('admin.catalogFilters')}, ${t('admin.catalogFiltersActive', { count: String(activeFilters) })}` : undefined" @click="filtering = true">
          <SlidersHorizontal class="size-4 shrink-0" aria-hidden="true" />
          {{ t('admin.catalogFilters') }}
          <CpBadge v-if="activeFilters" tone="neutral">{{ activeFilters }}</CpBadge>
        </CpButton>
      </div>
      <div v-if="chips.length" class="flex flex-wrap items-center gap-2">
        <CpBadge v-for="chip in chips" :key="chip" tone="neutral">{{ chip }}</CpBadge>
        <button type="button" class="cp-link cp-link-hover text-sm" @click="clearFilters">{{ t('admin.filtersClear') }}</button>
      </div>
      <CpErrorNote v-if="state === 'failed'">{{ t('admin.catalogError') }}</CpErrorNote>
      <CpPoulpyCard v-else-if="state === 'ready' && total === 0" :title="t('admin.catalogNone')">{{ t('admin.catalogNoneHint') }}</CpPoulpyCard>
      <template v-else-if="state === 'ready'">
        <p v-if="selectedCount" class="text-sm text-base-content/70" role="status">{{ selectedText }}</p>
        <CpTable>
          <thead>
            <tr>
              <th class="w-12 text-center">
                <details v-if="selectedCount" ref="menu" class="relative inline-block">
                  <summary class="cp-btn cp-btn-ghost cp-btn-square cp-btn-sm relative list-none" :aria-label="t('admin.catalogSelectionActions')">
                    <EllipsisVertical class="size-4" aria-hidden="true" />
                    <span class="cp-badge cp-badge-primary cp-badge-sm absolute -right-2 -top-2 z-10">{{ selectedCount }}</span>
                  </summary>
                  <!-- CLEANUP: Publish and Retire join this menu with the batch publication and the retirement of #389 and #390. -->
                  <ul class="cp-menu absolute left-0 z-20 mt-2 w-60 rounded-box border border-base-300 bg-base-100 p-2 text-start shadow-lg">
                    <li v-if="selectedCount < total"><button type="button" @click="selectAllMatching">{{ t('admin.catalogSelectAllMatching', { count: String(total) }) }}</button></li>
                    <li><button type="button" @click="clearSelection">{{ t('admin.catalogClearSelection') }}</button></li>
                  </ul>
                </details>
                <CpCheckbox v-else v-model="pageBox" :label="t('admin.catalogSelectPage')" :indeterminate="someOnPage && !allOnPage" />
              </th>
              <th v-for="column in columns" :key="column.sort" :aria-sort="ariaSort(column.sort)">
                <button type="button" class="cp-btn cp-btn-ghost cp-btn-sm -ms-3 gap-1 whitespace-nowrap" @click="sortBy(column.sort)">
                  {{ t(column.label) }}
                  <ChevronsUpDown v-if="query.sort !== column.sort" class="size-4 shrink-0" aria-hidden="true" />
                  <ChevronUp v-else-if="query.order === 'asc'" class="size-4 shrink-0" aria-hidden="true" />
                  <ChevronDown v-else class="size-4 shrink-0" aria-hidden="true" />
                </button>
              </th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in exercises" :key="row.exercise.slug" class="cp-hover" :class="{ 'bg-base-200': picked.has(row.exercise.slug) }" :aria-selected="picked.has(row.exercise.slug) ? 'true' : undefined">
              <td class="text-center"><CpCheckbox v-model="rowBoxes[row.exercise.slug].value" :label="t('admin.catalogSelectOne', { name: english(row) })" /></td>
              <td>
                <div class="flex flex-col">
                  <!-- CLEANUP: the name becomes a link to the exercise's editor with the editor of #388. -->
                  <span class="text-start font-semibold">{{ english(row) }}</span>
                  <span class="text-sm text-base-content/70">{{ french(row) }} · {{ row.exercise.slug }}</span>
                </div>
              </td>
              <td class="whitespace-nowrap">{{ domainLabel('pattern', row.exercise.movementPattern) }}</td>
              <td>
                <CpBadge :tone="row.missing.length ? 'error' : 'success'">{{ row.missing.length ? missingText(row) : t('admin.catalogLanguagesComplete') }}</CpBadge>
              </td>
              <td>
                <CpBadge v-if="contentLabel(row.change)" :tone="row.change === 'added' ? 'primary' : row.change === 'offered' ? 'success' : 'neutral'">{{ t(contentLabel(row.change)) }}</CpBadge>
                <span v-else class="text-base-content/70">{{ t('admin.catalogContentNone') }}</span>
              </td>
              <td>
                <CpBadge :tone="lifecycleOf(row) === 'active' ? 'success' : lifecycleOf(row) === 'retiring' ? 'outline' : 'neutral'">{{ t(lifecycleLabel[lifecycleOf(row)]) }}</CpBadge>
              </td>
            </tr>
          </tbody>
        </CpTable>
        <div class="flex flex-col items-center justify-between gap-3 sm:flex-row">
          <p class="text-sm text-base-content/70">{{ showing }}</p>
          <CpPagination v-model:page="page" :pages="pages" :nav-label="t('admin.pages')" :previous-label="t('admin.previousPage')" :next-label="t('admin.nextPage')" />
        </div>
      </template>
    </CpCard>
    <CpSheet v-model:open="filtering" side :title="t('admin.filtersTitle')" :close-label="t('admin.filtersClose')">
      <form class="flex flex-col gap-4" role="search" :aria-label="t('admin.catalogFiltersLabel')" @submit.prevent>
        <p class="text-sm text-base-content/70">{{ t('admin.filtersIntro') }}</p>
        <CpSelectField v-model="show" :label="t('admin.filterShow')" :options="showOptions" />
        <CpSelectField v-model="pattern" :label="t('admin.filterPattern')" :options="patternOptions" />
        <CpSelectField v-model="translation" :label="t('admin.filterTranslation')" :options="translationOptions" />
        <CpSelectField v-model="content" :label="t('admin.filterContent')" :options="contentOptions" />
        <CpSelectField v-model="lifecycle" :label="t('admin.filterLifecycle')" :options="lifecycleOptions" />
        <CpMultiSelectField v-model="muscles" :label="t('admin.filterMuscles')" :placeholder="t('admin.filterMusclesPlaceholder')" :options="muscleOptions" :remove-label="(name) => t('admin.filterRemove', { name })" :none-label="t('admin.filterNoMatch')" />
        <CpMultiSelectField v-model="positions" :label="t('admin.filterPositions')" :placeholder="t('admin.filterPositionsPlaceholder')" :options="positionOptions" :remove-label="(name) => t('admin.filterRemove', { name })" :none-label="t('admin.filterNoMatch')" />
        <CpMultiSelectField v-model="equipment" :label="t('admin.filterEquipment')" :placeholder="t('admin.filterEquipmentPlaceholder')" :options="equipmentOptions" :remove-label="(name) => t('admin.filterRemove', { name })" :none-label="t('admin.filterNoMatch')" />
      </form>
      <template #actions>
        <button type="button" class="cp-link cp-link-hover me-auto" @click="clearFilters">{{ t('admin.filtersClear') }}</button>
        <CpButton size="md" @click="filtering = false">{{ t('admin.filtersDone') }}</CpButton>
      </template>
    </CpSheet>
  </main>
</template>
