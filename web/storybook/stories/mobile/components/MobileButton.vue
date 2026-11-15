<script setup lang="ts">
import { lucideIcon } from './icons'

// The apps' buttons: primary (one per screen), secondary, tonal, grumpy (only the catch-up action), or the small one
// that sits in a top bar. Always a plain button (`type="button"`): in a browser it neither follows a link nor submits a
// form, as on a phone. `style`, `id` and `disabled` go through to the element.
const props = withDefaults(defineProps<{ kind?: 'primary' | 'secondary' | 'tonal' | 'grumpy' | 'small'; large?: boolean; icon?: string; iconSize?: number }>(), {
  kind: 'primary',
  iconSize: 20,
})
const base = props.kind === 'small' ? 'small-button' : `${props.kind}-button`
</script>

<template>
  <button type="button" :class="[base, { [`${base}--large`]: large }]" :data-component="`${kind === 'small' ? 'primary' : kind}-button`">
    <span class="button-content"><component :is="lucideIcon(icon)" v-if="icon" :size="iconSize" aria-hidden="true" /><slot /></span>
  </button>
</template>
