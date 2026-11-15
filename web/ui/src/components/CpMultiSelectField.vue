<script setup lang="ts">
// Choosing any number of values from a long list. It is a button that opens a panel only while it is used: a
// search box narrows the list and each match is a checkbox. The button says how many are picked. Escape or a
// click elsewhere closes the panel. `size` is the websites' scale (md when none is given; a filter bar's are `sm`).
import { computed, ref } from 'vue'
import { ChevronDown } from '@lucide/vue'
import { useDismissable } from '../dismissable'
import CpIcon from './CpIcon.vue'
import { buttonSize, controlChevron, inputSize, type CpSize } from './sizes'

const props = withDefaults(defineProps<{
  label: string
  placeholder: string
  options: { value: string; text: string }[]
  /** Said when the search matches no value. */
  noneLabel: string
  size?: CpSize
}>(), { size: 'md' })
const picked = defineModel<string[]>({ default: () => [] })
const needle = ref('')
const root = ref<HTMLDetailsElement>()

const shown = computed(() => {
  const text = needle.value.trim().toLowerCase()
  return props.options.filter((option) => option.text.toLowerCase().includes(text))
})
const toggle = (value: string, on: boolean) => (picked.value = on ? [...picked.value, value] : picked.value.filter((one) => one !== value))

useDismissable(root)
</script>

<template>
  <details ref="root" class="relative" data-component="multi-select-field">
    <summary class="cp-btn cp-btn-outline list-none gap-1" :class="buttonSize[size]">
      {{ label }}
      <span v-if="picked.length" class="cp-badge cp-badge-neutral cp-badge-sm">{{ picked.length }}</span>
      <CpIcon :icon="ChevronDown" :size="controlChevron[size]" />
    </summary>
    <div class="cp-card absolute left-0 z-20 mt-2 flex w-64 flex-col gap-2 border border-base-300 bg-base-100 p-2 shadow-lg">
      <input v-model="needle" type="search" class="cp-input w-full" :class="inputSize[size]" :placeholder="placeholder" :aria-label="placeholder" autocomplete="off" />
      <ul class="flex max-h-40 flex-col overflow-y-auto">
        <li v-for="option in shown" :key="option.value">
          <label class="flex cursor-pointer items-center gap-3 rounded-field px-2 py-1 hover:bg-base-200">
            <input type="checkbox" class="cp-checkbox cp-checkbox-sm" :checked="picked.includes(option.value)" @change="toggle(option.value, ($event.target as HTMLInputElement).checked)" />
            {{ option.text }}
          </label>
        </li>
        <li v-if="!shown.length" class="px-2 py-1 text-sm text-base-content/70">{{ noneLabel }}</li>
      </ul>
    </div>
  </details>
</template>
