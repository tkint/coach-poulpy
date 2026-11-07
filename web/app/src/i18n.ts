// The website's words, from i18n/messages/<locale>/web.json (i18n/README.md), in the browser's
// language when it is one we ship, English otherwise.
import en from '../../../i18n/messages/en/web.json'
import fr from '../../../i18n/messages/fr/web.json'

type Messages = { [key: string]: string | Messages }
const messages: Record<string, Messages> = { en, fr }

export const locale = navigator.languages.map((tag) => tag.slice(0, 2).toLowerCase()).find((tag) => tag in messages) ?? 'en'

/** The message at [id], such as `account.confirmedTitle`, with `{name}` arguments filled in. */
export function t(id: string, args: Record<string, string> = {}): string {
  let found: string | Messages | undefined = messages[locale]
  for (const part of id.split('.')) found = typeof found === 'object' ? found[part] : undefined
  if (typeof found !== 'string') throw new Error(`No web message ${id}`)
  return found.replace(/\{(\w+)\}/g, (whole, name: string) => args[name] ?? whole)
}
