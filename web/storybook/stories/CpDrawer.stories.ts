import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { expect, userEvent, within } from 'storybook/test'
import { Dumbbell, Menu } from '@lucide/vue'
import { CpDrawer, CpDrawerItem } from '@coach-poulpy/ui'
import doc from './CpDrawer.md?raw'

const meta = {
  title: 'Navigation/Drawer',
  component: CpDrawer,
  args: { toggleLabel: 'Toggle the menu', closeLabel: 'Close the menu' },
  // CLEANUP: the toggle is a <label> with an aria-label, which ARIA does not allow on a label; give the checkbox the name and
  // hide the label's icon from assistive technology, then drop this exemption.
  parameters: { docs: { description: { component: doc } }, a11y: { test: 'todo' } },
  render: (args) => ({
    components: { CpDrawer, CpDrawerItem, Menu, Dumbbell },
    setup: () => ({ args }),
    template: `<CpDrawer v-bind="args">
      <template #toggle-icon><Menu class="size-5" aria-hidden="true" /></template>
      <template #title>Coach Poulpy</template>
      <main class="p-4">Page content</main>
      <template #sidebar>
        <ul class="cp-menu w-full grow gap-1">
          <CpDrawerItem label="Exercises" active><template #icon><Dumbbell class="size-5" aria-hidden="true" /></template></CpDrawerItem>
        </ul>
      </template>
    </CpDrawer>`,
  }),
} satisfies Meta<typeof CpDrawer>
export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  play: async ({ canvasElement }) => {
    expect(within(canvasElement).getByLabelText('Toggle the menu', { selector: 'input' })).toBeInTheDocument()
  },
}
