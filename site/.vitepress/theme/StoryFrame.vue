<script setup lang="ts">
// One story of the component workshop (web/storybook, ADR 0028), embedded from the Storybook build the docs site
// publishes under /storybook/. `story` is the story id Storybook shows in its address, such as fields-selectfield--open.
import { withBase } from 'vitepress'
import { computed } from 'vue'

const props = defineProps<{ story: string; height?: number }>()
const src = computed(() => withBase(`/storybook/iframe.html?id=${props.story}&viewMode=story`))
</script>

<template>
  <iframe class="story-frame" :src="src" :style="{ height: `${height ?? 220}px` }" :title="`Story ${story}`" loading="lazy" />
</template>

<style scoped>
.story-frame {
  width: 100%;
  border: 1px solid var(--vp-c-divider);
  border-radius: 8px;
  margin: 12px 0;
}
</style>
