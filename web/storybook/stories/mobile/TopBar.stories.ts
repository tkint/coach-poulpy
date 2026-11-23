import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { h } from 'vue'
import { Screen } from './screens/screen'

const bar = (inner: string) => `<div style="width: 390px; background: var(--cp-background); border-bottom: 1px solid var(--cp-outline);">${inner}</div>`

const meta = {
  title: 'Mobile/Components/Top bar',
  tags: ['agreed'],
  parameters: { docs: { canvas: { sourceState: 'none' }, description: { component: '`MobileTopBar`: the one top bar of every phone screen. 44 under the status bar, then a 44 row; the title is a headline (21) on the left, the same on every screen. A pushed screen starts with a back chevron, a screen that ends a task with a close cross; a tab\'s root screen has neither. Actions go on the right, at most two. Filters, search and tabs sit below the bar, never in it.' } } },
} satisfies Meta
export default meta
type Story = StoryObj<typeof meta>

const story = (markup: string): Story => ({ render: () => h(Screen, { markup: bar(markup) }) })

/** Home: Record is the one action. */
export const Home: Story = story(`<MobileTopBar title="Home"><MobileButton size="sm" icon="circle-dot">Record</MobileButton></MobileTopBar>`)
/** Library: the tabs, search and filters are the page's, below the bar. */
export const Library: Story = story(`<MobileTopBar title="Library" />`)
/** Live: the device pill is the one action. */
export const Live: Story = story(`<MobileTopBar title="Live"><button type="button" class="device-pill"><span class="dot" aria-hidden="true"></span>3 connected</button></MobileTopBar>`)
/** You: the cog opens Settings. */
export const You: Story = story(`<MobileTopBar title="You"><MobileIconButton icon="settings" :icon-size="22" aria-label="Settings" /></MobileTopBar>`)
/** Settings, with its add button at the 40 size, as every bar action. */
export const Settings: Story = story(`<MobileTopBar title="Settings"><MobileIconButton icon="plus" :icon-size="22" filled aria-label="Add a device" /></MobileTopBar>`)
/** A screen reached from another: back, the title, an action. */
export const Pushed: Story = story(`<MobileTopBar title="Session summary" leading="back"><MobileIconButton icon="share" :icon-size="22" aria-label="Share" /></MobileTopBar>`)
/** A screen that ends a task: close. */
export const Task: Story = story(`<MobileTopBar title="Edit step" leading="close"><MobileButton kind="primary" size="sm">Save</MobileButton></MobileTopBar>`)
