import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { renderComponent } from './components/story'

const meta = {
  title: 'Mobile/Patterns/Nav bar',
  render: renderComponent('MobileNavBar', 'width: 390px; background: var(--cp-surface);'),
  args: { active: 'home' },
  argTypes: { active: { control: 'inline-radio', options: ['home', 'library', 'you'] } },
  parameters: { docs: { canvas: { sourceState: 'none' }, description: { component: '`MobileNavBar`: the bottom bar of the main screens: Home, Library and You, with the current destination marked.' } } },
} satisfies Meta
export default meta
type Story = StoryObj<typeof meta>

export const Home: Story = {}
export const Library: Story = { args: { active: 'library' } }
/** Home, Library and You; Live is Home's Record button. */
export const You: Story = { tags: ['agreed'], args: { active: 'you' } }
