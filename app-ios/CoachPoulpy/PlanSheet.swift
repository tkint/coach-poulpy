import SwiftUI
import Shared

/// Tick the days to train a workout, untick to take one off: a month calendar from
/// Monday, past days dimmed, today outlined, planned days filled. Saving hands back
/// every ticked day; none keeps the workout unplanned.
struct PlanSheet: View {
    let name: String
    let onSave: ([PlanDate]) -> Void
    @State private var ticked: Set<DateComponents>
    @State private var month: Date
    @Environment(\.dismiss) private var dismiss

    private static var calendar: Calendar {
        var calendar = Calendar.current
        calendar.firstWeekday = 2
        return calendar
    }

    init(name: String, planned: [PlannedDay], onSave: @escaping ([PlanDate]) -> Void) {
        self.name = name
        self.onSave = onSave
        let days = planned.map { DateComponents(year: Int($0.date.year), month: Int($0.date.month), day: Int($0.date.day)) }
        _ticked = State(initialValue: Set(days))
        let first = days.compactMap { Self.calendar.date(from: $0) }.min() ?? Date()
        _month = State(initialValue: Self.calendar.dateInterval(of: .month, for: first)?.start ?? first)
    }

    private var today: DateComponents { Self.calendar.dateComponents([.year, .month, .day], from: Date()) }

    private func isPast(_ day: DateComponents) -> Bool {
        guard let date = Self.calendar.date(from: day), let now = Self.calendar.date(from: today) else { return false }
        return date < now
    }

    private var upcoming: [DateComponents] { ticked.filter { !isPast($0) } }

    var body: some View {
        let calendar = Self.calendar
        VStack(alignment: .leading, spacing: 12) {
            VStack(alignment: .leading, spacing: 2) {
                Text(String(format: NSLocalizedString("plan.title %@", comment: ""), name)).font(PoulpyFont.headline)
                Text("plan.hint").font(.poulpy(.subheadline)).foregroundStyle(PoulpyColor.onSurfaceVariant)
            }
            HStack {
                Button {
                    month = calendar.date(byAdding: .month, value: -1, to: month) ?? month
                } label: {
                    Image(systemName: "chevron.left").frame(width: 44, height: 44)
                }
                .disabled(month <= (calendar.dateInterval(of: .month, for: Date())?.start ?? Date()))
                .accessibilityLabel(Text("plan.previous.month"))
                Spacer()
                Text(month.formatted(.dateTime.month(.wide).year()))
                    .font(.poulpy(.body).weight(.heavy))
                Spacer()
                Button {
                    month = calendar.date(byAdding: .month, value: 1, to: month) ?? month
                } label: {
                    Image(systemName: "chevron.right").frame(width: 44, height: 44)
                }
                .accessibilityLabel(Text("plan.next.month"))
            }
            .foregroundStyle(PoulpyColor.onSurfaceVariant)
            let columns = Array(repeating: GridItem(.flexible(), spacing: 0), count: 7)
            LazyVGrid(columns: columns, spacing: 4) {
                ForEach(0..<7, id: \.self) { index in
                    Text(calendar.veryShortWeekdaySymbols[(index + calendar.firstWeekday - 1) % 7])
                        .font(PoulpyFont.label)
                        .foregroundStyle(PoulpyColor.onSurfaceVariant)
                }
                ForEach(cells(calendar), id: \.self) { cell in
                    if let day = cell.day {
                        dayCell(DateComponents(year: cell.year, month: cell.month, day: day))
                    } else {
                        Color.clear.frame(height: 40)
                    }
                }
            }
            Button {
                onSave(upcoming.map { PlanDate(year: Int32($0.year ?? 0), month: Int32($0.month ?? 1), day: Int32($0.day ?? 1)) })
                dismiss()
            } label: {
                Text(upcoming.isEmpty ? NSLocalizedString("plan.keep.unplanned", comment: "") : String.localizedStringWithFormat(NSLocalizedString("plan.save %lld", comment: ""), Int64(upcoming.count)))
                    .font(.poulpy(.body).weight(.heavy))
                    .frame(maxWidth: .infinity, minHeight: 52)
                    .foregroundStyle(PoulpyColor.onPrimary)
                    .background(PoulpyColor.primary, in: Capsule())
            }
            .buttonStyle(.plain)
        }
        .padding(20)
        .presentationDetents([.large])
    }

    /// The month's days after blanks up to its first weekday, Monday first.
    private func cells(_ calendar: Calendar) -> [DateComponents] {
        let parts = calendar.dateComponents([.year, .month], from: month)
        let count = calendar.range(of: .day, in: .month, for: month)?.count ?? 30
        let lead = (calendar.component(.weekday, from: month) - calendar.firstWeekday + 7) % 7
        let blanks = (0..<lead).map { DateComponents(year: parts.year, month: parts.month, day: nil, hour: $0) }
        return blanks + (1...count).map { DateComponents(year: parts.year, month: parts.month, day: $0) }
    }

    private func dayCell(_ day: DateComponents) -> some View {
        let past = isPast(day)
        let on = ticked.contains(day)
        let now = today
        let isToday = day.year == now.year && day.month == now.month && day.day == now.day
        let date = Self.calendar.date(from: day) ?? Date()
        return Button {
            if on { ticked.remove(day) } else { ticked.insert(day) }
        } label: {
            Text("\(day.day ?? 0)")
                .font(.poulpy(.body).weight(on || isToday ? .heavy : .bold))
                .monospacedDigit()
                .frame(width: 40, height: 40)
                .foregroundStyle(on ? PoulpyColor.onPrimary : past ? PoulpyColor.onSurfaceVariant.opacity(0.5) : isToday ? PoulpyColor.primary : PoulpyColor.onSurface)
                .background(on ? PoulpyColor.primary : Color.clear, in: Circle())
                .overlay(Circle().stroke(isToday && !on ? PoulpyColor.primary : Color.clear, lineWidth: 1.5))
        }
        .buttonStyle(.plain)
        .disabled(past)
        .accessibilityLabel(Text(date.formatted(date: .complete, time: .omitted)))
        .accessibilityAddTraits(on ? [.isSelected] : [])
    }
}
