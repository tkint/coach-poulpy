import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { expect, userEvent, within } from 'storybook/test'
import { CpAvatar } from '@coach-poulpy/ui'
import doc from './CpAvatar.md?raw'

const meta = { title: 'Web/Components/Display/Avatar', component: CpAvatar, args: { initials: 'AM' }, parameters: { docs: { description: { component: doc } } } } satisfies Meta<typeof CpAvatar>
export default meta
type Story = StoryObj<typeof meta>

export const Small: Story = { args: { size: 'sm' } }
export const Medium: Story = { args: { size: 'md' } }
export const Large: Story = { args: { size: 'lg' } }
export const Primary: Story = { args: { tone: 'primary', size: 'md' } }
