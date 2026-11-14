<script setup lang="ts">
// A checkbox with no visible label of its own, for a table row or a header that selects rows:
// the label is read by assistive technology, so it names what it selects. A form asks yes or no with a toggle instead.
defineProps<{ label: string; indeterminate?: boolean }>()
const checked = defineModel<boolean>({ default: false })
</script>

<template>
  <label class="cp-btn cp-btn-ghost cp-btn-square cp-btn-sm" data-component="checkbox">
    <input v-model="checked" type="checkbox" class="cp-checkbox" :aria-label="label" :indeterminate="indeterminate" />
  </label>
</template>
