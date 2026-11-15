<script setup lang="ts">
import { computed } from 'vue'
import { lucideIcon } from './icons'

// A Lucide icon by its Lucide name (ADR 0024), decorative: the words beside it, or its button's label, say what it means.
const props = withDefaults(defineProps<{ name: string; size?: number }>(), { size: 24 })
const icon = computed(() => lucideIcon(props.name))
</script>

<template>
  <component :is="icon" :size="size" aria-hidden="true" :data-lucide="name" />
</template>
