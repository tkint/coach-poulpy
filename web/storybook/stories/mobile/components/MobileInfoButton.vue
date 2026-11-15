<script setup lang="ts">
import { lucideIcon } from './icons'

// The small "i" that opens the details of what it sits next to, in a sheet. Its `aria-label` names that thing.
const info = lucideIcon('info')
</script>

<template>
  <a class="info-button" data-component="info-button" href="#" aria-haspopup="dialog"><component :is="info" :size="20" aria-hidden="true" /></a>
</template>
