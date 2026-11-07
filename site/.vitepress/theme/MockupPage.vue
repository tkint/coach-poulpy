<script setup lang="ts">
import { computed } from 'vue'
import { withBase } from 'vitepress'
import { data as mockups } from '../../mockups.data.ts'
import { devices, platforms, stages } from '../mockup-labels'
import MockupFrame from './MockupFrame.vue'

// A section's page: every state of it one after the other, each with its own anchor.
const props = defineProps<{ platform: string; stage: string; section: string }>()
const states = computed(() => mockups.filter((m) => m.platform === props.platform && m.stage === props.stage && m.sectionSlug === props.section))
const first = computed(() => states.value[0])
// The same screen in the other stage, if it exists: a redesign next to the screen it replaces.
const counterpart = (m: (typeof mockups)[number]) =>
  mockups.find((o) => o.stage !== m.stage && o.device === m.device && o.screen === m.screen)
</script>

<template>
  <div v-if="first" class="mockup-page">
    <p class="crumbs">
      <a :href="withBase(`/mockups/${first.platform}/${first.stage}`)">{{ platforms[first.platform] }} · {{ stages[first.stage] }}</a>
    </p>
    <h1>{{ first.group }}</h1>
    <p v-if="first.stage === 'drafts'" class="note">Drafts: nothing is built from them until a pull request moves them to Agreed.</p>
    <div class="states">
      <section v-for="m in states" :key="m.path" class="state">
        <h2 :id="m.screen">{{ m.title }}<span v-if="m.device !== 'phone'" class="device"> · {{ devices[m.device] }}</span></h2>
        <p v-if="counterpart(m)" class="note">
          {{ counterpart(m)!.stage === 'agreed' ? 'Replaces' : 'Being redesigned in' }}
          <a :href="withBase(counterpart(m)!.link)">{{ stages[counterpart(m)!.stage] }} · {{ counterpart(m)!.title }}</a>.
        </p>
        <MockupFrame :mockup="m" />
      </section>
    </div>
  </div>
</template>

<style scoped>
.crumbs { font-size: 14px; color: var(--vp-c-text-2); margin: 0 0 4px; }
.crumbs a { color: var(--vp-c-brand-1); }
h1 { margin: 0 0 16px; }
.note { color: var(--vp-c-text-2); margin: 0 0 12px; }
.note a { color: var(--vp-c-brand-1); }
.states { display: grid; grid-template-columns: repeat(auto-fill, minmax(min(100%, 438px), 438px)); gap: 40px 32px; margin-top: 24px; }
.state { min-width: 0; scroll-margin-top: 80px; }
.state h2 { margin: 0 0 8px; padding: 0; border: 0; font-size: 20px; line-height: 28px; }
.device { color: var(--vp-c-text-2); font-weight: 500; }
</style>
