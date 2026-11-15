// How the admin words one changed field of an exercise in a publication review: its label from the field's path
// in the exercise JSON (`name.fr`, `instructions.2.en`, `muscleGroups`) and its value as the phones' domain text names it.
import { domainLabel } from './catalogText'
import { t } from './i18n'

const LANGUAGES: Record<string, string> = { en: 'admin.languageEn', fr: 'admin.languageFr' }
const language = (code: string) => (LANGUAGES[code] ? t(LANGUAGES[code]) : code)
const LABELS: Record<string, string> = {
  muscleGroups: 'admin.changeMuscles',
  equipment: 'admin.changeEquipment',
  optionalEquipment: 'admin.changeOptionalEquipment',
  movementPattern: 'admin.changePattern',
  measurement: 'admin.changeMeasurement',
  perSide: 'admin.changePerSide',
  focus: 'admin.changeFocus',
  positions: 'admin.changePositions',
  jumps: 'admin.changeJumps',
}
/** Which domain words name the values of a list or a code field. */
const GROUPS = { muscleGroups: 'muscle', optionalEquipment: 'equipment', movementPattern: 'pattern', measurement: 'measurement', focus: 'focus', positions: 'position' } as const

/** The label of the field at [path], such as "French name" for `name.fr` and "Step 3 · English" for `instructions.2.en`. */
export function fieldLabel(path: string): string {
  const [head, first, second] = path.split('.')
  if (head === 'name') return t('admin.changeName', { language: language(first) })
  if (head === 'aliases') return t('admin.changeAliases', { language: language(first) })
  if (head === 'variationNote') return t('admin.changeVariationNote', { language: language(first) })
  if (head === 'instructions') return t('admin.changeStep', { n: String(Number(first) + 1), language: language(second) })
  return LABELS[head] ? t(LABELS[head]) : path
}

/** [value] of the field at [path] in words; "Nothing" when the field is absent. */
export function fieldValue(path: string, value: unknown): string {
  if (value === null || value === undefined) return t('admin.changeNothing')
  const head = path.split('.')[0] as keyof typeof GROUPS | 'equipment'
  if (typeof value === 'boolean') return t(value ? 'admin.changeYes' : 'admin.changeNo')
  if (Array.isArray(value)) {
    if (head === 'equipment') return value.map((need) => (need as string[]).map((one) => domainLabel('equipment', one)).join(' / ')).join(', ')
    const group = GROUPS[head as keyof typeof GROUPS]
    return value.map((one) => (group ? domainLabel(group, String(one)) : String(one))).join(', ')
  }
  const group = GROUPS[head as keyof typeof GROUPS]
  return group ? domainLabel(group, String(value)) : String(value)
}
