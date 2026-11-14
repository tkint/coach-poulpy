<script setup lang="ts">
import { mobileIcons, type MobileIconName } from './icons'

// A row that opens a settings page: its icon (or Poulpy's mood picture), a title, one line of detail, a chevron.
defineProps<{ title: string; detail: string; icon?: MobileIconName; picture?: string }>()
</script>

<template>
  <a class="settings-row" href="#" data-component="settings-row">
    <img v-if="picture" :src="picture" width="44" height="44" alt="" aria-hidden="true" />
    <component :is="mobileIcons[icon]" v-else-if="icon" :size="22" aria-hidden="true" />
    <span><strong>{{ title }}</strong><small>{{ detail }}</small></span>
    <component :is="mobileIcons['chevron-right']" :size="22" aria-hidden="true" />
  </a>
</template>
