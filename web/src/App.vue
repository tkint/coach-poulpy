<script setup lang="ts">
import { onMounted, ref } from 'vue'

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
  <main>
    <h1>Coach <span>poulpy</span></h1>
    <p>The website arrives with accounts. Local server: {{ server }}</p>
  </main>
</template>

<style>
body {
  margin: 0;
  background: var(--cp-background);
  color: var(--cp-on-background);
  font: var(--cp-type-body);
}
</style>

<style scoped>
main {
  max-width: 40rem;
  margin: 4rem auto;
  padding: 0 var(--cp-space-lg);
}

h1 {
  font: var(--cp-type-display);
}

h1 span {
  color: var(--cp-poulpy-blue);
}
</style>
