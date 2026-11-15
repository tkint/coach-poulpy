import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { renderComponent } from './components/story'

const meta = {
  title: 'Mobile/Components/Stat tile',
  render: renderComponent('MobileStatTile', 'width: 160px; padding: 16px; display: flex;'),
  args: { label: 'Done', value: '3 times' },
  parameters: { docs: { canvas: { sourceState: 'none' }, description: { component: '`MobileStatTile`: a small figure under a caption, in a row of two or three.' } } },
} satisfies Meta
export default meta
type Story = StoryObj<typeof meta>

export const Count: Story = {}
export const Date: Story = { args: { label: 'Last time', value: 'Wed 21' } }
export const Duration: Story = { args: { label: 'Usually', value: '31 min' } }
