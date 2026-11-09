<script setup lang="ts">
// Pages of a long list: previous, the page numbers and next, joined in one row. The current page is
// marked for screen readers too.
const props = defineProps<{ pages: number; previousLabel: string; nextLabel: string; navLabel: string }>()
const page = defineModel<number>('page', { default: 1 })
const go = (to: number) => (page.value = Math.min(Math.max(to, 1), props.pages))
</script>

<template>
  <nav :aria-label="navLabel" data-component="pagination">
    <div class="cp-join">
      <button type="button" class="cp-join-item cp-btn cp-btn-sm" :disabled="page <= 1" :aria-label="previousLabel" @click="go(page - 1)">
        <svg class="size-4 shrink-0" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="m15 18-6-6 6-6" /></svg>
      </button>
      <button
        v-for="n in pages"
        :key="n"
        type="button"
        class="cp-join-item cp-btn cp-btn-sm"
        :class="{ 'cp-btn-active': n === page }"
        :aria-current="n === page ? 'page' : undefined"
        @click="go(n)"
      >{{ n }}</button>
      <button type="button" class="cp-join-item cp-btn cp-btn-sm" :disabled="page >= pages" :aria-label="nextLabel" @click="go(page + 1)">
        <svg class="size-4 shrink-0" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="m9 18 6-6-6-6" /></svg>
      </button>
    </div>
  </nav>
</template>
