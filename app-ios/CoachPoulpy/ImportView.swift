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

/// A workout file opened from another app: what is in it with Add to my library and Cancel, or why it can't be
/// added, crimson with an icon. `onAdded` gets the new workout's id; `onClose` leaves with nothing saved.
struct ImportView: View {
    private let model: WorkoutImport
    let onAdded: (String) -> Void
    let onClose: () -> Void

    init(graph: AppGraph, fileText: String, onAdded: @escaping (String) -> Void, onClose: @escaping () -> Void) {
        model = graph.importWorkout(fileText: fileText, languageCode: Language.code)
        self.onAdded = onAdded
        self.onClose = onClose
    }

    var body: some View {
        let state = model.state
        VStack(alignment: .leading, spacing: 0) {
            HStack {
                Button(action: onClose) {
                    Image(systemName: "xmark").font(.system(size: 18, weight: .semibold))
                }
                .accessibilityLabel(Text("action.close"))
                Spacer()
            }
            .padding(.horizontal, 20)
            .padding(.vertical, 12)
            ScrollView {
                VStack(alignment: .leading, spacing: 16) {
                    if state.ready {
                        VStack(alignment: .leading, spacing: 4) {
                            Text("import.someoneSent")
                                .font(.poulpy(.subheadline).weight(.heavy))
                                .foregroundStyle(PoulpyColor.primary)
                            Text("import.title").font(PoulpyFont.title)
                        }
                        VStack(alignment: .leading, spacing: 8) {
                            Text(state.name).font(.poulpy(.title3).weight(.heavy))
                            HStack(spacing: 8) {
                                Text(state.formatLabel)
                                    .font(.poulpy(.subheadline).weight(.heavy))
                                    .foregroundStyle(PoulpyColor.onPrimaryContainer)
                                    .padding(EdgeInsets(top: 4, leading: 10, bottom: 4, trailing: 10))
                                    .background(PoulpyColor.primaryContainer, in: Capsule())
                                Text("\(state.partsLabel) · \(state.stepsLabel)").font(.poulpy(.callout))
                            }
                        }
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .padding(16)
                        .background(PoulpyColor.surfaceVariant, in: RoundedRectangle(cornerRadius: 16))
                        Text("import.note").font(.poulpy(.subheadline)).foregroundStyle(PoulpyColor.onSurfaceVariant)
                    } else {
                        Text("import.errorTitle").font(PoulpyFont.title)
                        // Crimson with an icon, never ember.
                        HStack(alignment: .top, spacing: 12) {
                            Image(systemName: "exclamationmark.circle").font(.system(size: 22, weight: .semibold))
                            Text(state.errorMessage).font(.poulpy(.callout).weight(.bold))
                        }
                        .foregroundStyle(PoulpyColor.error)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .padding(16)
                        .background(PoulpyColor.surfaceVariant, in: RoundedRectangle(cornerRadius: 16))
                        Text("import.nothingAdded").font(.poulpy(.subheadline)).foregroundStyle(PoulpyColor.onSurfaceVariant)
                    }
                }
                .padding(.horizontal, 20)
            }
        }
        .background(PoulpyColor.background)
        .safeAreaInset(edge: .bottom) {
            VStack(spacing: 8) {
                if state.ready {
                    Button {
                        if let id = model.add() { onAdded(id) }
                    } label: {
                        Text("import.add")
                            .font(.poulpy(.body).weight(.heavy))
                            .foregroundStyle(PoulpyColor.onPrimary)
                            .frame(maxWidth: .infinity, minHeight: 56)
                            .background(PoulpyColor.primary, in: Capsule())
                    }
                    Button(action: onClose) {
                        Text("action.cancel").font(.poulpy(.body).weight(.heavy)).frame(maxWidth: .infinity, minHeight: 44)
                    }
                } else {
                    Button(action: onClose) {
                        Text("action.close")
                            .font(.poulpy(.body).weight(.heavy))
                            .frame(maxWidth: .infinity, minHeight: 48)
                            .overlay(Capsule().stroke(PoulpyColor.outline, lineWidth: 2))
                    }
                    .buttonStyle(.plain)
                }
            }
            .padding(.horizontal, 20)
            .padding(.vertical, 12)
            .background(PoulpyColor.surface)
        }
    }
}

/// A workout just added from a file, opened over the tabs.
private struct AddedWorkout: Identifiable {
    let id: String
}

/// Presents the preview of a workout file, then the page of the workout it added.
struct ImportPresenter: ViewModifier {
    let graph: AppGraph
    @Binding var incoming: IncomingWorkoutFile?
    @State private var added: AddedWorkout?

    func body(content: Content) -> some View {
        content
            .fullScreenCover(item: $incoming) { file in
                ImportView(graph: graph, fileText: file.text, onAdded: { id in
                    incoming = nil
                    added = AddedWorkout(id: id)
                }, onClose: { incoming = nil })
            }
            .fullScreenCover(item: $added) { workout in
                NavigationStack {
                    WorkoutPageView(graph: graph, workoutId: workout.id)
                        .toolbar {
                            ToolbarItem(placement: .navigationBarLeading) {
                                Button { added = nil } label: { Image(systemName: "xmark") }
                                    .accessibilityLabel(Text("action.close"))
                            }
                        }
                }
            }
    }
}
