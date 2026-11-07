import DefaultTheme from 'vitepress/theme'
import type { Theme } from 'vitepress'
import BrandBook from './BrandBook.vue'
import BaseFieldSamples from './BaseFieldSamples.vue'
import ComponentSamples from './ComponentSamples.vue'
import MockupList from './MockupList.vue'
import MockupPage from './MockupPage.vue'
import PoulpyGallery from './PoulpyGallery.vue'
import SampleThemeSwitch from './SampleThemeSwitch.vue'
import TokenSwatches from './TokenSwatches.vue'
import WebComponentSamples from './WebComponentSamples.vue'
import './style.css'

export default {
  extends: DefaultTheme,
  enhanceApp({ app }) {
    app.component('BrandBook', BrandBook)
    app.component('SampleThemeSwitch', SampleThemeSwitch)
    app.component('BaseFieldSamples', BaseFieldSamples)
    app.component('ComponentSamples', ComponentSamples)
    app.component('MockupList', MockupList)
    app.component('MockupPage', MockupPage)
    app.component('PoulpyGallery', PoulpyGallery)
    app.component('TokenSwatches', TokenSwatches)
    app.component('WebComponentSamples', WebComponentSamples)
  },
} satisfies Theme
