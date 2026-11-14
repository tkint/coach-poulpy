// Copies the repo's shared Markdown into the site before each build, at the same paths as in
// the repo, so relative links work the same on GitHub and on the site. The copies are ignored.
import { cpSync, rmSync } from 'node:fs'
import { execFileSync } from 'node:child_process'
import { dirname, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'
import { shared } from './published.mjs'

// Generate the ignored Tailwind/DaisyUI styles before copying the plain-HTML mockups and before
// VitePress imports the embed stylesheet. The docs build installs web/ dependencies first.
execFileSync('npm', ['run', 'build', '-w', 'ui'], {
  cwd: resolve(dirname(fileURLToPath(import.meta.url)), '../web'),
  stdio: 'inherit',
})

for (const path of shared) {
  rmSync(path, { recursive: true, force: true })
  cpSync(`../${path}`, path, { recursive: true })
}
cpSync('../design/poulpy/mood_happy.svg', 'public/poulpy.svg')

// The mockups link the token CSS, the web design system's CSS and Poulpy's drawings by relative
// path, so they are copied with them at the same paths as in the repo (ADR 0016, ADR 0023). The 3D
// models go the same way, so the Poulpy in 3D page can offer them to download.
for (const path of ['design/mockups', 'design/poulpy', 'design/poulpy-3d', 'web/ui/src/tokens.css', 'web/ui/poulpy-ui.css']) {
  rmSync(`public/${path}`, { recursive: true, force: true })
  cpSync(`../${path}`, `public/${path}`, { recursive: true })
}
