import Charts
import SwiftUI
import Shared

private enum HistoryTab { case calendar, stats }

/// Chart colours from the History mockup. Ember stays for Poulpy's grumpy side, so the km chart is deep blue.
private enum ChartColor {
    static let minutesPast = Color(light: 0x7FCBF0, dark: 0x0C4A73)
    static let kmCurrent = Color(light: 0x0C4A73, dark: 0xCDEBFA)
    static let kmPast = Color(light: 0xB6C4D0, dark: 0x374555)
}

struct HistoryView: View {
    @StateObject private var history: Observed<HistoryModel, HistoryState>
    @State private var tab = HistoryTab.calendar
    @Environment(\.unitSystem) private var units
    /// 0 for this month, -1 for last month.
    @State private var offset: Int32 = 0
    /// This month opens on today; an earlier one opens with no day picked (0).
    @State private var selected: Int32 = todayDay

    init(graph: AppGraph) {
        // Built once: StateObject evaluates this only for the first init of the view.
        _history = StateObject(wrappedValue: {
            let model = graph.history(languageCode: Language.code)
            return Observed(model, current: model.current, watch: model.watch, close: { $0.close() })
        }())
    }

    var body: some View {
        let state = history.value
        NavigationStack {
            VStack(spacing: 0) {
                viewSwitch
                    .padding(.horizontal, 20)
                    .padding(.top, PoulpySpace.sm)
                if state.loaded {
                    switch tab {
                    case .calendar: calendarView(state)
                    case .stats: ScrollView { statsView(state) }
                    }
                }
                Spacer(minLength: 0)
            }
            .background(PoulpyColor.background)
            .navigationTitle("tab.history")
        }
    }

    // MARK: Calendar | Stats

    private var viewSwitch: some View {
        HStack(spacing: 0) {
            switchButton(.calendar, "history.viewCalendar", systemImage: "calendar")
            switchButton(.stats, "history.viewStats", systemImage: "chart.bar")
        }
        .padding(4)
        .background(PoulpyColor.surfaceVariant, in: Capsule())
    }

    private func switchButton(_ item: HistoryTab, _ title: LocalizedStringKey, systemImage: String) -> some View {
        let on = tab == item
        return Button {
            tab = item
        } label: {
            Label(title, systemImage: systemImage)
                .font(.poulpy(.subheadline).weight(.heavy))
                .frame(maxWidth: .infinity)
                .frame(height: 36)
                .foregroundStyle(on ? PoulpyColor.onPrimaryContainer : PoulpyColor.onSurfaceVariant)
                .background(on ? PoulpyColor.surface : Color.clear, in: Capsule())
                .shadow(color: on ? .black.opacity(0.12) : .clear, radius: 1.5, y: 1)
        }
        .buttonStyle(.plain)
        .accessibilityAddTraits(on ? [.isSelected] : [])
    }

    // MARK: Calendar

    private func calendarView(_ state: HistoryState) -> some View {
        let month = history.model.month(state: state, offset: offset, selectedDay: selected)
        return VStack(spacing: 0) {
            monthCard(month, state)
            ScrollViewReader { proxy in
                ScrollView {
                    LazyVStack(alignment: .leading, spacing: 8) {
                        if month.sections.isEmpty && state.sessions.isEmpty {
                            VStack(spacing: 8) {
                                Text("history.emptyTitle").font(.poulpy(.title2))
                                Text("history.emptyBody")
                                    .foregroundStyle(.secondary)
                                    .multilineTextAlignment(.center)
                            }
                            .frame(maxWidth: .infinity)
                            .padding(.top, 32)
                        }
                        ForEach(month.sections, id: \.day) { section in
                            let highlighted = section.day == selected
                            dayHeader(section, highlighted: highlighted).id(sectionId(month, section.day))
                            if section.sessions.isEmpty {
                                restingRow
                            } else {
                                ForEach(section.sessions, id: \.id) { session in
                                    NavigationLink {
                                        SessionDetailView(session: session) { history.model.delete(sessionId: session.id) }
                                    } label: {
                                        sessionCard(session, highlighted: highlighted)
                                    }
                                    .buttonStyle(.plain)
                                }
                            }
                        }
                    }
                    .padding(.horizontal, 20)
                    .padding(.vertical, 12)
                }
                .onChange(of: selected) { day in
                    withAnimation { proxy.scrollTo(sectionId(month, day), anchor: .top) }
                }
            }
        }
    }

