<script setup lang="ts">
// A one-line text field, such as an email: a label above, and the field in error when the form says
// so (the message itself is a CpErrorNote under the form). A field that is not an account's own
// (a search, a confirmation) sets `autocomplete="off"`, which also tells password managers to leave it
// alone: use `type="search"` for a search, never `email`, so none of them offers to fill it. A search
// field gets a search icon and keeps the height of the other fields of its form (CpSearchField is the
// 48 high pill for a list's own search).
import { useId } from 'vue'

withDefaults(defineProps<{ label: string; type?: 'text' | 'email' | 'search'; invalid?: boolean; autocomplete?: string }>(), { type: 'text' })
const value = defineModel<string>({ default: '' })
const id = useId()
</script>

<template>
  <div class="cp-fieldset" data-component="text-field">
    <label class="cp-type-label" :for="id">{{ label }}</label>
    <label class="cp-input w-full" :class="{ 'cp-input-error': invalid }">
      <svg v-if="type === 'search'" class="size-5 shrink-0" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><circle cx="11" cy="11" r="7" /><path d="m20 20-4-4" /></svg>
      <input :id="id" v-model="value" :type="type" class="grow" :autocomplete="autocomplete" :aria-invalid="invalid ? 'true' : undefined" />
    </label>
  </div>
</template>
