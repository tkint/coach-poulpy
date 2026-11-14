import { defineComponent, h, onMounted, ref } from 'vue'
import { poulpyDrawings } from '@coach-poulpy/ui'
import { enableReorder } from './reorder'
import styles from './screens.css?inline'

// A phone or tablet screen of the apps (ADR 0029): its markup is plain HTML next to the story, drawn with
// screens.css in a shadow root, so the workshop's own styles (Tailwind's reset above all) never reach it, as
// they never reached the old mockup pages. The tokens come through from the page's :root, and so do the fonts.
// Poulpy's drawings are named by their file, as the markup writes them.

// A screen shown behind a sheet is a nested story; it takes the workshop's current theme.
const withTheme = (html: string) => html.replace(/(src="iframe\.html\?[^"]*)"/g, (_, src: string) => `${src}&amp;globals=theme:${document.documentElement.dataset.theme ?? 'light'}"`)

const markupOf = (html: string) => withTheme(html).replace(/(?:\.\.\/)+poulpy\/(\w+)\.svg/g, (path, name: string) => (poulpyDrawings as Record<string, string>)[name] ?? path)

const Screen = defineComponent({
  props: { markup: { type: String, required: true }, reorder: Boolean },
  setup(props) {
    const host = ref<HTMLElement>()
    onMounted(() => {
      const root = host.value!.attachShadow({ mode: 'open' })
      // A screen shown behind a sheet (?embedded) drops its device edge, as the sheet's own frame is the one on show.
      const embedded = new URLSearchParams(location.search).has('embedded') ? ' cp-screen--embedded' : ''
      // The stylesheet comes first, so a screen's own <style> rules (later in the markup) win ties, as they did in the old pages.
      root.innerHTML = `<style>${styles}</style><div class="cp-screen${embedded}">${markupOf(props.markup)}</div>`
      if (props.reorder) enableReorder(root.querySelector('.cp-screen') as HTMLElement)
    })
    return () => h('div', { ref: host, style: 'display: contents' })
  },
})

/** The render of a screen story; `reorder` makes the move buttons of its lists work, as a demo. */
export const screen = (html: string, options: { reorder?: boolean } = {}) => ({
  render: () => h(Screen, { markup: html, reorder: options.reorder ?? false }),
})
