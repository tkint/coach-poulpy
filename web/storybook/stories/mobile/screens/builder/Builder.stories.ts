import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { screen } from '../screen'
import screenBuilderChangeFormat from './builder-change-format.html?raw'
import screenBuilderChangeFormatOrder from './builder-change-format-order.html?raw'
import screenBuilderChangeFormatSheet from './builder-change-format-sheet.html?raw'
import screenBuilderCircuitEditStep from './builder-circuit-edit-step.html?raw'
import screenBuilderCircuitParts from './builder-circuit-parts.html?raw'
import screenBuilderDistanceSheet from './builder-distance-sheet.html?raw'
import screenBuilderRunParts from './builder-run-parts.html?raw'
import screenBuilderRunTarget from './builder-run-target.html?raw'
import screenBuilderSetsEditStep from './builder-sets-edit-step.html?raw'
import screenBuilderSetsSuperset from './builder-sets-superset.html?raw'
import screenBuilderTidyEmpty from './builder-tidy-empty.html?raw'
import screenBuilderTidyAddMenu from './builder-tidy-add-menu.html?raw'
import screenBuilderTidyRepeatMenu from './builder-tidy-repeat-menu.html?raw'
import screenBuilderTidyNameError from './builder-tidy-name-error.html?raw'
import screenBuilderTidyEmptyOptionalBlocks from './builder-tidy-empty-optional-blocks.html?raw'
import screenBuilderTidyRepeatHeader from './builder-tidy-repeat-header.html?raw'
import screenBuilderTidyRepeatAddMenu from './builder-tidy-repeat-add-menu.html?raw'
import screenBuilderTidyRepeatCountSheet from './builder-tidy-repeat-count-sheet.html?raw'
import screenBuilderTidyRepeatEmpty from './builder-tidy-repeat-empty.html?raw'
import screenBuilderStepMenu from './builder-step-menu.html?raw'

// The Builder screens of the apps (ADR 0029), drawn with the phone stylesheet (../screens.css).
// CLEANUP: the screens marked a11y todo carry axe findings the old mockups had (contrast of hints and past days, names of fields); fix them with the screens, then drop the todo.
const meta = {
  title: "Mobile/Screens/Builder",
  tags: ['!autodocs', 'agreed'],
  parameters: { layout: 'fullscreen', mockup: {}, docs: { canvas: { sourceState: 'none' } } },
} satisfies Meta
export default meta
type Story = StoryObj<typeof meta>

/** Builder · the format line is a control that opens the format sheet. */
export const BuilderChangeFormat: Story = {
  name: "Change the format",
  tags: ['!autodocs', 'agreed'],
  ...screen(screenBuilderChangeFormat, { reorder: true }),
  parameters: { mockup: { issue: 243 } },
}
/** Builder · the format sheet with the current format selected. */
export const BuilderChangeFormatSheet: Story = {
  name: "Change the format, sheet",
  tags: ['!autodocs', 'agreed'],
  ...screen(screenBuilderChangeFormatSheet, { reorder: true }),
  parameters: { mockup: { issue: 243 } },
}
/** Builder · after picking the other format: follow the new format or keep the order. */
export const BuilderChangeFormatOrder: Story = {
  name: "Change the format, order",
  tags: ['!autodocs', 'agreed'],
  ...screen(screenBuilderChangeFormatOrder, { reorder: true }),
  parameters: { mockup: { issue: 243 } },
}
/** Builder · circuit, edit a step. */
export const BuilderCircuitEditStep: Story = {
  name: "Circuit, edit a step",
  tags: ['!autodocs', 'agreed'],
  ...screen(screenBuilderCircuitEditStep, { reorder: true }),
  parameters: { a11y: { test: 'todo' }, mockup: { issue: 90 } },
}
/** Builder · circuit in parts. */
export const BuilderCircuitParts: Story = {
  name: "Circuit in parts",
  tags: ['!autodocs', 'agreed'],
  ...screen(screenBuilderCircuitParts, { reorder: true }),
  parameters: { mockup: { issue: 90 } },
}
/** Builder · type a distance. */
export const BuilderDistanceSheet: Story = {
  name: "Type a distance",
  tags: ['!autodocs', 'agreed'],
  ...screen(screenBuilderDistanceSheet, { reorder: true }),
  parameters: { mockup: { issue: 90 } },
}
/** Builder · run in parts. */
export const BuilderRunParts: Story = {
  name: "Run in parts",
  tags: ['!autodocs', 'agreed'],
  ...screen(screenBuilderRunParts, { reorder: true }),
  parameters: { mockup: { issue: 90 } },
}
/** Builder · run, edit a step. */
export const BuilderRunTarget: Story = {
  name: "Run, edit a step",
  tags: ['!autodocs', 'agreed'],
  ...screen(screenBuilderRunTarget, { reorder: true }),
  parameters: { a11y: { test: 'todo' }, mockup: { issue: 90 } },
}
/** Builder · sets, edit a step. */
export const BuilderSetsEditStep: Story = {
  name: "Sets, edit a step",
  tags: ['!autodocs', 'agreed'],
  ...screen(screenBuilderSetsEditStep, { reorder: true }),
  parameters: { a11y: { test: 'todo' }, mockup: { issue: 90 } },
}
/** Builder · sets with a superset. */
export const BuilderSetsSuperset: Story = {
  name: "Sets with a superset",
  tags: ['!autodocs', 'agreed'],
  ...screen(screenBuilderSetsSuperset, { reorder: true }),
  parameters: { mockup: { issue: 151 } },
}
/** Builder · step actions. */
export const BuilderStepMenu: Story = {
  name: "Step actions",
  tags: ['!autodocs', 'agreed'],
  ...screen(screenBuilderStepMenu, { reorder: true }),
  parameters: { mockup: { issue: 90 } },
}


