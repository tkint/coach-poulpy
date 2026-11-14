<script setup lang="ts">
// The exercise screen of the admin (the stories Web/Screens/Admin/Exercise, web/storybook/stories/web/AdminExercise.stories.ts, #388): one exercise of the
// catalog draft, edited at /admin/catalog/<slug>, or added at /admin/catalog/new. Saving writes the draft only: the
// published exercise, which the apps download, stays as it is until a publication. A text is written in one language
// at a time (`?lang=fr`), and under each translatable field a badge says whether every shipped language has it, so
// the screen shows at once what a publication would refuse. Saving never needs every language; publishing does.
import { computed, nextTick, onMounted, reactive, ref, watch } from 'vue'
import { GripVertical, Lock, Plus, X } from '@lucide/vue'
import { CpBadge, CpBodyMap, CpBreadcrumbs, CpButton, CpCard, CpChip, CpChipSelectField, CpErrorNote, CpPoulpyCard, CpSegmentedControl, CpSelectField, CpSuccessNote, CpTextarea, CpTextField } from '@coach-poulpy/ui'
import { getCatalogExercise, saveCatalogExercise, type CatalogExercise, type ExerciseJson } from '../session'
import { domainLabel, domainOptions } from '../catalogText'
import { groupAreas, groupOfArea } from '../bodyMap'
import { navigate, search, withParams } from '../route'
import { locale, shippedLanguages, t } from '../i18n'

const props = defineProps<{ slug?: string }>()

type Texts = Record<string, string>
type Form = {
  slug: string
  name: Texts
  /** Each step keeps its key while it moves, so the field and the focus follow it. */
  steps: { key: number; text: Texts }[]
  note: Texts
  /** Per language, the aliases separated by commas. */
  aliases: Texts
  needs: string[][]
  optional: string[]
  pattern: string
  measurement: string
  positions: string[]
  muscles: string[]
  focus: string[]
  perSide: boolean
  jumps: boolean
}

const blank = (): Texts => Object.fromEntries(shippedLanguages.map((language) => [language, '']))
/** Texts in every shipped language, empty where the exercise has none. */
const texts = (from?: Record<string, string>): Texts => ({ ...blank(), ...from })
// The catalog's JSON names enums in capitals; the forms and the address use their lowercase words.
const lower = (codes?: string[]) => (codes ?? []).map((code) => code.toLowerCase())
const upper = (codes: string[]) => codes.map((code) => code.toUpperCase())

let stepKeys = 0
const newStep = (from?: Record<string, string>) => ({ key: stepKeys++, text: texts(from) })

const fromExercise = (exercise?: ExerciseJson): Form => ({
  slug: exercise?.slug ?? '',
  name: texts(exercise?.name),
  steps: (exercise?.instructions ?? []).map(newStep),
  note: texts(exercise?.variationNote),
  aliases: Object.fromEntries(shippedLanguages.map((language) => [language, (exercise?.aliases?.[language] ?? []).join(', ')])),
  // The screen always shows one need, empty when the exercise needs nothing.
  needs: exercise?.equipment?.length ? exercise.equipment.map(lower) : [[]],
  optional: lower(exercise?.optionalEquipment),
  pattern: exercise?.movementPattern.toLowerCase() ?? 'squat',
  measurement: exercise?.measurement.toLowerCase() ?? 'reps',
  positions: lower(exercise?.positions),
  muscles: lower(exercise?.muscleGroups),
  focus: lower(exercise?.focus),
  perSide: exercise?.perSide ?? false,
  jumps: exercise?.jumps ?? false,
})

const written = (from: Texts): Texts => Object.fromEntries(Object.entries(from).map(([language, text]) => [language, text.trim()]).filter(([, text]) => text))

