import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { renderComponent } from './components/story'

const meta = {
  title: 'Mobile/Components/Role tag',
  render: renderComponent('MobileRoleTag'),
  args: { label: 'Recover' },
  parameters: { docs: { canvas: { sourceState: 'none' }, description: { component: '`MobileRoleTag`: the word that marks a part\'s role in a workout.' } } },
} satisfies Meta
export default meta
type Story = StoryObj<typeof meta>

export const Recover: Story = {}
