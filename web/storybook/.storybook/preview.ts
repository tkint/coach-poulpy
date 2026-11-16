import type { Preview } from '@storybook/vue3-vite'
import { DocsContainer, type DocsContainerProps } from '@storybook/addon-docs/blocks'
import { addons } from 'storybook/preview-api'
import { GLOBALS_UPDATED, SET_GLOBALS } from 'storybook/internal/core-events'
import { createElement, useEffect, useState, type PropsWithChildren } from 'react'
import { setUiLocale } from '@coach-poulpy/ui'
import { preferredMode, rememberMode, themes, type Mode } from './themes'
import './preview.css'

// tokens.css switches light and dark from data-theme on <html>; the toolbar sets it, and it opens in the docs site's mode.
// A docs page with no story runs no decorator, so the page follows the toolbar here too.
let mode: Mode = preferredMode()
const follow = ({ globals }: { globals: { theme?: Mode } }) => {
  if (globals.theme !== 'light' && globals.theme !== 'dark') return
  mode = globals.theme
  document.documentElement.dataset.theme = mode
  rememberMode(mode)
}
addons.getChannel().on(SET_GLOBALS, follow)
addons.getChannel().on(GLOBALS_UPDATED, follow)

// The docs pages' own text, code blocks and tables take the same mode.
function ThemedDocsContainer(props: PropsWithChildren<DocsContainerProps>) {
  const [current, setCurrent] = useState(mode)
  useEffect(() => {
    const channel = props.context.channel
    const update = ({ globals }: { globals: { theme?: Mode } }) => {
      if (globals.theme === 'light' || globals.theme === 'dark') setCurrent(globals.theme)
    }
    channel.on(GLOBALS_UPDATED, update)
    return () => channel.off(GLOBALS_UPDATED, update)
  }, [props.context.channel])
  return createElement(DocsContainer, { ...props, theme: themes[current] })
}

const preview: Preview = {
  tags: ['autodocs'],
  initialGlobals: { theme: preferredMode(), locale: 'en' },
  globalTypes: {
    theme: {
      description: 'Light or dark',
      toolbar: { icon: 'mirror', items: [{ value: 'light', title: 'Light' }, { value: 'dark', title: 'Dark' }], dynamicTitle: true },
    },
    // The words the components say themselves (ADR 0031).
    locale: {
      description: 'Language of the components’ own words',
      toolbar: { icon: 'globe', items: [{ value: 'en', title: 'English' }, { value: 'fr', title: 'Français' }], dynamicTitle: true },
    },
  },
  decorators: [
    (story, context) => {
      document.documentElement.dataset.theme = context.globals.theme
      setUiLocale(context.globals.locale)
      // A screen shown behind a sheet is another screen's story, framed by the sheet's: it drops its device edge.
      document.documentElement.toggleAttribute('data-embedded', new URLSearchParams(location.search).has('embedded'))
      // A popover opens past its component, and the docs page clips whatever leaves the story's box:
      // stories that declare `parameters.popover` keep room below for it.
      // A screen draws its own page, edge to edge.
      if (context.parameters.mockup) return { components: { story }, template: '<story />' }
      const room = context.parameters.popover ? ' style="padding-bottom: 18rem"' : ''
      return { components: { story }, template: `<div class="bg-base-200 p-4"${room}><story /></div>` }
    },
  ],
  parameters: {
    layout: 'fullscreen',
    docs: { container: ThemedDocsContainer },
    a11y: { test: 'error' },
    options: { storySort: { order: ['Design system', ['Introduction', 'Rules', ['Colour', 'Type', 'Space, radius and motion', 'Icons', 'Accessibility', 'Components', 'Screens', 'Websites'], 'Tokens'], 'Brand', ['Introduction', 'Character', 'Moods', 'Inclusive by default', 'Voice, logo and type', 'Drawings', 'Poulpy in 3D', 'Poulpy moves', ['Turning', 'Little jumps', 'Jumping around', 'The champion', 'The cocky wink', 'Annoyed', 'Fuming', 'Getting angry'], 'Brand book', 'Decisions', 'Ideas for later'], 'Core', 'Web', ['Components', 'Patterns', 'Screens'], 'Mobile', ['Components', 'Patterns', 'Screens'], '*'] } },
  },
}
export default preview
