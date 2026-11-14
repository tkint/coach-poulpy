<script setup lang="ts">
// A filter chip: on is primary, off is an outline. Chips are for filters and view switches;
// a form asks for a value with CpSelectField instead. With `compact` it is as high as the small controls of a filter bar.
defineProps<{ compact?: boolean }>()
const pressed = defineModel<boolean>({ default: false })
</script>

<template>
  <button
    type="button"
    class="cp-btn cp-chip"
    :class="[pressed ? 'cp-btn-primary' : 'cp-btn-outline', { 'cp-chip-compact': compact }]"
    :aria-pressed="pressed"
    data-component="chip"
    @click="pressed = !pressed"
  >
    <slot />
  </button>
</template>
