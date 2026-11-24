import Charts
import SwiftUI
import Shared

/// Chart colours from the History mockup. Ember stays for Poulpy's grumpy side, so the km chart is deep blue.
private enum ChartColor {
    static let minutesPast = Color(light: 0x7FCBF0, dark: 0x0C4A73)
    static let kmCurrent = Color(light: 0x0C4A73, dark: 0xCDEBFA)
    static let kmPast = Color(light: 0xB6C4D0, dark: 0x374555)
}

/// The History stats: this week and month, the weekly bar charts and the totals. Not shown by any screen yet; kept for the graphs
/// that come back, which may reuse it.
struct HistoryStatsView: View {
    let state: HistoryState
    @Environment(\.unitSystem) private var units

    var body: some View {
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
            .background(PoulpyColor.surface, in: RoundedRectangle(cornerRadius: PoulpyRadius.lg))
            .overlay(RoundedRectangle(cornerRadius: PoulpyRadius.lg).stroke(PoulpyColor.outline))
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
        .padding(.horizontal, PoulpySpace.lg)
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
        .background(highlighted ? PoulpyColor.primaryContainer : PoulpyColor.surface, in: RoundedRectangle(cornerRadius: PoulpyRadius.lg))
        .overlay(RoundedRectangle(cornerRadius: PoulpyRadius.lg).stroke(highlighted ? Color.clear : PoulpyColor.outline))
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
        .background(PoulpyColor.surface, in: RoundedRectangle(cornerRadius: PoulpyRadius.lg))
        .overlay(RoundedRectangle(cornerRadius: PoulpyRadius.lg).stroke(PoulpyColor.outline))
    }
}

/// A localized string with printf-style arguments, as in Localizable.strings.
private func L(_ key: String, _ args: CVarArg...) -> String {
    String(format: NSLocalizedString(key, comment: ""), locale: Locale.current, arguments: args)
}

private func decimal(_ value: Double, _ decimals: Int) -> String {
    value.formatted(.number.precision(.fractionLength(decimals)))
}

private func date(_ millis: Int64) -> Date { Date(timeIntervalSince1970: Double(millis) / 1000) }

/// "24 Oct", in the user's order.
private func shortDayMonth(_ millis: Int64) -> String {
    date(millis).formatted(.dateTime.day().month(.abbreviated))
}

/// Day and month in the user's order, such as "21/10" or "10/21".
private func shortDate(_ millis: Int64) -> String {
    date(millis).formatted(.dateTime.day().month(.defaultDigits))
}
