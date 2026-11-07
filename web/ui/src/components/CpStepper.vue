<script setup lang="ts">
// A stepper for sets, reps, load and rest: a label, the value, and minus and plus buttons.
// The value's text comes from the page, so units and translations stay out of this component.
const props = withDefaults(defineProps<{ label: string; valueText: string; lessLabel: string; moreLabel: string; step?: number; min?: number; max?: number }>(), {
  step: 1,
  min: 0,
  max: Number.POSITIVE_INFINITY,
})
const value = defineModel<number>({ required: true })
const change = (by: number) => (value.value = Math.min(props.max, Math.max(props.min, value.value + by)))
</script>

<template>
  <div class="cp-stepper" role="group" :aria-label="label" data-component="stepper">
    <span class="cp-label">{{ label }}</span>
    <div class="cp-stepper-row">
      <button type="button" class="btn btn-circle btn-outline" :aria-label="lessLabel" :disabled="value <= min" @click="change(-step)">−</button>
      <output class="cp-stepper-value" aria-live="polite">{{ valueText }}</output>
      <button type="button" class="btn btn-circle btn-outline" :aria-label="moreLabel" :disabled="value >= max" @click="change(step)">+</button>
    </div>
  </div>
</template>
