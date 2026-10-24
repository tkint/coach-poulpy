import SwiftUI
import Shared

/// Poulpy in the middle of his eight tentacles, with what he has to say. `compact` puts the
/// ring beside the text, as on Home, where the day's workouts need the room.
struct PoulpyHeaderView: View {
    let status: PoulpyStatus
    var compact = false

    var body: some View {
        let ember = !status.blueSide
        Group {
            if compact {
                HStack(spacing: 14) {
                    ring(side: 104, art: 72)
                    VStack(alignment: .leading, spacing: 2) {
                        Text(status.title).font(PoulpyFont.headline)
                        Text(status.line).font(.poulpy(.subheadline).weight(.semibold))
                        Text(status.tentaclesCaption).font(PoulpyFont.label).padding(.top, 2)
                    }
                    .foregroundStyle(ember ? PoulpyColor.onGrumpyContainer : PoulpyColor.onPrimaryContainer)
                    Spacer(minLength: 0)
                }
                .padding(EdgeInsets(top: PoulpySpace.md, leading: PoulpySpace.md, bottom: PoulpySpace.md, trailing: PoulpySpace.lg))
            } else {
                VStack(spacing: PoulpySpace.sm) {
                    ring(side: 208, art: 150)
                    Text(status.title).font(PoulpyFont.headline).multilineTextAlignment(.center)
                    Text(status.line).font(PoulpyFont.body).multilineTextAlignment(.center)
                    Text(status.tentaclesCaption)
                        .font(PoulpyFont.label)
                        .foregroundStyle(PoulpyColor.onSurfaceVariant)
                        .multilineTextAlignment(.center)
                }
                .frame(maxWidth: .infinity)
                .padding(PoulpySpace.lg)
            }
        }
        .background(ember ? PoulpyColor.grumpyContainer : PoulpyColor.primaryContainer, in: RoundedRectangle(cornerRadius: compact ? PoulpyRadius.lg : PoulpyRadius.md))
        .animation(.easeInOut(duration: PoulpyMotion.moodDuration), value: status.drawing.assetName)
    }

    private func ring(side: CGFloat, art: CGFloat) -> some View {
        let ember = !status.blueSide
        return ZStack {
            TentacleRing(
                lit: Int(status.tentaclesLit),
                on: ember ? PoulpyColor.grumpy : PoulpyColor.primary,
                off: PoulpyColor.outline
            )
            .accessibilityElement()
            .accessibilityLabel(status.tentaclesCaption)
            Image(status.drawing.imageName)
                .resizable()
                .scaledToFit()
                .frame(width: art, height: art)
                .accessibilityLabel(status.title)
                .id(status.drawing.assetName)
                .transition(.opacity)
        }
        .frame(width: side, height: side)
    }
}

/// Eight rounded segments clockwise from the top, with clear gaps so each tentacle reads on its own.
private struct TentacleRing: View {
    let lit: Int
    let on: Color
    let off: Color

    var body: some View {
        GeometryReader { geometry in
            let side = min(geometry.size.width, geometry.size.height)
            let width = side * 15 / 200
            ZStack {
                ForEach(0..<8, id: \.self) { i in
                    Circle()
                        // A circle's path starts at 3 o'clock; turn it so the ring starts at 12.
                        .trim(from: (Double(i) * 45 + 11) / 360, to: (Double(i) * 45 + 34) / 360)
                        .rotation(.degrees(-90))
                        .stroke(i < lit ? on : off, style: StrokeStyle(lineWidth: width, lineCap: .round))
                        .padding(width / 2)
                }
            }
            .frame(width: side, height: side)
        }
    }
}

private extension PoulpyDrawing {
    /// The generated asset: "pose_side_glance" is PoulpyPoseSideGlance in Assets.xcassets/Poulpy.
    var imageName: String {
        "Poulpy" + assetName.split(separator: "_").map { $0.prefix(1).uppercased() + $0.dropFirst() }.joined()
    }
}
