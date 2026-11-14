<script setup lang="ts">
// Choosing any number of values from a long list. It is a button that opens a panel only while it is used: a
// search box narrows the list and each match is a checkbox. The button says how many are picked. Escape or a
// click elsewhere closes the panel.
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { ChevronDown } from '@lucide/vue'

const props = defineProps<{
  label: string
  placeholder: string
  options: { value: string; text: string }[]
  /** Said when the search matches no value. */
  noneLabel: string
}>()
const picked = defineModel<string[]>({ default: () => [] })
const needle = ref('')
const root = ref<HTMLDetailsElement>()

const shown = computed(() => {
  const text = needle.value.trim().toLowerCase()
  return props.options.filter((option) => option.text.toLowerCase().includes(text))
})
const toggle = (value: string, on: boolean) => (picked.value = on ? [...picked.value, value] : picked.value.filter((one) => one !== value))

const close = () => {
  if (root.value) root.value.open = false
}
const outside = (event: Event) => {
  if (root.value?.open && !root.value.contains(event.target as Node)) close()
}
const escape = (event: KeyboardEvent) => {
  if (event.key === 'Escape') close()
}
onMounted(() => {
  document.addEventListener('pointerdown', outside)
  document.addEventListener('keydown', escape)
})
onBeforeUnmount(() => {
  document.removeEventListener('pointerdown', outside)
  document.removeEventListener('keydown', escape)
})
</script>

<template>
  <details ref="root" class="relative" data-component="multi-select-field">
    <summary class="cp-btn cp-btn-sm cp-btn-outline list-none gap-1">
      {{ label }}
      <span v-if="picked.length" class="cp-badge cp-badge-neutral cp-badge-sm">{{ picked.length }}</span>
      <ChevronDown class="size-4 shrink-0" aria-hidden="true" />
    </summary>
    <div class="cp-card absolute left-0 z-20 mt-2 flex w-64 flex-col gap-2 border border-base-300 bg-base-100 p-2 shadow-lg">
      <input v-model="needle" type="search" class="cp-input cp-input-sm w-full" :placeholder="placeholder" :aria-label="placeholder" autocomplete="off" />
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
