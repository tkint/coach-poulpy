<script setup lang="ts">
// The pictures painted from the 3D Poulpy (design/build-poulpy-pictures.mjs): his six moods and twelve poses,
// as the apps and the website take them, on the page's own background so they show as they will on a light or a dark screen.
const files = import.meta.glob<string>('../../../../design/poulpy-3d/pictures/*.png', { eager: true, query: '?url', import: 'default' })
const label = (name: string) => name.replace(/^(mood|pose)_/, '').replace(/_/g, ' ')
const group = (kind: string) =>
  Object.entries(files)
    .map(([path, url]) => ({ name: path.slice(path.lastIndexOf('/') + 1, -'.png'.length), url }))
    .filter(({ name }) => name.startsWith(`${kind}_`))
const groups = [{ title: 'Moods', pictures: group('mood') }, { title: 'Poses', pictures: group('pose') }]
</script>

<template>
  <div class="pictures">
    <section v-for="g in groups" :key="g.title" :aria-label="g.title">
      <h3>{{ g.title }}</h3>
      <ul>
        <li v-for="p in g.pictures" :key="p.name">
          <img :src="p.url" :alt="`Poulpy, ${label(p.name)}`" width="248" height="226" loading="lazy" />
          <span>{{ label(p.name) }}</span>
        </li>
      </ul>
    </section>
  </div>
</template>

<style scoped>
.pictures { margin: 16px 0; }
h3 { margin: 16px 0 8px; font-size: 18px; }
ul { display: grid; grid-template-columns: repeat(auto-fill, minmax(150px, 1fr)); gap: 8px; margin: 0; padding: 0; list-style: none; }
/* No tile behind them: they are transparent, and sit on the page whatever its theme. */
li { display: grid; justify-items: center; gap: 2px; padding: 8px; }
img { width: 100%; height: auto; }
span { font-size: 13px; font-weight: 700; text-transform: capitalize; color: var(--cp-on-surface-variant); }
</style>
