<script setup lang="ts">
import { computed } from 'vue'
import { withBase } from 'vitepress'
import { data as mockups } from '../../mockups.data.ts'
import { devices, stages } from '../mockup-labels'
import type { Mockup } from '../mockups.mts'

// Links only: a mockup is shown on its own page, never in a list.
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
  <h1>{{ stages[stage] }}</h1>
  <p>{{ blurbs[stage] }}</p>
  <p v-if="groups.length === 0">None yet.</p>
  <template v-for="[group, items] in groups" :key="group">
    <h2>{{ group }}</h2>
    <ul>
      <li v-for="m in items" :key="m.path">
        <a :href="withBase(m.link)">{{ m.title }}</a>
        <template v-if="m.issue"> · <a :href="`https://github.com/tkint/coach-poulpy/issues/${m.issue}`">#{{ m.issue }}</a></template>
      </li>
    </ul>
  </template>
</template>
