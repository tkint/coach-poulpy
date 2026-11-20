<script setup lang="ts">
import { ChevronRight } from '@lucide/vue'

// Home's one motivation row (draft): a small Poulpy, Poulpy's eight tentacles as eight segments with `lit` of them
// filled, and the latest achievements in the default slot (`MobileAchievement`). One tap opens Achievements.
// Never a streak: a lit tentacle never goes dark. `label` says the count in words.
defineProps<{ lit: number; label: string; picture: string; pictureAlt?: string }>()
</script>

<template>
  <button type="button" class="motivation-row" data-component="motivation-row" :aria-label="label">
    <img :src="picture" :alt="pictureAlt ?? ''" width="36" height="32" />
    <span class="segments" aria-hidden="true"><i v-for="index in 8" :key="index" :class="{ lit: index <= lit }"></i></span>
    <span class="achievements"><slot /></span>
    <ChevronRight :size="20" aria-hidden="true" />
  </button>
</template>
