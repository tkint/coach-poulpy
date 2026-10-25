import SwiftUI
import Shared

/// The searchable catalog, used by the library and by the builder's picker: a pill
/// search, muscle chips, the count line, then card rows grouped by movement pattern,
/// or ranked results while searching.
struct ExerciseList<Row: View>: View {
    let graph: AppGraph
    let type: WorkoutType?
    /// The builder's picker: recently used exercises come first.
    let withRecent: Bool
    let row: (ExerciseRow) -> Row

    @State private var model: ExerciseListModel
    @State private var query = ""
    @State private var area: MuscleArea?
    @State private var ticked: Set<Equipment>?
    @State private var choosing = false

    init(graph: AppGraph, type: WorkoutType?, withRecent: Bool = false, @ViewBuilder row: @escaping (ExerciseRow) -> Row) {
        self.graph = graph
        self.type = type
        self.withRecent = withRecent
        self.row = row
        // Built once per list, so the search index and the history are read once.
        _model = State(initialValue: graph.exerciseList(languageCode: Language.code))
        _ticked = State(initialValue: graph.equipmentFilter(memory: UserDefaultsStore()).ticked)
    }

    /// Keeps the choice on the phone, for the library and the builder's picker alike.
    private func tick(_ value: Set<Equipment>?) {
        graph.equipmentFilter(memory: UserDefaultsStore()).ticked = value
        ticked = value
    }

    var body: some View {
        let model = self.model
        let listing = model.filter(query: query, area: area, type: type, ticked: ticked, withRecent: withRecent)
        VStack(spacing: 0) {
            PillSearchField(query: $query, placeholder: "exercises.search", label: "exercises.search.label")
                .padding(.horizontal, 20)
                .padding(.top, 8)
                .padding(.bottom, 12)
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    EquipmentChip(ticked: ticked) { choosing = true }
                    Rectangle()
                        .fill(PoulpyColor.outline)
                        .frame(width: 1, height: 24)
                        .accessibilityHidden(true)
                    PoulpyChip(title: Text("exercises.all"), selected: area == nil) { area = nil }
                    ForEach(model.muscleFilters, id: \.label) { filter in
                        PoulpyChip(title: Text(filter.label), selected: area == filter.area) {
                            area = area == filter.area ? nil : filter.area
                        }
                    }
                }
                .padding(.horizontal, 20)
            }
            HStack {
                Text(listing.countLabel)
                    .font(.poulpy(.subheadline).weight(.bold))
                    .foregroundStyle(PoulpyColor.onSurfaceVariant)
                Spacer()
                if area != nil || ticked != nil {
                    Button("exercises.clear.filters") {
                        area = nil
                        tick(nil)
                    }
                        .font(.poulpy(.subheadline).weight(.heavy))
                        .foregroundStyle(PoulpyColor.primary)
                }
            }
            .frame(minHeight: 40)
            .padding(.leading, 24)
            .padding(.trailing, 20)
            ScrollView {
                LazyVStack(alignment: .leading, spacing: 8) {
                    ForEach(Array(listing.sections.enumerated()), id: \.offset) { _, section in
                        if let title = section.title {
                            Text(title.uppercased())
                                .font(PoulpyFont.label)
                                .tracking(1)
                                .foregroundStyle(PoulpyColor.onSurfaceVariant)
                                .padding(.leading, 4)
                                .padding(.top, 10)
                                .accessibilityAddTraits(.isHeader)
                        }
                        ForEach(section.rows, id: \.slug) { exercise in row(exercise) }
                    }
                    if listing.count == 0 {
                        NoExercises(query: query.trimmingCharacters(in: .whitespaces)) {
                            query = ""
                            area = nil
                            tick(nil)
                        }
                    }
                }
                .padding(.horizontal, 20)
                .padding(.bottom, 20)
            }
        }
        .background(PoulpyColor.background)
        .sheet(isPresented: $choosing) {
            EquipmentSheet(
                options: model.equipmentOptions,
                ticked: ticked,
                fitting: { Int(model.filter(query: query, area: area, type: type, ticked: $0, withRecent: false).count) }
            ) { value in
                tick(value)
                choosing = false
            }
            .presentationDetents([.medium, .large])
        }
    }
}

/// The icon-only Equipment chip; filled, with how many items are ticked, while the filter is on.
private struct EquipmentChip: View {
    let ticked: Set<Equipment>?
    let action: () -> Void

