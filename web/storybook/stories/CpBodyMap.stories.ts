import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { expect, userEvent, within } from 'storybook/test'
import { ref } from 'vue'
import { CpBodyMap, type BodyAreas } from '@coach-poulpy/ui'
import doc from './CpBodyMap.md?raw'

const meta = {
  title: 'Web/Components/Display/Body map',
  parameters: { docs: { description: { component: doc } } },
  component: CpBodyMap,
  args: {
    heat: { pectoral: 'both', biceps: { side: 'left', heat: 2 }, abs: { side: 'both', heat: 1 }, deltoid: 'both', triceps: { side: 'both', heat: 2 } },
    description: 'Chest and shoulders worked most',
    legend: 'top',
  },
} satisfies Meta<typeof CpBodyMap>
export default meta
type Story = StoryObj<typeof meta>

/** An area is lit by its name; a side string lights that side at the most, an object says how much. */
export const Default: Story = {
  play: async ({ canvasElement }) => {
    expect(within(canvasElement).getByRole('img')).toHaveAccessibleName('Chest and shoulders worked most')
    expect(canvasElement.querySelectorAll('.cp-body-part title').length).toBeGreaterThan(0)
  },
}

/** An injury takes the same object as the muscles worked and is drawn in the pain colour, its `heat` how much it hurts. */
export const Injury: Story = {
  args: { heat: undefined, injury: { deltoid: { side: 'right', heat: 3 }, biceps: 'right', 'lower-back': { side: 'both', heat: 1 } }, legend: 'hidden', description: 'Right shoulder hurts' },
}

/** The legend on the right, one side of the figure only. */
export const LegendRight: Story = { args: { legend: 'right', layout: 'front' } }

/** The other body figure. */
export const Hips: Story = { args: { figure: 'hips' } }

/** In a form, `v-model:heat` holds the areas: picking a side toggles it, and both sides make `both`. */
export const FormModel: Story = {
  args: { heat: undefined, legend: 'hidden', editing: 'heat' },
  render: (args) => ({
    components: { CpBodyMap },
    setup: () => ({ args, areas: ref<BodyAreas>({ biceps: 'left' }) }),
    template: '<CpBodyMap v-bind="args" v-model:heat="areas" /><p data-testid="areas">{{ JSON.stringify(areas) }}</p>',
  }),
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement)
    await userEvent.click(canvas.getAllByRole('button', { name: /biceps/i }).find((one) => one.getAttribute('aria-pressed') === 'false')!)
    expect(canvas.getByTestId('areas')).toHaveTextContent('{"biceps":"both"}')
  },
}

/** `interactive-mode="both"`: a tap on either side selects both sides of the area. */
export const BothSides: Story = {
  ...FormModel,
  args: { ...FormModel.args, interactiveMode: 'both' },
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement)
    await userEvent.click(canvas.getAllByRole('button', { name: /biceps/i })[0])
    expect(canvas.getByTestId('areas')).toHaveTextContent('{"biceps":"both"}')
    await userEvent.click(canvas.getAllByRole('button', { name: /biceps/i })[0])
    expect(canvas.getByTestId('areas')).toHaveTextContent('{}')
  },
}

/** `v-model:injury` with `editing="injury"`: a pick edits what hurts, and the muscles worked stay as they are. */
export const InjuryModel: Story = {
  args: { heat: undefined, legend: 'hidden', editing: 'injury', description: 'Where it hurts' },
  render: (args) => ({
    components: { CpBodyMap },
    setup: () => ({ args, hurt: ref<BodyAreas>({}) }),
    template: '<CpBodyMap v-bind="args" v-model:injury="hurt" /><p data-testid="hurt">{{ JSON.stringify(hurt) }}</p>',
  }),
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement)
    await userEvent.click(canvas.getAllByRole('button', { name: /shoulder/i })[0])
    expect(canvas.getByTestId('hurt')).toHaveTextContent(/"deltoid":"(left|right)"/)
  },
}

/** The picture is a field: every area is a button (`selectable` narrows which) that says its area and side when it is pressed. */
export const Interactive: Story = {
  args: { editing: 'heat', heat: undefined, legend: 'hidden' },
  render: (args) => ({
    components: { CpBodyMap },
    setup: () => {
      const lit = ref<BodyAreas>({ biceps: 'left' })
      const sides = (area: string) => (lit.value[area] === 'both' ? ['left', 'right'] : lit.value[area] ? [lit.value[area]] : [])
      const pick = (area: string, side: 'left' | 'right') => {
        const now = sides(area)
        const next = now.includes(side) ? now.filter((one) => one !== side) : [...now, side]
        const { [area]: _, ...rest } = lit.value
        lit.value = next.length === 2 ? { ...rest, [area]: 'both' } : next.length ? { ...rest, [area]: next[0] as 'left' | 'right' } : rest
      }
      return { args, lit, pick }
    },
    template: '<CpBodyMap v-bind="args" :heat="lit" @pick="pick" />',
  }),
  play: async ({ canvasElement }) => {
    const biceps = within(canvasElement).getAllByRole('button', { name: /biceps/i })
    const pressed = () => biceps.filter((one) => one.getAttribute('aria-pressed') === 'true').length
    expect(biceps).toHaveLength(2)
    expect(pressed()).toBe(1)
    await userEvent.click(biceps.find((one) => one.getAttribute('aria-pressed') === 'false')!)
    expect(pressed()).toBe(2)
  },
}
