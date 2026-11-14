// The words for an exercise's muscles, equipment, positions and movement patterns, from the shared domain text
// (i18n/messages/<locale>/domain.json), so the admin names them as the phones do.
import en from '../../../i18n/messages/en/domain.json'
import fr from '../../../i18n/messages/fr/domain.json'
import { locale } from './i18n'

type Group = 'muscle' | 'equipment' | 'pattern' | 'position'
const domain: Record<string, Record<string, Record<string, string>>> = { en, fr } as never

/** `quadriceps`, `pull_up_bar` or `QUADRICEPS` becomes the label of `muscle.quadriceps`, `equipment.pullUpBar`. */
export function domainLabel(group: Group, code: string): string {
  const key = code.toLowerCase().replace(/_(\w)/g, (_, letter: string) => letter.toUpperCase())
  return domain[locale][group][key] ?? domain.en[group][key] ?? code
}

/** Every value of [group] as a query word and its label, sorted by label, for a filter's options. */
export function domainOptions(group: Group): { value: string; text: string }[] {
  return Object.keys(domain.en[group])
    .map((key) => ({ value: key.replace(/[A-Z]/g, (letter) => `_${letter.toLowerCase()}`), text: domainLabel(group, key) }))
    .sort((a, b) => a.text.localeCompare(b.text, locale))
}
