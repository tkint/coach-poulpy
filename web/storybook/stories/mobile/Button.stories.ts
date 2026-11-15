import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { renderComponent } from './components/story'

const meta = {
  title: 'Mobile/Components/Button',
  render: renderComponent('MobileButton'),
  args: { label: 'Start now', kind: 'primary', large: false, icon: '' },
  argTypes: {
    kind: { control: 'inline-radio', options: ['primary', 'secondary', 'tonal', 'grumpy', 'small'] },
    icon: { control: 'text', description: 'A Lucide icon name, such as `play`.' },
    iconSize: { control: { type: 'number', min: 16, max: 24 } },
  },
  parameters: { docs: { canvas: { sourceState: 'none' }, description: { component: '`MobileButton`. One primary button per screen; secondary (outlined) and tonal for the others; grumpy only for the catch-up action; the small one sits in a top bar. Always a plain `type="button"`, so in a browser it neither follows a link nor submits a form. `style`, `id` and `disabled` go through to the element.' } } },
} satisfies Meta
export default meta
type Story = StoryObj<typeof meta>

export const Primary: Story = {}
export const Secondary: Story = { args: { kind: 'secondary', label: 'Edit' } }
export const Tonal: Story = { args: { kind: 'tonal', label: 'Skip the rest' } }
export const Grumpy: Story = { args: { kind: 'grumpy', label: 'Catch up' } }
export const Small: Story = { args: { kind: 'small', label: 'Save' } }
export const WithIcon: Story = { args: { icon: 'play' } }
export const Large: Story = { args: { large: true, icon: 'check', label: 'Done' } }
export const Disabled: Story = { args: { kind: 'secondary', large: true, label: 'Skip', disabled: true } }
