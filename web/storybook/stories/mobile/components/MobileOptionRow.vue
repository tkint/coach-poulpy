<script setup lang="ts">
import { lucideIcon } from './icons'

// One choice in a sheet's list. With a `control` it is a label around a radio or checkbox the person ticks; without
// one it is a button that acts at once. Its content is the choice's name; an `icon` or a `picture` leads it, and a
// `hint` adds a line under the name.
defineProps<{
  control?: 'radio' | 'checkbox'
  name?: string
  value?: string
  checked?: boolean
  icon?: string
  picture?: string
  hint?: string
}>()
</script>

<template>
  <component :is="control ? 'label' : 'button'" :type="control ? undefined : 'button'" class="option-row" data-component="option-row">
    <img v-if="picture" :src="picture" width="48" height="48" alt="" aria-hidden="true" />
    <component :is="lucideIcon(icon)" v-else-if="icon" :size="22" aria-hidden="true" />
    <span><slot /><small v-if="hint" class="hint">{{ hint }}</small></span>
    <input v-if="control" :type="control" :name="name" :value="value" :checked="checked" />
  </component>
</template>
