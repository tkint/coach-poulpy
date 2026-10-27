<script setup lang="ts">
import { computed } from 'vue'
import { withBase } from 'vitepress'
import { data as mockups } from '../../mockups.data.ts'
import { devices, stages } from '../mockup-labels'
import MockupFrame from './MockupFrame.vue'

const props = defineProps<{ stage: string; device: string; screen: string }>()
const siblings = computed(() => mockups.filter((m) => m.stage === props.stage))
const index = computed(() => siblings.value.findIndex((m) => m.device === props.device && m.screen === props.screen))
const mockup = computed(() => siblings.value[index.value])
const previous = computed(() => siblings.value[index.value - 1])
const next = computed(() => siblings.value[index.value + 1])
// The same screen in the other stage, if it exists: a redesign next to the screen it replaces.
const counterpart = computed(() =>
  mockups.find((m) => m.stage !== props.stage && m.device === props.device && m.screen === props.screen),
)
</script>

<template>
  <div v-if="mockup" class="mockup-page">
    <p class="crumbs">
      <a :href="withBase(`/mockups/${mockup.stage}`)">{{ stages[mockup.stage] }}</a> · {{ devices[mockup.device] }} · {{ mockup.section }}
    </p>
    <h1>{{ mockup.title }}</h1>
    <p v-if="mockup.stage === 'drafts'" class="note">A draft: nothing is built from it until a pull request moves it to Agreed.</p>
    <p v-if="counterpart">
      {{ counterpart.stage === 'agreed' ? 'Replaces' : 'Being redesigned in' }}
      <a :href="withBase(counterpart.link)">{{ stages[counterpart.stage] }} · {{ counterpart.title }}</a>.
    </p>
    <MockupFrame :mockup="mockup" />
    <nav class="pager">
      <a v-if="previous" :href="withBase(previous.link)">← {{ previous.title }}</a>
      <span v-else></span>
      <a v-if="next" :href="withBase(next.link)">{{ next.title }} →</a>
    </nav>
  </div>
</template>

<style scoped>
.mockup-page { max-width: 1152px; margin: 0 auto; padding: 24px 16px 48px; }
.crumbs { font-size: 14px; color: var(--vp-c-text-2); margin: 0 0 4px; }
.crumbs a { color: var(--vp-c-brand-1); }
h1 { font-size: 28px; line-height: 36px; font-weight: 700; margin: 0 0 8px; }
p { margin: 0 0 16px; }
p a { color: var(--vp-c-brand-1); }
.note { color: var(--vp-c-text-2); }
.pager { display: flex; justify-content: space-between; gap: 16px; max-width: 438px; margin-top: 24px; font-size: 14px; }
.pager a { color: var(--vp-c-brand-1); font-weight: 500; }
</style>
