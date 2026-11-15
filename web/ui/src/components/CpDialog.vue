<script setup lang="ts">
import CpIcon from './CpIcon.vue'
// A dialog: a centred box over the page with a title, a line of help, a close button, the content and a row of
// actions. For a review the person confirms (publishing, retiring). [wide] gives a long table room; [rising] makes
// it rise from the bottom on a phone-width page, as a CpSheet does.
// A native <dialog>, so focus, Escape and the backdrop behave as the browser expects.
import { ref, watch, onMounted } from 'vue'
import { X } from '@lucide/vue'
import { uiText } from '../i18n'

const props = defineProps<{ title: string; description?: string; closeLabel?: string; wide?: boolean; rising?: boolean }>()
const open = defineModel<boolean>('open', { default: false })
const dialog = ref<HTMLDialogElement>()
const titleId = `dialog-title-${Math.random().toString(36).slice(2, 8)}`

const sync = () => {
  if (!dialog.value) return
  if (open.value && !dialog.value.open) dialog.value.showModal()
  if (!open.value && dialog.value.open) dialog.value.close()
}
onMounted(sync)
watch(open, sync)
</script>

<template>
  <dialog ref="dialog" class="cp-modal" :class="{ 'cp-modal-bottom sm:cp-modal-middle': props.rising }" data-component="dialog" :aria-labelledby="titleId" @close="open = false">
    <div class="cp-modal-box flex max-h-[90vh] w-11/12 flex-col gap-4 overflow-y-auto" :class="props.wide ? 'max-w-4xl p-4 md:p-6' : 'max-w-2xl'">
      <div class="flex items-start justify-between gap-4">
        <div class="flex flex-col gap-1">
          <h2 :id="titleId" class="cp-type-headline">{{ props.title }}</h2>
          <p v-if="props.description" class="text-sm text-base-content/70">{{ props.description }}</p>
        </div>
        <form method="dialog">
          <button class="cp-btn cp-btn-ghost cp-btn-square cp-btn-sm" :aria-label="props.closeLabel ?? uiText('ui.close')"><CpIcon :icon="X" /></button>
        </form>
      </div>
      <slot />
      <div v-if="$slots.actions" class="flex flex-wrap justify-end gap-2 border-t border-base-300 pt-4"><slot name="actions" /></div>
    </div>
    <form method="dialog" class="cp-modal-backdrop"><button>{{ props.closeLabel ?? uiText('ui.close') }}</button></form>
  </dialog>
</template>
