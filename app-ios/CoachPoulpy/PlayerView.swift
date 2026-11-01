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
        // "WORK 1 · ROUND 2 OF 4": the part this step is in, with its icon.
        if let part = state.partLabel {
            HStack(spacing: 6) {
                if let kind = state.partKind { Image(systemName: partIcon(kind)).font(.system(size: 13, weight: .bold)) }
                Text(part).textCase(.uppercase).font(PoulpyFont.label).kerning(1)
            }
            .foregroundStyle(Color.accentColor)
        }
        HStack(spacing: 8) {
            if let role = state.roleLabel { recoverTag(role) }
            Text(state.title)
                .font(.poulpy(.largeTitle))
                .multilineTextAlignment(.center)
                .foregroundStyle(state.isRest ? Color.accentColor : Color.primary)
        }
        if !state.isRest {
            if state.setCount > 1 {
                Text("player.set \(Int(state.setNumber)) \(Int(state.setCount))").font(.poulpy(.title3))
                setDots(number: Int(state.setNumber), count: Int(state.setCount))
            }
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
            if let round = state.nextRound {
                Text("player.nextRound \(round)").textCase(.uppercase).font(PoulpyFont.label).foregroundStyle(.secondary)
            }
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
                Label(LocalizedStringKey(state.isRest ? "player.skipRest" : "player.skip"), systemImage: "forward.fill")
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
                Image(PoulpyDrawing.exhausted.imageName).resizable().scaledToFit().frame(width: 150, height: 150).accessibilityHidden(true)
                Text("player.finished").font(.poulpy(.largeTitle))
                VStack(spacing: 2) {
                    Text(state.workoutName).font(.poulpy(.title2).weight(.heavy)).multilineTextAlignment(.center)
                    if let screen {
                        let date = Date(timeIntervalSince1970: Double(screen.startedAtMillis) / 1000)
                        Text("\(date.formatted(.dateTime.weekday(.abbreviated).day().month(.abbreviated))) · \(date.formatted(date: .omitted, time: .shortened))")
                            .foregroundStyle(PoulpyColor.onSurfaceVariant)
                    }
                    if let summary = state.summary {
                        (Text("player.summary \(Int(summary.setsDone)) \(Int(summary.setsPlanned)) \(summary.durationLabel)")
                            + (summary.setsSkipped > 0 ? Text(" · ") + Text("player.summarySkipped \(Int(summary.setsSkipped))") : Text("")))
                            .multilineTextAlignment(.center)
                            .foregroundStyle(PoulpyColor.onSurfaceVariant)
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
            ShareLink(item: ShareCardItem(card: card), preview: SharePreview(card.title)) {
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

    /// The grey Recover tag beside "Rest" or a recover step.
    private func recoverTag(_ label: String) -> some View {
        Text(label)
            .textCase(.uppercase)
            .font(.custom(PoulpyFontFace.nunitoExtraBold, size: 11.5, relativeTo: .caption2))
            .foregroundStyle(PoulpyColor.onSurfaceVariant)
            .padding(EdgeInsets(top: 3, leading: 8, bottom: 3, trailing: 8))
            .background(PoulpyColor.surfaceVariant, in: Capsule())
    }

    /// One dot per set: done ones green, the current one in the primary colour.
    private func setDots(number: Int, count: Int) -> some View {
        HStack(spacing: 8) {
            ForEach(0..<count, id: \.self) { index in
                Capsule()
                    .fill(index < number - 1 ? PoulpyColor.success : index == number - 1 ? PoulpyColor.primary : PoulpyColor.outline)
                    .frame(width: 28, height: 8)
            }
        }
        .padding(.top, 10)
        .padding(.bottom, 6)
        .accessibilityHidden(true)
    }

    private func clock(_ seconds: Int) -> String {
        String(format: "%d:%02d", seconds / 60, seconds % 60)
    }
}
