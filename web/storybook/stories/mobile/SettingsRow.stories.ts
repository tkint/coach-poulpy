import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { renderComponent } from './components/story'

const meta = {
  title: 'Mobile/Components/Settings row',
  render: renderComponent('MobileSettingsRow', 'width: 390px; padding: 16px;'),
  args: { icon: 'weight', title: 'Weight', detail: '72.6 kg · logged 3 days ago' },
  argTypes: { icon: { control: 'text', description: 'A Lucide icon name.' }, picture: { control: 'text' } },
  parameters: { docs: { canvas: { sourceState: 'none' }, description: { component: '`MobileSettingsRow`: a row that opens a settings page. An icon (or a picture), a title, one line of detail and a chevron.' } } },
} satisfies Meta
export default meta
type Story = StoryObj<typeof meta>

export const WithIcon: Story = {}
export const WithPicture: Story = { args: { icon: undefined, picture: '../../../poulpy/mood_happy.svg', title: 'App icon', detail: 'Happy · default' } }
