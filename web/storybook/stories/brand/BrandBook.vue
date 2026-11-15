<script setup lang="ts">
// The brand book, drawn in the page itself. Its colours follow the workshop's light and dark toolbar.
import { nextTick, onMounted } from 'vue'
import html from './brand-book/book.html?raw'
import { drawBrandBook } from './brand-book/book'
import './brand-book/book.css'

const book = html.replace('__BRAND_DECISIONS__', '?path=/docs/brand-decisions--docs')
onMounted(async () => {
  await nextTick()
  drawBrandBook()
})
</script>

<template>
  <div class="brand-book" v-html="book"></div>
</template>
