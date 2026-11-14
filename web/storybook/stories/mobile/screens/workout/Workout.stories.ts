import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { screen } from '../screen'
import screenWorkoutPageExerciseDetailsFull from './workout-page-exercise-details-full.html?raw'
import screenWorkoutPageExerciseDetails from './workout-page-exercise-details.html?raw'

// The Workout screens of the apps (ADR 0029), drawn with the phone stylesheet (../screens.css).
const meta = {
  title: "Mobile/Screens/Workout",
  tags: ['!autodocs', 'agreed'],
  parameters: { layout: 'fullscreen', mockup: {}, docs: { canvas: { sourceState: 'none' } } },
} satisfies Meta
export default meta
type Story = StoryObj<typeof meta>

/** Workout page · exercise sheet, pulled up. Was design/mockups/agreed/phone/workout-page-exercise-details-full.html. */
export const WorkoutPageExerciseDetailsFull: Story = {
  name: "Exercise sheet, pulled up",
  ...screen(screenWorkoutPageExerciseDetailsFull),
  parameters: { mockup: { issue: 359 } },
}
/** Workout page · exercise sheet. Was design/mockups/agreed/phone/workout-page-exercise-details.html. */
export const WorkoutPageExerciseDetails: Story = {
  name: "Exercise sheet",
  ...screen(screenWorkoutPageExerciseDetails),
  parameters: { mockup: { issue: 359 } },
}
