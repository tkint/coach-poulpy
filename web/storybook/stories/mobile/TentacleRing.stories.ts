import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { renderComponent } from './components/story'

const meta = {
  title: 'Mobile/Components/Tentacle ring',
  render: renderComponent('MobileTentacleRing', undefined, { content: '' }),
  args: { lit: 6, label: '6 of 8 lit · 1 full octopus', fresh: false, size: 104, picture: '../../../poulpy/mood_happy.svg', pictureAlt: 'Poulpy, happy' },
  argTypes: { lit: { control: { type: 'range', min: 0, max: 8 } }, size: { control: 'inline-radio', options: [104, 64] }, picture: { control: 'text' } },
  parameters: { docs: { canvas: { sourceState: 'none' }, description: { component: '`MobileTentacleRing`: Poulpy\'s eight tentacles as a ring, `lit` of them in primary. `fresh` lights the last one brighter, for the tentacle the session just lit. A `picture` of Poulpy sits inside the large ring (104, Home); the small one (64) closes a session. `label` says the count in words.' } } },
} satisfies Meta
export default meta
type Story = StoryObj<typeof meta>

export const Home: Story = {}
export const Resting: Story = { args: { picture: '../../../poulpy/mood_recovering.svg', pictureAlt: 'Poulpy, recovering' } }
export const JustLit: Story = { args: { lit: 7, fresh: true, size: 64, picture: undefined, pictureAlt: undefined, label: '7 of 8 tentacles lit' } }
export const NoneLit: Story = { args: { lit: 0, label: '0 of 8 lit' } }
