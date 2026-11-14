import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { expect, userEvent, within } from 'storybook/test'
import { EllipsisVertical } from '@lucide/vue'
import { CpDropdownMenu } from '@coach-poulpy/ui'
import doc from './CpDropdownMenu.md?raw'

const meta = {
  title: 'Navigation/DropdownMenu',
  parameters: { docs: { description: { component: doc } } },
  component: CpDropdownMenu,
  args: { label: 'More actions' },
  render: (args) => ({
    components: { CpDropdownMenu, EllipsisVertical },
    setup: () => ({ args }),
    template: `<CpDropdownMenu v-bind="args">
      <template #trigger><EllipsisVertical class="size-5" aria-hidden="true" /></template>
      <li><a>Rename</a></li><li><a>Duplicate</a></li>
    </CpDropdownMenu>`,
  }),
} satisfies Meta<typeof CpDropdownMenu>
export default meta
type Story = StoryObj<typeof meta>

/** The menu is shut on load. */
export const Closed: Story = {
  play: async ({ canvasElement }) => {
    expect(canvasElement.querySelector('details')).not.toHaveAttribute('open')
  },
}
export const Open: Story = {
  play: async ({ canvasElement }) => {
    await userEvent.click(within(canvasElement).getByLabelText('More actions'))
    expect(canvasElement.querySelector('details')).toHaveAttribute('open')
  },
}
export const ChoosingClosesTheMenu: Story = {
  play: async ({ canvasElement }) => {
    await userEvent.click(within(canvasElement).getByLabelText('More actions'))
    await userEvent.click(within(canvasElement).getByText('Rename'))
    expect(canvasElement.querySelector('details')).not.toHaveAttribute('open')
  },
}
