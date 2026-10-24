import SwiftUI
import Shared

/// Bridges the Kotlin TodayModel to SwiftUI.
final class TodayViewModel: ObservableObject {
    @Published private(set) var workoutNames: [String] = []
    private let model: TodayModel
    private var watch: Cancellable?

    init(graph: AppGraph) {
        model = graph.today()
        watch = model.watch { [weak self] state in
            let names = state.workoutNames
            DispatchQueue.main.async { self?.workoutNames = names }
        }
    }

    deinit {
        watch?.cancel()
        model.close()
    }
}

struct TodayView: View {
    @StateObject private var viewModel: TodayViewModel

    init(graph: AppGraph) {
        _viewModel = StateObject(wrappedValue: TodayViewModel(graph: graph))
    }

    var body: some View {
        NavigationStack {
            Group {
                if viewModel.workoutNames.isEmpty {
                    VStack(spacing: 8) {
                        Text("today.empty.title").font(.title2)
                        Text("today.empty.body")
                            .font(.body)
                            .foregroundStyle(.secondary)
                            .multilineTextAlignment(.center)
                    }
                    .padding(32)
                } else {
                    List(viewModel.workoutNames, id: \.self) { Text($0) }
                }
            }
            .navigationTitle("tab.today")
        }
    }
}
