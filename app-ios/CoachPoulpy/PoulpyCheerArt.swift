import SwiftUI
import UIKit
import Shared

/// The sheets of the celebration, from the app's bundle (design/poulpy-3d/moves). They are big, so a sheet is cut into its frames
/// when it is needed and only the one playing and the next are kept.
private enum CheerSheets {
    private static var cache: [Int: [UIImage]] = [:]

    static func frames(_ index: Int) -> [UIImage] {
        if let known = cache[index] { return known }
        let cheer = PoulpyCheer.shared
        guard index < Int(cheer.SHEETS),
              let path = Bundle.main.path(forResource: cheer.sheet(index: Int32(index)), ofType: "png"),
              let image = UIImage(contentsOfFile: path)?.cgImage else { return [] }
        let columns = Int(cheer.COLUMNS), count = Int(cheer.SHEET_FRAMES)
        let width = image.width / columns, height = image.height / ((count + columns - 1) / columns)
        let frames: [UIImage] = (0..<count).compactMap { at in
            image.cropping(to: CGRect(x: at % columns * width, y: at / columns * height, width: width, height: height))
                .map { UIImage(cgImage: $0) }
        }
        cache = cache.filter { $0.key >= index - 1 }
        cache[index] = frames
        return frames
    }
}

/// Poulpy's still picture at `art` points, and when `drawing` celebrates (the champion), he jumps for joy three times with his medal
/// and confetti when the screen opens, frame by frame from the 3D model (`PoulpyCheer` in the shared code says which frame and when),
/// and settles on the picture; a tap plays it again. The frames are bigger than the picture so the confetti has room. With Reduce
/// Motion he just stands there.
struct PoulpyCheerArt: View {
    let drawing: PoulpyDrawing
    let art: CGFloat
    @Environment(\.accessibilityReduceMotion) private var reduceMotion
    @State private var start: Date?

    var body: some View {
        let cheer = PoulpyCheer.shared
        ZStack {
            if let start {
                TimelineView(.animation) { timeline in
                    let elapsed = Int64(timeline.date.timeIntervalSince(start) * 1000)
                    if let index = cheer.frame(elapsedMillis: elapsed)?.intValue {
                        let frames = CheerSheets.frames(Int(cheer.sheetOf(frame: Int32(index))))
                        let at = index % Int(cheer.SHEET_FRAMES)
                        if at < frames.count {
                            // The frame shows him the size of the still picture, with room around him: it is that much bigger, on the same centre.
                            Image(uiImage: frames[at])
                                .resizable()
                                .frame(
                                    width: art * CGFloat(cheer.FRAME_WIDTH) / CGFloat(cheer.PICTURE_WIDTH),
                                    height: art * (678.0 / 744.0) * CGFloat(cheer.FRAME_HEIGHT) / CGFloat(cheer.PICTURE_HEIGHT)
                                )
                        }
                    } else {
                        Color.clear.onAppear { self.start = nil }
                    }
                }
                .allowsHitTesting(false)
            }
            Image(drawing.imageName)
                .resizable()
                .scaledToFit()
                .frame(width: art, height: art)
                .accessibilityHidden(true)
                .opacity(start == nil ? 1 : 0)
        }
        .frame(width: art, height: art)
        .contentShape(Rectangle())
        .onAppear { play() }
        .onTapGesture { play() }
    }

    private func play() {
        guard start == nil, !reduceMotion else { return }
        start = Date()
    }
}
