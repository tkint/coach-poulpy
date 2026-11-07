import SwiftUI
import Shared

@main
struct CoachPoulpyApp: App {
    // Built once at launch. Guest mode: everything stays on the phone.
    private let graph: AppGraph
    /// The account, made at launch so that a signed-in phone syncs as soon as the app opens.
    private let account: AccountModel

    init() {
        graph = IosAppGraphFactory().create()
        account = graph.makeAccount()
        PoulpyAppearance.apply()
    }

    var body: some Scene {
        WindowGroup {
            RootView(graph: graph, account: account)
        }
    }
}
