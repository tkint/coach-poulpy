<script setup lang="ts">
// Choosing any number of values in a form: the picked ones sit in the field as removable badges, and typing in it
// narrows the list its panel shows while it is used. A picked value is checked in the list; clicking it again, its
// badge's cross or Backspace in an empty field takes it out. Escape or a click elsewhere closes the panel. Enter
// picks the first match, so it never sends the form. `ariaLabel` names a field whose label is written beside it
// (a heading above, as the equipment's Required and Optional). `size` is the websites' scale (md when none is given);
// it is the field's least height, which grows as badges wrap. The field's own label points at the input by `for`:
// without it the browser hands a click on a badge to the first button inside, the first badge's cross.
import { computed, onBeforeUnmount, onMounted, ref, useId } from 'vue'
import { Check, ChevronDown, X } from '@lucide/vue'
import CpIcon from './CpIcon.vue'
import { buttonSize, controlChevron, inputSize, type CpSize } from './sizes'

const props = withDefaults(defineProps<{
  label?: string
  ariaLabel?: string
  hint?: string
  placeholder?: string
  options: { value: string; text: string }[]
  /** Said when what is typed matches no value. */
  noneLabel: string
  /** Names a badge's cross, such as "Remove Standing". */
  removeLabel: (text: string) => string
  size?: CpSize
}>(), { size: 'md' })
const picked = defineModel<string[]>({ default: () => [] })
const needle = ref('')
const open = ref(false)
const root = ref<HTMLElement>()
const input = ref<HTMLInputElement>()
const id = useId()

const textOf = (value: string) => props.options.find((option) => option.value === value)?.text ?? value
const shown = computed(() => {
  const text = needle.value.trim().toLowerCase()
  return props.options.filter((option) => option.text.toLowerCase().includes(text))
})
const toggle = (value: string) => (picked.value = picked.value.includes(value) ? picked.value.filter((one) => one !== value) : [...picked.value, value])
const remove = (value: string) => (picked.value = picked.value.filter((one) => one !== value))
const pickFirst = () => {
  if (shown.value.length && needle.value.trim()) {
    toggle(shown.value[0].value)
    needle.value = ''
  }
}
const backspace = () => {
  if (!needle.value && picked.value.length) picked.value = picked.value.slice(0, -1)
}

const outside = (event: Event) => {
  if (open.value && !root.value?.contains(event.target as Node)) open.value = false
}
const escape = (event: KeyboardEvent) => {
  if (event.key === 'Escape' && open.value) {
    open.value = false
    input.value?.focus()
  }
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
  <div ref="root" class="cp-fieldset relative" data-component="chip-select-field">
    <label v-if="label" class="cp-type-label" :for="id">{{ label }}</label>
    <label :for="id" class="cp-input relative h-auto min-h-10 w-full flex-wrap gap-1 py-1 pe-9" :class="inputSize[size]">
      <span v-for="value in picked" :key="value" class="cp-badge cp-badge-neutral cp-badge-soft gap-1 whitespace-nowrap">
        {{ textOf(value) }}
        <button type="button" class="cp-btn cp-btn-circle cp-btn-ghost" :class="buttonSize.xs" :aria-label="removeLabel(textOf(value))" @click.prevent="remove(value)">
          <CpIcon :icon="X" size="xs" />
        </button>
      </span>
      <input
        :id="id"
        ref="input"
        v-model="needle"
        type="text"
        class="grow basis-16"
        role="combobox"
        aria-autocomplete="list"
        :aria-expanded="open"
        :aria-controls="`${id}-list`"
        :aria-label="ariaLabel"
        :aria-describedby="hint ? `${id}-hint` : undefined"
        :placeholder="picked.length ? undefined : placeholder"
        autocomplete="off"
        @focus="open = true"
        @input="open = true"
        @keydown.enter.prevent="pickFirst"
        @keydown.backspace="backspace"
      />
      <CpIcon :icon="ChevronDown" :size="controlChevron[size]" class="pointer-events-none absolute end-3 top-1/2 -translate-y-1/2" />
    </label>
    <ul
      v-show="open"
      :id="`${id}-list`"
      role="listbox"
      aria-multiselectable="true"
      class="cp-card absolute left-0 top-full z-20 mt-1 flex max-h-60 w-full flex-col overflow-y-auto border border-base-300 bg-base-100 p-2 shadow-lg"
    >
      <li v-for="option in shown" :key="option.value" role="presentation">
        <button
          type="button"
          role="option"
          :aria-selected="picked.includes(option.value)"
          class="flex w-full cursor-pointer items-center gap-3 rounded-field px-2 py-1 text-start hover:bg-base-200"
          :class="{ 'font-semibold': picked.includes(option.value) }"
          @click="toggle(option.value)"
        >
          {{ option.text }}
          <CpIcon v-if="picked.includes(option.value)" :icon="Check" size="sm" class="ms-auto" />
        </button>
      </li>
      <li v-if="!shown.length" class="px-2 py-1 text-sm text-base-content/70">{{ noneLabel }}</li>
    </ul>
    <p v-if="hint" :id="`${id}-hint`" class="cp-label whitespace-normal">{{ hint }}</p>
  </div>
</template>
