<script setup lang="ts">
// A filter chip: on is primary, off is an outline. Chips are for filters and view switches;
// a form asks for a value with CpSelectField instead.
const pressed = defineModel<boolean>({ default: false })
</script>

<template>
  <button
    type="button"
    class="btn cp-chip"
    :class="pressed ? 'btn-primary' : 'btn-outline'"
    :aria-pressed="pressed"
    data-component="chip"
    @click="pressed = !pressed"
  >
    <slot />
  </button>
</template>
