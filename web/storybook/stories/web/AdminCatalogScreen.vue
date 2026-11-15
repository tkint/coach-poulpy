<script setup lang="ts">
import { CpIcon } from '@coach-poulpy/ui'
// The admin's catalog list: the toolbar, the table of exercises and its selection menu (ADR 0029: composed from the
// Cp components, as the web app's AdminCatalogView does, with static exercises).
import { ref } from 'vue'
import { ChevronsUpDown, CircleExclamationPoint, EllipsisVertical, Plus, CloudUpload, RefreshCw } from '@lucide/vue'
import { CpBadge, CpButton, CpCard, CpCheckbox, CpChip, CpDropdownMenu, CpErrorNote, CpMultiSelectField, CpPagination, CpSearchField, CpSelectField, CpTable } from '@coach-poulpy/ui'
import AdminShell from './AdminShell.vue'

export interface Row { name: string; french: string; slug: string; pattern: string; missing?: string; content?: 'New' | 'Edited' | 'Offered again' | 'Retiring'; lifecycle?: 'Retired'; checked?: boolean }

const props = withDefaults(defineProps<{
  rows: Row[]
  intro?: string
  placeholder?: string
  pattern?: string
  translation?: string
  lifecycle?: string
  selection?: number
  /** What awaits publication: the status card says so, or that everything is published. */
  pending?: number
  showing?: string
  pages?: number
  error?: boolean
}>(), {
  intro: 'The exercises every phone gets. Edit the catalog, then publish the changes.',
  placeholder: 'Search exercises',
  pattern: '',
  translation: '',
  lifecycle: '',
  selection: 0,
  pending: 4,
  pages: 4,
})

const search = ref('')
const pending = ref(false)
const pattern = ref(props.pattern)
const translation = ref(props.translation)
const content = ref('')
const lifecycle = ref(props.lifecycle)
const muscles = ref<string[]>([])
const positions = ref<string[]>([])
const equipment = ref<string[]>([])
const page = ref(1)
const options = (...texts: string[]) => texts.map((text) => ({ value: text.toLowerCase(), text }))
const all = (label: string, ...texts: string[]) => [{ value: '', text: label }, ...options(...texts)]
const columns = ['Exercise', 'Pattern', 'Languages', 'Content', 'Lifecycle']
const contentTone = { New: 'primary', Edited: 'neutral', 'Offered again': 'success', Retiring: 'outline' } as const
</script>

