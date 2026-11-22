import SwiftUI
import Shared

private func L(_ key: String, _ args: CVarArg...) -> String {
    String(format: NSLocalizedString(key, comment: ""), locale: Locale.current, arguments: args)
}

extension Badge {
    var titleKey: LocalizedStringKey {
        switch self {
        case .firstRecord: return "achievements.firstRecord"
        case .goalMet: return "achievements.goalMet"
        default: return "achievements.firstWalk"
        }
    }

    var lucide: Lucide {
        switch self {
        case .firstRecord: return .award
        case .goalMet: return .target
        default: return .footprints
        }
    }
}

/// A badge as a square tile: an earned one shows its icon and name; a locked one is only a "?", never named.
struct BadgeTile: View {
    let badge: Badge?

    var body: some View {
        VStack(spacing: 6) {
            LucideIcon(badge?.lucide ?? .circleQuestionMark, size: 28)
                .foregroundStyle(badge == nil ? PoulpyColor.onSurfaceVariant : PoulpyColor.onPrimaryContainer)
            if let badge {
                Text(badge.titleKey)
                    .font(PoulpyFont.label)
                    .multilineTextAlignment(.center)
            }
        }
        .padding(PoulpySpace.sm)
        .frame(maxWidth: .infinity)
        .aspectRatio(1, contentMode: .fit)
        .background(badge == nil ? PoulpyColor.surface : PoulpyColor.primaryContainer, in: RoundedRectangle(cornerRadius: PoulpyRadius.lg))
        .overlay(RoundedRectangle(cornerRadius: PoulpyRadius.lg).stroke(badge == nil ? PoulpyColor.outline : PoulpyColor.primary))
        .accessibilityElement(children: .ignore)
        .accessibilityLabel(badge.map { Text($0.titleKey) } ?? Text("achievements.locked"))
    }
}

/// Achievements, opened from the motivation row on Home: Poulpy's tentacles, then the badges.
struct AchievementsView: View {
    @StateObject private var achievements: Observed<AchievementsModel, AchievementsState>

    init(graph: AppGraph) {
        _achievements = StateObject(wrappedValue: graph.observedAchievements())
    }

    var body: some View {
        let state = achievements.value
        let earned = state.achievements.earned
        ScrollView {
            VStack(spacing: PoulpySpace.md) {
                if let poulpy = state.poulpy {
                    // Poulpy stays big and plays his move when tapped, as on Home; the spacing around him is tight.
                    VStack(spacing: 2) {
                        PoulpyTapArt(drawing: poulpy.drawing, description: poulpy.title, art: 200)
                        Text(verbatim: poulpy.litCaption)
                            .font(PoulpyFont.headline)
                        Text(verbatim: state.sessionsToFull == 0 ? L("achievements.champion") : L("achievements.toGo %lld", Int64(state.sessionsToFull)))
                            .font(PoulpyFont.body)
                            .foregroundStyle(PoulpyColor.onSurfaceVariant)
                            .multilineTextAlignment(.center)
                        HStack(spacing: 6) {
                            ForEach(0..<8, id: \.self) { index in
                                Circle()
                                    .fill(index < Int(poulpy.tentaclesLit) ? PoulpyColor.primary : PoulpyColor.onPrimaryContainer.opacity(0.18))
                                    .frame(width: 10, height: 10)
                            }
                        }
                        .accessibilityHidden(true)
                    }
                    .frame(maxWidth: .infinity)
                }
                Text("achievements.badges")
                    .font(PoulpyFont.headline)
                    .frame(maxWidth: .infinity, alignment: .leading)
                let tiles = Array(earned.reversed()) + state.achievements.locked
                LazyVGrid(columns: Array(repeating: GridItem(.flexible(), spacing: 10), count: 3), spacing: 10) {
                    ForEach(Array(tiles.enumerated()), id: \.offset) { _, badge in
                        BadgeTile(badge: earned.contains(badge) ? badge : nil)
                    }
                }
            }
            .padding(.horizontal, PoulpySpace.lg)
            .padding(.bottom, PoulpySpace.lg)
        }
        .navigationTitle("achievements.title")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar(.hidden, for: .tabBar)
    }
}
