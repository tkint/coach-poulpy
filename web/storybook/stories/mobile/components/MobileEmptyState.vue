<script setup lang="ts">
// What a list or a screen shows when nothing is there yet or nothing matches. Deliberately quiet: no coloured card,
// a small Poulpy thinking beside a title and a line, then at most one small secondary button (the `action` slot).
// The apps' single `EmptyState` mirrors it.
import { computed } from 'vue'
import { poulpyDrawings } from '@coach-poulpy/ui'

const props = withDefaults(defineProps<{ title: string; pose?: string; stack?: boolean; size?: number }>(), { pose: 'pose_thinking', size: 64 })
const picture = computed(() => (poulpyDrawings as Record<string, string>)[props.pose])
</script>

<template>
  <section :class="['empty-state', { 'empty-state--stack': stack }]" data-component="empty-state">
    <img :src="picture" alt="" :width="size" :height="Math.round(size * 0.9)" />
    <div class="empty-state-text">
      <h2>{{ title }}</h2>
      <p><slot /></p>
      <div v-if="$slots.action" class="empty-state-action"><slot name="action" /></div>
    </div>
  </section>
</template>
