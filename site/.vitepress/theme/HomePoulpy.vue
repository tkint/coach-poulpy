<script setup lang="ts">
// Poulpy in 3D on the home page: the "Home" live story of the component workshop (a static site under /storybook/, ADR 0028),
// running his whole run from normal to annoyed to fuming and back. withBase keeps it working under a preview's base path.
import { withBase } from 'vitepress'

const src = withBase('/storybook/iframe.html?id=brand-live--home&viewMode=story')
</script>

<template>
  <iframe class="home-poulpy" :src="src" title="Poulpy getting angry, then calming down, in 3D" loading="lazy" />
</template>

<style scoped>
.home-poulpy { display: block; width: 100%; aspect-ratio: 4 / 3; border: 0; background: transparent; color-scheme: normal; }
</style>
