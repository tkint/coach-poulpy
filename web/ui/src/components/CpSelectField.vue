<script setup lang="ts">
// Choosing one value in a form: a native select, never a row of pills.
defineProps<{ label: string; options: { value: string; text: string }[] }>()
const value = defineModel<string>()
</script>

<template>
  <label class="cp-fieldset" data-component="select-field">
    <span class="cp-type-label">{{ label }}</span>
    <select v-model="value" class="cp-select w-full">
      <option v-for="option in options" :key="option.value" :value="option.value">{{ option.text }}</option>
    </select>
  </label>
</template>
