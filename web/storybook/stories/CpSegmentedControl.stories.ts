import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { expect, userEvent, within } from 'storybook/test'
import { ref } from 'vue'
import { CpSegmentedControl } from '@coach-poulpy/ui'
import doc from './CpSegmentedControl.md?raw'

const options = [
  { value: 'en', text: 'English' },
  { value: 'fr', text: 'Français' },
]
const meta = {
  title: 'Web/Components/Fields/SegmentedControl',
  component: CpSegmentedControl,
  parameters: { docs: { description: { component: doc } } },
  args: { label: 'Language', options },
  render: (args) => ({
    components: { CpSegmentedControl },
    setup: () => ({ args, value: ref('en') }),
    template: '<CpSegmentedControl v-bind="args" v-model="value" />',
  }),
} satisfies Meta<typeof CpSegmentedControl>
export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  play: async ({ canvasElement }) => {
    expect(within(canvasElement).getByRole('button', { name: 'English' })).toHaveAttribute('aria-current', 'true')
  },
}
/** A language some text still lacks carries a crimson icon, read aloud as its flag label. */
export const WithAFlaggedOption: Story = {
  args: { options: [options[0], { ...options[1], flagged: true, flagLabel: 'Text missing' }] },
  play: async ({ canvasElement }) => {
    expect(within(canvasElement).getByRole('button', { name: 'Français Text missing' })).toBeInTheDocument()
  },
}
export const Switching: Story = {
  play: async ({ canvasElement }) => {
    const french = within(canvasElement).getByRole('button', { name: 'Français' })
    await userEvent.click(french)
    expect(french).toHaveAttribute('aria-current', 'true')
  },
}
