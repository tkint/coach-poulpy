import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { ChevronRight } from '@lucide/vue'

const meta = {
  title: 'Mobile/Components/Select field',
  parameters: { docs: { canvas: { sourceState: 'none' }, description: { component: 'The select field, drawn on the shared tokens with the isolated mobile stylesheet. Its shape and notes are in the design system tables. Light and dark follow the theme toolbar.' } } },
} satisfies Meta
export default meta
type Story = StoryObj

const sample = (template: string, components = {}): Story => ({
  render: () => ({ components, template: `<div class="cp-mobile">${template}</div>` }),
})

export const Default: Story = sample(`<div class="sample">
  <button type="button" class="box select"><span><span class="label">Experience</span><strong>Getting going</strong></span><ChevronRight :size="20" aria-hidden="true" /></button>
  <button type="button" class="box select"><span><span class="label">Sex</span><strong class="muted">Not set</strong></span><ChevronRight :size="20" aria-hidden="true" /></button></div>`, { ChevronRight })
