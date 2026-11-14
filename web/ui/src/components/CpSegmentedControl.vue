<script setup lang="ts">
// A few values side by side, one of them on, such as the language an exercise's text is written in: small buttons
// joined in a row, the picked one filled. An option can be `flagged`: it carries a crimson icon with its `flagLabel`
// (read aloud), such as a language some text still lacks.
import { CircleExclamationPoint } from '@lucide/vue'

defineProps<{ label: string; options: { value: string; text: string; flagged?: boolean; flagLabel?: string }[] }>()
const value = defineModel<string>()
</script>

<template>
  <div class="cp-join" role="group" :aria-label="label" data-component="segmented-control">
    <button
      v-for="option in options"
      :key="option.value"
      type="button"
      class="cp-join-item cp-btn cp-btn-sm gap-1"
      :class="{ 'cp-btn-active': option.value === value }"
      :aria-current="option.value === value ? 'true' : undefined"
      @click="value = option.value"
    >
      {{ option.text }}
      <CircleExclamationPoint v-if="option.flagged" class="size-4 shrink-0 text-error" aria-hidden="true" />
      <span v-if="option.flagged && option.flagLabel" class="sr-only">{{ option.flagLabel }}</span>
    </button>
  </div>
</template>
