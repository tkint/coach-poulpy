// Generates the apps' Lucide icons (ADR 0024) from the Lucide drawings kept in design/icons.
//   node design/build-icons.mjs              write the Android and iOS files
//   node design/build-icons.mjs --check      fail if a generated file is out of date (used by CI)
//   node design/build-icons.mjs --add a b    copy Lucide icons a and b into design/icons, then write
//
// design/icons/<name>.svg is a Lucide icon, copied unchanged from the lucide-static package at the version
// web/ui uses for @lucide/vue, so the websites, the mockups and the apps draw the same icon. Every icon there is
// written for both apps:
//   app-android/src/main/kotlin/ch/coachpoulpy/android/ui/Lucide.kt  Compose ImageVectors (`Lucide.ChevronRight`)
//   app-ios/CoachPoulpy/Assets.xcassets/Lucide/<Name>.imageset       iOS vector template images
//   app-ios/CoachPoulpy/Lucide.swift                                  the `Lucide` enum and `LucideIcon` view
// Each keeps Lucide's 24 box and its 2 wide round stroke; the apps tint them with a colour role.
import { existsSync, mkdirSync, readFileSync, readdirSync, rmSync, writeFileSync } from 'node:fs'
import { dirname, join } from 'node:path'
import { fileURLToPath } from 'node:url'

const root = join(dirname(fileURLToPath(import.meta.url)), '..')
const ICONS = join(root, 'design/icons')
const ANDROID = 'app-android/src/main/kotlin/ch/coachpoulpy/android/ui/Lucide.kt'
const IOS_SWIFT = 'app-ios/CoachPoulpy/Lucide.swift'
const IOS_ASSETS = 'app-ios/CoachPoulpy/Assets.xcassets/Lucide'
const version = JSON.parse(readFileSync(join(root, 'web/ui/package.json'), 'utf8')).dependencies['@lucide/vue']

const args = process.argv.slice(2)
const check = args.includes('--check')
if (args[0] === '--add') {
  for (const name of args.slice(1)) {
    const url = `https://cdn.jsdelivr.net/npm/lucide-static@${version}/icons/${name}.svg`
    const response = await fetch(url)
    if (!response.ok) { console.error(`No Lucide icon "${name}" in lucide-static ${version} (${url})`); process.exit(1) }
    writeFileSync(join(ICONS, `${name}.svg`), await response.text())
    console.log(`Added design/icons/${name}.svg`)
  }
}

const pascal = (name) => name.split('-').map((w) => w[0].toUpperCase() + w.slice(1)).join('')
const camel = (name) => pascal(name)[0].toLowerCase() + pascal(name).slice(1)
// A case named after a Swift keyword (`repeat`) is declared in backticks; `.repeat` still reads it.
const SWIFT_KEYWORDS = new Set([
  'as', 'break', 'case', 'catch', 'class', 'continue', 'default', 'defer', 'do', 'else', 'enum', 'extension', 'false', 'for',
  'func', 'guard', 'if', 'import', 'in', 'init', 'is', 'let', 'nil', 'operator', 'protocol', 'repeat', 'return', 'self',
  'static', 'struct', 'subscript', 'super', 'switch', 'throw', 'true', 'try', 'var', 'where', 'while',
])
const swiftName = (name) => (SWIFT_KEYWORDS.has(camel(name)) ? `\`${camel(name)}\`` : camel(name))
const num = (n) => String(Math.round(n * 1e4) / 1e4)