    var body: some View {
        let on = ticked != nil
        Button(action: action) {
            HStack(spacing: 4) {
                Image(systemName: "dumbbell")
                    .font(.system(size: 15, weight: .semibold))
                if let ticked {
                    Text(verbatim: "\(ticked.count)")
                        .font(PoulpyFont.label)
                        .monospacedDigit()
                        .foregroundStyle(PoulpyColor.onPrimaryContainer)
                        .frame(minWidth: 18, minHeight: 18)
                        .padding(.horizontal, 2)
                        .background(PoulpyColor.surface, in: Capsule())
                }
            }
            .padding(.horizontal, 12)
            .frame(minWidth: 48)
            .frame(height: 36)
            .foregroundStyle(on ? PoulpyColor.primaryContainer : PoulpyColor.onSurface)
            .background(on ? PoulpyColor.onPrimaryContainer : PoulpyColor.surface, in: Capsule())
            .overlay(Capsule().stroke(on ? Color.clear : PoulpyColor.outline, lineWidth: 1.5))
        }
        .buttonStyle(.plain)
        .accessibilityLabel(ticked.map { Text("exercises.equipment.ticked \($0.count)") } ?? Text("exercises.equipment"))
        .accessibilityAddTraits(on ? [.isSelected] : [])
    }
}

/// Tick what you own: the list then only offers exercises it fits. "Show N exercises"
/// counts live and applies; "Clear" removes the filter.
private struct EquipmentSheet: View {
    let options: [EquipmentOption]
    let fitting: (Set<Equipment>) -> Int
    let apply: (Set<Equipment>?) -> Void
    @State private var pending: Set<Equipment>

    init(options: [EquipmentOption], ticked: Set<Equipment>?, fitting: @escaping (Set<Equipment>) -> Int, apply: @escaping (Set<Equipment>?) -> Void) {
        self.options = options
        self.fitting = fitting
        self.apply = apply
        _pending = State(initialValue: ticked ?? [])
    }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 12) {
                VStack(alignment: .leading, spacing: 2) {
                    Text("exercises.equipment").font(PoulpyFont.headline).accessibilityAddTraits(.isHeader)
                    Text("exercises.equipment.hint").font(.poulpy(.subheadline)).foregroundStyle(PoulpyColor.onSurfaceVariant)
                }
                VStack(spacing: 0) {
                    ForEach(options, id: \.label) { option in
                        let on = pending.contains(option.equipment)
                        Button {
                            if on { pending.remove(option.equipment) } else { pending.insert(option.equipment) }
                        } label: {
                            HStack(spacing: 14) {
                                Image(systemName: on ? "checkmark.square.fill" : "square")
                                    .font(.system(size: 22))
                                    .foregroundStyle(on ? PoulpyColor.primary : PoulpyColor.onSurfaceVariant)
                                Text(option.label).font(.poulpy(.callout).weight(.bold))
                                Spacer()
                                Text(option.countLabel)
                                    .font(.poulpy(.subheadline).weight(.bold))
                                    .monospacedDigit()
                                    .foregroundStyle(PoulpyColor.onSurfaceVariant)
                            }
                            .frame(minHeight: 48)
                            .contentShape(Rectangle())
                        }
                        .buttonStyle(.plain)
                        .accessibilityAddTraits(on ? [.isSelected] : [])
                        Divider()
                    }
                }
                HStack(spacing: 8) {
                    Button { apply(nil) } label: {
                        Text("exercises.equipment.clear")
                            .font(.poulpy(.callout).weight(.heavy))
                            .frame(maxWidth: .infinity)
                            .frame(height: 56)
                            .overlay(Capsule().stroke(PoulpyColor.outline, lineWidth: 2))
                    }
                    Button { apply(pending) } label: {
                        Text("exercises.equipment.show \(fitting(pending))")
                            .font(.poulpy(.callout).weight(.heavy))
                            .foregroundStyle(PoulpyColor.onPrimary)
                            .frame(maxWidth: .infinity)
                            .frame(height: 56)
                            .background(PoulpyColor.primary, in: Capsule())
                    }
                }
                .buttonStyle(.plain)
                .padding(.top, 4)
            }
            .padding(.horizontal, 20)
            .padding(.top, 24)
            .padding(.bottom, 28)
        }
        .background(PoulpyColor.surface)
    }
}

/// One exercise as a card: pattern badge, name, details, then a chevron, or a round plus in the builder's picker.
struct ExerciseRowView: View {
    let row: ExerciseRow
    var adding = false

