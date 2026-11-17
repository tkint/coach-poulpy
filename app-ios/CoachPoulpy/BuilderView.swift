import SwiftUI
import UIKit
import Shared

struct BuilderView: View {
    let graph: AppGraph
    @StateObject private var builder: Observed<BuilderModel, BuilderState>
    /// The part the exercise being picked goes to; nil when not picking.
    @State private var picking: PickTarget?
    /// The exercise whose details are open over the picker; nil when none.
    @State private var previewing: PreviewTarget?
    /// Whether saving was tried without a name, which shows what the name field is missing.
    @State private var nameAsked = false
    @State private var changingFormat = false
    /// The switch waiting for "Follow the new format" or "Keep the order"; nil when none.
    @State private var askingOrder: FormatChange?
    @Environment(\.dismiss) private var dismiss

    /// [newFormat] starts a new workout in that format, and [firstExercise] makes it already hold that exercise, from an exercise page;
    /// [copyOf] starts an unsaved copy of that workout; [importing] starts the workout of a file opened from another app.
    init(graph: AppGraph, workoutId: String?, newFormat: WorkoutFormat? = nil, firstExercise: String? = nil, copyOf: String? = nil, importing: WorkoutImport? = nil) {
        self.graph = graph
        _builder = StateObject(wrappedValue: {
            let language = Language.code
            let model: BuilderModel
            if let imported = importing?.builder() {
                model = imported
            } else if let copyOf, let copy = graph.duplicateWorkout(workoutId: copyOf, languageCode: language) {
                model = copy
            } else if let workoutId, let edit = graph.editWorkout(workoutId: workoutId, languageCode: language) {
                model = edit
            } else if let newFormat, let firstExercise {
                model = graph.createWorkoutWith(format: newFormat, exerciseSlug: firstExercise, languageCode: language)
            } else if let newFormat {
                model = graph.createWorkout(format: newFormat, languageCode: language)
            } else {
                fatalError("A builder opens on a workout, a copy, an import or a new one with its format")
            }
            return Observed(model, current: model.current, watch: model.watch, close: { $0.close() })
        }())
    }

    private var model: BuilderModel { builder.model }
    private var state: BuilderState { builder.value }

