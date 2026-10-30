import SwiftUI
import Shared

/// What to open over Home.
private enum Cover: Identifiable {
    case player(workoutId: String?)

    var id: String {
        switch self {
        case .player(let id): return "player:\(id ?? "")"
        }
    }
}

/// Home: Poulpy, what there is to do or was done today, and the next planned session.
struct HomeView: View {
    let graph: AppGraph
    let onOpenLibrary: () -> Void
    @StateObject private var home: Observed<HomeModel, HomeState>
    @State private var cover: Cover?

    init(graph: AppGraph, onOpenLibrary: @escaping () -> Void) {
        self.graph = graph
        self.onOpenLibrary = onOpenLibrary
        // Built once: StateObject evaluates this only for the first init of the view.
        _home = StateObject(wrappedValue: {
            let model = graph.home(languageCode: Language.code)
            return Observed(model, current: model.current, watch: model.watch, close: { $0.close() })
        }())
    }

    var body: some View {
        let state = home.value
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 10) {
                    Text(Date().formatted(.dateTime.weekday(.wide).day().month(.wide)).uppercased())
                        .font(PoulpyFont.label)
                        .kerning(1)
                        .foregroundStyle(PoulpyColor.onSurfaceVariant)
                    if let poulpy = state.poulpy {
                        PoulpyHeaderView(status: poulpy, compact: true)
                    }
                    if state.loaded {
                        if state.today.isEmpty {
                            restCard(firstTime: !state.hasWorkouts)
                        } else {
                            sectionLabel("home.today")
                            ForEach(state.today, id: \.key) { item in
                                todayCard(item)
                            }
                            Button(action: onOpenLibrary) {
                                HStack(spacing: 4) {
                                    Text("home.somethingElse")
                                    Image(systemName: "chevron.right").font(.system(size: 13, weight: .bold))
                                }
                                .font(.poulpy(.subheadline).weight(.heavy))
                                .frame(minHeight: 44)
                            }
                            .padding(.horizontal, 4)
                        }
                        if let next = state.next {
                            sectionLabel("home.next").padding(.top, 8)
                            NavigationLink {
                                WorkoutPageView(graph: graph, workoutId: next.workoutId)
                            } label: {
                                nextCard(next)
                            }
                            .buttonStyle(.plain)
                        }
                    }
                }
                .padding(.horizontal, 20)
                .padding(.bottom, PoulpySpace.xl)
            }
            .background(PoulpyColor.background)
            .navigationTitle("tab.home")
            .onAppear { home.model.refresh() }
            .fullScreenCover(item: $cover, onDismiss: { home.model.refresh() }) { cover in
                switch cover {
                case .player(let id): PlayerView(graph: graph, workoutId: id)
                }
            }
        }
    }

    private func sectionLabel(_ key: LocalizedStringKey) -> some View {
        Text(key)
            .textCase(.uppercase)
            .font(PoulpyFont.label)
            .kerning(1)
            .foregroundStyle(PoulpyColor.onSurfaceVariant)
            .padding(.leading, 4)
            .padding(.top, 6)
    }

    @ViewBuilder
    private func todayCard(_ item: TodayItem) -> some View {
        switch item.status {
        case .done:
            if let session = item.session {
                NavigationLink {
                    SessionDetailView(session: session, onDelete: { home.model.deleteSession(sessionId: session.id) })
                } label: {
                    doneCard(session)
                }
                .buttonStyle(.plain)
            }
        default:
            plannedCard(item)
        }
    }

    /// A workout to do today, or left halfway.
    private func plannedCard(_ item: TodayItem) -> some View {
        let inProgress = item.status == .inProgress
        return VStack(alignment: .leading, spacing: 12) {
            HStack(spacing: 12) {
                Image(systemName: "dumbbell")
                    .font(.system(size: 18, weight: .semibold))
                    .foregroundStyle(PoulpyColor.onPrimaryContainer)
                    .frame(width: 44, height: 44)
                    .background(PoulpyColor.primaryContainer, in: Circle())
                    .accessibilityHidden(true)
                VStack(alignment: .leading, spacing: 2) {
                    Text(item.title).font(PoulpyFont.headline).foregroundStyle(PoulpyColor.onSurface)
                    Text(item.details).font(.poulpy(.subheadline)).foregroundStyle(PoulpyColor.onSurfaceVariant)
                }
                Spacer(minLength: 0)
            }
            if inProgress {
                Text("home.inProgress").font(.poulpy(.subheadline).weight(.bold)).foregroundStyle(PoulpyColor.onSurface)
            } else if !item.exercises.isEmpty {
                Text(item.exercises).font(.poulpy(.subheadline)).foregroundStyle(PoulpyColor.onSurface)
            }
            HStack(spacing: 8) {
                if let workoutId = item.workoutId {
                    NavigationLink {
                        WorkoutPageView(graph: graph, workoutId: workoutId)
                    } label: {
                        Text("home.seeWorkout")
                            .font(.poulpy(.body).weight(.heavy))
                            .frame(maxWidth: .infinity, minHeight: 48)
                            .overlay(Capsule().stroke(PoulpyColor.outline, lineWidth: 2))
                    }
                    .buttonStyle(.plain)
                }
                Button {
                    cover = .player(workoutId: inProgress ? nil : item.workoutId)
                } label: {
                    Label(LocalizedStringKey(inProgress ? "player.resume" : "action.start"), systemImage: "play.fill")
                        .font(.poulpy(.body).weight(.heavy))
                        .frame(maxWidth: .infinity, minHeight: 48)
                        .foregroundStyle(PoulpyColor.onPrimary)
                        .background(PoulpyColor.primary, in: Capsule())
                }
                .buttonStyle(.plain)
                .layoutPriority(1)
            }
        }
        .padding(14)
        .background(PoulpyColor.surface, in: RoundedRectangle(cornerRadius: 20))
        .overlay(RoundedRectangle(cornerRadius: 20).stroke(PoulpyPalette.lagoon300, lineWidth: 1.5))
    }

    /// A session finished today, opening its page as in the history.
    private func doneCard(_ session: SessionRow) -> some View {
        HStack(spacing: 12) {
            Image(systemName: "checkmark")
                .font(.system(size: 18, weight: .heavy))
                .foregroundStyle(PoulpyColor.surface)
                .frame(width: 44, height: 44)
                .background(PoulpyColor.success, in: Circle())
                .accessibilityLabel(Text("workout.done"))
            VStack(alignment: .leading, spacing: 2) {
                Text(session.title).font(PoulpyFont.headline).foregroundStyle(PoulpyColor.onSurface)
                Text("\(time(session.startedAtMillis)) · \(clock(session.activeSeconds))")
                    .font(.poulpy(.subheadline))
                    .monospacedDigit()
                    .foregroundStyle(PoulpyColor.onSurfaceVariant)
            }
            Spacer(minLength: 0)
            Image(systemName: "chevron.right").foregroundStyle(PoulpyColor.onSurfaceVariant)
        }
        .padding(14)
        .background(PoulpyColor.surface, in: RoundedRectangle(cornerRadius: 20))
        .overlay(RoundedRectangle(cornerRadius: 20).stroke(PoulpyColor.outline))
    }

    private func nextCard(_ next: NextSession) -> some View {
        let day = Date(timeIntervalSince1970: Double(next.dayMillis) / 1000)
        let when = next.daysAway == 1
            ? L("home.tomorrow %@", next.details)
            : L("home.inDays %lld %@", Int64(next.daysAway), next.details)
        return HStack(spacing: 12) {
            VStack(spacing: 0) {
                Text(day.formatted(.dateTime.weekday(.abbreviated)).uppercased())
                    .font(.poulpy(.caption2).weight(.heavy))
                    .foregroundStyle(PoulpyColor.primary)
                Text(day.formatted(.dateTime.day()))
                    .font(PoulpyFont.headline)
                    .foregroundStyle(PoulpyColor.onPrimaryContainer)
            }
            .frame(width: 52, height: 56)
            .background(PoulpyColor.background, in: RoundedRectangle(cornerRadius: 14))
            .overlay(RoundedRectangle(cornerRadius: 14).stroke(PoulpyColor.outline))
            VStack(alignment: .leading, spacing: 2) {
                Text(next.title).font(.poulpy(.headline)).foregroundStyle(PoulpyColor.onSurface)
                Text(when).font(.poulpy(.subheadline)).foregroundStyle(PoulpyColor.onSurfaceVariant)
            }
            Spacer(minLength: 0)
            Image(systemName: "chevron.right").foregroundStyle(PoulpyColor.onSurfaceVariant)
        }
        .padding(12)
        .background(PoulpyColor.surface, in: RoundedRectangle(cornerRadius: 20))
        .overlay(RoundedRectangle(cornerRadius: 20).stroke(PoulpyColor.outline))
    }

    /// Nothing planned or done today: rest, or go and pick something.
    private func restCard(firstTime: Bool) -> some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(LocalizedStringKey(firstTime ? "home.firstTitle" : "home.restTitle")).font(PoulpyFont.headline)
            Text(LocalizedStringKey(firstTime ? "home.firstBody" : "home.restBody")).font(.poulpy(.subheadline))
            Button(action: onOpenLibrary) {
                Label("home.openLibrary", systemImage: "books.vertical")
                    .font(.poulpy(.body).weight(.heavy))
                    .padding(.horizontal, 20)
                    .frame(minHeight: 48)
                    .foregroundStyle(PoulpyColor.onPrimary)
                    .background(PoulpyColor.primary, in: Capsule())
            }
            .buttonStyle(.plain)
            .padding(.top, 8)
        }
        .foregroundStyle(PoulpyColor.onSurface)
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(.horizontal, 16)
        .padding(.vertical, 18)
        .overlay(
            RoundedRectangle(cornerRadius: 20)
                .stroke(PoulpyColor.onSurfaceVariant.opacity(0.5), style: StrokeStyle(lineWidth: 1.5, dash: [6, 4]))
        )
        .padding(.top, 6)
    }
}

/// A localized string with printf-style arguments, as in Localizable.strings.
private func L(_ key: String, _ args: CVarArg...) -> String {
    String(format: NSLocalizedString(key, comment: ""), locale: Locale.current, arguments: args)
}

private func time(_ millis: Int64) -> String {
    Date(timeIntervalSince1970: Double(millis) / 1000).formatted(date: .omitted, time: .shortened)
}

private func clock(_ seconds: Int64) -> String {
    let h = seconds / 3600, m = (seconds % 3600) / 60, s = seconds % 60
    return h > 0 ? String(format: "%lld:%02lld:%02lld", h, m, s) : String(format: "%lld:%02lld", m, s)
}
