import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { h } from 'vue'
import { Screen } from './screens/screen'

const meta = {
  title: 'Mobile/Components/Tabs',
  args: { selected: 'Workouts' },
  argTypes: { selected: { control: 'inline-radio', options: ['Workouts', 'Exercises'] } },
  render: (args: { selected: string }) => h(Screen, { key: args.selected, markup: `<div style="width: 390px; padding: 16px;"><MobileTabs aria-label="Library" :tabs="['Workouts', 'Exercises']" selected="${args.selected}" /></div>` }),
  parameters: { docs: { canvas: { sourceState: 'none' }, description: { component: '`MobileTabs`: two or three tabs that switch a page\'s view, one selected. `aria-label` names the group.' } } },
} satisfies Meta
export default meta
type Story = StoryObj<typeof meta>

export const FirstSelected: Story = {}
export const SecondSelected: Story = { args: { selected: 'Exercises' } }
