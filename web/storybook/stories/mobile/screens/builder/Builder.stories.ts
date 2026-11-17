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
import screenBuilderStepMenu from './builder-step-menu.html?raw'

// The Builder screens of the apps (ADR 0029), drawn with the phone stylesheet (../screens.css).
// CLEANUP: the screens marked a11y todo carry axe findings the old mockups had (contrast of hints and past days, names of fields); fix them with the screens, then drop the todo.
const meta = {
  title: "Mobile/Screens/Builder",
  tags: ['!autodocs', 'draft'],
  parameters: { layout: 'fullscreen', mockup: {}, docs: { canvas: { sourceState: 'none' } } },
} satisfies Meta
export default meta
type Story = StoryObj<typeof meta>

/** Builder · the format line is a control that opens the format sheet. */
export const BuilderChangeFormat: Story = {
  name: "Change the format",
  tags: ['!autodocs', 'draft'],
  ...screen(screenBuilderChangeFormat, { reorder: true }),
  parameters: { mockup: { issue: 243 } },
}
/** Builder · the format sheet with the current format selected. */
export const BuilderChangeFormatSheet: Story = {
  name: "Change the format, sheet",
  tags: ['!autodocs', 'draft'],
  ...screen(screenBuilderChangeFormatSheet, { reorder: true }),
  parameters: { mockup: { issue: 243 } },
}
/** Builder · after picking the other format: follow the new format or keep the order. */
export const BuilderChangeFormatOrder: Story = {
  name: "Change the format, order",
  tags: ['!autodocs', 'draft'],
  ...screen(screenBuilderChangeFormatOrder, { reorder: true }),
  parameters: { mockup: { issue: 243 } },
}
/** Builder · circuit, edit a step. */
export const BuilderCircuitEditStep: Story = {
  name: "Circuit, edit a step",
  tags: ['!autodocs', 'draft'],
  ...screen(screenBuilderCircuitEditStep, { reorder: true }),
  parameters: { a11y: { test: 'todo' }, mockup: { issue: 90 } },
}
/** Builder · circuit in parts. */
export const BuilderCircuitParts: Story = {
  name: "Circuit in parts",
  tags: ['!autodocs', 'draft'],
  ...screen(screenBuilderCircuitParts, { reorder: true }),
  parameters: { mockup: { issue: 90 } },
}
/** Builder · type a distance. */
export const BuilderDistanceSheet: Story = {
  name: "Type a distance",
  tags: ['!autodocs', 'draft'],
  ...screen(screenBuilderDistanceSheet, { reorder: true }),
  parameters: { mockup: { issue: 90 } },
}
/** Builder · run in parts. */
export const BuilderRunParts: Story = {
  name: "Run in parts",
  tags: ['!autodocs', 'draft'],
  ...screen(screenBuilderRunParts, { reorder: true }),
  parameters: { mockup: { issue: 90 } },
}
/** Builder · run, edit a step. */
export const BuilderRunTarget: Story = {
  name: "Run, edit a step",
  tags: ['!autodocs', 'draft'],
  ...screen(screenBuilderRunTarget, { reorder: true }),
  parameters: { a11y: { test: 'todo' }, mockup: { issue: 90 } },
}
/** Builder · sets, edit a step. */
export const BuilderSetsEditStep: Story = {
  name: "Sets, edit a step",
  tags: ['!autodocs', 'draft'],
  ...screen(screenBuilderSetsEditStep, { reorder: true }),
  parameters: { a11y: { test: 'todo' }, mockup: { issue: 90 } },
}
/** Builder · sets with a superset. */
export const BuilderSetsSuperset: Story = {
  name: "Sets with a superset",
  tags: ['!autodocs', 'draft'],
  ...screen(screenBuilderSetsSuperset, { reorder: true }),
  parameters: { mockup: { issue: 151 } },
}
/** Builder · step actions. */
export const BuilderStepMenu: Story = {
  name: "Step actions",
  tags: ['!autodocs', 'draft'],
  ...screen(screenBuilderStepMenu, { reorder: true }),
  parameters: { mockup: { issue: 90 } },
}

