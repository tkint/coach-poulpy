import DefaultTheme from 'vitepress/theme'
import type { Theme } from 'vitepress'
import { h } from 'vue'
import HomePoulpy from './HomePoulpy.vue'
import WorkshopLink from './WorkshopLink.vue'
import './style.css'

export default {
  extends: DefaultTheme,
  Layout: () => h(DefaultTheme.Layout, null, { 'home-hero-image': () => h(HomePoulpy) }),
  enhanceApp({ app }) {
    app.component('WorkshopLink', WorkshopLink)
  },
} satisfies Theme
