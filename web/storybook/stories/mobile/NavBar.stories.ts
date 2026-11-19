import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { renderComponent } from './components/story'

const meta = {
  title: 'Mobile/Patterns/Nav bar',
  render: renderComponent('MobileNavBar', 'width: 390px; background: var(--cp-surface);'),
  args: { active: 'home' },
  argTypes: { active: { control: 'inline-radio', options: ['home', 'library', 'live', 'history', 'you'] } },
  parameters: { docs: { canvas: { sourceState: 'none' }, description: { component: '`MobileNavBar`: the bottom bar of the main screens, with the current destination marked.' } } },
} satisfies Meta
export default meta
type Story = StoryObj<typeof meta>

export const Home: Story = {}
export const Library: Story = { args: { active: 'library' } }
export const Live: Story = { args: { active: 'live' } }
export const History: Story = { args: { active: 'history' } }
export const You: Story = { args: { active: 'you' } }
