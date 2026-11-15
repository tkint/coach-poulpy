<script setup lang="ts">
import { lucideIcon } from './icons'

// The apps' buttons: primary (one per screen), secondary, or the small one that sits in a top bar. A link by default;
// `tag="button"` for an action. Anything else (style, id, disabled) goes through to the element.
const props = withDefaults(defineProps<{ kind?: 'primary' | 'secondary' | 'small'; large?: boolean; icon?: string; iconSize?: number; tag?: 'a' | 'button' }>(), {
  kind: 'primary',
  iconSize: 20,
  tag: 'a',
})
const base = props.kind === 'small' ? 'small-button' : `${props.kind}-button`
</script>

<template>
  <component
    :is="tag"
    :class="[base, { [`${base}--large`]: large }]"
    :data-component="`${kind === 'secondary' ? 'secondary' : 'primary'}-button`"
    :href="tag === 'a' ? '#' : undefined"
    :type="tag === 'button' ? 'button' : undefined"
  >
    <component :is="lucideIcon(icon)" v-if="icon" :size="iconSize" aria-hidden="true" />
    <slot />
  </component>
</template>
