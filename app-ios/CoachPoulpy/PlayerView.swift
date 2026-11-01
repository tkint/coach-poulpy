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
            iconButton("xmark", "action.back") { dismiss() }
            Spacer()
        }
        Spacer()
        capsLabel(Text("\(state.workoutName) · \(state.exerciseCountLabel)"))
        Text("player.ready").font(PoulpyFont.display)
        Image(PoulpyDrawing.standingTall.imageName)
            .resizable().scaledToFit().frame(width: 200, height: 180).padding(.vertical, PoulpySpace.lg).accessibilityHidden(true)
        capsLabel(Text("player.firstUp"), color: PoulpyColor.primary)
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

        // Pause and end sit on the bar over the whole session; the step's own buttons are below.
        HStack(spacing: PoulpySpace.md) {
            iconButton(paused ? "play.fill" : "pause.fill", paused ? "player.resume" : "player.pause") {
                paused ? model.resume() : model.pause()
            }
            VStack(alignment: .leading, spacing: 6) {
                capsLabel(Text("player.step \(Int(state.phaseNumber)) \(Int(state.phaseCount))") + Text(" · \(state.sessionClock)"))
                progressTrack(Double(state.phaseNumber) / Double(max(state.phaseCount, 1)))
            }
            iconButton("xmark", "player.stop") { confirmStop = true }
        }

        if state.isRest { restStep(model, state, paused: paused) } else { workStep(model, state, paused: paused) }

        Color.clear.frame(height: 0)
            .confirmationDialog("player.stopTitle", isPresented: $confirmStop, titleVisibility: .visible) {
                Button("player.stop", role: .destructive) { model.stop() }
            } message: {
                Text("player.stopBody")
            }
    }

    /// An exercise: Poulpy straining, the part, the name, the set, the big target, what comes next, Skip and Done (`player-set`, `player-circuit-round`).
    @ViewBuilder
    private func workStep(_ model: PlayerModel, _ state: PlayerState, paused: Bool) -> some View {
        let remaining = state.remainingSeconds?.intValue
        Spacer(minLength: 0)
        Image(PoulpyDrawing.straining.imageName).resizable().scaledToFit().frame(width: 130, height: 117).accessibilityHidden(true)
        partLine(state)
        HStack(spacing: PoulpySpace.sm) {
            if let role = state.roleLabel { recoverTag(role) }
            Text(state.title).font(PoulpyFont.title).multilineTextAlignment(.center)
        }
        if let side = state.sideLabel { Text(side).font(.poulpy(.title3)).foregroundStyle(PoulpyColor.primary) }
        if state.setCount > 1 {
            Text("player.set \(Int(state.setNumber)) \(Int(state.setCount))")
                .font(.custom(PoulpyFontFace.nunitoBold, size: 17, relativeTo: .body))
                .foregroundStyle(PoulpyColor.onSurfaceVariant)
            setDots(number: Int(state.setNumber), count: Int(state.setCount))
        } else {
            Color.clear.frame(height: PoulpySpace.md)
        }
        // A rep target is a big number with its unit under it; a timer, a distance or an open target is one line.
        if let big = remaining.map(clock) ?? state.targetValue {
            Text(big).font(.custom(PoulpyFontFace.nunitoExtraBold, fixedSize: 120).monospacedDigit()).kerning(-2.4).minimumScaleFactor(0.5).lineLimit(1)
        } else if let target = state.targetLabel {
            Text(target).font(.custom(PoulpyFontFace.nunitoExtraBold, fixedSize: 48)).multilineTextAlignment(.center)
        }
        if let unit = state.targetUnit ?? state.loadLabel {
            Text(unit).font(.custom(PoulpyFontFace.nunitoBold, size: 21, relativeTo: .title3)).multilineTextAlignment(.center)
        }
        if paused { Text("player.paused").font(.poulpy(.headline)).foregroundStyle(PoulpyColor.error) }
        Spacer(minLength: 0)

        if let line = state.nextLine {
            HStack(spacing: 10) {
                capsLabel(Text("player.next"))
                Text(line).font(.custom(PoulpyFontFace.nunitoBold, size: 15, relativeTo: .subheadline))
                Spacer(minLength: 0)
            }
            .padding(.horizontal, PoulpySpace.lg)
            .padding(.vertical, PoulpySpace.md)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(PoulpyColor.surface, in: RoundedRectangle(cornerRadius: PoulpyRadius.md))
        }
        HStack(spacing: PoulpySpace.md) {
            Button {
                model.skip()
            } label: {
                Text("player.skip")
                    .font(.custom(PoulpyFontFace.nunitoExtraBold, size: 19, relativeTo: .body))
                    .foregroundStyle(PoulpyColor.onBackground)
                    .padding(.horizontal, 22)
                    .frame(maxWidth: state.isTimed ? .infinity : nil, minHeight: 64)
                    .overlay(Capsule().stroke(PoulpyColor.outline, lineWidth: 2))
            }
            if !state.isTimed {
                Button {
                    model.done()
                } label: {
                    Label("player.done", systemImage: "checkmark")
                        .font(.custom(PoulpyFontFace.nunitoExtraBold, size: 19, relativeTo: .body))
                        .foregroundStyle(PoulpyColor.onPrimary)
                        .frame(maxWidth: .infinity, minHeight: 64)
                        .background(PoulpyColor.primary, in: Capsule())
                }
            }
        }
        .buttonStyle(.plain)
        .disabled(paused)
        .opacity(paused ? 0.4 : 1)
    }

    /// A rest: the ring running down around Poulpy catching his breath, ±15 s, what is next, Skip rest (`player-rest`, `player-recover`).
    @ViewBuilder
    private func restStep(_ model: PlayerModel, _ state: PlayerState, paused: Bool) -> some View {
        let remaining = state.remainingSeconds?.intValue ?? 0
        let total = max(state.totalSeconds?.intValue ?? 0, 1)
        Spacer(minLength: 0)
        if state.inRepeat { partLine(state) }
        HStack(spacing: 10) {
            if state.inRepeat, let role = state.roleLabel { recoverTag(role) }
            Text(state.title).font(PoulpyFont.title).foregroundStyle(PoulpyColor.primary)
        }
        ZStack {
            Circle().stroke(PoulpyColor.surfaceVariant, lineWidth: 12 * 260 / 200)
            Circle()
                .trim(from: 0, to: min(max(Double(remaining) / Double(total), 0), 1))
                .stroke(PoulpyMood.recovering.tone, style: StrokeStyle(lineWidth: 12 * 260 / 200, lineCap: .round))
                .rotationEffect(.degrees(-90))
            VStack(spacing: PoulpySpace.xs) {
                Image(PoulpyDrawing.sideGlance.imageName).resizable().scaledToFit().frame(width: 92, height: 83).accessibilityHidden(true)
                Text(clockPadded(remaining)).font(PoulpyFont.timer)
            }
        }
        .padding(12 * 260 / 400)
        .frame(width: 260, height: 260)
        HStack(spacing: PoulpySpace.md) {
            adjustButton("player.restLess \(Self.restStep)", paused: paused) { model.adjustRest(seconds: -Int32(Self.restStep)) }
            adjustButton("player.restMore \(Self.restStep)", paused: paused) { model.adjustRest(seconds: Int32(Self.restStep)) }
        }
        if paused { Text("player.paused").font(.poulpy(.headline)).foregroundStyle(PoulpyColor.error) }
        Spacer(minLength: 0)

        if let headline = state.nextHeadline {
            VStack(alignment: .leading, spacing: 2) {
                if let round = state.nextRound {
                    capsLabel(Text("player.nextRound \(round)"))
                } else {
                    capsLabel(Text("player.nextUp"))
                }
                Text(headline).font(PoulpyFont.headline)
                if let detail = state.nextDetail {
                    Text(detail).font(.custom(PoulpyFontFace.nunitoMedium, size: 16, relativeTo: .body)).foregroundStyle(PoulpyColor.onSurfaceVariant)
                }
            }
            .padding(.horizontal, PoulpySpace.lg)
            .padding(.vertical, 14)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(PoulpyColor.surface, in: RoundedRectangle(cornerRadius: PoulpyRadius.md))
        }
        Button {
            model.skip()
        } label: {
            Label("player.skipRest", systemImage: "forward.end.fill")
                .font(.custom(PoulpyFontFace.nunitoExtraBold, size: 19, relativeTo: .body))
                .foregroundStyle(PoulpyColor.onPrimaryContainer)
                .frame(maxWidth: .infinity, minHeight: 64)
                .background(PoulpyColor.primaryContainer, in: Capsule())
        }
        .buttonStyle(.plain)
        .disabled(paused)
        .opacity(paused ? 0.4 : 1)
    }

    /// How much the two buttons under the rest ring add or take away.
    private static let restStep = 15

    private func adjustButton(_ title: LocalizedStringKey, paused: Bool, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Text(title)
                .font(.custom(PoulpyFontFace.nunitoExtraBold, size: 16, relativeTo: .body).monospacedDigit())
                .foregroundStyle(PoulpyColor.onBackground)
                .padding(.horizontal, 18)
                .frame(minHeight: 48)
                .overlay(Capsule().stroke(PoulpyColor.outline, lineWidth: 2))
        }
        .buttonStyle(.plain)
        .disabled(paused)
        .opacity(paused ? 0.4 : 1)
    }

    /// "WORK 1 · ROUND 2 OF 4": the part this step is in, with its icon.
    @ViewBuilder
    private func partLine(_ state: PlayerState) -> some View {
        if let part = state.partLabel {
            HStack(spacing: 6) {
                if let kind = state.partKind { Image(systemName: partIcon(kind)).font(.system(size: 13, weight: .bold)) }
                capsLabel(Text(part), color: PoulpyColor.primary)
            }
            .foregroundStyle(PoulpyColor.primary)
        }
    }

    /// The small capitals label of the mockups: the step count, "Next", "First up".
    private func capsLabel(_ text: Text, color: Color = PoulpyColor.onSurfaceVariant) -> some View {
        text.font(PoulpyFont.label).kerning(1).textCase(.uppercase).foregroundStyle(color)
    }

    /// The round button on a surface at the top of the player (`icon-button--filled`).
    private func iconButton(_ systemName: String, _ label: LocalizedStringKey, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Image(systemName: systemName)
                .font(.system(size: 18, weight: .bold))
                .foregroundStyle(PoulpyColor.onBackground)
                .frame(width: 44, height: 44)
                .background(PoulpyColor.surface, in: Circle())
        }
        .buttonStyle(.plain)
        .accessibilityLabel(label)
    }

    private func progressTrack(_ fraction: Double) -> some View {
        Capsule().fill(PoulpyColor.surfaceVariant).frame(height: 8)
            .overlay(alignment: .leading) {
                GeometryReader { proxy in
                    Capsule().fill(PoulpyColor.primary).frame(width: proxy.size.width * min(max(fraction, 0), 1))
                }
            }
            .clipShape(Capsule())
            .accessibilityHidden(true)
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

    private func clockPadded(_ seconds: Int) -> String {
        String(format: "%02d:%02d", seconds / 60, seconds % 60)
    }
}
