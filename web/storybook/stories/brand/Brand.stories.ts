import type { Meta, StoryObj } from '@storybook/vue3-vite'
import BrandBook from './BrandBook.vue'
import PoulpyModel from './PoulpyModel.vue'
import PoulpyMoodMoves from './PoulpyMoodMoves.vue'
import PoulpyMove from './PoulpyMove.vue'
import PoulpyPictures from './PoulpyPictures.vue'

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
export const Model: Story = { render: () => ({ components: { PoulpyModel }, template: '<PoulpyModel />' }) }
/** Poulpy in 3D in each of his six moods. */
export const Moods: Story = { render: () => ({ components: { PoulpyModel }, template: '<PoulpyModel moods />' }) }
/** Poulpy in 3D in each of his twelve poses. */
export const Poses: Story = { render: () => ({ components: { PoulpyModel }, template: '<PoulpyModel poses />' }) }
/** The pictures painted from the 3D Poulpy: his six moods and twelve poses. */
export const Pictures: Story = { render: () => ({ components: { PoulpyPictures }, template: '<PoulpyPictures />' }) }
/** Poulpy in 3D, turning in short steps, with the controls to try other feels. */
export const Turning: Story = { render: () => ({ components: { PoulpyMove }, template: '<PoulpyMove motion="turn" />' }) }
/** Poulpy in 3D, doing little jumps on the spot, with the controls to tune the jump. */
export const Jumping: Story = { render: () => ({ components: { PoulpyMove }, template: '<PoulpyMove motion="jump" />' }) }
/** The same jumps, each turning him a step. */
export const JumpingAround: Story = { render: () => ({ components: { PoulpyMove }, template: '<PoulpyMove motion="jump-turn" />' }) }
/** The champion jumping for joy: his medal bounces on its ribbon and confetti flies at each jump. */
export const Cheering: Story = { render: () => ({ components: { PoulpyMove }, template: '<PoulpyMove motion="cheer" />' }) }
/** The cocky one: eyes wide open, then a wink and a smirk as his stars pop up and twinkle. */
export const Winking: Story = { render: () => ({ components: { PoulpyMove }, template: '<PoulpyMove motion="wink" />' }) }
/** Poulpy fuming: how he answers a tap, no transitions. */
export const Fuming: Story = { render: () => ({ components: { PoulpyMoodMoves }, template: `<PoulpyMoodMoves :only="['tuck']" />` }) }
/** Poulpy annoyed: how he answers a tap, no transitions. */
export const Annoyed: Story = { render: () => ({ components: { PoulpyMoodMoves }, template: `<PoulpyMoodMoves :only="['huff']" />` }) }
/** The whole sequence from his normal self to fuming and back, played in one go. */
export const Sequence: Story = { render: () => ({ components: { PoulpyMoodMoves }, template: `<PoulpyMoodMoves plain />` }) }
/** Normal to annoyed: the spin. */
export const ToAnnoyed: Story = { render: () => ({ components: { PoulpyMoodMoves }, template: `<PoulpyMoodMoves :only="['annoyed']" />` }) }
/** Annoyed to fuming: the spin. */
export const ToFuming: Story = { render: () => ({ components: { PoulpyMoodMoves }, template: `<PoulpyMoodMoves :only="['fuming']" />` }) }
/** Fuming back to annoyed: the spin the other way. */
export const CalmDown: Story = { render: () => ({ components: { PoulpyMoodMoves }, template: `<PoulpyMoodMoves :only="['calm']" />` }) }
/** Annoyed back to normal: the spin the other way. */
export const BackToNormal: Story = { render: () => ({ components: { PoulpyMoodMoves }, template: `<PoulpyMoodMoves :only="['normal']" />` }) }
/** The brand book, drawn live. */
export const Book: Story = {
  render: () => ({ components: { BrandBook }, template: '<BrandBook />' }),
  // The light wordmark is the logo in its plush blue; WCAG 1.4.3 exempts logotypes from the contrast minimum.
  parameters: { a11y: { context: { exclude: ['.wm.l .name .p'] } } },
}