/** The exercise to save: what the person edited over what the server sent, so pictures and the like travel unchanged. */
function toExercise(form: Form, base?: ExerciseJson): ExerciseJson {
  const note = written(form.note)
  const aliases = Object.fromEntries(
    Object.entries(form.aliases)
      .map(([language, text]) => [language, text.split(',').map((alias) => alias.trim()).filter(Boolean)] as const)
      .filter(([, list]) => list.length),
  )
  const exercise: ExerciseJson = {
    ...base,
    slug: form.slug.trim(),
    name: written(form.name),
    aliases,
    movementPattern: form.pattern.toUpperCase(),
    muscleGroups: upper(form.muscles),
    equipment: form.needs.filter((need) => need.length).map(upper),
    optionalEquipment: upper(form.optional),
    measurement: form.measurement.toUpperCase(),
    perSide: form.perSide,
    focus: upper(form.focus),
    positions: upper(form.positions),
    jumps: form.jumps,
    instructions: form.steps.map((step) => written(step.text)).filter((step) => Object.keys(step).length),
  }
  if (Object.keys(note).length) exercise.variationNote = note
  else delete exercise.variationNote
  return exercise
}

const state = ref<'loading' | 'ready' | 'missing' | 'failed'>('loading')
const loaded = ref<CatalogExercise>()
const form = reactive<Form>(fromExercise())
const saving = ref(false)
const saved = ref(false)
const failure = ref<'failed' | 'invalid' | 'slug-taken'>()
const reason = ref('')
const isNew = computed(() => props.slug === undefined)
/** The slug is locked from the first publication: workouts point at it. */
const locked = computed(() => loaded.value?.published === true)

const language = computed(() => {
  const asked = search.value.get('lang') ?? ''
  return shippedLanguages.includes(asked) ? asked : shippedLanguages[0]
})
const base = computed(() => (props.slug ? `/admin/catalog/${encodeURIComponent(props.slug)}` : '/admin/catalog/new'))
const showLanguage = (code: string) => navigate(withParams(base.value, { lang: code }, { lang: shippedLanguages[0] }), 'replace')
const names = new Intl.DisplayNames([locale], { type: 'language' })
const languageName = (code: string) => names.of(code) ?? code
const original = shippedLanguages[0]
/** The language of the text on show is not the original one: each translatable field then shows the original below it. */
const translating = computed(() => language.value !== original)

function apply(found: CatalogExercise | undefined) {
  loaded.value = found
  Object.assign(form, fromExercise(found?.exercise))
}

async function load() {
  saved.value = false
  failure.value = undefined
  slugTouched.value = !isNew.value
  if (props.slug === undefined) {
    apply(undefined)
    state.value = 'ready'
    return
  }
  state.value = 'loading'
  const found = await getCatalogExercise(props.slug)
  if (!found) {
    // A slug that is no exercise and a server that can't be reached read differently.
    state.value = (await getCatalogExercise(props.slug)) === undefined && !navigator.onLine ? 'failed' : 'missing'
    return
  }
  apply(found)
  state.value = 'ready'
}
onMounted(load)
watch(() => props.slug, load)

// A new exercise's slug follows its English name until the slug is typed.
const slugTouched = ref(false)
const slugOf = (name: string) =>
  name
    .normalize('NFD')
    .replace(/[̀-ͯ]/g, '')
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/^-|-$/g, '')
watch(
  () => form.name[shippedLanguages[0]],
  (name) => {
    if (isNew.value && !slugTouched.value) form.slug = slugOf(name)
  },
)

/** The shipped languages [from] has no text in. */
const missingIn = (from: Texts) => shippedLanguages.filter((code) => !from[code]?.trim())
type Status = { tone: 'success' | 'error' | 'neutral'; text: string }
/**
 * What the badges under a translatable field say while the original is shown: where each other language stands. The name says it
 * for all of them at once; a text that is optional and empty everywhere (the variation note) says nothing, and aliases, which
 * a language can do without, say when one isn't needed.
 */
