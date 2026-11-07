<script setup lang="ts">
// One of the websites' real components from web/ui (ADR 0023), in the theme the page's switch chose. The panel
// pins it with a cp-theme-* class from tokens.css, and data-theme="poulpy" so DaisyUI reads the panel's colours. The embed build adds no page-wide rule, so the site keeps its look.
import { ref } from 'vue'
import { useSampleTheme } from './useSampleTheme'
import '../../../web/ui/src/tokens.css'
import '../../../web/ui/poulpy-ui-embed.css'
import {
  CpPasswordField,
  CpButton,
  CpChip,
  CpErrorNote,
  CpPoulpyCard,
  CpSearchField,
  CpSelectField,
  CpSheet,
  CpStepper,
} from '../../../web/ui/src/index'

defineProps<{ part: 'button' | 'chip' | 'search-field' | 'stepper' | 'select-field' | 'password-field' | 'poulpy-card' | 'error' | 'sheet' }>()
const { mode } = useSampleTheme()
const chips = ref({ legs: true, core: false })
const query = ref('squat')
const rest = ref(90)
const format = ref('circuit')
const sheet = ref(false)
const password = ref('correct horse')
const formats = [
  { value: 'sets', text: 'Sets' },
  { value: 'circuit', text: 'Circuit' },
  { value: 'emom', text: 'Every minute' },
]
</script>

<template>
  <div class="web-samples">
    <div :class="`cp-theme-${mode}`" class="panel" data-theme="poulpy">
      <div v-if="part === 'button'" class="row">
        <CpButton variant="primary">Start session</CpButton>
        <CpButton variant="tonal">Edit</CpButton>
        <CpButton variant="outline">Cancel</CpButton>
        <CpButton variant="grumpy">Catch up</CpButton>
      </div>
      <div v-else-if="part === 'chip'" class="row">
        <CpChip v-model="chips.legs">Legs</CpChip>
        <CpChip v-model="chips.core">Core</CpChip>
      </div>
      <CpSearchField v-else-if="part === 'search-field'" v-model="query" label="Search exercises" clear-label="Clear the search" placeholder="Search exercises" />
      <div v-else-if="part === 'stepper'" class="row">
        <CpStepper v-model="rest" label="Rest" :value-text="`${rest} s`" less-label="Less rest" more-label="More rest" :step="15" />
      </div>
      <div v-else-if="part === 'select-field'" class="row">
        <CpSelectField v-model="format" label="Format" :options="formats" />
      </div>
      <div v-else-if="part === 'password-field'" class="row">
        <CpPasswordField v-model="password" label="New password" hint="8 characters or more." show-label="Show password" hide-label="Hide password" autocomplete="new-password" />
      </div>
      <CpPoulpyCard v-else-if="part === 'poulpy-card'" title="No workouts yet">
        Build one, or start from a plan.
        <template #actions><CpButton>New workout</CpButton></template>
      </CpPoulpyCard>
      <CpErrorNote v-else-if="part === 'error'">We couldn't save your workout. Check your connection and try again.</CpErrorNote>
      <template v-else-if="part === 'sheet'">
        <div class="row">
          <CpButton variant="outline" @click="sheet = true">Open a sheet</CpButton>
        </div>
        <CpSheet v-model:open="sheet" :class="`cp-theme-${mode}`" data-theme="poulpy" title="Delete this workout?" close-label="Close">
          <p>Its sessions stay in your history.</p>
          <template #actions>
            <CpButton variant="outline" @click="sheet = false">Keep it</CpButton>
            <CpButton @click="sheet = false">Delete</CpButton>
          </template>
        </CpSheet>
      </template>
    </div>
  </div>
</template>

<style scoped>
.web-samples {
  display: grid;
  grid-template-columns: minmax(0, 1fr);
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
