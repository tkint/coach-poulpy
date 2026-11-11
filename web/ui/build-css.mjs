// Builds poulpy-ui.css and poulpy-ui-embed.css from src/prebuilt.css and src/embed.css (ADR 0023).
//   node build-css.mjs          writes them
//   node build-css.mjs --check  fails when a committed file no longer matches (CI)
// They are committed so plain-HTML web mockups can link them with no build, as tokens.css is.
// Run it after changing theme.css, a component's classes, or the classes a web mockup uses.
import { execFileSync } from 'node:child_process'
import { existsSync, readFileSync, writeFileSync } from 'node:fs'
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
const check = process.argv.includes('--check')
let stale = 0
for (const [out, { input, post }] of Object.entries(outputs)) {
  const css = banner + post(execFileSync(bin, ['--input', join(here, input)], { cwd: here, encoding: 'utf8', stdio: ['ignore', 'pipe', 'pipe'] }))
  if (check && out === 'poulpy-ui-embed.css') {
    const encoded = Buffer.from(css).toString('base64')
    console.log('POULPY_UI_EMBED_CSS_BEGIN')
    for (let offset = 0; offset < encoded.length; offset += 8000) console.log(encoded.slice(offset, offset + 8000))
    console.log('POULPY_UI_EMBED_CSS_END')
  }
  const path = join(here, out)
  const current = existsSync(path) ? readFileSync(path, 'utf8') : null
  if (current === css) continue
  if (check) {
    console.error(`Out of date: web/ui/${out}`)
    stale++
  } else {
    writeFileSync(path, css)
    console.log(`Wrote web/ui/${out}`)
  }
}
if (stale) {
  console.error('Run `npm run build -w ui` in web/ and commit the result.')
  process.exit(1)
}
