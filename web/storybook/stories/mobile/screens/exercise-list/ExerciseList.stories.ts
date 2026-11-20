import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { screen } from '../screen'
import screenLibraryExercises from './library-exercises.html?raw'
import screenLibraryExercisesByEquipment from './library-exercises-by-equipment.html?raw'
import screenLibraryExercisesByName from './library-exercises-by-name.html?raw'
import screenLibraryExercisesSort from './library-exercises-sort.html?raw'

// The Exercises tab of the Library, drawn with the phone stylesheet (../screens.css).
const meta = {
  title: "Mobile/Screens/Library/Exercise list",
  tags: ['!autodocs', 'agreed'],
  parameters: { layout: 'fullscreen', mockup: {}, docs: { canvas: { sourceState: 'none' } } },
} satisfies Meta
export default meta
type Story = StoryObj<typeof meta>

/** Library · exercises. */
export const LibraryExercises: Story = {
  name: "Exercises",
  tags: ['!autodocs', 'agreed'],
  ...screen(screenLibraryExercises),
  parameters: { mockup: { issue: 104 } },
}
/** Library · exercises grouped by equipment, then by name. */
export const LibraryExercisesByEquipment: Story = {
  name: "Exercises by equipment",
  tags: ['!autodocs', 'agreed'],
  ...screen(screenLibraryExercisesByEquipment),
  parameters: { mockup: { issue: 104 } },
}
/** Library · exercises by name, no groups. */
export const LibraryExercisesByName: Story = {
  name: "Exercises by name",
  tags: ['!autodocs', 'agreed'],
  ...screen(screenLibraryExercisesByName),
  parameters: { mockup: { issue: 104 } },
}
/** Library · the sort menu: movement, name or equipment. */
export const LibraryExercisesSort: Story = {
  name: "Exercises sort menu",
  tags: ['!autodocs', 'agreed'],
  ...screen(screenLibraryExercisesSort),
  parameters: { mockup: { issue: 104 } },
}
