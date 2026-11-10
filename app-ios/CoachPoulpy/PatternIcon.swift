import SwiftUI
import Shared

/// Stands in for the exercise picture until the catalog carries real images.
struct PatternBadge: View {
    let pattern: MovementPattern?
    var size: CGFloat = 40

    var body: some View {
        LucideIcon(symbol, size: size * 0.6)
            .foregroundStyle(PoulpyColor.onPrimaryContainer)
            .frame(width: size, height: size)
            .background(PoulpyColor.primaryContainer, in: Circle())
    }

    private var symbol: Lucide {
        guard let pattern else { return .dumbbell }
        switch pattern {
        case .squat: return .personStanding
        case .hinge: return .handGrab
        case .lunge: return .footprints
        case .push: return .dumbbell
        case .pull: return .bicepsFlexed
        case .carry: return .luggage
        case .core: return .shield
        case .rotation: return .rotateCw
        case .locomotion: return .sportShoe
        case .mobility: return .accessibility
        case .stretch: return .stretchHorizontal
        default: return .dumbbell
        }
    }
}
