import type { Meta, StoryObj } from '@storybook/vue3-vite'

const meta = {
  title: 'Mobile/Patterns/Option sheet',
  parameters: { docs: { canvas: { sourceState: 'none' }, description: { component: 'The option sheet, drawn on the shared tokens with the isolated mobile stylesheet. Its shape and notes are in the design system tables. Light and dark follow the theme toolbar.' } } },
} satisfies Meta
export default meta
type Story = StoryObj

const sample = (template: string, components = {}): Story => ({
  render: () => ({ components, template: `<div class="cp-mobile">${template}</div>` }),
})

export const Default: Story = sample(`<div class="sheet"><strong class="sheet-title">Pronouns</strong>
  <div class="option"><span class="radio on" aria-hidden="true"></span>They / them</div>
  <div class="option"><span class="radio" aria-hidden="true"></span>She / her</div>
  <div class="option"><span class="radio" aria-hidden="true"></span>Custom</div>
  <div class="box"><input value="" placeholder="Custom pronouns" aria-label="Custom pronouns" /></div>
  <button type="button" class="btn primary sm">Save</button></div>`)
