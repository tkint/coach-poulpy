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
    @Environment(\.unitSystem) private var units
    @Environment(\.colorScheme) private var colorScheme

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
        .background(PoulpyColor.background.ignoresSafeArea())
        .onAppear { UIApplication.shared.isIdleTimerDisabled = true }
        .onDisappear { UIApplication.shared.isIdleTimerDisabled = false }
    }

    @ViewBuilder
    private func content(_ model: PlayerModel, _ state: PlayerState) -> some View {
        VStack(spacing: 12) {
            if state.status == .ready {
                ready(model, state)
            } else if state.status == .finished {
                finished(model, state)
            } else {
                running(model, state)
            }
        }
        .padding(24)
    }

    @ViewBuilder
    private func ready(_ model: PlayerModel, _ state: PlayerState) -> some View {
        HStack {
            PlayerSteps.iconButton("xmark", "action.back") { dismiss() }
            Spacer()
        }
        Spacer()
        PlayerSteps.capsLabel(Text("\(state.workoutName) · \(state.exerciseCountLabel)"))
        Text("player.ready").font(PoulpyFont.display)
        Image(PoulpyDrawing.standingTall.imageName)
            .resizable().scaledToFit().frame(width: 200, height: 180).padding(.vertical, PoulpySpace.lg).accessibilityHidden(true)
        PlayerSteps.capsLabel(Text("player.firstUp"), color: PoulpyColor.primary)
        Text(state.title).font(PoulpyFont.title).multilineTextAlignment(.center)
        if let detail = state.targetDetail {
            Text(detail).font(.custom(PoulpyFontFace.nunitoBold, size: 17, relativeTo: .body)).foregroundStyle(PoulpyColor.onSurfaceVariant)
        }
        Spacer()
        Button {
            model.start()
        } label: {
            Label("player.start", systemImage: "play.fill")
                .font(.custom(PoulpyFontFace.nunitoExtraBold, size: 19, relativeTo: .body))
                .foregroundStyle(PoulpyColor.onPrimary)
                .frame(maxWidth: .infinity, minHeight: 64)
                .background(PoulpyColor.primary, in: Capsule())
        }
        .buttonStyle(.plain)
    }

    @ViewBuilder
    private func running(_ model: PlayerModel, _ state: PlayerState) -> some View {
        let paused = state.status == .paused

        PlayerSteps.bar(state, paused: paused, canToggle: true, onToggle: { paused ? model.resume() : model.pause() }, onEnd: { confirmStop = true })

        if state.isRest {
            PlayerSteps.restStep(state, paused: paused, onSkip: { model.skip() }, onAdjust: { model.adjustRest(seconds: Int32($0)) })
        } else {
            PlayerSteps.workStep(state, paused: paused, onSkip: { model.skip() }, onDone: { model.done() })
        }

        Color.clear.frame(height: 0)
            .confirmationDialog("player.stopTitle", isPresented: $confirmStop, titleVisibility: .visible) {
                Button("player.stop", role: .destructive) { model.stop() }
            } message: {
                Text("player.stopBody")
            }
    }

    @ViewBuilder
    private func finished(_ model: PlayerModel, _ state: PlayerState) -> some View {
        let screen = model.finished(units: units)
        ScrollView {
            VStack(spacing: 16) {
                Image((screen?.drawing ?? PoulpyDrawing.exhausted).imageName).resizable().scaledToFit().frame(width: 150, height: 150).accessibilityHidden(true)
                Text(screen?.heading ?? "").font(.poulpy(.largeTitle))
                if let quote = screen?.quote {
                    Text(quote).font(.poulpy(.headline).weight(.semibold)).foregroundStyle(PoulpyColor.primary).multilineTextAlignment(.center)
                }
                VStack(spacing: 2) {
                    Text(state.workoutName).font(.poulpy(.title2).weight(.heavy)).multilineTextAlignment(.center)
                    if let screen {
                        let date = Date(timeIntervalSince1970: Double(screen.startedAtMillis) / 1000)
                        Text("\(date.formatted(.dateTime.weekday(.abbreviated).day().month(.abbreviated))) · \(date.formatted(date: .omitted, time: .shortened))")
                            .foregroundStyle(PoulpyColor.onSurfaceVariant)
                    }
                    if let summary = screen?.summary {
                        Text(summary).multilineTextAlignment(.center).foregroundStyle(PoulpyColor.onSurfaceVariant)
                    }
                }
                if let screen {
                    HStack(spacing: 8) {
                        ForEach(Array(screen.stats.enumerated()), id: \.offset) { _, stat in
                            VStack(alignment: .leading, spacing: 2) {
                                Text(stat.label).font(PoulpyFont.label).fontWeight(.heavy).foregroundStyle(PoulpyColor.onSurfaceVariant).lineLimit(1)
                                Text(stat.value).font(PoulpyFont.headline).lineLimit(1).minimumScaleFactor(0.7)
                            }
                            .padding(EdgeInsets(top: 12, leading: 10, bottom: 12, trailing: 10))
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .background(PoulpyColor.surface, in: RoundedRectangle(cornerRadius: PoulpyRadius.md))
                            .overlay(RoundedRectangle(cornerRadius: PoulpyRadius.md).stroke(PoulpyColor.outline, lineWidth: 1))
                        }
                    }
                    HStack(spacing: 14) {
                        TentacleRing(lit: Int(screen.poulpy.tentaclesLit), on: PoulpyColor.primary, off: PoulpyColor.outline)
                            .frame(width: 64, height: 64)
                            .accessibilityElement()
                            .accessibilityLabel(screen.poulpy.tentaclesCaption)
                        VStack(alignment: .leading, spacing: 2) {
                            Text(screen.poulpy.title).font(PoulpyFont.headline)
                            Text(screen.poulpy.line).font(.poulpy(.subheadline).weight(.semibold))
                        }
                        .foregroundStyle(PoulpyColor.onPrimaryContainer)
                        Spacer(minLength: 0)
                    }
                    .padding(PoulpySpace.md)
                    .background(PoulpyColor.primaryContainer, in: RoundedRectangle(cornerRadius: PoulpyRadius.md))
                }
            }
            .frame(maxWidth: .infinity)
        }
        if let card = model.shareCard(units: units) {
            ShareLink(item: ShareCardItem(card: card, scheme: colorScheme), preview: SharePreview(card.title)) {
                Label("shareImage.button", systemImage: "square.and.arrow.up").frame(maxWidth: .infinity, minHeight: 44)
            }
            .buttonStyle(.bordered)
        }
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
}
