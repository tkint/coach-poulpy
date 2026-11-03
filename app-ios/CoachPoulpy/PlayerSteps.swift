import SwiftUI
import Shared

/// The pieces of the guided session that Live shares when it plays a workout mixing walks and runs with other
/// exercises (#82): the bar, a work step, a rest. Everything they show is decided in Kotlin; they only draw it.
enum PlayerSteps {
    /// Pause and end sit on the bar over the whole session; the step's own buttons are below.
    @ViewBuilder
    static func bar(_ state: PlayerState, paused: Bool, canToggle: Bool, onToggle: @escaping () -> Void, onEnd: @escaping () -> Void) -> some View {
        HStack(spacing: PoulpySpace.md) {
            iconButton(paused ? "play.fill" : "pause.fill", paused ? "player.resume" : "player.pause", action: onToggle)
                .disabled(!canToggle)
                .opacity(canToggle ? 1 : 0.4)
            VStack(alignment: .leading, spacing: 6) {
                capsLabel(Text("player.step \(Int(state.phaseNumber)) \(Int(state.phaseCount))") + Text(" · \(state.sessionClock)"))
                progressTrack(Double(state.phaseNumber) / Double(max(state.phaseCount, 1)))
            }
            iconButton("xmark", "player.stop", action: onEnd)
        }
    }

    static func waitingLine(_ key: LocalizedStringKey) -> some View {
        Text(key)
            .font(.custom(PoulpyFontFace.nunitoBold, size: 17, relativeTo: .body))
            .foregroundStyle(PoulpyColor.onSurfaceVariant)
            .multilineTextAlignment(.center)
    }

    /// A walk or run that has not begun, shared with the guided player: `message` says what it waits for, Start begins it.
    @ViewBuilder
    static func startStep<Message: View>(
        _ state: PlayerState,
        startLabel: LocalizedStringKey,
        onSkip: @escaping () -> Void,
        onDone: @escaping () -> Void,
        onStart: @escaping () -> Void,
        @ViewBuilder message: () -> Message
    ) -> some View {
        Spacer(minLength: 0)
        Image(PoulpyDrawing.straining.imageName).resizable().scaledToFit().frame(width: 130, height: 117).accessibilityHidden(true)
        partLine(state)
        Text(state.title).font(PoulpyFont.title).multilineTextAlignment(.center)
        message()
        Text(state.targetLabel ?? "").font(.custom(PoulpyFontFace.nunitoExtraBold, fixedSize: 48)).multilineTextAlignment(.center)
        Spacer(minLength: 0)
        Button(action: onStart) {
            Text(startLabel)
                .font(.custom(PoulpyFontFace.nunitoExtraBold, size: 19, relativeTo: .body))
                .foregroundStyle(PoulpyColor.onPrimary)
                .frame(maxWidth: .infinity, minHeight: 64)
                .background(PoulpyColor.primary, in: Capsule())
        }
        .buttonStyle(.plain)
        // Done cuts the step short without the feel of skipping it.
        HStack(spacing: PoulpySpace.md) {
            outlinedButton("player.skip", action: onSkip)
            outlinedButton("player.done", action: onDone)
        }
    }

    private static func outlinedButton(_ key: LocalizedStringKey, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Text(key)
                .font(.custom(PoulpyFontFace.nunitoExtraBold, size: 17, relativeTo: .body))
                .foregroundStyle(PoulpyColor.onBackground)
                .frame(maxWidth: .infinity, minHeight: 56)
                .overlay(Capsule().stroke(PoulpyColor.outline, lineWidth: 2))
        }
        .buttonStyle(.plain)
    }

    /// An exercise: Poulpy straining, the part, the name, the set, the big target, what comes next, Skip and Done (`player-set`, `player-circuit-round`).
    @ViewBuilder
    static func workStep(_ state: PlayerState, paused: Bool, alwaysDone: Bool = false, onSkip: @escaping () -> Void, onDone: @escaping () -> Void) -> some View {
        let remaining = state.remainingSeconds?.intValue
        // A timed step ends by itself; Live also offers Done, to cut a step short without skipping it.
        let showDone = !state.isTimed || alwaysDone
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
                onSkip()
            } label: {
                Text("player.skip")
                    .font(.custom(PoulpyFontFace.nunitoExtraBold, size: 19, relativeTo: .body))
                    .foregroundStyle(PoulpyColor.onBackground)
                    .padding(.horizontal, 22)
                    .frame(maxWidth: showDone ? nil : .infinity, minHeight: 64)
                    .overlay(Capsule().stroke(PoulpyColor.outline, lineWidth: 2))
            }
            if showDone {
                Button {
                    onDone()
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
    static func restStep(_ state: PlayerState, paused: Bool, onSkip: @escaping () -> Void, onAdjust: @escaping (Int) -> Void) -> some View {
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
            adjustButton("player.restLess \(Self.restAdjustSeconds)", paused: paused) { onAdjust(-Self.restAdjustSeconds) }
            adjustButton("player.restMore \(Self.restAdjustSeconds)", paused: paused) { onAdjust(Self.restAdjustSeconds) }
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
            onSkip()
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
    static let restAdjustSeconds = 15

    static func adjustButton(_ title: LocalizedStringKey, paused: Bool, action: @escaping () -> Void) -> some View {
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
    static func partLine(_ state: PlayerState) -> some View {
        if let part = state.partLabel {
            HStack(spacing: 6) {
                if let kind = state.partKind { Image(systemName: partIcon(kind)).font(.system(size: 13, weight: .bold)) }
                capsLabel(Text(part), color: PoulpyColor.primary)
            }
            .foregroundStyle(PoulpyColor.primary)
        }
    }

    /// The small capitals label of the mockups: the step count, "Next", "First up".
    static func capsLabel(_ text: Text, color: Color = PoulpyColor.onSurfaceVariant) -> some View {
        text.font(PoulpyFont.label).kerning(1).textCase(.uppercase).foregroundStyle(color)
    }

    /// The round button on a surface at the top of the player (`icon-button--filled`).
    static func iconButton(_ systemName: String, _ label: LocalizedStringKey, action: @escaping () -> Void) -> some View {
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

    static func progressTrack(_ fraction: Double) -> some View {
        Capsule().fill(PoulpyColor.surfaceVariant).frame(height: 8)
            .overlay(alignment: .leading) {
                GeometryReader { proxy in
                    Capsule().fill(PoulpyColor.primary).frame(width: proxy.size.width * min(max(fraction, 0), 1))
                }
            }
            .clipShape(Capsule())
            .accessibilityHidden(true)
    }


    /// The grey Recover tag beside "Rest" or a recover step.
    static func recoverTag(_ label: String) -> some View {
        Text(label)
            .textCase(.uppercase)
            .font(.custom(PoulpyFontFace.nunitoExtraBold, size: 11.5, relativeTo: .caption2))
            .foregroundStyle(PoulpyColor.onSurfaceVariant)
            .padding(EdgeInsets(top: 3, leading: 8, bottom: 3, trailing: 8))
            .background(PoulpyColor.surfaceVariant, in: Capsule())
    }

    /// One dot per set: done ones green, the current one in the primary colour.
    static func setDots(number: Int, count: Int) -> some View {
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

    static func clock(_ seconds: Int) -> String {
        String(format: "%d:%02d", seconds / 60, seconds % 60)
    }

    static func clockPadded(_ seconds: Int) -> String {
        String(format: "%02d:%02d", seconds / 60, seconds % 60)
    }
}
