<script setup lang="ts">
import { lucideIcon } from './icons'

// One achievement, a round token: earned in `primary` with its icon, or locked as a "?" so the next one stays a
// surprise. `name` names it for a screen reader.
defineProps<{ icon?: string; name: string; locked?: boolean; size?: 'sm' | 'lg' }>()
</script>

<template>
  <span :class="['achievement', size && `achievement--${size}`, { 'achievement--locked': locked }]" role="img" :aria-label="locked ? 'Locked achievement' : name" data-component="achievement">
    <component :is="lucideIcon(locked ? 'circle-question-mark' : icon ?? 'award')" :size="size === 'lg' ? 28 : 16" aria-hidden="true" />
  </span>
</template>