/** Builder · a new workout with nothing in it yet; the warm-up and cool down are always there, empty and optional. */
export const BuilderTidyEmpty: Story = {
  name: "Empty workout",
  tags: ['!autodocs', 'agreed'],
  ...screen(screenBuilderTidyEmpty, { reorder: true }),
  parameters: { mockup: {} },
}


/** Builder · the + of a block offers an exercise, a repeat or a rest. */
export const BuilderTidyAddMenu: Story = {
  name: "Add menu",
  tags: ['!autodocs', 'agreed'],
  ...screen(screenBuilderTidyAddMenu, { reorder: true }),
  parameters: { mockup: {} },
}

/** Builder · the actions of a repeat. */
export const BuilderTidyRepeatMenu: Story = {
  name: "Repeat menu",
  tags: ['!autodocs', 'agreed'],
  ...screen(screenBuilderTidyRepeatMenu, { reorder: true }),
  parameters: { mockup: {} },
}

/** Builder · saving without a name. */
export const BuilderTidyNameError: Story = {
  name: "Name missing",
  tags: ['!autodocs', 'agreed'],
  ...screen(screenBuilderTidyNameError, { reorder: true }),
  parameters: { mockup: {} },
}

/** Builder · a Work part with nothing planned before or after it. */
export const BuilderTidyEmptyOptionalBlocks: Story = {
  name: "Nothing planned before and after",
  tags: ['!autodocs', 'agreed'],
  ...screen(screenBuilderTidyEmptyOptionalBlocks, { reorder: true }),
  parameters: { mockup: {} },
}

/** Builder · a repeat's one-line title "Repeat 3 times" opens the count sheet; the ⋮ holds the actions, adding included. */
export const BuilderTidyRepeatHeader: Story = {
  name: "Repeat header",
  tags: ['!autodocs', 'draft'],
  ...screen(screenBuilderTidyRepeatHeader, { reorder: true }),
  parameters: { mockup: {} },
}

/** Builder · the ⋮ of a repeat starts with Add an exercise and Add a rest (never a repeat inside a repeat), then the usual actions. */
export const BuilderTidyRepeatAddMenu: Story = {
  name: "Repeat menu with add",
  tags: ['!autodocs', 'draft'],
  ...screen(screenBuilderTidyRepeatAddMenu, { reorder: true }),
  parameters: { mockup: {} },
}

/** Builder · tapping the title or the count of a repeat opens a sheet with a number field. */
export const BuilderTidyRepeatCountSheet: Story = {
  name: "Repeat count sheet",
  tags: ['!autodocs', 'draft'],
  ...screen(screenBuilderTidyRepeatCountSheet, { reorder: true }),
  parameters: { mockup: {} },
}

/** Builder · a repeat with nothing in it says "Tap to add an exercise." like the other blocks. */
export const BuilderTidyRepeatEmpty: Story = {
  name: "Empty repeat",
  tags: ['!autodocs', 'draft'],
  ...screen(screenBuilderTidyRepeatEmpty, { reorder: true }),
  parameters: { mockup: {} },
}
