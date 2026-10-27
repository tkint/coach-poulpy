import SwiftUI
import Shared

/// What to open over the workout page.
private enum Cover: Identifiable {
    case builder, player, copy

    var id: Self { self }
}

/// One saved workout: its exercises, how it went before, the day it is planned for, and Start.
struct WorkoutPageView: View {
    let graph: AppGraph
    let workoutId: String
    @StateObject private var page: Observed<WorkoutPageModel, WorkoutPageState>
    @State private var cover: Cover?
    @State private var picking = false
    @State private var deleting = false
    @Environment(\.dismiss) private var dismiss

    init(graph: AppGraph, workoutId: String) {
        self.graph = graph
        self.workoutId = workoutId
        // Built once: StateObject evaluates this only for the first init of the view.
        _page = StateObject(wrappedValue: {
            let model = graph.workoutPage(workoutId: workoutId, languageCode: Language.code)
            return Observed(model, current: model.current, watch: model.watch, close: { $0.close() })
        }())
    }

    var body: some View {
        let state = page.value
        ScrollView {
            if state.found {
                VStack(alignment: .leading, spacing: 12) {
                    VStack(alignment: .leading, spacing: 6) {
                        Text(state.name).font(PoulpyFont.title)
                        // The format chip leads the details line.
                        HStack(spacing: 8) {
                            Text(state.formatLabel)
                                .font(.poulpy(.subheadline).weight(.heavy))
                                .foregroundStyle(PoulpyColor.onPrimaryContainer)
                                .padding(EdgeInsets(top: 4, leading: 10, bottom: 4, trailing: 10))
                                .background(PoulpyColor.primaryContainer, in: Capsule())
                            Text(state.details).font(.poulpy(.callout)).foregroundStyle(PoulpyColor.onSurface)
                        }
                    }
                    planRow(state)
                    stats(state)
                    let numbers = exerciseNumbers(state)
                    ForEach(state.parts, id: \.key) { part in
                        partHeader(part)
                        ForEach(part.steps, id: \.key) { step in
                            if let row = step.row {
                                structureRow(row, number: numbers[row.key])
                            } else if let group = step.repeatGroup {
                                VStack(alignment: .leading, spacing: 6) {
                                    HStack(spacing: 8) {
                                        Image(systemName: "repeat").font(.system(size: 16, weight: .bold)).foregroundStyle(PoulpyColor.primary)
                                        Text("\(NSLocalizedString("builder.repeat", comment: "")) · \(group.timesLabel)")
                                            .font(.poulpy(.headline))
                                            .foregroundStyle(PoulpyColor.onPrimaryContainer)
                                    }
                                    .padding(EdgeInsets(top: 2, leading: 4, bottom: 2, trailing: 4))
                                    ForEach(group.rows, id: \.key) { row in
                                        structureRow(row, number: numbers[row.key])
                                    }
                                }
                                .padding(10)
                                .background(PoulpyColor.primaryContainer, in: RoundedRectangle(cornerRadius: 16))
                            }
                        }
                    }
                }
                .padding(.horizontal, 20)
                .padding(.top, 12)
                .padding(.bottom, PoulpySpace.lg)
            }
        }
        .background(PoulpyColor.background)
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            if state.found {
                Menu {
                    if state.editable {
                        Button { cover = .copy } label: { Label("action.copy", systemImage: "doc.on.doc") }
                        Divider()
                    }
                    Button(role: .destructive) { deleting = true } label: { Label("action.delete", systemImage: "trash") }
                } label: {
                    Label("action.more", systemImage: "ellipsis.circle")
                }
            }
        }
        .safeAreaInset(edge: .bottom) {
            if state.found { actions(state) }
        }
        .onChange(of: state.loaded && !state.found) { gone in
            // Deleted here or elsewhere: nothing left to show.
            if gone { dismiss() }
        }
        .sheet(isPresented: $picking) {
            PlanSheet(name: state.name, planned: state.plans) { days in page.model.setDays(days: days) }
        }
        .alert(state.deletePrompt.title, isPresented: $deleting) {
            Button("action.cancel", role: .cancel) {}
            Button("action.delete", role: .destructive) { page.model.delete() }
        } message: {
            Text(state.deletePrompt.message)
        }
        .fullScreenCover(item: $cover) { cover in
            switch cover {
            case .builder: BuilderView(graph: graph, workoutId: workoutId)
            case .player: PlayerView(graph: graph, workoutId: workoutId)
            case .copy: BuilderView(graph: graph, workoutId: nil, copyOf: workoutId)
            }
        }
    }

    private func plannedTitle(_ plans: [PlannedDay]) -> String {
        if plans.count > 1 {
            return String.localizedStringWithFormat(NSLocalizedString("workout.planned.days %lld", comment: ""), Int64(plans.count))
        }
        let plan = plans[0]
        return plan.daysAway == 0 ? NSLocalizedString("workout.planned.today", comment: "") : L("workout.planned %@", dayLabel(plan.dayMillis))
    }

    /// "Planned for Tue 27 Oct", or "Planned on 3 days" with each upcoming day as a pill, and
    /// "Change days"; a dashed button to plan it while it waits in the library.
    @ViewBuilder
    private func planRow(_ state: WorkoutPageState) -> some View {
        let plans = state.plans
        if !plans.isEmpty {
            VStack(alignment: .leading, spacing: 8) {
                HStack(spacing: 10) {
                    Image(systemName: "calendar").accessibilityHidden(true)
                    Text(plannedTitle(plans))
                        .font(.poulpy(.subheadline).weight(.heavy))
                    Spacer(minLength: 0)
                    Button("workout.change.days") { picking = true }
                        .font(.poulpy(.subheadline).weight(.heavy))
                        .padding(.horizontal, 14)
                        .frame(minHeight: 40)
                        .background(PoulpyColor.surface, in: Capsule())
                        .buttonStyle(.plain)
                }
                if plans.count > 1 {
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 6) {
                            ForEach(plans, id: \.planId) { plan in
                                Text(plan.daysAway == 0 ? NSLocalizedString("home.today", comment: "") : dayLabel(plan.dayMillis))
                                    .font(.poulpy(.subheadline).weight(.heavy))
                                    .padding(.horizontal, 12)
                                    .frame(height: 32)
                                    .background(PoulpyColor.surface, in: Capsule())
                            }
                        }
                    }
                }
            }
            .foregroundStyle(PoulpyColor.onPrimaryContainer)
            .padding(EdgeInsets(top: 6, leading: 14, bottom: 10, trailing: 6))
            .background(PoulpyColor.primaryContainer, in: RoundedRectangle(cornerRadius: 16))
        } else {
            Button {
                picking = true
            } label: {
                Label("workout.plan", systemImage: "calendar.badge.plus")
                    .font(.poulpy(.body).weight(.heavy))
                    .frame(maxWidth: .infinity, minHeight: 48)
                    .foregroundStyle(PoulpyColor.primary)
                    .overlay(
                        RoundedRectangle(cornerRadius: 16)
                            .stroke(PoulpyPalette.lagoon300, style: StrokeStyle(lineWidth: 2, dash: [6, 4]))
                    )
            }
            .buttonStyle(.plain)
        }
    }

    private func stats(_ state: WorkoutPageState) -> some View {
        HStack(spacing: 8) {
            statBox("workout.done", L("workout.times %lld", Int64(state.timesDone)))
            statBox("workout.last", state.lastDoneMillis.map { shortDay($0.int64Value) } ?? "–")
            statBox("workout.usually", state.usualMinutes.map { L("workout.minutes %lld", Int64($0.intValue)) } ?? "–")
        }
    }

    private func statBox(_ label: LocalizedStringKey, _ value: String) -> some View {
        VStack(alignment: .leading, spacing: 0) {
            Text(label).textCase(.uppercase).font(.poulpy(.caption).weight(.heavy)).foregroundStyle(PoulpyColor.onSurfaceVariant).lineLimit(1)
            Text(value).font(.poulpy(.title3)).monospacedDigit().lineLimit(1).foregroundStyle(PoulpyColor.onSurface)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(EdgeInsets(top: 8, leading: 10, bottom: 8, trailing: 10))
        .background(PoulpyColor.surface, in: RoundedRectangle(cornerRadius: 14))
        .overlay(RoundedRectangle(cornerRadius: 14).stroke(PoulpyColor.outline))
    }

    /// "Warm-up", "2 steps · about 6 min" beside the part's icon, as the builder heads a part.
    private func partHeader(_ part: WorkoutPagePart) -> some View {
        let work = part.kind == .work
        return HStack(spacing: 10) {
            Image(systemName: partIcon(part.kind))
                .font(.system(size: 16, weight: .bold))
                .foregroundStyle(work ? PoulpyColor.onPrimary : PoulpyColor.onPrimaryContainer)
                .frame(width: 36, height: 36)
                .background(work ? PoulpyColor.primary : PoulpyColor.primaryContainer, in: Circle())
                .accessibilityHidden(true)
            VStack(alignment: .leading, spacing: 0) {
                Text(part.name).font(.poulpy(.title3).weight(.heavy)).foregroundStyle(PoulpyColor.onSurface)
                Text(part.summary).font(.poulpy(.footnote)).monospacedDigit().foregroundStyle(PoulpyColor.onSurfaceVariant)
            }
        }
        .padding(.leading, 2)
        .padding(.top, 8)
    }

    /// Exercises are numbered in order, through parts and repeats; rests aren't.
    private func exerciseNumbers(_ state: WorkoutPageState) -> [String: Int] {
        let steps = state.parts.flatMap { $0.steps }
        let rows = steps.flatMap { step in step.row.map { [$0] } ?? step.repeatGroup?.rows ?? [] }
        var numbers: [String: Int] = [:]
        for row in rows where !row.isRest { numbers[row.key] = numbers.count + 1 }
        return numbers
    }

    /// An exercise opens its page; a rest shows a timer and opens nothing.
    @ViewBuilder
    private func structureRow(_ row: WorkoutStepRow, number: Int?) -> some View {
        if row.isRest {
            stepRow(row, number: nil)
        } else {
            NavigationLink {
                ExerciseDetailView(graph: graph, slug: row.slug)
            } label: {
                stepRow(row, number: number)
            }
            .buttonStyle(.plain)
        }
    }

    private func stepRow(_ step: WorkoutStepRow, number: Int?) -> some View {
        HStack(spacing: 12) {
            Group {
                if let number {
                    Text("\(number)").font(.poulpy(.title3)).foregroundStyle(PoulpyColor.onPrimaryContainer)
                } else {
                    Image(systemName: "timer").font(.system(size: 15, weight: .bold)).foregroundStyle(PoulpyColor.onSurfaceVariant)
                }
            }
            .frame(width: 32, height: 32)
            .background(step.isRest ? PoulpyColor.surfaceVariant : PoulpyColor.primaryContainer, in: Circle())
            .accessibilityHidden(true)
            VStack(alignment: .leading, spacing: 1) {
                if let role = step.roleLabel {
                    Text(role)
                        .textCase(.uppercase)
                        .font(.custom(PoulpyFontFace.nunitoExtraBold, size: 11.5, relativeTo: .caption2))
                        .kerning(0.9)
                        .foregroundStyle(PoulpyColor.primary)
                }
                Text(step.name).font(.poulpy(.headline)).foregroundStyle(PoulpyColor.onSurface)
                Text(step.prescription).font(.poulpy(.subheadline)).monospacedDigit().foregroundStyle(PoulpyColor.onSurfaceVariant)
            }
            Spacer(minLength: 0)
            if !step.isRest {
                Image(systemName: "chevron.right").foregroundStyle(PoulpyColor.onSurfaceVariant)
            }
        }
        .padding(EdgeInsets(top: 10, leading: 12, bottom: 10, trailing: 12))
        .background(PoulpyColor.surface, in: RoundedRectangle(cornerRadius: 16))
    }

    private func actions(_ state: WorkoutPageState) -> some View {
        HStack(spacing: 10) {
            if state.editable {
                Button {
                    cover = .builder
                } label: {
                    Label("action.edit", systemImage: "pencil")
                        .font(.poulpy(.body).weight(.heavy))
                        .frame(maxWidth: .infinity, minHeight: 52)
                        .foregroundStyle(PoulpyColor.onSurface)
                        .overlay(Capsule().stroke(PoulpyColor.outline, lineWidth: 2))
                }
                .buttonStyle(.plain)
            }
            Button {
                cover = .player
            } label: {
                Label("workout.start", systemImage: "play.fill")
                    .font(.poulpy(.body).weight(.heavy))
                    .frame(maxWidth: .infinity, minHeight: 52)
                    .foregroundStyle(PoulpyColor.onPrimary)
                    .background(PoulpyColor.primary, in: Capsule())
            }
            .buttonStyle(.plain)
            .layoutPriority(1)
        }
        .padding(.horizontal, 20)
        .padding(.vertical, 12)
        .background(PoulpyColor.surface.ignoresSafeArea())
        .overlay(alignment: .top) { Divider() }
    }
}

/// A localized string with printf-style arguments, as in Localizable.strings.
private func L(_ key: String, _ args: CVarArg...) -> String {
    String(format: NSLocalizedString(key, comment: ""), locale: Locale.current, arguments: args)
}

/// "Tue 27 Oct", in the user's order.
private func dayLabel(_ millis: Int64) -> String {
    Date(timeIntervalSince1970: Double(millis) / 1000).formatted(.dateTime.weekday(.abbreviated).day().month(.abbreviated))
}

/// "21 Oct", in the user's order.
private func shortDay(_ millis: Int64) -> String {
    Date(timeIntervalSince1970: Double(millis) / 1000).formatted(.dateTime.day().month(.abbreviated))
}

/// The icon of a part, as the builder adds it: the sun for a warm-up, a bolt for work, the moon for a cool down.
func partIcon(_ kind: PartKind) -> String {
    switch kind {
    case .warmUp: return "sun.max"
    case .coolDown: return "moon"
    default: return "bolt.fill"
    }
}
