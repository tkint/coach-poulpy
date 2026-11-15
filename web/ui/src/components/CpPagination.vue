<script setup lang="ts">
import CpIcon from './CpIcon.vue'
// Pages of a long list: previous, the page numbers and next, joined in one row. The current page is
// marked for screen readers too. A long list shows the first and last pages and the ones around the
// current page, with a gap (…) between. `size` is the websites' scale (md when none is given; a table's are `sm`).
import { ChevronLeft, ChevronRight } from '@lucide/vue'
import { computed } from 'vue'
import { uiText } from '../i18n'
import { buttonSize, controlIcon, type CpSize } from './sizes'

const props = withDefaults(defineProps<{ pages: number; previousLabel?: string; nextLabel?: string; navLabel?: string; size?: CpSize }>(), { size: 'md' })
const page = defineModel<number>('page', { default: 1 })
const items = computed<(number | 'gap')[]>(() => {
  const shown = [...new Set([1, page.value - 1, page.value, page.value + 1, props.pages])].filter((n) => n >= 1 && n <= props.pages).sort((a, b) => a - b)
  return shown.flatMap((n, i) => (i > 0 && n - shown[i - 1] > 1 ? ['gap' as const, n] : [n]))
})
const go = (to: number) => (page.value = Math.min(Math.max(to, 1), props.pages))
</script>

<template>
  <nav :aria-label="navLabel ?? uiText('ui.pages')" data-component="pagination">
    <div class="cp-join">
      <button type="button" class="cp-join-item cp-btn" :class="buttonSize[size]" :disabled="page <= 1" :aria-label="previousLabel ?? uiText('ui.previousPage')" @click="go(page - 1)">
        <CpIcon :icon="ChevronLeft" :size="controlIcon[size]" />
      </button>
      <template v-for="(n, i) in items" :key="n === 'gap' ? `gap${i}` : n">
        <button v-if="n === 'gap'" type="button" class="cp-join-item cp-btn" :class="buttonSize[size]" disabled aria-hidden="true">…</button>
        <button
          v-else
          type="button"
          class="cp-join-item cp-btn"
          :class="[buttonSize[size], { 'cp-btn-active': n === page }]"
          :aria-current="n === page ? 'page' : undefined"
          @click="go(n)"
        >{{ n }}</button>
      </template>
      <button type="button" class="cp-join-item cp-btn" :class="buttonSize[size]" :disabled="page >= pages" :aria-label="nextLabel ?? uiText('ui.nextPage')" @click="go(page + 1)">
        <CpIcon :icon="ChevronRight" :size="controlIcon[size]" />
      </button>
    </div>
  </nav>
</template>
