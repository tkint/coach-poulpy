import SwiftUI
import UIKit
import Shared

struct BuilderView: View {
    let graph: AppGraph
    @StateObject private var builder: Observed<BuilderModel, BuilderState>
    /// Where the exercise being picked goes (a part, or one of its repeats) and as what; nil when not picking.
    @State private var picking: PickTarget?
    /// The exercise whose details are open over the picker; nil when none.
    @State private var previewing: PreviewTarget?
    @State private var addingPart = false
    @Environment(\.dismiss) private var dismiss

    /// [firstExercise] starts a new workout that already holds it, from an exercise page;
    /// [copyOf] starts an unsaved copy of that workout.
    init(graph: AppGraph, workoutId: String?, firstExercise: String? = nil, copyOf: String? = nil) {
        self.graph = graph
        _builder = StateObject(wrappedValue: {
            let language = Language.code
            let model: BuilderModel
            if let copyOf, let copy = graph.duplicateWorkout(workoutId: copyOf, languageCode: language) {
                model = copy
            } else if let workoutId, let edit = graph.editWorkout(workoutId: workoutId, languageCode: language) {
                model = edit
            } else if let firstExercise {
                model = graph.createWorkoutWith(exerciseSlug: firstExercise, languageCode: language)
            } else {
                model = graph.createWorkout(languageCode: language)
            }
            return Observed(model, current: model.current, watch: model.watch, close: { $0.close() })
        }())
    }

    private var model: BuilderModel { builder.model }
    private var state: BuilderState { builder.value }

    /// The step being edited in the sheet: the one just added, or the one tapped.
    @State private var editing: String?
    /// The step or repeat the chevrons moved last: the list scrolls just enough to show it.
    @State private var movedKey: String?

