import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { expect, userEvent, within } from 'storybook/test'
import { CpBadge } from '@coach-poulpy/ui'
import doc from './CpBadge.md?raw'

const meta = {
  title: 'Display/Badge',
  parameters: { docs: { description: { component: doc } } },
  component: CpBadge,
  render: (args) => ({ components: { CpBadge }, setup: () => ({ args }), template: '<CpBadge v-bind="args">Configured</CpBadge>' }),
} satisfies Meta<typeof CpBadge>
export default meta
type Story = StoryObj<typeof meta>

export const Primary: Story = { args: { tone: 'primary' } }
export const Neutral: Story = { args: { tone: 'neutral' } }
export const Outline: Story = { args: { tone: 'outline' } }
// CLEANUP: the success colour is 3.1:1 on the light badge background, below 4.5:1; fix the badge's tone, then drop this exemption.
export const Success: Story = { parameters: { a11y: { test: 'todo' } }, args: { tone: 'success' } }
export const Error: Story = { args: { tone: 'error' } }
/** Under a field, saying where its translation stands. */
export const Small: Story = { args: { tone: 'neutral', small: true } }
