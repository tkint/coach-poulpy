import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { screen } from '../screen'
import screenHomeDone from './home-done.html?raw'
import screenHomeInProgress from './home-in-progress.html?raw'
import screenHomeNext from './home-next.html?raw'
import screenHomeNothingPlanned from './home-nothing-planned.html?raw'
import screenHomePlanned from './home-planned.html?raw'

// The Home screens of the apps (ADR 0029), drawn with the phone stylesheet (../screens.css).
const meta = {
  title: "Mobile/Screens/Home",
  tags: ['!autodocs', 'agreed'],
  parameters: { layout: 'fullscreen', mockup: {}, docs: { canvas: { sourceState: 'none' } } },
} satisfies Meta
export default meta
type Story = StoryObj<typeof meta>

/** Home · done. */
export const HomeDone: Story = {
  name: "Done",
  ...screen(screenHomeDone),
  parameters: { mockup: { issue: 222 } },
}
/** Home · in progress. */
export const HomeInProgress: Story = {
  name: "In progress",
  ...screen(screenHomeInProgress),
  parameters: { mockup: { issue: 222 } },
}
/** Home · next session. */
export const HomeNext: Story = {
  name: "Next session",
  ...screen(screenHomeNext),
  parameters: { mockup: { issue: 222 } },
}
/** Home · nothing planned. */
export const HomeNothingPlanned: Story = {
  name: "Nothing planned",
  ...screen(screenHomeNothingPlanned),
  parameters: { mockup: { issue: 222 } },
}
/** Home · planned. */
export const HomePlanned: Story = {
  name: "Planned",
  ...screen(screenHomePlanned),
  parameters: { mockup: { issue: 222 } },
}
