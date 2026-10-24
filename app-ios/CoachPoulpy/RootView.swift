import SwiftUI
import Shared

/// The tabs, in order. Each label lives in Localizable.strings under its tab.* key.
private enum AppTab: Hashable { case today, exercises, live, history, settings }

struct RootView: View {
    let graph: AppGraph
    @StateObject private var settings: Observed<SettingsModel, SettingsUiState>
    @State private var tab = AppTab.today
    @State private var settingsSection = SettingsSection.general

    init(graph: AppGraph) {
        self.graph = graph
        _settings = StateObject(wrappedValue: graph.observedSettings())
    }

    var body: some View {
        TabView(selection: $tab) {
            TodayView(graph: graph)
                .tabItem { Label("tab.today", systemImage: "sun.max") }
                .tag(AppTab.today)
            ExercisesView(graph: graph)
                .tabItem { Label("tab.exercises", systemImage: "list.bullet") }
                .tag(AppTab.exercises)
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
    }
}
