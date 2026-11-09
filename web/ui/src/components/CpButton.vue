<script setup lang="ts">
// The design system's buttons: one primary per screen, tonal for secondary actions, outline for
// cancel, grumpy only for the catch-up action while Poulpy is on the ember side, and danger (crimson)
// for a deletion that can't be undone. The default size is the large one; `md` suits a card's actions.
withDefaults(
  defineProps<{ variant?: 'primary' | 'tonal' | 'outline' | 'grumpy' | 'danger'; size?: 'md' | 'xl'; type?: 'button' | 'submit' | 'reset' }>(),
  { variant: 'primary', size: 'xl', type: 'button' },
)

const classes = { primary: 'cp-btn-primary', tonal: 'cp-btn-secondary', outline: 'cp-btn-outline', grumpy: 'cp-btn-accent', danger: 'cp-btn-error' }
</script>

<template>
  <button :type="type" class="cp-btn" :class="[classes[variant], { 'cp-btn-xl': size === 'xl' }]" data-component="button"><slot /></button>
</template>
