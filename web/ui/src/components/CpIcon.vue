<script setup lang="ts">
// A Lucide icon at one of the scale's sizes (xs 12, sm 16, md 20, lg 24, xl 32, xxl 40), md when none is given.
// Pass the Lucide component itself: `<CpIcon :icon="Search" size="sm" />`. It is always decorative: a control
// that has only an icon carries its own label.
import type { Component } from 'vue'
import type { CpSize } from './sizes'

withDefaults(defineProps<{ icon: Component; size?: CpSize }>(), { size: 'md' })
</script>

<template>
  <component :is="icon" class="shrink-0" :class="`cp-icon-${size}`" aria-hidden="true" data-component="icon" />
</template>
