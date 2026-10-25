// Copies the repo's shared Markdown into the site before each build, at the same paths as in
// the repo, so relative links work the same on GitHub and on the site. The copies are ignored.
import { cpSync, rmSync } from 'node:fs'

const shared = ['CONTEXT.md', 'CONTRIBUTING.md', 'docs']
for (const path of shared) {
  rmSync(path, { recursive: true, force: true })
  cpSync(`../${path}`, path, { recursive: true, filter: (src) => !src.includes('docs/agents') })
}
cpSync('../design/poulpy/mood_happy.svg', 'public/poulpy.svg')
