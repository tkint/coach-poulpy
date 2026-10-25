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
            if open {
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

    private func stepper(_ label: LocalizedStringKey, _ value: String, less: @escaping () -> Void, more: @escaping () -> Void) -> some View {
        cell(label) {
            HStack(spacing: 2) {
                roundStep("minus", "builder.less", less)
                Text(value)
                    .font(.custom(PoulpyFontFace.nunitoExtraBold, size: 22, relativeTo: .title3))
                    .monospacedDigit()
                    .lineLimit(1)
                    .minimumScaleFactor(0.6)
                    .frame(maxWidth: .infinity)
                roundStep("plus", "builder.more", more)
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
