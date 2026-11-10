<script setup lang="ts">
// A short label on a row: what kind of change, what state. Primary for the main kind, neutral for
// system or quiet ones, outline for a state that stays plain, success for a state that is fine, error
// for what went wrong (crimson, with its alert icon drawn in). Never ember, and not DaisyUI's warning.
withDefaults(defineProps<{ tone?: 'primary' | 'neutral' | 'outline' | 'success' | 'error' }>(), { tone: 'primary' })

const classes = {
  primary: 'cp-badge-primary cp-badge-soft',
  neutral: 'cp-badge-neutral cp-badge-soft',
  outline: 'cp-badge-outline',
  success: 'cp-badge-success cp-badge-soft',
  error: 'cp-badge-error cp-badge-soft gap-1',
}
</script>

<template>
  <span class="cp-badge whitespace-nowrap" :class="classes[tone]" data-component="badge">
    <svg v-if="tone === 'error'" class="size-3 shrink-0" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><circle cx="12" cy="12" r="9" /><path d="M12 7v6M12 16.5v.5" /></svg>
    <slot />
  </span>
</template>
