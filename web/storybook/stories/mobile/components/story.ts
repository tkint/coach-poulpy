import { h } from 'vue'
import { Screen } from '../screens/screen'

type Args = Record<string, unknown>

// Writes a component's args as the attributes of its tag in a screen's template: text as is, a flag bare, a number bound.
const attributes = (args: Args, skip: string[]) =>
  Object.entries(args)
    .filter(([key, value]) => !skip.includes(key) && value !== undefined && value !== false && value !== '')
    .map(([key, value]) => {
      const name = key.replace(/[A-Z]/g, (c) => `-${c.toLowerCase()}`)
      if (value === true) return ` ${name}`
      if (typeof value === 'number') return ` :${name}="${value}"`
      return ` ${name}="${String(value).replace(/"/g, '&quot;')}"`
    })
    .join('')

/**
 * The render of a mobile component's story: the component drawn the way the screens draw it (screens.css in a shadow
 * root), in a `wrapper` styled by `frame`. Its args are its attributes, except the `content` one (`label` unless
 * told otherwise; `''` for none), which goes between its tags. It redraws when a control changes.
 */
export const renderComponent =
  (tag: string, frame = 'width: 100%; max-width: 390px; padding: 16px; display: flex; gap: 8px; align-items: center;', { wrapper = 'div', content = 'label' } = {}) =>
  (args: Args) => {
    const text = content && typeof args[content] === 'string' ? (args[content] as string) : ''
    const skip = text ? [content] : []
    const element = text ? `<${tag}${attributes(args, skip)}>${text}</${tag}>` : `<${tag}${attributes(args, skip)} />`
    return h(Screen, { key: JSON.stringify(args), markup: `<${wrapper} style="${frame}">${element}</${wrapper}>` })
  }
