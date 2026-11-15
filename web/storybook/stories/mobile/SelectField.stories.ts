import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { renderComponent } from './components/story'

const meta = {
  title: 'Mobile/Components/Select field',
  render: renderComponent('MobileSelectField', 'width: 390px; padding: 16px;', { content: '' }),
  args: { label: 'Theme', value: 'System', opens: 'page' },
  argTypes: { opens: { control: 'inline-radio', options: ['page', 'list'] } },
  parameters: { docs: { canvas: { sourceState: 'none' }, description: { component: '`MobileSelectField`: a field that shows its value and opens a choice. `opens="page"` (a chevron right) opens a page or sheet of its own; `opens="list"` (a chevron down) opens a list over the screen. With no `value` it shows a muted "Not set".' } } },
} satisfies Meta
export default meta
type Story = StoryObj<typeof meta>

export const OpensPage: Story = {}
export const OpensList: Story = { args: { label: 'Measured by', value: 'Reps', opens: 'list' } }
export const NotSet: Story = { args: { label: 'Sex assigned at birth', value: '' } }
export const LongValue: Story = { args: { label: 'Pain or rehab', value: 'Left knee, left hip · until 4 Nov' } }
