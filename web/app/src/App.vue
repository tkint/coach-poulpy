<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { CpPoulpy } from '@coach-poulpy/ui'

// Placeholder page: shows whether the local server is running.
const server = ref<'checking' | 'up' | 'down'>('checking')

onMounted(async () => {
  try {
    const response = await fetch('/api/health')
    server.value = response.ok ? 'up' : 'down'
  } catch {
    server.value = 'down'
  }
})
</script>

<template>
  <main class="mx-auto my-16 max-w-xl px-4">
    <CpPoulpy drawing="mood_happy" :size="96" />
    <h1 class="cp-display">Coach <span class="text-primary">poulpy</span></h1>
    <p>The website arrives with accounts. Local server: {{ server }}</p>
  </main>
</template>

<style>
body {
  background: var(--cp-background);
  color: var(--cp-on-background);
}
</style>
