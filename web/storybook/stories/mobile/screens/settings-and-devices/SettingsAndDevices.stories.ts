import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { screen } from '../screen'
import screenDataSources from './data-sources.html?raw'
import screenDeviceAdd from './device-add.html?raw'
import screenDeviceDetail from './device-detail.html?raw'
import screenDevicesEmpty from './devices-empty.html?raw'

// The Settings and devices screens of the apps (ADR 0029), drawn with the phone stylesheet (../screens.css).
// CLEANUP: the screens marked a11y todo carry axe findings the old mockups had (contrast of hints and past days, names of fields); fix them with the screens, then drop the todo.
// CLEANUP: the screens marked a11y todo carry axe findings the old mockups had (contrast of hints and past days, names of fields); fix them with the screens, then drop the todo.
const meta = {
  title: "Mobile/Screens/Settings and devices",
  tags: ['!autodocs', 'agreed'],
  parameters: { layout: 'fullscreen', mockup: {}, docs: { canvas: { sourceState: 'none' } } },
} satisfies Meta
export default meta
type Story = StoryObj<typeof meta>

/** Settings · data sources. Was design/mockups/agreed/phone/data-sources.html. */
export const DataSources: Story = {
  name: "Data sources",
  ...screen(screenDataSources),
  parameters: { mockup: { issue: 104 } },
}
/** Settings · add a device. Was design/mockups/agreed/phone/device-add.html. */
export const DeviceAdd: Story = {
  name: "Add a device",
  ...screen(screenDeviceAdd),
  parameters: { a11y: { test: 'todo' }, mockup: { issue: 104 } },
}
/** Settings · device page. Was design/mockups/agreed/phone/device-detail.html. */
export const DeviceDetail: Story = {
  name: "Device page",
  ...screen(screenDeviceDetail),
  parameters: { a11y: { test: 'todo' }, mockup: { issue: 104 } },
}
/** Settings · no devices yet. Was design/mockups/agreed/phone/devices-empty.html. */
export const DevicesEmpty: Story = {
  name: "No devices yet",
  ...screen(screenDevicesEmpty),
  parameters: { mockup: { issue: 104 } },
}