    private func sectionId(_ month: CalendarMonth, _ day: Int32) -> String { "\(month.year)-\(month.month)-\(day)" }

    private func changeMonth(by: Int32, _ state: HistoryState) {
        let next = min(0, max(state.firstMonthOffset, offset + by))
        guard next != offset else { return }
        offset = next
        selected = next == 0 ? todayDay : 0
    }

    /// The month grid, Monday first; swipe or the arrows change month, never past this one.
    private func monthCard(_ month: CalendarMonth, _ state: HistoryState) -> some View {
        VStack(spacing: 4) {
            HStack {
                Button { changeMonth(by: -1, state) } label: {
                    Image(systemName: "chevron.left").font(.system(size: 18, weight: .bold)).frame(width: 44, height: 44)
                }
                .disabled(!month.canGoBack)
                .accessibilityLabel(Text("history.previousMonth"))
                Spacer()
                VStack(spacing: 0) {
                    Text(monthTitle(month.monthStartMillis)).font(.poulpy(.title3))
                    Text(L("history.activeDays %lld", Int64(month.activeDays)))
                        .font(.poulpy(.caption).weight(.bold))
                        .foregroundStyle(PoulpyColor.onSurfaceVariant)
                }
                Spacer()
                Button { changeMonth(by: 1, state) } label: {
                    Image(systemName: "chevron.right").font(.system(size: 18, weight: .bold)).frame(width: 44, height: 44)
                }
                .disabled(!month.canGoForward)
                .accessibilityLabel(Text("history.nextMonth"))
            }
            .foregroundStyle(PoulpyColor.primary)
            HStack(spacing: 0) {
                ForEach(Array(weekdaySymbols.enumerated()), id: \.offset) { _, symbol in
                    Text(symbol)
                        .font(.poulpy(.caption).weight(.heavy))
                        .foregroundStyle(PoulpyColor.onSurfaceVariant)
                        .frame(maxWidth: .infinity)
                }
            }
            .accessibilityHidden(true)
            LazyVGrid(columns: Array(repeating: GridItem(.flexible(), spacing: 0), count: 7), spacing: 2) {
                ForEach(0..<Int(month.leadingBlanks), id: \.self) { _ in
                    Color.clear.frame(height: 44)
                }
                ForEach(month.days, id: \.day) { day in
                    dayCell(month, day)
                }
            }
        }
        .padding(EdgeInsets(top: 10, leading: 12, bottom: 12, trailing: 12))
        .background(PoulpyColor.surface, in: RoundedRectangle(cornerRadius: 20))
        .overlay(RoundedRectangle(cornerRadius: 20).stroke(PoulpyColor.outline))
        .padding(.horizontal, 20)
        .padding(.top, 14)
        .gesture(
            DragGesture(minimumDistance: 30).onEnded { drag in
                if drag.translation.width > 80 { changeMonth(by: -1, state) }
                if drag.translation.width < -80 { changeMonth(by: 1, state) }
            }
        )
    }

