import SwiftUI
import Shared

/// The loading indicator: Poulpy's outline seen from above, swinging half a turn one way and then half a turn the other way,
/// forever, once every 3.6 s (12 s with Reduce Motion). It is one tentacle drawn eight times, and each tentacle hangs loose,
/// bending about its base behind the turn. Where everything is at a given time is `LoaderSwing` in the shared code.
struct PoulpyLoader: View {
    var size: CGFloat = 48
    /// Only turn clockwise, one full turn every 3.6 s, instead of swinging there and back.
    var clockwise = false
    @Environment(\.accessibilityReduceMotion) private var reduceMotion

    var body: some View {
        let period = reduceMotion ? LoaderSwing.shared.REDUCED_MILLIS : LoaderSwing.shared.MILLIS
        TimelineView(.animation) { timeline in
            let elapsed = Int64(timeline.date.timeIntervalSinceReferenceDate * 1000)
            ZStack {
                ForEach(0..<Int(LoaderSwing.shared.TENTACLES), id: \.self) { index in
                    Image("PoulpyLoaderTentacle")
                        .resizable()
                        .rotationEffect(
                            .degrees(Double(clockwise ? LoaderSwing.shared.clockwiseTentacleDegrees(elapsedMillis: elapsed, index: Int32(index), periodMillis: period) : LoaderSwing.shared.tentacleDegrees(elapsedMillis: elapsed, index: Int32(index), periodMillis: period))),
                            anchor: UnitPoint(x: 0.5, y: CGFloat(LoaderSwing.shared.BASE))
                        )
                        .rotationEffect(.degrees(Double(index) * 360 / Double(LoaderSwing.shared.TENTACLES)))
                }
            }
            .frame(width: size, height: size)
            .rotationEffect(.degrees(Double(clockwise ? LoaderSwing.shared.clockwiseDegrees(elapsedMillis: elapsed, periodMillis: period) : LoaderSwing.shared.degrees(elapsedMillis: elapsed, periodMillis: period))))
        }
        .accessibilityElement()
        .accessibilityLabel(Text("common.loading"))
    }
}
