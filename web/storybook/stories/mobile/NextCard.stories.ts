import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { renderComponent } from './components/story'

const meta = {
  title: 'Mobile/Components/Next card',
  render: renderComponent('MobileNextCard', 'width: 390px; padding: 16px; background: var(--cp-background);', { content: 'step' }),
  args: { label: 'Next', step: 'Rest 2 min, then set 3' },
  argTypes: { detail: { control: 'text' } },
  parameters: { docs: { canvas: { sourceState: 'none' }, description: { component: '`MobileNextCard`: what comes after the current step in the player. A `label` and the step (its content) on one line, or, with a `detail`, stacked as a headline over its numbers.' } } },
} satisfies Meta
export default meta
type Story = StoryObj<typeof meta>

export const OneLine: Story = {}
export const Stacked: Story = { args: { label: 'Next up · round 3 of 4', step: 'Goblet squat', detail: '15 reps at 16 kg' } }
