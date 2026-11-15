import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { renderComponent } from './components/story'

const meta = {
  title: 'Mobile/Components/Exercise row',
  render: renderComponent('MobileExerciseRow', 'width: 390px; padding: 16px;'),
  args: { icon: 'person-standing', name: 'Jumping jack', detail: '30 s', link: false, info: false },
  argTypes: {
    icon: { control: 'text', description: 'The Lucide icon of its movement pattern.' },
    time: { control: 'text' },
  },
  parameters: { docs: { canvas: { sourceState: 'none' }, description: { component: '`MobileExerciseRow`: one exercise in a list, its pattern icon, its name and one line of detail. `link` in the library and search, where the row opens the exercise; otherwise a line of a workout or a recap, ending with its `time` or the `info` button.' } } },
} satisfies Meta
export default meta
type Story = StoryObj<typeof meta>

export const Link: Story = { args: { link: true, icon: 'hand-grab', name: 'Glute bridge', detail: 'Glutes, hamstrings · Mat' } }
export const WithTime: Story = { args: { icon: 'dumbbell', name: 'One-arm dumbbell row', detail: '3 × 10 · 14 kg', time: '3:30' } }
export const WithInfo: Story = { args: { info: true } }
export const Plain: Story = {}
