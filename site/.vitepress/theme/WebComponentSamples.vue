<script setup lang="ts">
// The websites' real components from web/ui (ADR 0023), in light and dark side by side. Each panel
// pins one theme with a cp-theme-* class from tokens.css, and data-theme="poulpy" so DaisyUI
// reads that panel's colours. The embed build adds no page-wide rule, so the site keeps its look.
import { ref } from 'vue'
import '../../../web/ui/src/tokens.css'
import '../../../web/ui/poulpy-ui-embed.css'
import {
  CpButton,
  CpChip,
  CpErrorNote,
  CpPoulpyCard,
  CpSearchField,
  CpSelectField,
  CpSheet,
  CpStepper,
} from '../../../web/ui/src/index'

const modes = ['light', 'dark'] as const
const chips = ref({ light: { legs: true, core: false }, dark: { legs: true, core: false } })
const query = ref({ light: 'squat', dark: '' })
const rest = ref({ light: 90, dark: 90 })
const format = ref({ light: 'circuit', dark: 'circuit' })
const sheet = ref({ light: false, dark: false })
const formats = [
  { value: 'sets', text: 'Sets' },
  { value: 'circuit', text: 'Circuit' },
  { value: 'emom', text: 'Every minute' },
]
</script>

<template>
  <div class="web-samples">
    <div v-for="mode in modes" :key="mode" :class="`cp-theme-${mode}`" class="panel" data-theme="poulpy">
      <span class="cp-type-label">{{ mode }}</span>
      <div class="row">
        <CpButton variant="primary">Start session</CpButton>
        <CpButton variant="tonal">Edit</CpButton>
        <CpButton variant="outline">Cancel</CpButton>
        <CpButton variant="grumpy">Catch up</CpButton>
      </div>
      <div class="row">
        <CpChip v-model="chips[mode].legs">Legs</CpChip>
        <CpChip v-model="chips[mode].core">Core</CpChip>
      </div>
      <CpSearchField v-model="query[mode]" label="Search exercises" clear-label="Clear the search" placeholder="Search exercises" />
      <div class="row">
        <CpStepper v-model="rest[mode]" label="Rest" :value-text="`${rest[mode]} s`" less-label="Less rest" more-label="More rest" :step="15" />
        <CpSelectField v-model="format[mode]" label="Format" :options="formats" />
      </div>
      <CpPoulpyCard title="No workouts yet">
        Build one, or start from a plan.
        <template #actions><CpButton>New workout</CpButton></template>
      </CpPoulpyCard>
      <CpErrorNote>We couldn't save your workout. Check your connection and try again.</CpErrorNote>
      <div class="row">
        <CpButton variant="outline" @click="sheet[mode] = true">Open a sheet</CpButton>
      </div>
      <CpSheet v-model:open="sheet[mode]" :class="`cp-theme-${mode}`" data-theme="poulpy" title="Delete this workout?" close-label="Close">
        <p>Its sessions stay in your history.</p>
        <template #actions>
          <CpButton variant="outline" @click="sheet[mode] = false">Keep it</CpButton>
          <CpButton @click="sheet[mode] = false">Delete</CpButton>
        </template>
      </CpSheet>
    </div>
  </div>
</template>

<style scoped>
.web-samples {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(min(100%, 22rem), 1fr));
  gap: 16px;
  margin: 16px 0;
}

.panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 24px;
  border-radius: 16px;
  border: 1px solid var(--cp-outline);
  background: var(--cp-background);
  color: var(--cp-on-background);
}

.row {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-end;
  gap: 8px;
}

/* The site's prose styles stop at the samples. */
.panel :deep(p) {
  margin: 0;
  line-height: inherit;
}
.panel :deep(h2) {
  margin: 0;
  padding: 0;
  border: 0;
  letter-spacing: 0;
}
.panel :deep(button) {
  line-height: 1.2;
}
</style>
