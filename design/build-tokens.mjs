// Generates the Android, iOS and web theme files from design/tokens.json.
//   node design/build-tokens.mjs          write the generated files
//   node design/build-tokens.mjs --check  fail if a generated file is out of date (used by CI)
import { readFileSync, writeFileSync, mkdirSync, existsSync } from 'node:fs'
import { dirname, join } from 'node:path'
import { fileURLToPath } from 'node:url'

const root = join(dirname(fileURLToPath(import.meta.url)), '..')
const tokens = JSON.parse(readFileSync(join(root, 'design/tokens.json'), 'utf8'))
const banner = 'Generated from design/tokens.json by design/build-tokens.mjs. Do not edit by hand.'

const hex = (value) => value.replace('#', '').toUpperCase()
const pascal = (name) => name.charAt(0).toUpperCase() + name.slice(1)
const kotlinColor = (value) => `Color(0xFF${hex(value)})`
const swiftHex = (value) => `0x${hex(value)}`
const kebab = (name) => name.replace(/[A-Z]/g, (c) => '-' + c.toLowerCase())
const num = (n) => (Number.isInteger(n) ? String(n) : String(n))

const { light, dark } = tokens.theme

// Android (Compose)
function kotlin() {
  const out = [`// ${banner}`, 'package ch.coachpoulpy.android.ui', '']
  out.push(
    'import androidx.compose.animation.core.CubicBezierEasing',
    'import androidx.compose.foundation.shape.RoundedCornerShape',
    'import androidx.compose.material3.Shapes',
    'import androidx.compose.material3.Typography',
    'import androidx.compose.material3.darkColorScheme',
    'import androidx.compose.material3.lightColorScheme',
    'import androidx.compose.ui.graphics.Color',
    'import androidx.compose.ui.text.TextStyle',
    'import androidx.compose.ui.text.font.FontWeight',
    'import androidx.compose.ui.unit.dp',
    'import androidx.compose.ui.unit.em',
    'import androidx.compose.ui.unit.sp',
    '',
  )
  out.push('/** Fixed palette. The poulpy colours are the mascot itself and never change with the theme. */')
  out.push('object PoulpyPalette {')
  for (const [group, shades] of Object.entries(tokens.color)) {
    for (const [shade, value] of Object.entries(shades)) {
      out.push(`    val ${pascal(group)}${pascal(shade)} = ${kotlinColor(value)}`)
    }
  }
  out.push('}', '')
  out.push("/** Poulpy's mood shades, from the blue side to the ember side. */")
  out.push('enum class PoulpyMood(val tone: Color) {')
  for (const [mood, value] of Object.entries(tokens.mood)) out.push(`    ${pascal(mood)}(${kotlinColor(value)}),`)
  out.push('}', '')
  const scheme = (t) => [
    `    primary = ${kotlinColor(t.primary)}, onPrimary = ${kotlinColor(t.onPrimary)},`,
    `    primaryContainer = ${kotlinColor(t.primaryContainer)}, onPrimaryContainer = ${kotlinColor(t.onPrimaryContainer)},`,
    `    secondary = ${kotlinColor(t.primary)}, onSecondary = ${kotlinColor(t.onPrimary)},`,
    `    tertiary = ${kotlinColor(t.grumpy)}, onTertiary = ${kotlinColor(t.onGrumpy)},`,
    `    tertiaryContainer = ${kotlinColor(t.grumpyContainer)}, onTertiaryContainer = ${kotlinColor(t.onGrumpyContainer)},`,
    `    background = ${kotlinColor(t.background)}, onBackground = ${kotlinColor(t.onBackground)},`,
    `    surface = ${kotlinColor(t.surface)}, onSurface = ${kotlinColor(t.onSurface)},`,
    `    surfaceVariant = ${kotlinColor(t.surfaceVariant)}, onSurfaceVariant = ${kotlinColor(t.onSurfaceVariant)},`,
    `    outline = ${kotlinColor(t.outline)}, outlineVariant = ${kotlinColor(t.outline)},`,
    `    error = ${kotlinColor(t.error)}, onError = ${kotlinColor(t.onError)},`,
  ]
  out.push('internal val PoulpyLightColors = lightColorScheme(', ...scheme(light), ')', '')
  out.push('internal val PoulpyDarkColors = darkColorScheme(', ...scheme(dark), ')', '')
  out.push('/** Brand colours that Material 3 has no slot for. */')
  out.push('data class PoulpyExtraColors(')
  out.push('    val grumpy: Color,', '    val onGrumpy: Color,', '    val grumpyContainer: Color,', '    val onGrumpyContainer: Color,', '    val success: Color,', ')', '')
  const extras = (t) => `PoulpyExtraColors(${kotlinColor(t.grumpy)}, ${kotlinColor(t.onGrumpy)}, ${kotlinColor(t.grumpyContainer)}, ${kotlinColor(t.onGrumpyContainer)}, ${kotlinColor(t.success)})`
  out.push(`internal val PoulpyLightExtras = ${extras(light)}`)
  out.push(`internal val PoulpyDarkExtras = ${extras(dark)}`, '')
  const style = (t) => {
    const parts = [`fontWeight = FontWeight.W${t.weight}`, `fontSize = ${num(t.size)}.sp`, `lineHeight = ${num(t.lineHeight)}.sp`]
    if (t.tracking) parts.push(`letterSpacing = ${t.tracking}.em`)
    if (t.tabular) parts.push('fontFeatureSettings = "tnum"')
    return `TextStyle(${parts.join(', ')})`
  }
  out.push('/** Type scale. Baloo 2 and Nunito are not bundled yet, so the system font is used meanwhile. */')
  out.push('object PoulpyTextStyles {')
  for (const [name, t] of Object.entries(tokens.type)) out.push(`    val ${pascal(name)} = ${style(t)}`)
  out.push('}', '')
  out.push('internal val PoulpyTypography = Typography(')
  out.push('    headlineLarge = PoulpyTextStyles.Display,', '    headlineMedium = PoulpyTextStyles.Title,', '    titleLarge = PoulpyTextStyles.Headline,')
  out.push('    bodyLarge = PoulpyTextStyles.Body,', '    labelMedium = PoulpyTextStyles.Label,', ')', '')
  out.push('object PoulpySpace {')
  for (const [name, v] of Object.entries(tokens.space)) out.push(`    val ${name} = ${v}.dp`)
  out.push('}', '')
  out.push('object PoulpyRadius {')
  for (const [name, v] of Object.entries(tokens.radius)) out.push(`    val ${name} = ${v}.dp`)
  out.push('}', '')
  const r = tokens.radius
  out.push(`internal val PoulpyShapes = Shapes(small = RoundedCornerShape(${r.sm}.dp), medium = RoundedCornerShape(${r.md}.dp), large = RoundedCornerShape(${r.lg}.dp))`, '')
  out.push('object PoulpyMotion {')
  for (const [name, m] of Object.entries(tokens.motion)) {
    out.push(`    const val ${name}Millis = ${m.duration}`)
    if (m.easing) out.push(`    val ${name}Easing = CubicBezierEasing(${m.easing.map((e) => e + 'f').join(', ')})`)
  }
  out.push('}', '')
  return out.join('\n')
}

