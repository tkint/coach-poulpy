import type { Meta, StoryObj } from '@storybook/vue3-vite'

const meta = {
  title: 'Mobile/Components/Stepper',
  parameters: { docs: { description: { component: 'Sets, reps, load and rest.' } } },
} satisfies Meta
export default meta
type Story = StoryObj

const sample = (template: string, components = {}): Story => ({
  render: () => ({ components, template: `<div class="cp-mobile">${template}</div>` }),
})

export const Default: Story = sample(`<div class="stepper"><span class="eyebrow">Rest</span><div class="stepper-row">
  <button type="button" aria-label="Less rest">−</button><span class="value">90 s</span>
  <button type="button" aria-label="More rest">+</button></div></div>`)
