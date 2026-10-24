import SwiftUI
import Shared

struct BuilderView: View {
    let graph: AppGraph
    @StateObject private var builder: Observed<BuilderModel, BuilderState>
    @State private var picking = false
    @Environment(\.dismiss) private var dismiss

    init(graph: AppGraph, workoutId: String?) {
        self.graph = graph
        _builder = StateObject(wrappedValue: {
            let language = Language.code
            let model = workoutId.flatMap { graph.editWorkout(workoutId: $0, languageCode: language) }
                ?? graph.newWorkout(languageCode: language)
            return Observed(model, current: model.current, watch: model.watch, close: { $0.close() })
        }())
    }

    private var model: BuilderModel { builder.model }
    private var state: BuilderState { builder.value }

    var body: some View {
        NavigationStack {
            Form {
                Section {
                    TextField("builder.name", text: Binding(get: { state.name }, set: { model.rename(name: $0) }))
                }
                Section("builder.type") {
                    Picker("builder.type", selection: Binding(get: { state.type }, set: { model.setType(type: $0) })) {
                        ForEach(model.typeOptions, id: \.label) { option in
                            Text(option.label).tag(option.type)
                        }
                    }
                    .pickerStyle(.inline)
                    .labelsHidden()
                }
                if state.rows.isEmpty {
                    Text("builder.empty").foregroundStyle(.secondary)
                }
                ForEach(state.rows, id: \.key) { row in
                    Section {
                        EntryRow(model: model, row: row)
                    }
                }
                Section {
                    Button {
                        picking = true
                    } label: {
                        Label("builder.add", systemImage: "plus")
                    }
                }
            }
            .navigationTitle(LocalizedStringKey(model.isEditing ? "builder.edit.title" : "builder.new.title"))
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("action.cancel") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("action.save") {
                        if model.save() != nil { dismiss() }
                    }
                    .disabled(!state.canSave)
                }
            }
            .sheet(isPresented: $picking) {
                NavigationStack {
                    ExerciseList(graph: graph, type: state.type) { row in
                        Button {
                            model.add(slug: row.slug)
                            picking = false
                        } label: {
                            ExerciseRowView(row: row)
                        }
                        .foregroundStyle(.primary)
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
}

private struct EntryRow: View {
    let model: BuilderModel
    let row: BuilderRow
    @State private var weight: String

    init(model: BuilderModel, row: BuilderRow) {
        self.model = model
        self.row = row
        _weight = State(initialValue: row.loadKg.map { EntryRow.format($0.doubleValue) } ?? "")
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack(spacing: 12) {
                PatternBadge(pattern: row.pattern)
                VStack(alignment: .leading) {
                    Text(row.name).font(.headline)
                    if let perSide = row.perSideLabel {
                        Text(perSide).font(.caption).foregroundStyle(.secondary)
                    }
                }
                Spacer()
                Menu {
                    Button("builder.move.up") { model.moveUp(key: row.key) }
                    Button("builder.move.down") { model.moveDown(key: row.key) }
                    Button("builder.remove", role: .destructive) { model.remove(key: row.key) }
                } label: {
                    Image(systemName: "ellipsis.circle")
                }
            }
            if !row.fitsType {
                Text("builder.does.not.fit").font(.caption).foregroundStyle(.red)
            }
            stepper("builder.sets", "\(row.sets)", less: { model.setSets(key: row.key, sets: row.sets - 1) }, more: { model.setSets(key: row.key, sets: row.sets + 1) })
            stepper("builder.target", row.amountLabel, less: { model.changeAmount(key: row.key, steps: -1) }, more: { model.changeAmount(key: row.key, steps: 1) })
            stepper("builder.rest", row.restLabel, less: { model.changeRest(key: row.key, steps: -1) }, more: { model.changeRest(key: row.key, steps: 1) })
            if !row.isTimed {
                HStack {
                    Text("builder.weight")
                    Spacer()
                    TextField("0", text: $weight)
                        .keyboardType(.decimalPad)
                        .multilineTextAlignment(.trailing)
                        .frame(width: 80)
                        .onChange(of: weight) { text in
                            model.setLoadKg(key: row.key, kg: Double(text.replacingOccurrences(of: ",", with: ".")) ?? 0)
                        }
                }
            }
        }
        .buttonStyle(.borderless)
    }

    private func stepper(_ label: LocalizedStringKey, _ value: String, less: @escaping () -> Void, more: @escaping () -> Void) -> some View {
        HStack {
            Text(label)
            Spacer()
            Button(action: less) { Image(systemName: "minus.circle") }
            Text(value).monospacedDigit().frame(minWidth: 80)
            Button(action: more) { Image(systemName: "plus.circle") }
        }
    }

    private static func format(_ kg: Double) -> String {
        kg.truncatingRemainder(dividingBy: 1) == 0 ? String(Int(kg)) : String(kg)
    }
}
