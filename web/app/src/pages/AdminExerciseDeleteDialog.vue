<script setup lang="ts">
// The deletion of one exercise (the story Web/Screens/Admin/Exercise, Delete and DeletePublished, #457): for good,
// confirmed by typing its slug. A published exercise says that the workouts using it lose it.
import { computed, ref, watch } from 'vue'
import { Trash2 } from '@lucide/vue'
import { CpButton, CpDialog, CpErrorNote, CpTextField } from '@coach-poulpy/ui'
import { deleteCatalogExercise } from '../session'
import { t } from '../i18n'

const props = defineProps<{ slug: string; published: boolean }>()
const open = defineModel<boolean>('open', { default: false })
const emit = defineEmits<{ deleted: [] }>()

const typed = ref('')
const deleting = ref(false)
const failed = ref(false)
watch(open, (now) => {
  if (now) {
    typed.value = ''
    failed.value = false
  }
})
const matches = computed(() => typed.value === props.slug)

async function remove() {
  if (!matches.value) return
  deleting.value = true
  failed.value = false
  const done = await deleteCatalogExercise(props.slug)
  deleting.value = false
  if (done) {
    open.value = false
    emit('deleted')
  } else {
    failed.value = true
  }
}
</script>

<template>
  <CpDialog v-model:open="open" rising :title="t('admin.exerciseDeleteTitle', { slug })" :description="t('admin.exerciseDeleteIntro')" :close-label="t('admin.exerciseDeleteClose')">
    <p v-if="published" class="cp-alert cp-alert-error" role="alert">{{ t('admin.exerciseDeletePublishedWarning', { slug }) }}</p>
    <CpTextField v-model="typed" :label="t('admin.exerciseDeleteTypeSlug', { slug })" autocomplete="off" />
    <CpErrorNote v-if="failed">{{ t('admin.exerciseDeleteFailed') }}</CpErrorNote>
    <template #actions>
      <CpButton variant="ghost" size="md" @click="open = false">{{ t('admin.retireCancel') }}</CpButton>
      <CpButton variant="danger" size="md" class="gap-2" :loading="deleting" :disabled="!matches" @click="remove"><Trash2 class="size-5 shrink-0" aria-hidden="true" />{{ t('admin.exerciseDeleteConfirm') }}</CpButton>
    </template>
  </CpDialog>
</template>