// iOS (SwiftUI)
function swift() {
  const out = [`// ${banner}`, '', 'import SwiftUI', 'import UIKit', '']
  out.push('/// Fixed palette. The poulpy colours are the mascot itself and never change with the appearance.')
  out.push('enum PoulpyPalette {')
  for (const [group, shades] of Object.entries(tokens.color)) {
    for (const [shade, value] of Object.entries(shades)) out.push(`    static let ${group}${pascal(shade)} = Color(hex: ${swiftHex(value)})`)
  }
  out.push('}', '')
  out.push('/// Theme colours that follow light and dark appearance.')
  out.push('enum PoulpyColor {')
  for (const name of Object.keys(light)) out.push(`    static let ${name} = Color(light: ${swiftHex(light[name])}, dark: ${swiftHex(dark[name])})`)
  out.push('}', '')
  out.push("/// Poulpy's mood shades, from the blue side to the ember side.")
  out.push('enum PoulpyMood: String, CaseIterable {')
  out.push(`    case ${Object.keys(tokens.mood).join(', ')}`, '', '    var tone: Color {', '        switch self {')
  for (const [mood, value] of Object.entries(tokens.mood)) out.push(`        case .${mood}: return Color(hex: ${swiftHex(value)})`)
  out.push('        }', '    }', '}', '')
  const weight = { 400: 'regular', 500: 'medium', 600: 'semibold', 700: 'bold', 800: 'heavy', 900: 'black' }
  out.push('/// Type scale, scaled with Dynamic Type. Uses the rounded system font until Baloo 2 and Nunito are bundled.')
  out.push('enum PoulpyFont {')
  for (const [name, t] of Object.entries(tokens.type)) {
    const base = `.system(size: UIFontMetrics(forTextStyle: .${t.iosStyle}).scaledValue(for: ${num(t.size)}), weight: .${weight[t.weight]}, design: .rounded)`
    out.push(`    static var ${name}: Font { Font${base}${t.tabular ? '.monospacedDigit()' : ''} }`)
  }
  out.push('}', '')
  out.push('enum PoulpySpace {')
  for (const [name, v] of Object.entries(tokens.space)) out.push(`    static let ${name}: CGFloat = ${v}`)
  out.push('}', '')
  out.push('enum PoulpyRadius {')
  for (const [name, v] of Object.entries(tokens.radius)) out.push(`    static let ${name}: CGFloat = ${v}`)
  out.push('}', '')
  out.push('enum PoulpyMotion {')
  for (const [name, m] of Object.entries(tokens.motion)) {
    out.push(`    static let ${name}Duration: Double = ${m.duration / 1000}`)
    if (m.easing) out.push(`    static let ${name}Animation = Animation.timingCurve(${m.easing.join(', ')}, duration: ${m.duration / 1000})`)
  }
  out.push('}', '')
  out.push('extension Color {')
  out.push('    init(hex: UInt32) {')
  out.push('        self.init(UIColor(hex: hex))')
  out.push('    }', '')
  out.push('    init(light: UInt32, dark: UInt32) {')
  out.push('        self.init(UIColor { $0.userInterfaceStyle == .dark ? UIColor(hex: dark) : UIColor(hex: light) })')
  out.push('    }', '}', '')
  out.push('extension UIColor {')
  out.push('    convenience init(hex: UInt32) {')
  out.push('        self.init(')
  out.push('            red: CGFloat((hex >> 16) & 0xFF) / 255,')
  out.push('            green: CGFloat((hex >> 8) & 0xFF) / 255,')
  out.push('            blue: CGFloat(hex & 0xFF) / 255,')
  out.push('            alpha: 1')
  out.push('        )', '    }', '}', '')
  return out.join('\n')
}

