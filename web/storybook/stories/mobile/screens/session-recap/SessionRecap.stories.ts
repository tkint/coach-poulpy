import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { screen } from '../screen'
import screenSessionRecapCircuit from './session-recap-circuit.html?raw'
import screenSessionRecapRun from './session-recap-run.html?raw'
import screenSessionRecapSetsFull from './session-recap-sets-full.html?raw'
import screenSessionRecapSets from './session-recap-sets.html?raw'
import screenSessionRecapSummaryOnly from './session-recap-summary-only.html?raw'
import screenSessionRecapFree from './session-recap-free.html?raw'

// The Session recap screens of the apps (ADR 0029), drawn with the phone stylesheet (../screens.css).
const meta = {
  title: "Mobile/Screens/Session recap",
  tags: ['!autodocs', 'agreed'],
  parameters: { layout: 'fullscreen', mockup: {}, docs: { canvas: { sourceState: 'none' } } },
} satisfies Meta
export default meta
type Story = StoryObj<typeof meta>

/** Session recap · circuit. Was design/mockups/agreed/phone/session-recap-circuit.html. */
export const SessionRecapCircuit: Story = {
  name: "Circuit",
  ...screen(screenSessionRecapCircuit),
  parameters: { mockup: { issue: 223 } },
}
/** Session recap · treadmill run. Was design/mockups/agreed/phone/session-recap-run.html. */
export const SessionRecapRun: Story = {
  name: "Treadmill run",
  ...screen(screenSessionRecapRun),
  parameters: { mockup: { issue: 223 } },
}
/** Session recap · sets, list open. Was design/mockups/agreed/phone/session-recap-sets-full.html. */
export const SessionRecapSetsFull: Story = {
  name: "Sets, list open",
  ...screen(screenSessionRecapSetsFull),
  parameters: { mockup: { issue: 223 } },
}
/** Session recap · sets. Was design/mockups/agreed/phone/session-recap-sets.html. */
export const SessionRecapSets: Story = {
  name: "Sets",
  ...screen(screenSessionRecapSets),
  parameters: { mockup: { issue: 223 } },
}
/** Session recap · no sets logged. Was design/mockups/agreed/phone/session-recap-summary-only.html. */
export const SessionRecapSummaryOnly: Story = {
  name: "No sets logged",
  ...screen(screenSessionRecapSummaryOnly),
  parameters: { mockup: { issue: 223 } },
}
/** Session recap · free recording. Was design/mockups/drafts/phone/session-recap-free.html. */
export const SessionRecapFree: Story = {
  name: "Free recording",
  tags: ['!autodocs', 'draft'],
  ...screen(screenSessionRecapFree),
  parameters: { mockup: { issue: 223 } },
}
