import DefaultTheme from 'vitepress/theme'
import type { Theme } from 'vitepress'
import ComponentSamples from './ComponentSamples.vue'
import MockupList from './MockupList.vue'
import MockupPage from './MockupPage.vue'
import PoulpyGallery from './PoulpyGallery.vue'
import TokenSwatches from './TokenSwatches.vue'
import './style.css'

export default {
  extends: DefaultTheme,
  enhanceApp({ app }) {
    app.component('ComponentSamples', ComponentSamples)
    app.component('MockupList', MockupList)
    app.component('MockupPage', MockupPage)
    app.component('PoulpyGallery', PoulpyGallery)
    app.component('TokenSwatches', TokenSwatches)
  },
} satisfies Theme
