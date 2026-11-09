<script setup lang="ts">
// Pages of a long list: previous, the page numbers and next, joined in one row. The current page is
// marked for screen readers too. A long list shows the first and last pages and the ones around the
// current page, with a gap (…) between.
import { computed } from 'vue'

const props = defineProps<{ pages: number; previousLabel: string; nextLabel: string; navLabel: string }>()
const page = defineModel<number>('page', { default: 1 })
const items = computed<(number | 'gap')[]>(() => {
  const shown = [...new Set([1, page.value - 1, page.value, page.value + 1, props.pages])].filter((n) => n >= 1 && n <= props.pages).sort((a, b) => a - b)
  return shown.flatMap((n, i) => (i > 0 && n - shown[i - 1] > 1 ? ['gap' as const, n] : [n]))
})
const go = (to: number) => (page.value = Math.min(Math.max(to, 1), props.pages))
</script>

<template>
  <nav :aria-label="navLabel" data-component="pagination">
    <div class="cp-join">
      <button type="button" class="cp-join-item cp-btn cp-btn-sm" :disabled="page <= 1" :aria-label="previousLabel" @click="go(page - 1)">
        <svg class="size-4 shrink-0" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="m15 18-6-6 6-6" /></svg>
      </button>
      <template v-for="(n, i) in items" :key="n === 'gap' ? `gap${i}` : n">
        <button v-if="n === 'gap'" type="button" class="cp-join-item cp-btn cp-btn-sm" disabled aria-hidden="true">…</button>
        <button
          v-else
          type="button"
          class="cp-join-item cp-btn cp-btn-sm"
          :class="{ 'cp-btn-active': n === page }"
          :aria-current="n === page ? 'page' : undefined"
          @click="go(n)"
        >{{ n }}</button>
      </template>
      <button type="button" class="cp-join-item cp-btn cp-btn-sm" :disabled="page >= pages" :aria-label="nextLabel" @click="go(page + 1)">
        <svg class="size-4 shrink-0" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="m9 18 6-6-6-6" /></svg>
      </button>
    </div>
  </nav>
</template>
