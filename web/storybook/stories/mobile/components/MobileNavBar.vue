<script setup lang="ts">
import { House, LibraryBig, UserRound } from '@lucide/vue'

// The bottom bar of the apps' main screens, one destination current: Home, Library and You. Live is the Record button
// of Home's top bar, History is Home's past feed, and Settings (behind the cog) and Statistics sit under You.
defineProps<{ active?: 'home' | 'library' | 'you' }>()

const items = [
  { key: 'home', label: 'Home', icon: House },
  { key: 'library', label: 'Library', icon: LibraryBig },
  { key: 'you', label: 'You', icon: UserRound },
] as const
</script>

<template>
  <nav class="nav-bar" aria-label="Main" data-component="nav-bar">
    <button type="button" v-for="item in items" :key="item.key" class="nav-item" :aria-current="active === item.key ? 'page' : undefined">
      <span><component :is="item.icon" :size="22" aria-hidden="true" /></span>{{ item.label }}
    </button>
  </nav>
</template>