    var body: some View {
        HStack(spacing: 12) {
            PatternBadge(pattern: row.pattern, size: 44)
                .accessibilityHidden(true)
            VStack(alignment: .leading, spacing: 1) {
                Text(row.name).font(.poulpy(.headline).weight(.heavy)).foregroundStyle(PoulpyColor.onSurface)
                Text(row.details).font(.poulpy(.subheadline)).foregroundStyle(PoulpyColor.onSurfaceVariant)
            }
            Spacer(minLength: 0)
            if adding {
                Image(systemName: "plus")
                    .font(.system(size: 16, weight: .bold))
                    .foregroundStyle(PoulpyColor.onPrimary)
                    .frame(width: 36, height: 36)
                    .background(PoulpyColor.primary, in: Circle())
                    .accessibilityHidden(true)
            } else {
                Image(systemName: "chevron.right")
                    .font(.system(size: 15, weight: .bold))
                    .foregroundStyle(PoulpyColor.onSurfaceVariant)
                    .accessibilityHidden(true)
            }
        }
        .padding(EdgeInsets(top: 10, leading: 12, bottom: 10, trailing: 12))
        .frame(minHeight: 64)
        .background(PoulpyColor.surface, in: RoundedRectangle(cornerRadius: 16))
        .overlay(RoundedRectangle(cornerRadius: 16).stroke(PoulpyColor.outline))
        .contentShape(RoundedRectangle(cornerRadius: 16))
    }
}

/// Poulpy, happy on the blue side, when nothing matches: a search shows what was typed.
private struct NoExercises: View {
    let query: String
    let clear: () -> Void

    var body: some View {
        VStack(spacing: 8) {
            Image("PoulpyMoodHappy")
                .resizable()
                .scaledToFit()
                .frame(width: 96, height: 96)
                .accessibilityHidden(true)
            Group {
                if query.isEmpty {
                    Text("exercises.no.fit")
                } else {
                    Text("exercises.nothing.for \(query)")
                }
            }
            .font(PoulpyFont.headline)
            .foregroundStyle(PoulpyColor.onPrimaryContainer)
            Text(LocalizedStringKey(query.isEmpty ? "exercises.no.fit.hint" : "exercises.nothing.hint"))
                .font(.poulpy(.subheadline))
                .foregroundStyle(PoulpyColor.onSurfaceVariant)
            Button(action: clear) {
                Text("exercises.clear.all")
                    .font(.poulpy(.subheadline).weight(.heavy))
                    .padding(.horizontal, 22)
                    .frame(height: 48)
                    .foregroundStyle(PoulpyColor.onPrimary)
                    .background(PoulpyColor.primary, in: Capsule())
            }
            .buttonStyle(.plain)
            .padding(.top, 8)
        }
        .multilineTextAlignment(.center)
        .frame(maxWidth: .infinity)
        .padding(.horizontal, 20)
        .padding(.vertical, 24)
        .background(PoulpyColor.primaryContainer, in: RoundedRectangle(cornerRadius: 24))
        .padding(.top, 16)
    }
}

/// The 48 high pill search with a 1.5 px outline, `primary` once something is typed, and a clear button.
struct PillSearchField: View {
    @Binding var query: String
    let placeholder: LocalizedStringKey
    let label: LocalizedStringKey

    var body: some View {
        HStack(spacing: 10) {
            Image(systemName: "magnifyingglass")
                .foregroundStyle(PoulpyColor.onSurfaceVariant)
                .accessibilityHidden(true)
            TextField(placeholder, text: $query)
                .font(PoulpyFont.body)
                .autocorrectionDisabled()
                .submitLabel(.search)
                .accessibilityLabel(Text(label))
            if !query.isEmpty {
                Button {
                    query = ""
                } label: {
                    Image(systemName: "xmark")
                        .font(.system(size: 13, weight: .bold))
                        .foregroundStyle(PoulpyColor.onSurfaceVariant)
                        .frame(width: 36, height: 36)
                        .background(PoulpyColor.surfaceVariant, in: Circle())
                }
                .buttonStyle(.plain)
                .accessibilityLabel(Text("exercises.clear.search"))
            }
        }
        .padding(.leading, 18)
        .padding(.trailing, 6)
        .frame(height: 48)
        .background(PoulpyColor.surface, in: Capsule())
        .overlay(Capsule().stroke(query.isEmpty ? PoulpyColor.outline : PoulpyColor.primary, lineWidth: 1.5))
    }
}

/// The design-system chip: 36 high, pill; on = `primary`, off = 1.5 px `outline`.
struct PoulpyChip: View {
    let title: Text
    let selected: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            title
                .font(.poulpy(.subheadline).weight(selected ? .heavy : .bold))
                .padding(.horizontal, 16)
                .frame(height: 36)
                .foregroundStyle(selected ? PoulpyColor.onPrimary : PoulpyColor.onSurface)
                .background(selected ? PoulpyColor.primary : PoulpyColor.surface, in: Capsule())
                .overlay(Capsule().stroke(selected ? Color.clear : PoulpyColor.outline, lineWidth: 1.5))
        }
        .buttonStyle(.plain)
        .accessibilityAddTraits(selected ? [.isSelected] : [])
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
