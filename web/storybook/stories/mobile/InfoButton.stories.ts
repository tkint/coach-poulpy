import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { renderComponent } from './components/story'

const meta = {
  title: 'Mobile/Components/Info button',
  render: renderComponent('MobileInfoButton'),
  args: { ariaLabel: 'Details of Push-up' },
  argTypes: { ariaLabel: { control: 'text', description: 'Names what it gives the details of; always set.' } },
  parameters: { docs: { canvas: { sourceState: 'none' }, description: { component: '`MobileInfoButton`: the small "i" that opens the details of what it sits next to, in a sheet.' } } },
} satisfies Meta
export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {}
