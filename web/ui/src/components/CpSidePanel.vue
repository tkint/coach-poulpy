<script setup lang="ts">
// Details of the row picked in a list, in a column on the right of a wide page and over the whole page
// on a phone. Put it next to the list inside a `flex` row; the close button emits `close`.
defineProps<{ title: string; closeLabel: string }>()
defineEmits<{ close: [] }>()
</script>

<template>
  <aside
    class="fixed inset-0 z-30 flex flex-col gap-4 overflow-y-auto bg-base-100 p-4 md:p-6 lg:static lg:inset-auto lg:w-96 lg:shrink-0 lg:border-s lg:border-base-300"
    aria-labelledby="side-panel-title"
    data-component="side-panel"
  >
    <div class="flex items-start gap-3">
      <div v-if="$slots.avatar"><slot name="avatar" /></div>
      <div class="flex min-w-0 grow flex-col gap-1">
        <h2 id="side-panel-title" class="cp-type-headline break-all">{{ title }}</h2>
        <slot name="subtitle" />
      </div>
      <button type="button" class="cp-btn cp-btn-circle cp-btn-ghost cp-btn-sm" :aria-label="closeLabel" @click="$emit('close')">
        <svg class="size-5 shrink-0" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M18 6 6 18" /><path d="m6 6 12 12" /></svg>
      </button>
    </div>
    <slot />
  </aside>
</template>
