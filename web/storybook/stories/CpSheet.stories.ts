import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { expect, userEvent, within } from 'storybook/test'
import { ref } from 'vue'
import { CpButton, CpSheet } from '@coach-poulpy/ui'

const meta = {
  title: 'Overlays/Sheet',
  component: CpSheet,
  args: { title: 'Delete this workout?', closeLabel: 'Close' },
  render: (args, { argTypes }) => ({
    components: { CpSheet, CpButton },
    setup: () => ({ args, open: ref(Boolean(args.open)) }),
    template: `<div class="min-h-80">
      <CpButton variant="outline" @click="open = true">Open the sheet</CpButton>
      <CpSheet v-bind="args" v-model:open="open">
        <p>It cannot be undone.</p>
        <template #actions><CpButton variant="danger" @click="open = false">Delete</CpButton></template>
      </CpSheet>
    </div>`,
  }),
} satisfies Meta<typeof CpSheet>
export default meta
type Story = StoryObj<typeof meta>

/** A sheet never shows itself on load. */
export const Closed: Story = {
  play: async ({ canvasElement }) => {
    expect(canvasElement.querySelector('dialog')).not.toHaveAttribute('open')
  },
}
export const Open: Story = {
  args: { open: true },
  play: async ({ canvasElement }) => {
    expect(canvasElement.querySelector('dialog')).toHaveAttribute('open')
  },
}
export const OpensFromItsButton: Story = {
  play: async ({ canvasElement }) => {
    await userEvent.click(within(canvasElement).getByRole('button', { name: 'Open the sheet' }))
    expect(canvasElement.querySelector('dialog')).toHaveAttribute('open')
  },
}
