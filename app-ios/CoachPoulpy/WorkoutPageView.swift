import SwiftUI
import Shared
import UniformTypeIdentifiers

extension UTType {
    /// The `.poulpyworkout` file, declared as an exported type in Info.plist.
    static let poulpyWorkout = UTType(exportedAs: "ch.coachpoulpy.workout", conformingTo: .json)
}

/// A workout as the share sheet takes it: the file is written only when a destination is picked.
private struct WorkoutFileItem: Transferable {
    let make: () -> WorkoutFileExport?

    static var transferRepresentation: some TransferRepresentation {
        FileRepresentation(exportedContentType: .poulpyWorkout) { item in
            guard let file = item.make() else { throw CocoaError(.fileNoSuchFile) }
            let folder = FileManager.default.temporaryDirectory.appendingPathComponent(UUID().uuidString, isDirectory: true)
            try FileManager.default.createDirectory(at: folder, withIntermediateDirectories: true)
            let url = folder.appendingPathComponent(file.fileName)
            try file.content.write(to: url, atomically: true, encoding: .utf8)
            return SentTransferredFile(url)
        }
    }
}

/// What to open over the workout page.
private enum Cover: Identifiable {
    case builder, player, copy

    var id: Self { self }
}

/// An exercise whose details sheet is open.
private struct DetailTarget: Identifiable {
    let slug: String
    var id: String { slug }
}

/// One saved workout: the muscles it works on a body map, and over it a sheet you drag up for its exercises,
/// how it went before, the days it is planned for, and Start.
struct WorkoutPageView: View {
    let graph: AppGraph
    let workoutId: String
    @StateObject private var page: Observed<WorkoutPageModel, WorkoutPageState>
    @State private var cover: Cover?
    @State private var picking = false
    @State private var deleting = false
    /// The exercise whose details are open; nil when none.
    @State private var detailing: DetailTarget?
    private let figure: BodyFigure
    @Environment(\.dismiss) private var dismiss

    init(graph: AppGraph, workoutId: String) {
        self.graph = graph
        self.workoutId = workoutId
        let memory = UserDefaultsStore()
        figure = (graph.profile(memory: memory).ui.value as! ProfileUi).bodyFigure ?? .shoulders
        // Built once: StateObject evaluates this only for the first init of the view.
        _page = StateObject(wrappedValue: {
            let model = graph.workoutPage(workoutId: workoutId, languageCode: Language.code, memory: memory)
            return Observed(model, current: model.current, watch: model.watch, close: { $0.close() })
        }())
    }