    var body: some View {
        NavigationStack {
            ScrollViewReader { scroller in
            ScrollView {
                VStack(alignment: .leading, spacing: 14) {
                    PoulpyTextField(label: "builder.name", text: Binding(get: { state.name }, set: { model.rename(name: $0) }))
                    if let format = state.format {
                        formatLine(format)
                        ForEach(state.parts, id: \.key) { part in
                            partBlock(part)
                        }
                        Button { addingPart = true } label: {
                            dashedTile(Text("builder.addPart"), systemImage: "plus", height: 56)
                        }
                        .buttonStyle(.plain)
                    }
                }
                .padding(.horizontal, 20)
                .padding(.top, 8)
                .padding(.bottom, PoulpySpace.xl)
            }
            .onChange(of: movedKey) { key in
                // Once the move has its new place, scroll only as far as it takes to show the step in full.
                guard let key else { return }
                DispatchQueue.main.async {
                    withAnimation(.easeInOut(duration: 0.25)) { scroller.scrollTo(key) }
                    movedKey = nil
                }
            }
            }
            .background(PoulpyColor.background)
            .navigationTitle(LocalizedStringKey(model.isEditing ? "builder.editTitle" : "builder.newTitle"))
            .navigationBarTitleDisplayMode(.inline)
            .toolbarBackground(PoulpyColor.background, for: .navigationBar)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button { dismiss() } label: {
                        Image(systemName: "xmark").font(.system(size: 17, weight: .bold))
                    }
                    .foregroundStyle(PoulpyColor.onSurface)
                    .accessibilityLabel(Text("action.close"))
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button {
                        if model.save() != nil { dismiss() }
                    } label: {
                        Text("action.save")
                            .font(.poulpy(.subheadline).weight(.heavy))
                            .padding(.horizontal, 18)
                            .frame(height: 36)
                            .foregroundStyle(PoulpyColor.onPrimary)
                            .background(PoulpyColor.primary.opacity(state.canSave ? 1 : 0.4), in: Capsule())
                    }
                    .buttonStyle(.plain)
                    .disabled(!state.canSave)
                }
            }
            .onChange(of: state.addedKey) { key in
                // A newly added step opens in the sheet, once the exercise list has gone.
                guard let key else { return }
                DispatchQueue.main.asyncAfter(deadline: .now() + 0.4) { editing = key }
            }
            .sheet(isPresented: Binding(get: { editingRow != nil }, set: { if !$0 { editing = nil } })) {
                if let row = editingRow {
                    StepSheet(graph: graph, model: model, row: row, parts: state.parts, format: state.format) { editing = nil }
                }
            }
            // A new workout asks for its format first; closing the sheet without one leaves the builder.
            .sheet(isPresented: Binding(get: { state.format == nil }, set: { if !$0 && state.format == nil { dismiss() } })) {
                FormatSheet(options: model.formatOptions) { model.chooseFormat(format: $0) }
            }
            .sheet(isPresented: $addingPart) {
                PartSheet(options: state.partOptions) { kind in
                    model.addPart(kind: kind)
                    addingPart = false
                }
            }
            .sheet(item: $picking) { target in
                NavigationStack {
                    ExerciseList(graph: graph, withRecent: true) { row in
                        PickerRowView(row: row, onOpen: { previewing = PreviewTarget(slug: row.slug) }) {
                            model.add(slug: row.slug, partKey: target.partKey, repeatKey: target.repeatKey, role: target.role)
                            picking = nil
                        }
                    }
                    .sheet(item: $previewing) { preview in
                        ExerciseInfoSheet(graph: graph, slug: preview.slug, onAction: { slug in
                            model.add(slug: slug, partKey: target.partKey, repeatKey: target.repeatKey, role: target.role)
                            previewing = nil
                            picking = nil
                        })
                    }
                    .navigationTitle("builder.pickTitle")
                    .navigationBarTitleDisplayMode(.inline)
                    .toolbar {
                        ToolbarItem(placement: .cancellationAction) {
                            Button("action.cancel") { picking = nil }
                        }
                    }
                }
            }
        }
    }

    private var editingRow: BuilderRow? { state.rows.first { $0.key == editing } }

    /// Under the name: the format as a chip, with a lock, since it was chosen at creation and stays.
    private func formatLine(_ format: WorkoutFormat) -> some View {
        HStack(spacing: 10) {
            fieldLabel("builder.format")
            Label(state.formatLabel, systemImage: formatIcon(format))
                .font(.poulpy(.subheadline).weight(.heavy))
                .foregroundStyle(PoulpyColor.onPrimaryContainer)
                .padding(.horizontal, 12)
                .padding(.vertical, 6)
                .background(PoulpyColor.primaryContainer, in: Capsule())
            Image(systemName: "lock.fill").font(.system(size: 13, weight: .bold)).foregroundStyle(PoulpyColor.onSurfaceVariant).accessibilityHidden(true)
            Text("builder.formatLocked").font(.poulpy(.caption)).foregroundStyle(PoulpyColor.onSurfaceVariant)
        }
    }

    /// A part: its header, its steps and repeats, and "Add to Work 1".
    private func partBlock(_ part: BuilderPart) -> some View {
        VStack(alignment: .leading, spacing: 12) {
            partHeader(part)
            ForEach(part.steps, id: \.key) { step in
                if let row = step.row {
                    card(row)
                } else if let group = step.repeatGroup {
                    repeatBlock(group, in: part.key).id(group.key)
                }
            }
            addStepButton(Text(part.addLabel), height: 56, part: part.key, repeatKey: nil)
        }
        .padding(.bottom, 8)
    }

    /// A part's header: its icon, name and summary, and a menu to move a work part or delete the part.
    private func partHeader(_ part: BuilderPart) -> some View {
        HStack(spacing: 10) {
            PartBadge(kind: part.kind)
            VStack(alignment: .leading, spacing: 0) {
                Text(part.name).font(.poulpy(.title3).weight(.heavy)).foregroundStyle(PoulpyColor.onSurface)
                Text(part.summary).font(.poulpy(.footnote)).monospacedDigit().foregroundStyle(PoulpyColor.onSurfaceVariant)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            if part.canMoveUp || part.canMoveDown || part.canDelete {
                Menu {
                    if part.canMoveUp {
                        Button { model.movePartUp(key: part.key) } label: { Label("builder.moveUp", systemImage: "arrow.up") }
                    }
                    if part.canMoveDown {
                        Button { model.movePartDown(key: part.key) } label: { Label("builder.moveDown", systemImage: "arrow.down") }
                    }
                    if part.canDelete {
                        Divider()
                        Button(role: .destructive) { model.removePart(key: part.key) } label: { Label("builder.delete", systemImage: "trash") }
                    }
                } label: {
                    Image(systemName: "ellipsis").font(.system(size: 17, weight: .bold)).frame(width: 44, height: 44)
                }
                .foregroundStyle(PoulpyColor.onSurfaceVariant)
                .accessibilityLabel(Text(L("builder.moreActions %@", part.name)))
            }
        }
    }

    private func card(_ row: BuilderRow) -> some View {
        EntryCard(
            model: model, row: row, roleOptions: model.roleOptions, parts: state.parts, open: false,
            onOpen: { editing = row.key },
            onMove: { move(row.key, by: $0) }
        )
        .id(row.key)
    }

    /// A repeat: its header with − N times +, its steps inside a tinted block, and a way to add one.
    private func repeatBlock(_ group: BuilderRepeat, in partKey: String) -> some View {
        VStack(alignment: .leading, spacing: 6) {
            HStack(spacing: 8) {
                MoveChevrons(
                    upLabel: Text("builder.moveRepeatUp"),
                    downLabel: Text("builder.moveRepeatDown"),
                    canMoveUp: group.canMoveUp,
                    canMoveDown: group.canMoveDown,
                    onMove: { move(group.key, by: $0) }
                )
                Image(systemName: "repeat").font(.system(size: 17, weight: .bold)).foregroundStyle(PoulpyColor.primary)
                Text("builder.repeat")
                    .font(.custom(PoulpyFontFace.baloo2Bold, size: 19, relativeTo: .headline))
                    .foregroundStyle(PoulpyColor.onPrimaryContainer)
                    .frame(maxWidth: .infinity, alignment: .leading)
                HStack(spacing: 4) {
                    timesButton("minus", "builder.repeatFewer") { model.setRepeatTimes(key: group.key, times: group.times - 1) }
                    Text(group.timesLabel)
                        .font(.poulpy(.subheadline).weight(.heavy))
                        .monospacedDigit()
                        .foregroundStyle(PoulpyColor.onPrimaryContainer)
                        .frame(minWidth: 58)
                    timesButton("plus", "builder.repeatMore") { model.setRepeatTimes(key: group.key, times: group.times + 1) }
                }
                .padding(4)
                .background(PoulpyColor.surface.opacity(0.6), in: Capsule())
                Menu {
                    Button { model.duplicate(key: group.key) } label: { Label("builder.duplicate", systemImage: "plus.square.on.square") }
                    moveToPartMenu(group.key, from: partKey, parts: state.parts, model: model)
                    Divider()
                    Button(role: .destructive) { model.remove(key: group.key) } label: { Label("builder.delete", systemImage: "trash") }
                } label: {
                    Image(systemName: "ellipsis").font(.system(size: 17, weight: .bold)).frame(width: 44, height: 44)
                }
                .foregroundStyle(PoulpyColor.onSurfaceVariant)
                .accessibilityLabel(Text("builder.repeatActions"))
            }
            .accessibilityElement(children: .contain)
            VStack(spacing: 6) {
                ForEach(group.rows, id: \.key) { row in
                    card(row)
                }
                addStepButton(Text("builder.addToRepeat"), height: 44, part: partKey, repeatKey: group.key)
            }
            .padding(.leading, 10)
        }
        .padding(EdgeInsets(top: 4, leading: 6, bottom: 10, trailing: 8))
        // The agreed `.builder-repeat` look: primary container mixed 55% with the background, plus a 1.5pt border.
        .background(PoulpyColor.background, in: RoundedRectangle(cornerRadius: 16))
        .background(PoulpyColor.primaryContainer.opacity(0.55), in: RoundedRectangle(cornerRadius: 16))
        .overlay(RoundedRectangle(cornerRadius: 16).strokeBorder(PoulpyColor.primaryContainer, lineWidth: 1.5))
    }

    private func timesButton(_ symbol: String, _ description: LocalizedStringKey, _ action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Image(systemName: symbol)
                .font(.system(size: 15, weight: .heavy))
                .foregroundStyle(PoulpyColor.primary)
                .frame(width: 36, height: 36)
                .background(PoulpyColor.surface, in: Circle())
        }
        .buttonStyle(.plain)
        .accessibilityLabel(Text(description))
    }

    /// "Add to Work 1": asks what to add. An exercise or a recovery is picked from the exercise
    /// list; a rest, or a repeat at the end of a part (none inside a repeat), is added right away.
    private func addStepButton(_ label: Text, height: CGFloat, part: String, repeatKey: String?) -> some View {
        Menu {
            Button { picking = PickTarget(partKey: part, repeatKey: repeatKey, role: .work) } label: { Label("builder.addExercise", systemImage: "dumbbell") }
            Button { picking = PickTarget(partKey: part, repeatKey: repeatKey, role: .recover) } label: { Label("builder.addRecover", systemImage: "figure.walk") }
            Button { model.addRest(partKey: part, repeatKey: repeatKey) } label: { Label("builder.addRest", systemImage: "timer") }
            if repeatKey == nil {
                Button { model.addRepeat(partKey: part) } label: { Label("builder.addRepeat", systemImage: "repeat") }
            }
        } label: {
            dashedTile(label, systemImage: "plus", height: height)
        }
        .buttonStyle(.plain)
    }

    private func dashedTile(_ label: Text, systemImage: String, height: CGFloat) -> some View {
        Label { label } icon: { Image(systemName: systemImage) }
            .font(.poulpy(height < 56 ? .subheadline : .body).weight(.heavy))
            .foregroundStyle(PoulpyColor.primary)
            .frame(maxWidth: .infinity, minHeight: height)
            .overlay(RoundedRectangle(cornerRadius: height < 56 ? 12 : 16).stroke(PoulpyPalette.lagoon300, style: StrokeStyle(lineWidth: 2, dash: [8, 6])))
            .contentShape(RoundedRectangle(cornerRadius: 16))
    }

    /// Moves a step or a repeat one place up (-1) or down (1) among its siblings, sliding it there.
    private func move(_ key: String, by direction: Int) {
        withAnimation(.spring(response: 0.3, dampingFraction: 0.85)) {
            if direction < 0 { model.moveUp(key: key) } else { model.moveDown(key: key) }
        }
        UIImpactFeedbackGenerator(style: .light).impactOccurred()
        movedKey = key
    }

    private func fieldLabel(_ key: LocalizedStringKey) -> some View {
        Text(key).textCase(.uppercase).font(PoulpyFont.label).kerning(1).foregroundStyle(PoulpyColor.onSurfaceVariant)
    }
}

