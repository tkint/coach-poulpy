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
                    NavigationLink {
                        AchievementsView(graph: graph)
                    } label: {
                        MotivationRow(status: poulpy, achievements: state.achievements)
                    }
                    .buttonStyle(.plain)
                }
                if state.loaded {
                    let upNext = state.today.filter { $0.status != .done }
                    if !state.hasWorkouts {
                        restCard(firstTime: true)
                    } else {
                        sectionLabel("home.upNext")
                        ForEach(Array(upNext.enumerated()), id: \.element.key) { index, item in
                            upNextCard(item, highlighted: index == 0)
                        }
                        if upNext.isEmpty {
                            Button(action: onOpenLibrary) { restRow }
                                .buttonStyle(.plain)
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
                                            // Headings sit on the line without a dot, as drawn.
                                            dot: entry is DiaryEntrySession ? PoulpyColor.outline : entry is DiaryEntryWeekRecap ? PoulpyColor.primary : nil,
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
            ruledLabel(day.isToday ? L("history.todayDay %@", label) : label, color: day.isToday ? PoulpyColor.primary : PoulpyColor.onSurfaceVariant)
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
        .padding(.horizontal, PoulpySpace.xs)
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

    private func upNextRow(_ item: TodayItem, inProgress: Bool, highlighted: Bool, detail: String? = nil) -> some View {
        HStack(spacing: PoulpySpace.sm) {
            LucideIcon(.dumbbell, size: 22)
                .foregroundStyle(PoulpyColor.onPrimaryContainer)
                .frame(width: 44, height: 44)
                .background(PoulpyColor.primaryContainer, in: Circle())
                .accessibilityHidden(true)
            VStack(alignment: .leading, spacing: 2) {
                Text(item.title).font(.poulpy(.headline)).foregroundStyle(PoulpyColor.onSurface)
                let line = detail ?? (inProgress ? L("home.inProgress") : item.details)
                if !line.isEmpty {
                    Text(line).font(.poulpy(.subheadline)).foregroundStyle(PoulpyColor.onSurfaceVariant)
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

    /// Nothing to do today: a row the size of the Up next ones, opening the library.
    private var restRow: some View {
        HStack(spacing: PoulpySpace.sm) {
            LucideIcon(.moon, size: 22)
                .foregroundStyle(PoulpyColor.onSurfaceVariant)
                .frame(width: 44, height: 44)
                .background(PoulpyColor.surfaceVariant, in: Circle())
                .accessibilityHidden(true)
            VStack(alignment: .leading, spacing: 2) {
                Text("home.restTitle").font(.poulpy(.headline)).foregroundStyle(PoulpyColor.onSurface)
                Text("home.restLine").font(.poulpy(.subheadline)).foregroundStyle(PoulpyColor.onSurfaceVariant)
            }
            Spacer(minLength: 0)
            LucideIcon(.chevronRight).foregroundStyle(PoulpyColor.onSurfaceVariant)
        }
        .padding(PoulpySpace.sm)
        .frame(minHeight: 60)
        .background(PoulpyColor.surface, in: RoundedRectangle(cornerRadius: PoulpyRadius.lg))
        .overlay(RoundedRectangle(cornerRadius: PoulpyRadius.lg).stroke(PoulpyColor.outline))
    }

    /// The next planned session, drawn as an Up next row: "Tomorrow · Sets · about 25 min".
    private func nextCard(_ next: NextSession) -> some View {
        let when = next.daysAway == 1
            ? L("home.tomorrow %@", next.details)
            : L("home.inDays %lld %@", Int64(next.daysAway), next.details)
        return HStack(spacing: PoulpySpace.sm) {
            LucideIcon(.dumbbell, size: 22)
                .foregroundStyle(PoulpyColor.onPrimaryContainer)
                .frame(width: 44, height: 44)
                .background(PoulpyColor.primaryContainer, in: Circle())
                .accessibilityHidden(true)
            VStack(alignment: .leading, spacing: 2) {
                Text(next.title).font(.poulpy(.headline)).foregroundStyle(PoulpyColor.onSurface)
                Text(when).font(.poulpy(.subheadline)).foregroundStyle(PoulpyColor.onSurfaceVariant)
            }
            Spacer(minLength: 0)
            LucideIcon(.chevronRight).foregroundStyle(PoulpyColor.onSurfaceVariant)
        }
        .padding(PoulpySpace.sm)
        .frame(minHeight: 60)
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

/// The native calendar over Home, with a dot under the days that have sessions: a tapped day scrolls the past to it.
private struct JumpToDaySheet: View {
    @StateObject private var history: Observed<HistoryModel, HistoryState>
    @Environment(\.dismiss) private var dismiss
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
        NavigationStack {
            SessionCalendar(
                sessionDays: Set(state.sessions.map { dayKey(Date(timeIntervalSince1970: Double($0.startedAtMillis) / 1000)) }),
                firstDay: state.firstDayMillis.map { Date(timeIntervalSince1970: Double(truncating: $0) / 1000) }
            ) { picked in
                onDay(Int64(Calendar.current.startOfDay(for: picked).timeIntervalSince1970 * 1000))
            }
            .padding(.horizontal, PoulpySpace.lg)
            .frame(maxHeight: .infinity, alignment: .top)
            .background(PoulpyColor.background)
            .navigationTitle("home.jumpToDay")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button(LocalizedStringKey("action.close")) { dismiss() }
                }
            }
        }
        .presentationDetents([.medium, .large])
    }
}

private func dayKey(_ date: Date) -> Int {
    let parts = Calendar.current.dateComponents([.year, .month, .day], from: date)
    return (parts.year ?? 0) * 10_000 + (parts.month ?? 0) * 100 + (parts.day ?? 0)
}

/// UIKit's calendar (iOS 16): it is the system's own, and the only native one that can decorate days.
private struct SessionCalendar: UIViewRepresentable {
    let sessionDays: Set<Int>
    let firstDay: Date?
    let onPick: (Date) -> Void

    func makeCoordinator() -> Coordinator { Coordinator(self) }

    func makeUIView(context: Context) -> UICalendarView {
        let view = UICalendarView()
        view.calendar = Calendar.current
        view.tintColor = UIColor(PoulpyColor.primary)
        view.delegate = context.coordinator
        view.selectionBehavior = UICalendarSelectionSingleDate(delegate: context.coordinator)
        return view
    }

    func updateUIView(_ view: UICalendarView, context: Context) {
        context.coordinator.parent = self
        let calendar = Calendar.current
        // Nothing to jump to before the first session, or after today.
        let start = firstDay ?? calendar.startOfDay(for: Date())
        view.availableDateRange = DateInterval(start: start, end: Date())
        let days = sessionDays.compactMap { key in
            DateComponents(calendar: calendar, year: key / 10_000, month: key / 100 % 100, day: key % 100)
        }
        view.reloadDecorations(forDateComponents: days, animated: false)
    }

    final class Coordinator: NSObject, UICalendarViewDelegate, UICalendarSelectionSingleDateDelegate {
        var parent: SessionCalendar

        init(_ parent: SessionCalendar) { self.parent = parent }

        func calendarView(_ calendarView: UICalendarView, decorationFor dateComponents: DateComponents) -> UICalendarView.Decoration? {
            guard let date = calendarView.calendar.date(from: dateComponents), parent.sessionDays.contains(dayKey(date)) else { return nil }
            return .default(color: UIColor(PoulpyColor.primary), size: .large)
        }

        func dateSelection(_ selection: UICalendarSelectionSingleDate, didSelectDate dateComponents: DateComponents?) {
            guard let dateComponents, let date = Calendar.current.date(from: dateComponents) else { return }
            parent.onPick(date)
        }
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

/// One row of the diary's rail: a line down the left with a dot at the row's middle, primary for the week, outline for sessions, none for headings.
private struct TimelineRow<Content: View>: View {
    let dot: Color?
    let first: Bool
    let last: Bool
    @ViewBuilder let content: () -> Content

    var body: some View {
        content()
            .padding(.leading, PoulpySpace.xl)
            .padding(.bottom, PoulpySpace.sm)
            .background(alignment: .leading) {
                GeometryReader { geo in
                    let content = geo.size.height - PoulpySpace.sm
                    let middle = content / 2
                    // One line down the whole past, 12 in from its first and last rows.
                    Path { path in
                        path.move(to: CGPoint(x: 8, y: first ? 12 : 0))
                        path.addLine(to: CGPoint(x: 8, y: last ? content - 12 : geo.size.height))
                    }
                    .stroke(PoulpyColor.outline, style: StrokeStyle(lineWidth: 2, lineCap: .round))
                    if let dot {
                        Circle().fill(PoulpyColor.background).frame(width: 16, height: 16).position(x: 8, y: middle)
                        Circle().fill(dot).frame(width: 12, height: 12).position(x: 8, y: middle)
                    }
                }
            }
    }
}

/// Home's motivation row (`home-diary` mockup): a small Poulpy, his eight tentacles as segments (never a streak) and the
/// latest badges. It opens Achievements.
private struct MotivationRow: View {
    let status: PoulpyStatus
    let achievements: AchievementsUi

    var body: some View {
        HStack(spacing: PoulpySpace.sm) {
            Image(status.drawing.imageName)
                .resizable()
                .scaledToFit()
                .frame(width: 36, height: 32)
                .padding(.leading, 4)
            HStack(spacing: 3) {
                ForEach(0..<8, id: \.self) { index in
                    Capsule()
                        .fill(index < Int(status.tentaclesLit) ? PoulpyColor.primary : PoulpyColor.onPrimaryContainer.opacity(0.18))
                        .frame(height: 8)
                }
            }
            // The latest badges, then a locked "?" while some are still hidden.
            ForEach(Array(achievements.latest(count: 2).enumerated()), id: \.offset) { _, badge in
                chip(badge)
            }
            if !achievements.locked.isEmpty { chip(nil) }
        }
        .padding(.leading, PoulpySpace.xs)
        .padding(.trailing, PoulpySpace.sm)
        .frame(maxWidth: .infinity, minHeight: 48)
        .background(PoulpyColor.surface, in: Capsule())
        .overlay(Capsule().stroke(PoulpyColor.outline))
        .contentShape(Capsule())
        .accessibilityElement(children: .ignore)
        .accessibilityLabel(Text(verbatim: String(format: NSLocalizedString("achievements.open %@", comment: ""), locale: Locale.current, status.litCaption)))
        .accessibilityAddTraits(.isButton)
    }

    private func chip(_ badge: Badge?) -> some View {
        LucideIcon(badge?.lucide ?? .circleQuestionMark, size: 16)
            .foregroundStyle(badge == nil ? PoulpyColor.onSurfaceVariant : PoulpyColor.onPrimaryContainer)
            .frame(width: 32, height: 32)
            .background(badge == nil ? PoulpyColor.surface : PoulpyColor.primaryContainer, in: Circle())
            .overlay(Circle().stroke(badge == nil ? PoulpyColor.outline : PoulpyColor.primary))
    }
}
