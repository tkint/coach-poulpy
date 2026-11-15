<script setup lang="ts">
// A password: a field with a show/hide button, a hint under it, and the field in error when the
// form says so (the message itself is a CpErrorNote under the form). `size` is the websites' scale (md when none is given).
import { Eye, EyeOff } from '@lucide/vue'
import { ref, useId } from 'vue'
import { uiText } from '../i18n'
import CpIcon from './CpIcon.vue'
import { buttonSize, controlIcon, inputSize, insideButton, type CpSize } from './sizes'

const { size = 'md' } = defineProps<{ label: string; hint?: string; showLabel?: string; hideLabel?: string; invalid?: boolean; autocomplete?: string; size?: CpSize }>()
const value = defineModel<string>({ default: '' })
const shown = ref(false)
const id = useId()
</script>

<template>
  <div class="cp-fieldset" data-component="password-field">
    <label class="cp-type-label" :for="id">{{ label }}</label>
    <label class="cp-input w-full" :class="[inputSize[size], { 'cp-input-error': invalid }]">
      <input
        :id="id"
        v-model="value"
        :type="shown ? 'text' : 'password'"
        class="grow"
        :autocomplete="autocomplete ?? 'current-password'"
        :aria-describedby="hint ? `${id}-hint` : undefined"
        :aria-invalid="invalid ? 'true' : undefined"
      />
      <button type="button" class="cp-btn cp-btn-circle cp-btn-ghost" :class="buttonSize[insideButton[size]]" :aria-label="shown ? (hideLabel ?? uiText('ui.hidePassword')) : (showLabel ?? uiText('ui.showPassword'))" :aria-pressed="shown" @click="shown = !shown">
        <CpIcon v-if="shown" :icon="EyeOff" :size="controlIcon[insideButton[size]]" />
        <CpIcon v-else :icon="Eye" :size="controlIcon[insideButton[size]]" />
      </button>
    </label>
    <p v-if="hint" :id="`${id}-hint`" class="cp-label whitespace-normal">{{ hint }}</p>
  </div>
</template>
