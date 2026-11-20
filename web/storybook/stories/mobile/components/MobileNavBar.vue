<script setup lang="ts">
import { computed } from 'vue'
import { Activity, House, LibraryBig, RotateCcwClock, Settings, UserRound } from '@lucide/vue'

// The bottom bar of the apps' main screens, one destination current. `three` (draft) is the new bar: Home, Library
// and You; Live becomes the Record button of Home's top bar, History is Home's past feed, and Settings (behind the cog)
// and Statistics move under You. CLEANUP: drop the five-destination bar and this prop once the screens
// have moved over.
const props = defineProps<{ active?: 'home' | 'library' | 'live' | 'history' | 'settings' | 'you'; destinations?: 'five' | 'three' }>()

const five = [
  { key: 'home', label: 'Home', icon: House },
  { key: 'library', label: 'Library', icon: LibraryBig },
  { key: 'live', label: 'Live', icon: Activity },
  { key: 'history', label: 'History', icon: RotateCcwClock },
  { key: 'settings', label: 'Settings', icon: Settings },
] as const
const three = [five[0], five[1], { key: 'you', label: 'You', icon: UserRound }] as const
const items = computed(() => (props.destinations === 'three' ? three : five))
</script>

<template>
  <nav :class="['nav-bar', { 'nav-bar--three': destinations === 'three' }]" aria-label="Main" data-component="nav-bar">
    <button type="button" v-for="item in items" :key="item.key" class="nav-item" :aria-current="active === item.key ? 'page' : undefined">
      <span><component :is="item.icon" :size="22" aria-hidden="true" /></span>{{ item.label }}
    </button>
  </nav>
</template>
