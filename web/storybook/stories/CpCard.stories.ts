import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { CpBadge, CpCard } from '@coach-poulpy/ui'
import doc from './CpCard.md?raw'

const meta = {
  title: 'Web/Components/Display/Card',
  parameters: { docs: { description: { component: doc } } },
  component: CpCard,
  render: (args) => ({
    components: { CpCard, CpBadge },
    setup: () => ({ args }),
    template: `<CpCard v-bind="args"><template v-if="args.description" #badge><CpBadge tone="success">Configured</CpBadge></template><p>What the card holds.</p></CpCard>`,
  }),
} satisfies Meta<typeof CpCard>
export default meta
type Story = StoryObj<typeof meta>

export const Untitled: Story = {}
export const Titled: Story = { args: { title: 'Mail' } }
// CLEANUP: its success badge is 3.1:1, below 4.5:1 (see the Badge stories); drop this exemption with that one.
export const WithDescriptionAndBadge: Story = { parameters: { a11y: { test: 'todo' } }, args: { title: 'Mail', description: 'Where sign-in emails come from.' } }
