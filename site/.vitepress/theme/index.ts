import DefaultTheme from 'vitepress/theme'
import type { Theme } from 'vitepress'
import BrandBook from './BrandBook.vue'
import MockupList from './MockupList.vue'
import MockupPage from './MockupPage.vue'
import Poulpy3d from './Poulpy3d.vue'
import PoulpyGallery from './PoulpyGallery.vue'
import WorkshopLink from './WorkshopLink.vue'
import './style.css'

export default {
  extends: DefaultTheme,
  enhanceApp({ app }) {
    app.component('BrandBook', BrandBook)
    app.component('MockupList', MockupList)
    app.component('MockupPage', MockupPage)
    app.component('Poulpy3d', Poulpy3d)
    app.component('PoulpyGallery', PoulpyGallery)
    app.component('WorkshopLink', WorkshopLink)
  },
} satisfies Theme
