import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { screen } from '../screen'
import screenSettings from './settings.html?raw'

// Settings, behind the cog on You: Appearance, Region (language and units) and Devices.
const meta = {
  title: 'Mobile/Screens/You/Settings',
  tags: ['!autodocs', 'agreed'],
  parameters: { layout: 'fullscreen', mockup: {}, docs: { canvas: { sourceState: 'none' } } },
} satisfies Meta
export default meta
type Story = StoryObj<typeof meta>

/** Settings · Appearance, Region (language and units) and Devices, all on the page, reached from the cog on You. */
export const Settings: Story = { name: 'Settings', ...screen(screenSettings) }
