<script setup lang="ts">
// Poulpy speaking from a primary container card: his art (the `art` slot: a tentacle ring or a drawing), a `title`,
// a line (the content) and an optional `meta` line in small capitals. `compact` is the smaller card that closes a
// session.
defineProps<{ title: string; meta?: string; compact?: boolean }>()
</script>

<template>
  <section :class="['poulpy-card', { 'poulpy-card--compact': compact }]" data-component="poulpy-card">
    <slot name="art" />
    <div class="poulpy-card-text">
      <h2>{{ title }}</h2>
      <p><slot /></p>
      <span v-if="meta" class="label poulpy-card-meta">{{ meta }}</span>
    </div>
  </section>
</template>
