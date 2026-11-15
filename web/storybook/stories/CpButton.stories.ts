import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { expect, within } from 'storybook/test'
import { CpButton, cpSizes } from '@coach-poulpy/ui'
import { controlPx, sizeArgType } from './sizes'
import doc from './CpButton.md?raw'

const meta = {
  title: 'Web/Components/Actions/Button',
  parameters: { docs: { description: { component: doc } } },
  component: CpButton,
  argTypes: { size: sizeArgType },
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
/** Waiting for an answer: the label stays in place under a spinner, and clicks are ignored. */
export const Loading: Story = {
  args: { variant: 'primary', size: 'md', loading: true },
  play: async ({ canvasElement }) => {
    const button = within(canvasElement).getByRole('button')
    expect(button).toBeDisabled()
    expect(button).toHaveAttribute('aria-busy', 'true')
  },
}

/** Every step of the size scale, xs to xxl; md is what a button without a `size` draws. */
export const Sizes: Story = {
  render: (args) => ({
    components: { CpButton },
    setup: () => ({ args, cpSizes }),
    template: '<div class="flex flex-col items-start gap-4 pb-8"><div v-for="s in cpSizes" :key="s" class="flex items-center gap-4"><span class="cp-type-label w-10">{{ s }}</span><CpButton v-bind="args" :size="s">Start</CpButton></div></div>',
  }),
  play: async ({ canvasElement }) => {
    const heights = within(canvasElement).getAllByRole('button').map((button) => Math.round(button.getBoundingClientRect().height))
    expect(heights).toEqual(Object.values(controlPx))
  },
}
/** Without a `size` a button is md. */
export const DefaultsToMedium: Story = {
  play: async ({ canvasElement }) => {
    expect(Math.round(within(canvasElement).getByRole('button').getBoundingClientRect().height)).toBe(controlPx.md)
  },
}
