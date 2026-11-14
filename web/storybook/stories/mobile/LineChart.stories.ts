import type { Meta, StoryObj } from '@storybook/vue3-vite'

const meta = {
  title: 'Mobile/Components/Line chart',
  parameters: { docs: { canvas: { sourceState: 'none' }, description: { component: 'The line chart, drawn on the shared tokens with the isolated mobile stylesheet. Its shape and notes are in the design system tables. Light and dark follow the theme toolbar.' } } },
} satisfies Meta
export default meta
type Story = StoryObj

export const Default: Story = {
  render: () => ({
    setup: () => ({ grid: [0, 25, 50, 75, 100], points: '8,62 60,56 112,58 164,44 216,40 268,30' }),
    template: `<div class="cp-mobile"><div class="sample">
      <svg viewBox="0 0 290 100" class="chart" role="img" aria-label="Weight from 72 kg to 70 kg over three months">
        <text x="0" y="8" class="tick">kg</text>
        <g v-for="line in grid" :key="line">
          <line :x1="28" :x2="290" :y1="12 + line * 0.64" :y2="12 + line * 0.64" class="grid" />
          <text x="0" :y="16 + line * 0.64" class="tick">{{ 74 - line * 0.06 }}</text>
        </g>
        <polyline :points="points" class="line" transform="translate(28 0)" />
      </svg>
      <div class="ends"><span>1 Aug</span><span>1 Nov</span></div></div></div>`,
  }),
}

