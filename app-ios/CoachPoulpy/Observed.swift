import Foundation
import Shared

/// Keeps a Kotlin model alive for a screen and republishes the state it reports through `watch`.
final class Observed<Model: AnyObject, State>: ObservableObject {
    let model: Model
    @Published private(set) var value: State
    private var watch: Cancellable?
    private let onClose: (Model) -> Void

    init(_ model: Model, current: State, watch: (@escaping (State) -> Void) -> Cancellable, close: @escaping (Model) -> Void) {
        self.model = model
        value = current
        onClose = close
        self.watch = watch { [weak self] state in
            DispatchQueue.main.async { self?.value = state }
        }
    }

    deinit {
        watch?.cancel()
        onClose(model)
    }
}

enum Language {
    static var code: String { Locale.current.language.languageCode?.identifier ?? "en" }
}
