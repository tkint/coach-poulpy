import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { screen } from '../screen'
import screenLibraryActions from './library-actions.html?raw'
import screenLibraryEmpty from './library-empty.html?raw'
import screenLibraryExercises from './library-exercises.html?raw'
import screenLibraryNoResults from './library-no-results.html?raw'
import screenLibrarySearch from './library-search.html?raw'
import screenLibraryWorkouts from './library-workouts.html?raw'

// The Library screens of the apps (ADR 0029), drawn with the phone stylesheet (../screens.css).
// CLEANUP: the screens marked a11y todo carry axe findings the old mockups had (contrast of hints and past days, names of fields); fix them with the screens, then drop the todo.
// CLEANUP: the screens marked a11y todo carry axe findings the old mockups had (contrast of hints and past days, names of fields); fix them with the screens, then drop the todo.
// CLEANUP: the screens marked a11y todo carry axe findings the old mockups had (contrast of hints and past days, names of fields); fix them with the screens, then drop the todo.
const meta = {
  title: "Mobile/Screens/Library",
  tags: ['!autodocs', 'agreed'],
  parameters: { layout: 'fullscreen', mockup: {}, docs: { canvas: { sourceState: 'none' } } },
} satisfies Meta
export default meta
type Story = StoryObj<typeof meta>

/** Library · workout actions. Was design/mockups/agreed/phone/library-actions.html. */
export const LibraryActions: Story = {
  name: "Workout actions",
  ...screen(screenLibraryActions),
  parameters: { a11y: { test: 'todo' }, mockup: { issue: 104 } },
}
/** Library · no workouts yet. Was design/mockups/agreed/phone/library-empty.html. */
export const LibraryEmpty: Story = {
  name: "No workouts yet",
  ...screen(screenLibraryEmpty),
  parameters: { mockup: { issue: 104 } },
}
/** Library · exercises. Was design/mockups/agreed/phone/library-exercises.html. */
export const LibraryExercises: Story = {
  name: "Exercises",
  ...screen(screenLibraryExercises),
  parameters: { mockup: { issue: 104 } },
}
/** Library · no workout found. Was design/mockups/agreed/phone/library-no-results.html. */
export const LibraryNoResults: Story = {
  name: "No workout found",
  ...screen(screenLibraryNoResults),
  parameters: { mockup: { issue: 104 } },
}
/** Library · search “squat”. Was design/mockups/agreed/phone/library-search.html. */
export const LibrarySearch: Story = {
  name: "Search “squat”",
  ...screen(screenLibrarySearch),
  parameters: { a11y: { test: 'todo' }, mockup: { issue: 104 } },
}
/** Library · workouts. Was design/mockups/agreed/phone/library-workouts.html. */
export const LibraryWorkouts: Story = {
  name: "Workouts",
  ...screen(screenLibraryWorkouts),
  parameters: { a11y: { test: 'todo' }, mockup: { issue: 104 } },
}