/// An exercise whose details are open over the picker.
private struct PreviewTarget: Identifiable {
    let slug: String
    var id: String { slug }
}

/// Where a picked exercise goes: the part [partKey], or its repeat [repeatKey]; and what it is for.
private struct PickTarget: Identifiable {
    let partKey: String
    let repeatKey: String?
    let role: StepRole

    var id: String { "\(partKey)-\(repeatKey ?? "")-\(role.name)" }
}

/// "Move to another part" in a step's or a repeat's menu: the other parts by name.
@ViewBuilder
private func moveToPartMenu(_ key: String, from partKey: String, parts: [BuilderPart], model: BuilderModel) -> some View {
    let others = parts.filter { $0.key != partKey }
    if !others.isEmpty {
        Menu {
            ForEach(others, id: \.key) { part in
                Button(part.name) { model.moveToPart(key: key, partKey: part.key) }
            }
        } label: {
            Label("builder.moveToPart", systemImage: "arrow.right.doc.on.clipboard")
        }
    }
}

/// The format, asked once when a workout is created: Circuit or Sets, each with what it fits and an example.
private struct FormatSheet: View {
    let options: [WorkoutFormatOption]
    let onChoose: (WorkoutFormat) -> Void
    @State private var chosen: WorkoutFormat = .circuit

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 12) {
                SheetTitle(title: Text("builder.formatTitle"), subtitle: Text("builder.formatSubtitle"))
                ForEach(options, id: \.label) { option in
                    card(option)
                }
                Button { onChoose(chosen) } label: {
                    Text("builder.continue")
                        .font(.poulpy(.body).weight(.heavy))
                        .foregroundStyle(PoulpyColor.onPrimary)
                        .frame(maxWidth: .infinity, minHeight: 52)
                        .background(PoulpyColor.primary, in: Capsule())
                }
                .buttonStyle(.plain)
            }
            .padding(20)
        }
        .background(PoulpyColor.surface)
        .presentationDetents([.large])
    }

    private func card(_ option: WorkoutFormatOption) -> some View {
        let selected = chosen == option.format
        let circuit = option.format == .circuit
        return Button { chosen = option.format } label: {
            VStack(alignment: .leading, spacing: 6) {
                HStack(spacing: 10) {
                    Image(systemName: formatIcon(option.format))
                        .font(.system(size: 16, weight: .bold))
                        .foregroundStyle(selected ? PoulpyColor.onPrimary : PoulpyColor.onPrimaryContainer)
                        .frame(width: 36, height: 36)
                        .background(selected ? PoulpyColor.primary : PoulpyColor.primaryContainer, in: Circle())
                    Text(option.label).font(.custom(PoulpyFontFace.baloo2Bold, size: 19, relativeTo: .headline))
                }
                Text(LocalizedStringKey(circuit ? "builder.circuitDescription" : "builder.setsDescription"))
                    .font(.poulpy(.subheadline))
                    .fixedSize(horizontal: false, vertical: true)
                Text(LocalizedStringKey(circuit ? "builder.circuitExample" : "builder.setsExample"))
                    .font(.poulpy(.caption))
                    .foregroundStyle(PoulpyColor.onSurfaceVariant)
            }
            .foregroundStyle(PoulpyColor.onSurface)
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(14)
            .background(selected ? PoulpyColor.primaryContainer : PoulpyColor.surface, in: RoundedRectangle(cornerRadius: 16))
            .overlay(RoundedRectangle(cornerRadius: 16).stroke(selected ? PoulpyColor.primary : PoulpyColor.outline, lineWidth: selected ? 2 : 1))
        }
        .buttonStyle(.plain)
        .accessibilityAddTraits(selected ? .isSelected : [])
    }
}

