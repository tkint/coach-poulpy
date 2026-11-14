<script setup lang="ts">
// A link to the whole component workshop, which is a static site under /storybook/ (web/storybook, ADR 0028)
// and so is not a page VitePress knows: withBase keeps it working under a preview's base path.
import { withBase } from 'vitepress'
</script>

<template>
  <a :href="withBase('/storybook/index.html')" target="_blank" rel="noopener"><slot /></a>
</template>
