<script setup lang="ts">
// The design system's buttons: one primary per screen, tonal for secondary actions, outline for
// cancel (ghost for a quieter one, such as Cancel beside a Save), grumpy only for the catch-up action while Poulpy is on the ember side, and danger (crimson)
// for a deletion that can't be undone. `size` is the websites' scale (xs, sm, md, lg, xl, xxl; md when none is
// given): a screen's main action is `xl`, a card's actions `md`.
// A button that waits for something (a save, a publication) is `loading`: it keeps its size and label's
// place, shows a spinner over it and ignores clicks until the answer comes.
import { buttonSize, controlIcon, type CpSize } from './sizes'

withDefaults(
  defineProps<{
    variant?: 'primary' | 'tonal' | 'outline' | 'ghost' | 'grumpy' | 'danger'
    size?: CpSize
    type?: 'button' | 'submit' | 'reset'
    loading?: boolean
    disabled?: boolean
  }>(),
  { variant: 'primary', size: 'md', type: 'button', loading: false, disabled: false },
)

const classes = { primary: 'cp-btn-primary', tonal: 'cp-btn-secondary', outline: 'cp-btn-outline', ghost: 'cp-btn-ghost', grumpy: 'cp-btn-accent', danger: 'cp-btn-error' }
</script>

<template>
  <button
    :type="type"
    class="cp-btn"
    :class="[classes[variant], buttonSize[size], { 'relative': loading }]"
    :style="loading ? { color: 'transparent' } : undefined"
    :disabled="disabled || loading"
    :aria-busy="loading || undefined"
    data-component="button"
  >
    <slot />
    <span v-if="loading" class="cp-loading cp-loading-spinner absolute inset-0 m-auto" :class="`cp-icon-${controlIcon[size]}`" style="color: var(--btn-fg)" aria-hidden="true" />
  </button>
</template>
