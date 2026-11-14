<script setup lang="ts">
// The exercise screen of the admin (design/mockups/agreed/web/admin-catalog-exercise*.html, #388): one exercise of the
// catalog draft, edited at /admin/catalog/<slug>, or added at /admin/catalog/new. Saving writes the draft only: the
// published exercise, which the apps download, stays as it is until a publication. A text is written in one language
// at a time (`?lang=fr`), and under each translatable field a badge says whether every shipped language has it, so
// the screen shows at once what a publication would refuse. Saving never needs every language; publishing does.
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { CircleExclamationPoint, Plus, X } from '@lucide/vue'
import { CpBadge, CpBreadcrumbs, CpButton, CpCard, CpChip, CpErrorNote, CpMultiSelectField, CpPoulpyCard, CpSelectField, CpSuccessNote, CpTextarea, CpTextField } from '@coach-poulpy/ui'
import { changedFields, getCatalogExercise, saveCatalogExercise, type CatalogExercise, type ExerciseJson, type FieldChange } from '../session'
import { domainLabel, domainOptions } from '../catalogText'
import { navigate, search, withParams } from '../route'
import { shippedLanguages, t } from '../i18n'

const props = defineProps<{ slug?: string }>()

type Texts = Record<string, string>
type Form = {
  slug: string
  name: Texts
  steps: Texts[]
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

const fromExercise = (exercise?: ExerciseJson): Form => ({
  slug: exercise?.slug ?? '',
  name: texts(exercise?.name),
  steps: (exercise?.instructions ?? []).map(texts),
  note: texts(exercise?.variationNote),
  aliases: Object.fromEntries(shippedLanguages.map((language) => [language, (exercise?.aliases?.[language] ?? []).join(', ')])),
  needs: (exercise?.equipment ?? []).map(lower),
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
    instructions: form.steps.map(written).filter((step) => Object.keys(step).length),
  }
  if (Object.keys(note).length) exercise.variationNote = note
  else delete exercise.variationNote
  return exercise
}

const state = ref<'loading' | 'ready' | 'missing' | 'failed'>('loading')
const loaded = ref<CatalogExercise>()
const form = reactive<Form>(fromExercise())
const changes = ref<FieldChange[]>([])
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
const languageName = (code: string) => ({ en: 'English', fr: 'Français' } as Record<string, string>)[code] ?? code

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
    changes.value = []
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
  changes.value = (await changedFields(props.slug)) ?? []
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
/** The badge under a translatable field: every language has the text, or which ones lack it. An optional text left empty says nothing. */
function status(from: Texts, optional = false): { tone: 'success' | 'error'; text: string } | undefined {
  const missing = missingIn(from)
  if (optional && missing.length === shippedLanguages.length) return undefined
  if (!missing.length) return { tone: 'success', text: t('admin.exerciseTranslated') }
  return { tone: 'error', text: t('admin.catalogLanguagesMissing', { languages: missing.map(languageName).join(', ') }) }
}
/** The language switch marks a language some text still lacks. */
const lacks = (code: string) => [form.name, ...form.steps, ...(Object.keys(written(form.note)).length ? [form.note] : [])].some((from) => !from[code]?.trim())

const patternOptions = domainOptions('pattern')
const measurementOptions = domainOptions('measurement')
const positionOptions = domainOptions('position')
const muscleOptions = domainOptions('muscle')
const equipmentOptions = domainOptions('equipment')
const focusOptions = domainOptions('focus')
const group = (kind: 'position' | 'muscle' | 'equipment' | 'focus', codes: string[]) => codes.map((code) => domainLabel(kind, code)).join(', ')

const addStep = () => form.steps.push(blank())
const removeStep = (at: number) => form.steps.splice(at, 1)
const addNeed = () => form.needs.push([])
const removeNeed = (at: number) => form.needs.splice(at, 1)

const contentTone = computed(() => (loaded.value?.change === 'added' ? 'primary' : 'neutral'))
/** What a changed field of the draft is called, such as `name.fr` or `instructions.1.en`. */
function fieldLabel(field: string): string {
  const [head, ...rest] = field.split('.')
  const code = rest.at(-1) ?? ''
  const where = languageName(code)
  if (head === 'name') return t('admin.changeIn', { field: t('admin.fieldName'), language: where })
  if (head === 'variationNote') return t('admin.changeIn', { field: t('admin.variationNote'), language: where })
  if (head === 'aliases') return t('admin.changeIn', { field: t('admin.fieldAliases'), language: where })
  if (head === 'instructions') return t('admin.changeStep', { n: String(Number(rest[0]) + 1), language: where })
  const plain: Record<string, string> = {
    muscleGroups: 'admin.exerciseMuscles',
    equipment: 'admin.exerciseEquipment',
    optionalEquipment: 'admin.equipmentOptional',
    movementPattern: 'admin.filterPattern',
    measurement: 'admin.fieldMeasurement',
    positions: 'admin.filterPositions',
    focus: 'admin.fieldFocus',
    perSide: 'admin.attrEachSide',
    jumps: 'admin.attrJumps',
  }
  return t(plain[head] ?? 'admin.changeFallback')
}
const changedLabels = computed(() => [...new Set(changes.value.map((one) => fieldLabel(one.field)))])

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
    else changes.value = (await changedFields(answer.saved.exercise.slug)) ?? []
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
      <CpBreadcrumbs :label="t('admin.exerciseBreadcrumbs')">
        <li><a href="/admin/catalog" @click.prevent="navigate('/admin/catalog')">{{ t('admin.catalogTitle') }}</a></li>
        <li>{{ form.name[shippedLanguages[0]] || t('admin.exerciseNew') }}</li>
      </CpBreadcrumbs>
      <form class="flex flex-col gap-6" @submit.prevent="save">
        <div class="flex flex-wrap items-start justify-between gap-4">
          <div class="flex flex-col gap-2">
            <h1 class="cp-type-display">{{ isNew ? t('admin.exerciseNew') : t('admin.exerciseEdit') }}</h1>
            <div v-if="loaded" class="flex flex-wrap gap-2">
              <CpBadge v-if="loaded.change === 'added'" :tone="contentTone">{{ t('admin.catalogContentNew') }}</CpBadge>
              <CpBadge v-else-if="loaded.change === 'edited'" :tone="contentTone">{{ t('admin.catalogContentEdited') }}</CpBadge>
              <CpBadge v-else-if="loaded.change === 'offered'" tone="success">{{ t('admin.catalogContentOfferedAgain') }}</CpBadge>
              <CpBadge :tone="loaded.exercise.retired ? 'neutral' : 'success'">{{ loaded.exercise.retired ? t(loaded.change === 'retired' ? 'admin.catalogLifeRetiring' : 'admin.catalogLifeRetired') : t('admin.catalogLifeActive') }}</CpBadge>
            </div>
          </div>
          <div class="flex flex-wrap items-center gap-2">
            <div class="flex gap-1" role="group" :aria-label="t('admin.exerciseLanguage')">
              <CpChip v-for="code in shippedLanguages" :key="code" compact :model-value="code === language" @update:model-value="showLanguage(code)">
                {{ code.toUpperCase() }}
                <CircleExclamationPoint v-if="lacks(code)" class="size-3.5 shrink-0 text-error" aria-hidden="true" />
              </CpChip>
            </div>
            <CpButton variant="outline" size="md" @click="navigate('/admin/catalog')">{{ t('admin.exerciseCancel') }}</CpButton>
            <CpButton type="submit" size="md" :disabled="saving">{{ t('admin.exerciseSave') }}</CpButton>
          </div>
        </div>
        <CpSuccessNote v-if="saved">{{ t('admin.exerciseSaved') }}</CpSuccessNote>
        <CpErrorNote v-if="failure === 'failed'">{{ t('admin.exerciseSaveFailed') }}</CpErrorNote>
        <CpErrorNote v-if="failure === 'invalid'">{{ t('admin.exerciseInvalid', { reason }) }}</CpErrorNote>
        <CpCard v-if="isNew || loaded" :title="loaded?.change === 'added' || isNew ? t('admin.stateNew') : changedLabels.length ? t('admin.stateDraft') : t('admin.statePublished')">
          <template v-if="!isNew && loaded?.change !== 'added' && changedLabels.length">
            <p class="text-sm text-base-content/70">{{ t('admin.stateDraftHint') }}</p>
            <ul class="flex flex-wrap gap-2"><li v-for="label in changedLabels" :key="label"><CpBadge tone="neutral">{{ label }}</CpBadge></li></ul>
          </template>
        </CpCard>
        <div class="grid items-start gap-6 lg:grid-cols-3">
          <div class="flex flex-col gap-6 lg:col-span-2">
            <CpCard :title="t('admin.exerciseInfo')">
              <div class="grid gap-4 sm:grid-cols-2">
                <CpTextField v-model="form.name[language]" :label="t('admin.fieldName')" autocomplete="off">
                  <template #status><CpBadge v-if="status(form.name)" :tone="status(form.name)!.tone" class="w-fit">{{ status(form.name)!.text }}</CpBadge></template>
                </CpTextField>
                <CpTextField v-model="form.slug" :label="t('admin.fieldSlug')" autocomplete="off" :disabled="locked" :error="failure === 'slug-taken' ? t('admin.exerciseSlugTaken') : undefined" :hint="locked ? t('admin.slugLocked') : t('admin.slugHint')" @input="slugTouched = true" />
              </div>
            </CpCard>
            <CpCard :title="t('admin.exerciseSteps')" :description="t('admin.stepsHint')">
              <ol class="flex flex-col gap-4">
                <li v-for="(step, at) in form.steps" :key="at" class="flex items-start gap-2">
                  <div class="grow">
                    <CpTextarea v-model="step[language]" :label="t('admin.stepN', { n: String(at + 1) })">
                      <template #status><CpBadge v-if="status(step)" :tone="status(step)!.tone" class="w-fit">{{ status(step)!.text }}</CpBadge></template>
                    </CpTextarea>
                  </div>
                  <button type="button" class="cp-btn cp-btn-ghost cp-btn-square cp-btn-sm mt-6" :aria-label="t('admin.stepRemove', { n: String(at + 1) })" @click="removeStep(at)"><X class="size-4" aria-hidden="true" /></button>
                </li>
              </ol>
              <div><CpButton variant="outline" size="md" class="gap-2" @click="addStep"><Plus class="size-4" aria-hidden="true" />{{ t('admin.stepAdd') }}</CpButton></div>
              <CpTextField v-model="form.note[language]" :label="t('admin.variationNote')" autocomplete="off">
                <template #status><CpBadge v-if="status(form.note, true)" :tone="status(form.note, true)!.tone" class="w-fit">{{ status(form.note, true)!.text }}</CpBadge></template>
              </CpTextField>
            </CpCard>
            <CpCard :title="t('admin.exerciseEquipment')">
              <p v-if="!form.needs.length" class="text-sm text-base-content/70">{{ t('admin.equipmentNone') }}</p>
              <div v-for="(need, at) in form.needs" :key="at" class="flex items-start gap-2">
                <div class="flex grow flex-col gap-2">
                  <CpMultiSelectField v-model="form.needs[at]" :label="t('admin.equipmentNeed')" :placeholder="t('admin.filterEquipmentPlaceholder')" :options="equipmentOptions" :none-label="t('admin.filterNoMatch')" />
                  <p class="text-sm text-base-content/70">{{ group('equipment', need) || t('admin.equipmentNeedHint') }}</p>
                </div>
                <button type="button" class="cp-btn cp-btn-ghost cp-btn-square cp-btn-sm" :aria-label="t('admin.equipmentRemove', { n: String(at + 1) })" @click="removeNeed(at)"><X class="size-4" aria-hidden="true" /></button>
              </div>
              <div><CpButton variant="outline" size="md" class="gap-2" @click="addNeed"><Plus class="size-4" aria-hidden="true" />{{ t('admin.equipmentAdd') }}</CpButton></div>
              <div class="flex flex-col gap-2">
                <CpMultiSelectField v-model="form.optional" :label="t('admin.equipmentOptional')" :placeholder="t('admin.filterEquipmentPlaceholder')" :options="equipmentOptions" :none-label="t('admin.filterNoMatch')" />
                <p class="text-sm text-base-content/70">{{ group('equipment', form.optional) || t('admin.equipmentOptionalHint') }}</p>
              </div>
            </CpCard>
          </div>
          <div class="flex flex-col gap-6">
            <CpCard :title="t('admin.exerciseClassification')">
              <CpSelectField v-model="form.pattern" :label="t('admin.filterPattern')" :options="patternOptions" />
              <CpSelectField v-model="form.measurement" :label="t('admin.fieldMeasurement')" :options="measurementOptions" />
              <div class="flex flex-col gap-2">
                <CpMultiSelectField v-model="form.positions" :label="t('admin.filterPositions')" :placeholder="t('admin.filterPositionsPlaceholder')" :options="positionOptions" :none-label="t('admin.filterNoMatch')" />
                <p v-if="form.positions.length" class="text-sm text-base-content/70">{{ group('position', form.positions) }}</p>
              </div>
            </CpCard>
            <CpCard :title="t('admin.exerciseMuscles')">
              <div class="flex flex-col gap-2">
                <CpMultiSelectField v-model="form.muscles" :label="t('admin.filterMuscles')" :placeholder="t('admin.filterMusclesPlaceholder')" :options="muscleOptions" :none-label="t('admin.filterNoMatch')" />
                <p v-if="form.muscles.length" class="text-sm text-base-content/70">{{ group('muscle', form.muscles) }}</p>
              </div>
            </CpCard>
            <CpCard :title="t('admin.exerciseAdditional')">
              <div class="flex flex-col gap-2">
                <CpMultiSelectField v-model="form.focus" :label="t('admin.fieldFocus')" :placeholder="t('admin.filterFocusPlaceholder')" :options="focusOptions" :none-label="t('admin.filterNoMatch')" />
                <p v-if="form.focus.length" class="text-sm text-base-content/70">{{ group('focus', form.focus) }}</p>
              </div>
              <div class="flex flex-wrap gap-2">
                <CpChip v-model="form.perSide">{{ t('admin.attrEachSide') }}</CpChip>
                <CpChip v-model="form.jumps">{{ t('admin.attrJumps') }}</CpChip>
              </div>
              <CpTextField v-model="form.aliases[language]" :label="t('admin.fieldAliases')" autocomplete="off" :hint="t('admin.aliasesHint')" />
            </CpCard>
          </div>
        </div>
      </form>
    </template>
  </main>
</template>
