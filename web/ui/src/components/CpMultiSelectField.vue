<script setup lang="ts">
// Choosing any number of values from a long list: a search box narrows the list, each match is a checkbox,
// and what is picked stays on show as chips that take their value back off.
import { computed, ref, useId } from 'vue'
import { X } from '@lucide/vue'

const props = defineProps<{
  label: string
  placeholder: string
  options: { value: string; text: string }[]
  /** Names the button that takes one picked value off; gets the value's text. */
  removeLabel: (text: string) => string
  /** Said when the search matches no value. */
  noneLabel: string
}>()
const picked = defineModel<string[]>({ default: () => [] })
const needle = ref('')
const id = useId()

const shown = computed(() => {
  const text = needle.value.trim().toLowerCase()
  return props.options.filter((option) => option.text.toLowerCase().includes(text))
})
const textOf = (value: string) => props.options.find((option) => option.value === value)?.text ?? value
const toggle = (value: string, on: boolean) => (picked.value = on ? [...picked.value, value] : picked.value.filter((one) => one !== value))
</script>

<template>
  <div class="cp-fieldset" data-component="multi-select-field">
    <label class="cp-type-label" :for="id">{{ label }}</label>
    <div v-if="picked.length" class="flex flex-wrap gap-1">
      <button v-for="value in picked" :key="value" type="button" class="cp-badge cp-badge-neutral cp-badge-soft cp-btn-ghost gap-1" :aria-label="removeLabel(textOf(value))" @click="toggle(value, false)">
        {{ textOf(value) }}
        <X class="size-3 shrink-0" aria-hidden="true" />
      </button>
    </div>
    <input :id="id" v-model="needle" type="search" class="cp-input w-full" :placeholder="placeholder" autocomplete="off" />
    <ul class="flex max-h-40 flex-col overflow-y-auto rounded-box border border-base-300 bg-base-100 p-1">
      <li v-for="option in shown" :key="option.value">
        <label class="flex cursor-pointer items-center gap-3 rounded-field px-2 py-1 hover:bg-base-200">
          <input type="checkbox" class="cp-checkbox cp-checkbox-sm" :checked="picked.includes(option.value)" @change="toggle(option.value, ($event.target as HTMLInputElement).checked)" />
          {{ option.text }}
        </label>
      </li>
      <li v-if="!shown.length" class="px-2 py-1 text-sm text-base-content/70">{{ noneLabel }}</li>
    </ul>
  </div>
</template>