    private func dayCell(_ month: CalendarMonth, _ day: CalendarDay) -> some View {
        let isSelected = day.day == selected
        let text: Color = day.active ? PoulpyColor.onPrimary : (day.isFuture ? PoulpyColor.onSurfaceVariant.opacity(0.45) : PoulpyColor.onSurface)
        let dot: Color = !day.isToday ? .clear : (day.active ? PoulpyColor.onPrimary : PoulpyColor.primary)
        var label = fullDate(month.monthStartMillis, day.day)
        if day.active { label += ", " + L("history.sessions %lld", Int64(day.sessionCount)) }
        return Button {
            selected = day.day
        } label: {
            VStack(spacing: 1) {
                Text("\(day.day)")
                    .font(.poulpy(.subheadline).weight(day.active ? .heavy : .bold))
                    .monospacedDigit()
                Circle().fill(dot).frame(width: 4, height: 4)
            }
            .foregroundStyle(text)
            .frame(width: 36, height: 36)
            .background(Circle().fill(day.active ? PoulpyColor.primary : Color.clear))
            .padding(3)
            .overlay(Circle().stroke(isSelected ? PoulpyColor.onPrimaryContainer : Color.clear, lineWidth: 2))
        }
        .buttonStyle(.plain)
        .disabled(day.isFuture)
        .frame(height: 44)
        .accessibilityLabel(label)
        .accessibilityAddTraits(isSelected ? [.isSelected] : [])
    }

    private func dayHeader(_ section: DaySection, highlighted: Bool) -> some View {
        let day = dayLabel(section.dateMillis)
        let text = section.isToday ? L("history.todayDay %@", day) : day
        return HStack(spacing: 8) {
            Text(text.uppercased())
                .font(PoulpyFont.label)
                .kerning(1)
                .foregroundStyle(highlighted ? PoulpyColor.primary : PoulpyColor.onSurfaceVariant)
            Rectangle().fill(PoulpyColor.outline).frame(height: 1)
        }
        .padding(.horizontal, 4)
        .padding(.top, 6)
    }

    /// A dashed box for a picked day without sessions.
    private var restingRow: some View {
        Text("history.resting")
            .font(.poulpy(.subheadline))
            .foregroundStyle(PoulpyColor.onSurfaceVariant)
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(.horizontal, 14)
            .padding(.vertical, 12)
            .overlay(
                RoundedRectangle(cornerRadius: 16)
                    .stroke(PoulpyColor.onSurfaceVariant.opacity(0.5), style: StrokeStyle(lineWidth: 1.5, dash: [6, 4]))
            )
    }

    private func sessionCard(_ session: SessionRow, highlighted: Bool) -> some View {
        HStack(spacing: 12) {
            Image(systemName: session.kind == .treadmill ? "figure.run" : "dumbbell")
                .font(.system(size: 18, weight: .semibold))
                .foregroundStyle(PoulpyColor.onPrimaryContainer)
                .frame(width: 44, height: 44)
                .background(PoulpyColor.primaryContainer, in: Circle())
            VStack(alignment: .leading, spacing: 2) {
                Text(session.title).font(.poulpy(.headline)).foregroundStyle(PoulpyColor.onSurface)
                Text("\(time(session.startedAtMillis)) · \(headline(session))")
                    .font(.poulpy(.subheadline))
                    .monospacedDigit()
                    .foregroundStyle(PoulpyColor.onSurfaceVariant)
            }
            Spacer(minLength: 0)
        }
        .padding(.horizontal, 14)
        .padding(.vertical, 12)
        .background(PoulpyColor.surface, in: RoundedRectangle(cornerRadius: 16))
        .overlay(RoundedRectangle(cornerRadius: 16).stroke(highlighted ? PoulpyColor.primary : PoulpyColor.outline, lineWidth: highlighted ? 2 : 1))
    }

    /// The numbers that matter most for the row: duration, then distance for the treadmill or sets for a workout.
    private func headline(_ session: SessionRow) -> String {
        var parts = [clock(session.activeSeconds)]
        if let distance = session.distanceMeters { parts.append(Units.shared.distance(meters: distance.doubleValue, system: units, decimals: 2).text) }
        if session.kind == .workout, let done = session.setsDone, let planned = session.setsPlanned {
            parts.append(L("history.sets %lld %lld", Int64(done.intValue), Int64(planned.intValue)))
        }
        return parts.joined(separator: " · ")
    }

