<script setup lang="ts">
// A raised panel, with a title when given, for a table or a group of fields. A badge beside the title says the
// state of what the card is about (mail: configured), and a description under it says what the card holds.
// CpPoulpyCard is the one for empty states.
defineProps<{ title?: string; description?: string }>()
</script>

<template>
  <section class="cp-card bg-base-100 shadow-sm" data-component="card">
    <div class="cp-card-body gap-4 p-4 md:p-6">
      <div v-if="title && (description || $slots.badge)" class="flex flex-col gap-1">
        <div class="flex flex-wrap items-center gap-3">
          <h2 class="cp-card-title">{{ title }}</h2>
          <slot name="badge" />
        </div>
        <p v-if="description" class="text-sm text-base-content/70">{{ description }}</p>
      </div>
      <h2 v-else-if="title" class="cp-card-title">{{ title }}</h2>
      <slot />
    </div>
  </section>
</template>
