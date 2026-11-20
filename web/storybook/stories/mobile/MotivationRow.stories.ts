import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { h } from 'vue'
import { Screen } from './screens/screen'

const meta = {
  title: 'Mobile/Components/Motivation row',
  tags: ['draft'],
  parameters: { docs: { canvas: { sourceState: 'none' }, description: { component: '`MobileMotivationRow` (draft): Home\'s one motivation row. A small Poulpy, his eight tentacles as eight segments with some lit, the latest achievements and a chevron; a tap opens Achievements. A lit tentacle never goes dark and there is no streak, so the row never turns into a debt.' } } },
} satisfies Meta
export default meta
type Story = StoryObj<typeof meta>

const row = (lit: number, achievements: string) => ({
  render: () => h(Screen, { markup: `<div style="width: 358px; padding: 16px; background: var(--cp-background);"><MobileMotivationRow :lit="${lit}" label="${lit} of 8 tentacles lit" picture="../../../poulpy/mood_happy.svg" picture-alt="Poulpy, happy">${achievements}</MobileMotivationRow></div>` }),
})

export const WithAchievements: Story = row(5, '<MobileAchievement icon="award" name="First record" /><MobileAchievement icon="target" name="Goal met" /><MobileAchievement locked name="" />')
export const Beginning: Story = row(0, '<MobileAchievement locked name="" />')
export const AlmostFull: Story = row(7, '<MobileAchievement icon="award" name="First record" /><MobileAchievement locked name="" />')
