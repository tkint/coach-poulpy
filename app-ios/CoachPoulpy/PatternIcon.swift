import SwiftUI
import Shared

/// Stands in for the exercise picture until the catalog carries real images.
struct PatternBadge: View {
    let pattern: MovementPattern?
    var size: CGFloat = 40

    var body: some View {
        Image(systemName: symbol)
            .font(.system(size: size * 0.5))
            .foregroundStyle(PoulpyColor.onPrimaryContainer)
            .frame(width: size, height: size)
            .background(PoulpyColor.primaryContainer, in: Circle())
    }

    private var symbol: String {
        guard let pattern else { return "figure.mixed.cardio" }
        switch pattern {
        case .squat: return "figure.strengthtraining.functional"
        case .hinge: return "figure.strengthtraining.traditional"
        case .lunge: return "figure.step.training"
        case .push: return "dumbbell.fill"
        case .pull: return "figure.rower"
        case .carry: return "bag.fill"
        case .core: return "figure.core.training"
        case .rotation: return "arrow.triangle.2.circlepath"
        case .locomotion: return "figure.run"
        case .mobility: return "figure.mind.and.body"
        case .stretch: return "figure.flexibility"
        default: return "figure.mixed.cardio"
        }
    }
}
