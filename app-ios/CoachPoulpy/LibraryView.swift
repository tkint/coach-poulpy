import SwiftUI
import Shared

enum LibrarySection: Hashable { case workouts, exercises }

/// What to open over the library.
private enum Cover: Identifiable {
    case builder
    case player(workoutId: String)

    var id: String {
        switch self {
        case .builder: return "builder"
        case .player(let id): return "player:\(id)"
        }
    }
}

/// The saved workouts, with or without a day, and the exercise catalog beside them.
struct LibraryView: View {
    let graph: AppGraph
    @Binding var section: LibrarySection
    @StateObject private var library: Observed<LibraryModel, LibraryState>
    @State private var query = ""
    @State private var kind: WorkoutType?
    @State private var sort = LibrarySort.recent
    @State private var cover: Cover?

    init(graph: AppGraph, section: Binding<LibrarySection>) {
        self.graph = graph
        _section = section
        // Built once: StateObject evaluates this only for the first init of the view.
        _library = StateObject(wrappedValue: {
            let model = graph.library(languageCode: Language.code)
            return Observed(model, current: model.current, watch: model.watch, close: { $0.close() })
        }())
    }

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                sectionSwitch
                    .padding(.horizontal, 20)
                    .padding(.top, PoulpySpace.sm)
                switch section {
                case .workouts: workouts
                case .exercises:
                    ExerciseList(graph: graph, type: nil) { row in
                        NavigationLink {
                            ExerciseDetailView(graph: graph, slug: row.slug)
                        } label: {
                            ExerciseRowView(row: row)
                        }
                        .buttonStyle(.plain)
                    }
                }
            }
            .background(PoulpyColor.background)
            .navigationTitle("tab.library")
            .toolbar {
                if section == .workouts {
                    Button {
                        cover = .builder
                    } label: {
                        Label("library.new.workout", systemImage: "plus")
                    }
                }
            }
            .fullScreenCover(item: $cover) { cover in
                switch cover {
                case .builder: BuilderView(graph: graph, workoutId: nil)
                case .player(let id): PlayerView(graph: graph, workoutId: id)
                }
            }
        }
    }

    // MARK: Workouts | Exercises

    private var sectionSwitch: some View {
        HStack(spacing: 0) {
            switchButton(.workouts, "library.workouts")
            switchButton(.exercises, "library.exercises")
        }
        .padding(4)
        .background(PoulpyColor.surfaceVariant, in: Capsule())
    }

    private func switchButton(_ item: LibrarySection, _ title: LocalizedStringKey) -> some View {
        let on = section == item
        return Button {
            section = item
        } label: {
            Text(title)
                .font(.poulpy(.subheadline).weight(.heavy))
                .frame(maxWidth: .infinity)
                .frame(height: 36)
                .foregroundStyle(on ? PoulpyColor.onPrimaryContainer : PoulpyColor.onSurfaceVariant)
                .background(on ? PoulpyColor.surface : Color.clear, in: Capsule())
                .shadow(color: on ? .black.opacity(0.12) : .clear, radius: 1.5, y: 1)
        }
        .buttonStyle(.plain)
        .accessibilityAddTraits(on ? [.isSelected] : [])
    }

    // MARK: Workouts

    private var workouts: some View {
        let state = library.value
        let rows = library.model.filter(state: state, query: query, type: kind, sort: sort)
        return List {
            if state.kinds.count > 1 {
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 8) {
                        chip(Text("library.all"), selected: kind == nil) { kind = nil }
                        ForEach(state.kinds, id: \.label) { filter in
                            chip(Text(filter.label), selected: kind == filter.type) {
                                kind = kind == filter.type ? nil : filter.type
                            }
                        }
                    }
                }
                .listRowInsets(EdgeInsets(top: 8, leading: 20, bottom: 4, trailing: 20))
                .listRowBackground(Color.clear)
                .listRowSeparator(.hidden)
            }
            if state.loaded && state.rows.isEmpty {
                VStack(alignment: .leading, spacing: 6) {
                    Text("library.empty.title").font(PoulpyFont.headline)
                    Text("library.empty.body").font(.poulpy(.subheadline)).foregroundStyle(PoulpyColor.onSurfaceVariant)
                }
                .padding(.vertical, 16)
                .listRowBackground(Color.clear)
                .listRowSeparator(.hidden)
            } else if state.loaded {
                HStack {
                    Text(L("library.count %lld", Int64(rows.count)))
                        .font(.poulpy(.subheadline).weight(.bold))
                        .foregroundStyle(PoulpyColor.onSurfaceVariant)
                    Spacer()
                    Menu {
                        Picker("library.sort.recent", selection: $sort) {
                            Text("library.sort.recent").tag(LibrarySort.recent)
                            Text("library.sort.name").tag(LibrarySort.alphabetical)
                        }
                    } label: {
                        HStack(spacing: 4) {
                            Text(LocalizedStringKey(sort == .recent ? "library.sort.recent" : "library.sort.name"))
                            Image(systemName: "chevron.down").font(.system(size: 12, weight: .bold))
                        }
                        .font(.poulpy(.subheadline).weight(.heavy))
                    }
                }
                .listRowBackground(Color.clear)
                .listRowSeparator(.hidden)
                if rows.isEmpty {
                    Text("library.none").foregroundStyle(.secondary).listRowBackground(Color.clear)
                }
                ForEach(rows, id: \.id) { row in
                    workoutRow(row)
                        .listRowInsets(EdgeInsets(top: 4, leading: 20, bottom: 4, trailing: 20))
                        .listRowBackground(Color.clear)
                        .listRowSeparator(.hidden)
                }
            }
        }
        .listStyle(.plain)
        .scrollContentBackground(.hidden)
        .searchable(text: $query, prompt: Text("library.search"))
    }

    private func workoutRow(_ row: LibraryRow) -> some View {
        HStack(spacing: 12) {
            NavigationLink {
                WorkoutPageView(graph: graph, workoutId: row.id)
            } label: {
                HStack(spacing: 12) {
                    Image(systemName: icon(row.type))
                        .font(.system(size: 18, weight: .semibold))
                        .foregroundStyle(PoulpyColor.onPrimaryContainer)
                        .frame(width: 44, height: 44)
                        .background(PoulpyColor.primaryContainer, in: Circle())
                        .accessibilityHidden(true)
                    VStack(alignment: .leading, spacing: 1) {
                        Text(row.name).font(.poulpy(.headline)).foregroundStyle(PoulpyColor.onSurface)
                        Text(row.details).font(.poulpy(.subheadline)).foregroundStyle(PoulpyColor.onSurfaceVariant)
                        note(row).padding(.top, 3)
                    }
                    Spacer(minLength: 0)
                }
                .contentShape(Rectangle())
            }
            .buttonStyle(.plain)
            Button {
                cover = .player(workoutId: row.id)
            } label: {
                Image(systemName: "play.fill")
                    .foregroundStyle(PoulpyColor.onPrimaryContainer)
                    .frame(width: 44, height: 44)
                    .background(PoulpyColor.primaryContainer, in: Circle())
            }
            .buttonStyle(.plain)
            .accessibilityLabel(Text("action.start") + Text(" ") + Text(row.name))
        }
        .padding(EdgeInsets(top: 10, leading: 12, bottom: 10, trailing: 8))
        .background(PoulpyColor.surface, in: RoundedRectangle(cornerRadius: 16))
        .overlay(RoundedRectangle(cornerRadius: 16).stroke(PoulpyColor.outline))
    }

    @ViewBuilder
    private func note(_ row: LibraryRow) -> some View {
        let day = row.noteMillis.map { dayLabel($0.int64Value) } ?? ""
        switch row.note {
        case .planned:
            Text(L("library.planned %@", day))
                .font(PoulpyFont.label)
                .foregroundStyle(PoulpyColor.onPrimaryContainer)
                .padding(.horizontal, 8)
                .padding(.vertical, 2)
                .background(PoulpyColor.primaryContainer, in: Capsule())
        case .doneToday:
            Text("library.done.today").font(PoulpyFont.label).foregroundStyle(PoulpyColor.success)
        case .lastDone:
            Text(L("library.last.done %@", day)).font(.poulpy(.footnote).weight(.bold)).foregroundStyle(PoulpyColor.onSurfaceVariant)
        default:
            Text("library.not.done").font(.poulpy(.footnote).weight(.bold)).foregroundStyle(PoulpyColor.onSurfaceVariant)
        }
    }

    private func chip(_ title: Text, selected: Bool, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            title
                .font(.poulpy(.subheadline))
                .padding(.horizontal, 12)
                .padding(.vertical, 6)
                .background(selected ? Color.accentColor : Color.secondary.opacity(0.15), in: Capsule())
                .foregroundStyle(selected ? Color.white : Color.primary)
        }
        .buttonStyle(.plain)
    }

    private func icon(_ type: WorkoutType) -> String {
        switch type {
        case .treadmill: return "figure.run"
        case .mobility, .rehab: return "figure.mind.and.body"
        default: return "dumbbell"
        }
    }
}

/// A localized string with printf-style arguments, as in Localizable.strings.
private func L(_ key: String, _ args: CVarArg...) -> String {
    String(format: NSLocalizedString(key, comment: ""), locale: Locale.current, arguments: args)
}

/// "Sat 24 Oct", in the user's order.
private func dayLabel(_ millis: Int64) -> String {
    Date(timeIntervalSince1970: Double(millis) / 1000).formatted(.dateTime.weekday(.abbreviated).day().month(.abbreviated))
}
