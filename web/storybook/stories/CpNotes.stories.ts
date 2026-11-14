import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { expect, within } from 'storybook/test'
import { CpErrorNote, CpSuccessNote } from '@coach-poulpy/ui'

const meta = {
  title: 'Display/Notes',
  render: (args: { kind: 'error' | 'success' }) => ({
    components: { CpErrorNote, CpSuccessNote },
    setup: () => ({ args }),
    template: `<CpErrorNote v-if="args.kind === 'error'">The link has expired.</CpErrorNote><CpSuccessNote v-else>Your password is changed.</CpSuccessNote>`,
  }),
} satisfies Meta<{ kind: 'error' | 'success' }>
export default meta
type Story = StoryObj<typeof meta>

/** Crimson with an icon and words, never ember. */
export const Error: Story = {
  args: { kind: 'error' },
  play: async ({ canvasElement }) => {
    expect(within(canvasElement).getByRole('alert')).toHaveTextContent('The link has expired.')
  },
}
export const Success: Story = { args: { kind: 'success' } }
