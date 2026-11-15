import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { screen } from '../screen'
import screenPlayerCircuitRound from './player-circuit-round.html?raw'
import screenPlayerFinishedRun from './player-finished-run.html?raw'
import screenPlayerFinished from './player-finished.html?raw'
import screenPlayerPaused from './player-paused.html?raw'
import screenPlayerReady from './player-ready.html?raw'
import screenPlayerRecover from './player-recover.html?raw'
import screenPlayerRepeatInSets from './player-repeat-in-sets.html?raw'
import screenPlayerRest from './player-rest.html?raw'
import screenPlayerSet from './player-set.html?raw'
import screenPlayerWarmUp from './player-warm-up.html?raw'

// The Guided player screens of the apps (ADR 0029), drawn with the phone stylesheet (../screens.css).
// CLEANUP: the screens marked a11y todo carry axe findings the old mockups had (contrast of hints and past days, names of fields); fix them with the screens, then drop the todo.
const meta = {
  title: "Mobile/Screens/Guided player",
  tags: ['!autodocs', 'agreed'],
  parameters: { layout: 'fullscreen', mockup: {}, docs: { canvas: { sourceState: 'none' } } },
} satisfies Meta
export default meta
type Story = StoryObj<typeof meta>

/** Player · circuit round, no sets. */
export const PlayerCircuitRound: Story = {
  name: "Circuit round, no sets",
  ...screen(screenPlayerCircuitRound),
  parameters: { mockup: { issue: 92 } },
}
/** Player · finished, a run. */
export const PlayerFinishedRun: Story = {
  name: "Finished, a run",
  ...screen(screenPlayerFinishedRun),
  parameters: { mockup: { issue: 104 } },
}
/** Player · finished. */
export const PlayerFinished: Story = {
  name: "Finished",
  ...screen(screenPlayerFinished),
  parameters: { mockup: { issue: 104 } },
}
/** Player · sets workout, paused on set 2 of 3. */
export const PlayerPaused: Story = {
  name: "Sets workout, paused on set 2 of 3",
  ...screen(screenPlayerPaused),
  parameters: { a11y: { test: 'todo' }, mockup: { issue: 220 } },
}
/** Player · ready. */
export const PlayerReady: Story = {
  name: "Ready",
  ...screen(screenPlayerReady),
  parameters: { mockup: { issue: 104 } },
}
/** Player · recover in a round. */
export const PlayerRecover: Story = {
  name: "Recover in a round",
  ...screen(screenPlayerRecover),
  parameters: { mockup: { issue: 92 } },
}
/** Player · repeat in a sets workout. */
export const PlayerRepeatInSets: Story = {
  name: "Repeat in a sets workout",
  ...screen(screenPlayerRepeatInSets),
  parameters: { mockup: { issue: 92 } },
}
/** Player · rest between sets. */
export const PlayerRest: Story = {
  name: "Rest between sets",
  ...screen(screenPlayerRest),
  parameters: { mockup: { issue: 104 } },
}
/** Player · sets workout, set 2 of 3. */
export const PlayerSet: Story = {
  name: "Sets workout, set 2 of 3",
  ...screen(screenPlayerSet),
  parameters: { mockup: { issue: 220 } },
}
/** Player · in the warm-up. */
export const PlayerWarmUp: Story = {
  name: "In the warm-up",
  ...screen(screenPlayerWarmUp),
  parameters: { mockup: { issue: 92 } },
}
