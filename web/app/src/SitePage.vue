<script setup lang="ts">
// The frame of the account pages (design/mockups/agreed/web): the site's name, then Poulpy beside
// the words from tablet width, above them on a phone.
import { poulpyDrawings, type PoulpyDrawing } from '@coach-poulpy/ui'

defineProps<{ drawing: PoulpyDrawing }>()
</script>

<template>
  <header class="mx-auto flex max-w-5xl items-center gap-2 px-4 pt-6 md:px-8" data-component="site-header">
    <img :src="poulpyDrawings.mood_happy" alt="" width="36" height="36" />
    <span class="cp-type-headline">Coach <span class="text-primary">poulpy</span></span>
  </header>
  <main class="mx-auto grid max-w-5xl items-center gap-8 px-4 py-10 md:grid-cols-2 md:gap-16 md:px-8 md:py-24">
    <img :src="poulpyDrawings[drawing]" alt="" width="320" height="290" class="w-40 justify-self-center md:w-80" />
    <div class="flex max-w-md flex-col gap-6" aria-live="polite">
      <slot />
    </div>
  </main>
</template>
