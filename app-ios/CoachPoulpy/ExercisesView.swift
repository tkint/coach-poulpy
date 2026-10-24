import SwiftUI
import Shared

struct ExercisesView: View {
    let graph: AppGraph

    private var rows: [ExerciseRow] {
        let language = Locale.current.language.languageCode?.identifier ?? "en"
        return graph.exerciseList(languageCode: language).rows
    }

    var body: some View {
        NavigationStack {
            List(rows, id: \.slug) { row in
                VStack(alignment: .leading, spacing: 2) {
                    Text(row.name)
                    Text(row.details).font(.footnote).foregroundStyle(.secondary)
                }
            }
            .navigationTitle("tab.exercises")
        }
    }
}
