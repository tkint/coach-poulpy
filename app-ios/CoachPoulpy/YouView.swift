import SwiftUI
import Shared

/// You: the profile card with its badge, then Statistics for the chosen period. The cog opens Settings, the card opens the
/// profile and Edit chooses and orders the blocks. The other blocks (records, body map, focus split, cardio, volume and
/// load, time of day) wait for their own issues.
struct YouView: View {
    @ObservedObject var you: Observed<YouModel, YouState>
    @ObservedObject var account: Observed<AccountModel, AccountState>
    @ObservedObject var profile: Observed<ProfileModel, ProfileUi>
    @Environment(\.unitSystem) private var units
    @State private var editing = false

    var body: some View {
        let state = you.value
        ScrollView {
            VStack(alignment: .leading, spacing: PoulpySpace.md) {
                profileCard
                HStack {
                    Text("you.statistics")
                        .textCase(.uppercase)
                        .font(PoulpyFont.label)
                        .kerning(1)
                        .foregroundStyle(PoulpyColor.onSurfaceVariant)
                        .padding(.horizontal, 4)
                    Spacer()
                    Button { editing = true } label: {
                        Label("action.edit", lucide: .pencil)
                    }
                    .buttonStyle(PoulpyButtonStyle(.tonal, size: .sm))
                }
                .padding(.top, PoulpySpace.xs)
                periodTabs(state.period)
                if state.loaded { cards(state) }
            }
            .padding(.horizontal, PoulpySpace.lg)
            .padding(.vertical, PoulpySpace.sm)
        }
        .background(PoulpyColor.background)
        .navigationTitle("tab.you")
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                NavigationLink(value: SettingsPage.settings) { LucideIcon(.settings, size: 22) }
                    .accessibilityLabel(Text("tab.settings"))
            }
        }
        .fullScreenCover(isPresented: $editing) {
            EditStatisticsView(you: you) { editing = false }
        }
    }

    // MARK: Profile card

    private var profileCard: some View {
        let state = profile.value
        let summary = [
            state.goals.isEmpty ? nil : state.goals.map(goalLabel).joined(separator: ", "),
            state.experience.map(experienceLabel),
        ].compactMap { $0 }.joined(separator: " · ")
        return NavigationLink(value: SettingsPage.profile) {
            HStack(spacing: 14) {
                Image("PoulpyMoodHappy").resizable().scaledToFit().frame(width: 64, height: 64).accessibilityHidden(true)
                VStack(alignment: .leading, spacing: 2) {
                    if state.displayName.isEmpty {
                        Text("settings.yourProfile").font(.poulpy(.title3)).lineLimit(1)
                    } else {
                        Text(state.displayName).font(.poulpy(.title3)).lineLimit(1)
                    }
                    if !summary.isEmpty { Text(summary).lineLimit(1) }
                    badge.padding(.top, 4)
                }
                Spacer(minLength: 0)
                LucideIcon(.chevronRight, size: 22)
            }
            .foregroundStyle(PoulpyColor.onPrimaryContainer)
            .padding(PoulpySpace.lg)
            .background(PoulpyColor.primaryContainer, in: RoundedRectangle(cornerRadius: PoulpyRadius.lg))
        }
        .buttonStyle(.plain)
    }

    /// Guest, Confirm your email or Signed in. A label, not a button.
    private var badge: some View {
        let signedIn = account.value is AccountStateSignedIn
        return HStack(spacing: 2) {
            if signedIn { LucideIcon(.check, size: 14) }
            switch account.value {
            case is AccountStatePending: Text("account.rowPending")
            case is AccountStateSignedIn: Text("you.badgeSignedIn")
            default: Text("you.badgeGuest")
            }
        }
        .font(.poulpy(.caption).weight(.heavy))
        .lineLimit(1)
        .foregroundStyle(signedIn ? PoulpyColor.onPrimary : PoulpyColor.onSurface)
        .padding(.horizontal, 10)
        .padding(.vertical, 3)
        .background(signedIn ? PoulpyColor.primary : PoulpyColor.surface, in: Capsule())
    }

    // MARK: Period

    private func periodTabs(_ selected: StatsPeriod) -> some View {
        let periods: [(StatsPeriod, LocalizedStringKey)] = [
            (.days7, "you.period7"), (.days30, "you.period30"), (.year, "you.periodYear"), (.allTime, "you.periodAll"),
        ]
        return HStack(spacing: 2) {
            ForEach(periods, id: \.0) { period, label in
                let on = period == selected
                Button { you.model.selectPeriod(selected: period) } label: {
                    Text(label)
                        .font(.poulpy(.subheadline).weight(on ? .heavy : .semibold))
                        .lineLimit(1)
                        .foregroundStyle(on ? PoulpyColor.primary : PoulpyColor.onSurfaceVariant)
                        .frame(maxWidth: .infinity, minHeight: 32)
                        .background(on ? PoulpyColor.surface : Color.clear, in: RoundedRectangle(cornerRadius: PoulpyRadius.sm))
                }
                .buttonStyle(.plain)
                .accessibilityAddTraits(on ? [.isButton, .isSelected] : .isButton)
            }
        }
        .padding(3)
        .background(PoulpyColor.surfaceVariant, in: RoundedRectangle(cornerRadius: PoulpyRadius.md))
    }

    // MARK: Cards

    private struct Card: Identifiable {
        let id: String
        let label: String
        let value: String
        let line: String
        var highlighted = false
        var labelPrimary = false
        var muted = false
    }

    /// The cards of the blocks that show, in the person's order, two to a row.
    private func cards(_ state: YouState) -> some View {
        var list: [Card] = []
        for choice in state.blocks where choice.shown {
            if choice.block == StatBlock.sessionsAndTime {
                let stats = state.stats
                var parts = [L("history.minutes %lld", Int64(stats.activeMinutes))]
                if stats.distanceMeters > 0 { parts.append(Units.shared.distance(meters: stats.distanceMeters, system: units, decimals: 1).text) }
                list.append(Card(id: "sessions", label: L("you.sessions"), value: "\(stats.sessions)", line: parts.joined(separator: " · "), highlighted: true))
            } else {
                list.append(weightCard(state.weight))
                list.append(Card(
                    id: "longest", label: L("history.longestRun"),
                    value: state.stats.longestRunMeters.map { Units.shared.distance(meters: $0.doubleValue, system: units, decimals: 1).text } ?? "–",
                    line: state.stats.longestRunMillis.map { shortDayMonth($0.int64Value) } ?? ""
                ))
                list.append(Card(
                    id: "octopus", label: L("history.fullOctopus"), value: "×\(state.fullOctopuses)",
                    line: L("history.tentaclesLit %lld", Int64(state.tentaclesLit))
                ))
            }
        }
        let rows = stride(from: 0, to: list.count, by: 2).map { Array(list[$0..<min($0 + 2, list.count)]) }
        return VStack(spacing: PoulpySpace.md) {
            ForEach(rows, id: \.first!.id) { row in
                HStack(alignment: .top, spacing: PoulpySpace.md) {
                    ForEach(row) { card in cardView(card) }
                    if row.count == 1 { Color.clear.frame(maxWidth: .infinity, maxHeight: 0) }
                }
            }
        }
    }

    private func weightCard(_ weight: WeightNow?) -> Card {
        let line: String
        if let weight {
            switch weight.daysAgo {
            case 0: line = L("you.weightToday")
            case 1: line = L("you.weightYesterday")
            default: line = L("you.weightDaysAgo %lld", Int64(weight.daysAgo))
            }
        } else {
            line = L("body.nothingLogged")
        }
        return Card(
            id: "weight", label: L("body.weight"),
            value: weight.map { BodyDisplay.shared.weight(kg: $0.kilograms, system: units, languageCode: Language.code) } ?? "–",
            line: line, labelPrimary: true, muted: true
        )
    }

    private func cardView(_ card: Card) -> some View {
        let content = card.highlighted ? PoulpyColor.onPrimaryContainer : PoulpyColor.onSurface
        return VStack(alignment: .leading, spacing: 2) {
            Text(card.label)
                .textCase(.uppercase)
                .font(PoulpyFont.label)
                .kerning(1)
                .lineLimit(1)
                .foregroundStyle(card.labelPrimary ? PoulpyColor.primary : (card.highlighted ? content : PoulpyColor.onSurfaceVariant))
            Text(card.value).font(PoulpyFont.title).monospacedDigit().lineLimit(1).foregroundStyle(content)
            if !card.line.isEmpty {
                Text(card.line)
                    .font(.poulpy(.subheadline).weight(card.muted ? .regular : .bold))
                    .foregroundStyle(card.muted ? PoulpyColor.onSurfaceVariant : content)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(EdgeInsets(top: 14, leading: 16, bottom: 14, trailing: 16))
        .background(card.highlighted ? PoulpyColor.primaryContainer : PoulpyColor.surface, in: RoundedRectangle(cornerRadius: PoulpyRadius.lg))
        .overlay(RoundedRectangle(cornerRadius: PoulpyRadius.lg).stroke(card.highlighted ? Color.clear : PoulpyColor.outline))
    }
}

/// Edit statistics: a switch and a handle per block, to choose and order what You shows. Every change is saved as it is made.
struct EditStatisticsView: View {
    @ObservedObject var you: Observed<YouModel, YouState>
    let onClose: () -> Void
    @State private var dragging: StatBlock?
    @State private var dragY: CGFloat = 0

    private let rowHeight: CGFloat = 56
    private let gap: CGFloat = PoulpySpace.sm

    var body: some View {
        let blocks = you.value.blocks
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: gap) {
                    FieldNote("you.editNote")
                    ForEach(Array(blocks.enumerated()), id: \.element.block) { index, choice in
                        row(choice, index: index)
                            .zIndex(dragging == choice.block ? 1 : 0)
                            .offset(y: dragging == choice.block ? dragY : 0)
                    }
                }
                .padding(.horizontal, PoulpySpace.lg)
                .padding(.vertical, PoulpySpace.sm)
            }
            .background(PoulpyColor.background)
            .navigationTitle("you.editTitle")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button(action: onClose) { LucideIcon(.x) }.accessibilityLabel(Text("action.close"))
                }
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("action.done", action: onClose).font(.poulpy(.headline).weight(.heavy))
                }
            }
        }
    }

    private func row(_ choice: StatBlockChoice, index: Int) -> some View {
        let name = blockName(choice.block)
        return HStack(spacing: 4) {
            LucideIcon(.gripVertical, size: 22)
                .foregroundStyle(PoulpyColor.onSurfaceVariant)
                .frame(width: 44, height: 44)
                .contentShape(Rectangle())
                .accessibilityLabel(Text(L("you.dragHandle %@", name)))
                .gesture(
                    DragGesture()
                        .onChanged { value in
                            dragging = choice.block
                            dragY = value.translation.height
                        }
                        .onEnded { value in
                            let steps = Int((value.translation.height / (rowHeight + gap)).rounded())
                            you.model.move(block: choice.block, to: Int32(index + steps))
                            dragging = nil
                            dragY = 0
                        }
                )
            Text(name).font(.poulpy(.headline).weight(.heavy))
            Spacer()
            Toggle(name, isOn: Binding(get: { choice.shown }, set: { you.model.setShown(block: choice.block, shown: $0) }))
                .labelsHidden()
        }
        .padding(.horizontal, 10)
        .frame(maxWidth: .infinity, minHeight: rowHeight)
        .background(PoulpyColor.surface, in: RoundedRectangle(cornerRadius: PoulpyRadius.md))
        .overlay(RoundedRectangle(cornerRadius: PoulpyRadius.md).stroke(PoulpyColor.outline, lineWidth: 1))
    }

    private func blockName(_ block: StatBlock) -> String {
        block == StatBlock.sessionsAndTime ? L("you.blockSessions") : L("you.blockRecords")
    }
}

/// "24 Oct", in the user's order.
private func shortDayMonth(_ millis: Int64) -> String {
    Date(timeIntervalSince1970: TimeInterval(millis) / 1000).formatted(.dateTime.day().month(.abbreviated))
}

/// A localized string with printf-style arguments, as in Localizable.strings.
private func L(_ key: String, _ args: CVarArg...) -> String {
    String(format: NSLocalizedString(key, comment: ""), locale: Locale.current, arguments: args)
}
