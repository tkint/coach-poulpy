<script setup lang="ts">
import { computed } from 'vue'
import { lucideIcon } from './icons'

// The apps' buttons (draft): a rounded square, never a pill, on the websites' size scale (xs, sm, md, lg, xl, xxl; md
// when none is given). Kinds: primary (one per screen), secondary (outlined), tonal, grumpy (only the catch-up action).
// Always a plain button (`type="button"`): in a browser it neither follows a link nor submits a form, as on a phone.
// `style`, `id` and `disabled` go through to the element.
// CLEANUP: `large` and the `small` kind are the pill buttons the agreed screens still draw; drop them, and the
// `*-button` classes in screens.css, when the screens move to `size` once this shape is agreed.
const props = withDefaults(defineProps<{ kind?: 'primary' | 'secondary' | 'tonal' | 'grumpy' | 'small'; size?: 'xs' | 'sm' | 'md' | 'lg' | 'xl' | 'xxl'; large?: boolean; icon?: string; iconSize?: number }>(), {
  kind: 'primary',
})
// The icon a button of each size carries, as on the websites (controlIcon in web/ui).
const icons = { xs: 12, sm: 16, md: 20, lg: 20, xl: 20, xxl: 24 }
const legacy = computed(() => !props.size)
const base = computed(() => (legacy.value ? (props.kind === 'small' ? 'small-button' : `${props.kind}-button`) : `button button--${props.kind}`))
</script>

<template>
  <button
    type="button"
    :class="[base, { [`${base}--large`]: legacy && large }]"
    :data-size="size"
    :data-component="legacy ? `${kind === 'small' ? 'primary' : kind}-button` : 'button'"
  >
    <span class="button-content"><component :is="lucideIcon(icon)" v-if="icon" :size="iconSize ?? icons[size ?? 'md']" aria-hidden="true" /><slot /></span>
  </button>
</template>
