// The address bar as the pages' state: a page reads the current URL here and changes it with `navigate`,
// so the back button, a reload and a link to a screen all land on the same view. The screens of the web
// keep their own state (the open tab, a table's sort, filters and page, the open row) in the URL.
import { computed, ref } from 'vue'

const current = ref(new URL(location.href))
addEventListener('popstate', () => (current.value = new URL(location.href)))

/** The path, without a trailing slash. */
export const path = computed(() => current.value.pathname.replace(/\/+$/, '') || '/')

/** The query string, read-only; change it with `navigate`. */
export const search = computed(() => current.value.searchParams)

/** Goes to [to] (a path and its query). `push` adds a history entry; `replace` rewrites the current one, for typing. */
export function navigate(to: string, mode: 'push' | 'replace' = 'push') {
  const target = new URL(to, location.href)
  if (target.href === current.value.href) return
  if (mode === 'push') history.pushState(null, '', target)
  else history.replaceState(null, '', target)
  current.value = new URL(location.href)
}

/** [path] with the non-empty [params]; a value equal to its default isn't written, so the plain address stays plain. */
export function withParams(base: string, params: Record<string, string>, defaults: Record<string, string> = {}): string {
  const query = new URLSearchParams()
  for (const [name, value] of Object.entries(params)) if (value !== '' && value !== defaults[name]) query.set(name, value)
  const text = query.toString()
  return text ? `${base}?${text}` : base
}
