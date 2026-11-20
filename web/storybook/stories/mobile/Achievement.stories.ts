import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { renderComponent } from './components/story'

const meta = {
  title: 'Mobile/Components/Achievement',
  tags: ['agreed'],
  render: renderComponent('MobileAchievement', 'padding: 16px; display: flex;'),
  args: { icon: 'award', name: 'First record' },
  parameters: { docs: { canvas: { sourceState: 'none' }, description: { component: '`MobileAchievement`: one achievement as a round token, earned in the primary colour with its Lucide icon, or locked as a quiet "?" so the next one stays a surprise. Small (28) in the motivation row, large (64) on Achievements.' } } },
} satisfies Meta
export default meta
type Story = StoryObj<typeof meta>

export const Earned: Story = {}
export const Locked: Story = { args: { locked: true, name: '' } }
export const Large: Story = { args: { size: 'lg' } }
