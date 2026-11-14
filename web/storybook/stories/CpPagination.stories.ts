import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { expect, userEvent, within } from 'storybook/test'
import { ref } from 'vue'
import { CpPagination } from '@coach-poulpy/ui'
import doc from './CpPagination.md?raw'

const meta = {
  title: 'Web/Components/Navigation/Pagination',
  parameters: { docs: { description: { component: doc } } },
  component: CpPagination,
  args: { pages: 12, previousLabel: 'Previous page', nextLabel: 'Next page', navLabel: 'Pages' },
  render: (args) => ({
    components: { CpPagination },
    setup: () => ({ args, page: ref(args.page ?? 1) }),
    template: '<CpPagination v-bind="args" v-model:page="page" />',
  }),
} satisfies Meta<typeof CpPagination>
export default meta
type Story = StoryObj<typeof meta>

export const FirstPage: Story = {
  play: async ({ canvasElement }) => {
    expect(within(canvasElement).getByRole('button', { name: 'Previous page' })).toBeDisabled()
  },
}
export const MiddlePage: Story = { args: { page: 6 } }
export const LastPage: Story = {
  args: { page: 12 },
  play: async ({ canvasElement }) => {
    expect(within(canvasElement).getByRole('button', { name: 'Next page' })).toBeDisabled()
  },
}
export const OnlyAFewPages: Story = { args: { pages: 3 } }