/// "Add a part": warm-up, work and cool down, each saying where it goes or that it is already there.
private struct PartSheet: View {
    let options: [BuilderPartOption]
    let onPick: (PartKind) -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            SheetTitle(title: Text("builder.addPart"), subtitle: Text("builder.addPartSubtitle"))
            ForEach(options, id: \.name) { option in
                Button { onPick(option.kind) } label: {
                    HStack(spacing: 12) {
                        PartBadge(kind: option.kind)
                        VStack(alignment: .leading, spacing: 2) {
                            Text(option.name).font(.poulpy(.body).weight(.heavy)).foregroundStyle(PoulpyColor.onSurface)
                            Text(option.note).font(.poulpy(.caption)).foregroundStyle(PoulpyColor.onSurfaceVariant)
                        }
                        .frame(maxWidth: .infinity, alignment: .leading)
                    }
                    .padding(12)
                    .background(PoulpyColor.surface, in: RoundedRectangle(cornerRadius: 16))
                    .overlay(RoundedRectangle(cornerRadius: 16).stroke(PoulpyColor.outline, lineWidth: 1))
                    .opacity(option.enabled ? 1 : 0.5)
                }
                .buttonStyle(.plain)
                .disabled(!option.enabled)
            }
            Spacer(minLength: 0)
        }
        .padding(20)
        .background(PoulpyColor.surface)
        .presentationDetents([.medium])
    }
}

/// A sheet's title and the line under it.
private struct SheetTitle: View {
    let title: Text
    let subtitle: Text

    var body: some View {
        VStack(alignment: .leading, spacing: 2) {
            title.font(.poulpy(.title2).weight(.heavy))
            subtitle.font(.poulpy(.subheadline)).foregroundStyle(PoulpyColor.onSurfaceVariant)
        }
    }
}

/// Edits one step in a sheet: the open card, and Done.
private struct StepSheet: View {
    let graph: AppGraph
    let model: BuilderModel
    let row: BuilderRow
    let parts: [BuilderPart]
    let format: WorkoutFormat?
    let onDone: () -> Void
    /// The exercise whose details are open over this sheet; nil when none.
    @State private var detailing: PreviewTarget?

    var body: some View {
        ScrollView {
            VStack(spacing: 14) {
                EntryCard(model: model, row: row, roleOptions: model.roleOptions, parts: parts, format: format, open: true, onOpen: {}, onMove: { _ in },
                          onDetails: { detailing = PreviewTarget(slug: row.slug) })
                    .id(row.key)
                Button(action: onDone) {
                    Text("action.done")
                        .font(.poulpy(.body).weight(.heavy))
                        .foregroundStyle(PoulpyColor.onPrimary)
                        .frame(maxWidth: .infinity, minHeight: 52)
                        .background(PoulpyColor.primary, in: Capsule())
                }
                .buttonStyle(.plain)
            }
            .padding(20)
        }
        .background(PoulpyColor.background)
        // Opens fully, so every field shows without dragging the sheet up.
        .presentationDetents([.large])
        .sheet(item: $detailing) { detail in
            ExerciseInfoSheet(graph: graph, slug: detail.slug, adding: false, onAction: { _ in detailing = nil })
        }
    }
}

/// The up and down chevrons that move a step or a repeat among its siblings. A chevron at the end of the
/// list stays in place, dimmed and disabled, so nothing jumps. Each is 44 pt wide and 32 pt tall, as in the agreed mockup.
private struct MoveChevrons: View {
    let upLabel: Text
    let downLabel: Text
    let canMoveUp: Bool
    let canMoveDown: Bool
    let onMove: (Int) -> Void

