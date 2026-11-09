<script setup lang="ts">
// A one-line text field, such as an email: a label above, and the field in error when the form says
// so (the message itself is a CpErrorNote under the form). A field that is not an account's own
// (a search, a confirmation) sets `autocomplete="off"`, which also tells password managers to leave it
// alone: use `type="search"` for a search, never `email`, so none of them offers to fill it.
import { useId } from 'vue'

withDefaults(defineProps<{ label: string; type?: 'text' | 'email' | 'search'; invalid?: boolean; autocomplete?: string }>(), { type: 'text' })
const value = defineModel<string>({ default: '' })
const id = useId()
</script>

<template>
  <div class="cp-fieldset" data-component="text-field">
    <label class="cp-type-label" :for="id">{{ label }}</label>
    <label class="cp-input w-full" :class="{ 'cp-input-error': invalid }">
      <input :id="id" v-model="value" :type="type" class="grow" :autocomplete="autocomplete" :aria-invalid="invalid ? 'true' : undefined"
        :data-1p-ignore="autocomplete === 'off' ? '' : undefined"
        :data-lpignore="autocomplete === 'off' ? 'true' : undefined"
        :data-bwignore="autocomplete === 'off' ? '' : undefined"
        :data-protonpass-ignore="autocomplete === 'off' ? 'true' : undefined"
        :data-form-type="autocomplete === 'off' ? 'other' : undefined"
      />
    </label>
  </div>
</template>
