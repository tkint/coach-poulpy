<script setup lang="ts">
// A row of CpStat figures: stacked on a phone, side by side from a medium screen. `vertical` keeps them stacked,
// for a narrow column beside other content.
defineProps<{ vertical?: boolean }>()
</script>

<template>
  <div class="cp-stats bg-base-100 shadow-sm" :class="vertical ? 'cp-stats-vertical' : 'cp-stats-vertical md:cp-stats-horizontal'" data-component="stats">
    <slot />
  </div>
</template>
