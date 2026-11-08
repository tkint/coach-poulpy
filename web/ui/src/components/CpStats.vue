<script setup lang="ts">
// A row of CpStat figures: stacked on a phone, side by side from a medium screen.
</script>

<template>
  <div class="cp-stats cp-stats-vertical md:cp-stats-horizontal bg-base-100 shadow-sm" data-component="stats">
    <slot />
  </div>
</template>
