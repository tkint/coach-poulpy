import SwiftUI
import Shared

/// The tabs, in order. Each label lives in Localizable.strings under its tab.* key.
private enum AppTab: Hashable { case home, library, live, history, settings }

struct RootView: View {
    let graph: AppGraph
    let account: AccountModel
    @StateObject private var settings: Observed<SettingsModel, SettingsUiState>
    @StateObject private var accountState: Observed<AccountModel, AccountState>
    @StateObject private var confirmedElsewhere: Observed<AccountModel, Bool>
    @Environment(\.scenePhase) private var scenePhase
    /// Listens for the email link opened on another device, while the app is in front and the email waits.
    @State private var watchingConfirmation: Cancellable?
    /// A sign-in, sign-up or email-link screen shown over the tabs.
    @State private var accountFlow: AccountFlowStart?
    @State private var tab = AppTab.home
    @State private var librarySection = LibrarySection.workouts
    @State private var settingsPath = NavigationPath()
    /// The tab that opened Settings › Devices (Live's Manage devices), so back from it returns there instead of the hub.
    @State private var devicesFrom: AppTab?
    @State private var incoming: IncomingWorkoutFile?

    init(graph: AppGraph, account: AccountModel) {
        self.graph = graph
        self.account = account
        _settings = StateObject(wrappedValue: graph.observedSettings())
        _accountState = StateObject(wrappedValue: account.observedState())
        _confirmedElsewhere = StateObject(wrappedValue: account.observedConfirmedElsewhere())
    }

    var body: some View {
        TabView(selection: $tab) {
            HomeView(graph: graph, onOpenLibrary: {
                librarySection = .workouts
                tab = .library
            }, onOpenLive: { tab = .live })
                .tabItem { Label("tab.home", systemImage: "house") }
                .tag(AppTab.home)
            LibraryView(graph: graph, section: $librarySection)
                .tabItem { Label("tab.library", systemImage: "books.vertical") }
                .tag(AppTab.library)
            LiveView(graph: graph, onManageDevices: {
                settingsPath = NavigationPath([SettingsPage.devices])
                devicesFrom = .live
                tab = .settings
            })
                .tabItem { Label("tab.live", systemImage: "waveform.path.ecg") }
                .tag(AppTab.live)
            HistoryView(graph: graph)
                .tabItem { Label("tab.history", systemImage: "clock.arrow.circlepath") }
                .tag(AppTab.history)
            SettingsView(graph: graph, settings: settings, account: accountState, path: $settingsPath) { step in
                accountFlow = AccountFlowStart(step: step)
            }
                .tabItem { Label("tab.settings", systemImage: "gearshape") }
                .tag(AppTab.settings)
        }
        .onChange(of: settingsPath.count) { depth in
            if depth == 0, let from = devicesFrom {
                devicesFrom = nil
                tab = from
            }
        }
        .onChange(of: tab) { selected in
            if selected != .settings { devicesFrom = nil }
        }
        // Text without its own font (list rows, buttons, forms) is set in Nunito.
        .font(.poulpy(.body))
        .environment(\.unitSystem, settings.value.units)
        .preferredColorScheme(settings.value.theme.colorScheme)
        // An account email link opens its screen over Settings; a .poulpyworkout file opened from another app shows a
        // preview before it is added.
        .onOpenURL { url in
            if let step = accountStep(for: url) {
                openFromLink(step)
            } else {
                incoming = IncomingWorkoutFile(url: url)
            }
        }
        .onContinueUserActivity(NSUserActivityTypeBrowsingWeb) { activity in
            if let url = activity.webpageURL, let step = accountStep(for: url) { openFromLink(step) }
        }
        .fullScreenCover(item: $accountFlow) { start in
            AccountFlowView(account: account, start: start.step) { accountFlow = nil }
        }
        .modifier(ImportPresenter(graph: graph, incoming: $incoming))
        // The email confirmed on another device while the app is open: Email confirmed, over Settings.
        .onAppear(perform: updateConfirmationWatch)
        .onChange(of: scenePhase) { _ in updateConfirmationWatch() }
        .onReceive(accountState.$value) { _ in updateConfirmationWatch() }
        .onReceive(confirmedElsewhere.$value) { confirmed in
            guard confirmed else { return }
            account.confirmedShown()
            openFromLink("confirmed")
        }
    }

    private func updateConfirmationWatch() {
        let waiting = accountState.value is AccountStatePending && scenePhase == .active
        if waiting, watchingConfirmation == nil {
            watchingConfirmation = account.watchConfirmation()
        } else if !waiting, let watching = watchingConfirmation {
            watching.cancel()
            watchingConfirmation = nil
        }
    }

    /// Universal Links need the Associated Domains entitlement and app.coach-poulpy.com's apple-app-site-association (#24).
    private func openFromLink(_ step: String) {
        tab = .settings
        settingsPath = NavigationPath()
        devicesFrom = nil
        accountFlow = AccountFlowStart(step: step)
    }
}
