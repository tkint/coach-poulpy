import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { renderComponent } from './components/story'

const meta = {
  title: 'Mobile/Components/Button',
  render: renderComponent('MobileButton'),
  args: { label: 'Start now', kind: 'primary', large: false, icon: '', tag: 'a' },
  argTypes: {
    kind: { control: 'inline-radio', options: ['primary', 'secondary', 'tonal', 'small'] },
    tag: { control: 'inline-radio', options: ['a', 'button'] },
    icon: { control: 'text', description: 'A Lucide icon name, such as `play`.' },
    iconSize: { control: { type: 'number', min: 16, max: 24 } },
  },
  parameters: { docs: { canvas: { sourceState: 'none' }, description: { component: '`MobileButton`. One primary button per screen; secondary and tonal for the others; the small one sits in a top bar. A link by default, `tag="button"` for an action. `style`, `id` and `disabled` go through to the element.' } } },
} satisfies Meta
export default meta
type Story = StoryObj<typeof meta>

export const Primary: Story = {}
export const Secondary: Story = { args: { kind: 'secondary', label: 'Edit' } }
export const Tonal: Story = { args: { kind: 'tonal', label: 'Skip the rest' } }
export const Small: Story = { args: { kind: 'small', label: 'Save' } }
export const WithIcon: Story = { args: { icon: 'play' } }
export const Large: Story = { args: { large: true, icon: 'check', label: 'Done' } }
export const Disabled: Story = { args: { kind: 'secondary', large: true, tag: 'button', label: 'Skip', disabled: true } }

// CLEANUP: the design-system sheet's button shapes are drawn by mobile.css, apart from the screens' styles; merge them
// into MobileButton (the grumpy and outline shapes) and drop this story.
/** The four shapes of the design-system sheet, as mobile.css draws them. */
export const Shapes: Story = {
  render: () => ({
    template: `<div class="cp-mobile"><div class="row">
  <button type="button" class="btn primary">Start session</button>
  <button type="button" class="btn tonal">Edit</button>
  <button type="button" class="btn outline">Cancel</button>
  <button type="button" class="btn grumpy">Catch up</button></div></div>`,
  }),
}
