import SwiftUI

/// The loading indicator: a picture of the 3D Poulpy seen from above, turning clockwise (the top moves left to right) once
/// every 2.4 s, or every 10 s with Reduce Motion.
struct PoulpyLoader: View {
    var size: CGFloat = 48
    @Environment(\.accessibilityReduceMotion) private var reduceMotion

    var body: some View {
        let period = reduceMotion ? 10.0 : 2.4
        TimelineView(.animation) { timeline in
            let seconds = timeline.date.timeIntervalSinceReferenceDate
            Image("PoulpyLoader")
                .resizable()
                .frame(width: size, height: size)
                .rotationEffect(.degrees(seconds.truncatingRemainder(dividingBy: period) / period * 360))
        }
        .accessibilityElement()
        .accessibilityLabel(Text("common.loading"))
    }
}