// iOS accent colour asset, so system controls pick up the brand blue
function accentColor() {
  const comp = (value) => {
    const h = hex(value)
    const c = (i) => (parseInt(h.slice(i, i + 2), 16) / 255).toFixed(3)
    return { 'color-space': 'srgb', components: { alpha: '1.000', blue: c(4), green: c(2), red: c(0) } }
  }
  return JSON.stringify({
    colors: [
      { color: comp(light.primary), idiom: 'universal' },
      { appearances: [{ appearance: 'luminosity', value: 'dark' }], color: comp(dark.primary), idiom: 'universal' },
    ],
    info: { author: 'xcode', version: 1 },
  }, null, 2) + '\n'
}

// Web (CSS custom properties)
function css() {
  const out = [`/* ${banner} */`, '', ':root {']
  for (const [group, shades] of Object.entries(tokens.color)) {
    for (const [shade, value] of Object.entries(shades)) out.push(`  --cp-${group}-${shade}: ${value};`)
  }
  for (const [mood, value] of Object.entries(tokens.mood)) out.push(`  --cp-mood-${mood}: ${value};`)
  for (const [name, value] of Object.entries(light)) out.push(`  --cp-${kebab(name)}: ${value};`)
  for (const [name, f] of Object.entries(tokens.font)) out.push(`  --cp-font-${name}: "${f.family}", ${f.fallback};`)
  for (const [name, t] of Object.entries(tokens.type)) {
    out.push(`  --cp-type-${name}: ${t.weight} ${num(t.size)}px/${num(t.lineHeight)}px var(--cp-font-${t.font});`)
  }
  for (const [name, v] of Object.entries(tokens.space)) out.push(`  --cp-space-${name}: ${v}px;`)
  for (const [name, v] of Object.entries(tokens.radius)) out.push(`  --cp-radius-${name}: ${v}px;`)
  for (const [name, m] of Object.entries(tokens.motion)) {
    out.push(`  --cp-motion-${name}: ${m.duration}ms${m.easing ? ` cubic-bezier(${m.easing.join(', ')})` : ''};`)
  }
  out.push('  color-scheme: light dark;', '}', '', '@media (prefers-color-scheme: dark) {', '  :root {')
  for (const [name, value] of Object.entries(dark)) out.push(`    --cp-${kebab(name)}: ${value};`)
  out.push('  }', '}', '')
  return out.join('\n')
}

const files = {
  'app-android/src/main/kotlin/ch/coachpoulpy/android/ui/PoulpyTokens.kt': kotlin(),
  'app-ios/CoachPoulpy/Theme/PoulpyTokens.swift': swift(),
  'app-ios/CoachPoulpy/Assets.xcassets/AccentColor.colorset/Contents.json': accentColor(),
  'web/src/styles/tokens.css': css(),
}

const check = process.argv.includes('--check')
let stale = 0
for (const [path, content] of Object.entries(files)) {
  const full = join(root, path)
  const current = existsSync(full) ? readFileSync(full, 'utf8') : null
  if (current === content) continue
  if (check) {
    console.error(`Out of date: ${path}`)
    stale++
  } else {
    mkdirSync(dirname(full), { recursive: true })
    writeFileSync(full, content)
    console.log(`Wrote ${path}`)
  }
}
if (stale) {
  console.error('Run `node design/build-tokens.mjs` and commit the result.')
  process.exit(1)
}