    /// The step being edited in the sheet: the one just added, or the one tapped.
    @State private var editing: String?
    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 14) {
                    PoulpyTextField(
                        label: "builder.name", text: Binding(get: { state.name }, set: { model.rename(name: $0) }),
                        error: nameAsked && !state.hasName ? NSLocalizedString("builder.nameError", comment: "") : nil,
                        placeholder: "builder.namePlaceholder"
                    )
                    formatLine(state.format)
                    ForEach(state.parts, id: \.key) { part in
                        partBlock(part)
                    }
                }
                .padding(.horizontal, PoulpySpace.lg)
                .padding(.top, 8)
                .padding(.bottom, PoulpySpace.xl)
            }
            .background(PoulpyColor.background)
            .navigationTitle(LocalizedStringKey(model.isEditing ? "builder.editTitle" : "builder.newTitle"))
            .navigationBarTitleDisplayMode(.inline)
            .toolbarBackground(PoulpyColor.background, for: .navigationBar)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button { dismiss() } label: {
                        LucideIcon(.x, size: 20)
                    }
                    .foregroundStyle(PoulpyColor.onSurface)
                    .accessibilityLabel(Text("action.close"))
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button {
                        if !state.hasName { nameAsked = true } else if model.save() != nil { dismiss() }
                    } label: {
                        Text("action.save")
                    }
                    .buttonStyle(PoulpyButtonStyle(.primary, size: .sm))
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
                    StepSheet(graph: graph, model: model, row: row, format: state.format) { editing = nil }
                }
            }
            .sheet(isPresented: $changingFormat) {
                FormatSheet(options: model.formatOptions, current: state.format, onChoose: { format in
                    changingFormat = false
                    if let change = model.formatChange(format: format) {
                        if change.asks { askingOrder = change } else { model.changeFormat(format: format, how: .keepOrder) }
                    }
                }, onClose: { changingFormat = false })
            }
            .sheet(item: $askingOrder) { change in
                FormatOrderSheet(change: change, onChoose: { choice in
                    model.changeFormat(format: change.format, how: choice)
                    askingOrder = nil
                }, onClose: { askingOrder = nil })
            }
            .sheet(item: $picking) { target in
                NavigationStack {
                    ExerciseList(graph: graph, withRecent: true) { row in
                        PickerRowView(row: row, onOpen: { previewing = PreviewTarget(slug: row.slug) }) {
                            model.add(slug: row.slug, partKey: target.partKey, repeatKey: nil)
                            picking = nil
                        }
                    }
                    .sheet(item: $previewing) { preview in
                        ExerciseInfoSheet(graph: graph, slug: preview.slug, onAction: { slug in
                            model.add(slug: slug, partKey: target.partKey, repeatKey: nil)
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

    /// Under the name: the format as a chip with a chevron; a tap opens the format sheet to change it.
    private func formatLine(_ format: WorkoutFormat) -> some View {
        HStack(spacing: 10) {
            fieldLabel("builder.format")
            Button { changingFormat = true } label: {
                HStack(spacing: 6) {
                    Label(state.formatLabel, lucide: formatIcon(format))
                        .font(.poulpy(.subheadline).weight(.heavy))
                        .foregroundStyle(PoulpyColor.onPrimaryContainer)
                    LucideIcon(.chevronDown, size: 16).foregroundStyle(PoulpyColor.onSurfaceVariant)
                }
                .padding(.horizontal, 12)
                .padding(.vertical, 3)
                .background(PoulpyColor.primaryContainer, in: Capsule())
                .frame(minHeight: 44)
                .contentShape(Capsule())
            }
            .buttonStyle(.plain)
            .accessibilityLabel("builder.changeFormat \(state.formatLabel)")
        }
    }

    /// A part as an outlined block, like on the workout page: its steps as rows, a small + on the border to add to it,
    /// and while it is empty one line that opens the exercise list. The warm-up and the cool down are optional.
    private func partBlock(_ part: BuilderPart) -> some View {
        let empty = part.steps.isEmpty
        return PartBlockView(kind: part.kind, name: part.name, summary: part.summary, time: "") {
            if empty {
                Button { picking = PickTarget(partKey: part.key) } label: {
                    Text(LocalizedStringKey(part.kind == .work ? "builder.emptyWork" : "builder.emptyOptional"))
                        .font(.poulpy(.footnote))
                        .foregroundStyle(PoulpyColor.onSurfaceVariant)
                        .frame(maxWidth: .infinity, minHeight: 44, alignment: .leading)
                        .contentShape(Rectangle())
                }
                .buttonStyle(.plain)
                .accessibilityLabel(Text(part.addLabel))
            } else {
                VStack(spacing: 0) {
                    ForEach(Array(part.steps.enumerated()), id: \.element.key) { index, step in
                        if index > 0 { Divider().overlay(PoulpyColor.outline) }
                        if let row = step.row {
                            stepRow(row, inRepeat: false)
                        } else if let group = step.repeatGroup {
                            repeatBlock(group)
                        }
                    }
                }
                .padding(.vertical, 4)
            }
        } corner: {
            addButton(part, compact: empty)
        }
    }

    /// The small square + on a block's border: a tap asks what to add. An exercise is picked from the exercise list; a repeat or a rest is added right away.
    private func addButton(_ part: BuilderPart, compact: Bool) -> some View {
        Menu {
            Button { picking = PickTarget(partKey: part.key) } label: { Label("builder.addExercise", lucide: .personStanding) }
            Button { model.addRepeat(partKey: part.key) } label: { Label("builder.addRepeat", lucide: .repeat) }
            Button { model.addRest(partKey: part.key, repeatKey: nil) } label: { Label("builder.addRest", lucide: .timer) }
        } label: {
            LucideIcon(.plus, size: compact ? 16 : 20)
                .foregroundStyle(PoulpyColor.onSurfaceVariant)
                .frame(width: 28, height: 28)
                .background(PoulpyColor.background, in: RoundedRectangle(cornerRadius: PoulpyRadius.sm))
                .overlay(RoundedRectangle(cornerRadius: PoulpyRadius.sm).stroke(PoulpyColor.outline, lineWidth: 1))
                .frame(width: 44, height: 44)
                .contentShape(Rectangle())
        }
        .accessibilityLabel(Text(part.addLabel))
    }

    /// A step of the workout as a row, like on the workout page: its movement badge, name (with the Recover tag on its line)
    /// and what it asks for. A tap edits it in a sheet; the ⋮ menu has the rest. Inside a repeat the row sits on a card.
    private func stepRow(_ row: BuilderRow, inRepeat: Bool) -> some View {
        HStack(spacing: 0) {
            Button { editing = row.key } label: {
                HStack(spacing: 10) {
                    if row.isRest {
                        LucideIcon(.timer, size: 16).foregroundStyle(PoulpyColor.onSurfaceVariant)
                            .frame(width: 32, height: 32)
                            .background(PoulpyColor.surfaceVariant, in: Circle())
                            .accessibilityHidden(true)
                    } else {
                        PatternBadge(pattern: row.pattern, size: 32)
                    }
                    VStack(alignment: .leading, spacing: 0) {
                        HStack(spacing: 6) {
                            Text(row.name).font(.poulpy(.subheadline).weight(.heavy)).foregroundStyle(PoulpyColor.onSurface).lineLimit(1)
                            if let role = row.roleLabel { RoleTag(label: role, recover: row.role == .recover, compact: true) }
                        }
                        Text(row.summary).font(.poulpy(.footnote)).monospacedDigit().foregroundStyle(PoulpyColor.onSurfaceVariant)
                    }
                    Spacer(minLength: 0)
                }
                .padding(.vertical, 6)
                .frame(minHeight: 44)
                .contentShape(Rectangle())
            }
            .buttonStyle(.plain)
            .accessibilityLabel(Text(L("builder.open %@", row.name)))
            stepMenu(row)
        }
        .padding(.leading, inRepeat ? 8 : 0)
        .background(inRepeat ? PoulpyColor.surface : Color.clear, in: RoundedRectangle(cornerRadius: PoulpyRadius.sm))
    }

    /// Edit, duplicate, the moves that apply (up, down, into the repeat above, out of its repeat), and delete.
    private func stepMenu(_ row: BuilderRow) -> some View {
        Menu {
            Button("builder.edit") { editing = row.key }
            Button("builder.duplicate") { model.duplicate(key: row.key) }
            if row.canMoveUp { Button("builder.moveUp") { move(row.key, by: -1) } }
            if row.canMoveDown { Button("builder.moveDown") { move(row.key, by: 1) } }
            if row.canMoveIntoRepeat { Button("builder.moveIntoRepeat") { model.moveIntoRepeatAbove(key: row.key) } }
            if row.repeatKey != nil { Button("builder.moveOutOfRepeat") { model.moveOutOfRepeat(key: row.key) } }
            Divider()
            Button(role: .destructive) { model.remove(key: row.key) } label: { Label("builder.delete", lucide: .trash) }
        } label: {
            LucideIcon(.ellipsisVertical, size: 20).frame(width: 44, height: 44)
        }
        .foregroundStyle(PoulpyColor.onSurfaceVariant)
        .padding(.trailing, -8)
        .accessibilityLabel(Text(L("builder.moreActions %@", row.name)))
    }

    /// A repeat inside a block: "Repeat", how many times as a small − N times +, and a menu; its steps sit in it as rows.
    private func repeatBlock(_ group: BuilderRepeat) -> some View {
        VStack(spacing: 4) {
            HStack(spacing: 8) {
                LucideIcon(.repeat, size: 20).foregroundStyle(PoulpyColor.primary).padding(.leading, 4).accessibilityHidden(true)
                Text("builder.repeat")
                    .font(.poulpy(.subheadline).weight(.heavy))
                    .foregroundStyle(PoulpyColor.onPrimaryContainer)
                    .frame(maxWidth: .infinity, alignment: .leading)
                HStack(spacing: 2) {
                    squareStep("−", "builder.repeatFewer") { model.setRepeatTimes(key: group.key, times: group.times - 1) }
                    Text(group.timesLabel)
                        .font(.poulpy(.footnote).weight(.bold))
                        .monospacedDigit()
                        .foregroundStyle(PoulpyColor.onPrimaryContainer)
                        .frame(width: 56)
                    squareStep("+", "builder.repeatMore") { model.setRepeatTimes(key: group.key, times: group.times + 1) }
                }
                .accessibilityElement(children: .contain)
                Menu {
                    Button { model.duplicate(key: group.key) } label: { Label("builder.duplicate", lucide: .copy) }
                    if group.canMoveUp { Button { move(group.key, by: -1) } label: { Label("builder.moveUp", lucide: .arrowUp) } }
                    if group.canMoveDown { Button { move(group.key, by: 1) } label: { Label("builder.moveDown", lucide: .arrowDown) } }
                    Divider()
                    Button(role: .destructive) { model.remove(key: group.key) } label: { Label("builder.delete", lucide: .trash) }
                } label: {
                    LucideIcon(.ellipsisVertical, size: 20).frame(width: 44, height: 44)
                }
                .foregroundStyle(PoulpyColor.onSurfaceVariant)
                .padding(.trailing, -8)
                .accessibilityLabel(Text("builder.repeatActions"))
            }
            ForEach(group.rows, id: \.key) { row in
                stepRow(row, inRepeat: true)
            }
        }
        .padding(6)
        .background(PoulpyColor.primaryContainer, in: RoundedRectangle(cornerRadius: PoulpyRadius.md))
        .padding(.vertical, 2)
    }

    /// A small square − or +, discreet beside the repeat's count; its touch area is bigger than it looks.
    private func squareStep(_ sign: String, _ description: LocalizedStringKey, _ action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Text(sign)
                .font(.poulpy(.body).weight(.bold))
                .foregroundStyle(PoulpyColor.onPrimaryContainer)
                .frame(width: 28, height: 28)
                .background(PoulpyColor.primary.opacity(0.12), in: RoundedRectangle(cornerRadius: PoulpyRadius.sm))
                .frame(width: 40, height: 40)
                .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
        .accessibilityLabel(Text(description))
    }

    /// Moves a step or a repeat one place up (-1) or down (1) among its siblings.
    private func move(_ key: String, by direction: Int) {
        withAnimation(.spring(response: 0.3, dampingFraction: 0.85)) {
            if direction < 0 { model.moveUp(key: key) } else { model.moveDown(key: key) }
        }
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

/// The part a picked exercise goes to.
private struct PickTarget: Identifiable {
    let partKey: String

    var id: String { partKey }
}

/// The small tag on a step's name line, or above it in the step sheet: "RECOVER".
private struct RoleTag: View {
    let label: String
    let recover: Bool
    var compact = false

    var body: some View {
        Text(label)
            .textCase(.uppercase)
            .font(.custom(PoulpyFontFace.nunitoExtraBold, size: compact ? 10.5 : 11.5, relativeTo: .caption2))
            .kerning(compact ? 0.6 : 0.9)
            .foregroundStyle(recover ? PoulpyColor.onSurfaceVariant : PoulpyColor.onPrimaryContainer)
            .padding(.horizontal, compact ? 6 : 8)
            .padding(.vertical, compact ? 1 : 3)
            .background(recover ? PoulpyColor.surfaceVariant : PoulpyColor.primaryContainer, in: Capsule())
    }
}

/// A sheet's title and the line under it.
struct SheetTitle: View {
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
    let format: WorkoutFormat?
    let onDone: () -> Void
    /// The exercise whose details are open over this sheet; nil when none.
    @State private var detailing: PreviewTarget?

    var body: some View {
        ScrollView {
            VStack(spacing: 14) {
                EntryCard(model: model, row: row, roleOptions: model.roleOptions, format: format,
                          onDetails: { detailing = PreviewTarget(slug: row.slug) })
                    .id(row.key)
                Button(action: onDone) {
                    Text("action.done")
                        .frame(maxWidth: .infinity)
                }
                .buttonStyle(PoulpyButtonStyle(.primary, size: .lg))
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

/// A step of the workout, open in its sheet: where it is, what the step is for, then reps and weight for an
/// exercise, with its sets and the rest between them in a Sets workout; a duration and intensity for a walk
/// or a run; or the length of a rest.
private struct EntryCard: View {
    let model: BuilderModel
    let row: BuilderRow
    let roleOptions: [StepRoleOption]
    let format: WorkoutFormat?
    /// Opens the exercise's details.
    var onDetails: (() -> Void)?
    @State private var weight: String
    @State private var editing: TargetField?

    init(model: BuilderModel, row: BuilderRow, roleOptions: [StepRoleOption], format: WorkoutFormat? = nil, onDetails: (() -> Void)? = nil) {
        self.model = model
        self.row = row
        self.roleOptions = roleOptions
        self.format = format
        self.onDetails = onDetails
        _weight = State(initialValue: row.weight)
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack(spacing: 10) {
                VStack(alignment: .leading, spacing: 2) {
                    if let role = row.roleLabel { RoleTag(label: role, recover: row.role == .recover) }
                    Text(row.name).font(.custom(PoulpyFontFace.baloo2Bold, size: 19, relativeTo: .headline))
                    Text(row.place).font(.poulpy(.subheadline)).foregroundStyle(PoulpyColor.onSurfaceVariant)
                    if row.isRest {
                        Text(row.summary).font(.poulpy(.subheadline)).monospacedDigit().foregroundStyle(PoulpyColor.onSurfaceVariant)
                    }
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                if !row.isRest, let onDetails {
                    Button(action: onDetails) {
                        Label("builder.details", lucide: .info)
                            .font(.poulpy(.subheadline).weight(.heavy))
                            .foregroundStyle(PoulpyColor.onSurface)
                            .padding(.horizontal, 14)
                            .frame(minHeight: 44)
                            .background(PoulpyColor.surfaceVariant, in: Capsule())
                    }
                    .buttonStyle(.plain)
                    .accessibilityLabel(Text(L("exercise.details %@", row.name)))
                }
            }
            if row.isRest {
                numberCell("builder.duration", row.restSeconds, unit: .seconds, set: { model.setRest(key: row.key, seconds: $0) },
                           less: { model.changeRest(key: row.key, steps: -1) }, more: { model.changeRest(key: row.key, steps: 1) })
                    .padding(.trailing, 4)
            } else {
                select("builder.step", value: roleOptions.first(where: { $0.role == row.role })?.label ?? "") {
                    ForEach(roleOptions, id: \.label) { option in
                        Button(option.label) { model.setRole(key: row.key, role: option.role) }
                    }
                }
                .padding(.trailing, 4)
            }
            if !row.isRest, let target = row.target {
                targetEditor(target)
            } else if !row.isRest {
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
        .padding(EdgeInsets(top: 10, leading: 12, bottom: 14, trailing: 8))
        .background(PoulpyColor.surface, in: RoundedRectangle(cornerRadius: PoulpyRadius.lg))
        .overlay(RoundedRectangle(cornerRadius: PoulpyRadius.lg).stroke(PoulpyColor.outline, lineWidth: 1))
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
                LucideIcon(.chevronDown, size: 16).foregroundStyle(PoulpyColor.onSurfaceVariant)
            }
            .padding(.leading, 12)
            .padding(.trailing, 10)
            .frame(maxWidth: .infinity, minHeight: 52)
            .background(PoulpyColor.surface, in: RoundedRectangle(cornerRadius: PoulpyRadius.md))
            .overlay(RoundedRectangle(cornerRadius: PoulpyRadius.md).stroke(PoulpyColor.outline, lineWidth: 1.5))
            .contentShape(RoundedRectangle(cornerRadius: PoulpyRadius.md))
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
        .background(PoulpyColor.background, in: RoundedRectangle(cornerRadius: PoulpyRadius.md))
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
                roundStep(.minus, "builder.less", less)
                if let onValue {
                    Button(action: onValue) { stepperValue(value, tappable: true) }
                        .buttonStyle(.plain)
                        .accessibilityHint(Text("builder.tapToType"))
                } else {
                    stepperValue(value, tappable: false)
                }
                roundStep(.plus, "builder.more", more)
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

    private func roundStep(_ symbol: Lucide, _ description: LocalizedStringKey, _ action: @escaping () -> Void) -> some View {
        Button(action: action) {
            LucideIcon(symbol, size: 22)
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
                    .background(PoulpyColor.surface, in: RoundedRectangle(cornerRadius: PoulpyRadius.lg))
                    .overlay(RoundedRectangle(cornerRadius: PoulpyRadius.lg).stroke(PoulpyColor.primary, lineWidth: 1.5))
                    .accessibilityLabel(Text(field.title))
                    .onChange(of: text) { _ in invalid = false }
                if field != .duration {
                    Text(field == .distance ? target.distanceUnit.symbol : target.paceUnit.symbol)
                        .font(.poulpy(.headline))
                        .foregroundStyle(PoulpyColor.onSurfaceVariant)
                }
            }
            if invalid {
                Label("builder.valueInvalid", lucide: .circleExclamationPoint)
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
                    .frame(maxWidth: .infinity)
            }
            .buttonStyle(PoulpyButtonStyle(.primary, size: .lg))
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
