<script setup lang="ts">
// Choosing one value in a form, never a row of pills. A button shows the current value and opens a panel only
// while it is used, its options looking like the multi-select field's without the checkboxes: the chosen one is
// bold with a check. Choosing, Escape or a click elsewhere closes it. With `compact` it is one control of a
// filter bar: no visible label (the label still names it for assistive technology, and its first option usually
// says it too) and only as wide as its longest option.
import { computed, ref, useId } from 'vue'
import { Check, ChevronDown } from '@lucide/vue'
import { useDismissable } from '../dismissable'

const props = defineProps<{ label: string; options: { value: string; text: string }[]; compact?: boolean }>()
const value = defineModel<string>()
const root = ref<HTMLDetailsElement>()
const labelId = useId()
const { close } = useDismissable(root)
const current = computed(() => props.options.find((option) => option.value === value.value)?.text ?? '')
const choose = (picked: string) => {
  value.value = picked
  close()
}
</script>

<template>
  <div class="cp-fieldset" data-component="select-field">
    <span :id="labelId" class="cp-type-label" :class="{ 'sr-only': compact }">{{ label }}</span>
    <details ref="root" class="relative" :class="compact ? 'w-auto' : 'w-full'">
      <summary class="cp-btn cp-btn-outline list-none justify-between gap-2 font-normal" :class="compact ? 'cp-btn-sm' : 'w-full'" :aria-labelledby="labelId">
        <span class="truncate">{{ current }}</span>
        <ChevronDown class="size-4 shrink-0" aria-hidden="true" />
      </summary>
      <ul role="listbox" :aria-labelledby="labelId" class="cp-card absolute left-0 z-20 mt-2 flex max-h-60 min-w-full w-max max-w-80 flex-col overflow-y-auto border border-base-300 bg-base-100 p-2 shadow-lg">
        <li v-for="option in options" :key="option.value" role="presentation">
          <button
            type="button"
            role="option"
            :aria-selected="option.value === value"
            class="flex w-full cursor-pointer items-center gap-3 rounded-field px-2 py-1 text-start hover:bg-base-200"
            :class="{ 'font-semibold': option.value === value }"
            @click="choose(option.value)"
          >
            {{ option.text }}
            <Check v-if="option.value === value" class="ms-auto size-4 shrink-0" aria-hidden="true" />
          </button>
        </li>
      </ul>
    </details>
  </div>
</template>