// The elements of a Lucide SVG, as [tag, attributes].
export const elementsOf = (svg) =>
  [...svg.matchAll(/<(path|circle|rect|line|polyline|polygon|ellipse)\b([^>]*?)\/?>/g)].map((m) => [
    m[1],
    Object.fromEntries([...m[2].matchAll(/([\w-]+)="([^"]*)"/g)].map((a) => [a[1], a[2]])),
  ])

// Path data with every number and arc flag spelled out and spaced, which every parser reads the same way.
const ARGS = { m: 2, l: 2, h: 1, v: 1, c: 6, s: 4, q: 4, t: 2, a: 7, z: 0 }
function spacedPath(d) {
  const out = []
  let i = 0, command = null
  const skip = () => { while (i < d.length && /[\s,]/.test(d[i])) i++ }
  const number = () => {
    skip()
    const m = /^[+-]?(\d+\.?\d*|\.\d+)(e[+-]?\d+)?/i.exec(d.slice(i))
    if (!m) throw new Error(`Bad path data at ${i}: ${d}`)
    i += m[0].length
    return m[0]
  }
  const flag = () => { skip(); const f = d[i++]; if (f !== '0' && f !== '1') throw new Error(`Bad arc flag: ${d}`); return f }
  for (skip(); i < d.length; skip()) {
    if (/[a-z]/i.test(d[i])) { command = d[i++]; out.push(command) }
    const count = ARGS[command.toLowerCase()]
    if (count === 0) continue
    for (let k = 0; k < count; k++) out.push(command.toLowerCase() === 'a' && (k === 3 || k === 4) ? flag() : number())
  }
  return out.join(' ')
}

// Every element as path data: Compose only draws paths.
function pathOf([tag, a]) {
  const n = (k, fallback = 0) => (a[k] === undefined ? fallback : Number(a[k]))
  switch (tag) {
    case 'path': return spacedPath(a.d)
    case 'line': return `M ${n('x1')} ${n('y1')} L ${n('x2')} ${n('y2')}`
    case 'polyline':
    case 'polygon': {
      const p = a.points.trim().split(/[\s,]+/)
      const pairs = []
      for (let k = 0; k < p.length; k += 2) pairs.push(`${p[k]} ${p[k + 1]}`)
      return `M ${pairs[0]} L ${pairs.slice(1).join(' L ')}${tag === 'polygon' ? ' Z' : ''}`
    }
    case 'circle':
    case 'ellipse': {
      const cx = n('cx'), cy = n('cy'), rx = tag === 'circle' ? n('r') : n('rx'), ry = tag === 'circle' ? n('r') : n('ry')
      return `M ${num(cx - rx)} ${num(cy)} A ${num(rx)} ${num(ry)} 0 1 0 ${num(cx + rx)} ${num(cy)} A ${num(rx)} ${num(ry)} 0 1 0 ${num(cx - rx)} ${num(cy)} Z`
    }
    case 'rect': {
      const x = n('x'), y = n('y'), w = n('width'), h = n('height')
      const rx = Math.min(n('rx', n('ry')), w / 2), ry = Math.min(n('ry', n('rx')), h / 2)
      if (!rx || !ry) return `M ${num(x)} ${num(y)} H ${num(x + w)} V ${num(y + h)} H ${num(x)} Z`
      const arc = (ex, ey) => `A ${num(rx)} ${num(ry)} 0 0 1 ${num(ex)} ${num(ey)}`
      return [
        `M ${num(x + rx)} ${num(y)}`, `H ${num(x + w - rx)}`, arc(x + w, y + ry), `V ${num(y + h - ry)}`, arc(x + w - rx, y + h),
        `H ${num(x + rx)}`, arc(x, y + h - ry), `V ${num(y + ry)}`, arc(x + rx, y), 'Z',
      ].join(' ')
    }
  }
  throw new Error(`Unknown element ${tag}`)
}

const names = readdirSync(ICONS).filter((f) => f.endsWith('.svg')).map((f) => f.slice(0, -4)).sort()
const icons = names.map((name) => {
  const svg = readFileSync(join(ICONS, `${name}.svg`), 'utf8')
  if (!/viewBox="0 0 24 24"/.test(svg) || !/stroke-width="2"/.test(svg)) throw new Error(`design/icons/${name}.svg is not a Lucide icon`)
  return { name, elements: elementsOf(svg) }
})

const banner = 'Generated by design/build-icons.mjs from the Lucide icons in design/icons. Do not edit by hand.'
const files = {}

files[ANDROID] = [
  `// ${banner}`,
  'package ch.coachpoulpy.android.ui',
  '',
  'import androidx.compose.ui.graphics.Color',
  'import androidx.compose.ui.graphics.SolidColor',
  'import androidx.compose.ui.graphics.StrokeCap',
  'import androidx.compose.ui.graphics.StrokeJoin',
  'import androidx.compose.ui.graphics.vector.ImageVector',
  'import androidx.compose.ui.graphics.vector.PathParser',
  'import androidx.compose.ui.unit.dp',
  '',
  '/**',
  ` * The Lucide icons (ADR 0024), lucide-static ${version}, named as Lucide names them: \`chevron-right\` is [ChevronRight].`,
  ' * Each is drawn as Lucide draws it, in a 24 box with a 2 wide round stroke; [androidx.compose.material3.Icon] tints it.',
  ' * Add one with `node design/build-icons.mjs --add <name>`.',
  ' */',
  'object Lucide {',
  ...icons.map(({ name, elements }) =>
    `    val ${pascal(name)}: ImageVector by lazy { lucideIcon("${name}", ${elements.map((e) => JSON.stringify(pathOf(e))).join(', ')}) }`),
  '}',
  '',
  'private fun lucideIcon(name: String, vararg paths: String): ImageVector =',
  '    ImageVector.Builder(name = "lucide.$name", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f)',
  '        .apply {',
  '            paths.forEach {',
  '                addPath(',
  '                    PathParser().parsePathString(it).toNodes(),',
  '                    stroke = SolidColor(Color.Black),',
  '                    strokeLineWidth = 2f,',
  '                    strokeLineCap = StrokeCap.Round,',
  '                    strokeLineJoin = StrokeJoin.Round,',
  '                )',
  '            }',
  '        }',
  '        .build()',
  '',
].join('\n')

files[IOS_SWIFT] = [
  `// ${banner}`,
  '',
  'import SwiftUI',
  '',
  `/// The Lucide icons (ADR 0024), lucide-static ${version}, named as Lucide names them: \`chevron-right\` is \`.chevronRight\`.`,
  '/// Each is a template image in Assets.xcassets/Lucide, drawn in a 24 box with a 2 wide round stroke.',
  '/// Add one with `node design/build-icons.mjs --add <name>`.',
  'enum Lucide: String, CaseIterable {',
  ...icons.map(({ name }) => `    case ${swiftName(name)} = "Lucide${pascal(name)}"`),
  '',
  '    /// The icon at its own size, 24 points, tinted by the foreground style: for tab bars and menus.',
  '    var templateImage: Image { Image(decorative: rawValue).renderingMode(.template) }',
  '}',
  '',
  '/// A Lucide icon drawn `size` points square and tinted by the foreground style. It is decorative: the control',
  '/// it sits in carries the label.',
  'struct LucideIcon: View {',
  '    let icon: Lucide',
  '    var size: CGFloat = 24',
  '',
  '    init(_ icon: Lucide, size: CGFloat = 24) {',
  '        self.icon = icon',
  '        self.size = size',
  '    }',
  '',
  '    var body: some View {',
  '        icon.templateImage.resizable().scaledToFit().frame(width: size, height: size)',
  '    }',
  '}',
  '',
  'extension Label where Title == Text, Icon == Image {',
  '    /// A label with a Lucide icon, in place of `Label(_:systemImage:)`.',
  '    init(_ titleKey: LocalizedStringKey, lucide: Lucide) {',
  '        self.init { Text(titleKey) } icon: { lucide.templateImage }',
  '    }',
  '',
  '    /// A label with a Lucide icon, in place of `Label(_:systemImage:)`. A literal title is a key, as with SwiftUI.',
  '    @_disfavoredOverload',
  '    init<S: StringProtocol>(_ title: S, lucide: Lucide) {',
  '        self.init { Text(title) } icon: { lucide.templateImage }',
  '    }',
  '}',
  '',
].join('\n')

const json = (value) => JSON.stringify(value, null, 2) + '\n'
files[`${IOS_ASSETS}/Contents.json`] = json({ info: { author: 'xcode', version: 1 } })
for (const { name, elements } of icons) {
  const asset = `Lucide${pascal(name)}`
  // currentColor means nothing to the asset catalog: a template image only keeps the shape, so draw it in black.
  const body = elements.map(([tag, a]) => `  <${tag} ${Object.entries(a).map(([k, v]) => `${k}="${v}"`).join(' ')}/>`).join('\n')
  files[`${IOS_ASSETS}/${asset}.imageset/${asset}.svg`] = [
    `<!-- ${banner} Lucide ${name}, lucide-static ${version}, ISC licence. -->`,
    '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24">',
    '  <g fill="none" stroke="#000000" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">',
    body.replace(/^/gm, '  '),
    '  </g>',
    '</svg>',
    '',
  ].join('\n')
  files[`${IOS_ASSETS}/${asset}.imageset/Contents.json`] = json({
    images: [{ filename: `${asset}.svg`, idiom: 'universal' }],
    info: { author: 'xcode', version: 1 },
    properties: { 'preserves-vector-representation': true, 'template-rendering-intent': 'template' },
  })
}

// Image sets this script wrote for an icon that has since left design/icons.
const stale = existsSync(join(root, IOS_ASSETS))
  ? readdirSync(join(root, IOS_ASSETS)).filter((d) => d.endsWith('.imageset') && !files[`${IOS_ASSETS}/${d}/Contents.json`])
  : []

let problems = 0
for (const [path, content] of Object.entries(files)) {
  const full = join(root, path)
  if (check) {
    if (!existsSync(full) || readFileSync(full, 'utf8') !== content) { console.error(`Out of date: ${path}`); problems++ }
  } else {
    mkdirSync(dirname(full), { recursive: true })
    writeFileSync(full, content)
  }
}
for (const dir of stale) {
  if (check) { console.error(`No longer in design/icons: ${IOS_ASSETS}/${dir}`); problems++ }
  else rmSync(join(root, IOS_ASSETS, dir), { recursive: true })
}
if (problems) { console.error('Run `node design/build-icons.mjs` and commit the result.'); process.exit(1) }
console.log(check ? 'Lucide icons are up to date.' : `Wrote ${names.length} Lucide icons for Android and iOS.`)
