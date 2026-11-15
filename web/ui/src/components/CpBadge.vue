<script setup lang="ts">
import CpIcon from './CpIcon.vue'
// A short label on a row: what kind of change, what state. Primary for the main kind, neutral for
// system or quiet ones, outline for a state that stays plain, success for a state that is fine, error
// for what went wrong (crimson, with its alert icon drawn in). Never ember, and not DaisyUI's warning.
import { CircleExclamationPoint } from '@lucide/vue'
// `small` is the badge under a field, such as where its translation stands: smaller, and an error draws no icon.
withDefaults(defineProps<{ tone?: 'primary' | 'neutral' | 'outline' | 'success' | 'error'; small?: boolean }>(), { tone: 'primary' })

const classes = {
  primary: 'cp-badge-primary cp-badge-soft',
  neutral: 'cp-badge-neutral cp-badge-soft',
  outline: 'cp-badge-outline',
  success: 'cp-badge-success cp-badge-soft',
  error: 'cp-badge-error cp-badge-soft gap-1',
}
</script>

<template>
  <span class="cp-badge whitespace-nowrap" :class="[classes[tone], { 'cp-badge-sm': small }]" data-component="badge">
    <CpIcon :icon="CircleExclamationPoint" size="xs" v-if="tone === 'error' && !small" />
    <slot />
  </span>
</template>
