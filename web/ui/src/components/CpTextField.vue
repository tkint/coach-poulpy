<script setup lang="ts">
// A one-line text field, such as an email (the `icon` slot, before the text, takes an icon such as a lock; the `status` slot, under the hint, takes a badge saying where a translated text stands): a label above, a hint under it, and the field in error when the
// form says so. The message is a CpErrorNote under the form, or, for a field checked on its own (an address
// that isn't one), `error`: it takes the hint's place, in crimson with its icon. A field that is not an account's own
// (a search, a confirmation, a setting) sets `autocomplete="off"`, which also tells password managers to leave
// it alone (`disabled` for a value that can no longer change, such as a published slug): use `type="search"` for a search, never `email`, so none of them offers to fill it. A search
// field gets a search icon and keeps the height of the other fields of its form (CpSearchField is the
// 48 high pill for a list's own search).
import { CircleExclamationPoint, Search } from '@lucide/vue'
import { computed, useId } from 'vue'

const props = withDefaults(
  defineProps<{ label: string; type?: 'text' | 'email' | 'search'; invalid?: boolean; autocomplete?: string; hint?: string; error?: string; disabled?: boolean; placeholder?: string }>(),
  { type: 'text' },
)
const value = defineModel<string>({ default: '' })
const id = useId()
const note = computed(() => props.error || props.hint)
</script>

<template>
  <div class="cp-fieldset" data-component="text-field">
    <label class="cp-type-label" :for="id">{{ label }}</label>
    <label class="cp-input w-full" :class="{ 'cp-input-error': invalid || !!error }">
      <Search v-if="type === 'search'" class="size-5 shrink-0" aria-hidden="true" />
      <slot name="icon" />
      <input
        :id="id"
        v-model="value"
        :type="type"
        class="grow"
        :autocomplete="autocomplete"
        :disabled="disabled"
        :placeholder="placeholder"
        :aria-invalid="invalid || error ? 'true' : undefined"
        :aria-describedby="note ? `${id}-note` : undefined"
      />
    </label>
    <p v-if="error" :id="`${id}-note`" class="cp-label flex items-center gap-1 whitespace-normal text-error" role="alert">
      <CircleExclamationPoint class="size-4 shrink-0" aria-hidden="true" />{{ error }}
    </p>
    <p v-else-if="hint" :id="`${id}-note`" class="cp-label whitespace-normal">{{ hint }}</p>
    <slot name="status" />
  </div>
</template>
