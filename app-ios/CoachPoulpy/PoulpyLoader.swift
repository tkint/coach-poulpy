import SwiftUI
import Shared

/// The loading indicator: Poulpy's outline seen from underneath, his tentacles loose, one cycle every 3.6 s (12 s with Reduce Motion).
/// It swings half a turn one way and then half a turn the other way, or with `clockwise` only turns clockwise. The picture of each
/// moment is drawn frame by frame from the 3D model (design/build-poulpy-pictures.mjs) and shows how his tentacles hang; the turn
/// is smooth. Which frame and how far it is turned is `LoaderSwing` in the shared code.
struct PoulpyLoader: View {
    var size: CGFloat = 48
    /// Only turn clockwise, one full turn every cycle, instead of swinging there and back.
    var clockwise = false
    @Environment(\.accessibilityReduceMotion) private var reduceMotion

    var body: some View {
        let period = reduceMotion ? LoaderSwing.shared.REDUCED_MILLIS : LoaderSwing.shared.MILLIS
        TimelineView(.animation) { timeline in
            let elapsed = Int64(timeline.date.timeIntervalSinceReferenceDate * 1000)
            let frame = Int(LoaderSwing.shared.frame(elapsedMillis: elapsed, periodMillis: period))
            let degrees = clockwise
                ? LoaderSwing.shared.clockwiseDegrees(elapsedMillis: elapsed, periodMillis: period)
                : LoaderSwing.shared.degrees(elapsedMillis: elapsed, periodMillis: period)
            Image("PoulpyLoader\(clockwise ? "Clockwise" : "Swing")\(String(format: "%02d", frame))")
                .resizable()
                .frame(width: size, height: size)
                .rotationEffect(.degrees(Double(degrees)))
        }
        .accessibilityElement()
        .accessibilityLabel(Text("common.loading"))
    }
}
