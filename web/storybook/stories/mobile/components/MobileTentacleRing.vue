<script setup lang="ts">
import { computed } from 'vue'

// Poulpy's eight tentacles as a ring: `lit` of them in primary, the last one lit brighter when it is `fresh` (just lit
// by the session that ended), and a `picture` of Poulpy inside. `label` says the count in words. The large ring (104)
// sits on Home (a `MobilePoulpyTap` can go in its slot instead of the picture); the small one (64) on a finished session.
const props = withDefaults(
  defineProps<{ lit: number; label: string; fresh?: boolean; size?: 104 | 64; picture?: string; pictureAlt?: string }>(),
  { size: 104, pictureAlt: '' },
)
const arcs = computed(() => {
  const [radius, from, to, digits] = props.size === 104 ? [92.5, 11, 34, 2] : [86, 12, 33, 1]
  const at = (degrees: number) => {
    const angle = (degrees * Math.PI) / 180
    return `${(100 + radius * Math.sin(angle)).toFixed(digits)} ${(100 - radius * Math.cos(angle)).toFixed(digits)}`
  }
  return Array.from({ length: 8 }, (_, index) => ({
    d: `M${at(index * 45 + from)} A${radius} ${radius} 0 0 1 ${at(index * 45 + to)}`,
    lit: index < props.lit,
    fresh: props.fresh && index === props.lit - 1,
  }))
})
</script>

<template>
  <div class="tentacle-ring" :style="size === 104 ? undefined : { width: `${size}px`, height: `${size}px` }">
    <svg viewBox="0 0 200 200" :width="size" :height="size" role="img" :aria-label="label">
      <path v-for="(arc, index) in arcs" :key="index" :class="{ lit: arc.lit, new: arc.fresh }" :d="arc.d" />
    </svg>
    <img v-if="picture && !$slots.default" :src="picture" :alt="pictureAlt" />
    <slot />
  </div>
</template>
