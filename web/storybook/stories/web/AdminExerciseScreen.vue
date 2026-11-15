<script setup lang="ts">
// The admin's exercise screen: one exercise of the catalog draft, edited or added (ADR 0029: composed from the Cp
// components, as the web app's AdminExerciseView does, with a static exercise). A text is written in one language at a
// time; under each translatable field a badge says where the other language stands.
import { computed, reactive } from 'vue'
import { GripVertical, Lock, Plus, X, Archive, Target, Trash2 } from '@lucide/vue'
import { CpBadge, CpBodyMap, CpBreadcrumbs, CpButton, CpCard, CpChip, CpChipSelectField, CpErrorNote, CpSegmentedControl, CpSelectField, CpTextarea, CpTextField } from '@coach-poulpy/ui'
import { groupAreas, groupOfArea, selectableAreas } from '../../../app/src/bodyMap'
import AdminShell from './AdminShell.vue'

type Texts = { en: string; fr: string }
export interface Exercise {
  name: Texts
  slug: string
  steps: Texts[]
  note: Texts
  aliases: Texts
  needs: string[]
  optional: string[]
  pattern: string
  measurement: string
  positions: string[]
  muscles: string[]
  focus: string[]
  perSide?: boolean
  jumps?: boolean
}

const props = withDefaults(defineProps<{
  exercise: Exercise
  language?: 'en' | 'fr'
  /** A new exercise: its slug is open, and nothing is published. */
  isNew?: boolean
  badges?: { text: string; tone: 'success' | 'neutral' | 'outline' | 'primary' }[]
  slugTaken?: boolean
  /** A saved draft that no publication has ever included: the only kind that can be deleted. */
  neverPublished?: boolean
  /** Said under the language switch: where the French text stands. */
  frenchFlagged?: boolean
}>(), { language: 'en' })

const form = reactive({ ...props.exercise, steps: props.exercise.steps.map((step) => ({ ...step })), name: { ...props.exercise.name }, note: { ...props.exercise.note }, aliases: { ...props.exercise.aliases } })
const translating = computed(() => props.language !== 'en')
const code = computed(() => props.language)

const options = (...texts: string[]) => texts.map((text) => ({ value: text.toLowerCase().replace(/ /g, '_'), text }))
const patternOptions = options('Squat', 'Hinge', 'Lunge', 'Push', 'Pull', 'Carry', 'Core', 'Rotation', 'Cardio', 'Mobility', 'Stretch')
const measurementOptions = options('Reps', 'Distance', 'Time', 'Hold')
const positionOptions = options('Standing', 'Seated', 'Lying', 'Kneeling', 'Hanging')
const equipmentOptions = options('Barbell', 'Bench', 'Dumbbells', 'Kettlebell', 'Pull-up bar', 'Resistance band')
const focusOptions = options('Strength', 'Power', 'Endurance', 'Mobility', 'Stability')
const muscleNames: Record<string, string> = { quadriceps: 'Quadriceps', glutes: 'Glutes', core: 'Core', hamstrings: 'Hamstrings', chest: 'Chest', shoulders: 'Shoulders', triceps: 'Triceps', full_body: 'Full body' }
const muscleLabel = (group: string) => muscleNames[group] ?? group
const musclesList = computed(() => form.muscles.map(muscleLabel).join(', '))
const lit = computed(() => groupAreas(form.muscles))
const musclesSaid = computed(() => (form.muscles.length ? `Muscles worked: ${musclesList.value}.` : 'No muscle picked yet.'))
const removeLabel = (text: string) => `Remove ${text}`

const filled = (text?: string) => !!text?.trim()
type Status = { tone: 'success' | 'error' | 'neutral'; text: string }
/** Where the French text stands while the original is shown. */
const status = (from: Texts, kind: 'name' | 'step' | 'note' | 'aliases'): Status[] => {
  if (kind === 'note' && !filled(from.en) && !filled(from.fr)) return []
  if (kind === 'name' && filled(from.fr)) return [{ tone: 'success', text: 'All languages translated' }]
  if (filled(from.fr)) return [{ tone: 'success', text: 'French · translated' }]
  return [kind === 'aliases' ? { tone: 'neutral', text: 'French · not needed' } : { tone: 'error', text: 'French · missing' }]
}
const originalOf = (from: Texts) => `English original: ${from.en.trim() || 'Nothing in English'}`
const languages = [
  { value: 'en', text: 'EN' },
  { value: 'fr', text: 'FR', flagged: props.frenchFlagged, flagLabel: 'Some text is missing in French' },
]
const slugLocked = computed(() => !props.isNew)
</script>

