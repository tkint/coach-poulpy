<script setup lang="ts">
// Details of the row picked in a list, in a panel that floats over the page from the right (the whole
// width on a phone) with a dim backdrop, so the list under it never moves. A native <dialog> like
// CpSheet: Escape and a click outside close it, and the page behind can't be reached meanwhile.
// Render it with `v-if` while a row is picked; it emits `close` once it has closed itself.
import { X } from '@lucide/vue'
import { onMounted, ref } from 'vue'

defineProps<{ title: string; closeLabel: string }>()
defineEmits<{ close: [] }>()
const dialog = ref<HTMLDialogElement>()
onMounted(() => dialog.value?.showModal())
</script>

<template>
  <dialog ref="dialog" class="cp-modal cp-modal-end" aria-labelledby="side-panel-title" data-component="side-panel" @close="$emit('close')">
    <div class="cp-modal-box flex w-full flex-col gap-4 sm:w-96">
      <div class="flex items-start gap-3">
        <div v-if="$slots.avatar"><slot name="avatar" /></div>
        <div class="flex min-w-0 grow flex-col gap-1">
          <h2 id="side-panel-title" class="cp-type-headline break-all">{{ title }}</h2>
          <slot name="subtitle" />
        </div>
        <button type="button" class="cp-btn cp-btn-circle cp-btn-ghost cp-btn-sm" :aria-label="closeLabel" @click="dialog?.close()">
          <X class="size-5 shrink-0" aria-hidden="true" />
        </button>
      </div>
      <slot />
    </div>
    <form method="dialog" class="cp-modal-backdrop"><button>{{ closeLabel }}</button></form>
  </dialog>
</template>
