import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { renderComponent } from './components/story'

const meta = {
  title: 'Mobile/Components/Poulpy loader',
  render: renderComponent('MobilePoulpyLoader', undefined, { content: '' }),
  args: { size: 48, label: 'Loading' },
  argTypes: { size: { control: { type: 'range', min: 16, max: 160, step: 8 } } },
  parameters: { docs: { canvas: { sourceState: 'none' }, description: { component: '`MobilePoulpyLoader`: the loading indicator, Poulpy\'s outline seen from above, swinging half a turn one way and then half a turn the other, forever, once every 3.6 s (12 s with reduced motion), each of his eight tentacles hanging loose behind the turn. It replaces spinners: 16 in a row, 48 in a list, 112 on a full page. `label` says what is loading, for a screen reader.' } } },
} satisfies Meta
export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {}
export const Page: Story = { args: { size: 112, label: 'Searching for devices' } }
export const InARow: Story = { args: { size: 16 } }
