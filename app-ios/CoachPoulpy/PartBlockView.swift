import SwiftUI
import Shared

/// A part as an outlined block, its icon, name and summary sitting on the top border's left and its `time` on the right
/// (`session-recap-*` and `workout-page-*` mockups). The border breaks around both, so neither needs a background of its
/// own. `content` is its rows, which the caller separates.
struct PartBlockView<Content: View>: View {
    let kind: PartKind
    let name: String
    let summary: String
    let time: String
    @ViewBuilder let content: () -> Content
    @State private var headerWidth: CGFloat = 0
    @State private var timeWidth: CGFloat = 0

    var body: some View {
        let gap = PoulpySpace.sm
        return VStack(spacing: 0, content: content)
            .padding(.horizontal, 10)
            .padding(.top, PoulpySpace.md)
            .overlay(
                GappedOutline(gap: gap, headerWidth: headerWidth, timeWidth: time.isEmpty ? 0 : timeWidth, radius: PoulpyRadius.sm)
                    .stroke(PoulpyColor.outline, lineWidth: 1)
            )
            .overlay(alignment: .topLeading) {
                header
                    .background(GeometryReader { proxy in Color.clear.preference(key: HeaderWidthKey.self, value: proxy.size.width) })
                    .offset(x: gap, y: -13)
            }
            .overlay(alignment: .topTrailing) {
                if !time.isEmpty {
                    PartTimeLabel(time: time)
                        .padding(.horizontal, 6)
                        .background(GeometryReader { proxy in Color.clear.preference(key: TimeWidthKey.self, value: proxy.size.width) })
                        .offset(x: -gap, y: -9)
                }
            }
            .onPreferenceChange(HeaderWidthKey.self) { headerWidth = $0 }
            .onPreferenceChange(TimeWidthKey.self) { timeWidth = $0 }
            .padding(.top, 13)
    }

    private var header: some View {
        let work = kind == .work
        return HStack(spacing: 6) {
            Image(systemName: partIcon(kind))
                .font(.system(size: 11, weight: .bold))
                .foregroundStyle(work ? PoulpyColor.onPrimary : PoulpyColor.onPrimaryContainer)
                .frame(width: 24, height: 24)
                .background(work ? PoulpyColor.primary : PoulpyColor.primaryContainer, in: Circle())
                .accessibilityHidden(true)
            Text(name).font(.poulpy(.subheadline).weight(.heavy)).foregroundStyle(PoulpyColor.primary).lineLimit(1)
            Text(summary).font(.poulpy(.caption)).foregroundStyle(PoulpyColor.onSurfaceVariant).lineLimit(1)
        }
        .padding(.horizontal, 6)
        .accessibilityElement(children: .combine)
        .accessibilityAddTraits(.isHeader)
    }
}

/// A time in small heavy digits, on a block's border or at the end of a recap row.
struct PartTimeLabel: View {
    let time: String

    var body: some View {
        Text(time).font(.poulpy(.caption).weight(.heavy)).monospacedDigit().foregroundStyle(PoulpyColor.onSurfaceVariant).lineLimit(1)
    }
}

private struct HeaderWidthKey: PreferenceKey {
    static var defaultValue: CGFloat = 0
    static func reduce(value: inout CGFloat, nextValue: () -> CGFloat) { value = max(value, nextValue()) }
}

private struct TimeWidthKey: PreferenceKey {
    static var defaultValue: CGFloat = 0
    static func reduce(value: inout CGFloat, nextValue: () -> CGFloat) { value = max(value, nextValue()) }
}

/// A rounded outline whose top edge is open for the title (`headerWidth` from `gap`) and, when `timeWidth` is not zero, for the time (that wide, `gap` from the right edge).
private struct GappedOutline: Shape {
    let gap: CGFloat
    let headerWidth: CGFloat
    let timeWidth: CGFloat
    let radius: CGFloat

    func path(in bounds: CGRect) -> Path {
        let rect = bounds.insetBy(dx: 0.5, dy: 0.5)
        let r = radius
        let titleEnd = rect.minX + gap + headerWidth
        var path = Path()
        if timeWidth > 0 {
            path.move(to: CGPoint(x: titleEnd, y: rect.minY))
            path.addLine(to: CGPoint(x: rect.maxX - gap - timeWidth, y: rect.minY))
            path.move(to: CGPoint(x: rect.maxX - gap, y: rect.minY))
        } else {
            path.move(to: CGPoint(x: titleEnd, y: rect.minY))
        }
        path.addLine(to: CGPoint(x: rect.maxX - r, y: rect.minY))
        path.addArc(tangent1End: CGPoint(x: rect.maxX, y: rect.minY), tangent2End: CGPoint(x: rect.maxX, y: rect.minY + r), radius: r)
        path.addLine(to: CGPoint(x: rect.maxX, y: rect.maxY - r))
        path.addArc(tangent1End: CGPoint(x: rect.maxX, y: rect.maxY), tangent2End: CGPoint(x: rect.maxX - r, y: rect.maxY), radius: r)
        path.addLine(to: CGPoint(x: rect.minX + r, y: rect.maxY))
        path.addArc(tangent1End: CGPoint(x: rect.minX, y: rect.maxY), tangent2End: CGPoint(x: rect.minX, y: rect.maxY - r), radius: r)
        path.addLine(to: CGPoint(x: rect.minX, y: rect.minY + r))
        path.addArc(tangent1End: CGPoint(x: rect.minX, y: rect.minY), tangent2End: CGPoint(x: rect.minX + r, y: rect.minY), radius: r)
        path.addLine(to: CGPoint(x: rect.minX + gap, y: rect.minY))
        return path
    }
}

