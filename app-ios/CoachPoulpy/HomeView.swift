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

/// Home, the diary: what is up next, then the past by week and day, newest first. Record starts a session.
struct HomeView: View {
    let graph: AppGraph
    let onOpenLibrary: () -> Void
    /// Opens Live over the tabs, to record a walk or run or to open a recording left open.
    let onOpenLive: () -> Void
    @StateObject private var home: Observed<HomeModel, HomeState>
    @State private var cover: Cover?
    @State private var discarding = false
    @State private var choosingRecord = false
    @State private var jumping = false

    init(graph: AppGraph, onOpenLibrary: @escaping () -> Void, onOpenLive: @escaping () -> Void) {
        self.graph = graph
        self.onOpenLibrary = onOpenLibrary
        self.onOpenLive = onOpenLive
        // Built once: StateObject evaluates this only for the first init of the view.
        _home = StateObject(wrappedValue: {
            let model = graph.home(languageCode: Language.code, memory: UserDefaultsStore())
            return Observed(model, current: model.current, watch: model.watch, close: { $0.close() })
        }())
    }

    var body: some View {
        let state = home.value
        NavigationStack {
            // The top stays put and only the past scrolls, as drawn.
            VStack(alignment: .leading, spacing: PoulpySpace.sm) {
                if let poulpy = state.poulpy {
                    PoulpyHeaderView(status: poulpy, compact: true)
                }
                if state.loaded {
                    let upNext = state.today.filter { $0.status != .done }
                    if upNext.isEmpty && state.next == nil {
                        restCard(firstTime: !state.hasWorkouts)
                    } else {
                        sectionLabel("home.upNext")
                        ForEach(Array(upNext.enumerated()), id: \.element.key) { index, item in
                            upNextCard(item, highlighted: index == 0)
                        }
                        if let next = state.next {
                            NavigationLink {
                                WorkoutPageView(graph: graph, workoutId: next.workoutId)
                            } label: {
                                nextCard(next)
                            }
                            .buttonStyle(.plain)
                        }
                    }
                    if !state.past.isEmpty {
                        HStack {
                            sectionLabel("home.past")
                            Spacer(minLength: 0)
                            // 40 square with the screens' corner, as drawn.
                            Button { jumping = true } label: {
                                LucideIcon(.calendar, size: 20).frame(width: 40, height: 40)
                            }
                            .foregroundStyle(PoulpyColor.onSurface)
                            .accessibilityLabel(Text("home.jumpToDay"))
                        }
                        ScrollViewReader { proxy in
                            ScrollView {
                                LazyVStack(alignment: .leading, spacing: 0) {
                                    ForEach(Array(state.past.enumerated()), id: \.offset) { index, entry in
                                        TimelineRow(
                                            dot: entry is DiaryEntrySession ? PoulpyColor.outline : PoulpyColor.primary,
                                            first: index == 0,
                                            last: index == state.past.count - 1
                                        ) {
                                            pastRow(entry)
                                        }
                                        .id(index)
                                    }
                                }
                            }
                            .sheet(isPresented: $jumping) {
                                JumpToDaySheet(graph: graph) { dayMillis in
                                    jumping = false
                                    let index = home.model.indexOfDay(dayMillis: dayMillis)
                                    if index >= 0 { withAnimation { proxy.scrollTo(Int(index), anchor: .top) } }
                                }
                            }
                        }
                    } else {
                        Spacer(minLength: 0)
                    }
                }
            }
            .padding(.horizontal, PoulpySpace.lg)
            .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topLeading)
            .background(PoulpyColor.background)
            .navigationTitle("tab.home")
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button { choosingRecord = true } label: {
                        Label("home.record", lucide: .circleDot)
                    }
                    .buttonStyle(PoulpyButtonStyle(.primary, size: .md))
                }
            }
            .onAppear { home.model.refresh() }
            .confirmationDialog("home.discardTitle", isPresented: $discarding, titleVisibility: .visible) {
                Button("home.discard", role: .destructive) { home.model.discardUnfinished() }
            } message: {
                Text("home.discardBody")
            }
            .sheet(isPresented: $choosingRecord) {
                RecordSheet(
                    onWalkOrRun: {
                        choosingRecord = false
                        onOpenLive()
                    },
                    onLibrary: {
                        choosingRecord = false
                        onOpenLibrary()
                    },
                    onClose: { choosingRecord = false }
                )
            }
            .fullScreenCover(item: $cover, onDismiss: { home.model.refresh() }) { cover in
                switch cover {
                case .player(let id): PlayerView(graph: graph, workoutId: id)
                }
            }
        }
    }

    private func sectionLabel(_ key: LocalizedStringKey) -> some View {
        sectionStyle(Text(key))
    }

    /// A heading already built from a date, so not a key to look up.
    private func sectionLabel(verbatim text: String) -> some View {
        sectionStyle(Text(verbatim: text))
    }

    private func sectionStyle(_ text: Text) -> some View {
        text
            .textCase(.uppercase)
            .font(PoulpyFont.label)
            .kerning(1)
            .foregroundStyle(PoulpyColor.onSurfaceVariant)
            .padding(.leading, 4)
            .padding(.top, 6)
    }

    @ViewBuilder
    private func pastRow(_ entry: DiaryEntry) -> some View {
        switch entry {
        case let week as DiaryEntryWeekHeading:
            ruledLabel(week.isCurrent ? L("history.thisWeek") : L("home.weekOf %@", shortDate(week.startMillis)),
                       color: week.isCurrent ? PoulpyColor.primary : PoulpyColor.onSurfaceVariant)
        case let recap as DiaryEntryWeekRecap:
            VStack(alignment: .leading, spacing: 2) {
                Text("home.yourWeek").font(.poulpy(.headline))
                Text("\(L("history.sessions %lld", Int64(recap.sessions))) · \(L("history.minutes %lld", Int64(recap.activeMinutes)))")
                    .font(.poulpy(.subheadline).weight(.bold))
            }
            .foregroundStyle(PoulpyColor.onPrimaryContainer)
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(PoulpySpace.md)
            .background(PoulpyColor.primaryContainer, in: RoundedRectangle(cornerRadius: PoulpyRadius.lg))
        case let day as DiaryEntryDayHeading:
            let label = dayLabel(day.dateMillis)
            ruledLabel(day.isToday ? L("history.todayDay %@", label) : label, color: PoulpyColor.onSurfaceVariant)
        case let item as DiaryEntrySession:
            NavigationLink {
                SessionDetailView(graph: graph, session: item.row, onDelete: { home.model.deleteSession(sessionId: item.row.id) })
            } label: {
                SessionCardView(session: item.row, diary: true)
            }
            .buttonStyle(.plain)
        default:
            EmptyView()
        }
    }

    /// A label followed by a hairline to the edge, as the day headings of the diary are drawn.
    private func ruledLabel(_ text: String, color: Color) -> some View {
        HStack(spacing: PoulpySpace.sm) {
            Text(verbatim: text)
                .textCase(.uppercase)
                .font(PoulpyFont.label)
                .kerning(1)
                .foregroundStyle(color)
            Rectangle().fill(PoulpyColor.outline).frame(height: 1)
        }
        .padding(.leading, PoulpySpace.xs)
        .padding(.top, 6)
    }

    /// A workout to do today, or a session left halfway, which can be opened (it comes back paused) or discarded.
    private func upNextCard(_ item: TodayItem, highlighted: Bool) -> some View {
        let inProgress = item.status == .inProgress
        return VStack(alignment: .leading, spacing: PoulpySpace.xs) {
            Group {
                if inProgress {
                    Button {
                        if item.live { onOpenLive() } else { cover = .player(workoutId: nil) }
                    } label: { upNextRow(item, inProgress: true, highlighted: highlighted) }
                } else if let workoutId = item.workoutId {
                    NavigationLink {
                        WorkoutPageView(graph: graph, workoutId: workoutId)
                    } label: { upNextRow(item, inProgress: false, highlighted: highlighted) }
                }
            }
            .buttonStyle(.plain)
            if inProgress {
                // Destructive, so crimson with an icon, never ember.
                Button { discarding = true } label: {
                    Label("home.discard", lucide: .trash)
                        .font(.poulpy(.subheadline).weight(.heavy))
                        .foregroundStyle(PoulpyColor.error)
                        .frame(minHeight: 44)
                }
                .buttonStyle(.plain)
                .padding(.leading, 4)
            }
        }
    }

    private func upNextRow(_ item: TodayItem, inProgress: Bool, highlighted: Bool) -> some View {
        HStack(spacing: PoulpySpace.sm) {
            LucideIcon(.dumbbell, size: 22)
                .foregroundStyle(PoulpyColor.onPrimaryContainer)
                .frame(width: 44, height: 44)
                .background(PoulpyColor.primaryContainer, in: Circle())
                .accessibilityHidden(true)
            VStack(alignment: .leading, spacing: 2) {
                Text(item.title).font(.poulpy(.headline)).foregroundStyle(PoulpyColor.onSurface)
                let detail = inProgress ? L("home.inProgress") : item.details
                if !detail.isEmpty {
                    Text(detail).font(.poulpy(.subheadline)).foregroundStyle(PoulpyColor.onSurfaceVariant)
                }
            }
            Spacer(minLength: 0)
            LucideIcon(.chevronRight).foregroundStyle(PoulpyColor.onSurfaceVariant)
        }
        .padding(PoulpySpace.sm)
        .frame(minHeight: 60)
        .background(PoulpyColor.surface, in: RoundedRectangle(cornerRadius: PoulpyRadius.lg))
        .overlay(
            RoundedRectangle(cornerRadius: PoulpyRadius.lg)
                .stroke(highlighted ? PoulpyColor.primary.opacity(0.5) : PoulpyColor.outline, lineWidth: highlighted ? 1.5 : 1)
        )
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
                    .font(.poulpy(.headline))
                    .foregroundStyle(PoulpyColor.onPrimaryContainer)
            }
            .frame(width: 44, height: 48)
            .background(PoulpyColor.background, in: RoundedRectangle(cornerRadius: PoulpyRadius.md))
            .overlay(RoundedRectangle(cornerRadius: PoulpyRadius.md).stroke(PoulpyColor.outline))
            VStack(alignment: .leading, spacing: 2) {
                Text(next.title).font(.poulpy(.headline)).foregroundStyle(PoulpyColor.onSurface)
                Text(when).font(.poulpy(.subheadline)).foregroundStyle(PoulpyColor.onSurfaceVariant)
            }
            Spacer(minLength: 0)
            LucideIcon(.chevronRight).foregroundStyle(PoulpyColor.onSurfaceVariant)
        }
        .padding(PoulpySpace.sm)
        .background(PoulpyColor.surface, in: RoundedRectangle(cornerRadius: PoulpyRadius.lg))
        .overlay(RoundedRectangle(cornerRadius: PoulpyRadius.lg).stroke(PoulpyColor.outline))
    }

    /// Nothing planned or done today: rest, or go and pick something.
    private func restCard(firstTime: Bool) -> some View {
        VStack(alignment: .leading, spacing: PoulpySpace.xs) {
            Text(LocalizedStringKey(firstTime ? "home.firstTitle" : "home.restTitle")).font(PoulpyFont.headline)
            Text(LocalizedStringKey(firstTime ? "home.firstBody" : "home.restBody")).font(.poulpy(.subheadline))
            Button(action: onOpenLibrary) {
                Label("home.openLibrary", lucide: .libraryBig)
            }
            .buttonStyle(PoulpyButtonStyle(.primary, size: .md))
            .padding(.top, PoulpySpace.xs)
        }
        .foregroundStyle(PoulpyColor.onSurface)
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(PoulpySpace.md)
        .overlay(
            RoundedRectangle(cornerRadius: PoulpyRadius.lg)
                .stroke(PoulpyColor.onSurfaceVariant.opacity(0.5), style: StrokeStyle(lineWidth: 1.5, dash: [6, 4]))
        )
        .padding(.top, PoulpySpace.xs)
    }
}

