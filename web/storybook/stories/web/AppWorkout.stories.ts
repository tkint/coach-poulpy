import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { List, Moon, Sun, Zap } from '@lucide/vue'
import { CpBadge, CpBodyMap, CpBreadcrumbs, CpButton, CpCard, CpSheet, CpStat, CpStats } from '@coach-poulpy/ui'
import { heatAreas } from '../../../app/src/bodyMap'
import AppShell from './AppShell.vue'

// One workout of the web app (issue #355). Was design/mockups/agreed/web/app-workout.html.
const meta = {
  title: 'Web/Screens/App/Workout',
  tags: ['!autodocs', 'agreed'],
  // CLEANUP: the shell's CpDrawer puts aria-label on a <label> (axe aria-prohibited-attr), as in the Drawer stories.
  parameters: { layout: 'fullscreen', mockup: { issue: 355 }, a11y: { test: 'todo' } },
} satisfies Meta
export default meta
type Story = StoryObj

const parts = [
  { icon: Sun, name: 'Warm-up', summary: '2 steps · about 6 min', rows: [['1', 'Walk', '5 min'], ['2', 'Arm circle', '30 s']] },
  { icon: Zap, name: 'Work', summary: '4 exercises · about 25 min', rows: [['3', 'Bench press', '3 × 8 · 40 kg · 2 min between sets'], ['4', 'Bent-over row', '3 × 10 · 30 kg · 1 min 30 s between sets'], ['5', 'Biceps curl', '12 reps · 10 kg · repeat 3 times'], ['6', 'Triceps dip', '12 reps · repeat 3 times']] },
  { icon: Moon, name: 'Cool down', summary: '1 step · 1 min', rows: [['7', 'Chest stretch', '30 s hold · each side']] },
]
// The heat the API sends for each area: shoulders and triceps most, then chest, back and biceps, a little core.
const heat = heatAreas({ deltoid: 3, triceps: 3, pectoral: 2, back: 2, scapula: 2, 'lower-back': 2, biceps: 2, abs: 1, obliques: 1 })
const captions = { front: 'Front', back: 'Back' }
const description = 'Muscles worked: shoulders and triceps most, then chest, back and biceps, a little core'

const page = (sheet: boolean) => ({
  components: { AppShell, CpBadge, CpBodyMap, CpBreadcrumbs, CpButton, CpCard, CpSheet, CpStat, CpStats, List },
  setup: () => ({ parts, heat, captions, description, sheet }),
  template: `<AppShell title="Workouts" active="workouts">
    <main class="flex flex-col gap-6 p-4 md:p-8">
      <CpBreadcrumbs label="You are here"><li><a href="#">Workouts</a></li><li>Upper body</li></CpBreadcrumbs>
      <div class="flex flex-col gap-2">
        <h1 class="cp-type-display">Upper body</h1>
        <p class="flex flex-wrap items-center gap-2 text-base-content/70">
          <CpBadge tone="primary" class="gap-1"><List class="size-4 shrink-0" aria-hidden="true" />Sets</CpBadge>
          <span>7 exercises · about 32 min · barbell, dumbbells</span>
        </p>
      </div>
      <a href="#" class="cp-btn cp-btn-outline lg:hidden" aria-haspopup="dialog">Stats and muscles worked</a>
      <div class="grid items-start gap-6 lg:grid-cols-[minmax(0,1fr)_22rem]">
        <div class="flex flex-col gap-6">
          <CpCard v-for="part in parts" :key="part.name">
            <div class="flex items-center gap-3">
              <component :is="part.icon" class="size-6 shrink-0 text-primary" aria-hidden="true" />
              <div><h2 class="cp-card-title">{{ part.name }}</h2><p class="text-base-content/70">{{ part.summary }}</p></div>
            </div>
            <div class="overflow-x-auto" data-component="table">
              <table class="cp-table"><tbody>
                <tr v-for="[n, name, line] in part.rows" :key="n"><td class="w-10 text-base-content/70">{{ n }}</td><td><strong>{{ name }}</strong></td><td class="text-base-content/70">{{ line }}</td></tr>
              </tbody></table>
            </div>
          </CpCard>
        </div>
        <div class="hidden flex-col gap-6 lg:sticky lg:top-8 lg:flex" role="region" aria-label="Workout stats and muscles worked">
          <CpStats vertical class="w-full">
            <CpStat title="Done" value="3 times" />
            <CpStat title="Last time" value="Wed 21" />
            <CpStat title="Usually" value="31 min" />
          </CpStats>
          <CpCard title="Muscles worked"><CpBodyMap :front="heat" :back="heat" :captions="captions" legend="top" :description="description" less="Less" more="More" /></CpCard>
        </div>
      </div>
      <CpCard title="Change it on your phone"><p class="text-base-content/70">Workouts are read-only here. Open the app to edit, plan or start this one.</p></CpCard>
      <CpSheet :open="sheet" title="Stats and muscles worked" close-label="Close">
        <div class="flex flex-col gap-4 py-2">
          <CpStats vertical class="w-full"><CpStat title="Done" value="3 times" /><CpStat title="Last time" value="Wed 21" /><CpStat title="Usually" value="31 min" /></CpStats>
          <CpBodyMap :front="heat" :back="heat" :captions="captions" legend="top" :description="description" less="Less" more="More" />
        </div>
      </CpSheet>
    </main></AppShell>`,
})
/** The parts of the workout with their steps, read-only, and its stats and muscles in a column on the right. */
export const Default: Story = { render: () => page(false) }
/** On a phone the stats and muscles open in a sheet. */
export const OverviewSheet: Story = { render: () => page(true), globals: { viewport: { value: 'mobile2', isRotated: false } } }
