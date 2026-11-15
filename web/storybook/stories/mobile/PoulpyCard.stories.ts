import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { h } from 'vue'
import { Screen } from './screens/screen'

const frame = (card: string) => h(Screen, { markup: `<div style="width: 390px; padding: 16px;">${card}</div>` })

const meta = {
  title: 'Mobile/Patterns/Poulpy card',
  parameters: { docs: { canvas: { sourceState: 'none' }, description: { component: '`MobilePoulpyCard`: Poulpy speaking from a primary container card. His art goes in the `art` slot (a `MobileTentacleRing` or a drawing), then a `title`, a line (the content) and an optional `meta` line. `compact` is the smaller card that closes a session.' } } },
} satisfies Meta
export default meta
type Story = StoryObj

export const Home: Story = {
  render: () => frame(`<MobilePoulpyCard title="Ready when you are" meta="6 of 8 lit · 1 full octopus">
  <template #art><MobileTentacleRing :lit="6" label="6 of 8 lit · 1 full octopus" picture="../../../poulpy/mood_happy.svg" picture-alt="Poulpy, happy" /></template>
  Today’s session lights tentacle 7.
</MobilePoulpyCard>`),
}
export const SessionDone: Story = {
  render: () => frame(`<MobilePoulpyCard title="Tentacle 7 lit!" compact>
  <template #art><MobileTentacleRing :lit="7" fresh :size="64" label="7 of 8 tentacles lit" /></template>
  One more and Poulpy is a champion.
</MobilePoulpyCard>`),
}
export const WithDrawing: Story = {
  render: () => frame(`<MobilePoulpyCard title="Taking it easy">
  <template #art><img src="../../../poulpy/pose_patched_up.svg" alt="" width="71" height="64"></template>
  Poulpy is patched up with you until 4 Nov. No catching up, just rest.
</MobilePoulpyCard>`),
}
