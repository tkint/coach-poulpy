<script setup lang="ts">
// A password: a field with a show/hide button, a hint under it, and the field in error when the
// form says so (the message itself is a CpErrorNote under the form).
import { ref, useId } from 'vue'

defineProps<{ label: string; hint?: string; showLabel: string; hideLabel: string; invalid?: boolean; autocomplete?: string }>()
const value = defineModel<string>({ default: '' })
const shown = ref(false)
const id = useId()
</script>

<template>
  <div class="cp-fieldset" data-component="password-field">
    <label class="cp-type-label" :for="id">{{ label }}</label>
    <label class="cp-input w-full" :class="{ 'cp-input-error': invalid }">
      <input
        :id="id"
        v-model="value"
        :type="shown ? 'text' : 'password'"
        class="grow"
        :autocomplete="autocomplete ?? 'current-password'"
        :aria-describedby="hint ? `${id}-hint` : undefined"
        :aria-invalid="invalid ? 'true' : undefined"
      />
      <button type="button" class="cp-btn cp-btn-circle cp-btn-ghost cp-btn-sm" :aria-label="shown ? hideLabel : showLabel" :aria-pressed="shown" @click="shown = !shown">
        <svg v-if="shown" width="22" height="22" viewBox="0 0 24 24" aria-hidden="true" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10.733 5.076a10.744 10.744 0 0 1 11.205 6.575 1 1 0 0 1 0 .696 10.747 10.747 0 0 1-1.444 2.49" /><path d="M14.084 14.158a3 3 0 0 1-4.242-4.242" /><path d="M17.479 17.499a10.75 10.75 0 0 1-15.417-5.151 1 1 0 0 1 0-.696 10.75 10.75 0 0 1 4.446-5.143" /><path d="m2 2 20 20" /></svg>
        <svg v-else width="22" height="22" viewBox="0 0 24 24" aria-hidden="true" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2.062 12.348a1 1 0 0 1 0-.696 10.75 10.75 0 0 1 19.876 0 1 1 0 0 1 0 .696 10.75 10.75 0 0 1-19.876 0" /><circle cx="12" cy="12" r="3" /></svg>
      </button>
    </label>
    <p v-if="hint" :id="`${id}-hint`" class="cp-label whitespace-normal">{{ hint }}</p>
  </div>
</template>
