import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { screen } from './screens/screen'

const meta = {
  title: 'Mobile/Components/Settings row',
  parameters: { docs: { canvas: { sourceState: 'none' }, description: { component: 'A row that opens a settings page: an icon, a title, one line of detail and a chevron. In the stories it is `MobileSettingsRow`.' } } },
} satisfies Meta
export default meta
type Story = StoryObj

export const Default: Story = screen(`<div style="width: 390px; padding: 16px;"><MobileSettingsRow icon="weight" title="Weight" detail="72.6 kg · logged 3 days ago" /></div>`)
export const WithPicture: Story = screen(`<div style="width: 390px; padding: 16px;"><MobileSettingsRow picture="../../../poulpy/mood_happy.svg" title="App icon" detail="Happy · default" /></div>`)
