import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { renderComponent } from './components/story'

const meta = {
  title: 'Mobile/Components/Icon button',
  render: renderComponent('MobileIconButton'),
  args: { icon: 'x', ariaLabel: 'Close', filled: false },
  argTypes: {
    icon: { control: 'text', description: 'A Lucide icon name.' },
    ariaLabel: { control: 'text', description: 'Its accessible name; always set.' },
    floating: { control: 'inline-radio', options: [undefined, 'start', 'end'] },
  },
  parameters: { docs: { canvas: { sourceState: 'none' }, description: { component: '`MobileIconButton`: a round button holding one icon, always with an `aria-label`. `filled` over a picture or a header, `floating` to sit in a corner of the screen.' } } },
} satisfies Meta
export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {}
export const Filled: Story = { args: { icon: 'share-2', ariaLabel: 'Share this session', filled: true } }
export const FloatingBack: Story = {
  args: { icon: 'chevron-left', ariaLabel: 'Back', filled: true, floating: 'start' },
  render: renderComponent('MobileIconButton', 'position: relative; width: 390px; height: 96px; background: var(--cp-surface-variant);'),
}
export const FloatingMenu: Story = {
  args: { icon: 'ellipsis-vertical', ariaLabel: 'More options', filled: true, floating: 'end', ariaHaspopup: 'menu' },
  render: renderComponent('MobileIconButton', 'position: relative; width: 390px; height: 96px; background: var(--cp-surface-variant);'),
}
