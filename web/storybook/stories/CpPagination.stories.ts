import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { expect, userEvent, within } from 'storybook/test'
import { ref } from 'vue'
import { CpPagination, cpSizes } from '@coach-poulpy/ui'
import { sizeArgType } from './sizes'
import doc from './CpPagination.md?raw'

const meta = {
  title: 'Web/Components/Navigation/Pagination',
  parameters: { docs: { description: { component: doc } } },
  component: CpPagination,
  argTypes: { size: sizeArgType },
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

// ADR 0031: with no labels passed, the component says its own words in the page's language.
export const SaysItsOwnWords: Story = {
  args: { previousLabel: undefined, nextLabel: undefined, navLabel: undefined },
  play: async ({ canvasElement }) => {
    expect(within(canvasElement).getByRole('button', { name: 'Previous page' })).toBeDisabled()
  },
}
export const SaysItsOwnWordsInFrench: Story = {
  args: { previousLabel: undefined, nextLabel: undefined, navLabel: undefined },
  globals: { locale: 'fr' },
  play: async ({ canvasElement }) => {
    expect(within(canvasElement).getByRole('button', { name: 'Page précédente' })).toBeDisabled()
  },
}

/** Every step of the size scale, xs to xxl; md is what a component without a `size` draws. */
export const Sizes: Story = {
  render: (args) => ({
    components: { CpPagination },
    setup: () => ({ args, cpSizes, value: ref(1) }),
    template: '<div class="flex flex-col items-start gap-4 pb-8"><div v-for="s in cpSizes" :key="s" class="flex items-center gap-4"><span class="cp-type-label w-10">{{ s }}</span><CpPagination v-bind="args" :size="s" :nav-label="`Pages ${s}`" v-model:page="value" /></div></div>',
  }),
}
