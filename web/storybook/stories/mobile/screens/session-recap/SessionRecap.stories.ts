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

/** Session recap · circuit. */
export const SessionRecapCircuit: Story = {
  name: "Circuit",
  ...screen(screenSessionRecapCircuit),
  parameters: { mockup: { issue: 223 } },
}
/** Session recap · treadmill run. */
export const SessionRecapRun: Story = {
  name: "Treadmill run",
  ...screen(screenSessionRecapRun),
  parameters: { mockup: { issue: 223 } },
}
/** Session recap · sets, list open. */
export const SessionRecapSetsFull: Story = {
  name: "Sets, list open",
  ...screen(screenSessionRecapSetsFull),
  parameters: { mockup: { issue: 223 } },
}
/** Session recap · sets. */
export const SessionRecapSets: Story = {
  name: "Sets",
  ...screen(screenSessionRecapSets),
  parameters: { mockup: { issue: 223 } },
}
/** Session recap · no sets logged. */
export const SessionRecapSummaryOnly: Story = {
  name: "No sets logged",
  ...screen(screenSessionRecapSummaryOnly),
  parameters: { mockup: { issue: 223 } },
}
/** Session recap · free recording. */
export const SessionRecapFree: Story = {
  name: "Free recording",
  tags: ['!autodocs', 'draft'],
  ...screen(screenSessionRecapFree),
  parameters: { mockup: { issue: 223 } },
}
