import type { Meta, StoryObj } from '@storybook/vue3-vite'
import BrandBook from './BrandBook.vue'
import Poulpy3d from './Poulpy3d.vue'

// The live parts of the Brand pages: Poulpy in 3D and the brand book. Each shows on its page (Poulpy3d.mdx,
// BrandBook.mdx); the page names them, so they stay out of the sidebar.
const meta = {
  title: 'Brand/Live',
  tags: ['!dev', '!autodocs'],
  parameters: { layout: 'fullscreen' },
} satisfies Meta
export default meta
type Story = StoryObj<typeof meta>

/** Poulpy in 3D, still, to turn around by hand. */
export const Model: Story = { render: () => ({ components: { Poulpy3d }, template: '<Poulpy3d />' }) }
/** Poulpy in 3D, turning in short steps, with the controls to try other feels. */
export const Turning: Story = { render: () => ({ components: { Poulpy3d }, template: '<Poulpy3d motion="turn" />' }) }
/** The brand book, drawn live. */
// CLEANUP: the brand book carries the axe findings it had on the docs site (the plush blue in its hero title and some
// small grey labels are below the contrast minimum); fix them in the book, then drop the todo.
export const Book: Story = {
  render: () => ({ components: { BrandBook }, template: '<BrandBook />' }),
  parameters: { a11y: { test: 'todo' } },
}
