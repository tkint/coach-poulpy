import type { Meta, StoryObj } from '@storybook/vue3-vite'

const meta = {
  title: 'Mobile/Components/Button',
  parameters: { docs: { canvas: { sourceState: 'none' }, description: { component: 'The four button shapes. One primary button per screen; the grumpy button is only the catch-up action.' } } },
} satisfies Meta
export default meta
type Story = StoryObj

const sample = (template: string, components = {}): Story => ({
  render: () => ({ components, template: `<div class="cp-mobile">${template}</div>` }),
})

export const Default: Story = sample(`<div class="row">
  <button type="button" class="btn primary">Start session</button>
  <button type="button" class="btn tonal">Edit</button>
  <button type="button" class="btn outline">Cancel</button>
  <button type="button" class="btn grumpy">Catch up</button></div>`)
