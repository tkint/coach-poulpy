<script setup lang="ts">
// The Poulpy card for empty lists and no results: Poulpy happy on the blue side, a title, a line
// and one primary button (in the actions slot).
import CpPoulpy from './CpPoulpy.vue'

defineProps<{ title: string }>()
</script>

<template>
  <section class="card bg-secondary text-secondary-content cp-poulpy-card" data-component="poulpy-card">
    <div class="card-body items-center">
      <CpPoulpy drawing="mood_happy" :size="96" />
      <h2 class="card-title">{{ title }}</h2>
      <p><slot /></p>
      <div v-if="$slots.actions" class="card-actions"><slot name="actions" /></div>
    </div>
  </section>
</template>
