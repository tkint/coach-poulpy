import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { screen } from '../screen'
import screenWorkoutDelete from './workout-delete.html?raw'
import screenWorkoutExport from './workout-export.html?raw'
import screenWorkoutMenu from './workout-menu.html?raw'
import screenWorkoutPageBodyMap from './workout-page-body-map.html?raw'
import screenWorkoutPageCircuit from './workout-page-circuit.html?raw'
import screenWorkoutPageDetails from './workout-page-details.html?raw'
import screenWorkoutPagePain from './workout-page-pain.html?raw'
import screenWorkoutPagePlanned from './workout-page-planned.html?raw'
import screenWorkoutPageSets from './workout-page-sets.html?raw'
import screenWorkoutPlan from './workout-plan.html?raw'

// The Workout page screens of the apps (ADR 0029), drawn with the phone stylesheet (../screens.css).
// CLEANUP: the screens marked a11y todo carry axe findings the old mockups had (contrast of hints and past days, names of fields); fix them with the screens, then drop the todo.
const meta = {
  title: "Mobile/Screens/Workout page",
  tags: ['!autodocs', 'agreed'],
  parameters: { layout: 'fullscreen', mockup: {}, docs: { canvas: { sourceState: 'none' } } },
} satisfies Meta
export default meta
type Story = StoryObj<typeof meta>

/** Workout page · delete. */
export const WorkoutDelete: Story = {
  name: "Delete",
  ...screen(screenWorkoutDelete),
  parameters: { mockup: { issue: 104 } },
}
/** Workout page · export sheet. */
export const WorkoutExport: Story = {
  name: "Export",
  ...screen(screenWorkoutExport),
  parameters: { mockup: { issue: 152 } },
}
/** Workout page · menu. */
export const WorkoutMenu: Story = {
  name: "Menu",
  ...screen(screenWorkoutMenu),
  parameters: { mockup: { issue: 104 } },
}
/** Workout page · half body map, half details. */
export const WorkoutPageBodyMap: Story = {
  name: "Half body map, half details",
  ...screen(screenWorkoutPageBodyMap),
  parameters: { mockup: { issue: 244 } },
}
/** Workout page · circuit in parts. */
export const WorkoutPageCircuit: Story = {
  name: "Circuit in parts",
  ...screen(screenWorkoutPageCircuit),
  parameters: { mockup: { issue: 92 } },
}
/** Workout page · details full screen. */
export const WorkoutPageDetails: Story = {
  name: "Details full screen",
  ...screen(screenWorkoutPageDetails),
  parameters: { mockup: { issue: 244 } },
}
/** Workout page · a worked muscle hurts. */
export const WorkoutPagePain: Story = {
  name: "A worked muscle hurts",
  ...screen(screenWorkoutPagePain),
  parameters: { mockup: { issue: 244 } },
}
/** Workout page · muscles and details. */
export const WorkoutPagePlanned: Story = {
  name: "Muscles and details",
  ...screen(screenWorkoutPagePlanned),
  parameters: { mockup: { issue: 244 } },
}
/** Workout page · sets in parts. */
export const WorkoutPageSets: Story = {
  name: "Sets in parts",
  ...screen(screenWorkoutPageSets),
  parameters: { mockup: { issue: 92 } },
}
/** Workout page · plan days. */
export const WorkoutPlan: Story = {
  name: "Plan days",
  ...screen(screenWorkoutPlan),
  parameters: { a11y: { test: 'todo' }, mockup: { issue: 104 } },
}
