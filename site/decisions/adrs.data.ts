import { createContentLoader } from 'vitepress'

export interface Adr {
  number: string
  title: string
  status: string
  date: string
  url: string
}

declare const data: Adr[]
export { data }

export default createContentLoader('docs/adr/*.md', {
  includeSrc: true,
  transform: (pages) =>
    pages
      .map((page) => ({
        number: page.url.split('/').pop()!.slice(0, 4),
        title: page.src?.match(/^# (.+)$/m)?.[1] ?? page.url,
        status: page.frontmatter.status ?? 'accepted',
        date: page.frontmatter.date instanceof Date ? page.frontmatter.date.toISOString().slice(0, 10) : String(page.frontmatter.date ?? ''),
        url: page.url,
      }))
      .sort((a, b) => a.number.localeCompare(b.number)),
})
