import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { expect, userEvent, within } from 'storybook/test'
import { ref } from 'vue'
import { CpBodyMap, type BodyAreas } from '@coach-poulpy/ui'
import doc from './CpBodyMap.md?raw'

const meta = {
  title: 'Core/Body map',
  parameters: { docs: { description: { component: doc } } },
  component: CpBodyMap,
  args: {
    front: { pectoral: 'both', biceps: { side: 'left', heat: 2 }, abs: { side: 'both', heat: 1 } },
    back: { deltoid: 'both', triceps: { side: 'both', heat: 2 } },
    captions: { front: 'Front', back: 'Back' },
    description: 'Chest and shoulders worked most',
    legend: 'top',
    less: 'Less',
    more: 'More',
  },
} satisfies Meta<typeof CpBodyMap>
export default meta
type Story = StoryObj<typeof meta>

/** An area is lit by its name; a side string lights that side at the most, an object says how much. */
export const Default: Story = {
  play: async ({ canvasElement }) => {
    expect(within(canvasElement).getByRole('img')).toHaveAccessibleName('Chest and shoulders worked most')
  },
}

/** An injury takes the same object as the muscles worked and is drawn in the pain colour, its `heat` how much it hurts. */
export const Injury: Story = {
  args: { front: undefined, back: undefined, frontInjury: { deltoid: { side: 'right', heat: 3 }, biceps: 'right' }, backInjury: { deltoid: { side: 'right', heat: 3 }, 'lower-back': { side: 'both', heat: 1 } }, legend: 'hidden', description: 'Right shoulder hurts' },
}

/** The legend on the right, one side of the figure only. */
export const LegendRight: Story = { args: { legend: 'right', layout: 'front' } }

/** The other body figure. */
export const Hips: Story = { args: { figure: 'hips' } }

/** In a form, `v-model` holds the areas: picking a side toggles it, and both sides make `both`. */
export const FormModel: Story = {
  args: { front: undefined, back: undefined, legend: 'hidden', interactive: true },
  render: (args) => ({
    components: { CpBodyMap },
    setup: () => ({ args, areas: ref<BodyAreas>({ biceps: 'left' }), areaLabel: (area: string) => (area === 'biceps' ? 'Biceps' : undefined) }),
    template: '<CpBodyMap v-bind="args" v-model="areas" :area-label="areaLabel" /><p data-testid="areas">{{ JSON.stringify(areas) }}</p>',
  }),
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement)
    await userEvent.click(canvas.getAllByRole('button', { name: 'Biceps' }).find((one) => one.getAttribute('aria-pressed') === 'false')!)
    expect(canvas.getByTestId('areas')).toHaveTextContent('{"biceps":"both"}')
  },
}

/** The picture is a field: an area `areaLabel` names is a button that says its area and side when it is pressed. */
export const Interactive: Story = {
  args: { interactive: true, front: undefined, back: undefined, legend: 'hidden' },
  render: (args) => ({
    components: { CpBodyMap },
    setup: () => {
      const lit = ref<BodyAreas>({ biceps: 'left' })
      const areaLabel = (area: string) => (area === 'biceps' ? 'Biceps' : area === 'quads' ? 'Quadriceps' : undefined)
      const sides = (area: string) => (lit.value[area] === 'both' ? ['left', 'right'] : lit.value[area] ? [lit.value[area]] : [])
      const pick = (area: string, side: 'left' | 'right') => {
        const now = sides(area)
        const next = now.includes(side) ? now.filter((one) => one !== side) : [...now, side]
        const { [area]: _, ...rest } = lit.value
        lit.value = next.length === 2 ? { ...rest, [area]: 'both' } : next.length ? { ...rest, [area]: next[0] as 'left' | 'right' } : rest
      }
      return { args, lit, areaLabel, pick }
    },
    template: '<CpBodyMap v-bind="args" :front="lit" :area-label="areaLabel" @pick="pick" />',
  }),
  play: async ({ canvasElement }) => {
    const biceps = within(canvasElement).getAllByRole('button', { name: 'Biceps' })
    const pressed = () => biceps.filter((one) => one.getAttribute('aria-pressed') === 'true').length
    expect(biceps).toHaveLength(2)
    expect(pressed()).toBe(1)
    await userEvent.click(biceps.find((one) => one.getAttribute('aria-pressed') === 'false')!)
    expect(pressed()).toBe(2)
  },
}
