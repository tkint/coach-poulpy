<script setup lang="ts">
// The search field: a pill with a search icon, primary while it holds text, and a round clear button.
defineProps<{ label: string; clearLabel: string; placeholder?: string }>()
const query = defineModel<string>({ default: '' })
</script>

<template>
  <label class="cp-input cp-search-field" data-component="search-field">
    <svg width="20" height="20" viewBox="0 0 24 24" aria-hidden="true" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round"><circle cx="11" cy="11" r="7" /><path d="M20 20l-4-4" /></svg>
    <input v-model="query" type="search" class="grow" :aria-label="label" :placeholder="placeholder ?? ' '" />
    <button v-if="query" type="button" class="cp-btn cp-btn-circle cp-btn-ghost cp-btn-xs" :aria-label="clearLabel" @click="query = ''">
      <svg width="16" height="16" viewBox="0 0 24 24" aria-hidden="true" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round"><path d="M6 6l12 12M18 6L6 18" /></svg>
    </button>
  </label>
</template>
