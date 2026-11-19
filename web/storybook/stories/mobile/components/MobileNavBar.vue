<script setup lang="ts">
import { computed } from 'vue'
import { Activity, CircleUser, House, LibraryBig, RotateCcwClock, Settings } from '@lucide/vue'

// The bottom bar of the apps' main screens: the five destinations, one of them current.
// `you` draws the last destination as "You" (Settings and Stats in two tabs), the list-first History draft of #511.
// CLEANUP: once the owner agrees it, make "You" the only last destination and drop `settings` and this prop (#511).
const props = defineProps<{ active?: 'home' | 'library' | 'live' | 'history' | 'settings' | 'you'; you?: boolean }>()

const base = [
  { key: 'home', label: 'Home', icon: House },
  { key: 'library', label: 'Library', icon: LibraryBig },
  { key: 'live', label: 'Live', icon: Activity },
  { key: 'history', label: 'History', icon: RotateCcwClock },
  { key: 'settings', label: 'Settings', icon: Settings },
] as const
const items = computed(() => (props.you || props.active === 'you' ? [...base.slice(0, 4), { key: 'you', label: 'You', icon: CircleUser }] : base))
</script>

<template>
  <nav class="nav-bar" aria-label="Main" data-component="nav-bar">
    <button type="button" v-for="item in items" :key="item.key" class="nav-item" :aria-current="active === item.key ? 'page' : undefined">
      <span><component :is="item.icon" :size="22" aria-hidden="true" /></span>{{ item.label }}
    </button>
  </nav>
</template>
