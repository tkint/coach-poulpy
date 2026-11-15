import { addons } from 'storybook/manager-api'
import { create } from 'storybook/theming'

// The workshop is published inside the docs site at storybook/ (ADR 0032), so the title links one
// level up to the docs home, on the live site and on every PR preview alike.
addons.setConfig({
  theme: create({
    base: 'light',
    brandTitle: 'Coach Poulpy docs',
    brandUrl: '../',
    brandTarget: '_self',
  }),
})
