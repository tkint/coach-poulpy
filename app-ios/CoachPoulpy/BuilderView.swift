import SwiftUI
import UIKit
import Shared

struct BuilderView: View {
    let graph: AppGraph
    @StateObject private var builder: Observed<BuilderModel, BuilderState>
    @State private var picking = false
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

    @State private var open: String?
    @State private var count = 0
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
                        fieldLabel("builder.type")
                        FlowLayout(spacing: 8) {
                            ForEach(model.typeOptions, id: \.label) { option in
                                PoulpyChip(title: Text(option.label), selected: state.type == option.type) {
                                    model.setType(type: option.type)
                                }
                            }
                        }
                    }
                    if state.rows.isEmpty {
                        Text("builder.empty").font(.poulpy(.subheadline)).foregroundStyle(PoulpyColor.onSurfaceVariant)
                    }
                    ForEach(state.rows, id: \.key) { row in
                        let lifted = dragging == row.key
                        EntryCard(
                            model: model, row: row, open: open == row.key,
                            onOpen: { open = row.key },
                            onDrag: { drag(row.key, by: $0) },
                            onDrop: drop
                        )
                        .background(GeometryReader { geometry in
                            Color.clear.preference(key: CardFrames.self, value: [row.key: geometry.frame(in: .named("builder"))])
                        })
                        .offset(y: lifted ? dragStart + dragY - (frames[row.key]?.midY ?? dragStart) : 0)
                        .shadow(color: .black.opacity(lifted ? 0.18 : 0), radius: 12, y: 6)
                        .zIndex(lifted ? 1 : 0)
                        .transaction { if lifted { $0.animation = nil } }
                    }
                    Button {
                        picking = true
                    } label: {
                        Label("builder.add", systemImage: "plus")
                            .font(.poulpy(.body).weight(.heavy))
                            .foregroundStyle(PoulpyColor.primary)
                            .frame(maxWidth: .infinity, minHeight: 56)
                            .overlay(RoundedRectangle(cornerRadius: 16).stroke(PoulpyPalette.lagoon300, style: StrokeStyle(lineWidth: 2, dash: [8, 6])))
                            .contentShape(RoundedRectangle(cornerRadius: 16))
                    }
                    .buttonStyle(.plain)
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
            .onAppear {
                if count == 0 {
                    count = state.rows.count
                    open = state.rows.first?.key
                }
            }
            .onChange(of: state.rows.count) { rows in
                // A newly added exercise opens; one card is open at a time.
                if rows > count { open = state.rows.last?.key }
                count = rows
            }
            .sheet(isPresented: $picking) {
                NavigationStack {
                    ExerciseList(graph: graph, type: state.type, withRecent: true) { row in
                        Button {
                            model.add(slug: row.slug)
                            picking = false
                        } label: {
                            ExerciseRowView(row: row, adding: true)
                        }
                        .buttonStyle(.plain)
                    }
                    .navigationTitle("builder.pick.title")
                    .navigationBarTitleDisplayMode(.inline)
                    .toolbar {
                        ToolbarItem(placement: .cancellationAction) {
                            Button("action.cancel") { picking = false }
                        }
                    }
                }
            }
        }
    }

    /// Follows the finger; once the card's middle passes over another card, it takes that card's place.
    private func drag(_ key: String, by y: CGFloat) {
        if dragging == nil {
            dragging = key
            dragStart = frames[key]?.midY ?? 0
        }
        dragY = y
        let middle = dragStart + y
        let rows = state.rows
        guard
            let from = rows.firstIndex(where: { $0.key == key }),
            let to = rows.firstIndex(where: { row in
                guard row.key != key, let frame = frames[row.key] else { return false }
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

/// An exercise of the workout: open, its four settings in a 2×2 grid; folded, its name and summary.
private struct EntryCard: View {
    let model: BuilderModel
    let row: BuilderRow
    let open: Bool
    let onOpen: () -> Void
    let onDrag: (CGFloat) -> Void
    let onDrop: () -> Void
    @State private var weight: String
    @State private var editing: TargetField?

    init(model: BuilderModel, row: BuilderRow, open: Bool, onOpen: @escaping () -> Void, onDrag: @escaping (CGFloat) -> Void, onDrop: @escaping () -> Void) {
        self.model = model
        self.row = row
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
                Button(action: onOpen) {
                    VStack(alignment: .leading, spacing: 0) {
                        Text(row.name).font(.custom(PoulpyFontFace.baloo2Bold, size: 19, relativeTo: .headline))
                        if !open {
                            Text(row.summary).font(.poulpy(.subheadline)).monospacedDigit().foregroundStyle(PoulpyColor.onSurfaceVariant)
                        }
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .contentShape(Rectangle())
                }
                .buttonStyle(.plain)
                // Screen readers can't drag: they get Move up and Move down as actions.
                .accessibilityAction(named: Text("builder.move.up")) { model.moveUp(key: row.key) }
                .accessibilityAction(named: Text("builder.move.down")) { model.moveDown(key: row.key) }
                if open {
                    Button { model.remove(key: row.key) } label: {
                        Image(systemName: "trash").frame(width: 44, height: 44)
                    }
                    .buttonStyle(.plain)
                    .foregroundStyle(PoulpyColor.onSurfaceVariant)
                    .accessibilityLabel(Text(L("builder.remove.named %@", row.name)))
                } else {
                    Button(action: onOpen) {
                        Image(systemName: "chevron.down").font(.system(size: 15, weight: .bold)).frame(width: 44, height: 44)
                    }
                    .buttonStyle(.plain)
                    .foregroundStyle(PoulpyColor.onSurfaceVariant)
                    .accessibilityLabel(Text(L("builder.open %@", row.name)))
                }
            }
            if !row.fitsType {
                Label("builder.does.not.fit", systemImage: "exclamationmark.circle")
                    .font(.poulpy(.caption))
                    .foregroundStyle(PoulpyColor.error)
            }
            if open, let target = row.target {
                targetEditor(target)
            } else if open {
                let columns = [GridItem(.flexible(), spacing: 8), GridItem(.flexible(), spacing: 8)]
                LazyVGrid(columns: columns, spacing: 8) {
                    stepper("builder.sets", "\(row.sets)", less: { model.setSets(key: row.key, sets: row.sets - 1) }, more: { model.setSets(key: row.key, sets: row.sets + 1) })
                    stepper("builder.target", row.amountLabel, less: { model.changeAmount(key: row.key, steps: -1) }, more: { model.changeAmount(key: row.key, steps: 1) })
                    stepper("builder.rest", row.restLabel, less: { model.changeRest(key: row.key, steps: -1) }, more: { model.changeRest(key: row.key, steps: 1) })
                    if !row.isTimed { weightCell }
                }
                .padding(.trailing, 4)
            }
        }
        .padding(EdgeInsets(top: 12, leading: 12, bottom: open ? 14 : 12, trailing: 8))
        .background(PoulpyColor.surface, in: RoundedRectangle(cornerRadius: 16))
        .overlay(RoundedRectangle(cornerRadius: 16).stroke(open ? PoulpyPalette.lagoon300 : PoulpyColor.outline, lineWidth: open ? 1.5 : 1))
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

    /// A walk or a run: one block with a duration or a distance, and an optional pace, instead of sets.
    private func targetEditor(_ target: WalkRunTarget) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("builder.goal").textCase(.uppercase).font(PoulpyFont.label).kerning(1).foregroundStyle(PoulpyColor.onSurfaceVariant)
            FlowLayout(spacing: 8) {
                ForEach([TargetKind.duration, .distance, .durationPace, .distancePace], id: \.self) { kind in
                    PoulpyChip(title: Text(TargetField.label(of: kind)), selected: target.kind == kind) {
                        model.setTarget(key: row.key, kind: kind)
                    }
                }
            }
            if target.durationSeconds != nil {
                stepper("builder.duration", target.durationLabel,
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
            stepper("builder.rest", row.restLabel, less: { model.changeRest(key: row.key, steps: -1) }, more: { model.changeRest(key: row.key, steps: 1) })
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

    static func label(of kind: TargetKind) -> LocalizedStringKey {
        switch kind {
        case .distance: return "builder.distance"
        case .durationPace: return "builder.duration.pace"
        case .distancePace: return "builder.distance.pace"
        default: return "builder.duration"
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
