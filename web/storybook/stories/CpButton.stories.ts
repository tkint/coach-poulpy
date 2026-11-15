import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { expect, userEvent, within } from 'storybook/test'
import { CpButton } from '@coach-poulpy/ui'
import doc from './CpButton.md?raw'

const meta = {
  title: 'Web/Components/Actions/Button',
  parameters: { docs: { description: { component: doc } } },
  component: CpButton,
  render: (args) => ({ components: { CpButton }, setup: () => ({ args }), template: '<CpButton v-bind="args">Start the session</CpButton>' }),
} satisfies Meta<typeof CpButton>
export default meta
type Story = StoryObj<typeof meta>

export const Primary: Story = { args: { variant: 'primary', size: 'xl' } }
export const Tonal: Story = { args: { variant: 'tonal', size: 'xl' } }
export const Outline: Story = { args: { variant: 'outline', size: 'xl' } }
/** A quieter cancel beside a save. */
export const Ghost: Story = { args: { variant: 'ghost', size: 'xl' } }
export const Grumpy: Story = { args: { variant: 'grumpy', size: 'xl' } }
/** Crimson, for a deletion that cannot be undone. */
export const Danger: Story = { args: { variant: 'danger', size: 'xl' } }
export const Medium: Story = { args: { variant: 'primary', size: 'md' } }
/** Waiting for an answer: the label stays in place under a spinner, and clicks are ignored. */
export const Loading: Story = {
  args: { variant: 'primary', size: 'md', loading: true },
  play: async ({ canvasElement }) => {
    const button = within(canvasElement).getByRole('button')
    expect(button).toBeDisabled()
    expect(button).toHaveAttribute('aria-busy', 'true')
  },
}
