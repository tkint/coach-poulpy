import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { screen } from '../screen'
import screenWorkoutImportError from './workout-import-error.html?raw'
import screenWorkoutImport from './workout-import.html?raw'

// The Workout import screens of the apps (ADR 0029), drawn with the phone stylesheet (../screens.css).
const meta = {
  title: "Mobile/Screens/Workout import",
  tags: ['!autodocs', 'agreed'],
  parameters: { layout: 'fullscreen', mockup: {}, docs: { canvas: { sourceState: 'none' } } },
} satisfies Meta
export default meta
type Story = StoryObj<typeof meta>

/** Workout import · refused. */
export const WorkoutImportError: Story = {
  name: "Refused",
  ...screen(screenWorkoutImportError),
  parameters: { mockup: { issue: 129 } },
}
/** Workout import · preview. */
export const WorkoutImport: Story = {
  name: "Preview",
  ...screen(screenWorkoutImport),
  parameters: { mockup: { issue: 129 } },
}
