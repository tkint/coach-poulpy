import SwiftUI
import Shared

/// A workout file another app opened here, as text; empty when it couldn't be read, which the preview refuses.
struct IncomingWorkoutFile: Identifiable {
    let id = UUID()
    let text: String

    /// A workout file is a few kilobytes; anything past this is not one.
    private static let maxBytes = 1_000_000

    init(url: URL) {
        let scoped = url.startAccessingSecurityScopedResource()
        defer { if scoped { url.stopAccessingSecurityScopedResource() } }
        if let data = try? Data(contentsOf: url), data.count <= Self.maxBytes, let text = String(data: data, encoding: .utf8) {
            self.text = text
        } else {
            self.text = ""
        }
    }
}

/// A workout file that can't be opened in the builder: why, crimson with an icon, and Close. A good file never shows
/// here, it opens in the builder.
struct ImportView: View {
    let state: WorkoutImportState
    let onClose: () -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            HStack {
                Button(action: onClose) {
                    LucideIcon(.x, size: 22)
                }
                .accessibilityLabel(Text("action.close"))
                Spacer()
            }
            .padding(.horizontal, PoulpySpace.lg)
            .padding(.vertical, 12)
            ScrollView {
                VStack(alignment: .leading, spacing: 16) {
                    Text("import.errorTitle").font(PoulpyFont.title)
                    // Crimson with an icon, never ember.
                    HStack(alignment: .top, spacing: 12) {
                        LucideIcon(.circleExclamationPoint, size: 26)
                        Text(state.errorMessage).font(.poulpy(.callout).weight(.bold))
                    }
                    .foregroundStyle(PoulpyColor.error)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(16)
                    .background(PoulpyColor.surfaceVariant, in: RoundedRectangle(cornerRadius: PoulpyRadius.lg))
                    Text("import.nothingAdded").font(.poulpy(.subheadline)).foregroundStyle(PoulpyColor.onSurfaceVariant)
                }
                .padding(.horizontal, PoulpySpace.lg)
            }
        }
        .background(PoulpyColor.background)
        .safeAreaInset(edge: .bottom) {
            Button(action: onClose) {
                Text("action.close")
                    .frame(maxWidth: .infinity)
            }
            .buttonStyle(PoulpyButtonStyle(.secondary, size: .lg))
            .padding(.horizontal, PoulpySpace.lg)
            .padding(.vertical, 12)
            .background(PoulpyColor.surface)
        }
    }
}

/// A workout just added from a file the builder can't hold, opened over the tabs.
private struct AddedWorkout: Identifiable {
    let id: String
}

/// Presents a workout file opened from another app: a good one in the builder as an unsaved copy, one the builder
/// can't hold added with its page opened, a refused one with why.
struct ImportPresenter: ViewModifier {
    let graph: AppGraph
    @Binding var incoming: IncomingWorkoutFile?
    @State private var added: AddedWorkout?

    func body(content: Content) -> some View {
        content
            .fullScreenCover(item: $incoming) { file in
                ImportedFileView(graph: graph, fileText: file.text, onAdded: { id in
                    incoming = nil
                    added = AddedWorkout(id: id)
                }, onClose: { incoming = nil })
            }
            .fullScreenCover(item: $added) { workout in
                NavigationStack {
                    WorkoutPageView(graph: graph, workoutId: workout.id)
                        .toolbar {
                            ToolbarItem(placement: .navigationBarLeading) {
                                Button { added = nil } label: { LucideIcon(.x) }
                                    .accessibilityLabel(Text("action.close"))
                            }
                        }
                }
            }
    }
}

private struct ImportedFileView: View {
    let graph: AppGraph
    let onAdded: (String) -> Void
    let onClose: () -> Void
    private let model: WorkoutImport

    init(graph: AppGraph, fileText: String, onAdded: @escaping (String) -> Void, onClose: @escaping () -> Void) {
        self.graph = graph
        self.onAdded = onAdded
        self.onClose = onClose
        model = graph.importWorkout(fileText: fileText, languageCode: Language.code)
    }

    var body: some View {
        if !model.state.ready {
            ImportView(state: model.state, onClose: onClose)
        } else if model.opensInBuilder {
            BuilderView(graph: graph, workoutId: nil, importing: model)
        } else {
            Color.clear.onAppear {
                if let id = model.add() { onAdded(id) } else { onClose() }
            }
        }
    }
}
