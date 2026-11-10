<script setup lang="ts">
// The search field: a pill with a search icon, primary while it holds text, and a round clear button.
import { Search, X } from '@lucide/vue'
defineProps<{ label: string; clearLabel: string; placeholder?: string }>()
const query = defineModel<string>({ default: '' })
</script>

<template>
  <label class="cp-input cp-search-field" data-component="search-field">
    <Search :size="20" aria-hidden="true" />
    <input v-model="query" type="search" autocomplete="off" class="grow" :aria-label="label" :placeholder="placeholder ?? ' '" />
    <button v-if="query" type="button" class="cp-btn cp-btn-circle cp-btn-ghost cp-btn-xs" :aria-label="clearLabel" @click="query = ''">
      <X :size="16" aria-hidden="true" />
    </button>
  </label>
</template>
