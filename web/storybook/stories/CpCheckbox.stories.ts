import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { ref } from 'vue'
import { CpCheckbox } from '@coach-poulpy/ui'
import doc from './CpCheckbox.md?raw'

const meta = {
  title: 'Web/Components/Fields/Checkbox',
  parameters: { docs: { description: { component: doc } } },
  component: CpCheckbox,
  args: { label: 'Warm-up included' },
  render: (args) => ({
    components: { CpCheckbox },
    setup: () => ({ args, on: ref(Boolean(args.modelValue)) }),
    template: '<CpCheckbox v-bind="args" v-model="on" />',
  }),
} satisfies Meta<typeof CpCheckbox>
export default meta
type Story = StoryObj<typeof meta>

export const Unchecked: Story = {}
export const Checked: Story = { args: { modelValue: true } }
export const Indeterminate: Story = { args: { indeterminate: true } }
