import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { screen } from '../screen'
import screenBuilderStepDetailsSheet from './builder-step-details-sheet.html?raw'
import screenExerciseInfoSheetHowTo from './exercise-info-sheet-how-to.html?raw'
import screenExerciseInfoSheetVariations from './exercise-info-sheet-variations.html?raw'
import screenExerciseInfoSheet from './exercise-info-sheet.html?raw'
import screenExercisePicker from './exercise-picker.html?raw'
import screenExercisesEmpty from './exercises-empty.html?raw'
import screenExercisesEquipment from './exercises-equipment.html?raw'
import screenExercisesFocus from './exercises-focus.html?raw'
import screenExercisesPosition from './exercises-position.html?raw'
import screenExercisesSearch from './exercises-search.html?raw'
import screenExercisesTags from './exercises-tags.html?raw'
import screenExercisesZones from './exercises-zones.html?raw'

// The Exercises and search screens of the apps (ADR 0029), drawn with the phone stylesheet (../screens.css).
// CLEANUP: the screens marked a11y todo carry axe findings the old mockups had (contrast of hints and past days, names of fields); fix them with the screens, then drop the todo.
// CLEANUP: the screens marked a11y todo carry axe findings the old mockups had (contrast of hints and past days, names of fields); fix them with the screens, then drop the todo.
// CLEANUP: the screens marked a11y todo carry axe findings the old mockups had (contrast of hints and past days, names of fields); fix them with the screens, then drop the todo.
// CLEANUP: the screens marked a11y todo carry axe findings the old mockups had (contrast of hints and past days, names of fields); fix them with the screens, then drop the todo.
const meta = {
  title: "Mobile/Screens/Exercises and search",
  tags: ['!autodocs', 'agreed'],
  parameters: { layout: 'fullscreen', mockup: {}, docs: { canvas: { sourceState: 'none' } } },
} satisfies Meta
export default meta
type Story = StoryObj<typeof meta>

/** Builder · Exercise details of an added step. Was design/mockups/agreed/phone/builder-step-details-sheet.html. */
export const BuilderStepDetailsSheet: Story = {
  name: "Exercise details of an added step",
  ...screen(screenBuilderStepDetailsSheet),
  parameters: { mockup: { issue: 189 } },
}
/** Picker details sheet · How to do it tab. Was design/mockups/agreed/phone/exercise-info-sheet-how-to.html. */
export const ExerciseInfoSheetHowTo: Story = {
  name: "How to do it tab",
  ...screen(screenExerciseInfoSheetHowTo),
  parameters: { a11y: { test: 'todo' }, mockup: { issue: undefined } },
}
/** Picker details sheet · variations list. Was design/mockups/agreed/phone/exercise-info-sheet-variations.html. */
export const ExerciseInfoSheetVariations: Story = {
  name: "Variations list",
  ...screen(screenExerciseInfoSheetVariations),
  parameters: { a11y: { test: 'todo' }, mockup: { issue: undefined } },
}
/** Picker details sheet · Muscles tab. Was design/mockups/agreed/phone/exercise-info-sheet.html. */
export const ExerciseInfoSheet: Story = {
  name: "Muscles tab",
  ...screen(screenExerciseInfoSheet),
  parameters: { a11y: { test: 'todo' }, mockup: { issue: undefined } },
}
/** Builder · pick an exercise. Was design/mockups/agreed/phone/exercise-picker.html. */
export const ExercisePicker: Story = {
  name: "Pick an exercise",
  ...screen(screenExercisePicker),
  parameters: { a11y: { test: 'todo' }, mockup: { issue: 189 } },
}
/** Exercises · no results. Was design/mockups/agreed/phone/exercises-empty.html. */
export const ExercisesEmpty: Story = {
  name: "No results",
  ...screen(screenExercisesEmpty),
  parameters: { mockup: { issue: 104 } },
}
/** Exercises · equipment. Was design/mockups/agreed/phone/exercises-equipment.html. */
export const ExercisesEquipment: Story = {
  name: "Equipment",
  ...screen(screenExercisesEquipment),
  parameters: { mockup: { issue: 104 } },
}
/** Exercises · focus. Was design/mockups/agreed/phone/exercises-focus.html. */
export const ExercisesFocus: Story = {
  name: "Focus",
  ...screen(screenExercisesFocus),
  parameters: { mockup: { issue: undefined } },
}
/** Exercises · position. Was design/mockups/agreed/phone/exercises-position.html. */
export const ExercisesPosition: Story = {
  name: "Position",
  ...screen(screenExercisesPosition),
  parameters: { mockup: { issue: undefined } },
}
/** Exercises · search results. Was design/mockups/agreed/phone/exercises-search.html. */
export const ExercisesSearch: Story = {
  name: "Search results",
  ...screen(screenExercisesSearch),
  parameters: { mockup: { issue: 104 } },
}
/** Exercises · tags. Was design/mockups/agreed/phone/exercises-tags.html. */
export const ExercisesTags: Story = {
  name: "Tags",
  ...screen(screenExercisesTags),
  parameters: { mockup: { issue: undefined } },
}
/** Exercises · zones. Was design/mockups/agreed/phone/exercises-zones.html. */
export const ExercisesZones: Story = {
  name: "Zones",
  ...screen(screenExercisesZones),
  parameters: { mockup: { issue: undefined } },
}
