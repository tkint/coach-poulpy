<script setup lang="ts">
import { lucideIcon } from './icons'

// A round button holding one icon. `filled` for one over a picture or a header, `floating` to sit at a corner.
// The accessible name (`aria-label`) and anything else go through to the link.
defineProps<{ icon: string; iconSize?: number; filled?: boolean; floating?: 'start' | 'end' }>()
</script>

<template>
  <a
    :class="['icon-button', { 'icon-button--filled': filled }, floating && `floating floating--${floating}`]"
    data-component="icon-button"
    href="#"
  >
    <component :is="lucideIcon(icon)" :size="iconSize ?? 20" aria-hidden="true" />
  </a>
</template>
