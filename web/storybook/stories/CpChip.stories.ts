import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { expect, userEvent, within } from 'storybook/test'
import { ref } from 'vue'
import { CpChip } from '@coach-poulpy/ui'
import doc from './CpChip.md?raw'

const meta = {
  title: 'Web/Components/Fields/Chip',
  parameters: { docs: { description: { component: doc } } },
  component: CpChip,
  render: (args) => ({
    components: { CpChip },
    setup: () => ({ args, on: ref(Boolean(args.modelValue)) }),
    template: '<CpChip v-bind="args" v-model="on">Legs</CpChip>',
  }),
} satisfies Meta<typeof CpChip>
export default meta
type Story = StoryObj<typeof meta>

export const Off: Story = {
  play: async ({ canvasElement }) => {
    expect(within(canvasElement).getByRole('button')).toHaveAttribute('aria-pressed', 'false')
  },
}
export const On: Story = { args: { modelValue: true } }
export const Compact: Story = { args: { compact: true } }
export const Toggles: Story = {
  play: async ({ canvasElement }) => {
    const chip = within(canvasElement).getByRole('button')
    await userEvent.click(chip)
    expect(chip).toHaveAttribute('aria-pressed', 'true')
  },
}
