<script setup lang="ts">
import { Activity, House, LibraryBig, RotateCcwClock, Settings } from '@lucide/vue'

// The bottom bar of the apps' main screens: the five destinations, one of them current.
defineProps<{ active?: 'home' | 'library' | 'live' | 'history' | 'settings' }>()

const items = [
  { key: 'home', label: 'Home', icon: House },
  { key: 'library', label: 'Library', icon: LibraryBig },
  { key: 'live', label: 'Live', icon: Activity },
  { key: 'history', label: 'History', icon: RotateCcwClock },
  { key: 'settings', label: 'Settings', icon: Settings },
] as const
</script>

<template>
  <nav class="nav-bar" aria-label="Main" data-component="nav-bar">
    <a v-for="item in items" :key="item.key" class="nav-item" href="#" :aria-current="active === item.key ? 'page' : undefined">
      <span><component :is="item.icon" :size="22" aria-hidden="true" /></span>{{ item.label }}
    </a>
  </nav>
</template>
