<script setup lang="ts">
// The brand book, drawn in the page itself so the sidebar and the outline work (it used to be an
// iframe). Its sections have ids, so the outline lists them; its colours follow the site's theme.
import { nextTick, onMounted } from 'vue'
import { withBase } from 'vitepress'
import html from './brand-book/book.html?raw'
import { drawBrandBook } from './brand-book/book'
import './brand-book/book.css'

const book = html.replace('__BRAND_DECISIONS__', withBase('/docs/brand-decisions'))
onMounted(async () => {
  await nextTick()
  drawBrandBook()
})
</script>

<template>
  <div class="brand-book vp-raw" v-html="book"></div>
</template>