    var body: some View {
        VStack(spacing: 0) {
            chevron("chevron.up", upLabel, enabled: canMoveUp) { onMove(-1) }
            chevron("chevron.down", downLabel, enabled: canMoveDown) { onMove(1) }
        }
    }

    private func chevron(_ symbol: String, _ label: Text, enabled: Bool, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Image(systemName: symbol)
                .font(.system(size: 16, weight: .heavy))
                .foregroundStyle(enabled ? PoulpyColor.primary : PoulpyColor.outline)
                .frame(width: 44, height: 32)
                .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
        .disabled(!enabled)
        .accessibilityLabel(label)
    }
}

/// A step of the workout. Folded: its role, name and summary. Open: where it is, what the step
/// is for, then reps and weight for an exercise, with its sets and the rest between them in a
/// Sets workout; a duration and intensity for a walk or a run; or the length of a rest.
private struct EntryCard: View {
    let model: BuilderModel
    let row: BuilderRow
    let roleOptions: [StepRoleOption]
    let parts: [BuilderPart]
    let format: WorkoutFormat?
    let open: Bool
    let onOpen: () -> Void
    let onMove: (Int) -> Void
    /// Opens the exercise's details, from the step's sheet; nil elsewhere.
    var onDetails: (() -> Void)?
    @State private var weight: String
    @State private var editing: TargetField?

    init(model: BuilderModel, row: BuilderRow, roleOptions: [StepRoleOption], parts: [BuilderPart], format: WorkoutFormat? = nil, open: Bool, onOpen: @escaping () -> Void, onMove: @escaping (Int) -> Void, onDetails: (() -> Void)? = nil) {
        self.model = model
        self.row = row
        self.roleOptions = roleOptions
        self.parts = parts
        self.format = format
        self.open = open
        self.onOpen = onOpen
        self.onMove = onMove
        self.onDetails = onDetails
        _weight = State(initialValue: row.weight)
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack(spacing: 10) {
                if !open {
                    MoveChevrons(
                        upLabel: Text(L("builder.moveStepUp %@", row.name)),
                        downLabel: Text(L("builder.moveStepDown %@", row.name)),
                        canMoveUp: row.canMoveUp,
                        canMoveDown: row.canMoveDown,
                        onMove: onMove
                    )
                }
                Button(action: onOpen) {
                    VStack(alignment: .leading, spacing: 2) {
                        if let role = row.roleLabel { roleTag(role) }
                        Text(row.name).font(.custom(PoulpyFontFace.baloo2Bold, size: 19, relativeTo: .headline))
                        if open {
                            Text(row.place).font(.poulpy(.subheadline)).foregroundStyle(PoulpyColor.onSurfaceVariant)
                        }
                        if !open || row.isRest {
                            Text(row.summary).font(.poulpy(.subheadline)).monospacedDigit().foregroundStyle(PoulpyColor.onSurfaceVariant)
                        }
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .contentShape(Rectangle())
                }
                .buttonStyle(.plain)
                // The chevrons move a step up and down; moving into a repeat or out of it are actions on the card.
                .accessibilityActions {
                    if row.canMoveIntoRepeat {
                        Button("builder.moveIntoRepeat") { model.moveIntoRepeatAbove(key: row.key) }
                    }
                    if row.repeatKey != nil {
                        Button("builder.moveOutOfRepeat") { model.moveOutOfRepeat(key: row.key) }
                    }
                }
                if open, !row.isRest, let onDetails {
                    Button(action: onDetails) {
                        Label("builder.details", systemImage: "info.circle")
                            .font(.poulpy(.subheadline).weight(.heavy))
                            .foregroundStyle(PoulpyColor.onSurface)
                            .padding(.horizontal, 14)
                            .frame(minHeight: 44)
                            .background(PoulpyColor.surfaceVariant, in: Capsule())
                    }
                    .buttonStyle(.plain)
                    .accessibilityLabel(Text(L("exercise.details %@", row.name)))
                }
                if !open {
                    stepMenu
                }
            }
            if open && row.isRest {
                numberCell("builder.duration", row.restSeconds, unit: .seconds, set: { model.setRest(key: row.key, seconds: $0) },
                           less: { model.changeRest(key: row.key, steps: -1) }, more: { model.changeRest(key: row.key, steps: 1) })
                    .padding(.trailing, 4)
            } else if open {
                select("builder.step", value: roleOptions.first(where: { $0.role == row.role })?.label ?? "") {
                    ForEach(roleOptions, id: \.label) { option in
                        Button(option.label) { model.setRole(key: row.key, role: option.role) }
                    }
                }
                .padding(.trailing, 4)
            }
            if open, !row.isRest, let target = row.target {
                targetEditor(target)
            } else if open, !row.isRest {
                if row.showsSets {
                    numberCell("builder.sets", row.sets, unit: nil, set: { model.setSets(key: row.key, sets: $0) },
                               less: { model.setSets(key: row.key, sets: row.sets - 1) }, more: { model.setSets(key: row.key, sets: row.sets + 1) })
                        .padding(.trailing, 4)
                }
                numberCell("builder.target", row.amount, unit: row.amountUnit, set: { model.setAmount(key: row.key, value: $0) },
                           less: { model.changeAmount(key: row.key, steps: -1) }, more: { model.changeAmount(key: row.key, steps: 1) })
                    .padding(.trailing, 4)
                if row.showsSets && row.sets >= 2 {
                    numberCell("builder.restBetweenSets", row.restBetweenSets, unit: .seconds, set: { model.setRestBetweenSets(key: row.key, seconds: $0) },
                               less: { model.changeRestBetweenSets(key: row.key, steps: -1) }, more: { model.changeRestBetweenSets(key: row.key, steps: 1) })
                        .padding(.trailing, 4)
                }
                if !row.isTimed {
                    weightCell.padding(.trailing, 4)
                }
                Text(LocalizedStringKey(format == .circuit ? "builder.circuitHint" : "builder.setsHint"))
                    .font(.poulpy(.caption))
                    .foregroundStyle(PoulpyColor.onSurfaceVariant)
            }
        }
        .padding(EdgeInsets(top: open ? 10 : 6, leading: open ? 12 : 6, bottom: open ? 14 : 6, trailing: 8))
        .background(PoulpyColor.surface, in: RoundedRectangle(cornerRadius: 16))
        .overlay(RoundedRectangle(cornerRadius: 16).stroke(PoulpyColor.outline, lineWidth: 1))
    }

