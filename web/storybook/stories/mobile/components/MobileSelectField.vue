<script setup lang="ts">
import { computed } from 'vue'
import { lucideIcon } from './icons'

// A field that shows its value and opens a choice: a list over the screen (`opens="list"`, a chevron down) or a page
// or sheet of its own (the default, a chevron right). With no `value` it shows a muted "Not set".
const props = withDefaults(defineProps<{ label: string; value?: string; opens?: 'list' | 'page' }>(), { opens: 'page' })
const chevron = computed(() => lucideIcon(props.opens === 'list' ? 'chevron-down' : 'chevron-right'))
</script>

<template>
  <button
    type="button"
    class="select-field"
    data-component="select-field"
    :aria-haspopup="opens === 'list' ? 'listbox' : undefined"
  >
    <span><span class="label">{{ label }}</span><strong v-if="value">{{ value }}</strong><strong v-else class="muted">Not set</strong></span>
    <component :is="chevron" :size="20" aria-hidden="true" />
  </button>
</template>
