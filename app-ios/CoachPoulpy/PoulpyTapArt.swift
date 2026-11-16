import SwiftUI
import UIKit
import Shared

/// The sheets of the moves, from the app's bundle (design/poulpy-3d/moves), cut into their frames once for every Poulpy on screen.
private enum MovePictures {
    private static var cache: [String: [UIImage]] = [:]

    static func frames(_ sheet: String) -> [UIImage] {
        if let known = cache[sheet] { return known }
        guard let path = Bundle.main.path(forResource: sheet, ofType: "png"),
              let image = UIImage(contentsOfFile: path)?.cgImage else { return [] }
        let columns = Int(PoulpyMoves.shared.COLUMNS), count = Int(PoulpyMoves.shared.FRAMES)
        let width = image.width / columns, height = image.height / ((count + columns - 1) / columns)
        let frames: [UIImage] = (0..<count).compactMap { index in
            image.cropping(to: CGRect(x: index % columns * width, y: index / columns * height, width: width, height: height))
                .map { UIImage(cgImage: $0) }
        }
        cache[sheet] = frames
        return frames
    }
}

/// Poulpy's still picture at `art` points, and when `drawing` has a move, a tap on him plays it: his jump, leaning to one side or the
/// other at random, frame by frame from the 3D model (`PoulpyMoves` in the shared code says which frame and when). The frames are
/// bigger than the picture, so he jumps out of its box and back into it. With Reduce Motion a tap does nothing.
struct PoulpyTapArt: View {
    let drawing: PoulpyDrawing
    let description: String
    let art: CGFloat
    @Environment(\.accessibilityReduceMotion) private var reduceMotion
    @State private var move: (sheet: String, start: Date)?

    var body: some View {
        // The champion's tap is his celebration: the same three jumps of joy as at the end of a session, once.
        if PoulpyCheer.shared.celebrates(drawing: drawing) {
            PoulpyCheerArt(drawing: drawing, art: art, autoplay: false, loop: false)
        } else {
            moveArt
        }
    }

    @ViewBuilder private var moveArt: some View {
        let moves = PoulpyMoves.shared
        ZStack {
            if let move {
                TimelineView(.animation) { timeline in
                    let elapsed = Int64(timeline.date.timeIntervalSince(move.start) * 1000)
                    let frames = MovePictures.frames(move.sheet)
                    if let index = moves.frame(elapsedMillis: elapsed)?.intValue, index < frames.count {
                        // The frame shows him the size of the still picture, with room around him: it is that much bigger, on the same centre.
                        Image(uiImage: frames[index])
                            .resizable()
                            .frame(
                                width: art * CGFloat(moves.FRAME_WIDTH) / CGFloat(moves.PICTURE_WIDTH),
                                height: art * (678.0 / 744.0) * CGFloat(moves.FRAME_HEIGHT) / CGFloat(moves.PICTURE_HEIGHT)
                            )
                    } else {
                        Color.clear.onAppear { self.move = nil }
                    }
                }
                .allowsHitTesting(false)
            }
            Image(drawing.imageName)
                .resizable()
                .scaledToFit()
                .frame(width: art, height: art)
                .accessibilityLabel(description)
                .opacity(move == nil ? 1 : 0)
        }
        .frame(width: art, height: art)
        .contentShape(Rectangle())
        .onTapGesture {
            guard move == nil, !reduceMotion, moves.moves(drawing: drawing),
                  let choice = moves.movesOf(drawing: drawing).randomElement(),
                  let sheet = moves.sheet(drawing: drawing, move: choice) else { return }
            move = (sheet, Date())
        }
    }
}
