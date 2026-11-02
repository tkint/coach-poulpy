import SwiftUI
import Shared

/// The sheet a filter chip opens.
private enum FilterSheet: Identifiable {
    case equipment, focus, position, zones, tags
    var id: Self { self }
}

/// The searchable catalog, used by the library and by the builder's picker: a pill
/// search, five filter chips (equipment, focus, position, zones, tags) that each open a
/// sheet, the count line, then card rows grouped by movement pattern, or ranked
/// results while searching.
struct ExerciseList<Row: View>: View {
    let graph: AppGraph
    /// The builder's picker: recently used exercises come first.
    let withRecent: Bool
    let row: (ExerciseRow) -> Row

    @State private var model: ExerciseListModel
    @State private var query = ""
    @State private var ticked: Set<Equipment>?
    @State private var focus: Set<ExerciseFocus> = []
    @State private var positions: Set<BodyPosition> = []
    @State private var tags: Set<ExerciseTag> = []
    @State private var picked: Set<String> = []
    @State private var sheet: FilterSheet?
    private let figure: BodyFigure

    init(graph: AppGraph, withRecent: Bool = false, @ViewBuilder row: @escaping (ExerciseRow) -> Row) {
        self.graph = graph
        self.withRecent = withRecent
        self.row = row
        // Built once per list, so the search index and the history are read once.
        _model = State(initialValue: graph.exerciseList(languageCode: Language.code))
        _ticked = State(initialValue: graph.equipmentFilter(memory: UserDefaultsStore()).ticked)
        figure = (graph.profile(memory: UserDefaultsStore()).ui.value as! ProfileUi).bodyFigure ?? .shoulders
    }

    /// Keeps the choice on the phone, for the library and the builder's picker alike.
    private func tick(_ value: Set<Equipment>?) {
        let filter = graph.equipmentFilter(memory: UserDefaultsStore())
        filter.ticked = value
        ticked = filter.ticked
    }

    private func clearFilters() {
        tick(nil)
        focus = []
        positions = []
        tags = []
        picked = []
    }

    /// How many exercises the list would hold with one filter changed, for "Show N exercises".
    private func count(
        ticked: Set<Equipment>? = nil,
        focus: Set<ExerciseFocus>? = nil,
        positions: Set<BodyPosition>? = nil,
        tags: Set<ExerciseTag>? = nil,
        picked: Set<String>? = nil
    ) -> Int {
        let zones = Zones.shared.zones(picked: picked ?? self.picked)
        return Int(model.filter(
            query: query, ticked: ticked ?? self.ticked, withRecent: false,
            focus: focus ?? self.focus, positions: positions ?? self.positions,
            tags: tags ?? self.tags, zones: zones
        ).count)
    }

    var body: some View {
        let model = self.model
        let zoneCount = Zones.shared.zones(picked: picked).count
        let listing = model.filter(
            query: query, ticked: ticked, withRecent: withRecent,
            focus: focus, positions: positions, tags: tags, zones: Zones.shared.zones(picked: picked)
        )
        VStack(spacing: 0) {
            ListFilters(query: $query, placeholder: "exercises.search", label: "exercises.searchLabel", countLabel: listing.countLabel) {
                FilterChipButton(
                    icon: "dumbbell", count: ticked?.count ?? 0, name: Text("exercises.equipment"),
                    tickedName: Text("exercises.equipmentTicked \(ticked?.count ?? 0)")
                ) { sheet = .equipment }
                FilterChipButton(
                    icon: "scope", count: focus.count, name: Text("exercises.focus"),
                    tickedName: Text("exercises.focusTicked \(focus.count)")
                ) { sheet = .focus }
                FilterChipButton(
                    icon: "figure.mind.and.body", count: positions.count, name: Text("exercises.position"),
                    tickedName: Text("exercises.positionTicked \(positions.count)")
                ) { sheet = .position }
                FilterChipButton(
                    icon: "figure.arms.open", count: zoneCount, name: Text("exercises.zones"),
                    tickedName: Text("exercises.zonesTicked \(zoneCount)")
                ) { sheet = .zones }
                FilterChipButton(
                    icon: "tag", count: tags.count, name: Text("exercises.tags"),
                    tickedName: Text("exercises.tagsTicked \(tags.count)")
                ) { sheet = .tags }
            } action: {
                if ticked != nil || !focus.isEmpty || !positions.isEmpty || !tags.isEmpty || !picked.isEmpty {
                    Button("exercises.clearFilters") { clearFilters() }
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
                            clearFilters()
                        }
                    }
                }
                .padding(.horizontal, 20)
                .padding(.bottom, 20)
            }
        }
        .background(PoulpyColor.background)
        .sheet(item: $sheet) { which in
            switch which {
            case .equipment:
                FilterOptionSheet(
                    title: "exercises.equipment", hint: "exercises.equipmentHint",
                    rows: model.equipmentOptions.map { FilterOptionRow(value: $0.equipment, label: $0.label, countLabel: $0.countLabel) },
                    ticked: ticked ?? [], fitting: { count(ticked: $0) }
                ) { value in
                    tick(value.isEmpty ? nil : value)
                    sheet = nil
                }
            case .focus:
                FilterOptionSheet(
                    title: "exercises.focus", hint: "exercises.focusHint",
                    rows: model.focusOptions.map { FilterOptionRow(value: $0.value, label: $0.label, countLabel: $0.countLabel) },
                    ticked: focus, fitting: { count(focus: $0) }
                ) { value in
                    focus = value
                    sheet = nil
                }
            case .position:
                FilterOptionSheet(
                    title: "exercises.position", hint: "exercises.positionHint",
                    rows: model.positionOptions.map { FilterOptionRow(value: $0.value, label: $0.label, countLabel: $0.countLabel) },
                    ticked: positions, fitting: { count(positions: $0) }
                ) { value in
                    positions = value
                    sheet = nil
                }
            case .tags:
                FilterOptionSheet(
                    title: "exercises.tags", hint: "exercises.tagsHint",
                    rows: model.tagOptions.map { FilterOptionRow(value: $0.value, label: $0.label, countLabel: $0.countLabel) },
                    ticked: tags, fitting: { count(tags: $0) }
                ) { value in
                    tags = value
                    sheet = nil
                }
            case .zones:
                ZonesSheet(figure: figure, picked: picked, fitting: { count(picked: $0) }) { value in
                    picked = value
                    sheet = nil
                }
            }
        }
    }
}

