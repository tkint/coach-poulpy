<script setup lang="ts">
// One entry of CpNavMenu. Put a Lucide icon (size-5) before the label; the current page is active.
defineProps<{ active?: boolean }>()
</script>

<template>
  <li data-component="nav-item">
    <a :class="active ? 'cp-menu-active' : ''" :aria-current="active ? 'page' : undefined"><slot /></a>
  </li>
</template>
