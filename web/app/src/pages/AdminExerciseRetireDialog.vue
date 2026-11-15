<script setup lang="ts">
// The retirement of one exercise from its own screen (the story Web/Screens/Admin/Exercise, Retire, #389): a draft
// change, not a deletion. The exercise stays offered until a publication carries it.
import { ref, watch } from 'vue'
import { Archive } from '@lucide/vue'
import { CpButton, CpDialog, CpErrorNote } from '@coach-poulpy/ui'
import { retireCatalogExercise } from '../session'
import { t } from '../i18n'

const props = defineProps<{ slug: string; name: string }>()
const open = defineModel<boolean>('open', { default: false })
const emit = defineEmits<{ retired: [] }>()

const retiring = ref(false)
const failed = ref(false)
watch(open, (now) => {
  if (now) failed.value = false
})

async function retire() {
  retiring.value = true
  failed.value = false
  const done = await retireCatalogExercise(props.slug)
  retiring.value = false
  if (done) {
    open.value = false
    emit('retired')
  } else {
    failed.value = true
  }
}
</script>

<template>
  <CpDialog v-model:open="open" rising :title="t('admin.exerciseRetireTitle', { name })" :description="t('admin.exerciseRetireIntro', { name })" :close-label="t('admin.retireClose')">
    <CpErrorNote v-if="failed">{{ t('admin.retireFailed') }}</CpErrorNote>
    <template #actions>
      <CpButton variant="ghost" size="md" @click="open = false">{{ t('admin.retireCancel') }}</CpButton>
      <CpButton variant="outline" size="md" class="gap-2" :loading="retiring" @click="retire"><Archive class="size-5 shrink-0" aria-hidden="true" />{{ t('admin.exerciseRetireConfirm') }}</CpButton>
    </template>
  </CpDialog>
</template>
