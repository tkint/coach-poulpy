import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { h } from 'vue'
import { Screen } from './screens/screen'

const frame = (card: string) => h(Screen, { markup: `<div style="width: 390px; padding: 16px;">${card}</div>` })

const meta = {
  title: 'Mobile/Patterns/Empty state',
  tags: ['draft'],
  parameters: { docs: { canvas: { sourceState: 'none' }, description: { component: '`MobileEmptyState`: what a list or screen shows when nothing is there yet or nothing matches. It is deliberately quiet: a dashed outline and no fill, a small Poulpy thinking beside a `title` and a line, then at most one small secondary button (the `action` slot). Poulpy is never guilt, so the line says what to do next, not what is missing.' } } },
} satisfies Meta
export default meta
type Story = StoryObj

export const WithAction: Story = {
  render: () => frame(`<MobileEmptyState title="No workouts yet">
  Build one once, then plan it for any day or keep it here for later.
  <template #action><MobileButton kind="secondary" size="sm" icon="plus">Build your first workout</MobileButton></template>
</MobileEmptyState>`),
}
export const NoMatch: Story = {
  render: () => frame(`<MobileEmptyState title="Nothing for “zumba”">
  Try another word, or look in the exercises to build one.
  <template #action><MobileButton kind="secondary" size="sm">Clear search and filters</MobileButton></template>
</MobileEmptyState>`),
}
export const TextOnly: Story = {
  render: () => frame(`<MobileEmptyState title="No sessions yet">
  Finish a workout or a treadmill session and it will show up here.
</MobileEmptyState>`),
}
