<script setup lang="ts">
import { Activity, CircleUser, House, LibraryBig, RotateCcwClock } from '@lucide/vue'

// The bottom bar of the apps' main screens: the five destinations, one of them current. "You" holds Stats and Settings in two tabs.
defineProps<{ active?: 'home' | 'library' | 'live' | 'history' | 'you' }>()

const items = [
  { key: 'home', label: 'Home', icon: House },
  { key: 'library', label: 'Library', icon: LibraryBig },
  { key: 'live', label: 'Live', icon: Activity },
  { key: 'history', label: 'History', icon: RotateCcwClock },
  { key: 'you', label: 'You', icon: CircleUser },
] as const
</script>

<template>
  <nav class="nav-bar" aria-label="Main" data-component="nav-bar">
    <button type="button" v-for="item in items" :key="item.key" class="nav-item" :aria-current="active === item.key ? 'page' : undefined">
      <span><component :is="item.icon" :size="22" aria-hidden="true" /></span>{{ item.label }}
    </button>
  </nav>
</template>
