// How the web app writes a day: "Today", or "Fri 23 Oct" in the browser's language.
import { locale, t } from './i18n'

const day = new Intl.DateTimeFormat(locale, { weekday: 'short', day: 'numeric', month: 'short' })

/** "Today" for a moment today, otherwise the day with its weekday. */
export function formatDay(at: string): string {
  const date = new Date(at)
  return date.toDateString() === new Date().toDateString() ? t('app.today') : day.format(date)
}
