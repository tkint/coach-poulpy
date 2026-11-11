<script setup lang="ts">
// One of the websites' real components from web/ui (ADR 0023), in the theme the page's switch chose. The panel
// pins it with a cp-theme-* class from tokens.css, and data-theme="poulpy" so DaisyUI reads the panel's colours. The embed build adds no page-wide rule, so the site keeps its look.
import { ref } from 'vue'
import { useSampleTheme } from './useSampleTheme'
import '../../../web/ui/src/tokens.css'
import '../../../web/ui/poulpy-ui-embed.css'
import {
  CpAvatar,
  CpBadge,
  CpBodyMap,
  CpBreadcrumbs,
  CpCard,
  CpDivider,
  CpSuccessNote,
  CpNavItem,
  CpNavMenu,
  CpPasswordField,
  CpStat,
  CpStats,
  CpTable,
  CpButton,
  CpChip,
  CpErrorNote,
  CpPagination,
  CpPoulpyCard,
  CpSearchField,
  CpSelectField,
  CpSheet,
  CpSidePanel,
  CpStepper,
  CpTextField,
  CpTextarea,
} from '../../../web/ui/src/index'

defineProps<{ part: 'badge' | 'body-map' | 'breadcrumbs' | 'avatar' | 'card' | 'table' | 'stats' | 'nav-menu' | 'button' | 'chip' | 'search-field' | 'stepper' | 'select-field' | 'text-field' | 'textarea' | 'password-field' | 'poulpy-card' | 'error' | 'sheet' | 'pagination' | 'side-panel' }>()
const { mode } = useSampleTheme()
// A stand-in figure: the real ones are the generated body map shapes.
const sampleFigures = [
  { caption: 'Front', paths: [{ d: 'M60 40 a14 14 0 1 0 0.01 0z', outline: true }, { d: 'M36 60 h48 v70 h-48z', heat: 3 as const }, { d: 'M42 130 h14 v90 h-14z M64 130 h14 v90 h-14z', heat: 1 as const }] },
  { caption: 'Back', paths: [{ d: 'M60 40 a14 14 0 1 0 0.01 0z', outline: true }, { d: 'M36 60 h48 v70 h-48z', heat: 2 as const }, { d: 'M42 130 h14 v90 h-14z M64 130 h14 v90 h-14z' }] },
]
const chips = ref({ legs: true, core: false })
const query = ref('squat')
const rest = ref(90)
const format = ref('circuit')
const sheet = ref(false)
const page = ref(2)
const panel = ref(true)
const password = ref('correct horse')
const email = ref('ana@example.com')
const sender = ref('hello@coachpoulpy.example')
const stepText = ref('Keep your chest lifted and lower under control.\nPause briefly, then return to standing.')
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
        <CpButton variant="danger">Delete</CpButton>
        <CpButton variant="outline" size="md">Disable</CpButton>
      </div>
      <div v-else-if="part === 'badge'" class="row">
        <CpBadge>Published catalog</CpBadge>
        <CpBadge tone="neutral">Made admin</CpBadge>
        <CpBadge tone="outline">Disabled account</CpBadge>
        <CpBadge tone="success">Active</CpBadge>
        <CpBadge tone="error">Send failed</CpBadge>
      </div>
      <div v-else-if="part === 'avatar'" class="row">
        <CpAvatar initials="A" />
        <CpAvatar initials="A" tone="primary" size="md" />
        <CpAvatar initials="A" tone="primary" size="lg" />
      </div>
      <CpCard v-else-if="part === 'card'" title="Latest changes">Everything an admin changed, newest first.</CpCard>
      <template v-else-if="part === 'card-badge'">
        <CpCard title="Mail">
          <template #badge><CpBadge tone="success">Configured</CpBadge></template>
          The server sends emails through its mail server.
        </CpCard>
        <CpCard title="Mail">
          <template #badge><CpBadge tone="error">Not configured</CpBadge></template>
          <CpErrorNote>No mail server is set on the server, so no email can be sent.</CpErrorNote>
        </CpCard>
        <CpCard title="Pending registrations" description="Accounts that have not confirmed their email yet." />
      </template>
      <CpCard v-else-if="part === 'table'" title="Latest changes">
        <CpTable>
          <thead><tr><th>When</th><th>What</th></tr></thead>
          <tbody>
            <tr><td>8 Nov, 14:20</td><td><CpBadge>Published catalog</CpBadge></td></tr>
            <tr><td>8 Nov, 14:02</td><td><CpBadge tone="outline">Disabled account</CpBadge></td></tr>
          </tbody>
        </CpTable>
      </CpCard>
      <CpBreadcrumbs v-else-if="part === 'breadcrumbs'" label="You are here">
        <li><a href="#">Workouts</a></li>
        <li>Upper body</li>
      </CpBreadcrumbs>
      <CpBodyMap v-else-if="part === 'body-map'" :figures="sampleFigures" description="Muscles worked: chest most, then legs a little" less="Less" more="More" />
      <CpStats v-else-if="part === 'stats'">
        <CpStat title="Changes this week" value="12" description="Since Monday" accent />
        <CpStat title="Admins" value="1" description="Ana" />
      </CpStats>
      <CpNavMenu v-else-if="part === 'nav-menu'">
        <CpNavItem active>Audit log</CpNavItem>
        <CpNavItem>Accounts</CpNavItem>
        <CpNavItem>Catalog</CpNavItem>
      </CpNavMenu>
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
      <div v-else-if="part === 'text-field'" class="row">
        <CpTextField v-model="email" label="Email" type="email" autocomplete="username" />
        <CpTextField v-model="sender" label="From address" type="email" autocomplete="off" hint="The address emails come from." />
        <CpTextField model-value="hello@" label="From address" type="email" autocomplete="off" error="This is not an email address. Use the form name@example.com." />
      </div>
      <div v-else-if="part === 'textarea'" class="row">
        <CpTextarea v-model="stepText" label="Step 1" hint="One instruction per step. The field grows as you write." :rows="2" />
      </div>
      <div v-else-if="part === 'password-field'" class="row">
        <CpPasswordField v-model="password" label="New password" hint="8 characters or more." show-label="Show password" hide-label="Hide password" autocomplete="new-password" />
      </div>
      <CpPoulpyCard v-else-if="part === 'poulpy-card'" title="No workouts yet">
        Build one, or start from a plan.
        <template #actions><CpButton>New workout</CpButton></template>
      </CpPoulpyCard>
      <CpErrorNote v-else-if="part === 'error'">We couldn't save your workout. Check your connection and try again.</CpErrorNote>
      <CpSuccessNote v-else-if="part === 'success'">Test email sent to ana@example.com.</CpSuccessNote>
      <CpCard v-else-if="part === 'divider'" title="Email sender">
        The address emails come from.
        <CpDivider />
        Send a test email with the saved settings.
      </CpCard>
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
      <CpPagination v-else-if="part === 'pagination'" v-model:page="page" :pages="5" previous-label="Previous page" next-label="Next page" nav-label="Pages" />
      <template v-else-if="part === 'side-panel'">
        <div class="row"><CpButton variant="outline" size="md" @click="panel = !panel">{{ panel ? 'Hide' : 'Show' }} the details</CpButton></div>
        <CpSidePanel v-if="panel" :class="`cp-theme-${mode}`" data-theme="poulpy" title="lea@example.com" close-label="Close the details" @close="panel = false">
          <template #subtitle><CpBadge>Active</CpBadge></template>
          <p>Member since 9 Nov 2026.</p>
        </CpSidePanel>
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