<template>
  <AdminShell title="Catalog" active="catalog">
    <main class="flex min-w-0 flex-col gap-6 p-4 md:p-8">
      <div class="flex flex-col gap-4 bg-base-200 py-2" data-component="form-header">
        <CpBreadcrumbs label="You are here">
          <li><a href="#">Catalog</a></li>
          <li>{{ isNew ? 'New exercise' : exercise.name.en }}</li>
        </CpBreadcrumbs>
        <div class="flex flex-wrap items-center justify-between gap-4">
          <div class="flex flex-col gap-1">
            <h1 class="cp-type-display">{{ isNew ? 'New exercise' : 'Edit exercise' }}</h1>
            <p class="flex flex-wrap items-center gap-2">
              <CpBadge v-for="badge in badges" :key="badge.text" :tone="badge.tone">{{ badge.text }}</CpBadge>
              <span v-if="isNew" class="text-sm text-base-content/70">Not published yet</span>
            </p>
          </div>
          <div class="flex flex-wrap items-center gap-2">
            <CpSegmentedControl :model-value="language" label="Language" :options="languages" />
            <CpButton variant="ghost" size="md">Cancel</CpButton>
            <CpButton type="submit" form="exercise-form" size="md">{{ isNew ? 'Add to the draft' : 'Save' }}</CpButton>
          </div>
        </div>
        <p class="text-sm text-base-content/70" :class="{ invisible: !translating }" :aria-hidden="!translating">Editing the French text. English is the original; structure, variations, muscles, equipment and classification are the same in every language.</p>
      </div>
      <CpErrorNote v-if="slugTaken">Not saved. Fix the slug and save again.</CpErrorNote>
      <form id="exercise-form" class="grid items-start gap-6 lg:flex" @submit.prevent>
        <div class="contents lg:flex lg:min-w-0 lg:flex-1 lg:flex-col lg:gap-6" data-component="main-column">
          <CpCard title="Exercise information" class="order-1 lg:order-none">
            <div class="grid items-start gap-4 sm:grid-cols-2">
              <CpTextField
                v-model="form.name[code]"
                :label="translating ? 'Name in French' : 'Name'"
                :placeholder="translating ? 'Needed before publishing' : undefined"
                :hint="translating ? originalOf(form.name) : undefined"
                :invalid="translating && !filled(form.name.fr)"
                autocomplete="off"
              >
                <template v-if="!translating" #status>
                  <p class="cp-label flex flex-wrap gap-1 whitespace-normal"><CpBadge v-for="one in status(form.name, 'name')" :key="one.text" :tone="one.tone" small>{{ one.text }}</CpBadge></p>
                </template>
              </CpTextField>
              <CpTextField
                v-model="form.slug"
                label="Slug"
                autocomplete="off"
                :disabled="slugLocked"
                :error="slugTaken ? 'Another exercise already has this slug. Pick another one.' : undefined"
                :hint="slugLocked ? 'Permanent identifier used by existing workouts. Not translated.' : 'Lowercase words joined by dashes. Editable until the exercise is first published, then never. Not translated.'"
              >
                <template v-if="slugLocked" #icon><Lock class="size-5 shrink-0" aria-hidden="true" /></template>
              </CpTextField>
            </div>
            <div class="cp-divider my-0" role="separator"></div>
            <div class="flex flex-col gap-1">
              <h2 class="cp-card-title">Steps</h2>
              <p class="text-sm text-base-content/70">In order, one instruction each. Adding or removing a step does it in every language.</p>
            </div>
            <ol class="flex flex-col gap-3">
              <template v-for="(step, at) in form.steps" :key="at">
                <div v-if="at" class="cp-divider my-0" role="separator"></div>
                <li class="flex items-start gap-3" data-component="step">
                  <span class="cp-badge cp-badge-primary cp-badge-soft shrink-0">{{ at + 1 }}</span>
                  <div class="min-w-0 grow">
                    <CpTextarea
                      v-model="step[code]"
                      :label="translating ? `Step ${at + 1} in French` : `Step ${at + 1}`"
                      :placeholder="translating ? 'Needed before publishing' : undefined"
                      :hint="translating ? originalOf(step) : undefined"
                      :invalid="translating && !filled(step.fr)"
                    >
                      <template v-if="!translating" #status>
                        <p class="cp-label flex flex-wrap gap-1 whitespace-normal"><CpBadge v-for="one in status(step, 'step')" :key="one.text" :tone="one.tone" small>{{ one.text }}</CpBadge></p>
                      </template>
                    </CpTextarea>
                  </div>
                  <div class="flex shrink-0 flex-col">
                    <button type="button" class="cp-btn cp-btn-circle cp-btn-ghost cp-btn-sm cursor-grab" :aria-label="`Move step ${at + 1}`" :disabled="form.steps.length < 2">
                      <GripVertical class="size-4 shrink-0" aria-hidden="true" />
                    </button>
                    <button type="button" class="cp-btn cp-btn-circle cp-btn-ghost cp-btn-sm" :aria-label="`Remove step ${at + 1}`">
                      <X class="size-4 shrink-0" aria-hidden="true" />
                    </button>
                  </div>
                </li>
              </template>
            </ol>
            <div><CpButton variant="outline" size="md" class="cp-btn-sm gap-2"><Plus class="size-4 shrink-0" aria-hidden="true" />Add step</CpButton></div>
          </CpCard>
          <CpCard class="order-5 lg:order-none">
            <div class="grid gap-6 md:grid-cols-2">
              <div class="flex min-w-0 flex-col gap-4">
                <h2 class="cp-card-title">Equipment</h2>
                <h3 class="cp-type-label">Required</h3>
                <CpChipSelectField v-model="form.needs" aria-label="Required equipment" hint="Any of these" placeholder="Select equipment" :options="equipmentOptions" none-label="Nothing matches" :remove-label="removeLabel" />
                <h3 class="cp-type-label">Optional</h3>
                <CpChipSelectField v-model="form.optional" aria-label="Optional equipment" hint="Helps but not needed" placeholder="Select equipment" :options="equipmentOptions" none-label="Nothing matches" :remove-label="removeLabel" />
              </div>
              <div class="flex min-w-0 flex-col gap-4">
                <h2 class="cp-card-title">Classification</h2>
                <div class="grid gap-3 sm:grid-cols-2">
                  <CpSelectField v-model="form.pattern" label="Movement pattern" :options="patternOptions" />
                  <CpSelectField v-model="form.measurement" label="Measured in" :options="measurementOptions" />
                </div>
                <CpChipSelectField v-model="form.positions" label="Position" :options="positionOptions" none-label="Nothing matches" :remove-label="removeLabel" />
              </div>
            </div>
          </CpCard>
          <!-- CLEANUP: the danger zone is not built yet: drawn with the classes of the mockup. -->
          <CpCard v-if="!isNew" title="Danger zone" class="order-6 border border-error lg:order-none" data-component="danger-zone">
            <div class="flex flex-wrap items-center justify-between gap-4">
              <div class="flex min-w-0 flex-1 flex-col gap-1">
                <h3 class="font-semibold">Retire this exercise</h3>
                <p class="text-sm text-base-content/70">Prepares a lifecycle change. The exercise remains active until a publication includes its retirement. Workouts that use it still open; nothing is deleted, and you can offer it again later.</p>
              </div>
              <CpButton variant="outline" size="md" class="gap-2"><Archive class="size-5 shrink-0" aria-hidden="true" />Prepare retirement</CpButton>
            </div>
            <div class="cp-divider my-0" role="separator"></div>
            <div class="flex flex-wrap items-center justify-between gap-4">
              <div class="flex min-w-0 flex-1 flex-col gap-1">
                <h3 class="font-semibold">Delete this exercise</h3>
                <p v-if="neverPublished" class="text-sm text-base-content/70">Removes this draft for good. It was never published, so no workout or phone uses it.</p>
                <p v-else class="text-sm text-base-content/70">A published exercise cannot be deleted: workouts and history on phones refer to it. Retire it instead.</p>
              </div>
              <button type="button" class="cp-btn cp-btn-error gap-2" :disabled="!neverPublished"><Trash2 class="size-5 shrink-0" aria-hidden="true" />Delete exercise</button>
            </div>
          </CpCard>
        </div>
        <div class="contents lg:flex lg:w-full lg:max-w-md lg:flex-col lg:gap-6" data-component="side-column">
          <CpCard title="Variations" class="order-3 lg:order-none">
            <!-- CLEANUP: the variation group (a table to reorder) is not built yet: drawn with the classes of the mockup. -->
            <template v-if="!isNew">
              <p class="text-sm text-base-content/70">Exercises in this group, ordered from easiest to hardest. The shared order is reflected on every exercise in the group.</p>
              <div class="overflow-x-auto">
                <table class="cp-table w-full text-sm">
                  <thead><tr><th scope="col" class="w-10 px-2 py-2"><span class="sr-only">Reorder</span></th><th scope="col" class="px-2 py-2 font-semibold">Exercise</th><th scope="col" class="w-12 px-2 py-2 text-right"><span class="sr-only">Focus</span></th></tr></thead>
                  <tbody>
                    <tr v-for="(row, at) in ['Sit to stand', 'Bodyweight squat', exercise.name.en]" :key="row" :class="{ 'bg-base-200': at === 2 }">
                      <td class="w-10 px-2 py-2"><button type="button" class="cp-btn cp-btn-circle cp-btn-ghost cp-btn-sm" :aria-label="`Reorder ${row}`" title="Drag to reorder"><GripVertical class="size-4 shrink-0" aria-hidden="true" /></button></td>
                      <td class="px-2 py-2"><a href="#" class="cp-link cp-link-hover font-semibold">{{ row }}</a></td>
                      <td class="w-12 px-2 py-2 text-right"><span v-if="at === 2" class="cp-badge cp-badge-primary cp-badge-soft cp-badge-sm" title="Focus" aria-label="Focus"><Target class="size-3 shrink-0" aria-hidden="true" /></span></td>
                    </tr>
                  </tbody>
                </table>
              </div>
              <div><CpButton variant="outline" size="md" class="cp-btn-sm">Add exercise</CpButton></div>
            </template>
            <CpTextField v-model="form.note[code]" label="How it differs" :hint="translating ? originalOf(form.note) : undefined" autocomplete="off">
              <template v-if="!translating" #status>
                <p class="cp-label flex flex-wrap gap-1 whitespace-normal"><CpBadge v-for="one in status(form.note, 'note')" :key="one.text" :tone="one.tone" small>{{ one.text }}</CpBadge></p>
              </template>
            </CpTextField>
          </CpCard>
          <CpCard title="Muscles" description="Tap an area to add or remove its muscle group." class="order-4 lg:order-none">
            <CpBodyMap :heat="lit" :description="musclesSaid" :selectable="selectableAreas" editing="heat" />
            <div class="flex flex-wrap items-center justify-center gap-3">
              <CpChip :model-value="form.muscles.includes('full_body')" compact>Full body</CpChip>
            </div>
            <p class="text-sm text-base-content/70">{{ musclesSaid }}</p>
            <h3 class="cp-card-title">Additional attributes</h3>
            <div class="flex flex-col gap-3 py-2">
              <CpChipSelectField v-model="form.focus" label="Focus" :options="focusOptions" none-label="Nothing matches" :remove-label="removeLabel" />
              <div class="flex flex-wrap gap-2">
                <CpChip v-model="form.perSide" compact>Each side</CpChip>
                <CpChip v-model="form.jumps" compact>Jumps</CpChip>
              </div>
              <CpTextField
                v-model="form.aliases[code]"
                :label="translating ? 'Search aliases in French' : 'Search aliases'"
                :hint="translating ? originalOf(form.aliases) : 'Other names people search with, separated by commas. Never shown.'"
                autocomplete="off"
              >
                <template v-if="!translating" #status>
                  <p class="cp-label flex flex-wrap gap-1 whitespace-normal"><CpBadge v-for="one in status(form.aliases, 'aliases')" :key="one.text" :tone="one.tone" small>{{ one.text }}</CpBadge></p>
                </template>
              </CpTextField>
            </div>
          </CpCard>
        </div>
      </form>
      <div class="flex flex-wrap justify-end gap-2 lg:hidden" data-component="form-actions">
        <CpButton variant="ghost" size="md">Cancel</CpButton>
        <CpButton type="submit" form="exercise-form" size="md">{{ isNew ? 'Add to the draft' : 'Save' }}</CpButton>
      </div>
    </main>
    <slot name="dialog" />
  </AdminShell>
</template>
