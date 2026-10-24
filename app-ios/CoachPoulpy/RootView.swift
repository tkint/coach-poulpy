import SwiftUI
import Shared

struct RootView: View {
    let graph: AppGraph

    var body: some View {
        TabView {
            TodayView(graph: graph)
                .tabItem { Label("tab.today", systemImage: "sun.max") }
            ExercisesView(graph: graph)
                .tabItem { Label("tab.exercises", systemImage: "list.bullet") }
            TreadmillView(graph: graph)
                .tabItem { Label("tab.treadmill", systemImage: "figure.run") }
        }
        // Text without its own font (list rows, buttons, forms) is set in Nunito.
        .font(.poulpy(.body))
    }
}
