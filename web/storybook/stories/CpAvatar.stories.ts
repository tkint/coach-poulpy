import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { expect, userEvent, within } from 'storybook/test'
import { CpAvatar } from '@coach-poulpy/ui'

const meta = { title: 'Display/Avatar', component: CpAvatar, args: { initials: 'AM' } } satisfies Meta<typeof CpAvatar>
export default meta
type Story = StoryObj<typeof meta>

export const Small: Story = { args: { size: 'sm' } }
export const Medium: Story = { args: { size: 'md' } }
export const Large: Story = { args: { size: 'lg' } }
export const Primary: Story = { args: { tone: 'primary', size: 'md' } }
