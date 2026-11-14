import type { Meta, StoryObj } from '@storybook/vue3-vite'

const meta = {
  title: 'Mobile/Components/Empty chart card',
  parameters: { docs: { canvas: { sourceState: 'none' }, description: { component: 'Shown instead of the chart when nothing is logged.' } } },
} satisfies Meta
export default meta
type Story = StoryObj

const sample = (template: string, components = {}): Story => ({
  render: () => ({ components, template: `<div class="cp-mobile">${template}</div>` }),
})

export const Default: Story = sample(`<div class="empty"><strong>No weight yet</strong>
  <span>Log it once to see your chart. It stays on this phone, and only you see it.</span>
  <button type="button" class="btn primary sm">Log your weight</button></div>`)
