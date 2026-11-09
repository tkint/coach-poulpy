<script setup lang="ts">
// Where a page sits: links to the pages above it, then the page itself (the last item, plain text).
// Put one `li` per step in the slot: `<li><a href="…">Workouts</a></li>`, the current page last.
defineProps<{ label: string }>()
</script>
<template>
  <nav class="cp-breadcrumbs text-sm" :aria-label="label" data-component="breadcrumbs">
    <ul><slot /></ul>
  </nav>
</template>
