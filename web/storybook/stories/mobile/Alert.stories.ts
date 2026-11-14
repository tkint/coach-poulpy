import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { CircleExclamationPoint } from '@lucide/vue'

const meta = {
  title: 'Mobile/Components/Alert',
  parameters: { docs: { canvas: { sourceState: 'none' }, description: { component: 'The error banner: `error` tint, an icon and text, never ember.' } } },
} satisfies Meta
export default meta
type Story = StoryObj

const sample = (template: string, components = {}): Story => ({
  render: () => ({ components, template: `<div class="cp-mobile">${template}</div>` }),
})

export const Default: Story = sample(`<div class="alert" role="note"><CircleExclamationPoint :size="20" aria-hidden="true" />
  The treadmill stopped answering. Recording resumes when it's back.</div>`, { CircleExclamationPoint })
