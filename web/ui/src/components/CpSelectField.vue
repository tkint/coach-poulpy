<script setup lang="ts">
// Choosing one value in a form: a native select, never a row of pills. With `compact` it is one control of a
// filter bar: no visible label (the label still names it for assistive technology, and its first option usually
// says it too) and only as wide as its longest option.
defineProps<{ label: string; options: { value: string; text: string }[]; compact?: boolean }>()
const value = defineModel<string>()
</script>

<template>
  <label class="cp-fieldset" data-component="select-field">
    <span class="cp-type-label" :class="{ 'sr-only': compact }">{{ label }}</span>
    <select v-model="value" class="cp-select" :class="compact ? 'cp-select-sm w-auto' : 'w-full'">
      <option v-for="option in options" :key="option.value" :value="option.value">{{ option.text }}</option>
    </select>
  </label>
</template>
