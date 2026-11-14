import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { CpNavItem, CpNavMenu } from '@coach-poulpy/ui'

const meta = {
  title: 'Navigation/NavMenu',
  component: CpNavMenu,
  render: () => ({
    components: { CpNavMenu, CpNavItem },
    template: '<div class="w-64"><CpNavMenu><CpNavItem active>Exercises</CpNavItem><CpNavItem>Workouts</CpNavItem></CpNavMenu></div>',
  }),
} satisfies Meta<typeof CpNavMenu>
export default meta
export const Default: StoryObj<typeof meta> = {}
