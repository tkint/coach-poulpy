import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { CpBreadcrumbs } from '@coach-poulpy/ui'
import doc from './CpBreadcrumbs.md?raw'

const meta = {
  title: 'Web/Components/Navigation/Breadcrumbs',
  parameters: { docs: { description: { component: doc } } },
  component: CpBreadcrumbs,
  args: { label: 'You are here' },
  render: (args) => ({
    components: { CpBreadcrumbs },
    setup: () => ({ args }),
    template: '<CpBreadcrumbs v-bind="args"><li><a href="#">Catalog</a></li><li><a href="#">Exercises</a></li><li>Squat</li></CpBreadcrumbs>',
  }),
} satisfies Meta<typeof CpBreadcrumbs>
export default meta
export const Default: StoryObj<typeof meta> = {}
