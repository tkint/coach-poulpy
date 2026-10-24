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
    <h1>Coach Poulpy</h1>
    <p>The website arrives with accounts. Local server: {{ server }}</p>
  </main>
</template>

<style scoped>
main {
  font-family: system-ui, sans-serif;
  max-width: 40rem;
  margin: 4rem auto;
  padding: 0 1rem;
}
</style>