/// A localized string with printf-style arguments, as in Localizable.strings.
private func L(_ key: String, _ args: CVarArg...) -> String {
    String(format: NSLocalizedString(key, comment: ""), locale: Locale.current, arguments: args)
}

/// What can be recorded: a walk or run followed live, or a workout from the library.
private struct RecordSheet: View {
    let onWalkOrRun: () -> Void
    let onLibrary: () -> Void
    let onClose: () -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: PoulpySpace.sm) {
            HStack(alignment: .top) {
                VStack(alignment: .leading, spacing: 2) {
                    Text("record.title").font(.poulpy(.title3))
                    Text("record.subtitle").font(.poulpy(.subheadline)).foregroundStyle(PoulpyColor.onSurfaceVariant)
                }
                Spacer(minLength: 0)
                Button(action: onClose) { LucideIcon(.x, size: 22).frame(width: 44, height: 44) }
                    .accessibilityLabel(Text("action.close"))
            }
            choice(.footprints, "record.walkTitle", "record.walkDetail", onWalkOrRun)
            choice(.libraryBig, "record.libraryTitle", "record.libraryDetail", onLibrary)
            Spacer(minLength: 0)
        }
        .padding(PoulpySpace.lg)
        .background(PoulpyColor.surface)
        .presentationDetents([.medium])
    }

    private func choice(_ icon: Lucide, _ title: LocalizedStringKey, _ detail: LocalizedStringKey, _ action: @escaping () -> Void) -> some View {
        Button(action: action) {
            HStack(spacing: PoulpySpace.sm) {
                LucideIcon(icon, size: 22)
                    .foregroundStyle(PoulpyColor.onPrimaryContainer)
                    .frame(width: 44, height: 44)
                    .background(PoulpyColor.primaryContainer, in: Circle())
                    .accessibilityHidden(true)
                VStack(alignment: .leading, spacing: 2) {
                    Text(title).font(.poulpy(.headline)).foregroundStyle(PoulpyColor.onSurface)
                    Text(detail).font(.poulpy(.subheadline)).foregroundStyle(PoulpyColor.onSurfaceVariant)
                        .multilineTextAlignment(.leading)
                }
                Spacer(minLength: 0)
                LucideIcon(.chevronRight).foregroundStyle(PoulpyColor.onSurfaceVariant)
            }
            .padding(PoulpySpace.sm)
            .frame(minHeight: 60)
            .background(PoulpyColor.surface, in: RoundedRectangle(cornerRadius: PoulpyRadius.lg))
            .overlay(RoundedRectangle(cornerRadius: PoulpyRadius.lg).stroke(PoulpyColor.outline))
        }
        .buttonStyle(.plain)
    }
}

