import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { expect } from 'storybook/test'
import { Dumbbell } from '@lucide/vue'
import { CpIcon, cpSizes } from '@coach-poulpy/ui'
import { iconPx, sizeArgType } from './sizes'
import doc from './CpIcon.md?raw'

const meta = {
  title: 'Web/Components/Actions/Icon',
  parameters: { docs: { description: { component: doc } } },
  component: CpIcon,
  argTypes: { size: sizeArgType },
  args: { icon: Dumbbell },
} satisfies Meta<typeof CpIcon>
export default meta
type Story = StoryObj<typeof meta>

/** Without a `size` an icon is md, 20 wide. */
export const Default: Story = {
  play: async ({ canvasElement }) => {
    expect(Math.round(canvasElement.querySelector('svg')!.getBoundingClientRect().width)).toBe(iconPx.md)
  },
}
/** Every step of the size scale, xs to xxl. */
export const Sizes: Story = {
  render: (args) => ({
    components: { CpIcon },
    setup: () => ({ args, cpSizes }),
    template: '<div class="flex items-end gap-4"><div v-for="s in cpSizes" :key="s" class="flex flex-col items-center gap-1"><CpIcon v-bind="args" :size="s" /><span class="cp-type-label">{{ s }}</span></div></div>',
  }),
  play: async ({ canvasElement }) => {
    const widths = [...canvasElement.querySelectorAll('svg')].map((svg) => Math.round(svg.getBoundingClientRect().width))
    expect(widths).toEqual(Object.values(iconPx))
  },
}
