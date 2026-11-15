<script setup lang="ts">
// The publication review of the catalog (the Web/Screens/Admin/Catalog stories, #389): the pending changes of the
// selected exercises, each with its translation status and what it changes, and the field-by-field before and after
// of an edit. Publishing gives each of them a new revision; no other exercise is written (ADR 0030).
import { computed, ref, watch } from 'vue'
import { CloudUpload } from '@lucide/vue'
import { CpBadge, CpButton, CpDialog, CpErrorNote } from '@coach-poulpy/ui'
import { publishCatalog, type CatalogChanges, type ChangedExercise } from '../session'
import { fieldLabel, fieldValue } from '../catalogChanges'
import { t } from '../i18n'

const props = defineProps<{ changes: CatalogChanges; slugs: string[] }>()
const open = defineModel<boolean>('open', { default: false })
const emit = defineEmits<{ published: [] }>()

type Row = { slug: string; name: ChangedExercise['name']; kind: 'added' | 'edited' | 'retired' | 'offered'; fields: { field: string; before: unknown; after: unknown }[]; missing: string[] }
const rows = computed<Row[]>(() => {
  const chosen = new Set(props.slugs)
  const missing = (slug: string) => props.changes.missing.find((one) => one.slug === slug)?.languages ?? []
  const all: Row[] = [
    ...props.changes.added.map((one) => ({ ...one, kind: 'added' as const, fields: [], missing: missing(one.slug) })),
    ...props.changes.edited.map((one) => ({ slug: one.slug, name: one.name, kind: 'edited' as const, fields: one.fields, missing: missing(one.slug) })),
    ...props.changes.retired.map((one) => ({ ...one, kind: 'retired' as const, fields: [], missing: missing(one.slug) })),
    ...props.changes.offered.map((one) => ({ ...one, kind: 'offered' as const, fields: [], missing: missing(one.slug) })),
  ]
  return all.filter((one) => chosen.has(one.slug))
})
const blocked = computed(() => rows.value.some((one) => one.missing.length > 0))
const kindLabel = { added: 'admin.publishCreation', edited: 'admin.publishEdition', retired: 'admin.publishRetirement', offered: 'admin.publishOffered' }
const languageNames: Record<string, string> = { en: 'admin.languageEn', fr: 'admin.languageFr' }
const missingText = (languages: string[]) => t('admin.publishMissing', { languages: languages.map((code) => (languageNames[code] ? t(languageNames[code]) : code)).join(', ') })
const title = (name: Record<string, string>, slug: string) => name.en ?? slug

const publishing = ref(false)
const failed = ref(false)
watch(open, (now) => {
  if (now) failed.value = false
})

async function publish() {
  publishing.value = true
  failed.value = false
  const answer = await publishCatalog(rows.value.map((one) => one.slug))
  publishing.value = false
  if ('published' in answer) {
    open.value = false
    emit('published')
  } else {
    failed.value = true
  }
}
</script>

<template>
  <CpDialog v-model:open="open" wide :title="t('admin.publishTitle')" :description="t('admin.publishIntro')" :close-label="t('admin.publishClose')">
    <div class="overflow-x-auto rounded-box border border-base-300">
      <table class="cp-table bg-base-100">
        <thead>
          <tr>
            <th>{{ t('admin.publishColExercise') }}</th>
            <th>{{ t('admin.publishColStatus') }}</th>
            <th>{{ t('admin.publishColChange') }}</th>
          </tr>
        </thead>
        <tbody>
          <template v-for="row in rows" :key="row.slug">
            <tr>
              <td>
                <div class="font-semibold">{{ title(row.name, row.slug) }}</div>
                <div class="text-sm text-base-content/70">{{ row.slug }}</div>
              </td>
              <td>
                <CpBadge :tone="row.missing.length ? 'error' : 'success'">{{ row.missing.length ? missingText(row.missing) : t('admin.publishComplete') }}</CpBadge>
              </td>
              <td>
                <CpBadge :tone="row.kind === 'added' ? 'primary' : row.kind === 'offered' ? 'success' : 'neutral'">{{ t(kindLabel[row.kind]) }}</CpBadge>
              </td>
            </tr>
            <tr v-if="row.fields.length" class="bg-base-200/50">
              <td colspan="3" class="!py-2">
                <div class="flex flex-col gap-2 border-s-2 border-base-300 ps-3">
                  <div class="grid grid-cols-[minmax(7rem,auto)_1fr] gap-x-3 gap-y-1 text-sm">
                    <template v-for="change in row.fields" :key="change.field">
                      <span class="font-semibold text-base-content/70">{{ fieldLabel(change.field) }}</span>
                      <div class="grid grid-cols-[1fr_auto_1fr] items-start gap-2">
                        <span class="rounded bg-error/10 px-2 py-1 text-error line-through">{{ fieldValue(change.field, change.before) }}</span>
                        <span class="text-base-content/50" aria-hidden="true">→</span>
                        <span class="rounded bg-success/10 px-2 py-1 text-success">{{ fieldValue(change.field, change.after) }}</span>
                      </div>
                    </template>
                  </div>
                </div>
              </td>
            </tr>
          </template>
        </tbody>
      </table>
    </div>
    <p class="text-sm text-base-content/70">{{ t('admin.publishNote') }}</p>
    <CpErrorNote v-if="blocked">{{ t('admin.publishBlocked') }}</CpErrorNote>
    <CpErrorNote v-else-if="failed">{{ t('admin.publishFailed') }}</CpErrorNote>
    <template #actions>
      <CpButton variant="ghost" size="md" @click="open = false">{{ t('admin.publishCancel') }}</CpButton>
      <CpButton size="md" class="gap-2" :loading="publishing" :disabled="blocked || rows.length === 0" @click="publish"><CloudUpload class="size-5 shrink-0" aria-hidden="true" />{{ t('admin.publishConfirm') }}</CpButton>
    </template>
  </CpDialog>
</template>