/// The calendar over Home: a tapped day scrolls the past to it.
private struct JumpToDaySheet: View {
    @StateObject private var history: Observed<HistoryModel, HistoryState>
    @State private var offset: Int32 = 0
    let onDay: (Int64) -> Void

    init(graph: AppGraph, onDay: @escaping (Int64) -> Void) {
        self.onDay = onDay
        _history = StateObject(wrappedValue: {
            let model = graph.history(languageCode: Language.code)
            return Observed(model, current: model.current, watch: model.watch, close: { $0.close() })
        }())
    }

    var body: some View {
        let state = history.value
        let month = history.model.month(state: state, offset: offset, selectedDay: 0)
        VStack(alignment: .leading, spacing: 0) {
            Text("home.jumpToDay").font(.poulpy(.title3)).padding(.horizontal, PoulpySpace.lg).padding(.top, PoulpySpace.lg)
            MonthCardView(month: month, selected: 0, onSelect: { day in
                let start = Date(timeIntervalSince1970: Double(month.monthStartMillis) / 1000)
                let target = Calendar.current.date(byAdding: .day, value: Int(day) - 1, to: start) ?? start
                onDay(Int64(Calendar.current.startOfDay(for: target).timeIntervalSince1970 * 1000))
            }, onMonth: { step in
                offset = min(0, max(state.firstMonthOffset, offset + step))
            })
            Spacer(minLength: 0)
        }
        .background(PoulpyColor.background)
        .presentationDetents([.medium, .large])
    }
}

