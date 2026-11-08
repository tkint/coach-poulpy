<script setup lang="ts">
// A one-line text field, such as an email: a label above, and the field in error when the form says
// so (the message itself is a CpErrorNote under the form).
import { useId } from 'vue'

withDefaults(defineProps<{ label: string; type?: 'text' | 'email'; invalid?: boolean; autocomplete?: string }>(), { type: 'text' })
const value = defineModel<string>({ default: '' })
const id = useId()
</script>

<template>
  <div class="cp-fieldset" data-component="text-field">
    <label class="cp-type-label" :for="id">{{ label }}</label>
    <label class="cp-input w-full" :class="{ 'cp-input-error': invalid }">
      <input :id="id" v-model="value" :type="type" class="grow" :autocomplete="autocomplete" :aria-invalid="invalid ? 'true' : undefined" />
    </label>
  </div>
</template>
