import DefaultTheme from 'vitepress/theme'
import type { Theme } from 'vitepress'
import ComponentSamples from './ComponentSamples.vue'
import PoulpyGallery from './PoulpyGallery.vue'
import TokenSwatches from './TokenSwatches.vue'
import './style.css'

export default {
  extends: DefaultTheme,
  enhanceApp({ app }) {
    app.component('ComponentSamples', ComponentSamples)
    app.component('PoulpyGallery', PoulpyGallery)
    app.component('TokenSwatches', TokenSwatches)
  },
} satisfies Theme
