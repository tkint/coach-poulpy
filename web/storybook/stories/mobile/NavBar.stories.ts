import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { renderComponent } from './components/story'

const meta = {
  title: 'Mobile/Patterns/Nav bar',
  render: renderComponent('MobileNavBar', 'width: 390px; background: var(--cp-surface);'),
  args: { active: 'home' },
  argTypes: { active: { control: 'inline-radio', options: ['home', 'library', 'live', 'history', 'settings', 'you'] }, destinations: { control: 'inline-radio', options: ['five', 'three'] } },
  parameters: { docs: { canvas: { sourceState: 'none' }, description: { component: '`MobileNavBar`: the bottom bar of the main screens, with the current destination marked.' } } },
} satisfies Meta
export default meta
type Story = StoryObj<typeof meta>

export const Home: Story = {}
export const Library: Story = { args: { active: 'library' } }
export const Live: Story = { args: { active: 'live' } }
export const History: Story = { args: { active: 'history' } }
export const Settings: Story = { args: { active: 'settings' } }
/** The new bar: Home, Library and You; Live is Home's Record button. */
export const Three: Story = { tags: ['agreed'], args: { active: 'you', destinations: 'three' } }
