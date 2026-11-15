<script setup lang="ts">
import { lucideIcon } from './icons'

// The small "i" that opens the details of what it sits next to, in a sheet. Its `aria-label` names that thing.
const info = lucideIcon('info')
</script>

<template>
  <button type="button" class="info-button" data-component="info-button" aria-haspopup="dialog"><component :is="info" :size="20" aria-hidden="true" /></button>
</template>