    // MARK: Stats

    private func statsView(_ state: HistoryState) -> some View {
        VStack(spacing: 12) {
            HStack(alignment: .top, spacing: 12) {
                period("history.thisWeek", state.thisWeek, highlighted: true)
                period("history.thisMonth", state.thisMonth, highlighted: false)
            }
            VStack(alignment: .leading, spacing: 14) {
                weekChart(
                    title: "history.chartMinutes",
                    weeks: state.weeks,
                    value: { Double($0.activeMinutes) },
                    label: { String(Int($0)) },
                    current: PoulpyColor.primary,
                    past: ChartColor.minutesPast,
                    height: 104,
                    showWeeks: !state.hasDistance
                )
                if state.hasDistance {
                    weekChart(
                        title: "history.chartDistance",
                        weeks: state.weeks,
                        value: { $0.distanceKm },
                        label: { decimal($0, 1) },
                        current: ChartColor.kmCurrent,
                        past: ChartColor.kmPast,
                        height: 84,
                        showWeeks: true
                    )
                }
            }
            .padding(EdgeInsets(top: 14, leading: 16, bottom: 14, trailing: 16))
            .background(PoulpyColor.surface, in: RoundedRectangle(cornerRadius: 16))
            .overlay(RoundedRectangle(cornerRadius: 16).stroke(PoulpyColor.outline))
            HStack(alignment: .top, spacing: 12) {
                tile("history.total", value: "\(state.totalSessions)", caption: L("history.sessionsUnit %lld", Int64(state.totalSessions)))
                tile(
                    "history.longestRun",
                    value: state.longestRunMeters.map { Units.shared.distance(meters: $0.doubleValue, system: units, decimals: 1).text } ?? "–",
                    caption: state.longestRunMillis.map { shortDayMonth($0.int64Value) } ?? ""
                )
                tile("history.fullOctopus", value: "×\(state.fullOctopuses)", caption: L("history.tentaclesLit %lld", Int64(state.tentaclesLit)))
            }
        }
        .padding(.horizontal, 20)
        .padding(.vertical, 14)
    }

