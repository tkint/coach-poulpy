import SwiftUI
import Shared

/// What to open over the Today tab.
private enum Cover: Identifiable {
    case builder(workoutId: String?)
    case player(workoutId: String?)

    var id: String {
        switch self {
        case .builder(let id): return "builder:\(id ?? "")"
        case .player(let id): return "player:\(id ?? "")"
        }
    }
}

struct TodayView: View {
    let graph: AppGraph
    @StateObject private var today: Observed<TodayModel, TodayState>
    @State private var cover: Cover?
    @State private var deleting: WorkoutRow?
    @State private var unfinished: String?

    init(graph: AppGraph) {
        self.graph = graph
        // Built once: StateObject evaluates this only for the first init of the view.
        _today = StateObject(wrappedValue: {
            let model = graph.today(languageCode: Language.code)
            return Observed(model, current: model.current, watch: model.watch, close: { $0.close() })
        }())
    }

    var body: some View {
        NavigationStack {
            List {
                if let name = unfinished {
                    Section {
                        VStack(alignment: .leading, spacing: 8) {
                            Text("today.resume \(name)").font(.headline)
                            Text("today.resume.body").font(.subheadline).foregroundStyle(.secondary)
                            Button("player.resume") { cover = .player(workoutId: nil) }
                                .buttonStyle(.borderedProminent)
                        }
                        .padding(.vertical, 4)
                    }
                }
                if today.value.workouts.isEmpty {
                    VStack(spacing: 8) {
                        Text("today.empty.title").font(.title2)
                        Text("today.empty.body")
                            .foregroundStyle(.secondary)
                            .multilineTextAlignment(.center)
                    }
                    .frame(maxWidth: .infinity)
                    .padding(32)
                    .listRowBackground(Color.clear)
                }
                ForEach(today.value.workouts, id: \.id) { workout in
                    HStack {
                        VStack(alignment: .leading, spacing: 2) {
                            Text(workout.name).font(.headline)
                            Text(workout.details).font(.footnote).foregroundStyle(.secondary)
                        }
                        Spacer()
                        Button {
                            cover = .player(workoutId: workout.id)
                        } label: {
                            Label("action.start", systemImage: "play.fill")
                        }
                        .buttonStyle(.borderedProminent)
                    }
                    .contextMenu {
                        Button("action.edit") { cover = .builder(workoutId: workout.id) }
                        Button("action.delete", role: .destructive) { deleting = workout }
                    }
                    .swipeActions {
                        Button("action.delete", role: .destructive) { deleting = workout }
                        Button("action.edit") { cover = .builder(workoutId: workout.id) }
                    }
                }
            }
            .navigationTitle("tab.today")
            .toolbar {
                Button {
                    cover = .builder(workoutId: nil)
                } label: {
                    Label("today.new.workout", systemImage: "plus")
                }
            }
            .onAppear { unfinished = graph.unfinishedSessionName() }
            .fullScreenCover(item: $cover, onDismiss: { unfinished = graph.unfinishedSessionName() }) { cover in
                switch cover {
                case .builder(let id): BuilderView(graph: graph, workoutId: id)
                case .player(let id): PlayerView(graph: graph, workoutId: id)
                }
            }
            .confirmationDialog("delete.title", isPresented: Binding(get: { deleting != nil }, set: { if !$0 { deleting = nil } }), titleVisibility: .visible) {
                Button("action.delete", role: .destructive) {
                    if let workout = deleting { graph.deleteWorkout(workoutId: workout.id) }
                    deleting = nil
                }
            } message: {
                Text("delete.body")
            }
        }
    }
}
