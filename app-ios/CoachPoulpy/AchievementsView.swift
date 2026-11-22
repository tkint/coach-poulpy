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

/// A badge as a square tile, as drawn: an earned one shows its icon and name; a locked one is only a "?", never named.
struct BadgeTile: View {
    let badge: Badge?

    var body: some View {
        VStack(spacing: PoulpySpace.xs) {
            LucideIcon(badge?.lucide ?? .circleQuestionMark, size: 30)
                .foregroundStyle(badge == nil ? PoulpyColor.onSurfaceVariant : PoulpyColor.onPrimaryContainer)
            if let badge {
                Text(badge.titleKey)
                    .font(.custom(PoulpyFontFace.nunitoExtraBold, size: 13, relativeTo: .caption))
                    .foregroundStyle(PoulpyColor.onPrimaryContainer)
                    .multilineTextAlignment(.center)
            }
        }
        .padding(PoulpySpace.xs)
        .frame(maxWidth: .infinity)
        .aspectRatio(1, contentMode: .fit)
        .background(badge == nil ? PoulpyColor.surfaceVariant : PoulpyColor.primaryContainer, in: RoundedRectangle(cornerRadius: PoulpyRadius.lg))
        .accessibilityElement(children: .ignore)
        .accessibilityLabel(badge.map { Text($0.titleKey) } ?? Text("achievements.locked"))
    }
}

/// Achievements, opened from the motivation row on Home (`achievements` mockup): Poulpy's tentacles, then the badges.
struct AchievementsView: View {
    @StateObject private var achievements: Observed<AchievementsModel, AchievementsState>

    init(graph: AppGraph) {
        _achievements = StateObject(wrappedValue: graph.observedAchievements())
    }

    var body: some View {
        let state = achievements.value
        // Earned badges first, then the locked ones as "?", as drawn.
        let tiles: [Badge?] = state.achievements.earnedInOrder.map { Optional($0) } + state.achievements.locked.map { _ in nil }
        ScrollView {
            VStack(spacing: PoulpySpace.md) {
                if let poulpy = state.poulpy {
                    // Poulpy stays big and plays his move when tapped, as on Home. The drawing has room of its own, so the block
                    // is pulled up as the mockup's negative margin does.
                    VStack(spacing: PoulpySpace.xs) {
                        PoulpyTapArt(drawing: poulpy.drawing, description: poulpy.title, art: 200)
                        Text(verbatim: poulpy.litCaption)
                            .font(PoulpyFont.headline)
                        Text(verbatim: state.sessionsToFull == 0 ? L("achievements.champion") : L("achievements.toGo %lld", Int64(state.sessionsToFull)))
                            .font(.custom(PoulpyFontFace.nunitoMedium, size: 15, relativeTo: .subheadline))
                            .foregroundStyle(PoulpyColor.onSurfaceVariant)
                            .multilineTextAlignment(.center)
                        HStack(spacing: 6) {
                            ForEach(0..<8, id: \.self) { index in
                                Circle()
                                    .fill(index < Int(poulpy.tentaclesLit) ? PoulpyColor.primary : PoulpyColor.onPrimaryContainer.opacity(0.18))
                                    .frame(width: 14, height: 14)
                            }
                        }
                        .padding(.top, PoulpySpace.sm)
                        .accessibilityHidden(true)
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.bottom, PoulpySpace.sm)
                    .padding(.top, -18)
                }
                Text("achievements.badges")
                    .textCase(.uppercase)
                    .font(PoulpyFont.label)
                    .kerning(1)
                    .foregroundStyle(PoulpyColor.onSurfaceVariant)
                    .padding(.leading, PoulpySpace.xs)
                    .padding(.top, 6)
                    .frame(maxWidth: .infinity, alignment: .leading)
                LazyVGrid(columns: Array(repeating: GridItem(.flexible(), spacing: PoulpySpace.sm), count: 3), spacing: PoulpySpace.sm) {
                    ForEach(Array(tiles.enumerated()), id: \.offset) { _, badge in
                        BadgeTile(badge: badge)
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
