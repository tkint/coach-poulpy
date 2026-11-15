<script setup lang="ts">
import CpIcon from './CpIcon.vue'
import { buttonSize, type CpSize } from './sizes'
// A few values side by side, one of them on, such as the language an exercise's text is written in: small buttons
// joined in a row, the picked one filled. An option can be `flagged`: it carries a crimson icon with its `flagLabel`
// (read aloud), such as a language some text still lacks. `size` is the websites' scale (md when none is given).
import { CircleExclamationPoint } from '@lucide/vue'

withDefaults(defineProps<{ label: string; options: { value: string; text: string; flagged?: boolean; flagLabel?: string }[]; size?: CpSize }>(), { size: 'md' })
const value = defineModel<string>()
</script>

<template>
  <div class="cp-join" role="group" :aria-label="label" data-component="segmented-control">
    <button
      v-for="option in options"
      :key="option.value"
      type="button"
      class="cp-join-item cp-btn gap-1"
      :class="[buttonSize[size], option.value === value ? 'cp-btn-primary' : 'cp-btn-outline']"
      :aria-current="option.value === value ? 'true' : undefined"
      @click="value = option.value"
    >
      {{ option.text }}
      <CpIcon :icon="CircleExclamationPoint" size="sm" v-if="option.flagged" class="text-error" />
      <span v-if="option.flagged && option.flagLabel" class="sr-only">{{ option.flagLabel }}</span>
    </button>
  </div>
</template>
