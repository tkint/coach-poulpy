import DefaultTheme from 'vitepress/theme'
import type { Theme } from 'vitepress'
import ComponentSamples from './ComponentSamples.vue'
import MockupGallery from './MockupGallery.vue'
import MockupPage from './MockupPage.vue'
import PoulpyGallery from './PoulpyGallery.vue'
import TokenSwatches from './TokenSwatches.vue'
import './style.css'

export default {
  extends: DefaultTheme,
  enhanceApp({ app }) {
    app.component('ComponentSamples', ComponentSamples)
    app.component('MockupGallery', MockupGallery)
    app.component('MockupPage', MockupPage)
    app.component('PoulpyGallery', PoulpyGallery)
    app.component('TokenSwatches', TokenSwatches)
  },
} satisfies Theme
