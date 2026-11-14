import type { Meta, StoryObj } from '@storybook/vue3-vite'

const meta = {
  title: 'Mobile/Components/Chip',
  parameters: { docs: { description: { component: 'Filters, with `aria-pressed`. On is `primary`, off is a 1.5 px `outline`.' } } },
} satisfies Meta
export default meta
type Story = StoryObj

const sample = (template: string, components = {}): Story => ({
  render: () => ({ components, template: `<div class="cp-mobile">${template}</div>` }),
})

export const Default: Story = sample(`<div class="row">
  <button type="button" class="chip on" aria-pressed="true">Legs</button>
  <button type="button" class="chip" aria-pressed="false">Core</button>
  <button type="button" class="chip" aria-pressed="false">Dumbbell</button></div>`)
