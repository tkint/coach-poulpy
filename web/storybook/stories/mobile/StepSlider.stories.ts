import type { Meta, StoryObj } from '@storybook/vue3-vite'

const meta = {
  title: 'Mobile/Components/Step slider',
  parameters: { docs: { canvas: { sourceState: 'none' }, description: { component: 'The step slider, drawn on the shared tokens with the isolated mobile stylesheet. Its shape and notes are in the design system tables. Light and dark follow the theme toolbar.' } } },
} satisfies Meta
export default meta
type Story = StoryObj

const sample = (template: string, components = {}): Story => ({
  render: () => ({ components, template: `<div class="cp-mobile">${template}</div>` }),
})

export const Default: Story = sample(`<div class="sample"><strong>Getting going</strong>
  <div class="slider" aria-hidden="true"><span class="dot"></span><span class="bar"></span><span class="dot"></span><span class="bar"></span><span class="dot"></span><span class="bar off"></span><span class="dot off"></span><span class="bar off"></span><span class="dot off"></span></div>
  <div class="ends"><span>Just starting</span><span>Very experienced</span></div></div>`)
