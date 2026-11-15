import SwiftUI
import Shared

/// The loading indicator: Poulpy's outline seen from above, swinging half a turn one way and then half a turn the other
/// way, forever, once every 3.6 s (12 s with Reduce Motion). The swing itself is `LoaderSwing` in the shared code.
struct PoulpyLoader: View {
    var size: CGFloat = 48
    @Environment(\.accessibilityReduceMotion) private var reduceMotion

    var body: some View {
        let period = reduceMotion ? LoaderSwing.shared.REDUCED_MILLIS : LoaderSwing.shared.MILLIS
        TimelineView(.animation) { timeline in
            let millis = Int64(timeline.date.timeIntervalSinceReferenceDate * 1000)
            Image("PoulpyLoader")
                .resizable()
                .frame(width: size, height: size)
                .rotationEffect(.degrees(Double(LoaderSwing.shared.degrees(elapsedMillis: millis, periodMillis: period))))
        }
        .accessibilityElement()
        .accessibilityLabel(Text("common.loading"))
    }
}
