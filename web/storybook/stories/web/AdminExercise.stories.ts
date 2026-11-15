import type { Meta, StoryObj } from '@storybook/vue3-vite'
import AdminExerciseScreen, { type Exercise } from './AdminExerciseScreen.vue'
import AdminExerciseRetireDialog from './AdminExerciseRetireDialog.vue'
import AdminExerciseDeleteDialog from './AdminExerciseDeleteDialog.vue'

// The admin's exercise screen (issues #386 and #338).
const meta = {
  title: 'Web/Screens/Admin/Exercise',
  component: AdminExerciseScreen,
  tags: ['!autodocs', 'agreed'],
  // CLEANUP: the admin shell's CpDrawer puts aria-label on a <label> (axe aria-prohibited-attr), as in the Drawer stories.
  parameters: { layout: 'fullscreen', mockup: { issue: 386 }, a11y: { test: 'todo' } },
} satisfies Meta<typeof AdminExerciseScreen>
export default meta
type Story = StoryObj<typeof meta>

const goblet: Exercise = {
  name: { en: 'Goblet squat', fr: 'Squat goblet' },
  slug: 'goblet-squat',
  steps: [
    { en: 'Hold the weight against your chest, elbows down.', fr: 'Tenez la charge contre la poitrine, coudes vers le bas.' },
    { en: 'Sit down between your heels, chest up, then stand.', fr: 'Descendez entre vos talons, buste droit, puis remontez.' },
  ],
  note: { en: 'A weight held against your chest', fr: 'Une charge tenue contre la poitrine' },
  aliases: { en: '', fr: '' },
  needs: ['dumbbells', 'kettlebell'],
  optional: [],
  pattern: 'squat',
  measurement: 'reps',
  positions: ['standing'],
  muscles: ['quadriceps', 'glutes', 'core', 'hamstrings'],
  focus: ['strength'],
}
const pike: Exercise = {
  name: { en: 'Pike push-up', fr: '' },
  slug: 'pike-push-up',
  steps: [
    { en: 'Start in a push-up with your hips high, like an upside-down V.', fr: '' },
    { en: 'Bend your elbows to bring your head toward the floor, then push back.', fr: '' },
  ],
  note: { en: '', fr: '' },
  aliases: { en: '', fr: '' },
  needs: [],
  optional: [],
  pattern: 'push',
  measurement: 'reps',
  positions: [],
  muscles: ['shoulders', 'triceps'],
  focus: [],
}
const wallSit: Exercise = {
  ...goblet,
  name: { en: 'Wall sit', fr: 'Chaise contre le mur' },
  slug: 'wall-sit',
  steps: [
    { en: 'Back against a wall, slide down until your knees make a right angle.', fr: 'Dos au mur, glissez jusqu’à former un angle droit aux genoux.' },
    { en: 'Keep your knees over your ankles and hold.', fr: 'Gardez les genoux au-dessus des chevilles et tenez.' },
  ],
  needs: [],
  measurement: 'hold',
  muscles: ['quadriceps', 'glutes'],
  focus: [],
}
const published = [{ text: 'Offered', tone: 'success' as const }, { text: 'Edited', tone: 'neutral' as const }, { text: 'Version 4', tone: 'outline' as const }]
const added = [{ text: 'New', tone: 'primary' as const }]

/** A published exercise in English: its slug is locked, and each field says where French stands. */
export const Edit: Story = { args: { exercise: goblet, badges: published } }
/** The same exercise with French picked: each field shows its English original under it. */
export const French: Story = { args: { exercise: goblet, badges: published, language: 'fr' } }
/** On a phone the screen is one column, and Save is repeated at the end since the header does not stick. */
export const FrenchOnPhone: Story = {
  args: { exercise: goblet, badges: published, language: 'fr' },
  globals: { viewport: { value: 'mobile2', isRotated: false } },
}
/** A new exercise: the slug is open, French is empty, and the FR switch carries the crimson icon. */
export const New: Story = { args: { exercise: pike, isNew: true, badges: added, frenchFlagged: true } }
/** The new exercise with French picked: each French field is empty and crimson. */
export const NewFrench: Story = { args: { exercise: pike, isNew: true, badges: added, language: 'fr', frenchFlagged: true } }
/** Saving a new exercise whose slug another exercise already has: the field says why, in crimson. */
export const SlugTaken: Story = { args: { exercise: { ...pike, slug: 'push-up' }, isNew: true, badges: added, frenchFlagged: true, slugTaken: true } }
/** Retiring an exercise asks first; it is a draft change, not a deletion. */
export const Retire: Story = {
  args: { exercise: wallSit, badges: [{ text: 'Offered', tone: 'success' as const }] },
  render: (args) => ({ components: { AdminExerciseScreen, AdminExerciseRetireDialog }, setup: () => ({ args }), template: '<AdminExerciseScreen v-bind="args"><template #dialog><AdminExerciseRetireDialog /></template></AdminExerciseScreen>' }),
}
/** The danger zone of a draft that was never published. */
export const DangerDraft: Story = { tags: ['agreed'], args: { exercise: pike, badges: [{ text: 'New', tone: 'primary' as const }], neverPublished: true } }
/** Deleting asks for the slug; the button stays off until it matches exactly. */
export const Delete: Story = {
  tags: ['agreed'],
  args: { exercise: pike, badges: [{ text: 'New', tone: 'primary' as const }], neverPublished: true },
  render: (args) => ({ components: { AdminExerciseScreen, AdminExerciseDeleteDialog }, setup: () => ({ args }), template: '<AdminExerciseScreen v-bind="args"><template #dialog><AdminExerciseDeleteDialog slug="pike-push-up" typed="pike-push" /></template></AdminExerciseScreen>' }),
}
/** A published exercise can be deleted too (beta): the dialog warns that workouts using it lose it. */
export const DeletePublished: Story = {
  tags: ['agreed'],
  args: { exercise: wallSit, badges: [{ text: 'Offered', tone: 'success' as const }] },
  render: (args) => ({ components: { AdminExerciseScreen, AdminExerciseDeleteDialog }, setup: () => ({ args }), template: '<AdminExerciseScreen v-bind="args"><template #dialog><AdminExerciseDeleteDialog slug="wall-sit" typed="wall-sit" published /></template></AdminExerciseScreen>' }),
}