    var body: some View {
        let state = page.value
        ZStack {
            PoulpyColor.background.ignoresSafeArea()
            if state.found {
                DetailSheet(
                    showBackgroundLabel: "workout.showMap",
                    showDetailsLabel: "workout.showExercises",
                    background: { room, safeTop in
                        HeatMapPanel(
                            figure: figure, heat: state.heat, hurting: Set(state.painAreas),
                            description: mapDescription(state), room: room, safeTop: safeTop
                        )
                    },
                    floating: { floatingButtons(state) },
                    actions: { actions(state) },
                    content: { details(state) }
                )
            }
        }
        .navigationBarBackButtonHidden(true)
        .toolbar(.hidden, for: .navigationBar)
        .onChange(of: state.loaded && !state.found) { gone in
            // Deleted here or elsewhere: nothing left to show.
            if gone { dismiss() }
        }
        .sheet(item: $detailing) { detail in
            ExerciseInfoSheet(graph: graph, slug: detail.slug, adding: false, closeLabel: "action.close", onAction: { _ in detailing = nil })
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

    /// What VoiceOver reads for the map: the muscles worked, and the pain note when there is one.
    private func mapDescription(_ state: WorkoutPageState) -> String {
        let mapText = L("workout.mapLabel %@", state.muscles.map(\.label).joined(separator: ", "))
        return state.painNote.isEmpty ? mapText : "\(mapText). \(state.painNote)"
    }

    /// A round filled button over the map: back, and the menu of share, copy and delete.
    private func floatingButtons(_ state: WorkoutPageState) -> some View {
        HStack {
            Button { dismiss() } label: {
                Image(systemName: "chevron.left").font(.system(size: 17, weight: .bold)).modifier(FloatingButtonLook())
            }
            .accessibilityLabel("action.back")
            Spacer()
            Menu {
                ShareLink(
                    item: WorkoutFileItem(make: { page.model.exportFile() }),
                    preview: SharePreview(state.name)
                ) {
                    Label("action.share", systemImage: "square.and.arrow.up")
                }
                if state.editable {
                    Button { cover = .copy } label: { Label("action.copy", systemImage: "doc.on.doc") }
                }
                Divider()
                Button(role: .destructive) { deleting = true } label: { Label("action.delete", systemImage: "trash") }
            } label: {
                Image(systemName: "ellipsis").font(.system(size: 17, weight: .bold)).modifier(FloatingButtonLook())
            }
            .accessibilityLabel("action.more")
        }
    }

    /// What the sheet holds, top to bottom: name and details, badges, Poulpy's pain note, planned days, stats, exercises, muscles.
    private func details(_ state: WorkoutPageState) -> some View {
        let equipment: [String] = state.equipment
        return VStack(alignment: .leading, spacing: 12) {
            VStack(alignment: .leading, spacing: 2) {
                Text(state.name).font(PoulpyFont.title)
                Text(state.details).font(.poulpy(.subheadline)).foregroundStyle(PoulpyColor.onSurfaceVariant)
            }
            FlowLayout(spacing: 6) {
                badge(state.formatLabel, icon: "bolt.fill", primary: true)
                ForEach(equipment, id: \.self) { badge($0, icon: "dumbbell.fill", primary: false) }
            }
            if !state.painNote.isEmpty {
                HStack(spacing: 10) {
                    Image(PoulpyDrawing.moodRecovering.imageName).resizable().scaledToFit().frame(width: 44, height: 44).accessibilityHidden(true)
                    Text(state.painNote).font(.poulpy(.subheadline).weight(.bold)).foregroundStyle(PoulpyColor.onPrimaryContainer)
                    Spacer(minLength: 0)
                }
                .padding(EdgeInsets(top: 10, leading: 10, bottom: 10, trailing: PoulpySpace.md))
                .background(PoulpyColor.primaryContainer, in: RoundedRectangle(cornerRadius: PoulpyRadius.lg))
            }
            planRow(state)
            stats(state)
            sectionLabel("workout.exercises")
            ForEach(state.parts, id: \.key) { part in
                partHeader(part)
                ForEach(part.steps, id: \.key) { step in
                    if let row = step.row {
                        structureRow(row)
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
                                structureRow(row)
                            }
                        }
                        .padding(10)
                        .background(PoulpyColor.primaryContainer, in: RoundedRectangle(cornerRadius: PoulpyRadius.lg))
                    }
                }
            }
            if !state.muscles.isEmpty {
                sectionLabel("workout.musclesWorked")
                VStack(spacing: 10) {
                    ForEach(state.muscles, id: \.label) { muscleRow($0) }
                }
                .padding(.horizontal, 4)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(.bottom, PoulpySpace.lg)
    }

    private func sectionLabel(_ key: LocalizedStringKey) -> some View {
        Text(key)
            .textCase(.uppercase)
            .font(.poulpy(.caption).weight(.heavy))
            .foregroundStyle(PoulpyColor.onSurfaceVariant)
            .padding(.leading, 4)
            .padding(.top, 4)
    }

    /// The format (filled with the primary container) or one piece of equipment (outlined), as a small pill.
    private func badge(_ text: String, icon: String, primary: Bool) -> some View {
        HStack(spacing: 6) {
            Image(systemName: icon).font(.system(size: 13, weight: .bold)).accessibilityHidden(true)
            Text(text).font(.poulpy(.subheadline).weight(.heavy)).lineLimit(1)
        }
        .foregroundStyle(primary ? PoulpyColor.onPrimaryContainer : PoulpyColor.onSurface)
        .padding(.leading, 10)
        .padding(.trailing, PoulpySpace.md)
        .frame(height: 30)
        .background(primary ? PoulpyColor.primaryContainer : PoulpyColor.surface, in: Capsule())
        .overlay(primary ? nil : Capsule().stroke(PoulpyColor.outline))
    }

    /// "Shoulders", a bar as long as its share of the busiest muscle in that muscle's heat, and "6 sets".
    private func muscleRow(_ load: MuscleLoad) -> some View {
        HStack(spacing: 10) {
            Text(load.label).font(.poulpy(.subheadline).weight(.heavy)).lineLimit(1)
                .foregroundStyle(PoulpyColor.onSurface)
                .frame(width: 92, alignment: .leading)
            GeometryReader { bar in
                Capsule().fill(PoulpyColor.surfaceVariant)
                    .overlay(alignment: .leading) {
                        Capsule().fill(heatFill(Int(load.heat)) ?? PoulpyColor.heat3)
                            .frame(width: bar.size.width * CGFloat(load.percent) / 100)
                    }
            }
            .frame(height: 10)
            Text(load.setsLabel).font(.poulpy(.subheadline).weight(.heavy)).monospacedDigit().lineLimit(1)
                .foregroundStyle(PoulpyColor.onSurfaceVariant)
                .frame(width: 52, alignment: .trailing)
        }
    }

    /// "3 sessions planned" and "Next on Fri 30 Oct" with "Change days"; a dashed button to plan it while it waits in the library.
    @ViewBuilder
    private func planRow(_ state: WorkoutPageState) -> some View {
        if let next = state.plans.first {
            HStack(spacing: 10) {
                Image(systemName: "calendar").font(.system(size: 18, weight: .semibold)).accessibilityHidden(true)
                VStack(alignment: .leading, spacing: 0) {
                    Text(String.localizedStringWithFormat(NSLocalizedString("workout.plannedSessions %lld", comment: ""), Int64(state.plans.count)))
                        .font(.poulpy(.subheadline).weight(.heavy))
                    Text(next.daysAway == 0 ? NSLocalizedString("workout.nextToday", comment: "") : L("workout.nextOn %@", dayLabel(next.dayMillis)))
                        .font(.poulpy(.subheadline).weight(.bold))
                        .monospacedDigit()
                }
                Spacer(minLength: 0)
                Button("workout.changeDays") { picking = true }
                    .font(.poulpy(.subheadline).weight(.heavy))
                    .foregroundStyle(PoulpyColor.onSurface)
                    .padding(.horizontal, PoulpySpace.md)
                    .frame(minHeight: 40)
                    .background(PoulpyColor.surface, in: Capsule())
                    .buttonStyle(.plain)
            }
            .foregroundStyle(PoulpyColor.onPrimaryContainer)
            .padding(EdgeInsets(top: 10, leading: PoulpySpace.md, bottom: 10, trailing: 10))
            .background(PoulpyColor.primaryContainer, in: RoundedRectangle(cornerRadius: PoulpyRadius.lg))
        } else {
            Button {
                picking = true
            } label: {
                Label("workout.plan", systemImage: "calendar.badge.plus")
                    .font(.poulpy(.body).weight(.heavy))
                    .frame(maxWidth: .infinity, minHeight: 48)
                    .foregroundStyle(PoulpyColor.primary)
                    .overlay(
                        RoundedRectangle(cornerRadius: PoulpyRadius.lg)
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
        .background(PoulpyColor.surface, in: RoundedRectangle(cornerRadius: PoulpyRadius.lg))
        .overlay(RoundedRectangle(cornerRadius: PoulpyRadius.lg).stroke(PoulpyColor.outline))
    }

    /// "Warm-up", "2 steps · about 6 min" beside the part's icon, as the builder heads a part.
    private func partHeader(_ part: WorkoutPagePart) -> some View {
        HStack(spacing: 10) {
            PartBadge(kind: part.kind)
            VStack(alignment: .leading, spacing: 0) {
                Text(part.name).font(.poulpy(.title3).weight(.heavy)).foregroundStyle(PoulpyColor.onSurface)
                Text(part.summary).font(.poulpy(.footnote)).monospacedDigit().foregroundStyle(PoulpyColor.onSurfaceVariant)
            }
        }
        .padding(.leading, 2)
        .padding(.top, 8)
    }

    /// An exercise opens its details sheet; a rest shows a timer and opens nothing.
    @ViewBuilder
    private func structureRow(_ row: WorkoutStepRow) -> some View {
        if row.isRest {
            stepRow(row)
        } else {
            Button {
                detailing = DetailTarget(slug: row.slug)
            } label: {
                stepRow(row)
            }
            .buttonStyle(.plain)
        }
    }

    private func stepRow(_ step: WorkoutStepRow) -> some View {
        HStack(spacing: 12) {
            if step.isRest {
                Image(systemName: "timer").font(.system(size: 16, weight: .bold)).foregroundStyle(PoulpyColor.onSurfaceVariant)
                    .frame(width: 40, height: 40)
                    .background(PoulpyColor.surfaceVariant, in: Circle())
                    .accessibilityHidden(true)
            } else {
                PatternBadge(pattern: step.pattern, size: 40)
            }
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
        .padding(EdgeInsets(top: 8, leading: 12, bottom: 8, trailing: 12))
        .frame(minHeight: 56)
        .background(PoulpyColor.surface, in: RoundedRectangle(cornerRadius: PoulpyRadius.lg))
        .overlay(RoundedRectangle(cornerRadius: PoulpyRadius.lg).stroke(PoulpyColor.outline))
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
        .padding(.horizontal, PoulpySpace.lg)
        .padding(.vertical, 12)
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
/// A part's icon in its round badge, filled for work, as on the workout page and in the builder.
struct PartBadge: View {
    let kind: PartKind

    var body: some View {
        let work = kind == .work
        Image(systemName: partIcon(kind))
            .font(.system(size: 16, weight: .bold))
            .foregroundStyle(work ? PoulpyColor.onPrimary : PoulpyColor.onPrimaryContainer)
            .frame(width: 36, height: 36)
            .background(work ? PoulpyColor.primary : PoulpyColor.primaryContainer, in: Circle())
            .accessibilityHidden(true)
    }
}

/// The icon of a format: a loop for a circuit, a list for sets.
func formatIcon(_ format: WorkoutFormat) -> String {
    format == .circuit ? "repeat" : "list.bullet"
}

func partIcon(_ kind: PartKind) -> String {
    switch kind {
    case .warmUp: return "sun.max"
    case .coolDown: return "moon"
    default: return "bolt.fill"
    }
}

/// The round, outlined look of the buttons floating over the map.
struct FloatingButtonLook: ViewModifier {
    func body(content: Content) -> some View {
        content
            .foregroundStyle(PoulpyColor.onSurface)
            .frame(width: 40, height: 40)
            .background(PoulpyColor.surface, in: Circle())
            .overlay(Circle().stroke(PoulpyColor.outline))
    }
}
