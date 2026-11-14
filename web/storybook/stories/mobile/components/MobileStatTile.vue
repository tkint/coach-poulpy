<script setup lang="ts">
// A small figure under a caption: how many times, when last, how long.
defineProps<{ label: string; value: string }>()
</script>

<template>
  <div class="stat-tile" data-component="stat-tile"><span class="label">{{ label }}</span><strong>{{ value }}</strong></div>
</template>
