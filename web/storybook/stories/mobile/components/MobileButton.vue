<script setup lang="ts">
import { computed } from 'vue'
import { lucideIcon } from './icons'

// The apps' buttons: a rounded square, never a pill, on the websites' size scale (xs, sm, md, lg, xl, xxl; md when none
// is given). Kinds: primary (one per screen), secondary (outlined), tonal, grumpy (only the catch-up action).
// Always a plain button (`type="button"`): in a browser it neither follows a link nor submits a form, as on a phone.
// `style`, `id` and `disabled` go through to the element.
const props = withDefaults(defineProps<{ kind?: 'primary' | 'secondary' | 'tonal' | 'grumpy'; size?: 'xs' | 'sm' | 'md' | 'lg' | 'xl' | 'xxl'; icon?: string; iconSize?: number }>(), {
  kind: 'primary',
  size: 'md',
})
// The icon a button of each size carries, as on the websites (controlIcon in web/ui).
const icons = { xs: 12, sm: 16, md: 20, lg: 20, xl: 20, xxl: 24 }
const iconPx = computed(() => props.iconSize ?? icons[props.size])
</script>

<template>
  <button type="button" :class="`button button--${kind}`" :data-size="size" data-component="button">
    <span class="button-content"><component :is="lucideIcon(icon)" v-if="icon" :size="iconPx" aria-hidden="true" /><slot /></span>
  </button>
</template>
