import { create } from 'storybook/theming'
import tokens from '../../../design/tokens.json'

export type Mode = 'light' | 'dark'

// The workshop's own frame (sidebar, toolbar, docs pages) wears the Poulpy theme, light or dark, from design/tokens.json.
const themeFor = (mode: Mode) => {
  const t = tokens.theme[mode]
  return create({
    base: mode,
    // The workshop is published inside the docs site at storybook/ (ADR 0032), so the title links one
    // level up to the docs home, on the live site and on every PR preview alike.
    brandTitle: 'Coach Poulpy docs',
    brandUrl: '../',
    brandTarget: '_self',
    colorPrimary: t.primary,
    colorSecondary: t.primary,
    appBg: t.background,
    appContentBg: t.surface,
    appPreviewBg: t.background,
    appBorderColor: t.outline,
    appBorderRadius: tokens.radius.md,
    fontBase: `"${tokens.font.body.family}", ${tokens.font.body.fallback}`,
    textColor: t.onSurface,
    textInverseColor: t.surface,
    textMutedColor: t.onSurfaceVariant,
    barBg: t.surface,
    barTextColor: t.onSurfaceVariant,
    barHoverColor: t.primary,
    barSelectedColor: t.primary,
    buttonBg: t.surfaceVariant,
    buttonBorder: t.outline,
    booleanBg: t.surfaceVariant,
    booleanSelectedBg: t.surface,
    inputBg: t.surface,
    inputBorder: t.outline,
    inputTextColor: t.onSurface,
    inputBorderRadius: tokens.radius.sm,
  })
}

export const themes = { light: themeFor('light'), dark: themeFor('dark') }

// The docs site (VitePress) keeps the reader's choice under this key, on the same origin as the workshop:
// the workshop opens in the same mode as the site, and a switch in the workshop carries back to the site.
const siteKey = 'vitepress-theme-appearance'

export function preferredMode(): Mode {
  try {
    const saved = localStorage.getItem(siteKey)
    if (saved === 'light' || saved === 'dark') return saved
  } catch {
    // No storage (a private window): fall back to the system's choice.
  }
  return matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'
}

export function rememberMode(mode: Mode) {
  if (mode === preferredMode()) return // leaves the site on "auto" while the system already agrees
  try {
    localStorage.setItem(siteKey, mode)
  } catch {
    // Nothing to carry back without storage.
  }
}