    /// The small tag above a step's name: "RECOVER".
    private func roleTag(_ label: String) -> some View {
        let recover = row.role == .recover
        return Text(label)
            .textCase(.uppercase)
            .font(.custom(PoulpyFontFace.nunitoExtraBold, size: 11.5, relativeTo: .caption2))
            .kerning(0.9)
            .foregroundStyle(recover ? PoulpyColor.onSurfaceVariant : PoulpyColor.onPrimaryContainer)
            .padding(.horizontal, 8)
            .padding(.vertical, 3)
            .background(recover ? PoulpyColor.surfaceVariant : PoulpyColor.primaryContainer, in: Capsule())
    }

    /// Edit, duplicate, the moves into or out of a repeat when they apply, the move to another part, and delete.
    private var stepMenu: some View {
        Menu {
            Button(action: onOpen) { Label("builder.edit", systemImage: "pencil") }
            Button { model.duplicate(key: row.key) } label: { Label("builder.duplicate", systemImage: "plus.square.on.square") }
            if row.canMoveIntoRepeat {
                Button { model.moveIntoRepeatAbove(key: row.key) } label: { Label("builder.moveIntoRepeat", systemImage: "repeat") }
            }
            if row.repeatKey != nil {
                Button { model.moveOutOfRepeat(key: row.key) } label: { Label("builder.moveOutOfRepeat", systemImage: "rectangle.portrait.and.arrow.right") }
            }
            moveToPartMenu(row.key, from: row.partKey, parts: parts, model: model)
            Divider()
            Button(role: .destructive) { model.remove(key: row.key) } label: { Label("builder.delete", systemImage: "trash") }
        } label: {
            Image(systemName: "ellipsis").font(.system(size: 17, weight: .bold)).frame(width: 44, height: 44)
        }
        .foregroundStyle(PoulpyColor.onSurfaceVariant)
        .accessibilityLabel(Text(L("builder.moreActions %@", row.name)))
    }

