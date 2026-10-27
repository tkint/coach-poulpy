import SwiftUI
import UIKit
import Shared

struct BuilderView: View {
    let graph: AppGraph
    @StateObject private var builder: Observed<BuilderModel, BuilderState>
    /// Where the exercise being picked goes (the workout, or a repeat) and as what; nil when not picking.
    @State private var picking: PickTarget?
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
    // The card being dragged, where its middle started and how far the finger went.
    @State private var dragging: String?
    @State private var dragStart: CGFloat = 0
    @State private var dragY: CGFloat = 0
    @State private var frames: [String: CGRect] = [:]

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 14) {
                    VStack(alignment: .leading, spacing: 6) {
                        fieldLabel("builder.name")
                        TextField("", text: Binding(get: { state.name }, set: { model.rename(name: $0) }))
                            .font(.poulpy(.body).weight(.bold))
                            .padding(.horizontal, 16)
                            .frame(height: 52)
                            .background(PoulpyColor.surface, in: RoundedRectangle(cornerRadius: 16))
                            .overlay(RoundedRectangle(cornerRadius: 16).stroke(state.name.isEmpty ? PoulpyColor.outline : PoulpyColor.primary, lineWidth: 1.5))
                            .accessibilityLabel(Text("builder.name"))
                    }
                    VStack(alignment: .leading, spacing: 8) {
                        fieldLabel("builder.format")
                        FlowLayout(spacing: 8) {
                            ForEach(model.formatOptions, id: \.label) { option in
                                PoulpyChip(title: Text(option.label), selected: state.format == option.format) {
                                    model.setFormat(format: option.format)
                                }
                            }
                        }
                    }
                    if state.steps.isEmpty {
                        Text("builder.empty").font(.poulpy(.subheadline)).foregroundStyle(PoulpyColor.onSurfaceVariant)
                    }
                    ForEach(state.steps, id: \.key) { step in
                        if let row = step.row {
                            card(row).liftable(step.key, dragging: dragging, dragStart: dragStart, dragY: dragY, frames: frames)
                        } else if let group = step.repeatGroup {
                            repeatBlock(group).liftable(step.key, dragging: dragging, dragStart: dragStart, dragY: dragY, frames: frames)
                        }
                    }
                    HStack(spacing: 8) {
                        addStepButton("builder.add.step", height: 56, to: nil)
                        Button { model.addRepeat() } label: {
                            dashedTile("builder.add.repeat", systemImage: "repeat", height: 56)
                        }
                        .buttonStyle(.plain)
                    }
                }
                .padding(.horizontal, 20)
                .padding(.top, 8)
                .padding(.bottom, PoulpySpace.xl)
                .coordinateSpace(name: "builder")
                .onPreferenceChange(CardFrames.self) { frames = $0 }
            }
            .background(PoulpyColor.background)
            .navigationTitle(LocalizedStringKey(model.isEditing ? "builder.edit.title" : "builder.new.title"))
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
                    StepSheet(model: model, row: row, roleOptions: model.roleOptions) { editing = nil }
                }
            }
            .sheet(item: $picking) { target in
                NavigationStack {
                    ExerciseList(graph: graph, withRecent: true) { row in
                        Button {
                            model.add(slug: row.slug, repeatKey: target.repeatKey, role: target.role)
                            picking = nil
                        } label: {
                            ExerciseRowView(row: row, adding: true)
                        }
                        .buttonStyle(.plain)
                    }
                    .navigationTitle("builder.pick.title")
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

    private func card(_ row: BuilderRow) -> some View {
        EntryCard(
            model: model, row: row, roleOptions: model.roleOptions, open: false,
            onOpen: { editing = row.key },
            onDrag: { drag(row.key, by: $0) },
            onDrop: drop
        )
    }

    /// A repeat: its header with − N times +, its steps inside a tinted block, and a way to add one.
    private func repeatBlock(_ group: BuilderRepeat) -> some View {
        VStack(alignment: .leading, spacing: 6) {
            HStack(spacing: 8) {
                dragHandle(group.key)
                Image(systemName: "repeat").font(.system(size: 17, weight: .bold)).foregroundStyle(PoulpyColor.primary)
                Text("builder.repeat")
                    .font(.custom(PoulpyFontFace.baloo2Bold, size: 19, relativeTo: .headline))
                    .foregroundStyle(PoulpyColor.onPrimaryContainer)
                    .frame(maxWidth: .infinity, alignment: .leading)
                HStack(spacing: 4) {
                    timesButton("minus", "builder.repeat.fewer") { model.setRepeatTimes(key: group.key, times: group.times - 1) }
                    Text(group.timesLabel)
                        .font(.poulpy(.subheadline).weight(.heavy))
                        .monospacedDigit()
                        .foregroundStyle(PoulpyColor.onPrimaryContainer)
                        .frame(minWidth: 58)
                    timesButton("plus", "builder.repeat.more") { model.setRepeatTimes(key: group.key, times: group.times + 1) }
                }
                .padding(4)
                .background(PoulpyColor.surface.opacity(0.6), in: Capsule())
                Menu {
                    Button { model.duplicate(key: group.key) } label: { Label("builder.duplicate", systemImage: "plus.square.on.square") }
                    Divider()
                    Button(role: .destructive) { model.remove(key: group.key) } label: { Label("builder.delete", systemImage: "trash") }
                } label: {
                    Image(systemName: "ellipsis").font(.system(size: 17, weight: .bold)).frame(width: 44, height: 44)
                }
                .foregroundStyle(PoulpyColor.onSurfaceVariant)
                .accessibilityLabel(Text("builder.repeat.actions"))
            }
            .accessibilityElement(children: .contain)
            .accessibilityAction(named: Text("builder.move.up")) { model.moveUp(key: group.key) }
            .accessibilityAction(named: Text("builder.move.down")) { model.moveDown(key: group.key) }
            VStack(spacing: 6) {
                ForEach(group.rows, id: \.key) { row in
                    card(row).liftable(row.key, dragging: dragging, dragStart: dragStart, dragY: dragY, frames: frames)
                }
                addStepButton("builder.add.to.repeat", height: 44, to: group.key)
            }
            .padding(.leading, 10)
        }
        .padding(EdgeInsets(top: 8, leading: 12, bottom: 10, trailing: 8))
        .background(PoulpyColor.primaryContainer, in: RoundedRectangle(cornerRadius: 16))
    }

    private func dragHandle(_ key: String) -> some View {
        Image(systemName: "line.3.horizontal")
            .font(.system(size: 16, weight: .bold))
            .foregroundStyle(PoulpyPalette.neutral500)
            .frame(width: 28, height: 44)
            .contentShape(Rectangle())
            .highPriorityGesture(
                DragGesture(minimumDistance: 1, coordinateSpace: .named("builder"))
                    .onChanged { drag(key, by: $0.translation.height) }
                    .onEnded { _ in drop() }
            )
            .accessibilityHidden(true)
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

    /// "Add a step": asks what the step is for. A warm-up, an exercise, a recovery or a cool down
    /// is picked from the exercise list; a rest is added right away. A repeat offers no warm-up
    /// or cool down, which belong at the ends of the workout.
    private func addStepButton(_ label: LocalizedStringKey, height: CGFloat, to repeatKey: String?) -> some View {
        Menu {
            if repeatKey == nil {
                Button { picking = PickTarget(repeatKey: nil, role: .warmUp) } label: { Label("builder.add.warm.up", systemImage: "sun.max") }
            }
            Button { picking = PickTarget(repeatKey: repeatKey, role: .work) } label: { Label("builder.add.exercise", systemImage: "dumbbell") }
            Button { picking = PickTarget(repeatKey: repeatKey, role: .recover) } label: { Label("builder.add.recover", systemImage: "figure.walk") }
            Button { model.addRest(repeatKey: repeatKey) } label: { Label("builder.add.rest", systemImage: "timer") }
            if repeatKey == nil {
                Button { picking = PickTarget(repeatKey: nil, role: .coolDown) } label: { Label("builder.add.cool.down", systemImage: "moon") }
            }
        } label: {
            dashedTile(label, systemImage: "plus", height: height)
        }
        .buttonStyle(.plain)
    }

    private func dashedTile(_ label: LocalizedStringKey, systemImage: String, height: CGFloat) -> some View {
        Label(label, systemImage: systemImage)
            .font(.poulpy(height < 56 ? .subheadline : .body).weight(.heavy))
            .foregroundStyle(PoulpyColor.primary)
            .frame(maxWidth: .infinity, minHeight: height)
            .overlay(RoundedRectangle(cornerRadius: height < 56 ? 12 : 16).stroke(PoulpyPalette.lagoon300, style: StrokeStyle(lineWidth: 2, dash: [8, 6])))
            .contentShape(RoundedRectangle(cornerRadius: 16))
    }

    /// The keys of the list holding [key]: the workout's steps, or its repeat's.
    private func siblings(_ key: String) -> [String] {
        let top = state.steps.map(\.key)
        if top.contains(key) { return top }
        for step in state.steps {
            if let rows = step.repeatGroup?.rows.map(\.key), rows.contains(key) { return rows }
        }
        return []
    }

    /// Follows the finger; once the card's middle passes over another step of its list, it takes that step's place.
    private func drag(_ key: String, by y: CGFloat) {
        if dragging == nil {
            dragging = key
            dragStart = frames[key]?.midY ?? 0
        }
        dragY = y
        let middle = dragStart + y
        let keys = siblings(key)
        guard
            let from = keys.firstIndex(of: key),
            let to = keys.firstIndex(where: { other in
                guard other != key, let frame = frames[other] else { return false }
                return frame.minY <= middle && middle < frame.maxY
            }),
            to != from
        else { return }
        withAnimation(.spring(response: 0.3, dampingFraction: 0.85)) {
            model.move(key: key, toIndex: Int32(to))
        }
        UIImpactFeedbackGenerator(style: .light).impactOccurred()
    }

    private func drop() {
        withAnimation(.spring(response: 0.3, dampingFraction: 0.85)) {
            dragging = nil
            dragY = 0
        }
    }

    private func fieldLabel(_ key: LocalizedStringKey) -> some View {
        Text(key).textCase(.uppercase).font(PoulpyFont.label).kerning(1).foregroundStyle(PoulpyColor.onSurfaceVariant)
    }
}

/// Where a picked exercise goes: the workout, or the repeat [repeatKey]; and what it is for.
private struct PickTarget: Identifiable {
    let repeatKey: String?
    let role: BuilderRole

    var id: String { "\(repeatKey ?? "")-\(role.name)" }
}

/// Edits one step in a sheet: the open card, and Done.
private struct StepSheet: View {
    let model: BuilderModel
    let row: BuilderRow
    let roleOptions: [StepRoleOption]
    let onDone: () -> Void

    var body: some View {
        ScrollView {
            VStack(spacing: 14) {
                EntryCard(model: model, row: row, roleOptions: roleOptions, open: true, onOpen: {}, onDrag: { _ in }, onDrop: {})
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
    }
}

extension View {
    /// Lifts the step being dragged above the others with a shadow, and records where each step sits.
    fileprivate func liftable(_ key: String, dragging: String?, dragStart: CGFloat, dragY: CGFloat, frames: [String: CGRect]) -> some View {
        let lifted = dragging == key
        return self
            .background(GeometryReader { geometry in
                Color.clear.preference(key: CardFrames.self, value: [key: geometry.frame(in: .named("builder"))])
            })
            .offset(y: lifted ? dragStart + dragY - (frames[key]?.midY ?? dragStart) : 0)
            .shadow(color: .black.opacity(lifted ? 0.18 : 0), radius: 12, y: 6)
            .zIndex(lifted ? 1 : 0)
            .transaction { if lifted { $0.animation = nil } }
    }
}

/// A step of the workout. Folded: its role, name and summary. Open: what the step is for, then
/// sets, reps and weight for an exercise, a duration and intensity for a walk or a run, or the
/// length of a rest.
private struct EntryCard: View {
    let model: BuilderModel
    let row: BuilderRow
    let roleOptions: [StepRoleOption]
    let open: Bool
    let onOpen: () -> Void
    let onDrag: (CGFloat) -> Void
    let onDrop: () -> Void
    @State private var weight: String
    @State private var editing: TargetField?

    init(model: BuilderModel, row: BuilderRow, roleOptions: [StepRoleOption], open: Bool, onOpen: @escaping () -> Void, onDrag: @escaping (CGFloat) -> Void, onDrop: @escaping () -> Void) {
        self.model = model
        self.row = row
        self.roleOptions = roleOptions
        self.open = open
        self.onOpen = onOpen
        self.onDrag = onDrag
        self.onDrop = onDrop
        _weight = State(initialValue: row.loadKg.map { EntryCard.format($0.doubleValue) } ?? "")
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack(spacing: 10) {
                // Only the handle drags, so the rest of the card still scrolls the list.
                if !open {
                Image(systemName: "line.3.horizontal")
                    .font(.system(size: 16, weight: .bold))
                    .foregroundStyle(PoulpyPalette.neutral500)
                    .frame(width: 28, height: 44)
                    .contentShape(Rectangle())
                    .highPriorityGesture(
                        DragGesture(minimumDistance: 1, coordinateSpace: .named("builder"))
                            .onChanged { onDrag($0.translation.height) }
                            .onEnded { _ in onDrop() }
                    )
                    .accessibilityHidden(true)
                }
                Button(action: onOpen) {
                    VStack(alignment: .leading, spacing: 2) {
                        if let role = row.roleLabel { roleTag(role) }
                        Text(row.name).font(.custom(PoulpyFontFace.baloo2Bold, size: 19, relativeTo: .headline))
                        if !open || row.isRest {
                            Text(row.summary).font(.poulpy(.subheadline)).monospacedDigit().foregroundStyle(PoulpyColor.onSurfaceVariant)
                        }
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .contentShape(Rectangle())
                }
                .buttonStyle(.plain)
                // Screen readers can't drag: they get the moves as actions.
                .accessibilityAction(named: Text("builder.move.up")) { model.moveUp(key: row.key) }
                .accessibilityAction(named: Text("builder.move.down")) { model.moveDown(key: row.key) }
                .accessibilityActions {
                    if row.canMoveIntoRepeat {
                        Button("builder.move.into.repeat") { model.moveIntoRepeatAbove(key: row.key) }
                    }
                    if row.repeatKey != nil {
                        Button("builder.move.out.of.repeat") { model.moveOutOfRepeat(key: row.key) }
                    }
                }
                if !open {
                    stepMenu
                }
            }
            if !row.fitsFormat {
                Label("builder.does.not.fit", systemImage: "exclamationmark.circle")
                    .font(.poulpy(.caption))
                    .foregroundStyle(PoulpyColor.error)
            }
            if open && row.isRest {
                stepper("builder.duration", row.restLabel, less: { model.changeRest(key: row.key, steps: -1) }, more: { model.changeRest(key: row.key, steps: 1) })
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
                let columns = [GridItem(.flexible(), spacing: 8), GridItem(.flexible(), spacing: 8)]
                LazyVGrid(columns: columns, spacing: 8) {
                    stepper("builder.sets", "\(row.sets)", less: { model.setSets(key: row.key, sets: row.sets - 1) }, more: { model.setSets(key: row.key, sets: row.sets + 1) })
                    stepper("builder.target", row.amountLabel, less: { model.changeAmount(key: row.key, steps: -1) }, more: { model.changeAmount(key: row.key, steps: 1) })
                    if !row.isTimed { weightCell }
                }
                .padding(.trailing, 4)
            }
        }
        .padding(EdgeInsets(top: 10, leading: 12, bottom: open ? 14 : 10, trailing: 8))
        .background(PoulpyColor.surface, in: RoundedRectangle(cornerRadius: 16))
        .overlay(RoundedRectangle(cornerRadius: 16).stroke(row.fitsFormat ? PoulpyColor.outline : PoulpyColor.error, lineWidth: 1))
    }

    /// The small tag above a step's name: "WARM-UP", "RECOVER", "COOL DOWN".
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

    /// Edit, duplicate, the moves into or out of a repeat when they apply, and delete.
    private var stepMenu: some View {
        Menu {
            Button(action: onOpen) { Label("builder.edit", systemImage: "pencil") }
            Button { model.duplicate(key: row.key) } label: { Label("builder.duplicate", systemImage: "plus.square.on.square") }
            if row.canMoveIntoRepeat {
                Button { model.moveIntoRepeatAbove(key: row.key) } label: { Label("builder.move.into.repeat", systemImage: "repeat") }
            }
            if row.repeatKey != nil {
                Button { model.moveOutOfRepeat(key: row.key) } label: { Label("builder.move.out.of.repeat", systemImage: "rectangle.portrait.and.arrow.right") }
            }
            Divider()
            Button(role: .destructive) { model.remove(key: row.key) } label: { Label("builder.delete", systemImage: "trash") }
        } label: {
            Image(systemName: "ellipsis").font(.system(size: 17, weight: .bold)).frame(width: 44, height: 44)
        }
        .foregroundStyle(PoulpyColor.onSurfaceVariant)
        .accessibilityLabel(Text(L("builder.more.actions %@", row.name)))
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
                        .accessibilityHint(Text("builder.tap.to.type"))
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
        let untilPressed = row.isLast ? "builder.until.end" : "builder.until.pressed"
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
            Text([target.hint.map { "≈ \($0)" }, NSLocalizedString("builder.tap.to.type", comment: "")].compactMap { $0 }.joined(separator: " · "))
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

    private var weightCell: some View {
        cell("builder.weight") {
            HStack(spacing: 4) {
                TextField("0", text: $weight)
                    .keyboardType(.decimalPad)
                    .multilineTextAlignment(.center)
                    .font(.custom(PoulpyFontFace.nunitoExtraBold, size: 22, relativeTo: .title3))
                    .frame(width: 60, height: 40)
                    .background(PoulpyColor.surface, in: RoundedRectangle(cornerRadius: 10))
                    .overlay(RoundedRectangle(cornerRadius: 10).stroke(PoulpyColor.outline, lineWidth: 1.5))
                    .accessibilityLabel(Text("builder.weight.kg"))
                    .onChange(of: weight) { text in
                        model.setLoadKg(key: row.key, kg: Double(text.replacingOccurrences(of: ",", with: ".")) ?? 0)
                    }
                Text("kg").font(.poulpy(.body).weight(.bold)).foregroundStyle(PoulpyColor.onSurfaceVariant)
            }
            .frame(height: 44)
        }
    }

    private static func format(_ kg: Double) -> String {
        kg.truncatingRemainder(dividingBy: 1) == 0 ? String(Int(kg)) : String(kg)
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
                Label("builder.value.invalid", systemImage: "exclamationmark.circle")
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
            sectionLabel("builder.quick.picks")
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

/// Where each exercise card sits in the builder, so a drag knows which card it is over.
private struct CardFrames: PreferenceKey {
    static var defaultValue: [String: CGRect] = [:]
    static func reduce(value: inout [String: CGRect], nextValue: () -> [String: CGRect]) {
        value.merge(nextValue()) { $1 }
    }
}

/// A localized string with printf-style arguments, as in Localizable.strings.
private func L(_ key: String, _ args: CVarArg...) -> String {
    String(format: NSLocalizedString(key, comment: ""), locale: Locale.current, arguments: args)
}
