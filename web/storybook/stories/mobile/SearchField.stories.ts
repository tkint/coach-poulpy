import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { renderComponent } from './components/story'

const meta = {
  title: 'Mobile/Components/Search field',
  render: renderComponent('MobileSearchField', 'width: 390px; padding: 16px;'),
  args: { label: 'Search your workouts', placeholder: 'Name, type or exercise', value: '' },
  parameters: { docs: { canvas: { sourceState: 'none' }, description: { component: '`MobileSearchField`: a magnifier, the field and, once something is typed, a button that clears it. `label` is the field\'s accessible name.' } } },
} satisfies Meta
export default meta
type Story = StoryObj<typeof meta>

export const Empty: Story = {}
export const Filled: Story = { args: { value: 'squat' } }
