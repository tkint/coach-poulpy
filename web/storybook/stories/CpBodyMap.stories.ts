import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { expect, userEvent, within } from 'storybook/test'
import { ref } from 'vue'
import { CpBodyMap } from '@coach-poulpy/ui'
import doc from './CpBodyMap.md?raw'

// A stand-in figure: the real ones are the generated body map shapes.
const figures = [
  { caption: 'Front', paths: [{ d: 'M60 40 a14 14 0 1 0 0.01 0z', outline: true }, { d: 'M36 60 h48 v70 h-48z', heat: 3 as const }, { d: 'M42 130 h14 v90 h-14z M64 130 h14 v90 h-14z', heat: 1 as const }] },
  { caption: 'Back', paths: [{ d: 'M60 40 a14 14 0 1 0 0.01 0z', outline: true }, { d: 'M36 60 h48 v70 h-48z', heat: 2 as const }, { d: 'M42 130 h14 v90 h-14z M64 130 h14 v90 h-14z' }] },
]
const meta = {
  title: 'Display/BodyMap',
  parameters: { docs: { description: { component: doc } } },
  component: CpBodyMap,
  args: { figures, description: 'Legs and chest worked most', less: 'Less', more: 'More' },
} satisfies Meta<typeof CpBodyMap>
export default meta
export const Default: StoryObj<typeof meta> = {
  play: async ({ canvasElement }) => {
    expect(within(canvasElement).getByRole('img')).toHaveAccessibleName('Legs and chest worked most')
  },
}

/** The picture is a field: an area with a key and a label is a button that says its key when it is pressed. */
export const Interactive: StoryObj<typeof meta> = {
  args: { interactive: true, less: undefined, more: undefined },
  render: (args) => ({
    components: { CpBodyMap },
    setup: () => {
      const lit = ref<string[]>(['chest'])
      const areas = [
        { key: 'chest', label: 'Chest', d: 'M36 60 h48 v40 h-48z' },
        { key: 'legs', label: 'Legs', d: 'M42 130 h14 v90 h-14z M64 130 h14 v90 h-14z' },
      ]
      const figures = () => [
        { caption: 'Front', paths: [{ d: 'M60 40 a14 14 0 1 0 0.01 0z', outline: true }, ...areas.map((area) => ({ ...area, heat: lit.value.includes(area.key) ? (3 as const) : undefined }))] },
      ]
      const pick = (key: string) => (lit.value = lit.value.includes(key) ? lit.value.filter((one) => one !== key) : [...lit.value, key])
      return { args, figures, pick }
    },
    template: '<CpBodyMap v-bind="args" :figures="figures()" @pick="pick" />',
  }),
  play: async ({ canvasElement }) => {
    const legs = within(canvasElement).getByRole('button', { name: 'Legs' })
    expect(legs).toHaveAttribute('aria-pressed', 'false')
    await userEvent.click(legs)
    expect(legs).toHaveAttribute('aria-pressed', 'true')
  },
}
