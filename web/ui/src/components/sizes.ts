// The websites' one size scale for buttons, fields and icons: xs, sm, md, lg, xl, xxl, md when none is given.
// Heights and icon boxes come from `webSize` in design/tokens.json (--cp-control-* and --cp-icon-*, see theme.css).
//
// The class names are written out in full on purpose: DaisyUI and Tailwind only emit the classes they find in
// the components' source, so a name built from `size` would never reach the stylesheet. This file lives in
// components/ for that reason.
export const cpSizes = ['xs', 'sm', 'md', 'lg', 'xl', 'xxl'] as const
export type CpSize = (typeof cpSizes)[number]
export const defaultSize: CpSize = 'md'

export const buttonSize: Record<CpSize, string> = { xs: 'cp-btn-xs', sm: 'cp-btn-sm', md: 'cp-btn-md', lg: 'cp-btn-lg', xl: 'cp-btn-xl', xxl: 'cp-btn-xxl' }
export const inputSize: Record<CpSize, string> = { xs: 'cp-input-xs', sm: 'cp-input-sm', md: 'cp-input-md', lg: 'cp-input-lg', xl: 'cp-input-xl', xxl: 'cp-input-xxl' }

// The icon a control of a given size carries: its own icon (a search field's magnifier, a button's spinner),
// and the quieter one beside its text (a select's chevron).
export const controlIcon: Record<CpSize, CpSize> = { xs: 'xs', sm: 'sm', md: 'md', lg: 'md', xl: 'md', xxl: 'lg' }
export const controlChevron: Record<CpSize, CpSize> = { xs: 'xs', sm: 'sm', md: 'sm', lg: 'sm', xl: 'sm', xxl: 'md' }
// The size of the small round button inside a field of a given size (a password's eye, a search's clear).
export const insideButton: Record<CpSize, CpSize> = { xs: 'xs', sm: 'xs', md: 'sm', lg: 'sm', xl: 'sm', xxl: 'md' }
