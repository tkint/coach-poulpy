import SwiftUI
import Shared

/// Poulpy in the middle of his eight tentacles, with what he has to say.
struct PoulpyHeaderView: View {
    let status: PoulpyStatus

    var body: some View {
        let ember = !status.blueSide
        VStack(spacing: PoulpySpace.sm) {
            ZStack {
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
                    .frame(width: 150, height: 150)
                    .accessibilityLabel(status.title)
                    .id(status.drawing.assetName)
                    .transition(.opacity)
            }
            .frame(width: 208, height: 208)
            Text(status.title).font(PoulpyFont.headline).multilineTextAlignment(.center)
            Text(status.line).font(PoulpyFont.body).multilineTextAlignment(.center)
            Text(status.tentaclesCaption)
                .font(PoulpyFont.label)
                .foregroundStyle(PoulpyColor.onSurfaceVariant)
                .multilineTextAlignment(.center)
        }
        .frame(maxWidth: .infinity)
        .padding(PoulpySpace.lg)
        .background(ember ? PoulpyColor.grumpyContainer : PoulpyColor.primaryContainer, in: RoundedRectangle(cornerRadius: PoulpyRadius.md))
        .animation(.easeInOut(duration: PoulpyMotion.moodDuration), value: status.drawing.assetName)
    }
}

/// Eight rounded segments clockwise from the top, as in the brand book's week ring.
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
                        .trim(from: (Double(i) * 45 + 5) / 360, to: (Double(i) * 45 + 40) / 360)
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
