<script setup lang="ts">
// Every drawing design/build-poulpy.mjs generates, so the gallery grows with the generator.
import tokens from '../../../design/tokens.json'

// Mood portraits follow the spectrum, blue side first; the rest are alphabetical.
const moodOrder = Object.keys(tokens.mood)
const rank = (name: string) => {
  const i = moodOrder.indexOf(name.replace('mood_', ''))
  return i < 0 ? moodOrder.length : i
}
const files = import.meta.glob('../../../design/poulpy/*.svg', { query: '?url', import: 'default', eager: true }) as Record<string, string>

const drawings = Object.entries(files)
  .map(([path, url]) => {
    const name = path.split('/').pop()!.replace('.svg', '')
    const [kind, ...rest] = name.split('_')
    return { name, kind, title: rest.join(' '), url }
  })
  .sort((a, b) => rank(a.name) - rank(b.name) || a.name.localeCompare(b.name))

const groups = [
  { kind: 'mood', title: 'Mood portraits' },
  { kind: 'pose', title: 'Poses' },
  { kind: 'loader', title: 'Loader' },
]
</script>

<template>
  <div v-for="group in groups" :key="group.kind">
    <h2 :id="group.kind">{{ group.title }}</h2>
    <div class="grid">
      <figure v-for="d in drawings.filter((x) => x.kind === group.kind)" :key="d.name">
        <img :src="d.url" :alt="`Poulpy, ${d.title || d.name}`" loading="lazy" />
        <figcaption>{{ d.title || d.name.replace('_', ' ') }}</figcaption>
        <code>{{ d.name }}</code>
      </figure>
    </div>
  </div>
</template>

<style scoped>
.grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(140px, 1fr)); gap: 16px; }
figure { margin: 0; display: flex; flex-direction: column; align-items: center; gap: 4px; padding: 12px; border-radius: 16px; background: var(--vp-c-bg-soft); }
img { width: 100%; max-width: 124px; aspect-ratio: 248 / 226; object-fit: contain; }
figcaption { font-weight: 800; text-transform: capitalize; }
code { font-size: 12px; }
</style>
