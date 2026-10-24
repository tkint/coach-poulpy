import SwiftUI
import Shared

@main
struct CoachPoulpyApp: App {
    // Built once at launch. Guest mode: everything stays on the phone.
    private let graph = IosAppGraphFactory().create()

    var body: some Scene {
        WindowGroup {
            RootView(graph: graph)
        }
    }
}
