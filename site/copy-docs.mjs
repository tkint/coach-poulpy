// Copies the repo's shared Markdown into the site before each build, at the same paths as in
// the repo, so relative links work the same on GitHub and on the site. The copies are ignored.
import { cpSync, rmSync } from 'node:fs'
import { execFileSync } from 'node:child_process'
import { dirname, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'
import { shared } from './published.mjs'

// Generate the ignored Tailwind/DaisyUI styles before VitePress imports the embed stylesheet. The docs
// build installs web/ dependencies first.
execFileSync('npm', ['run', 'build', '-w', 'ui'], {
  cwd: resolve(dirname(fileURLToPath(import.meta.url)), '../web'),
  stdio: 'inherit',
})

// The component workshop (web/storybook, ADR 0028) is built as static files and published under /storybook/,
// where the component workshop page embeds it. Its stories read the same CSS source as the apps.
const web = resolve(dirname(fileURLToPath(import.meta.url)), '../web')
execFileSync('npm', ['run', 'build', '-w', 'storybook'], { cwd: web, stdio: 'inherit' })
rmSync('public/storybook', { recursive: true, force: true })
cpSync(resolve(web, 'storybook/dist'), 'public/storybook', { recursive: true })

for (const path of shared) {
  rmSync(path, { recursive: true, force: true })
  cpSync(`../${path}`, path, { recursive: true })
}
cpSync('../design/poulpy/mood_happy.svg', 'public/poulpy.svg')

// Poulpy's drawings and 3D models are published at the same paths as in the repo, so the Drawings and
// Poulpy in 3D pages can offer them to download.
for (const path of ['design/poulpy', 'design/poulpy-3d']) {
  rmSync(`public/${path}`, { recursive: true, force: true })
  cpSync(`../${path}`, `public/${path}`, { recursive: true })
}