    private func period(_ title: LocalizedStringKey, _ period: PeriodUi, highlighted: Bool) -> some View {
        let content = highlighted ? PoulpyColor.onPrimaryContainer : PoulpyColor.onSurface
        var parts = [L("history.minutes %lld", Int64(period.activeMinutes))]
        if period.distanceMeters > 0 { parts.append(Units.shared.distance(meters: period.distanceMeters, system: units, decimals: 1).text) }
        return VStack(alignment: .leading, spacing: 2) {
            Text(title)
                .textCase(.uppercase)
                .font(PoulpyFont.label)
                .kerning(1)
                .foregroundStyle(highlighted ? content : PoulpyColor.primary)
            Text(L("history.sessions %lld", Int64(period.sessions))).font(PoulpyFont.title).foregroundStyle(content)
            Text(parts.joined(separator: " · ")).font(.poulpy(.subheadline).weight(.bold)).monospacedDigit().foregroundStyle(content)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(EdgeInsets(top: 14, leading: 16, bottom: 14, trailing: 16))
        .background(highlighted ? PoulpyColor.primaryContainer : PoulpyColor.surface, in: RoundedRectangle(cornerRadius: 16))
        .overlay(RoundedRectangle(cornerRadius: 16).stroke(highlighted ? Color.clear : PoulpyColor.outline))
    }

    /// One bar per week, the current one last in the stronger colour, the value above each bar.
    private func weekChart(
        title: LocalizedStringKey,
        weeks: [WeekBar],
        value: @escaping (WeekBar) -> Double,
        label: @escaping (Double) -> String,
        current: Color,
        past: Color,
        height: CGFloat,
        showWeeks: Bool
    ) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(title)
                .textCase(.uppercase)
                .font(PoulpyFont.label)
                .kerning(1)
                .foregroundStyle(PoulpyColor.onSurfaceVariant)
            Chart(weeks, id: \.startMillis) { week in
                BarMark(x: .value("week", shortDate(week.startMillis)), y: .value("value", value(week)))
                    .foregroundStyle(week.isCurrent ? current : past)
                    .cornerRadius(4)
                    .annotation(position: .top) {
                        if value(week) > 0 {
                            Text(label(value(week)))
                                .font(.poulpy(.caption2).weight(.heavy))
                                .foregroundStyle(PoulpyColor.onSurfaceVariant)
                        }
                    }
            }
            .chartYAxis(.hidden)
            .chartXAxis(showWeeks ? .automatic : .hidden)
            .frame(height: height)
        }
    }

    private func tile(_ title: LocalizedStringKey, value: String, caption: String) -> some View {
        VStack(alignment: .leading, spacing: 2) {
            Text(title).font(.poulpy(.caption).weight(.heavy)).foregroundStyle(PoulpyColor.onSurfaceVariant).lineLimit(1)
            Text(value).font(.poulpy(.title3)).monospacedDigit().lineLimit(1)
            Text(caption).font(.poulpy(.footnote).weight(.bold)).foregroundStyle(PoulpyColor.onSurfaceVariant).lineLimit(1)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(EdgeInsets(top: 12, leading: 14, bottom: 12, trailing: 14))
        .background(PoulpyColor.surface, in: RoundedRectangle(cornerRadius: 16))
        .overlay(RoundedRectangle(cornerRadius: 16).stroke(PoulpyColor.outline))
    }
}

struct SessionDetailView: View {
    let session: SessionRow
    let onDelete: () -> Void
    @Environment(\.dismiss) private var dismiss
    @State private var deleting = false
    @Environment(\.unitSystem) private var units

    var body: some View {
        ScrollView {
            VStack(spacing: 14) {
                HStack(spacing: 14) {
                    Image("PoulpyPoseExhausted")
                        .resizable()
                        .scaledToFit()
                        .frame(width: 96, height: 86)
                        .accessibilityHidden(true)
                    VStack(alignment: .leading, spacing: 2) {
                        if let type = session.formatLabel {
                            Text(type.uppercased()).font(PoulpyFont.label).kerning(1)
                        }
                        Text(dateTime(session.startedAtMillis)).font(PoulpyFont.headline)
                    }
                    Spacer(minLength: 0)
                }
                .foregroundStyle(PoulpyColor.onPrimaryContainer)
                .padding(EdgeInsets(top: 16, leading: 18, bottom: 16, trailing: 18))
                .background(PoulpyColor.primaryContainer, in: RoundedRectangle(cornerRadius: 24))

                VStack(spacing: 0) {
                    line("treadmill.summaryDuration", clock(session.activeSeconds))
                    if session.kind == .workout, let done = session.setsDone {
                        line("history.setsDone", session.setsPlanned.map { "\(done.intValue) / \($0.intValue)" } ?? "\(done.intValue)")
                    }
                    if session.kind == .workout, let skipped = session.setsSkipped, skipped.intValue > 0 {
                        line("history.setsSkipped", "\(skipped.intValue)")
                    }
                    if let distance = session.distanceMeters {
                        line("metric.distance", Units.shared.distance(meters: distance.doubleValue, system: units, decimals: 2).text)
                    }
                    if let speed = session.averageSpeedKmh {
                        line("treadmill.summaryAverageSpeed", Units.shared.speed(kmh: speed.doubleValue, system: units).text)
                    }
                    if let hr = session.averageHeartRateBpm {
                        line("treadmill.summaryAverageHr", "\(hr.intValue) bpm")
                    }
                    if let hr = session.maxHeartRateBpm {
                        line("treadmill.summaryMaxHr", "\(hr.intValue) bpm")
                    }
                    if let kcal = session.energyKcal {
                        line("metric.energy", "\(kcal.intValue) kcal")
                    }
                    if let steps = session.estimatedSteps {
                        line("metric.steps", "≈ \(steps.intValue)")
                    }
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 4)
                .background(PoulpyColor.surface, in: RoundedRectangle(cornerRadius: 16))
                .overlay(RoundedRectangle(cornerRadius: 16).stroke(PoulpyColor.outline))
            }
            .padding(.horizontal, 20)
            .padding(.vertical, 12)
        }
        .background(PoulpyColor.background)
        .navigationTitle(session.title)
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            Button(role: .destructive) {
                deleting = true
            } label: {
                Label("action.delete", systemImage: "trash")
            }
        }
        .confirmationDialog("history.deleteTitle", isPresented: $deleting, titleVisibility: .visible) {
            Button("action.delete", role: .destructive) {
                onDelete()
                dismiss()
            }
        } message: {
            Text("history.deleteBody")
        }
    }

    private func line(_ label: String, _ value: String) -> some View {
        HStack {
            Text(LocalizedStringKey(label)).font(.poulpy(.body).weight(.semibold)).foregroundStyle(PoulpyColor.onSurfaceVariant)
            Spacer()
            Text(value).font(.poulpy(.body).weight(.heavy)).monospacedDigit()
        }
        .padding(.vertical, 12)
    }
}

