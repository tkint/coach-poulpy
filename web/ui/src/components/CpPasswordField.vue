<script setup lang="ts">
// A password: a field with a show/hide button, a hint under it, and the field in error when the
// form says so (the message itself is a CpErrorNote under the form).
import { Eye, EyeOff } from '@lucide/vue'
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
        <EyeOff v-if="shown" :size="22" aria-hidden="true" />
        <Eye v-else :size="22" aria-hidden="true" />
      </button>
    </label>
    <p v-if="hint" :id="`${id}-hint`" class="cp-label whitespace-normal">{{ hint }}</p>
  </div>
</template>
