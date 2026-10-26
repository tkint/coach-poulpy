---
layout: page
---

<script setup>
import { withBase } from 'vitepress'
import { data as mockups } from './mockups.data.ts'

const stages = [
  { id: 'agreed', name: 'Agreed', blurb: 'Validated by the owner. The apps are built from these.' },
  { id: 'drafts', name: 'Drafts', blurb: 'Being explored or redesigned. Nothing is built from a draft.' },
]
const devices = { phone: 'Phone', tablet: 'Tablet', web: 'Website' }
const groups = (stage) => {
  const bySection = new Map()
  for (const m of mockups.filter((m) => m.stage === stage)) {
    const key = `${devices[m.device]} · ${m.section}`
    if (!bySection.has(key)) bySection.set(key, [])
    bySection.get(key).push(m)
  }
  return [...bySection.entries()]
}
</script>

<div class="mockups">

# Mockups

The screen mockups from [`design/mockups/`](https://github.com/tkint/coach-poulpy/tree/main/design/mockups), as they are on `main` ([ADR 0016](/docs/adr/0016-mockups-are-plain-html-in-the-repo)). Each one is a plain HTML page styled with the design tokens. A screen moves from Drafts to Agreed through a pull request.

<section v-for="stage in stages" :key="stage.id">
  <h2 :id="stage.id">{{ stage.name }}</h2>
  <p>{{ stage.blurb }}</p>
  <p v-if="groups(stage.id).length === 0" class="empty">None yet.</p>
  <div v-for="[group, items] in groups(stage.id)" :key="group">
    <h3>{{ group }}</h3>
    <div class="grid">
      <figure v-for="m in items" :key="m.path">
        <iframe :src="withBase('/' + m.path)" :title="m.title" loading="lazy" :style="{ height: (m.height + 48) + 'px' }"></iframe>
        <figcaption>
          <a :href="withBase('/' + m.path)" target="_blank" rel="noopener">{{ m.title }}</a>
          <span v-if="m.issue"> · <a :href="'https://github.com/tkint/coach-poulpy/issues/' + m.issue">#{{ m.issue }}</a></span>
        </figcaption>
      </figure>
    </div>
  </div>
</section>

</div>

<style>
.mockups { max-width: 1440px; margin: 0 auto; padding: 24px 16px; }
.mockups h1 { font-size: 32px; line-height: 40px; font-weight: 700; margin-bottom: 12px; }
.mockups h2 { font-size: 24px; margin: 40px 0 4px; border-top: 1px solid var(--vp-c-divider); padding-top: 24px; }
.mockups h3 { font-size: 18px; margin: 24px 0 12px; }
.mockups .empty { color: var(--vp-c-text-2); }
.mockups .grid { display: flex; flex-wrap: wrap; gap: 24px; align-items: flex-start; }
.mockups figure { margin: 0; width: 438px; max-width: 100%; }
.mockups iframe { width: 438px; max-width: 100%; border: 0; border-radius: 24px; display: block; }
.mockups figcaption { margin-top: 8px; font-size: 14px; }
</style>
