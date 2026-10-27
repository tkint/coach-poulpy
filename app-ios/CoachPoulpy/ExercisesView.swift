import SwiftUI
import Shared

/// The searchable catalog, used by the library and by the builder's picker: a pill
/// search, muscle chips, the count line, then card rows grouped by movement pattern,
/// or ranked results while searching.
struct ExerciseList<Row: View>: View {
    let graph: AppGraph
    /// The builder's picker: recently used exercises come first.
    let withRecent: Bool
    let row: (ExerciseRow) -> Row

    @State private var model: ExerciseListModel
    @State private var query = ""
    @State private var area: MuscleArea?
    @State private var ticked: Set<Equipment>?
    @State private var choosing = false

    init(graph: AppGraph, withRecent: Bool = false, @ViewBuilder row: @escaping (ExerciseRow) -> Row) {
        self.graph = graph
        self.withRecent = withRecent
        self.row = row
        // Built once per list, so the search index and the history are read once.
        _model = State(initialValue: graph.exerciseList(languageCode: Language.code))
        _ticked = State(initialValue: graph.equipmentFilter(memory: UserDefaultsStore()).ticked)
    }

    /// Keeps the choice on the phone, for the library and the builder's picker alike.
    private func tick(_ value: Set<Equipment>?) {
        let filter = graph.equipmentFilter(memory: UserDefaultsStore())
        filter.ticked = value
        ticked = filter.ticked
    }

