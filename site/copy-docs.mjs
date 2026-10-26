// Copies the repo's shared Markdown into the site before each build, at the same paths as in
// the repo, so relative links work the same on GitHub and on the site. The copies are ignored.
import { cpSync, renameSync, rmSync, statSync } from 'node:fs'

const shared = ['CONTEXT.md', 'CONTRIBUTING.md', 'docs']
for (const path of shared) {
  rmSync(path, { recursive: true, force: true })
  cpSync(`../${path}`, path, { recursive: true, filter: (src) => !src.includes('docs/agents') })
}
// The translation guide and glossaries, without the message files; the README becomes the
// section's index, because the site skips README.md files.
rmSync('i18n', { recursive: true, force: true })
cpSync('../i18n', 'i18n', { recursive: true, filter: (src) => statSync(src).isDirectory() || src.endsWith('.md') })
renameSync('i18n/README.md', 'i18n/index.md')
cpSync('../design/poulpy/mood_happy.svg', 'public/poulpy.svg')

// The mockups link the token CSS and Poulpy's drawings by relative path, so they are copied with
// them at the same paths as in the repo (ADR 0016).
for (const path of ['design/mockups', 'design/poulpy', 'web/src/styles/tokens.css']) {
  rmSync(`public/${path}`, { recursive: true, force: true })
  cpSync(`../${path}`, `public/${path}`, { recursive: true })
}
