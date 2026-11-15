<script setup lang="ts">
import { lucideIcon } from './icons'

// A calm note in the primary container: an icon and a sentence or two that explain what happens next.
withDefaults(defineProps<{ icon?: string; iconSize?: number }>(), { icon: 'info', iconSize: 22 })
</script>

<template>
  <section class="info-card" data-component="info-card">
    <component :is="lucideIcon(icon)" :size="iconSize" aria-hidden="true" />
    <p><slot /></p>
  </section>
</template>
