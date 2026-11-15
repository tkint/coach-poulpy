import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { expect, userEvent, within } from 'storybook/test'
import { ref } from 'vue'
import { CpButton, CpDialog } from '@coach-poulpy/ui'
import doc from './CpDialog.md?raw'

const meta = {
  title: 'Web/Components/Overlays/Dialog',
  parameters: { docs: { description: { component: doc } } },
  component: CpDialog,
  args: {
    title: 'Publish catalog changes',
    description: 'Review the changes before publishing. Phones download the updated catalog the next time they open the app, guests included.',
    closeLabel: 'Close publication review',
  },
  render: (args) => ({
    components: { CpDialog, CpButton },
    setup: () => ({ args, open: ref(Boolean(args.open)) }),
    template: `<div class="min-h-80">
      <CpButton variant="outline" @click="open = true">Open the dialog</CpButton>
      <CpDialog v-bind="args" v-model:open="open">
        <p>Goblet squat · French name changed.</p>
        <template #actions>
          <CpButton variant="ghost" size="md" @click="open = false">Cancel</CpButton>
          <CpButton size="md" @click="open = false">Publish changes</CpButton>
        </template>
      </CpDialog>
    </div>`,
  }),
} satisfies Meta<typeof CpDialog>
export default meta
type Story = StoryObj<typeof meta>

/** The wide one holds a table; the rising one is the retirement review, a sheet on a phone. */
export const Wide: Story = { args: { wide: true, open: true } }
export const Rising: Story = { args: { rising: true, open: true } }

/** A dialog never shows itself on load. */
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
export const OpensFromItsButtonAndClosesWithCancel: Story = {
  play: async ({ canvasElement }) => {
    await userEvent.click(within(canvasElement).getByRole('button', { name: 'Open the dialog' }))
    expect(canvasElement.querySelector('dialog')).toHaveAttribute('open')
    await userEvent.click(within(canvasElement.querySelector('dialog')!).getByRole('button', { name: 'Cancel' }))
    expect(canvasElement.querySelector('dialog')).not.toHaveAttribute('open')
  },
}
