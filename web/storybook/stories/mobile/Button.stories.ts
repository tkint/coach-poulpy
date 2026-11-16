import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { h } from 'vue'
import { Screen } from './screens/screen'
import { renderComponent } from './components/story'

const sizes = ['xs', 'sm', 'md', 'lg', 'xl', 'xxl'] as const

const meta = {
  title: 'Mobile/Components/Button',
  tags: ['draft'],
  render: renderComponent('MobileButton'),
  args: { label: 'Start now', kind: 'primary', size: 'md', icon: '' },
  argTypes: {
    kind: { control: 'inline-radio', options: ['primary', 'secondary', 'tonal', 'grumpy'] },
    size: { control: 'select', options: [...sizes], description: 'One of the websites\' size scale: xs, sm, md, lg, xl, xxl.', table: { defaultValue: { summary: 'md' } } },
    icon: { control: 'text', description: 'A Lucide icon name, such as `play`.' },
    iconSize: { control: { type: 'number', min: 12, max: 24 } },
  },
  parameters: { docs: { canvas: { sourceState: 'none' }, description: { component: '`MobileButton` (draft). A rounded square (10 radius, like the New workout button and the settings rows), never a pill, on the websites\' size scale: xs 24, sm 32, md 40, lg 48, xl 56, xxl 64. Every example is md; the Sizes story shows the rest, one per line. Below 44 the touch area still reaches 44. One primary button per screen; secondary (outlined) and tonal for the others; grumpy only for the catch-up action. Always a plain `type="button"`. `style`, `id` and `disabled` go through to the element.' } } },
} satisfies Meta
export default meta
type Story = StoryObj<typeof meta>

export const Primary: Story = {}
export const Secondary: Story = { args: { kind: 'secondary', label: 'Edit' } }
export const Tonal: Story = { args: { kind: 'tonal', label: 'Skip the rest' } }
export const Grumpy: Story = { args: { kind: 'grumpy', label: 'Catch up' } }
export const WithIcon: Story = { args: { icon: 'play' } }
export const Disabled: Story = { args: { kind: 'secondary', label: 'Skip', disabled: true } }

/** Every step of the size scale, xs to xxl, one per line; md is what a button without a `size` draws. */
export const Sizes: Story = {
  render: () => {
    const rows = sizes
      .map((s) => `<div style="display: flex; align-items: center; gap: 16px;"><span style="width: 32px; font-size: 13px; color: var(--cp-on-surface-variant);">${s}</span><MobileButton size="${s}">Start now</MobileButton></div>`)
      .join('')
    return h(Screen, { markup: `<div style="width: 390px; padding: 16px; display: flex; flex-direction: column; align-items: flex-start; gap: 12px;">${rows}</div>` })
  },
}
