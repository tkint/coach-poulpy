import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { screen } from './screens/screen'

const meta = {
  title: 'Mobile/Components/Stat tile',
  parameters: { docs: { canvas: { sourceState: 'none' }, description: { component: 'A small figure under a caption: how many times, when last, how long. In the stories it is `MobileStatTile`.' } } },
} satisfies Meta
export default meta
type Story = StoryObj

export const Default: Story = screen(`<div style="width: 390px; padding: 16px; display: flex; gap: 8px;"><MobileStatTile label="Done" value="3 times" /><MobileStatTile label="Last time" value="Wed 21" /><MobileStatTile label="Usually" value="31 min" /></div>`)
