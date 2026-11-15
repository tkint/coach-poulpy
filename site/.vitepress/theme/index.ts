import DefaultTheme from 'vitepress/theme'
import type { Theme } from 'vitepress'
import WorkshopLink from './WorkshopLink.vue'
import './style.css'

export default {
  extends: DefaultTheme,
  enhanceApp({ app }) {
    app.component('WorkshopLink', WorkshopLink)
  },
} satisfies Theme
