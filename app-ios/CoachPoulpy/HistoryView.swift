import Charts
import SwiftUI
import Shared

struct HistoryView: View {
    @StateObject private var history: Observed<HistoryModel, HistoryState>

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
            List {
                if state.loaded && state.sessions.isEmpty {
                    VStack(spacing: 8) {
                        Text("history.empty.title").font(.poulpy(.title2))
                        Text("history.empty.body")
                            .foregroundStyle(.secondary)
                            .multilineTextAlignment(.center)
                    }
                    .frame(maxWidth: .infinity)
                    .padding(32)
                    .listRowBackground(Color.clear)
                } else if state.loaded {
                    Section {
                        HStack(alignment: .top) {
                            period("history.this.week", state.thisWeek)
                            Divider()
                            period("history.this.month", state.thisMonth)
                        }
                    }
                    Section {
                        weekChart(
                            title: "history.chart.minutes",
                            weeks: state.weeks,
                            value: { Double($0.activeMinutes) },
                            label: { String(Int($0)) },
                            color: .accentColor
                        )
                        if state.hasDistance {
                            weekChart(
                                title: "history.chart.distance",
                                weeks: state.weeks,
                                value: { $0.distanceKm },
                                label: { decimal($0, 1) },
                                color: .orange
                            )
                        }
                    }
                    Section("history.sessions.title") {
                        ForEach(state.sessions, id: \.id) { session in
                            NavigationLink {
                                SessionDetailView(session: session) { history.model.delete(sessionId: session.id) }
                            } label: {
                                sessionRow(session)
                            }
                        }
                    }
                }
            }
            .navigationTitle("tab.history")
        }
    }

    private func period(_ title: LocalizedStringKey, _ period: PeriodUi) -> some View {
        VStack(alignment: .leading, spacing: 2) {
            Text(title).font(.poulpy(.footnote).weight(.semibold)).foregroundStyle(Color.accentColor)
            Text(L("history.sessions %lld", Int64(period.sessions))).font(.poulpy(.title3).weight(.semibold))
            Text(L("history.minutes %lld", Int64(period.activeMinutes))).font(.poulpy(.subheadline))
            if period.distanceMeters > 0 {
                Text("\(decimal(period.distanceMeters / 1000, 1)) km").font(.poulpy(.subheadline))
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }

    /// One bar per week, the current one last and fully coloured.
    private func weekChart(
        title: LocalizedStringKey,
        weeks: [WeekBar],
        value: @escaping (WeekBar) -> Double,
        label: @escaping (Double) -> String,
        color: Color
    ) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(title).font(.poulpy(.subheadline).weight(.semibold))
            Chart(weeks, id: \.startMillis) { week in
                BarMark(x: .value("week", shortDate(week.startMillis)), y: .value("value", value(week)))
                    .foregroundStyle(week.isCurrent ? color : color.opacity(0.45))
                    .annotation(position: .top) {
                        if value(week) > 0 {
                            Text(label(value(week))).font(.poulpy(.caption2)).foregroundStyle(.secondary)
                        }
                    }
            }
            .chartYAxis(.hidden)
            .frame(height: 120)
        }
        .padding(.vertical, 4)
    }

    private func sessionRow(_ session: SessionRow) -> some View {
        HStack(spacing: 12) {
            Image(systemName: session.kind == .treadmill ? "figure.run" : "dumbbell")
                .foregroundStyle(Color.accentColor)
                .frame(width: 28)
            VStack(alignment: .leading, spacing: 2) {
                Text(session.title).font(.poulpy(.headline))
                Text("\(dateTime(session.startedAtMillis)) · \(headline(session))")
                    .font(.poulpy(.footnote))
                    .foregroundStyle(.secondary)
            }
        }
    }

    /// The one number that matters most for the row: distance for the treadmill, sets for a workout.
    private func headline(_ session: SessionRow) -> String {
        var parts = [clock(session.activeSeconds)]
        if let distance = session.distanceMeters { parts.append("\(decimal(distance.doubleValue / 1000, 2)) km") }
        if session.kind == .workout, let done = session.setsDone, let planned = session.setsPlanned {
            parts.append(L("history.sets %lld %lld", Int64(done.intValue), Int64(planned.intValue)))
        }
        return parts.joined(separator: " · ")
    }
}

struct SessionDetailView: View {
    let session: SessionRow
    let onDelete: () -> Void
    @Environment(\.dismiss) private var dismiss
    @State private var deleting = false

    var body: some View {
        List {
            Section {
                Text(dateTime(session.startedAtMillis)).font(.poulpy(.headline))
                if let type = session.typeLabel {
                    Text(type).foregroundStyle(.secondary)
                }
            }
            Section {
                LabeledContent(LocalizedStringKey("treadmill.summary.duration"), value: clock(session.activeSeconds))
                if session.kind == .workout, let done = session.setsDone {
                    LabeledContent(LocalizedStringKey("history.sets.done"), value: session.setsPlanned.map { "\(done.intValue) / \($0.intValue)" } ?? "\(done.intValue)")
                }
                if session.kind == .workout, let skipped = session.setsSkipped, skipped.intValue > 0 {
                    LabeledContent(LocalizedStringKey("history.sets.skipped"), value: "\(skipped.intValue)")
                }
                if let distance = session.distanceMeters {
                    LabeledContent(LocalizedStringKey("metric.distance"), value: "\(decimal(distance.doubleValue / 1000, 2)) km")
                }
                if let speed = session.averageSpeedKmh {
                    LabeledContent(LocalizedStringKey("treadmill.summary.average.speed"), value: "\(decimal(speed.doubleValue, 1)) km/h")
                }
                if let hr = session.averageHeartRateBpm {
                    LabeledContent(LocalizedStringKey("treadmill.summary.average.hr"), value: "\(hr.intValue) bpm")
                }
                if let hr = session.maxHeartRateBpm {
                    LabeledContent(LocalizedStringKey("treadmill.summary.max.hr"), value: "\(hr.intValue) bpm")
                }
                if let kcal = session.energyKcal {
                    LabeledContent(LocalizedStringKey("metric.energy"), value: "\(kcal.intValue) kcal")
                }
                if let steps = session.estimatedSteps {
                    LabeledContent(LocalizedStringKey("metric.steps"), value: "≈ \(steps.intValue)")
                }
            }
        }
        .navigationTitle(session.title)
        .toolbar {
            Button(role: .destructive) {
                deleting = true
            } label: {
                Label("action.delete", systemImage: "trash")
            }
        }
        .confirmationDialog("history.delete.title", isPresented: $deleting, titleVisibility: .visible) {
            Button("action.delete", role: .destructive) {
                onDelete()
                dismiss()
            }
        } message: {
            Text("history.delete.body")
        }
    }
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

/// Day and month in the user's order, such as "21/10" or "10/21".
private func shortDate(_ millis: Int64) -> String {
    date(millis).formatted(.dateTime.day().month(.defaultDigits))
}
