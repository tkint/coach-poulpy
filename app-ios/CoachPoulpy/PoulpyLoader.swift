import SwiftUI
import UIKit
import Shared

/// The loader's pictures, from the app's bundle (design/poulpy-3d/loader), read once for every loader on screen: the swing's sheet cut
/// into its frames, or the one clockwise picture.
private enum LoaderPictures {
    private static func load(_ name: String) -> UIImage? {
        guard let path = Bundle.main.path(forResource: name, ofType: "png") else { return nil }
        return UIImage(contentsOfFile: path)
    }

    static let swing: [UIImage] = {
        guard let sheet = load("swing")?.cgImage else { return [] }
        let columns = Int(LoaderSwing.shared.COLUMNS), side = sheet.width / columns
        return (0..<Int(LoaderSwing.shared.FRAMES)).compactMap { index in
            sheet.cropping(to: CGRect(x: index % columns * side, y: index / columns * side, width: side, height: side))
                .map { UIImage(cgImage: $0) }
        }
    }()
    static let clockwise: UIImage? = load("clockwise")
}

/// The loading indicator: Poulpy's outline seen from underneath, his tentacles loose, one cycle every 3.6 s (12 s with Reduce Motion).
/// It swings half a turn one way and then half a turn the other, or with `clockwise` only turns clockwise. The picture of each moment
/// is drawn frame by frame from the 3D model (design/build-poulpy-pictures.mjs) and shows how his tentacles hang (clockwise, they
/// trail the same all the time, so that is one picture); the turn is smooth. Which frame and how far it is turned is `LoaderSwing`
/// in the shared code.
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
            let picture = clockwise ? LoaderPictures.clockwise : (frame < LoaderPictures.swing.count ? LoaderPictures.swing[frame] : nil)
            Image(uiImage: picture ?? UIImage())
                .resizable()
                .frame(width: size, height: size)
                .rotationEffect(.degrees(Double(degrees)))
        }
        .accessibilityElement()
        .accessibilityLabel(Text("common.loading"))
    }
}
