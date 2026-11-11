// Builds ignored poulpy-ui.css and poulpy-ui-embed.css from src/prebuilt.css and src/embed.css (ADR 0023).
// The web app and docs-site build generate them; plain-HTML mockups use the prebuilt file copied
// into the docs site's public tree.
// Run it after changing theme.css, a component's classes, or the classes a web mockup uses.
import { execFileSync } from 'node:child_process'
import { existsSync, writeFileSync } from 'node:fs'
import { dirname, join } from 'node:path'
import { fileURLToPath } from 'node:url'
import postcss from 'postcss'

const here = dirname(fileURLToPath(import.meta.url))
const cli = join(here, 'node_modules/.bin/tailwindcss')
const bin = existsSync(cli) ? cli : join(here, '../node_modules/.bin/tailwindcss')
const banner = '/* Generated from web/ui/src by web/ui/build-css.mjs. Do not edit by hand. */\n'

// The embed build leaves its cascade layers: a page that has its own unlayered styles, as the
// docs site does (VitePress resets every button), would otherwise win over every DaisyUI class.
const unlayer = (css) => {
  const ast = postcss.parse(css)
  // Layers nest, so unwrap until none is left.
  for (let found = true; found; ) {
    found = false
    ast.walkAtRules('layer', (rule) => {
      found = true
      if (rule.nodes) rule.replaceWith(rule.nodes)
      else rule.remove()
    })
  }
  return ast.toString()
}
// CLEANUP: DaisyUI 5.7.50 prefixes the decimals of its rotator keyframes (`89.cp-9999%`), which
// browsers reject. Remove this once a DaisyUI release keeps keyframe percentages as they are.
const unprefixKeyframes = (css) => css.replace(/(\d)\.cp-(\d)/g, '$1.$2')
const outputs = {
  'poulpy-ui.css': { input: 'src/prebuilt.css', post: unprefixKeyframes },
  'poulpy-ui-embed.css': { input: 'src/embed.css', post: (css) => unlayer(unprefixKeyframes(css)) },
}
for (const [out, { input, post }] of Object.entries(outputs)) {
  const css = banner + post(execFileSync(bin, ['--input', join(here, input)], { cwd: here, encoding: 'utf8', stdio: ['ignore', 'pipe', 'pipe'] }))
  const path = join(here, out)
  writeFileSync(path, css)
  console.log(`Wrote web/ui/${out}`)
}
