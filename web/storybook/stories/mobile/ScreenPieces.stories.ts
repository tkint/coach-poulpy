import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { screen } from './screens/screen'

const meta = {
  title: 'Mobile/Components/Screen pieces',
  parameters: { docs: { canvas: { sourceState: 'none' }, description: { component: 'The Vue components the phone screens are built from: `MobileButton`, `MobileIconButton` and `MobileFilterChip`, next to `MobileNavBar`, `MobileSettingsRow` and `MobileStatTile`. Each draws the same markup the screens always had.' } } },
} satisfies Meta
export default meta
type Story = StoryObj

const row = (markup: string): Story => screen(`<div style="width: 390px; padding: 16px; display: flex; gap: 8px; align-items: center;">${markup}</div>`)

export const Buttons: Story = row(`<MobileButton icon="play">Start now</MobileButton><MobileButton kind="secondary" icon="pencil">Edit</MobileButton><MobileButton kind="small">Save</MobileButton>`)
export const LargeButtons: Story = row(`<MobileButton large icon="check">Done</MobileButton><MobileButton kind="secondary" large tag="button">Skip</MobileButton>`)
export const IconButtons: Story = row(`<MobileIconButton icon="share-2" filled aria-label="Share this session" /><MobileIconButton icon="x" aria-label="Close" />`)
export const FilterChips: Story = row(`<MobileFilterChip aria-pressed="true">All</MobileFilterChip><MobileFilterChip aria-pressed="false">Sets</MobileFilterChip><MobileFilterChip icon="dumbbell" have :count="2" aria-label="Equipment, 2 ticked" /><MobileFilterChip small aria-pressed="false">5 km</MobileFilterChip>`)
