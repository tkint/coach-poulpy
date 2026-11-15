import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { renderComponent } from './components/story'

const meta = {
  title: 'Mobile/Components/Info card',
  render: renderComponent('MobileInfoCard', 'width: 390px; padding: 16px;'),
  args: { label: 'Choosing a new password signs every phone out.', icon: 'info' },
  argTypes: { icon: { control: 'text', description: 'A Lucide icon name.' }, iconSize: { control: 'number' } },
  parameters: { docs: { canvas: { sourceState: 'none' }, description: { component: '`MobileInfoCard`: a calm note in the primary container, an icon and a sentence or two that explain what happens next. Its content is the sentence.' } } },
} satisfies Meta
export default meta
type Story = StoryObj<typeof meta>

export const Info: Story = {}
export const Done: Story = { args: { label: 'Password changed. Sign in with the new one.', icon: 'check', iconSize: 18 } }
