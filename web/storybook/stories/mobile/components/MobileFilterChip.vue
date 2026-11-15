<script setup lang="ts">
import { lucideIcon } from './icons'

// A chip that filters a list: a word, or just an icon that opens a picker. `have` when the filter is on and `count` says
// how many values are ticked. `aria-label`, `aria-pressed` and the rest go through to the button.
defineProps<{ icon?: string; have?: boolean; small?: boolean; count?: number }>()
</script>

<template>
  <button :class="['filter-chip', { 'filter-chip--have': have, 'filter-chip--small': small }]" data-component="filter-chip" type="button">
    <component :is="lucideIcon(icon)" v-if="icon" :size="20" aria-hidden="true" />
    <slot />
    <b v-if="count !== undefined" aria-hidden="true">{{ count }}</b>
  </button>
</template>
