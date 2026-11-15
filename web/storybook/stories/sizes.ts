import { cpSizes } from '@coach-poulpy/ui'

// The size control of every web component that has the scale (web/storybook/stories/design-system/design-system.md, "Web sizes").
export const sizeArgType = {
  control: 'select',
  options: [...cpSizes],
  description: 'One of the websites\' size scale: xs, sm, md, lg, xl, xxl.',
  table: { defaultValue: { summary: 'md' } },
} as const

/** The pixel size each step of the scale draws at, read from the tokens the stylesheet uses. */
export const controlPx = { xs: 24, sm: 32, md: 40, lg: 48, xl: 56, xxl: 64 } as const
export const iconPx = { xs: 12, sm: 16, md: 20, lg: 24, xl: 32, xxl: 40 } as const
