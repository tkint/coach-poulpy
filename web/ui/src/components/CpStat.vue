<script setup lang="ts">
// One figure in CpStats. The accent colours the value primary for the number that matters most.
defineProps<{ title: string; value: string; description?: string; accent?: boolean }>()
</script>

<template>
  <div class="cp-stat" data-component="stat">
    <div class="cp-stat-title">{{ title }}</div>
    <div class="cp-stat-value" :class="accent ? 'text-primary' : ''">{{ value }}</div>
    <div v-if="description" class="cp-stat-desc">{{ description }}</div>
  </div>
</template>
