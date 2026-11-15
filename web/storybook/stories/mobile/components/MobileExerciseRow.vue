<script setup lang="ts">
import { lucideIcon } from './icons'
import MobileInfoButton from './MobileInfoButton.vue'

// One exercise in a list: its pattern icon, its name and one line of detail (muscles and equipment, or sets and load).
// `link` makes the whole row open the exercise, with a chevron. Otherwise it is a line of a workout or a recap, ending
// with its `time` or, with `info`, the button that opens its details.
const props = defineProps<{ icon: string; name: string; detail: string; link?: boolean; time?: string; info?: boolean }>()
const chevron = lucideIcon('chevron-right')
</script>

<template>
  <button type="button" v-if="link" class="list-row" data-component="exercise-row" style="min-height: 56px; padding-right: var(--cp-space-md);">
    <span class="row-icon" aria-hidden="true"><component :is="lucideIcon(icon)" :size="22" aria-hidden="true" /></span>
    <span class="row-text"><strong>{{ name }}</strong><span>{{ detail }}</span></span>
    <span class="chevron"><component :is="chevron" :size="20" aria-hidden="true" /></span>
  </button>
  <div v-else class="list-row recap-row" data-component="exercise-row">
    <span class="row-icon" aria-hidden="true"><component :is="lucideIcon(icon)" :size="22" aria-hidden="true" /></span>
    <span class="row-text"><strong>{{ name }}</strong><span>{{ detail }}</span></span>
    <span v-if="time" class="row-time">{{ time }}</span>
    <MobileInfoButton v-if="info" :aria-label="`Details of ${props.name}`" />
  </div>
</template>
