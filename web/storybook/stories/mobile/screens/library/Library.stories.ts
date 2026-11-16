import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { screen } from '../screen'
import screenLibraryActions from './library-actions.html?raw'
import screenLibraryEmpty from './library-empty.html?raw'
import screenLibraryExercises from './library-exercises.html?raw'
import screenLibraryExercisesByEquipment from './library-exercises-by-equipment.html?raw'
import screenLibraryExercisesByName from './library-exercises-by-name.html?raw'
import screenLibraryExercisesSort from './library-exercises-sort.html?raw'
import screenLibraryNewWorkoutSheet from './library-new-workout-sheet.html?raw'
import screenLibraryNoResults from './library-no-results.html?raw'
import screenLibrarySearch from './library-search.html?raw'
import screenLibraryWorkouts from './library-workouts.html?raw'

// The Library screens of the apps (ADR 0029), drawn with the phone stylesheet (../screens.css).
// CLEANUP: the screens marked a11y todo carry axe findings the old mockups had (contrast of hints and past days, names of fields); fix them with the screens, then drop the todo.
const meta = {
  title: "Mobile/Screens/Library",
  tags: ['!autodocs', 'agreed'],
  parameters: { layout: 'fullscreen', mockup: {}, docs: { canvas: { sourceState: 'none' } } },
} satisfies Meta
export default meta
type Story = StoryObj<typeof meta>

/** Library · workout actions. */
export const LibraryActions: Story = {
  name: "Workout actions",
  tags: ['!autodocs', 'draft'],
  ...screen(screenLibraryActions),
  parameters: { a11y: { test: 'todo' }, mockup: { issue: 104 } },
}
/** Library · no workouts yet. */
export const LibraryEmpty: Story = {
  name: "No workouts yet",
  ...screen(screenLibraryEmpty),
  parameters: { mockup: { issue: 104 } },
}
/** Library · exercises. */
export const LibraryExercises: Story = {
  name: "Exercises",
  tags: ['!autodocs', 'draft'],
  ...screen(screenLibraryExercises),
  parameters: { mockup: { issue: 104 } },
}
/** Library · exercises grouped by equipment, then by name. */
export const LibraryExercisesByEquipment: Story = {
  name: "Exercises by equipment",
  tags: ['!autodocs', 'draft'],
  ...screen(screenLibraryExercisesByEquipment),
  parameters: { mockup: { issue: 104 } },
}
/** Library · exercises by name, no groups. */
export const LibraryExercisesByName: Story = {
  name: "Exercises by name",
  tags: ['!autodocs', 'draft'],
  ...screen(screenLibraryExercisesByName),
  parameters: { mockup: { issue: 104 } },
}
/** Library · the sort menu: movement, name or equipment. */
export const LibraryExercisesSort: Story = {
  name: "Exercises sort menu",
  tags: ['!autodocs', 'draft'],
  ...screen(screenLibraryExercisesSort),
  parameters: { mockup: { issue: 104 } },
}
/** Library · "New workout" opens the format sheet over the Library. */
export const LibraryNewWorkoutSheet: Story = {
  name: "New workout, format sheet",
  tags: ['!autodocs', 'draft'],
  ...screen(screenLibraryNewWorkoutSheet),
  parameters: { mockup: { issue: 242 } },
}
/** Library · no workout found. */
export const LibraryNoResults: Story = {
  name: "No workout found",
  ...screen(screenLibraryNoResults),
  parameters: { mockup: { issue: 104 } },
}
/** Library · search “squat”. */
export const LibrarySearch: Story = {
  name: "Search “squat”",
  tags: ['!autodocs', 'draft'],
  ...screen(screenLibrarySearch),
  parameters: { a11y: { test: 'todo' }, mockup: { issue: 104 } },
}
/** Library · workouts. */
export const LibraryWorkouts: Story = {
  name: "Workouts",
  tags: ['!autodocs', 'draft'],
  ...screen(screenLibraryWorkouts),
  parameters: { a11y: { test: 'todo' }, mockup: { issue: 104 } },
}