/// The icon-only filter chip; filled, with how many are ticked, while its filter is on.
private struct FilterChipButton: View {
    let icon: String
    let count: Int
    let name: Text
    let tickedName: Text
    let action: () -> Void

    var body: some View {
        let on = count > 0
        Button(action: action) {
            HStack(spacing: 4) {
                Image(systemName: icon)
                    .font(.system(size: 15, weight: .semibold))
                if on {
                    Text(verbatim: "\(count)")
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
        .accessibilityLabel(on ? tickedName : name)
        .accessibilityAddTraits(on ? [.isSelected] : [])
    }
}

/// One line of an option sheet: what is ticked, its name, and how many exercises it lists.
private struct FilterOptionRow<Value: Hashable> {
    let value: Value
    let label: String
    let countLabel: String
}

/// Tick what you want: the list then only offers exercises that fit. "Show N exercises"
/// counts live and applies; "Clear" removes the filter.
private struct FilterOptionSheet<Value: Hashable>: View {
    let title: LocalizedStringKey
    let hint: LocalizedStringKey
    let rows: [FilterOptionRow<Value>]
    let fitting: (Set<Value>) -> Int
    let apply: (Set<Value>) -> Void
    @State private var pending: Set<Value>

    init(title: LocalizedStringKey, hint: LocalizedStringKey, rows: [FilterOptionRow<Value>], ticked: Set<Value>, fitting: @escaping (Set<Value>) -> Int, apply: @escaping (Set<Value>) -> Void) {
        self.title = title
        self.hint = hint
        self.rows = rows
        self.fitting = fitting
        self.apply = apply
        _pending = State(initialValue: ticked)
    }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 12) {
                SheetHeading(title: title, hint: hint)
                VStack(spacing: 0) {
                    ForEach(rows, id: \.label) { option in
                        let on = pending.contains(option.value)
                        Button {
                            if on { pending.remove(option.value) } else { pending.insert(option.value) }
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
                FilterSheetActions(count: fitting(pending), clear: { apply([]) }, show: { apply(pending) })
            }
            .padding(.horizontal, 20)
            .padding(.top, 24)
            .padding(.bottom, 28)
        }
        .background(PoulpyColor.surface)
        .presentationDetents([.large])
    }
}

/// Zones: tap the parts of the body to work on the front and back figures; left and right count together.
private struct ZonesSheet: View {
    let figure: BodyFigure
    let fitting: (Set<String>) -> Int
    let apply: (Set<String>) -> Void
    @State private var pending: Set<String>
    @State private var side: Shared.BodyView = .front

    init(figure: BodyFigure, picked: Set<String>, fitting: @escaping (Set<String>) -> Int, apply: @escaping (Set<String>) -> Void) {
        self.figure = figure
        self.fitting = fitting
        self.apply = apply
        _pending = State(initialValue: picked)
    }

    var body: some View {
        let summary: String = Zones.shared.summary(picked: pending, languageCode: Language.code)
        ScrollView {
            VStack(alignment: .leading, spacing: 12) {
                SheetHeading(title: "exercises.zones", hint: "exercises.zonesHint")
                Picker("exercises.zones", selection: $side) {
                    Text("profile.figureFront").tag(Shared.BodyView.front)
                    Text("profile.figureBack").tag(Shared.BodyView.back)
                }
                .pickerStyle(.segmented)
                BodyMapFigure(figure: figure, view: side, selected: pending, width: 200) { key in
                    pending = Zones.shared.toggle(picked: pending, key: key)
                }
                .frame(maxWidth: .infinity)
                if summary.isEmpty {
                    Text("exercises.zonesNone").font(.poulpy(.body).weight(.heavy)).foregroundStyle(PoulpyColor.onPrimaryContainer)
                } else {
                    Text(summary).font(.poulpy(.body).weight(.heavy)).foregroundStyle(PoulpyColor.onPrimaryContainer)
                }
                FilterSheetActions(count: fitting(pending), clear: { apply([]) }, show: { apply(pending) })
            }
            .padding(.horizontal, 20)
            .padding(.top, 24)
            .padding(.bottom, 28)
        }
        .background(PoulpyColor.surface)
        .presentationDetents([.large])
    }
}

private struct SheetHeading: View {
    let title: LocalizedStringKey
    let hint: LocalizedStringKey

    var body: some View {
        VStack(alignment: .leading, spacing: 2) {
            Text(title).font(PoulpyFont.headline).accessibilityAddTraits(.isHeader)
            Text(hint).font(.poulpy(.subheadline)).foregroundStyle(PoulpyColor.onSurfaceVariant)
        }
    }
}

private struct FilterSheetActions: View {
    let count: Int
    let clear: () -> Void
    let show: () -> Void

    var body: some View {
        HStack(spacing: 8) {
            Button(action: clear) {
                Text("exercises.filterClear")
                    .font(.poulpy(.callout).weight(.heavy))
                    .frame(maxWidth: .infinity)
                    .frame(height: 56)
                    .overlay(Capsule().stroke(PoulpyColor.outline, lineWidth: 2))
            }
            Button(action: show) {
                Text("exercises.filterShow \(count)")
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

/// A picker row: the card opens the exercise's details, and the round plus inside it adds the exercise.
struct PickerRowView: View {
    let row: ExerciseRow
    let onOpen: () -> Void
    let onAdd: () -> Void

    var body: some View {
        HStack(spacing: 0) {
            Button(action: onOpen) {
                HStack(spacing: 12) {
                    PatternBadge(pattern: row.pattern, size: 44)
                        .accessibilityHidden(true)
                    VStack(alignment: .leading, spacing: 1) {
                        Text(row.name).font(.poulpy(.headline).weight(.heavy)).foregroundStyle(PoulpyColor.onSurface)
                        Text(row.details).font(.poulpy(.subheadline)).foregroundStyle(PoulpyColor.onSurfaceVariant)
                    }
                    .multilineTextAlignment(.leading)
                    Spacer(minLength: 0)
                }
                .padding(EdgeInsets(top: 10, leading: 12, bottom: 10, trailing: 0))
                .frame(minHeight: 64)
                .contentShape(Rectangle())
            }
            .buttonStyle(.plain)
            .accessibilityHint(String(localized: "exercise.details \(row.name)"))
            // A 56 pt touch target around the 36 pt plus.
            Button(action: onAdd) {
                Image(systemName: "plus")
                    .font(.system(size: 16, weight: .bold))
                    .foregroundStyle(PoulpyColor.onPrimary)
                    .frame(width: 36, height: 36)
                    .background(PoulpyColor.primary, in: Circle())
                    .frame(width: 56, height: 56)
                    .contentShape(Rectangle())
            }
            .buttonStyle(.plain)
            .accessibilityLabel(String(localized: "exercise.addNamed \(row.name)"))
        }
        .background(PoulpyColor.surface, in: RoundedRectangle(cornerRadius: 16))
        .overlay(RoundedRectangle(cornerRadius: 16).stroke(PoulpyColor.outline))
    }
}

/// Poulpy's card when nothing matches: a search shows what was typed.
private struct NoExercises: View {
    let query: String
    let clear: () -> Void

    var body: some View {
        PoulpyCard(
            title: query.isEmpty ? Text("exercises.noFit") : Text("exercises.nothingFor \(query)"),
            message: Text(LocalizedStringKey(query.isEmpty ? "exercises.noFitHint" : "exercises.nothingHint")),
            button: Text("exercises.clearAll"),
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
                            Text("exercise.variationPosition \(detail.variationPosition)")
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
                    .accessibilityLabel(Text("exercise.variationsA11y \(detail.variationPosition)"))
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
                    Text("exercise.variationsHint").font(.poulpy(.subheadline)).foregroundStyle(PoulpyColor.onSurfaceVariant)
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
                        Text("exercise.variationCurrent")
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
