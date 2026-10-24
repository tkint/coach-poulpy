// Generated from design/tokens.json by design/build-tokens.mjs. Do not edit by hand.

import SwiftUI
import UIKit

/// Fixed palette. The poulpy colours are the mascot itself and never change with the appearance.
enum PoulpyPalette {
    static let poulpyBlue = Color(hex: 0x3AA6E0)
    static let poulpyEmber = Color(hex: 0xD9622B)
    static let poulpyInk = Color(hex: 0x141A26)
    static let lagoon50 = Color(hex: 0xEAF6FD)
    static let lagoon100 = Color(hex: 0xCDEBFA)
    static let lagoon300 = Color(hex: 0x7FCBF0)
    static let lagoon400 = Color(hex: 0x3AA6E0)
    static let lagoon600 = Color(hex: 0x1574B0)
    static let lagoon800 = Color(hex: 0x0C4A73)
    static let lagoon900 = Color(hex: 0x082F4A)
    static let ember50 = Color(hex: 0xFFF1E8)
    static let ember100 = Color(hex: 0xFFDCC7)
    static let ember300 = Color(hex: 0xF4A473)
    static let ember400 = Color(hex: 0xE8873F)
    static let ember500 = Color(hex: 0xD9622B)
    static let ember600 = Color(hex: 0xB4471C)
    static let ember800 = Color(hex: 0x7A2C12)
    static let neutral0 = Color(hex: 0xFFFFFF)
    static let neutral50 = Color(hex: 0xF4F9FC)
    static let neutral100 = Color(hex: 0xE6EFF5)
    static let neutral300 = Color(hex: 0xB6C4D0)
    static let neutral500 = Color(hex: 0x6B7C8C)
    static let neutral700 = Color(hex: 0x374555)
    static let neutral900 = Color(hex: 0x141A26)
}

/// Theme colours that follow light and dark appearance.
enum PoulpyColor {
    static let primary = Color(light: 0x1574B0, dark: 0x5BBEF0)
    static let onPrimary = Color(light: 0xFFFFFF, dark: 0x06202F)
    static let primaryContainer = Color(light: 0xDDF0FB, dark: 0x123A52)
    static let onPrimaryContainer = Color(light: 0x082F4A, dark: 0xCDEBFA)
    static let grumpy = Color(light: 0xB4471C, dark: 0xFF9A66)
    static let onGrumpy = Color(light: 0xFFFFFF, dark: 0x2A1006)
    static let grumpyContainer = Color(light: 0xFFE6D6, dark: 0x3A1E12)
    static let onGrumpyContainer = Color(light: 0x4A1A0B, dark: 0xFFDCC7)
    static let background = Color(light: 0xF4F9FC, dark: 0x0A1520)
    static let onBackground = Color(light: 0x141A26, dark: 0xE8F1F7)
    static let surface = Color(light: 0xFFFFFF, dark: 0x112230)
    static let onSurface = Color(light: 0x141A26, dark: 0xE8F1F7)
    static let surfaceVariant = Color(light: 0xE6EFF5, dark: 0x17303F)
    static let onSurfaceVariant = Color(light: 0x56687A, dark: 0x95AABB)
    static let outline = Color(light: 0xD3E0E9, dark: 0x21394A)
    static let success = Color(light: 0x2E9E6A, dark: 0x4CC38A)
    static let error = Color(light: 0xC8264A, dark: 0xFF6B85)
    static let onError = Color(light: 0xFFFFFF, dark: 0x2A0610)
}

/// Poulpy's mood shades, from the blue side to the ember side.
enum PoulpyMood: String, CaseIterable {
    case proud, happy, recovering, annoyed, grumpy, fuming

    var tone: Color {
        switch self {
        case .proud: return Color(hex: 0x2C9CE0)
        case .happy: return Color(hex: 0x3AA6E0)
        case .recovering: return Color(hex: 0x5DB4E6)
        case .annoyed: return Color(hex: 0xE8873F)
        case .grumpy: return Color(hex: 0xD9622B)
        case .fuming: return Color(hex: 0xC0352B)
        }
    }
}

/// Type scale, scaled with Dynamic Type. Uses the rounded system font until Baloo 2 and Nunito are bundled.
enum PoulpyFont {
    static var display: Font { Font.system(size: UIFontMetrics(forTextStyle: .largeTitle).scaledValue(for: 40), weight: .heavy, design: .rounded) }
    static var title: Font { Font.system(size: UIFontMetrics(forTextStyle: .title).scaledValue(for: 28), weight: .bold, design: .rounded) }
    static var headline: Font { Font.system(size: UIFontMetrics(forTextStyle: .title3).scaledValue(for: 21), weight: .bold, design: .rounded) }
    static var body: Font { Font.system(size: UIFontMetrics(forTextStyle: .body).scaledValue(for: 16), weight: .medium, design: .rounded) }
    static var label: Font { Font.system(size: UIFontMetrics(forTextStyle: .caption).scaledValue(for: 12.5), weight: .heavy, design: .rounded) }
    static var timer: Font { Font.system(size: UIFontMetrics(forTextStyle: .largeTitle).scaledValue(for: 64), weight: .heavy, design: .rounded).monospacedDigit() }
}

enum PoulpySpace {
    static let xs: CGFloat = 4
    static let sm: CGFloat = 8
    static let md: CGFloat = 12
    static let lg: CGFloat = 16
    static let xl: CGFloat = 24
    static let xxl: CGFloat = 32
    static let xxxl: CGFloat = 48
}

enum PoulpyRadius {
    static let sm: CGFloat = 10
    static let md: CGFloat = 16
    static let lg: CGFloat = 24
    static let pill: CGFloat = 999
}

enum PoulpyMotion {
    static let flipDuration: Double = 0.7
    static let flipAnimation = Animation.timingCurve(0.5, -0.35, 0.35, 1.4, duration: 0.7)
    static let moodDuration: Double = 0.3
    static let quickDuration: Double = 0.2
}

extension Color {
    init(hex: UInt32) {
        self.init(UIColor(hex: hex))
    }

    init(light: UInt32, dark: UInt32) {
        self.init(UIColor { $0.userInterfaceStyle == .dark ? UIColor(hex: dark) : UIColor(hex: light) })
    }
}

extension UIColor {
    convenience init(hex: UInt32) {
        self.init(
            red: CGFloat((hex >> 16) & 0xFF) / 255,
            green: CGFloat((hex >> 8) & 0xFF) / 255,
            blue: CGFloat(hex & 0xFF) / 255,
            alpha: 1
        )
    }
}
