// Copies the repo's shared Markdown into the site before each build, at the same paths as in
// the repo, so relative links work the same on GitHub and on the site. The copies are ignored.
import { cpSync, rmSync } from 'node:fs'
import { shared } from './published.mjs'

for (const path of shared) {
  rmSync(path, { recursive: true, force: true })
  cpSync(`../${path}`, path, { recursive: true })
}
cpSync('../design/poulpy/mood_happy.svg', 'public/poulpy.svg')

// The mockups link the token CSS and Poulpy's drawings by relative path, so they are copied with
// them at the same paths as in the repo (ADR 0016).
for (const path of ['design/mockups', 'design/poulpy', 'web/src/styles/tokens.css']) {
  rmSync(`public/${path}`, { recursive: true, force: true })
  cpSync(`../${path}`, `public/${path}`, { recursive: true })
}
