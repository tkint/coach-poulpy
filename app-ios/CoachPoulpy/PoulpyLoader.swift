import SwiftUI

/// The loading indicator: Poulpy seen from above as a blue outline, turning clockwise (the top moves left to right) once every
/// 2.4 s, or every 10 s with Reduce Motion. Below 32 points the line is thicker.
struct PoulpyLoader: View {
    var size: CGFloat = 48
    @Environment(\.accessibilityReduceMotion) private var reduceMotion

    var body: some View {
        let period = reduceMotion ? 10.0 : 2.4
        TimelineView(.animation) { timeline in
            let seconds = timeline.date.timeIntervalSinceReferenceDate
            Image(size < 32 ? "PoulpyLoaderSmall" : "PoulpyLoader")
                .resizable()
                .frame(width: size, height: size)
                .rotationEffect(.degrees(seconds.truncatingRemainder(dividingBy: period) / period * 360))
        }
        .accessibilityElement()
        .accessibilityLabel(Text("common.loading"))
    }
}