function statuses(from: Texts, kind: 'name' | 'step' | 'note' | 'aliases'): Status[] {
  const others = shippedLanguages.filter((code) => code !== original)
  const missing = missingIn(from)
  if (kind === 'note' && missing.length === shippedLanguages.length) return []
  if (kind === 'name' && !missing.length) return [{ tone: 'success', text: t('admin.exerciseTranslated') }]
  return others.map((code) => {
    const language = languageName(code)
    if (from[code]?.trim()) return { tone: 'success', text: t('admin.translationDone', { language }) }
    return kind === 'aliases' ? { tone: 'neutral', text: t('admin.translationNotNeeded', { language }) } : { tone: 'error', text: t('admin.translationMissing', { language }) }
  })
}
/** The language switch marks a language some text still lacks. */
const lacks = (code: string) => [form.name, ...form.steps.map((step) => step.text), ...(Object.keys(written(form.note)).length ? [form.note] : [])].some((from) => !from[code]?.trim())
const languages = computed(() =>
  shippedLanguages.map((code) => ({ value: code, text: code.toUpperCase(), flagged: lacks(code), flagLabel: t('admin.languageNeedsText', { language: languageName(code) }) })),
)
const shown = computed({ get: () => language.value, set: (code: string) => showLanguage(code) })
/** What a field of the language on show asks for, and what it shows of the original under it. */
const originalOf = (from: Texts) => t('admin.englishOriginal', { text: from[original]?.trim() || t('admin.englishNothing') })
const inLanguage = (label: string) => t(label, { language: languageName(language.value) })

const patternOptions = domainOptions('pattern')
const measurementOptions = domainOptions('measurement')
const positionOptions = domainOptions('position')
const equipmentOptions = domainOptions('equipment')
const focusOptions = domainOptions('focus')

const removeLabel = (text: string) => t('admin.chipRemove', { name: text })

const addStep = () => form.steps.push(newStep())
const removeStep = (at: number) => form.steps.splice(at, 1)
// Steps move by dragging their handle or, from the keyboard, with the arrow keys on it.
const dragged = ref<number>()
function moveStep(from: number | undefined, to: number) {
  if (from === undefined || to < 0 || to >= form.steps.length || from === to) return
  const [step] = form.steps.splice(from, 1)
  form.steps.splice(to, 0, step)
  void nextTick(() => document.getElementById(`step-grip-${step.key}`)?.focus())
}

// The muscles: tapping an area of the figure adds or removes its muscle group; full body is its own chip, since no area is it.
const muscleLabel = (code: string) => domainLabel('muscle', code)
const lit = computed(() => groupAreas(form.muscles))
const captions = computed(() => ({ front: t('app.bodyFront'), back: t('app.bodyBack') }))
const areaLabel = (area: string) => {
  const group = groupOfArea(area)
  return group ? muscleLabel(group) : undefined
}
const musclesList = computed(() => form.muscles.map(muscleLabel).join(', '))
const musclesSaid = computed(() => (form.muscles.length ? t('admin.musclesWorked', { list: musclesList.value }) : t('admin.musclesNone')))
function pickArea(area: string) {
  const group = groupOfArea(area)
  if (!group || form.muscles.includes('full_body')) return
  form.muscles = form.muscles.includes(group) ? form.muscles.filter((one) => one !== group) : [...form.muscles, group]
}
const fullBody = computed({
  get: () => form.muscles.includes('full_body'),
  set: (on: boolean) => (form.muscles = on ? ['full_body'] : []),
})

async function save() {
  saving.value = true
  saved.value = false
  failure.value = undefined
  const answer = await saveCatalogExercise(props.slug, toExercise(form, loaded.value?.exercise))
  saving.value = false
  if ('saved' in answer) {
    saved.value = true
    apply(answer.saved)
    // A new exercise now has its own address.
    if (isNew.value) navigate(withParams(`/admin/catalog/${encodeURIComponent(answer.saved.exercise.slug)}`, { lang: language.value }, { lang: shippedLanguages[0] }), 'replace')
    return
  }
  reason.value = answer.reason ?? ''
  failure.value = answer.error === 'slug-taken' ? 'slug-taken' : answer.error === 'invalid-exercise' ? 'invalid' : 'failed'
}

// Typing again clears what the last save said.
watch(form, () => (saved.value = false), { deep: true })
</script>

