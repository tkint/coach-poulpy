// Names shown for the mockup folders. Kept apart from mockups.mts, which reads files, so the
// browser can import them too.
export const stages = { agreed: 'Agreed', drafts: 'Drafts' } as const
export const devices = { phone: 'Phone', tablet: 'Tablet', web: 'Website' } as const
export const platforms = { mobile: 'Mobile', web: 'Web' } as const
/** Phones and tablets draw the native apps; the website draws the web design system. */
export const platformOf = (device: keyof typeof devices): keyof typeof platforms => (device === 'web' ? 'web' : 'mobile')

/** A tablet mockup shows under its own heading; others keep the section declared in their HTML metadata. */
export const groupOf = (m: { device: keyof typeof devices; section: string; screen: string }) => {
  if (m.device === 'tablet') return `Tablet · ${m.section}`
  return m.section
}

export const slug = (title: string) => title.toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/^-|-$/g, '')
