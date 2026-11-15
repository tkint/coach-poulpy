import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { renderComponent } from './components/story'

const frame = 'width: 390px; padding: 16px; display: flex; flex-direction: column;'

const meta = {
  title: 'Mobile/Components/Option row',
  render: renderComponent('MobileOptionRow', frame),
  args: { label: 'Prefer not to say', control: 'radio', name: 'sex', checked: false },
  argTypes: {
    control: { control: 'inline-radio', options: [undefined, 'radio', 'checkbox'] },
    icon: { control: 'text', description: 'A Lucide icon name.' },
    picture: { control: 'text' },
    hint: { control: 'text' },
  },
  parameters: { docs: { canvas: { sourceState: 'none' }, description: { component: '`MobileOptionRow`: one choice in a sheet\'s list. With a `control` it is a label around a radio or checkbox; without one it is a button that acts at once. An `icon` or a `picture` can lead it, and a `hint` adds a line under the label.' } } },
} satisfies Meta
export default meta
type Story = StoryObj<typeof meta>

export const Radio: Story = {}
export const RadioChecked: Story = { args: { checked: true } }
export const Checkbox: Story = { args: { label: 'Build strength', control: 'checkbox', name: 'goals', checked: true } }
export const WithPicture: Story = { args: { label: 'Happy · default', name: 'app-icon', value: 'happy', checked: true, picture: '../../../poulpy/mood_happy.svg' } }
export const Action: Story = { args: { control: undefined, name: undefined, label: 'Image', icon: 'image', hint: 'A card with your numbers to send or post' } }
