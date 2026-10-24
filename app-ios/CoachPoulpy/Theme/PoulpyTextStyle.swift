import SwiftUI
import UIKit

extension Font {
    /// The system text styles in the brand fonts, at Apple's default sizes and scaled with Dynamic Type:
    /// Baloo 2 for titles, Nunito for everything else. Use it where a view would use `.font(.headline)`.
    static func poulpy(_ style: Font.TextStyle) -> Font {
        switch style {
        case .largeTitle: return .custom(PoulpyFontFace.baloo2ExtraBold, size: 34, relativeTo: style)
        case .title: return .custom(PoulpyFontFace.baloo2Bold, size: 28, relativeTo: style)
        case .title2: return .custom(PoulpyFontFace.baloo2Bold, size: 22, relativeTo: style)
        case .title3: return .custom(PoulpyFontFace.baloo2Bold, size: 20, relativeTo: style)
        case .headline: return .custom(PoulpyFontFace.nunitoBold, size: 17, relativeTo: style)
        case .callout: return .custom(PoulpyFontFace.nunitoMedium, size: 16, relativeTo: style)
        case .subheadline: return .custom(PoulpyFontFace.nunitoMedium, size: 15, relativeTo: style)
        case .footnote: return .custom(PoulpyFontFace.nunitoMedium, size: 13, relativeTo: style)
        case .caption: return .custom(PoulpyFontFace.nunitoMedium, size: 12, relativeTo: style)
        case .caption2: return .custom(PoulpyFontFace.nunitoMedium, size: 11, relativeTo: style)
        default: return .custom(PoulpyFontFace.nunitoMedium, size: 17, relativeTo: .body)
        }
    }
}

enum PoulpyAppearance {
    /// Navigation bar and tab bar titles are UIKit, so they get the brand fonts through the appearance proxies.
    static func apply() {
        let nav = UINavigationBar.appearance()
        if let large = UIFont(name: PoulpyFontFace.baloo2ExtraBold, size: 34) {
            nav.largeTitleTextAttributes = [.font: UIFontMetrics(forTextStyle: .largeTitle).scaledFont(for: large)]
        }
        if let inline = UIFont(name: PoulpyFontFace.baloo2Bold, size: 18) {
            nav.titleTextAttributes = [.font: UIFontMetrics(forTextStyle: .headline).scaledFont(for: inline)]
        }
        if let tab = UIFont(name: PoulpyFontFace.nunitoBold, size: 10) {
            UITabBarItem.appearance().setTitleTextAttributes([.font: tab], for: .normal)
        }
    }
}
