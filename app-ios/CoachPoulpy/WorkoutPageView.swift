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
                    VStack(alignment: .leading, spacing: 2) {
                        Text(state.typeLabel.uppercased()).font(PoulpyFont.label).kerning(1).foregroundStyle(PoulpyColor.primary)
                        Text(state.name).font(PoulpyFont.title)
                        Text(state.details).font(.poulpy(.callout)).foregroundStyle(PoulpyColor.onSurface)
                    }
                    planRow(state)
                    stats(state)
                    Text("workout.exercises")
                        .textCase(.uppercase)
                        .font(PoulpyFont.label)
                        .kerning(1)
                        .foregroundStyle(PoulpyColor.onSurfaceVariant)
                        .padding(.leading, 4)
                        .padding(.top, 4)
                    ForEach(Array(state.steps.enumerated()), id: \.element.key) { index, step in
                        NavigationLink {
                            ExerciseDetailView(graph: graph, slug: step.slug)
                        } label: {
                            stepRow(index: index, step: step)
                        }
                        .buttonStyle(.plain)
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

    private func stepRow(index: Int, step: WorkoutStepRow) -> some View {
        HStack(spacing: 12) {
            Text("\(index + 1)")
                .font(.poulpy(.title3))
                .foregroundStyle(PoulpyColor.onPrimaryContainer)
                .frame(width: 32, height: 32)
                .background(PoulpyColor.primaryContainer, in: Circle())
                .accessibilityHidden(true)
            VStack(alignment: .leading, spacing: 1) {
                Text(step.name).font(.poulpy(.headline)).foregroundStyle(PoulpyColor.onSurface)
                Text(step.prescription).font(.poulpy(.subheadline)).monospacedDigit().foregroundStyle(PoulpyColor.onSurfaceVariant)
            }
            Spacer(minLength: 0)
            Image(systemName: "chevron.right").foregroundStyle(PoulpyColor.onSurfaceVariant)
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
