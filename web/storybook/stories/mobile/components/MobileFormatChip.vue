<script setup lang="ts">
import { lucideIcon } from './icons'

// A small tag that names a thing's kind, such as a workout's format, with its icon.
defineProps<{ icon?: string }>()
</script>

<template>
  <span class="chip" data-component="chip"><component :is="lucideIcon(icon)" v-if="icon" :size="16" aria-hidden="true" /><slot /></span>
</template>
