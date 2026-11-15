<script setup lang="ts">
import { lucideIcon } from './icons'

// A search box: a magnifier, the field and, once something is typed (`value`), a button that clears it.
// `label` is the field's accessible name.
defineProps<{ label: string; placeholder: string; value?: string }>()
const search = lucideIcon('search')
const clear = lucideIcon('x')
</script>

<template>
  <label :class="['search-field', { 'search-field--filled': value }]" data-component="search-field">
    <component :is="search" :size="20" aria-hidden="true" />
    <input type="search" :value="value" :placeholder="placeholder" :aria-label="label" />
    <button v-if="value" class="clear-button" type="button" aria-label="Clear search"><component :is="clear" :size="16" aria-hidden="true" /></button>
  </label>
</template>
