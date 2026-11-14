import type { Meta, StoryObj } from '@storybook/vue3-vite'

const meta = {
  title: 'Mobile/Components/Number field',
  parameters: { docs: { canvas: { sourceState: 'none' }, description: { component: 'The number field, drawn on the shared tokens with the isolated mobile stylesheet. Its shape and notes are in the design system tables. Light and dark follow the theme toolbar.' } } },
} satisfies Meta
export default meta
type Story = StoryObj

const sample = (template: string, components = {}): Story => ({
  render: () => ({ components, template: `<div class="cp-mobile">${template}</div>` }),
})

export const Default: Story = sample(`<div class="sample">
  <div class="box"><input inputmode="decimal" value="64" aria-label="Resting heart rate" /><span class="unit">bpm</span></div>
  <div class="entry"><button type="button" aria-label="Less">−</button>
    <div class="box"><input inputmode="numeric" value="8" aria-label="Reps" /><span class="unit">reps</span></div>
    <button type="button" aria-label="More">+</button></div></div>`)
