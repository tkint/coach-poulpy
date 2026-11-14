import type { Meta, StoryObj } from '@storybook/vue3-vite'

const meta = {
  title: 'Mobile/Components/Card',
  parameters: { docs: { description: { component: 'A row with a pattern badge: the exercise and workout lists are made of these.' } } },
} satisfies Meta
export default meta
type Story = StoryObj

const sample = (template: string, components = {}): Story => ({
  render: () => ({ components, template: `<div class="cp-mobile">${template}</div>` }),
})

export const Default: Story = sample(`<div class="card"><span class="badge" aria-hidden="true">SQ</span>
  <span class="card-text"><strong>Goblet squat</strong><span>4 × 8 at 16 kg</span></span></div>`)
