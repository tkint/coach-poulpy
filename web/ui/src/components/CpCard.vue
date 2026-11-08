<script setup lang="ts">
// A raised panel, with a title when given, for a table or a group of fields. CpPoulpyCard is the one for
// empty states.
defineProps<{ title?: string }>()
</script>

<template>
  <section class="cp-card bg-base-100 shadow-sm" data-component="card">
    <div class="cp-card-body gap-4 p-4 md:p-6">
      <h2 v-if="title" class="cp-card-title">{{ title }}</h2>
      <slot />
    </div>
  </section>
</template>
