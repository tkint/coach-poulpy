// The words the shared components speak themselves (close, next page, show password…), from
// i18n/messages/<locale>/ui.json, read as they are like the website's own (ADR 0031). A caller that
// needs other words passes them as props; otherwise the component says them in the page's language.
import { ref } from 'vue'
import en from '../../../i18n/messages/en/ui.json'
import fr from '../../../i18n/messages/fr/ui.json'
import domainEn from '../../../i18n/messages/en/domain.json'
import domainFr from '../../../i18n/messages/fr/domain.json'

type Messages = { [key: string]: string | Messages }
const messages: Record<string, Messages> = { en, fr }

const browserLocale = typeof navigator === 'undefined' ? 'en' : (navigator.languages.map((tag) => tag.slice(0, 2).toLowerCase()).find((tag) => tag in messages) ?? 'en')

/** The language the components speak: the browser's when we ship it, English otherwise. */
export const uiLocale = ref(browserLocale)

/** Speaks another language, for a page with its own choice and for the workshop's language toolbar. */
export function setUiLocale(locale: string): void {
  uiLocale.value = locale in messages ? locale : 'en'
}

/** The component message at [id], such as `ui.close`. */
export function uiText(id: string): string {
  let found: string | Messages | undefined = messages[uiLocale.value]
  for (const part of id.split('.')) found = typeof found === 'object' ? found[part] : undefined
  if (typeof found !== 'string') throw new Error(`No ui message ${id}`)
  return found
}

const bodyAreas: Record<string, Record<string, string>> = { en: domainEn.bodyArea, fr: domainFr.bodyArea }

/** The name of a body map area as the shared `domain.bodyArea` words say it, such as "Left shoulder" for `deltoid` on the left. */
export function bodyAreaText(area: string, side?: 'left' | 'right'): string {
  const camel = area.replace(/-(\w)/g, (_, letter: string) => letter.toUpperCase())
  const key = side ? `${camel}${side === 'left' ? 'Left' : 'Right'}` : camel
  return bodyAreas[uiLocale.value]?.[key] ?? bodyAreas.en[key] ?? area
}
