import SwiftUI

/// The three places a detail sheet rests, lowest first.
private enum SheetStop: Int {
    case peek, half, full
}

/// How far the content of a sheet is scrolled, read from a marker at the top of the content.
private struct ScrollOffsetKey: PreferenceKey {
    static var defaultValue: CGFloat = 0
    static func reduce(value: inout CGFloat, nextValue: () -> CGFloat) { value = nextValue() }
}

/// A sheet over a full-height background (the body map of the workout page and the session recap), resting at three stops: peek shows its top,
/// half (`halfShare` of the screen left to the background) is the default, full covers the background and scrolls.
/// The handle drags or taps between stops; below full, dragging anywhere on the sheet moves it; at full, pulling
/// down from the top of the content drags the sheet back down. `background` gets the room above the sheet, never less
/// than at the half stop, so what it draws does not shrink when the sheet goes up, and the top safe area inset.
struct DetailSheet<Background: View, Floating: View, Actions: View, Content: View>: View {
    let showBackgroundLabel: LocalizedStringKey
    let showDetailsLabel: LocalizedStringKey
    var halfShare: CGFloat = 0.46
    let background: (_ room: CGFloat, _ safeTop: CGFloat) -> Background
    let floating: () -> Floating
    let actions: () -> Actions
    let content: () -> Content

    /// The height of the sheet at its lowest stop: the handle, a title and the action bar.
    private static var peekHeight: CGFloat { 196 }
    /// How far a pull down from the top of the content goes before the sheet starts to follow it.
    private static var pullDeadZone: CGFloat { 56 }
    /// How much of a fling's speed counts when picking where the sheet rests: little, so only a wide drag changes the stop.
    private static var flingShare: CGFloat { 0.25 }

    @State private var stop = SheetStop.half
    @GestureState private var dragged: CGFloat = 0
    /// How far the content is pulled down from its top at full height; the sheet follows it.
    @State private var pulled: CGFloat = 0
    @State private var pullStartedAtTop: Bool?
    @State private var contentOffset: CGFloat = 0

    var body: some View {
        GeometryReader { geo in
            let halfTop = geo.size.height * halfShare
            let peekTop = geo.size.height - Self.peekHeight - geo.safeAreaInsets.bottom
            let stops: [CGFloat] = [peekTop, halfTop, 0]
            let base = stops[stop.rawValue]
            let top = min(max(base + dragged + pulled, 0), peekTop)
            let full = min(max(1 - top / halfTop, 0), 1)
            ZStack(alignment: .topLeading) {
                PoulpyColor.background.ignoresSafeArea()
                background(max(top, halfTop), geo.safeAreaInsets.top)
                sheet(height: geo.size.height - top, safeTop: geo.safeAreaInsets.top * full, corner: PoulpyRadius.lg * (1 - full), stops: stops)
                    .offset(y: top)
                floating()
                    .padding(.top, geo.safeAreaInsets.top + 8)
                    .padding(.horizontal, PoulpySpace.lg)
            }
            .frame(width: geo.size.width, height: geo.size.height, alignment: .topLeading)
            .animation(.spring(response: 0.35, dampingFraction: 0.85), value: stop)
        }
        .ignoresSafeArea()
    }

    /// Rests at the stop nearest to where the sheet was heading, momentum included.
    private func settle(from current: CGFloat, predicted: CGFloat, stops: [CGFloat]) {
        let heading = current + predicted * Self.flingShare
        let nearest = stops.indices.min { abs(stops[$0] - heading) < abs(stops[$1] - heading) } ?? SheetStop.half.rawValue
        stop = SheetStop(rawValue: nearest) ?? .half
    }

    private func sheet(height: CGFloat, safeTop: CGFloat, corner: CGFloat, stops: [CGFloat]) -> some View {
        let drag = DragGesture()
            .updating($dragged) { value, current, _ in current = value.translation.height }
            .onEnded { value in
                settle(from: stops[stop.rawValue], predicted: value.predictedEndTranslation.height, stops: stops)
            }
        // At full height the content scrolls; once it is at the top, a pull down lowers the sheet instead.
        let pull = DragGesture(minimumDistance: 8)
            .onChanged { value in
                if pullStartedAtTop == nil { pullStartedAtTop = contentOffset <= 1 }
                pulled = pullStartedAtTop == true ? max(value.translation.height - Self.pullDeadZone, 0) : 0
            }
            .onEnded { value in
                if pullStartedAtTop == true && pulled > 0 {
                    settle(from: stops[stop.rawValue] + pulled, predicted: value.predictedEndTranslation.height - value.translation.height, stops: stops)
                }
                pulled = 0
                pullStartedAtTop = nil
            }
        return VStack(spacing: 0) {
            Button { stop = stop == .full ? .half : .full } label: {
                Capsule().fill(PoulpyColor.outline).frame(width: 40, height: 5)
                    .frame(maxWidth: .infinity, minHeight: 44)
                    .padding(.top, safeTop)
                    .contentShape(Rectangle())
            }
            .buttonStyle(.plain)
            .accessibilityLabel(stop == .full ? showBackgroundLabel : showDetailsLabel)
            .gesture(drag)
            ScrollView {
                content()
                    .padding(.horizontal, PoulpySpace.lg)
                    .background(
                        GeometryReader { marker in
                            Color.clear.preference(key: ScrollOffsetKey.self, value: -marker.frame(in: .named("detail-sheet-scroll")).minY)
                        }
                    )
            }
            .coordinateSpace(name: "detail-sheet-scroll")
            .onPreferenceChange(ScrollOffsetKey.self) { contentOffset = $0 }
            // Below full height the content stays put and dragging anywhere moves the sheet; at full height it scrolls.
            .scrollDisabled(stop != .full || pulled > 0)
            .simultaneousGesture(drag, including: stop == .full ? .none : .all)
            .simultaneousGesture(pull, including: stop == .full ? .all : .none)
            actions()
        }
        .frame(height: max(height, 0), alignment: .top)
        .background(
            PoulpyColor.surface
                .clipShape(UnevenRoundedRectangle(topLeadingRadius: corner, topTrailingRadius: corner))
                .ignoresSafeArea(edges: .bottom)
        )
        .clipped()
    }
}

extension DetailSheet where Actions == EmptyView {
    /// A sheet with no action bar under its content.
    init(
        showBackgroundLabel: LocalizedStringKey,
        showDetailsLabel: LocalizedStringKey,
        halfShare: CGFloat = 0.46,
        background: @escaping (_ room: CGFloat, _ safeTop: CGFloat) -> Background,
        floating: @escaping () -> Floating,
        content: @escaping () -> Content
    ) {
        self.init(
            showBackgroundLabel: showBackgroundLabel, showDetailsLabel: showDetailsLabel, halfShare: halfShare,
            background: background, floating: floating, actions: { EmptyView() }, content: content
        )
    }
}
