<script setup lang="ts">
// The design system's buttons: one primary per screen, tonal for secondary actions, outline for
// cancel (ghost for a quieter one, such as Cancel beside a Save), grumpy only for the catch-up action while Poulpy is on the ember side, and danger (crimson)
// for a deletion that can't be undone. The default size is the large one; `md` suits a card's actions.
// A button that waits for something (a save, a publication) is `loading`: it keeps its size and label's
// place, shows a spinner over it and ignores clicks until the answer comes.
withDefaults(
  defineProps<{
    variant?: 'primary' | 'tonal' | 'outline' | 'ghost' | 'grumpy' | 'danger'
    size?: 'md' | 'xl'
    type?: 'button' | 'submit' | 'reset'
    loading?: boolean
    disabled?: boolean
  }>(),
  { variant: 'primary', size: 'xl', type: 'button', loading: false, disabled: false },
)

const classes = { primary: 'cp-btn-primary', tonal: 'cp-btn-secondary', outline: 'cp-btn-outline', ghost: 'cp-btn-ghost', grumpy: 'cp-btn-accent', danger: 'cp-btn-error' }
</script>

<template>
  <button
    :type="type"
    class="cp-btn"
    :class="[classes[variant], { 'cp-btn-xl': size === 'xl', 'relative': loading }]"
    :style="loading ? { color: 'transparent' } : undefined"
    :disabled="disabled || loading"
    :aria-busy="loading || undefined"
    data-component="button"
  >
    <slot />
    <span v-if="loading" class="cp-loading cp-loading-spinner absolute inset-0 m-auto size-5" style="color: var(--btn-fg)" aria-hidden="true" />
  </button>
</template>