<template>
  <main class="flex min-w-0 flex-col gap-6 p-4 md:p-8">
    <CpPoulpyCard v-if="state === 'missing'" :title="t('admin.exerciseMissing')">
      {{ t('admin.exerciseMissingHint') }}
      <a href="/admin/catalog" class="cp-link cp-link-hover" @click.prevent="navigate('/admin/catalog')">{{ t('admin.exerciseBack') }}</a>
    </CpPoulpyCard>
    <CpErrorNote v-else-if="state === 'failed'">{{ t('admin.exerciseLoadFailed') }}</CpErrorNote>
    <p v-else-if="state === 'loading'" role="status">{{ t('admin.checking') }}</p>
    <template v-else>
      <div class="flex flex-col gap-4 bg-base-200 py-2" data-component="form-header">
        <CpBreadcrumbs :label="t('admin.exerciseBreadcrumbs')">
          <li><a href="/admin/catalog" @click.prevent="navigate('/admin/catalog')">{{ t('admin.catalogTitle') }}</a></li>
          <li>{{ isNew ? t('admin.exerciseNew') : form.name[original] }}</li>
        </CpBreadcrumbs>
        <div class="flex flex-wrap items-center justify-between gap-4">
          <div class="flex flex-col gap-1">
            <h1 class="cp-type-display">{{ isNew ? t('admin.exerciseNew') : t('admin.exerciseEdit') }}</h1>
            <p v-if="isNew" class="flex flex-wrap items-center gap-2">
              <CpBadge>{{ t('admin.catalogContentNew') }}</CpBadge>
              <span class="text-sm text-base-content/70">{{ t('admin.exerciseNotPublished') }}</span>
            </p>
            <p v-else-if="loaded" class="flex flex-wrap items-center gap-2">
              <CpBadge :tone="loaded.exercise.retired ? 'neutral' : 'success'">{{ loaded.exercise.retired ? t(loaded.change === 'retired' ? 'admin.catalogLifeRetiring' : 'admin.catalogLifeRetired') : t('admin.exerciseOffered') }}</CpBadge>
              <CpBadge v-if="loaded.change === 'added'">{{ t('admin.catalogContentNew') }}</CpBadge>
              <CpBadge v-else-if="loaded.change === 'edited'" tone="neutral">{{ t('admin.catalogContentEdited') }}</CpBadge>
              <CpBadge v-else-if="loaded.change === 'offered'" tone="success">{{ t('admin.catalogContentOfferedAgain') }}</CpBadge>
            </p>
          </div>
          <div class="flex flex-wrap items-center gap-2">
            <CpSegmentedControl v-model="shown" :label="t('admin.exerciseLanguage')" :options="languages" />
            <CpButton variant="ghost" size="md" @click="navigate('/admin/catalog')">{{ t('admin.exerciseCancel') }}</CpButton>
            <CpButton type="submit" form="exercise-form" size="md" :disabled="saving">{{ isNew ? t('admin.exerciseAdd') : t('admin.exerciseSave') }}</CpButton>
          </div>
        </div>
        <!-- Always there, hidden while the original shows, so switching language never moves the page. -->
        <p class="text-sm text-base-content/70" :class="{ invisible: !translating }" :aria-hidden="!translating">{{ t('admin.exerciseEditingLanguage', { language: languageName(translating ? language : shippedLanguages[1] ?? language) }) }}</p>
      </div>
      <CpSuccessNote v-if="saved">{{ t('admin.exerciseSaved') }}</CpSuccessNote>
      <CpErrorNote v-if="failure === 'failed'">{{ t('admin.exerciseSaveFailed') }}</CpErrorNote>
      <CpErrorNote v-if="failure === 'invalid'">{{ t('admin.exerciseInvalid', { reason }) }}</CpErrorNote>
      <CpErrorNote v-if="failure === 'slug-taken'">{{ t('admin.exerciseSlugTakenNote') }}</CpErrorNote>
      <form id="exercise-form" class="grid items-start gap-6 lg:flex" @submit.prevent="save">
        <div class="contents lg:flex lg:min-w-0 lg:flex-1 lg:flex-col lg:gap-6" data-component="main-column">
          <CpCard :title="t('admin.exerciseInfo')" class="order-1 lg:order-none">
            <div class="grid items-start gap-4 sm:grid-cols-2">
              <CpTextField
                v-model="form.name[language]"
                :label="translating ? inLanguage('admin.fieldNameIn') : t('admin.fieldName')"
                :placeholder="translating ? t('admin.neededBeforePublishing') : undefined"
                :hint="translating ? originalOf(form.name) : undefined"
                :invalid="translating && !form.name[language]?.trim()"
                autocomplete="off"
              >
                <template v-if="!translating" #status>
                  <p class="cp-label flex flex-wrap gap-1 whitespace-normal"><CpBadge v-for="one in statuses(form.name, 'name')" :key="one.text" :tone="one.tone" small>{{ one.text }}</CpBadge></p>
                </template>
              </CpTextField>
              <CpTextField
                v-model="form.slug"
                :label="t('admin.fieldSlug')"
                autocomplete="off"
                :disabled="locked"
                :error="failure === 'slug-taken' ? t('admin.exerciseSlugTaken') : undefined"
                :hint="locked ? t('admin.slugLocked') : t('admin.slugHint')"
                @input="slugTouched = true"
              >
                <template v-if="locked" #icon><Lock class="size-5 shrink-0" aria-hidden="true" /></template>
              </CpTextField>
            </div>
            <div class="cp-divider my-0" role="separator"></div>
            <div class="flex flex-col gap-1">
              <h2 class="cp-card-title">{{ t('admin.exerciseSteps') }}</h2>
              <p class="text-sm text-base-content/70">{{ t('admin.stepsHint') }}</p>
            </div>
            <ol class="flex flex-col gap-3">
              <template v-for="(step, at) in form.steps" :key="step.key">
                <div v-if="at" class="cp-divider my-0" role="separator"></div>
                <li class="flex items-start gap-3" data-component="step" @dragover.prevent @drop.prevent="moveStep(dragged, at)">
                  <span class="cp-badge cp-badge-primary cp-badge-soft shrink-0">{{ at + 1 }}</span>
                  <div class="min-w-0 grow">
                    <CpTextarea
                      v-model="step.text[language]"
                      :label="translating ? t('admin.stepIn', { n: String(at + 1), language: languageName(language) }) : t('admin.stepN', { n: String(at + 1) })"
                      :placeholder="translating ? t('admin.neededBeforePublishing') : undefined"
                      :hint="translating ? originalOf(step.text) : undefined"
                    >
                      <template v-if="!translating" #status>
                        <p class="cp-label flex flex-wrap gap-1 whitespace-normal"><CpBadge v-for="one in statuses(step.text, 'step')" :key="one.text" :tone="one.tone" small>{{ one.text }}</CpBadge></p>
                      </template>
                    </CpTextarea>
                  </div>
                  <div class="flex shrink-0 flex-col">
                    <button
                      :id="`step-grip-${step.key}`"
                      type="button"
                      class="cp-btn cp-btn-circle cp-btn-ghost cp-btn-sm cursor-grab"
                      :aria-label="t('admin.stepMove', { n: String(at + 1) })"
                      :disabled="form.steps.length < 2"
                      draggable="true"
                      @dragstart="dragged = at"
                      @dragend="dragged = undefined"
                      @keydown.up.prevent="moveStep(at, at - 1)"
                      @keydown.down.prevent="moveStep(at, at + 1)"
                    >
                      <GripVertical class="size-4 shrink-0" aria-hidden="true" />
                    </button>
                    <button type="button" class="cp-btn cp-btn-circle cp-btn-ghost cp-btn-sm" :aria-label="t('admin.stepRemove', { n: String(at + 1) })" @click="removeStep(at)">
                      <X class="size-4 shrink-0" aria-hidden="true" />
                    </button>
                  </div>
                </li>
              </template>
            </ol>
            <div><CpButton variant="outline" size="md" class="cp-btn-sm gap-2" @click="addStep"><Plus class="size-4 shrink-0" aria-hidden="true" />{{ t('admin.stepAdd') }}</CpButton></div>
          </CpCard>
          <CpCard class="order-5 lg:order-none">
            <div class="grid gap-6 md:grid-cols-2">
              <div class="flex min-w-0 flex-col gap-4">
                <h2 class="cp-card-title">{{ t('admin.exerciseEquipment') }}</h2>
                <h3 class="cp-type-label">{{ t('admin.equipmentRequired') }}</h3>
                <CpChipSelectField
                  v-for="(_, at) in form.needs"
                  :key="at"
                  v-model="form.needs[at]"
                  :aria-label="t('admin.equipmentRequiredAria')"
                  :hint="t('admin.equipmentRequiredHint')"
                  :placeholder="t('admin.equipmentPlaceholder')"
                  :options="equipmentOptions"
                  :none-label="t('admin.filterNoMatch')"
                  :remove-label="removeLabel"
                />
                <h3 class="cp-type-label">{{ t('admin.equipmentOptional') }}</h3>
                <CpChipSelectField
                  v-model="form.optional"
                  :aria-label="t('admin.equipmentOptionalAria')"
                  :hint="t('admin.equipmentOptionalHint')"
                  :placeholder="t('admin.equipmentPlaceholder')"
                  :options="equipmentOptions"
                  :none-label="t('admin.filterNoMatch')"
                  :remove-label="removeLabel"
                />
              </div>
              <div class="flex min-w-0 flex-col gap-4">
                <h2 class="cp-card-title">{{ t('admin.exerciseClassification') }}</h2>
                <div class="grid gap-3 sm:grid-cols-2">
                  <CpSelectField v-model="form.pattern" :label="t('admin.filterPattern')" :options="patternOptions" />
                  <CpSelectField v-model="form.measurement" :label="t('admin.fieldMeasurement')" :options="measurementOptions" />
                </div>
                <CpChipSelectField
                  v-model="form.positions"
                  :label="t('admin.fieldPosition')"
                  :options="positionOptions"
                  :none-label="t('admin.filterNoMatch')"
                  :remove-label="removeLabel"
                />
              </div>
            </div>
          </CpCard>
        </div>
        <div class="contents lg:flex lg:w-full lg:max-w-md lg:flex-col lg:gap-6" data-component="side-column">
          <CpCard :title="t('admin.exerciseVariations')" class="order-3 lg:order-none">
            <CpTextField
              v-model="form.note[language]"
              :label="t('admin.variationNote')"
              :hint="translating ? originalOf(form.note) : undefined"
              autocomplete="off"
            >
              <template v-if="!translating" #status>
                <p class="cp-label flex flex-wrap gap-1 whitespace-normal"><CpBadge v-for="one in statuses(form.note, 'note')" :key="one.text" :tone="one.tone" small>{{ one.text }}</CpBadge></p>
              </template>
            </CpTextField>
          </CpCard>
          <CpCard :title="t('admin.exerciseMuscles')" :description="t('admin.musclesHint')" class="order-4 lg:order-none">
            <CpBodyMap :heat="lit" :captions="captions" :description="musclesSaid" :area-label="areaLabel" interactive @pick="pickArea" />
            <div class="flex flex-wrap items-center justify-center gap-3">
              <CpChip v-model="fullBody" compact>{{ muscleLabel('full_body') }}</CpChip>
            </div>
            <p class="text-sm text-base-content/70">{{ musclesSaid }}</p>
            <h3 class="cp-card-title">{{ t('admin.exerciseAdditional') }}</h3>
            <div class="flex flex-col gap-3 py-2">
              <CpChipSelectField v-model="form.focus" :label="t('admin.fieldFocus')" :options="focusOptions" :none-label="t('admin.filterNoMatch')" :remove-label="removeLabel" />
              <div class="flex flex-wrap gap-2">
                <CpChip v-model="form.perSide" compact>{{ t('admin.attrEachSide') }}</CpChip>
                <CpChip v-model="form.jumps" compact>{{ t('admin.attrJumps') }}</CpChip>
              </div>
              <CpTextField
                v-model="form.aliases[language]"
                :label="translating ? inLanguage('admin.fieldAliasesIn') : t('admin.fieldAliases')"
                :hint="translating ? originalOf(form.aliases) : t('admin.aliasesHint')"
                autocomplete="off"
              >
                <template v-if="!translating" #status>
                  <p class="cp-label flex flex-wrap gap-1 whitespace-normal"><CpBadge v-for="one in statuses(form.aliases, 'aliases')" :key="one.text" :tone="one.tone" small>{{ one.text }}</CpBadge></p>
                </template>
              </CpTextField>
            </div>
          </CpCard>
        </div>
      </form>
      <div class="flex flex-wrap justify-end gap-2 lg:hidden" data-component="form-actions">
        <CpButton variant="ghost" size="md" @click="navigate('/admin/catalog')">{{ t('admin.exerciseCancel') }}</CpButton>
        <CpButton type="submit" form="exercise-form" size="md" :disabled="saving">{{ isNew ? t('admin.exerciseAdd') : t('admin.exerciseSave') }}</CpButton>
      </div>
    </template>
  </main>
</template>
