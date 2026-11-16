import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { screen } from '../screen'
import screenHomeChampionTap from './home-champion-tap.html?raw'
import screenHomeDone from './home-done.html?raw'
import screenHomeInProgress from './home-in-progress.html?raw'
import screenHomeNext from './home-next.html?raw'
import screenHomeNothingPlanned from './home-nothing-planned.html?raw'
import screenHomePlanned from './home-planned.html?raw'
import screenHomePlannedTap from './home-planned-tap.html?raw'

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
/** Home · planned, with Poulpy playing when tapped (agreed by the owner: decision 16, step 4). He stays a still picture; a tap plays one short animation for his mood, here the happy one's jump, then he settles back. */
export const HomePlannedTap: Story = {
  name: "Planned, tap Poulpy",
  tags: ['!autodocs', 'agreed'],
  ...screen(screenHomePlannedTap),
  parameters: { mockup: { issue: 222 } },
}

/** Home · done, the eighth tentacle lit: Poulpy is the champion. He stays a still picture; a tap plays his celebration, the same three jumps of joy with his medal and confetti as on the end-of-session screen. */
export const HomeChampionTap: Story = {
  name: "Champion, tap Poulpy",
  tags: ['!autodocs', 'draft'],
  ...screen(screenHomeChampionTap),
  parameters: { mockup: { issue: 222 } },
}
