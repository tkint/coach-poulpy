import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { renderComponent } from './components/story'

const meta = {
  title: 'Mobile/Components/Icon',
  render: renderComponent('MobileIcon', undefined, { content: '' }),
  args: { name: 'dumbbell', size: 24 },
  argTypes: { name: { control: 'text', description: 'A Lucide icon name, as on lucide.dev.' }, size: { control: 'number' } },
  parameters: { docs: { canvas: { sourceState: 'none' }, description: { component: '`MobileIcon`: a Lucide icon by its Lucide name (ADR 0024), in the text colour. It is decorative: the words beside it, or its button\'s label, say what it means. An unknown name fails the story. The apps draw the same icon from `design/icons/`; add one they lack with `node design/build-icons.mjs --add <name>`.' } } },
} satisfies Meta
export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {}
export const Small: Story = { args: { name: 'chevron-right', size: 16 } }
export const Large: Story = { args: { name: 'heart-pulse', size: 32 } }
