import { addons } from 'storybook/manager-api'
import { GLOBALS_UPDATED, SET_GLOBALS } from 'storybook/internal/core-events'
import { preferredMode, themes, type Mode } from './themes'

addons.setConfig({ theme: themes[preferredMode()] })

// The frame follows the Light or dark toolbar, so the sidebar, toolbar and stories always match.
addons.register('coach-poulpy/theme', (api) => {
  const follow = ({ globals }: { globals: { theme?: Mode } }) => {
    if (globals.theme === 'light' || globals.theme === 'dark') api.setOptions({ theme: themes[globals.theme] })
  }
  api.on(SET_GLOBALS, follow)
  api.on(GLOBALS_UPDATED, follow)
})
