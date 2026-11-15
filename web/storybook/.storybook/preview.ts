import type { Preview } from '@storybook/vue3-vite'
import { setUiLocale } from '@coach-poulpy/ui'
import './preview.css'

// tokens.css switches light and dark from data-theme on <html>; the toolbar sets it.
const preview: Preview = {
  tags: ['autodocs'],
  initialGlobals: { theme: 'light', locale: 'en' },
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
    a11y: { test: 'error' },
    options: { storySort: { order: ['Design system', ['Introduction', 'Rules', 'Tokens'], 'Core', 'Web', 'Mobile', '*'] } },
  },
}
export default preview
