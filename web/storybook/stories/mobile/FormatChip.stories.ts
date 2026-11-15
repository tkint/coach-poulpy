import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { renderComponent } from './components/story'

const meta = {
  title: 'Mobile/Components/Format chip',
  render: renderComponent('MobileFormatChip'),
  args: { icon: 'repeat', label: 'Circuit' },
  argTypes: { icon: { control: 'text', description: 'A Lucide icon name.' } },
  parameters: { docs: { canvas: { sourceState: 'none' }, description: { component: '`MobileFormatChip`: a small tag that names a thing\'s kind, such as a workout\'s format, with its icon. Not tappable.' } } },
} satisfies Meta
export default meta
type Story = StoryObj<typeof meta>

export const Circuit: Story = {}
export const Sets: Story = { args: { icon: 'list', label: 'Sets' } }
