<script setup lang="ts">
// The page of a public screen: the site header, then Poulpy beside the words (ADR 0029: a web mockup composes the Cp components).
import { CpPoulpy } from '@coach-poulpy/ui'
import type { PoulpyDrawing } from '@coach-poulpy/ui'

defineProps<{ poulpy: PoulpyDrawing; title: string }>()
</script>

<template>
  <div class="min-h-screen bg-base-100 text-base-content">
    <header class="mx-auto flex max-w-5xl items-center gap-2 px-4 pt-6 md:px-8" data-component="site-header">
      <CpPoulpy drawing="mood_happy" :size="36" />
      <span class="cp-type-headline">Coach <span class="text-primary">poulpy</span></span>
    </header>
    <main class="mx-auto grid max-w-5xl items-center gap-8 px-4 py-10 md:grid-cols-2 md:gap-16 md:px-8 md:py-24">
      <CpPoulpy :drawing="poulpy" :size="320" class="w-40 justify-self-center md:w-80" />
      <div class="flex max-w-md flex-col gap-6">
        <div class="flex flex-col gap-2">
          <h1 class="cp-type-display">{{ title }}</h1>
          <p><slot name="intro" /></p>
        </div>
        <slot />
      </div>
    </main>
  </div>
</template>
