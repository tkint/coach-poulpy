// Names shown for the mockup folders. Kept apart from mockups.mts, which reads files, so the
// browser can import them too.
export const stages = { agreed: 'Agreed', drafts: 'Drafts' } as const
export const devices = { phone: 'Phone', tablet: 'Tablet', web: 'Website' } as const
export const platforms = { mobile: 'Mobile', web: 'Web' } as const
/** Phones and tablets draw the native apps; the website draws the web design system. */
export const platformOf = (device: keyof typeof devices): keyof typeof platforms => (device === 'web' ? 'web' : 'mobile')

/**
 * Each web app view gets its own documentation page. Catalog and exercise are separate views,
 * even though their routes share the catalog feature; the other admin areas are independent too.
 * Other mockups keep the section declared in their HTML metadata.
 */
export const groupOf = (m: { device: keyof typeof devices; section: string; screen: string }) => {
  if (m.device === 'tablet') return `Tablet · ${m.section}`
  if (m.device !== 'web') return m.section

  const screen = m.screen
  if (screen === 'admin-shell' || screen.startsWith('admin-audit')) return 'Admin · Audit'
  if (screen.startsWith('admin-sign-in')) return 'Admin · Sign in'
  if (screen.startsWith('admin-accounts')) return 'Admin · Accounts'
  if (screen.startsWith('admin-settings')) return 'Admin · Settings'
  if (screen.startsWith('admin-status')) return 'Admin · Status'
  if (screen.startsWith('admin-catalog-exercise')) return 'Admin · Exercise'
  if (screen.startsWith('admin-catalog')) return 'Admin · Catalog'
  if (screen.startsWith('app-sign-in')) return 'Web app · Sign in'
  if (screen.startsWith('app-status')) return 'Web app · Status'
  if (screen === 'app-profile') return 'Web app · Profile'
  if (screen === 'app-workouts') return 'Web app · Workouts'
  if (screen.startsWith('app-workout')) return 'Web app · Workout'

  return m.section
}

export const slug = (title: string) => title.toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/^-|-$/g, '')
