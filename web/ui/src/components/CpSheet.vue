<script setup lang="ts">
// A sheet: rises from the bottom on a phone-width page, a dialog in the middle on a wide one.
// A native <dialog>, so focus, Escape and the backdrop behave as the browser expects.
import { ref, watch, onMounted } from 'vue'

defineProps<{ title: string; closeLabel: string }>()
const open = defineModel<boolean>('open', { default: false })
const dialog = ref<HTMLDialogElement>()

const sync = () => {
  if (!dialog.value) return
  if (open.value && !dialog.value.open) dialog.value.showModal()
  if (!open.value && dialog.value.open) dialog.value.close()
}
onMounted(sync)
watch(open, sync)
</script>

<template>
  <dialog ref="dialog" class="cp-modal cp-modal-bottom sm:cp-modal-middle cp-sheet" data-component="sheet" @close="open = false">
    <div class="cp-modal-box">
      <h2 class="cp-type-headline">{{ title }}</h2>
      <slot />
      <div v-if="$slots.actions" class="cp-modal-action"><slot name="actions" /></div>
    </div>
    <form method="dialog" class="cp-modal-backdrop"><button>{{ closeLabel }}</button></form>
  </dialog>
</template>
