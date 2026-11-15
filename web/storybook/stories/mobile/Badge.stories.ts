import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { renderComponent } from './components/story'

const meta = {
  title: 'Mobile/Components/Badge',
  render: renderComponent('MobileBadge', 'width: 390px; padding: 16px; display: flex; gap: 6px; list-style: none; margin: 0;', { wrapper: 'ul' }),
  args: { label: 'Dumbbells', icon: 'dumbbell', format: false },
  argTypes: { icon: { control: 'text', description: 'A Lucide icon name.' } },
  parameters: { docs: { canvas: { sourceState: 'none' }, description: { component: '`MobileBadge`: one item in the row of badges that sums a workout up. The format is highlighted (`format`); what the workout needs is outlined. It is a list item: put it in a `ul`.' } } },
} satisfies Meta
export default meta
type Story = StoryObj<typeof meta>

export const Equipment: Story = {}
export const Format: Story = { args: { label: 'Strength', icon: 'zap', format: true } }
