import AudioToolbox
import SwiftUI
import UIKit
import Shared

/// Owns the Kotlin session for the screen; `model` is nil when there was nothing to open.
final class PlayerHolder: ObservableObject {
    let model: PlayerModel?
    @Published private(set) var state: PlayerState?
    private var watch: Cancellable?

    init(model: PlayerModel?) {
        self.model = model
        state = model?.current
        watch = model?.watch { [weak self] state in
            DispatchQueue.main.async { self?.state = state }
        }
    }

    deinit {
        watch?.cancel()
        model?.close()
    }
}

/// The guided session. A nil workout id reopens the session left open.
struct PlayerView: View {
    @StateObject private var holder: PlayerHolder
    @State private var confirmStop = false
    @Environment(\.dismiss) private var dismiss

    init(graph: AppGraph, workoutId: String?) {
        // Inside the autoclosure, so the session is opened once, not on every redraw.
        _holder = StateObject(wrappedValue: PlayerHolder(model: {
            let language = Language.code
            if let workoutId { return graph.player(workoutId: workoutId, languageCode: language) }
            return graph.resumeSession(languageCode: language)
        }()))
    }

    var body: some View {
        Group {
            if let model = holder.model, let state = holder.state {
                content(model, state)
                    .onChange(of: state.phaseNumber) { _ in cue(long: true) }
                    .onChange(of: state.remainingSeconds?.intValue) { left in
                        if let left, (1...3).contains(left), state.status == .running { cue(long: false) }
                    }
            } else {
                Color.clear.onAppear { dismiss() }
            }
        }
        // The phone sits on the floor during a session: keep the screen on.
        .onAppear { UIApplication.shared.isIdleTimerDisabled = true }
        .onDisappear { UIApplication.shared.isIdleTimerDisabled = false }
    }

    @ViewBuilder
    private func content(_ model: PlayerModel, _ state: PlayerState) -> some View {
        VStack(spacing: 12) {
            if state.status == .ready {
                ready(model, state)
            } else if state.status == .finished {
                finished(state)
            } else {
                running(model, state)
            }
        }
        .padding(24)
    }

    @ViewBuilder
    private func ready(_ model: PlayerModel, _ state: PlayerState) -> some View {
        HStack {
            Button { dismiss() } label: { Image(systemName: "xmark").font(.poulpy(.title2)) }
            Spacer()
        }
        Spacer()
        Text(state.workoutName).font(.poulpy(.largeTitle)).multilineTextAlignment(.center)
        Text("player.ready").font(.poulpy(.title3)).foregroundStyle(.secondary)
        PatternBadge(pattern: state.pattern, size: 120).padding(32)
        Text(state.title).font(.poulpy(.title2))
        if let target = state.targetLabel { Text(target) }
        Spacer()
        Button {
            model.start()
        } label: {
            Label("player.start", systemImage: "play.fill").frame(maxWidth: .infinity, minHeight: 44)
        }
        .buttonStyle(.borderedProminent)
        .controlSize(.large)
    }

