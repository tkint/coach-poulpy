import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { screen } from '../screen'
import screenLiveDisconnected from './live-disconnected.html?raw'
import screenLiveFinish from './live-finish.html?raw'
import screenLiveMixedPlayerNoTreadmill from './live-mixed-player-no-treadmill.html?raw'
import screenLiveMixedPlayerPaused from './live-mixed-player-paused.html?raw'
import screenLiveMixedPlayerStrength from './live-mixed-player-strength.html?raw'
import screenLiveMixedPlayerWaitsForBelt from './live-mixed-player-waits-for-belt.html?raw'
import screenLivePicker from './live-picker.html?raw'
import screenLiveReady from './live-ready.html?raw'
import screenLiveSummary from './live-summary.html?raw'
import screenLiveWaiting from './live-waiting.html?raw'

// The Live screens of the apps (ADR 0029), drawn with the phone stylesheet (../screens.css).
// CLEANUP: the screens marked a11y todo carry axe findings the old mockups had (contrast of hints and past days, names of fields); fix them with the screens, then drop the todo.
const meta = {
  title: "Mobile/Screens/Home/Live",
  tags: ['!autodocs', 'agreed'],
  parameters: { layout: 'fullscreen', mockup: {}, docs: { canvas: { sourceState: 'none' } } },
} satisfies Meta
export default meta
type Story = StoryObj<typeof meta>

/** Live · treadmill lost, finish?. */
export const LiveDisconnected: Story = {
  name: "Treadmill lost, finish?",
  ...screen(screenLiveDisconnected),
  parameters: { mockup: { issue: 104 } },
}
/** Live · belt still, finish?. */
export const LiveFinish: Story = {
  name: "Belt still, finish?",
  ...screen(screenLiveFinish),
  parameters: { mockup: { issue: 104 } },
}
/** Player · walk step without a treadmill. */
export const LiveMixedPlayerNoTreadmill: Story = {
  name: "Walk step without a treadmill",
  ...screen(screenLiveMixedPlayerNoTreadmill),
  parameters: { mockup: { issue: 82 } },
}
/** Player · mixed workout, paused on a strength step. */
export const LiveMixedPlayerPaused: Story = {
  name: "Mixed workout, paused on a strength step",
  ...screen(screenLiveMixedPlayerPaused),
  parameters: { a11y: { test: 'todo' }, mockup: { issue: 220 } },
}
/** Player · mixed workout, strength step with treadmill numbers. */
export const LiveMixedPlayerStrength: Story = {
  name: "Mixed workout, strength step with treadmill numbers",
  ...screen(screenLiveMixedPlayerStrength),
  parameters: { mockup: { issue: 220 } },
}
/** Player · walk step waits for the belt. */
export const LiveMixedPlayerWaitsForBelt: Story = {
  name: "Walk step waits for the belt",
  ...screen(screenLiveMixedPlayerWaitsForBelt),
  parameters: { mockup: { issue: 82 } },
}
/** Live · what are you doing?. */
export const LivePicker: Story = {
  name: "What are you doing?",
  ...screen(screenLivePicker),
  parameters: { mockup: { issue: 104 } },
}
/** Live · ready. */
export const LiveReady: Story = {
  name: "Ready",
  ...screen(screenLiveReady),
  parameters: { mockup: { issue: 104 } },
}
/** Live · summary. */
export const LiveSummary: Story = {
  name: "Summary",
  ...screen(screenLiveSummary),
  parameters: { mockup: { issue: 104 } },
}
/** Live · waiting for the treadmill. */
export const LiveWaiting: Story = {
  name: "Waiting for the treadmill",
  ...screen(screenLiveWaiting),
  parameters: { mockup: { issue: 104 } },
}
