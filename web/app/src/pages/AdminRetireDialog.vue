<script setup lang="ts">
// The retirement review of the selected exercises (the Web/Screens/Admin/Catalog stories, #390): which exercises
// would be retired, where each stands and what it has pending. Retiring only marks them in the draft: they stay
// offered until a publication carries it, and workouts and history are untouched.
import { computed, ref, watch } from 'vue'
import { CpBadge, CpButton, CpDialog, CpErrorNote } from '@coach-poulpy/ui'
import { retireCatalogExercise, type CatalogExercise } from '../session'
import { t } from '../i18n'

const props = defineProps<{ exercises: CatalogExercise[] }>()
const open = defineModel<boolean>('open', { default: false })
const emit = defineEmits<{ retired: [] }>()

/** Only an exercise still offered can be retired. */
const rows = computed(() => props.exercises.filter((row) => !row.exercise.retired))
const contentLabel = (row: CatalogExercise) =>
  ({ added: 'admin.catalogContentNew', edited: 'admin.catalogContentEdited', offered: 'admin.catalogContentOfferedAgain' } as Record<string, string>)[row.change ?? ''] ?? 'admin.retireUnchanged'
const title = computed(() => (rows.value.length === 1 ? t('admin.retireTitleOne') : t('admin.retireTitle', { count: String(rows.value.length) })))
const confirm = computed(() => (rows.value.length === 1 ? t('admin.retireConfirmOne') : t('admin.retireConfirm', { count: String(rows.value.length) })))

const retiring = ref(false)
const failed = ref(false)
watch(open, (now) => {
  if (now) failed.value = false
})

async function retire() {
  retiring.value = true
  failed.value = false
  const results = await Promise.all(rows.value.map((row) => retireCatalogExercise(row.exercise.slug)))
  retiring.value = false
  if (results.every(Boolean)) {
    open.value = false
  } else {
    failed.value = true
  }
  emit('retired')
}
</script>

<template>
  <CpDialog v-model:open="open" rising :title="title" :description="t('admin.retireIntro')" :close-label="t('admin.retireClose')">
    <div class="overflow-x-auto rounded-box border border-base-300">
      <table class="cp-table bg-base-100">
        <thead>
          <tr>
            <th>{{ t('admin.retireColExercise') }}</th>
            <th>{{ t('admin.retireColState') }}</th>
            <th>{{ t('admin.retireColContent') }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="row in rows" :key="row.exercise.slug">
            <td class="font-semibold">{{ row.exercise.name.en ?? row.exercise.slug }}</td>
            <td><CpBadge tone="success">{{ t('admin.retireOffered') }}</CpBadge></td>
            <td>{{ t(contentLabel(row)) }}</td>
          </tr>
        </tbody>
      </table>
    </div>
    <p class="text-sm text-base-content/70">{{ t('admin.retireNote') }}</p>
    <CpErrorNote v-if="failed">{{ t('admin.retireFailed') }}</CpErrorNote>
    <template #actions>
      <CpButton variant="ghost" size="md" @click="open = false">{{ t('admin.retireCancel') }}</CpButton>
      <CpButton variant="outline" size="md" :disabled="retiring || rows.length === 0" @click="retire">{{ confirm }}</CpButton>
    </template>
  </CpDialog>
</template>