    @ViewBuilder
    private func running(_ model: PlayerModel, _ state: PlayerState) -> some View {
        let paused = state.status == .paused
        let remaining = state.remainingSeconds?.intValue

        HStack {
            Button { confirmStop = true } label: { Image(systemName: "xmark").font(.poulpy(.title2)) }
            Spacer()
            Text("player.step \(Int(state.phaseNumber)) \(Int(state.phaseCount))").font(.poulpy(.subheadline))
        }
        ProgressView(value: Double(state.phaseNumber - 1), total: Double(max(state.phaseCount, 1)))

        Spacer()
        if !state.isRest { PatternBadge(pattern: state.pattern, size: 96) }
        // "WARM-UP", "ROUND 2 OF 4": where this step sits in the workout.
        let tags = [state.roleLabel, state.roundLabel].compactMap { $0 }
        if !tags.isEmpty {
            Text(tags.joined(separator: " · ")).textCase(.uppercase).font(PoulpyFont.label).kerning(1).foregroundStyle(Color.accentColor)
        }
        Text(state.title)
            .font(.poulpy(.largeTitle))
            .multilineTextAlignment(.center)
            .foregroundStyle(state.isRest ? Color.accentColor : Color.primary)
        if !state.isRest {
            if state.setCount > 1 { Text("player.set \(Int(state.setNumber)) \(Int(state.setCount))").font(.poulpy(.title3)) }
            if let side = state.sideLabel { Text(side).font(.poulpy(.title3)).foregroundStyle(Color.accentColor) }
        }
        if let remaining {
            Text(clock(remaining)).font(.custom(PoulpyFontFace.nunitoExtraBold, fixedSize: 88).monospacedDigit())
            if let total = state.totalSeconds?.intValue, total > 0 {
                ProgressView(value: Double(total - remaining), total: Double(total)).frame(maxWidth: 220)
            }
        } else if let target = state.targetLabel {
            Text(target).font(.custom(PoulpyFontFace.nunitoExtraBold, fixedSize: 48))
        }
        if !state.isRest, let load = state.loadLabel { Text(load).font(.poulpy(.title2)) }
        if paused { Text("player.paused").font(.poulpy(.headline)).foregroundStyle(.red) }

        Spacer()
        if state.isRest || remaining != nil, let next = state.nextTitle {
            Text("player.next \(next) \(state.nextDetail ?? "")").multilineTextAlignment(.center)
        }
        HStack(spacing: 12) {
            Button {
                paused ? model.resume() : model.pause()
            } label: {
                Label(LocalizedStringKey(paused ? "player.resume" : "player.pause"), systemImage: paused ? "play.fill" : "pause.fill")
                    .frame(maxWidth: .infinity, minHeight: 36)
            }
            .buttonStyle(.bordered)
            Button {
                model.skip()
            } label: {
                Label(LocalizedStringKey(state.isRest ? "player.skip.rest" : "player.skip"), systemImage: "forward.fill")
                    .frame(maxWidth: .infinity, minHeight: 36)
            }
            .buttonStyle(.bordered)
            .disabled(paused)
        }
        if !state.isTimed {
            Button {
                model.done()
            } label: {
                Label("player.done", systemImage: "checkmark").font(.poulpy(.title3)).frame(maxWidth: .infinity, minHeight: 52)
            }
            .buttonStyle(.borderedProminent)
            .disabled(paused)
        }
        Color.clear.frame(height: 0)
            .confirmationDialog("player.stop.title", isPresented: $confirmStop, titleVisibility: .visible) {
                Button("player.stop", role: .destructive) { model.stop() }
            } message: {
                Text("player.stop.body")
            }
    }

    @ViewBuilder
    private func finished(_ state: PlayerState) -> some View {
        Spacer()
        Text("player.finished").font(.poulpy(.largeTitle))
        Text(state.workoutName).font(.poulpy(.title2))
        if let summary = state.summary {
            Text("player.summary \(Int(summary.setsDone)) \(Int(summary.setsPlanned)) \(summary.durationLabel)")
                .multilineTextAlignment(.center)
                .padding(.top)
            if summary.setsSkipped > 0 {
                Text("player.summary.skipped \(Int(summary.setsSkipped))").foregroundStyle(.secondary)
            }
        }
        Spacer()
        Button {
            dismiss()
        } label: {
            Text("player.close").frame(maxWidth: .infinity, minHeight: 44)
        }
        .buttonStyle(.borderedProminent)
    }

    /// A short tick for the last seconds of a timer and a longer sound when a phase starts.
    private func cue(long: Bool) {
        AudioServicesPlaySystemSound(long ? 1005 : 1057)
    }

    private func clock(_ seconds: Int) -> String {
        String(format: "%d:%02d", seconds / 60, seconds % 60)
    }
}
