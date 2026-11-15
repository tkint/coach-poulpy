<script setup lang="ts">
// Two or three tabs that switch a page's view, one of them selected. Its `aria-label` names the group.
defineProps<{ tabs: string[]; selected: string }>()
</script>

<template>
  <div class="tabs" role="tablist" data-component="tabs">
    <button v-for="tab in tabs" :key="tab" type="button" role="tab" :aria-selected="tab === selected ? 'true' : 'false'">{{ tab }}</button>
  </div>
</template>
