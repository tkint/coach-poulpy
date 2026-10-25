import SwiftUI
import Shared

/// The searchable catalog, used by the library and by the builder's picker.
struct ExerciseList<Row: View>: View {
    let graph: AppGraph
    let type: WorkoutType?
    let row: (ExerciseRow) -> Row

    @State private var query = ""
    @State private var muscle: MuscleGroup?

    init(graph: AppGraph, type: WorkoutType?, @ViewBuilder row: @escaping (ExerciseRow) -> Row) {
        self.graph = graph
        self.type = type
        self.row = row
    }

    private var model: ExerciseListModel { graph.exerciseList(languageCode: Language.code) }

    var body: some View {
        let model = self.model
        let rows = model.filter(query: query, muscle: muscle, type: type)
        List {
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    Chip(title: Text("exercises.all"), selected: muscle == nil) { muscle = nil }
                    ForEach(model.muscleFilters, id: \.label) { filter in
                        Chip(title: Text(filter.label), selected: muscle == filter.group) {
                            muscle = muscle == filter.group ? nil : filter.group
                        }
                    }
                }
            }
            .listRowInsets(EdgeInsets(top: 8, leading: 16, bottom: 8, trailing: 16))
            if rows.isEmpty {
                Text("exercises.none").foregroundStyle(.secondary)
            }
            ForEach(rows, id: \.slug) { exercise in row(exercise) }
        }
        .listStyle(.plain)
        .searchable(text: $query, prompt: Text("exercises.search"))
    }
}

struct ExerciseRowView: View {
    let row: ExerciseRow

    var body: some View {
        HStack(spacing: 12) {
            PatternBadge(pattern: row.pattern)
            VStack(alignment: .leading, spacing: 2) {
                Text(row.name)
                Text(row.details).font(.poulpy(.footnote)).foregroundStyle(.secondary)
            }
        }
    }
}

private struct Chip: View {
    let title: Text
    let selected: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            ChipLabel(title: title, selected: selected)
        }
        .buttonStyle(.plain)
    }
}

/// A chip's pill, for when the tap belongs to something else, like a navigation link.
private struct ChipLabel: View {
    let title: Text
    let selected: Bool

    var body: some View {
        title
            .font(.poulpy(.subheadline))
            .padding(.horizontal, 12)
            .padding(.vertical, 6)
            .background(selected ? Color.accentColor : Color.secondary.opacity(0.15), in: Capsule())
            .foregroundStyle(selected ? Color.white : Color.primary)
    }
}

struct ExerciseDetailView: View {
    let graph: AppGraph
    let slug: String

    var body: some View {
        if let detail = graph.exerciseList(languageCode: Language.code).detail(slug: slug) {
            ScrollView {
                VStack(alignment: .leading, spacing: 16) {
                    HStack(spacing: 16) {
                        PatternBadge(pattern: detail.pattern, size: 72)
                        Text(detail.patternLabel).font(.poulpy(.headline))
                    }
                    section("exercise.muscles", detail.muscles)
                    section("exercise.equipment", detail.equipment)
                    if detail.perSide {
                        Text("exercise.per.side")
                    }
                    Text("exercise.how").font(.poulpy(.headline))
                    ForEach(Array(detail.instructions.enumerated()), id: \.offset) { index, step in
                        HStack(alignment: .top, spacing: 12) {
                            Text("\(index + 1).").foregroundStyle(Color.accentColor)
                            Text(step)
                        }
                    }
                    if !detail.variations.isEmpty {
                        variations(detail.variations)
                    }
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding()
            }
            .navigationTitle(detail.name)
        }
    }

    /// The exercise's group as one line of pills, easiest first; this exercise is checked in place and the others open.
    private func variations(_ variations: [VariationRow]) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("exercise.variations").font(.poulpy(.headline))
            Text("exercise.variations.hint").font(.poulpy(.subheadline)).foregroundStyle(PoulpyColor.onSurfaceVariant)
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    ForEach(variations, id: \.slug) { variation in
                        if variation.current {
                            ChipLabel(title: Text("\(Image(systemName: "checkmark")) \(variation.name)"), selected: true)
                                .accessibilityElement(children: .ignore)
                                .accessibilityLabel(Text(variation.name))
                                .accessibilityValue(Text("exercise.variation.current"))
                        } else {
                            NavigationLink {
                                ExerciseDetailView(graph: graph, slug: variation.slug)
                            } label: {
                                ChipLabel(title: Text(variation.name), selected: false)
                            }
                            .buttonStyle(.plain)
                        }
                    }
                }
            }
        }
    }

    private func section(_ title: LocalizedStringKey, _ body: String) -> some View {
        VStack(alignment: .leading, spacing: 2) {
            Text(title).font(.poulpy(.caption)).foregroundStyle(Color.accentColor)
            Text(body)
        }
    }
}