<template>
  <AdminShell title="Catalog" active="catalog">
    <main class="flex min-w-0 flex-col gap-6 p-4 md:p-8">
      <div class="flex flex-wrap items-center justify-between gap-4">
        <div class="flex flex-col gap-1">
          <h1 class="cp-type-display">Catalog</h1>
          <p class="text-base-content/70">{{ intro }}</p>
        </div>
        <div v-if="error" class="flex flex-wrap gap-2">
          <CpButton variant="outline" size="md" class="gap-2"><CpIcon :icon="Plus" />New exercise</CpButton>
          <CpButton size="md" class="gap-2" disabled><CpIcon :icon="CloudUpload" />Publish changes</CpButton>
        </div>
      </div>
      <section v-if="error" class="cp-card bg-base-100 shadow-sm">
        <div class="cp-card-body gap-4 p-4 md:p-6">
          <CpErrorNote>The catalog did not load. The server did not answer; nothing in the draft was lost.</CpErrorNote>
          <div><CpButton variant="outline" size="md" class="gap-2"><CpIcon :icon="RefreshCw" />Try again</CpButton></div>
        </div>
      </section>
      <template v-else>
      <CpCard
        :title="pending === 0 ? 'Everything is published' : `${pending} changes await publication`"
        :description="pending === 0 ? 'Nothing is awaiting publication: phones have every exercise as it stands.' : 'Review them, then publish: phones download them the next time they open the app.'"
      >
        <div v-if="pending > 0" class="flex flex-wrap items-center justify-between gap-3">
          <div class="flex flex-wrap items-center gap-2">
            <CpBadge tone="primary">Creation · 1</CpBadge>
            <CpBadge tone="neutral">Edition · 2</CpBadge>
            <CpBadge tone="outline">Retirement · 1</CpBadge>
            <CpBadge tone="error">1 lack a translation</CpBadge>
          </div>
          <CpButton size="md" class="gap-2"><CpIcon :icon="CloudUpload" />Review and publish</CpButton>
        </div>
      </CpCard>
      <CpCard>
        <div class="flex flex-wrap items-center gap-2" role="search" aria-label="Filter the exercises">
          <CpSearchField size="sm" v-model="search" compact label="Search exercises by name, slug or alias" :placeholder="placeholder" />
          <CpChip v-model="pending" compact>Pending changes</CpChip>
          <CpSelectField size="sm" v-model="pattern" compact label="Movement pattern" :options="all('Movement pattern', 'Squat', 'Hinge', 'Lunge', 'Push', 'Pull', 'Carry', 'Core', 'Rotation', 'Locomotion', 'Mobility', 'Stretch')" />
          <CpSelectField size="sm" v-model="translation" compact label="Translation status" :options="all('Translation status', 'Complete', 'Incomplete')" />
          <CpSelectField size="sm" v-model="content" compact label="Content status" :options="all('Content status', 'Draft', 'Modified', 'Published', 'Pending publication')" />
          <CpSelectField size="sm" v-model="lifecycle" compact label="Lifecycle status" :options="all('Lifecycle status', 'Active', 'Retirement pending', 'Retired')" />
          <CpMultiSelectField size="sm" v-model="muscles" label="Muscles" placeholder="Muscles" none-label="Nothing matches" :options="options('Back', 'Biceps', 'Calves', 'Chest', 'Core', 'Forearms', 'Full body', 'Glutes', 'Hamstrings', 'Hips', 'Neck', 'Quadriceps', 'Shoulders', 'Triceps')" />
          <CpMultiSelectField size="sm" v-model="positions" label="Positions" placeholder="Positions" none-label="Nothing matches" :options="options('Standing', 'Seated', 'Floor')" />
          <CpMultiSelectField size="sm" v-model="equipment" label="Equipment" placeholder="Equipment" none-label="Nothing matches" :options="options('Band', 'Barbell', 'Bench or step', 'Bodyweight', 'Dumbbells', 'Kettlebell', 'Machine', 'Mat', 'Pull-up bar', 'Treadmill')" />
          <a href="#" class="cp-link cp-link-hover text-sm" @click.prevent>Clear filters</a>
          <CpButton size="md" class="ms-auto gap-2"><CpIcon :icon="Plus" />New exercise</CpButton>
        </div>
        <CpTable>
          <thead>
            <tr>
              <th class="w-12 text-center">
                <CpDropdownMenu v-if="selection" :label="`Actions for ${selection} selected ${selection === 1 ? 'exercise' : 'exercises'}`">
                  <template #trigger>
                    <CpIcon :icon="EllipsisVertical" size="sm" />
                    <span class="cp-badge cp-badge-primary cp-badge-sm absolute -right-2 -top-2 z-10">{{ selection }}</span>
                  </template>
                  <li><button type="button">Retire <span class="cp-badge cp-badge-sm">{{ selection }}</span></button></li>
                  <li class="my-1 border-t border-base-300" aria-hidden="true" />
                  <li><button type="button">Select all</button></li>
                  <li><button type="button">Clear selection</button></li>
                </CpDropdownMenu>
                <CpCheckbox v-else label="Select all exercises on this page" />
              </th>
              <th v-for="column in columns" :key="column" aria-sort="none">
                <button type="button" class="cp-btn cp-btn-ghost cp-btn-sm -ms-3 gap-1 whitespace-nowrap">{{ column }}<CpIcon :icon="ChevronsUpDown" size="sm" /></button>
              </th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in rows" :key="row.slug" class="cp-hover" :class="{ 'bg-base-200': row.checked }" :aria-selected="row.checked ? 'true' : undefined">
              <td class="text-center"><CpCheckbox :model-value="!!row.checked" :label="`Select ${row.name}`" /></td>
              <td>
                <div class="flex flex-col">
                  <a href="#" class="cp-link cp-link-hover text-start font-semibold" @click.prevent>{{ row.name }}</a>
                  <span class="text-sm text-base-content/70">{{ row.french }} · {{ row.slug }}</span>
                </div>
              </td>
              <td class="whitespace-nowrap">{{ row.pattern }}</td>
              <td><CpBadge :tone="row.missing ? 'error' : 'success'">{{ row.missing ?? 'Complete' }}</CpBadge></td>
              <td>
                <CpBadge v-if="row.content" :tone="contentTone[row.content]">{{ row.content }}</CpBadge>
                <span v-else class="text-base-content/70">None</span>
              </td>
              <td><CpBadge :tone="row.lifecycle ? 'neutral' : 'success'">{{ row.lifecycle ?? 'Offered' }}</CpBadge></td>
            </tr>
          </tbody>
        </CpTable>
        <div class="flex flex-col items-center justify-between gap-3 sm:flex-row">
          <p class="text-sm text-base-content/70">{{ showing }}</p>
          <CpPagination size="sm" v-model:page="page" :pages="pages" />
        </div>
      </CpCard>
      </template>
    </main>
    <slot />
  </AdminShell>
</template>
