import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { renderComponent } from './components/story'

const meta = {
  title: 'Mobile/Components/Filter chip',
  render: renderComponent('MobileFilterChip'),
  args: { label: 'Sets', ariaPressed: 'false', have: false, small: false },
  argTypes: {
    ariaPressed: { control: 'inline-radio', options: ['false', 'true'] },
    icon: { control: 'text', description: 'A Lucide icon name, for a chip that opens a picker.' },
    count: { control: { type: 'number', min: 0 } },
  },
  parameters: { docs: { canvas: { sourceState: 'none' }, description: { component: '`MobileFilterChip`: a word that filters a list (`aria-pressed`), or an icon that opens a picker (`aria-haspopup`, `aria-label`). `have` and `count` when values are ticked; `small` in a row of presets.' } } },
} satisfies Meta
export default meta
type Story = StoryObj<typeof meta>

export const Word: Story = {}
export const Pressed: Story = { args: { label: 'All', ariaPressed: 'true' } }
export const Picker: Story = { args: { label: '', ariaPressed: undefined, icon: 'dumbbell', ariaHaspopup: 'dialog', ariaLabel: 'Equipment', style: 'padding: 0 var(--cp-space-md); min-width: 48px;' } }
export const Ticked: Story = { args: { label: '', ariaPressed: undefined, icon: 'dumbbell', have: true, count: 2, ariaHaspopup: 'dialog', ariaLabel: 'Equipment, 2 ticked', style: 'padding: 0 var(--cp-space-md); min-width: 48px;' } }
export const Small: Story = { args: { label: '5 km', small: true } }
