import type { Meta, StoryObj } from '@storybook/vue3-vite'

const meta = {
  title: 'Mobile/Components/Text field',
  parameters: { docs: { description: { component: 'The text field, drawn on the shared tokens with the isolated mobile stylesheet. Its shape and notes are in the design system tables. Light and dark follow the theme toolbar.' } } },
} satisfies Meta
export default meta
type Story = StoryObj

const sample = (template: string, components = {}): Story => ({
  render: () => ({ components, template: `<div class="cp-mobile">${template}</div>` }),
})

export const Default: Story = sample(`<div class="sample"><label class="label" for="name">Name</label>
  <div class="box"><input id="name" value="Alex" /></div>
  <span class="note">Shown to you only, on this phone.</span></div>`)
