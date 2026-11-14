import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { screen } from './screens/screen'

const meta = {
  title: 'Mobile/Components/Nav bar',
  parameters: { docs: { canvas: { sourceState: 'none' }, description: { component: 'The bottom bar of the main screens: Home, Library, Live, History and Settings, one of them current. In the stories it is `MobileNavBar` with `active`.' } } },
} satisfies Meta
export default meta
type Story = StoryObj

const bar = (active: string): Story => screen(`<div style="width: 390px; background: var(--cp-surface);"><MobileNavBar active="${active}" /></div>`)

export const Home: Story = bar('home')
export const Library: Story = bar('library')
export const Live: Story = bar('live')
export const History: Story = bar('history')
export const Settings: Story = bar('settings')
