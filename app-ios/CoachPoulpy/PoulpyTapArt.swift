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

/// The frames of a mood move, cut from its sheet when it plays and let go after: the sheets are big, so none is kept.
private func moodFrames(_ move: TemperReaction) -> [UIImage] {
    guard let path = Bundle.main.path(forResource: move.sheet, ofType: "png"),
          let image = UIImage(contentsOfFile: path)?.cgImage else { return [] }
    let columns = Int(PoulpyMoves.shared.COLUMNS), count = Int(move.frames), rows = Int(move.rows)
    let width = image.width / columns, height = image.height / rows
    return (0..<count).compactMap { index in
        image.cropping(to: CGRect(x: index % columns * width, y: index / columns * height, width: width, height: height)).map { UIImage(cgImage: $0) }
    }
}

/// Poulpy's still picture at `art` points, and when `drawing` has a move, a tap on him plays it: his jump, leaning to one side or the
/// other at random, frame by frame from the 3D model (`PoulpyMoves` in the shared code says which frame and when). The frames are
/// bigger than the picture, so he jumps out of its box and back into it. With Reduce Motion a tap does nothing.
///
/// The happy Poulpy loses patience when tapped again and again (`PoulpyTemper`): the fifth tap in a row turns him round and he comes out annoyed, three more
/// and he comes out fuming, each tap in between answered with a move of his mood; left alone for 5 seconds he improves his mood a step, by a turn the other way.
struct PoulpyTapArt: View {
    let drawing: PoulpyDrawing
    let description: String
    let art: CGFloat
    @Environment(\.accessibilityReduceMotion) private var reduceMotion
    @State private var move: (sheet: String, start: Date)?
    // His temper: only the happy Poulpy gets angry.
    @State private var temper = PoulpyTemper()
    @State private var level = TemperLevel.calm
    @State private var reaction: (move: TemperReaction, start: Date, frames: [UIImage])?
    @State private var loading = false
    @State private var calm: DispatchWorkItem?

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
            if let reaction {
                TimelineView(.animation) { timeline in
                    let elapsed = Int64(timeline.date.timeIntervalSince(reaction.start) * 1000)
                    if let index = reaction.move.frame(elapsedMillis: elapsed)?.intValue, index < reaction.frames.count {
                        Image(uiImage: reaction.frames[index])
                            .resizable()
                            .frame(
                                width: art * CGFloat(moves.FRAME_WIDTH) / CGFloat(moves.PICTURE_WIDTH),
                                height: art * (678.0 / 744.0) * CGFloat(moves.FRAME_HEIGHT) / CGFloat(moves.PICTURE_HEIGHT)
                            )
                    } else {
                        Color.clear.onAppear { reactionOver() }
                    }
                }
                .allowsHitTesting(false)
            }
            Image((angry ? level.drawing : drawing).imageName)
                .resizable()
                .scaledToFit()
                .frame(width: art, height: art)
                .accessibilityLabel(description)
                .opacity(move == nil && reaction == nil ? 1 : 0)
        }
        .frame(width: art, height: art)
        .contentShape(Rectangle())
        .onTapGesture { tap() }
    }

    private var angry: Bool { PoulpyTemper.Companion.shared.canGetAngry(drawing: drawing) }

    private func tap() {
        let moves = PoulpyMoves.shared
        guard move == nil, reaction == nil, !loading, !reduceMotion else { return }
        if angry {
            waitToCalm()
            if let answer = temper.tap() { play(answer); return }
        }
        guard moves.moves(drawing: drawing),
              let choice = moves.movesOf(drawing: drawing).randomElement(),
              let sheet = moves.sheet(drawing: drawing, move: choice) else { return }
        move = (sheet, Date())
    }

    /// Plays a mood move: its sheet is cut off the main thread, and the move starts when it is ready.
    private func play(_ answer: TemperReaction) {
        loading = true
        DispatchQueue.global(qos: .userInitiated).async {
            let frames = moodFrames(answer)
            DispatchQueue.main.async {
                loading = false
                reaction = (answer, Date(), frames)
            }
        }
    }

    /// The mood move is over: he is in the mood it ends in, and the wait to calm down starts again.
    private func reactionOver() {
        guard let done = reaction?.move else { return }
        temper.finished(reaction: done)
        level = temper.level
        reaction = nil
        waitToCalm()
    }

    /// Left alone for a while he improves his mood by a step.
    private func waitToCalm() {
        calm?.cancel()
        let item = DispatchWorkItem {
            guard move == nil, reaction == nil, !loading, let answer = temper.calm() else { return }
            play(answer)
        }
        calm = item
        DispatchQueue.main.asyncAfter(deadline: .now() + Double(PoulpyTemper.Companion.shared.CALM_MILLIS) / 1000, execute: item)
    }
}