    /// A compact select: its label just above the chosen value, and a chevron; a tap lists the options.
    private func select<Options: View>(_ label: LocalizedStringKey, value: String, @ViewBuilder options: () -> Options) -> some View {
        Menu {
            options()
        } label: {
            HStack(spacing: 4) {
                VStack(alignment: .leading, spacing: 0) {
                    Text(label).textCase(.uppercase).font(.custom(PoulpyFontFace.nunitoExtraBold, size: 11.5, relativeTo: .caption2)).kerning(0.9).foregroundStyle(PoulpyColor.onSurfaceVariant)
                    Text(value).font(.poulpy(.body).weight(.bold)).lineLimit(1).foregroundStyle(PoulpyColor.onSurface)
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                Image(systemName: "chevron.down").font(.system(size: 14, weight: .bold)).foregroundStyle(PoulpyColor.onSurfaceVariant)
            }
            .padding(.leading, 12)
            .padding(.trailing, 10)
            .frame(maxWidth: .infinity, minHeight: 52)
            .background(PoulpyColor.surface, in: RoundedRectangle(cornerRadius: 12))
            .overlay(RoundedRectangle(cornerRadius: 12).stroke(PoulpyColor.outline, lineWidth: 1.5))
            .contentShape(RoundedRectangle(cornerRadius: 12))
        }
        .buttonStyle(.plain)
    }

    private func cell<Content: View>(_ label: LocalizedStringKey, @ViewBuilder content: () -> Content) -> some View {
        VStack(spacing: 2) {
            Text(label).textCase(.uppercase).font(PoulpyFont.label).kerning(1).lineLimit(1).foregroundStyle(PoulpyColor.onSurfaceVariant)
            content()
        }
        .padding(6)
        .frame(maxWidth: .infinity)
        .background(PoulpyColor.background, in: RoundedRectangle(cornerRadius: 12))
    }

    /// A value between − and +; with [onValue], tapping the value opens a sheet to type it.
    private func stepper(
        _ label: LocalizedStringKey,
        _ value: String,
        less: @escaping () -> Void,
        more: @escaping () -> Void,
        onValue: (() -> Void)? = nil
    ) -> some View {
        cell(label) {
            HStack(spacing: 2) {
                roundStep("minus", "builder.less", less)
                if let onValue {
                    Button(action: onValue) { stepperValue(value, tappable: true) }
                        .buttonStyle(.plain)
                        .accessibilityHint(Text("builder.tapToType"))
                } else {
                    stepperValue(value, tappable: false)
                }
                roundStep("plus", "builder.more", more)
            }
        }
    }

    private func stepperValue(_ value: String, tappable: Bool) -> some View {
        Text(value)
            .font(.custom(PoulpyFontFace.nunitoExtraBold, size: 22, relativeTo: .title3))
            .monospacedDigit()
            .lineLimit(1)
            .minimumScaleFactor(0.6)
            .foregroundStyle(tappable ? PoulpyColor.primary : PoulpyColor.onSurface)
            .frame(maxWidth: .infinity, minHeight: 44)
            .contentShape(Rectangle())
    }

    /// A walk or a run: one block with a duration (time, distance or until button press) and an intensity, instead of sets.
    private func targetEditor(_ target: WalkRunTarget) -> some View {
        let untilPressed = row.isLast ? "builder.untilEnd" : "builder.untilPressed"
        let durations: [(kind: DurationKind, key: String)] = [(.distance, "builder.distance"), (.time, "builder.time"), (.untilPressed, untilPressed)]
        let intensities: [(kind: IntensityKind, key: String)] = [(.free, "builder.free"), (.pace, "builder.pace")]
        return VStack(alignment: .leading, spacing: 8) {
            HStack(spacing: 8) {
                select("builder.duration", value: NSLocalizedString(durations.first(where: { $0.kind == target.duration })?.key ?? "", comment: "")) {
                    ForEach(durations.indices, id: \.self) { index in
                        Button { model.setDurationKind(key: row.key, kind: durations[index].kind) } label: { Text(LocalizedStringKey(durations[index].key)) }
                    }
                }
                select("builder.intensity", value: NSLocalizedString(intensities.first(where: { $0.kind == target.intensity })?.key ?? "", comment: "")) {
                    ForEach(intensities.indices, id: \.self) { index in
                        Button { model.setIntensityKind(key: row.key, kind: intensities[index].kind) } label: { Text(LocalizedStringKey(intensities[index].key)) }
                    }
                }
            }
            if target.durationSeconds != nil {
                stepper("builder.time", target.durationLabel,
                        less: { model.changeDuration(key: row.key, steps: -1) },
                        more: { model.changeDuration(key: row.key, steps: 1) },
                        onValue: { editing = .duration })
            }
            if target.distanceMeters != nil {
                stepper("builder.distance", target.distanceLabel,
                        less: { model.changeDistance(key: row.key, steps: -1) },
                        more: { model.changeDistance(key: row.key, steps: 1) },
                        onValue: { editing = .distance })
            }
            if target.speedKmh != nil {
                stepper("builder.pace", target.paceLabel,
                        less: { model.changePace(key: row.key, steps: -1) },
                        more: { model.changePace(key: row.key, steps: 1) },
                        onValue: { editing = .pace })
            }
            Text([target.hint.map { "≈ \($0)" }, NSLocalizedString("builder.tapToType", comment: "")].compactMap { $0 }.joined(separator: " · "))
                .font(.poulpy(.caption))
                .foregroundStyle(PoulpyColor.onSurfaceVariant)
        }
        .padding(.trailing, 4)
        .sheet(item: $editing) { field in
            if let target = row.target {
                ValueSheet(model: model, key: row.key, target: target, field: field) { editing = nil }
            }
        }
    }

    private func roundStep(_ symbol: String, _ description: LocalizedStringKey, _ action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Image(systemName: symbol)
                .font(.system(size: 18, weight: .heavy))
                .foregroundStyle(PoulpyColor.primary)
                .frame(width: 44, height: 44)
                .background(PoulpyColor.surface, in: Circle())
        }
        .buttonStyle(.plain)
        .accessibilityLabel(Text(description))
    }

    /// A label above a number field with − and +: sets, reps, seconds or metres, typed or nudged.
    private func numberCell(
        _ label: LocalizedStringKey,
        _ value: Int32,
        unit: AmountUnit?,
        set: @escaping (Int32) -> Void,
        less: @escaping () -> Void,
        more: @escaping () -> Void
    ) -> some View {
        VStack(alignment: .leading, spacing: 4) {
            cellLabel(label).padding(.horizontal, 4)
            NumberEntry(onLess: less, onMore: more, lessLabel: "builder.less", moreLabel: "builder.more") {
                StepNumberField(value: value, description: label, unit: unit.map(unitText), set: set)
            }
        }
    }

    private func cellLabel(_ key: LocalizedStringKey) -> some View {
        Text(key).textCase(.uppercase).font(PoulpyFont.label).kerning(1).foregroundStyle(PoulpyColor.onSurfaceVariant)
    }

    private func unitText(_ unit: AmountUnit) -> String {
        switch unit {
        case .reps: return NSLocalizedString("builder.unitReps", comment: "")
        case .seconds: return NSLocalizedString("builder.unitSeconds", comment: "")
        default: return NSLocalizedString("builder.unitMeters", comment: "")
        }
    }

    /// The weight in the user's unit, typed with the decimal keyboard.
    private var weightCell: some View {
        VStack(alignment: .leading, spacing: 4) {
            cellLabel("builder.weight").padding(.horizontal, 4)
            NumberField(text: $weight, description: row.weightUnit == "lb" ? "builder.weightLb" : "builder.weightKg", unit: row.weightUnit, decimal: true)
                .onChange(of: weight) { text in
                    model.setWeight(key: row.key, text: text)
                }
        }
    }
}

/// A `NumberField` over a whole number: typing sets it, and a change from outside (− and +, or a limit) rewrites the text.
private struct StepNumberField: View {
    let value: Int32
    let description: LocalizedStringKey
    let unit: String?
    let set: (Int32) -> Void
    @State private var text: String

