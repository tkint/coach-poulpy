import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { screen } from './screens/screen'

const meta = {
  title: 'Mobile/Patterns/Sheet',
  parameters: { layout: 'fullscreen', docs: { canvas: { sourceState: 'none' }, description: { component: '`MobileSheet` with `MobileSheetHandle`: a dialog that rises over the screen and holds what it is given. The handle\'s `aria-label` says where a drag takes it; the sheet\'s names it.' } } },
} satisfies Meta
export default meta
type Story = StoryObj

const frame = (inner: string) => `<main class="phone" style="--frame-height: 420px; background: var(--cp-surface-variant);">${inner}</main>`

export const WithHandle: Story = screen(frame(`<MobileSheet aria-label="Log your weight" style="position: absolute; inset: auto 0 0 0;"><MobileSheetHandle aria-label="Drag down to close" /><div style="padding: 8px 20px 24px; display: flex; flex-direction: column; gap: 12px;"><strong style="font-size: 20px;">Log your weight</strong><MobileButton large class="button--block">Save</MobileButton></div></MobileSheet>`))
