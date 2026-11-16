import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { screen } from '../screen'
import screenHomeChampionTap from './home-champion-tap.html?raw'
import screenHomeDone from './home-done.html?raw'
import screenHomeInProgress from './home-in-progress.html?raw'
import screenHomeNext from './home-next.html?raw'
import screenHomeNothingPlanned from './home-nothing-planned.html?raw'
import screenHomePlanned from './home-planned.html?raw'
import screenHomePlannedTap from './home-planned-tap.html?raw'
import screenHomeMoodTap from './home-mood-tap.html?raw'

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
/** Home · planned, with Poulpy losing patience when tapped again and again (draft). Five taps in a row make the happy one turn round and come out annoyed; three more make him turn round and come out fuming; each tap answers with a small move for his mood (a jump, a shake, a shudder). Left alone for 5 seconds he improves his mood by a step, each time by a full turn the other way round. The moves are the ones validated on Brand/Poulpy moves. */
export const HomeMoodTap: Story = {
  name: "Planned, tap Poulpy until he is angry",
  tags: ['!autodocs', 'draft'],
  ...screen(screenHomeMoodTap),
}
/** Home · done, the eighth tentacle lit: Poulpy is the champion. He stays a still picture; a tap plays his celebration, the same three jumps of joy with his medal and confetti as on the end-of-session screen. */
export const HomeChampionTap: Story = {
  name: "Champion, tap Poulpy",
  tags: ['!autodocs', 'draft'],
  ...screen(screenHomeChampionTap),
  parameters: { mockup: { issue: 222 } },
}