    init(value: Int32, description: LocalizedStringKey, unit: String?, set: @escaping (Int32) -> Void) {
        self.value = value
        self.description = description
        self.unit = unit
        self.set = set
        _text = State(initialValue: String(value))
    }

    var body: some View {
        NumberField(text: $text, description: description, unit: unit)
            .onChange(of: text) { typed in
                if let number = Int32(typed) { set(number) }
            }
            .onChange(of: value) { text = String($0) }
    }
}

private enum TargetField: Identifiable {
    case duration, distance, pace

    var id: Self { self }

    var title: LocalizedStringKey {
        switch self {
        case .duration: return "builder.duration"
        case .distance: return "builder.distance"
        case .pace: return "builder.pace"
        }
    }
}

/// Types a duration, distance or pace, with its unit and quick picks such as 10 km, 1 h or 5:00 /km.
private struct ValueSheet: View {
    let model: BuilderModel
    let key: String
    let target: WalkRunTarget
    let field: TargetField
    let onDone: () -> Void
    @State private var text: String
    @State private var invalid = false

    init(model: BuilderModel, key: String, target: WalkRunTarget, field: TargetField, onDone: @escaping () -> Void) {
        self.model = model
        self.key = key
        self.target = target
        self.field = field
        self.onDone = onDone
        switch field {
        case .duration: _text = State(initialValue: target.durationText)
        case .distance: _text = State(initialValue: target.distanceText)
        case .pace: _text = State(initialValue: target.paceText)
        }
    }

    private var picks: [ValuePick] {
        switch field {
        case .duration: return model.durationPicks()
        case .distance: return model.distancePicks(unit: target.distanceUnit)
        case .pace: return model.pacePicks(key: key, unit: target.paceUnit)
        }
    }

    private var decimal: Bool { field == .distance || (field == .pace && target.paceUnit.isSpeed) }

    private func apply(_ value: String) -> Bool {
        switch field {
        case .duration: return model.setDuration(key: key, text: value)
        case .distance: return model.setDistance(key: key, text: value, unit: target.distanceUnit)
        case .pace: return model.setPace(key: key, text: value, unit: target.paceUnit)
        }
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 14) {
            Text(field.title).font(.poulpy(.title2).weight(.heavy))
            HStack(spacing: 10) {
                TextField("", text: $text)
                    .keyboardType(decimal ? .decimalPad : .numbersAndPunctuation)
                    .multilineTextAlignment(.center)
                    .font(.custom(PoulpyFontFace.nunitoExtraBold, size: 22, relativeTo: .title3))
                    .frame(height: 56)
                    .background(PoulpyColor.surface, in: RoundedRectangle(cornerRadius: 16))
                    .overlay(RoundedRectangle(cornerRadius: 16).stroke(PoulpyColor.primary, lineWidth: 1.5))
                    .accessibilityLabel(Text(field.title))
                    .onChange(of: text) { _ in invalid = false }
                if field != .duration {
                    Text(field == .distance ? target.distanceUnit.symbol : target.paceUnit.symbol)
                        .font(.poulpy(.headline))
                        .foregroundStyle(PoulpyColor.onSurfaceVariant)
                }
            }
            if invalid {
                Label("builder.valueInvalid", systemImage: "exclamationmark.circle")
                    .font(.poulpy(.caption))
                    .foregroundStyle(PoulpyColor.error)
            }
            if field != .duration {
                sectionLabel("builder.unit")
                FlowLayout(spacing: 8) {
                    if field == .distance {
                        ForEach([DistanceUnit.m, .km, .mi], id: \.self) { unit in
                            PoulpyChip(title: Text(unit.symbol), selected: target.distanceUnit == unit) {
                                text = model.setDistanceUnit(key: key, unit: unit, typed: text)
                            }
                        }
                    } else {
                        ForEach([PaceUnit.perKm, .perMi, .kmh, .mph], id: \.self) { unit in
                            PoulpyChip(title: Text(unit.symbol), selected: target.paceUnit == unit) {
                                text = model.setPaceUnit(key: key, unit: unit, typed: text)
                            }
                        }
                    }
                }
            }
            sectionLabel("builder.quickPicks")
            FlowLayout(spacing: 8) {
                ForEach(picks, id: \.text) { pick in
                    PoulpyChip(title: Text(pick.label), selected: text == pick.text) {
                        if apply(pick.text) { onDone() }
                    }
                }
            }
            Button {
                if apply(text) { onDone() } else { invalid = true }
            } label: {
                Text("action.done")
                    .font(.poulpy(.body).weight(.heavy))
                    .foregroundStyle(PoulpyColor.onPrimary)
                    .frame(maxWidth: .infinity, minHeight: 52)
                    .background(PoulpyColor.primary, in: Capsule())
            }
            .buttonStyle(.plain)
            Spacer(minLength: 0)
        }
        .padding(20)
        .background(PoulpyColor.surface)
        .presentationDetents([.medium, .large])
    }

    private func sectionLabel(_ key: LocalizedStringKey) -> some View {
        Text(key).textCase(.uppercase).font(PoulpyFont.label).kerning(1).foregroundStyle(PoulpyColor.onSurfaceVariant)
    }
}

/// A localized string with printf-style arguments, as in Localizable.strings.
private func L(_ key: String, _ args: CVarArg...) -> String {
    String(format: NSLocalizedString(key, comment: ""), locale: Locale.current, arguments: args)
}