private let todayDay = Int32(Calendar.current.component(.day, from: Date()))

/// Monday first, as in the shared month grid: "M T W T F S S".
private var weekdaySymbols: [String] {
    let symbols = Calendar.current.veryShortStandaloneWeekdaySymbols
    return Array(symbols[1...] + symbols[..<1])
}

/// A localized string with printf-style arguments, as in Localizable.strings.
private func L(_ key: String, _ args: CVarArg...) -> String {
    String(format: NSLocalizedString(key, comment: ""), locale: Locale.current, arguments: args)
}

private func decimal(_ value: Double, _ decimals: Int) -> String {
    value.formatted(.number.precision(.fractionLength(decimals)))
}

private func clock(_ seconds: Int64) -> String {
    let h = seconds / 3600, m = (seconds % 3600) / 60, s = seconds % 60
    return h > 0 ? String(format: "%lld:%02lld:%02lld", h, m, s) : String(format: "%lld:%02lld", m, s)
}

private func date(_ millis: Int64) -> Date { Date(timeIntervalSince1970: Double(millis) / 1000) }

private func dateTime(_ millis: Int64) -> String {
    date(millis).formatted(date: .abbreviated, time: .shortened)
}

private func time(_ millis: Int64) -> String {
    date(millis).formatted(date: .omitted, time: .shortened)
}

/// "October 2026".
private func monthTitle(_ millis: Int64) -> String {
    date(millis).formatted(.dateTime.month(.wide).year())
}

/// "Sat 24 Oct", in the user's order.
private func dayLabel(_ millis: Int64) -> String {
    date(millis).formatted(.dateTime.weekday(.abbreviated).day().month(.abbreviated))
}

/// The whole date of [day] in the month starting at [monthStartMillis], for VoiceOver.
private func fullDate(_ monthStartMillis: Int64, _ day: Int32) -> String {
    let start = date(monthStartMillis)
    let target = Calendar.current.date(byAdding: .day, value: Int(day) - 1, to: start) ?? start
    return target.formatted(date: .complete, time: .omitted)
}

/// "24 Oct", in the user's order.
private func shortDayMonth(_ millis: Int64) -> String {
    date(millis).formatted(.dateTime.day().month(.abbreviated))
}

/// Day and month in the user's order, such as "21/10" or "10/21".
private func shortDate(_ millis: Int64) -> String {
    date(millis).formatted(.dateTime.day().month(.defaultDigits))
}
