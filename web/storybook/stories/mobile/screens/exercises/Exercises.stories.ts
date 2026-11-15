import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { screen } from '../screen'
import screenExercisePageHowTo from './exercise-page-how-to.html?raw'
import screenExercisePage from './exercise-page.html?raw'
import screenExerciseVariations from './exercise-variations.html?raw'

// The Exercises screens of the apps (ADR 0029), drawn with the phone stylesheet (../screens.css).
// CLEANUP: the screens marked a11y todo carry axe findings the old mockups had (contrast of hints and past days, names of fields); fix them with the screens, then drop the todo.
const meta = {
  title: "Mobile/Screens/Exercises",
  tags: ['!autodocs', 'agreed'],
  parameters: { layout: 'fullscreen', mockup: {}, docs: { canvas: { sourceState: 'none' } } },
} satisfies Meta
export default meta
type Story = StoryObj<typeof meta>

/** Exercise page · How to do it. */
export const ExercisePageHowTo: Story = {
  name: "How to do it",
  ...screen(screenExercisePageHowTo),
  parameters: { a11y: { test: 'todo' }, mockup: { issue: 104 } },
}
/** Exercise page. */
export const ExercisePage: Story = {
  name: "Exercise page",
  ...screen(screenExercisePage),
  parameters: { a11y: { test: 'todo' }, mockup: { issue: 104 } },
}
/** Exercise page · variations. */
export const ExerciseVariations: Story = {
  name: "Variations",
  ...screen(screenExerciseVariations),
  parameters: { a11y: { test: 'todo' }, mockup: { issue: 104 } },
}