/// "24 Oct", in the user's order.
private func shortDate(_ millis: Int64) -> String {
    Date(timeIntervalSince1970: Double(millis) / 1000).formatted(.dateTime.day().month(.abbreviated))
}

/// "Sat 24 Oct", in the user's order.
private func dayLabel(_ millis: Int64) -> String {
    Date(timeIntervalSince1970: Double(millis) / 1000).formatted(.dateTime.weekday(.abbreviated).day().month(.abbreviated))
}

/// One row of the diary's rail: a line down the left with a dot at the row's middle, primary for headings and the week, outline for sessions.
private struct TimelineRow<Content: View>: View {
    let dot: Color
    let first: Bool
    let last: Bool
    @ViewBuilder let content: () -> Content

    var body: some View {
        content()
            .padding(.leading, PoulpySpace.xl)
            .padding(.bottom, PoulpySpace.sm)
            .background(alignment: .leading) {
                GeometryReader { geo in
                    let middle = (geo.size.height - PoulpySpace.sm) / 2
                    Path { path in
                        path.move(to: CGPoint(x: 8, y: first ? middle : 0))
                        path.addLine(to: CGPoint(x: 8, y: last ? middle : geo.size.height))
                    }
                    .stroke(PoulpyColor.outline, style: StrokeStyle(lineWidth: 2, lineCap: .round))
                    Circle().fill(PoulpyColor.background).frame(width: 16, height: 16).position(x: 8, y: middle)
                    Circle().fill(dot).frame(width: 12, height: 12).position(x: 8, y: middle)
                }
            }
    }
}
