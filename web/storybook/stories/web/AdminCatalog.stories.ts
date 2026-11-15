import type { Meta, StoryObj } from '@storybook/vue3-vite'
import AdminCatalogScreen, { type Row } from './AdminCatalogScreen.vue'
import AdminCatalogPublishDialog from './AdminCatalogPublishDialog.vue'
import AdminCatalogRetireDialog from './AdminCatalogRetireDialog.vue'

// The admin's catalog list (issue #336).
const meta = {
  title: 'Web/Screens/Admin/Catalog',
  component: AdminCatalogScreen,
  tags: ['!autodocs', 'agreed'],
  // CLEANUP: the admin shell's CpDrawer puts aria-label on a <label> (axe aria-prohibited-attr), as in the Drawer stories.
  parameters: { layout: 'fullscreen', mockup: { issue: 336 }, a11y: { test: 'todo' } },
} satisfies Meta<typeof AdminCatalogScreen>
export default meta
type Story = StoryObj<typeof meta>

const rows: Row[] = [
  { name: 'Pike push-up', french: 'No French name', slug: 'pike-push-up', pattern: 'Push', missing: 'French missing', content: 'New' },
  { name: 'Goblet squat', french: 'Squat gobelet', slug: 'goblet-squat', pattern: 'Squat', content: 'Edited', checked: true },
  { name: 'Bodyweight squat', french: 'Squat au poids du corps', slug: 'bodyweight-squat', pattern: 'Squat' },
  { name: 'Sit to stand', french: 'Assis-debout sur chaise', slug: 'chair-squat', pattern: 'Squat' },
  { name: 'Wall sit', french: 'Chaise contre le mur', slug: 'wall-sit', pattern: 'Squat', content: 'Retiring' },
  { name: 'Glute bridge', french: 'Pont fessier', slug: 'glute-bridge', pattern: 'Hinge', checked: true },
  { name: 'Romanian deadlift', french: 'Soulevé de terre roumain', slug: 'romanian-deadlift', pattern: 'Hinge', content: 'Edited', checked: true },
  { name: 'Kettlebell swing', french: 'Swing kettlebell', slug: 'kettlebell-swing', pattern: 'Hinge', content: 'Offered again' },
  { name: 'Good morning', french: 'Good morning', slug: 'good-morning', pattern: 'Hinge' },
  { name: 'Reverse lunge', french: 'Fente arrière', slug: 'reverse-lunge', pattern: 'Lunge' },
  { name: 'Walking lunge', french: 'Fentes marchées', slug: 'walking-lunge', pattern: 'Lunge' },
  { name: 'Step-up', french: 'Montée sur banc', slug: 'step-up', pattern: 'Lunge' },
  { name: 'Push-up', french: 'Pompe', slug: 'push-up', pattern: 'Push' },
  { name: 'Knee push-up', french: 'Pompe sur les genoux', slug: 'knee-push-up', pattern: 'Push' },
  { name: 'Incline push-up', french: 'Pompe inclinée', slug: 'incline-push-up', pattern: 'Push' },
  { name: 'Diamond push-up', french: 'Pompe diamant', slug: 'diamond-push-up', pattern: 'Push' },
  { name: 'One-arm push-up', french: 'Pompe sur un bras', slug: 'one-arm-push-up', pattern: 'Push' },
  { name: 'Dumbbell bench press', french: 'Développé couché haltères', slug: 'dumbbell-bench-press', pattern: 'Push' },
  { name: 'Dumbbell overhead press', french: 'Développé militaire haltères', slug: 'overhead-press', pattern: 'Push' },
  { name: 'Bench dip', french: 'Dips sur banc', slug: 'bench-dip', pattern: 'Push' },
]
const showing = 'Showing 1 to 20 of 61 exercises'

export const List: Story = { args: { rows, showing, selection: 3, pattern: 'squat', translation: 'incomplete' } }
export const Filtered: Story = {
  args: {
    rows: [{ name: 'Wall sit', french: 'Chaise contre le mur', slug: 'wall-sit', pattern: 'Squat', lifecycle: 'Retired' }],
    intro: 'Retired exercises remain available to existing workouts and can be offered again.',
    placeholder: 'Search retired exercises',
    lifecycle: 'retired',
    selection: 1,
    showing,
  },
}
export const LoadError: Story = { args: { rows: [], error: true } }

/** Retiring the three selected exercises asks first. */
export const RetireSelected: Story = {
  args: { rows, showing, selection: 3, pattern: 'squat', translation: 'incomplete' },
  render: (args) => ({ components: { AdminCatalogScreen, AdminCatalogRetireDialog }, setup: () => ({ args }), template: '<AdminCatalogScreen v-bind="args"><AdminCatalogRetireDialog /></AdminCatalogScreen>' }),
}
/** Publishing lists what changed before it goes out. */
export const Publish: Story = {
  args: { rows, showing, selection: 3, pattern: 'squat', translation: 'incomplete' },
  render: (args) => ({ components: { AdminCatalogScreen, AdminCatalogPublishDialog }, setup: () => ({ args }), template: '<AdminCatalogScreen v-bind="args"><AdminCatalogPublishDialog /></AdminCatalogScreen>' }),
}
