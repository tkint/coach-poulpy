import type { Preview } from '@storybook/vue3-vite'
import './preview.css'

// tokens.css switches light and dark from data-theme on <html>; the toolbar sets it.
const preview: Preview = {
  tags: ['autodocs'],
  initialGlobals: { theme: 'light' },
  globalTypes: {
    theme: {
      description: 'Light or dark',
      toolbar: { icon: 'mirror', items: [{ value: 'light', title: 'Light' }, { value: 'dark', title: 'Dark' }], dynamicTitle: true },
    },
  },
  decorators: [
    (story, context) => {
      document.documentElement.dataset.theme = context.globals.theme
      return { components: { story }, template: '<div class="bg-base-200 p-4"><story /></div>' }
    },
  ],
  parameters: { layout: 'fullscreen', a11y: { test: 'error' } },
}
export default preview
