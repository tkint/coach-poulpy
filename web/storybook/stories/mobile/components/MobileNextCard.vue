<script setup lang="ts">
// What comes after the current step in the player: a `label` and the step (the content) on one line, or, with a
// `detail`, stacked as a headline over its numbers.
defineProps<{ label: string; detail?: string }>()
</script>

<template>
  <div :class="['next-card', { 'next-card--stacked': detail }]" data-component="next-card">
    <span class="label">{{ label }}</span>
    <span v-if="detail" class="headline"><slot /></span><span v-else><slot /></span>
    <span v-if="detail" class="muted numbers">{{ detail }}</span>
  </div>
</template>
