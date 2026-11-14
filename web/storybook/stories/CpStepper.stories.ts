import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { expect, userEvent, within } from 'storybook/test'
import { ref } from 'vue'
import { CpStepper } from '@coach-poulpy/ui'

const meta = {
  title: 'Fields/Stepper',
  component: CpStepper,
  args: { label: 'Rest', valueText: '90 s', lessLabel: 'Less rest', moreLabel: 'More rest', step: 15, min: 30, max: 120 },
  render: (args) => ({
    components: { CpStepper },
    setup: () => ({ args, value: ref(90) }),
    template: '<CpStepper v-bind="args" v-model="value" />',
  }),
} satisfies Meta<typeof CpStepper>
export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {}
export const AtItsMinimum: Story = {
  render: (args) => ({
    components: { CpStepper },
    setup: () => ({ args, value: ref(30) }),
    template: '<CpStepper v-bind="args" v-model="value" />',
  }),
  play: async ({ canvasElement }) => {
    expect(within(canvasElement).getByRole('button', { name: 'Less rest' })).toBeDisabled()
  },
}
export const AtItsMaximum: Story = {
  render: (args) => ({
    components: { CpStepper },
    setup: () => ({ args, value: ref(120) }),
    template: '<CpStepper v-bind="args" v-model="value" />',
  }),
  play: async ({ canvasElement }) => {
    expect(within(canvasElement).getByRole('button', { name: 'More rest' })).toBeDisabled()
  },
}
