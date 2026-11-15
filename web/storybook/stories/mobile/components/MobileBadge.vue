<script setup lang="ts">
import { lucideIcon } from './icons'

// One item in a row of badges that sums a workout up: its format (`format`, highlighted) or what it needs.
defineProps<{ icon: string; format?: boolean }>()
</script>

<template>
  <li :class="['badge', { 'badge--format': format }]" data-component="badge"><component :is="lucideIcon(icon)" :size="16" aria-hidden="true" /><slot /></li>
</template>
