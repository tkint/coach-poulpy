import SwiftUI
import Shared

/// The tabs, in order. Each label lives in Localizable.strings under its tab.* key.
private enum AppTab: Hashable { case home, library, live, history, settings }

struct RootView: View {
    let graph: AppGraph
    @StateObject private var settings: Observed<SettingsModel, SettingsUiState>
    @State private var tab = AppTab.home
    @State private var librarySection = LibrarySection.workouts
    @State private var settingsSection = SettingsSection.general
    /// A session left open when the app was last closed: asked once per launch, before anything else.
    @State private var unfinished: UnfinishedSessionUi?
    @State private var resuming = false
    /// Home reads the session left open when it shows; a discard or a resume builds it again.
    @State private var homeVersion = 0

    init(graph: AppGraph) {
        self.graph = graph
        _settings = StateObject(wrappedValue: graph.observedSettings())
    }

    var body: some View {
        TabView(selection: $tab) {
            HomeView(graph: graph, onOpenLibrary: {
                librarySection = .workouts
                tab = .library
            })
                .id(homeVersion)
                .tabItem { Label("tab.home", systemImage: "house") }
                .tag(AppTab.home)
            LibraryView(graph: graph, section: $librarySection)
                .tabItem { Label("tab.library", systemImage: "books.vertical") }
                .tag(AppTab.library)
            LiveView(graph: graph, onManageDevices: {
                settingsSection = .devices
                tab = .settings
            })
                .tabItem { Label("tab.live", systemImage: "waveform.path.ecg") }
                .tag(AppTab.live)
            HistoryView(graph: graph)
                .tabItem { Label("tab.history", systemImage: "clock.arrow.circlepath") }
                .tag(AppTab.history)
            SettingsView(graph: graph, settings: settings, section: $settingsSection)
                .tabItem { Label("tab.settings", systemImage: "gearshape") }
                .tag(AppTab.settings)
        }
        // Text without its own font (list rows, buttons, forms) is set in Nunito.
        .font(.poulpy(.body))
        .environment(\.unitSystem, settings.value.units)
        .preferredColorScheme(settings.value.theme.colorScheme)
        .onAppear { unfinished = graph.unfinishedSessionAtLaunch() }
        .alert(unfinishedTitle, isPresented: Binding(get: { unfinished != nil }, set: { if !$0 { unfinished = nil } }), presenting: unfinished) { session in
            Button("player.resume") { resume(session) }
            // Destructive: iOS shows it in red, never ember.
            Button("unfinished.discard", role: .destructive) {
                graph.discardUnfinishedSession()
                homeVersion += 1
            }
            Button("unfinished.later", role: .cancel) { graph.dismissUnfinishedSession() }
        } message: { session in
            Text(L("unfinished.body %@", clock(session.stoppedAtSeconds)))
        }
        .fullScreenCover(isPresented: $resuming, onDismiss: { homeVersion += 1 }) {
            PlayerView(graph: graph, workoutId: nil)
        }
    }

    private var unfinishedTitle: String {
        guard let session = unfinished else { return "" }
        return session.live ? L("unfinished.liveTitle") : L("unfinished.title %@", session.workoutName ?? "")
    }

    private func resume(_ session: UnfinishedSessionUi) {
        if session.live {
            graph.dismissUnfinishedSession()
            tab = .live
        } else {
            // The player's resumeSession() closes the question.
            resuming = true
        }
    }
}

private func L(_ key: String, _ args: CVarArg...) -> String {
    String(format: NSLocalizedString(key, comment: ""), locale: Locale.current, arguments: args)
}

private func clock(_ seconds: Int64) -> String {
    let h = seconds / 3600, m = (seconds % 3600) / 60, s = seconds % 60
    return h > 0 ? String(format: "%lld:%02lld:%02lld", h, m, s) : String(format: "%lld:%02lld", m, s)
}