    var body: some View {
        let model = self.model
        let listing = model.filter(query: query, area: area, ticked: ticked, withRecent: withRecent)
        VStack(spacing: 0) {
            ListFilters(query: $query, placeholder: "exercises.search", label: "exercises.search.label", countLabel: listing.countLabel) {
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
            } action: {
                if area != nil || ticked != nil {
                    Button("exercises.clear.filters") {
                        area = nil
                        tick(nil)
                    }
                        .font(.poulpy(.subheadline).weight(.heavy))
                        .foregroundStyle(PoulpyColor.primary)
                }
            }
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
                fitting: { Int(model.filter(query: query, area: area, ticked: $0, withRecent: false).count) }
            ) { value in
                tick(value)
                choosing = false
            }
            .presentationDetents([.large])
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

/// Poulpy's card when nothing matches: a search shows what was typed.
private struct NoExercises: View {
    let query: String
    let clear: () -> Void

    var body: some View {
        PoulpyCard(
            title: query.isEmpty ? Text("exercises.no.fit") : Text("exercises.nothing.for \(query)"),
            message: Text(LocalizedStringKey(query.isEmpty ? "exercises.no.fit.hint" : "exercises.nothing.hint")),
            button: Text("exercises.clear.all"),
            action: clear
        )
        .padding(.top, 16)
    }
}

/// An exercise's page: a fixed picture area with Back and, in a variation group, the
/// "Variation 3 of 5" button that opens the variations sheet, so it never moves; then the
/// eyebrow, name, Muscles and Equipment cards and the steps, with "Add to a workout" pinned
/// at the bottom. Picking another variation replaces the page instead of stacking it.
struct ExerciseDetailView: View {
    let graph: AppGraph
    @State private var slug: String
    @State private var choosing = false
    @State private var adding = false
    @Environment(\.dismiss) private var dismiss

    init(graph: AppGraph, slug: String) {
        self.graph = graph
        _slug = State(initialValue: slug)
    }

    var body: some View {
        Group {
            if let detail = graph.exerciseList(languageCode: Language.code).detail(slug: slug) {
                page(detail)
            }
        }
        .toolbar(.hidden, for: .navigationBar)
    }

    private func page(_ detail: ExerciseDetail) -> some View {
        VStack(spacing: 0) {
            pictureArea(detail)
            ScrollView {
                VStack(alignment: .leading, spacing: 18) {
                    VStack(alignment: .leading, spacing: 6) {
                        Text(detail.eyebrow.uppercased())
                            .font(PoulpyFont.label)
                            .tracking(1)
                            .foregroundStyle(PoulpyColor.primary)
                        Text(detail.name)
                            .font(PoulpyFont.title)
                            .foregroundStyle(PoulpyColor.onBackground)
                            .accessibilityAddTraits(.isHeader)
                    }
                    HStack(alignment: .top, spacing: 12) {
                        factCard("exercise.muscles", detail.muscles)
                        factCard("exercise.equipment", detail.equipment)
                    }
                    VStack(alignment: .leading, spacing: 10) {
                        Text("exercise.how").font(PoulpyFont.headline).accessibilityAddTraits(.isHeader)
                        ForEach(Array(detail.instructions.enumerated()), id: \.offset) { index, step in
                            HStack(alignment: .top, spacing: 12) {
                                Text(verbatim: "\(index + 1)")
                                    .font(.poulpy(.subheadline).weight(.heavy))
                                    .foregroundStyle(PoulpyColor.onPrimary)
                                    .frame(width: 28, height: 28)
                                    .background(PoulpyColor.primary, in: Circle())
                                    .accessibilityHidden(true)
                                Text(step).font(PoulpyFont.body)
                            }
                        }
                    }
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(.horizontal, 20)
                .padding(.top, 20)
                .padding(.bottom, 16)
            }
            Divider()
            Button {
                adding = true
            } label: {
                Label("exercise.add", systemImage: "plus")
                    .font(.poulpy(.callout).weight(.heavy))
                    .foregroundStyle(PoulpyColor.onPrimary)
                    .frame(maxWidth: .infinity)
                    .frame(height: 56)
                    .background(PoulpyColor.primary, in: Capsule())
            }
            .buttonStyle(.plain)
            .padding(.horizontal, 20)
            .padding(.top, 12)
            .padding(.bottom, 8)
        }
        .background(PoulpyColor.background)
        .sheet(isPresented: $choosing) {
            VariationsSheet(variations: detail.variations) { picked in
                choosing = false
                slug = picked
            }
            .presentationDetents([.large])
        }
        .fullScreenCover(isPresented: $adding) {
            BuilderView(graph: graph, workoutId: nil, firstExercise: detail.slug)
        }
    }

    /// The pattern illustration until real pictures exist, with Back and the variations button over it.
    private func pictureArea(_ detail: ExerciseDetail) -> some View {
        ZStack(alignment: .top) {
            PoulpyColor.primaryContainer.ignoresSafeArea(edges: .top)
            PatternIllustration(pattern: detail.pattern)
                .padding(.top, 36)
                .frame(maxWidth: .infinity, maxHeight: .infinity)
            HStack {
                Button {
                    dismiss()
                } label: {
                    Image(systemName: "chevron.left")
                        .font(.system(size: 18, weight: .bold))
                        .foregroundStyle(PoulpyColor.onSurface)
                        .frame(width: 44, height: 44)
                        .background(PoulpyColor.surface, in: Circle())
                }
                .accessibilityLabel(Text("action.back"))
                Spacer()
                if !detail.variations.isEmpty {
                    Button {
                        choosing = true
                    } label: {
                        HStack(spacing: 8) {
                            Image(systemName: "chart.bar.fill").font(.system(size: 14, weight: .bold))
                            Text("exercise.variation.position \(detail.variationPosition)")
                            Image(systemName: "chevron.down").font(.system(size: 12, weight: .bold))
                        }
                        .font(.poulpy(.subheadline).weight(.heavy))
                        .foregroundStyle(PoulpyColor.onPrimaryContainer)
                        .padding(.leading, 14)
                        .padding(.trailing, 12)
                        .frame(height: 44)
                        .background(PoulpyColor.surface, in: Capsule())
                    }
                    .accessibilityElement(children: .ignore)
                    .accessibilityLabel(Text("exercise.variations.a11y \(detail.variationPosition)"))
                    .accessibilityAddTraits(.isButton)
                }
            }
            .buttonStyle(.plain)
            .padding(.horizontal, 16)
            .padding(.top, 4)
        }
        .frame(height: 212)
    }

    private func factCard(_ title: LocalizedStringKey, _ body: String) -> some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(title)
                .textCase(.uppercase)
                .font(PoulpyFont.label)
                .tracking(1)
                .foregroundStyle(PoulpyColor.onSurfaceVariant)
            Text(body).font(.poulpy(.callout).weight(.bold)).foregroundStyle(PoulpyColor.onSurface)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(.horizontal, 14)
        .padding(.vertical, 12)
        .background(PoulpyColor.surface, in: RoundedRectangle(cornerRadius: 16))
    }
}

/// Stands in for the exercise picture: the pattern's symbol, large.
private struct PatternIllustration: View {
    let pattern: MovementPattern

    var body: some View {
        PatternBadge(pattern: pattern, size: 140)
            .accessibilityLabel(Text("exercise.picture"))
    }
}

/// The group easiest at the top, hardest at the bottom, each with a difficulty ladder and its note.
private struct VariationsSheet: View {
    let variations: [VariationRow]
    let pick: (String) -> Void
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 12) {
                VStack(alignment: .leading, spacing: 2) {
                    Text("exercise.variations").font(PoulpyFont.headline).accessibilityAddTraits(.isHeader)
                    Text("exercise.variations.hint").font(.poulpy(.subheadline)).foregroundStyle(PoulpyColor.onSurfaceVariant)
                }
                VStack(spacing: 6) {
                    ForEach(Array(variations.enumerated()), id: \.element.slug) { rung, variation in
                        Button {
                            if variation.current { dismiss() } else { pick(variation.slug) }
                        } label: {
                            row(rung: rung, variation: variation)
                        }
                        .buttonStyle(.plain)
                        .accessibilityAddTraits(variation.current ? [.isSelected] : [])
                    }
                }
            }
            .padding(.horizontal, 20)
            .padding(.top, 24)
            .padding(.bottom, 28)
        }
        .background(PoulpyColor.surface)
    }

    private func row(rung: Int, variation: VariationRow) -> some View {
        HStack(spacing: 12) {
            HStack(alignment: .bottom, spacing: 3) {
                ForEach(0..<variations.count, id: \.self) { bar in
                    RoundedRectangle(cornerRadius: 2)
                        .fill(bar <= rung ? PoulpyColor.primary : PoulpyColor.outline)
                        .frame(width: 5, height: CGFloat(8 + bar * 16 / max(variations.count - 1, 1)))
                }
            }
            .frame(width: 40, height: 24, alignment: .bottomLeading)
            .accessibilityHidden(true)
            VStack(alignment: .leading, spacing: 0) {
                Text(variation.name).font(.poulpy(.callout).weight(.heavy)).foregroundStyle(PoulpyColor.onSurface)
                Group {
                    if variation.current {
                        Text("exercise.variation.current")
                    } else {
                        Text(variation.note)
                    }
                }
                .font(.poulpy(.subheadline))
                .foregroundStyle(PoulpyColor.onSurfaceVariant)
            }
            Spacer(minLength: 0)
            if variation.current {
                Image(systemName: "checkmark")
                    .font(.system(size: 18, weight: .bold))
                    .foregroundStyle(PoulpyColor.primary)
                    .accessibilityHidden(true)
            }
        }
        .padding(.horizontal, 12)
        .padding(.vertical, 8)
        .frame(minHeight: 60)
        .background(variation.current ? PoulpyColor.primaryContainer : PoulpyColor.surface, in: RoundedRectangle(cornerRadius: 16))
        .overlay(
            RoundedRectangle(cornerRadius: 16)
                .stroke(variation.current ? PoulpyColor.primary : PoulpyColor.outline, lineWidth: variation.current ? 1.5 : 1)
        )
        .contentShape(RoundedRectangle(cornerRadius: 16))
    }
}
