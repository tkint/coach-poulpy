import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { screen } from '../screen'
import screenSessionShareCard from './session-share-card.html?raw'

// The Session summary screens of the apps (ADR 0029), drawn with the phone stylesheet (../screens.css).
const meta = {
  title: "Mobile/Screens/Session summary",
  tags: ['!autodocs', 'agreed'],
  parameters: { layout: 'fullscreen', mockup: {}, docs: { canvas: { sourceState: 'none' } } },
} satisfies Meta
export default meta
type Story = StoryObj<typeof meta>

/** Session share card. */
export const SessionShareCard: Story = {
  name: "Session share card",
  ...screen(screenSessionShareCard),
  parameters: { mockup: { issue: 125 } },
}
