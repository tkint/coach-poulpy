<script setup lang="ts">
// The design system's buttons: one primary per screen, tonal for secondary actions, outline for
// cancel, and grumpy only for the catch-up action while Poulpy is on the ember side.
withDefaults(defineProps<{ variant?: 'primary' | 'tonal' | 'outline' | 'grumpy'; type?: 'button' | 'submit' | 'reset' }>(), {
  variant: 'primary',
  type: 'button',
})

const classes = { primary: 'btn-primary', tonal: 'btn-secondary', outline: 'btn-outline', grumpy: 'btn-accent' }
</script>

<template>
  <button :type="type" class="btn cp-button" :class="classes[variant]" data-component="button"><slot /></button>
</template>
