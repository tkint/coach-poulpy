import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { CpTable } from '@coach-poulpy/ui'
import doc from './CpTable.md?raw'

const meta = {
  title: 'Display/Table',
  parameters: { docs: { description: { component: doc } } },
  component: CpTable,
  render: () => ({
    components: { CpTable },
    template: `<CpTable><thead><tr><th>Exercise</th><th>Sets</th></tr></thead>
      <tbody><tr><td>Squat</td><td>3</td></tr><tr><td>Lunge</td><td>2</td></tr></tbody></CpTable>`,
  }),
} satisfies Meta<typeof CpTable>
export default meta
export const Default: StoryObj<typeof meta> = {}
