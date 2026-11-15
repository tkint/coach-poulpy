import type { Meta, StoryObj } from '@storybook/vue3-vite'
import BrandBook from './BrandBook.vue'
import Poulpy3d from './Poulpy3d.vue'

// The live parts of the Brand pages: Poulpy in 3D and the brand book. Each shows on its page (Poulpy3d.mdx and the
// pages of his moves in poulpy-moves/, one move a page so no page runs two at once, and BrandBook.mdx); the page names them, so they stay out of the sidebar.
const meta = {
  title: 'Brand/Live',
  tags: ['!dev', '!autodocs'],
  parameters: { layout: 'fullscreen' },
} satisfies Meta
export default meta
type Story = StoryObj<typeof meta>

/** Poulpy in 3D, still, to turn around by hand. */
export const Model: Story = { render: () => ({ components: { Poulpy3d }, template: '<Poulpy3d />' }) }
/** Poulpy in 3D in each of his six moods. */
export const Moods: Story = { render: () => ({ components: { Poulpy3d }, template: '<Poulpy3d moods />' }) }
/** Poulpy in 3D in each of his twelve poses. */
export const Poses: Story = { render: () => ({ components: { Poulpy3d }, template: '<Poulpy3d poses />' }) }
/** Poulpy in 3D, turning in short steps, with the controls to try other feels. */
export const Turning: Story = { render: () => ({ components: { Poulpy3d }, template: '<Poulpy3d motion="turn" />' }) }
/** Poulpy in 3D, doing little jumps on the spot, with the controls to tune the jump. */
export const Jumping: Story = { render: () => ({ components: { Poulpy3d }, template: '<Poulpy3d motion="jump" />' }) }
/** The same jumps, each turning him a step. */
export const JumpingAround: Story = { render: () => ({ components: { Poulpy3d }, template: '<Poulpy3d motion="jump-turn" />' }) }
/** The champion jumping for joy: his medal bounces on its ribbon and confetti flies at each jump. */
export const Cheering: Story = { render: () => ({ components: { Poulpy3d }, template: '<Poulpy3d motion="cheer" />' }) }
/** The brand book, drawn live. */
export const Book: Story = {
  render: () => ({ components: { BrandBook }, template: '<BrandBook />' }),
  // The light wordmark is the logo in its plush blue; WCAG 1.4.3 exempts logotypes from the contrast minimum.
  parameters: { a11y: { context: { exclude: ['.wm.l .name .p'] } } },
}
