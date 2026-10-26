<script setup lang="ts">
import { computed } from 'vue'
import { withBase } from 'vitepress'
import { data as mockups } from '../../mockups.data.ts'
import { devices, stages } from '../mockup-labels'
import type { Mockup } from '../mockups.mts'

const props = defineProps<{ stage: 'agreed' | 'drafts' }>()
const blurbs = {
  agreed: 'Screens the owner validated. The apps are built from these.',
  drafts: 'Screens being explored or redesigned. Nothing is built from a draft until a pull request moves it to Agreed.',
}
const groups = computed(() => {
  const byGroup = new Map<string, Mockup[]>()
  for (const m of mockups.filter((m) => m.stage === props.stage)) {
    const key = `${devices[m.device]} · ${m.section}`
    byGroup.set(key, [...(byGroup.get(key) ?? []), m])
  }
  return [...byGroup.entries()]
})
</script>

<template>
  <div class="mockup-gallery">
    <h1>{{ stages[stage] }}</h1>
    <p>{{ blurbs[stage] }} Open a mockup to try it and reset it.</p>
    <p v-if="groups.length === 0" class="empty">None yet.</p>
    <section v-for="[group, items] in groups" :key="group">
      <h2>{{ group }}</h2>
      <ul>
        <li v-for="m in items" :key="m.path">
          <a :href="withBase(m.link)">
            <span class="thumb" :style="{ height: Math.min(m.height, 844) / 2 + 'px' }">
              <iframe :src="withBase('/' + m.path)" :title="m.title" loading="lazy" tabindex="-1" aria-hidden="true" :style="{ height: m.height + 48 + 'px' }"></iframe>
            </span>
            <span class="name">{{ m.title }}</span>
          </a>
        </li>
      </ul>
    </section>
  </div>
</template>

<style scoped>
.mockup-gallery { max-width: 1440px; margin: 0 auto; padding: 24px 16px 48px; }
h1 { font-size: 32px; line-height: 40px; font-weight: 700; margin: 0 0 8px; }
h2 { font-size: 18px; margin: 32px 0 12px; }
.empty { color: var(--vp-c-text-2); }
ul { list-style: none; padding: 0; margin: 0; display: flex; flex-wrap: wrap; gap: 24px; }
li { margin: 0; }
li a { text-decoration: none; display: flex; flex-direction: column; gap: 8px; width: 219px; color: var(--vp-c-text-1); font-size: 14px; font-weight: 600; }
li a:hover .name { color: var(--vp-c-brand-1); }
/* A half-size, non-interactive preview: the click goes to the mockup's own page. */
.thumb { display: block; width: 219px; overflow: hidden; border-radius: 16px; pointer-events: none; }
.thumb iframe { width: 438px; border: 0; transform: scale(0.5); transform-origin: top left; }
</style>
