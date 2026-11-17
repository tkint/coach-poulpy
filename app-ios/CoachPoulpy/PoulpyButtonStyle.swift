import SwiftUI

/// Primary is one per screen, secondary (outlined) and tonal are the others, grumpy only the catch-up action.
enum PoulpyButtonKind {
    case primary, secondary, tonal, grumpy
}

/// The size scale of the buttons, shared with the websites and drawn by the `Button` mockup (`PoulpyControl`, from design/tokens.json).
enum PoulpyButtonSize {
    case xs, sm, md, lg, xl, xxl

    var height: CGFloat {
        switch self {
        case .xs: return PoulpyControl.xs
        case .sm: return PoulpyControl.sm
        case .md: return PoulpyControl.md
        case .lg: return PoulpyControl.lg
        case .xl: return PoulpyControl.xl
        case .xxl: return PoulpyControl.xxl
        }
    }

    var horizontal: CGFloat {
        switch self {
        case .xs: return PoulpySpace.sm
        case .sm: return PoulpySpace.md
        default: return PoulpySpace.lg
        }
    }

    var fontSize: CGFloat {
        switch self {
        case .xs: return 12
        case .sm: return 14
        case .md: return 15
        case .lg: return 16
        case .xl: return 17
        case .xxl: return 18
        }
    }
}

/// The apps' one button: a rounded square (`PoulpyRadius.md`), never a pill, in a size of the scale (`.md` unless told
/// otherwise). Mirrors `PoulpyButton` on Android and the `Button` mockup. The label is only content: text, or an icon and text.
struct PoulpyButtonStyle: ButtonStyle {
    @Environment(\.isEnabled) private var isEnabled
    let kind: PoulpyButtonKind
    let size: PoulpyButtonSize

    init(_ kind: PoulpyButtonKind = .primary, size: PoulpyButtonSize = .md) {
        self.kind = kind
        self.size = size
    }

    func makeBody(configuration: Configuration) -> some View {
        let shape = RoundedRectangle(cornerRadius: PoulpyRadius.md)
        configuration.label
            .font(.custom(PoulpyFontFace.nunitoBold, size: size.fontSize, relativeTo: .headline).weight(.heavy))
            .foregroundStyle(foreground)
            .padding(.horizontal, size.horizontal)
            .frame(height: size.height)
            .background(background, in: shape)
            .overlay(kind == .secondary && isEnabled ? shape.stroke(PoulpyColor.outline, lineWidth: 2) : nil)
            .opacity(configuration.isPressed ? 0.85 : 1)
            // Below 44 the touch area still reaches 44.
            .frame(minHeight: PoulpySize.touch)
            .contentShape(Rectangle())
    }

    private var background: Color {
        guard isEnabled else { return PoulpyColor.surfaceVariant }
        switch kind {
        case .primary: return PoulpyColor.primary
        case .secondary: return Color.clear
        case .tonal: return PoulpyColor.primaryContainer
        case .grumpy: return PoulpyColor.grumpy
        }
    }

    private var foreground: Color {
        guard isEnabled else { return PoulpyColor.onSurfaceVariant }
        switch kind {
        case .primary: return PoulpyColor.onPrimary
        case .secondary: return PoulpyColor.onSurface
        case .tonal: return PoulpyColor.onPrimaryContainer
        case .grumpy: return PoulpyColor.onGrumpy
        }
    }
}
