<script setup lang="ts">
import { lucideIcon } from './icons'

// A row that opens a page: its icon (or Poulpy's mood picture), a title, one line of detail, a chevron. Settings rows sit
// joined in a `settings-list`. With `card` the row stands alone as its own card, as in the Library. `note` adds a small
// line under the detail (`done` or `planned` colour it) and `start` ends the row with a play button, named by that label,
// in place of the chevron.
const props = defineProps<{ title: string; detail: string; icon?: string; picture?: string; card?: boolean; note?: string; noteTone?: 'done' | 'planned'; start?: string }>()
const chevron = lucideIcon('chevron-right')
const classes = ['settings-row', { 'settings-row--card': props.card || props.start }]
</script>

<template>
  <div v-if="start" :class="classes" data-component="settings-row">
    <component :is="lucideIcon(icon)" v-if="icon" :size="22" aria-hidden="true" />
    <button type="button" class="settings-row__main">
      <strong>{{ title }}</strong><small>{{ detail }}</small>
      <span v-if="note" :class="['row-note', noteTone && `row-note--${noteTone}`]">{{ note }}</span>
    </button>
    <button type="button" class="row-icon" :aria-label="start"><component :is="lucideIcon('play')" :size="20" aria-hidden="true" /></button>
  </div>
  <button v-else type="button" :class="classes" data-component="settings-row">
    <img v-if="picture" :src="picture" width="44" height="44" alt="" aria-hidden="true" />
    <component :is="lucideIcon(icon)" v-else-if="icon" :size="22" aria-hidden="true" />
    <span>
      <strong>{{ title }}</strong><small>{{ detail }}</small>
      <span v-if="note" :class="['row-note', noteTone && `row-note--${noteTone}`]">{{ note }}</span>
    </span>
    <component :is="chevron" :size="22" aria-hidden="true" />
  </button>
</template>
