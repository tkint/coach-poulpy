import SwiftUI
import Shared

/// What opening a finished session shows (`session-recap-*` mockups): its name and date, the figures, then each part
/// with the exercises done in it. Everything is decided in the shared `SessionRecap`; this only draws it.
/// The body map half of the opening view is not drawn yet.
struct SessionRecapView: View {
    let recap: SessionRecap
    let dateLine: String

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: PoulpySpace.sm) {
                VStack(alignment: .leading, spacing: 2) {
                    Text(recap.title).font(PoulpyFont.title).accessibilityAddTraits(.isHeader)
                    Text(dateLine).font(.poulpy(.subheadline)).foregroundStyle(PoulpyColor.onSurfaceVariant)
                }
                ForEach(Array(tileLines.enumerated()), id: \.offset) { _, line in
                    HStack(spacing: PoulpySpace.sm) {
                        ForEach(line, id: \.label) { tile in tileView(tile) }
                        ForEach(0..<(3 - line.count), id: \.self) { _ in Color.clear.frame(maxWidth: .infinity) }
                    }
                }
                if !recap.parts.isEmpty {
                    Text(recap.listTitle.uppercased())
                        .font(PoulpyFont.label)
                        .kerning(1)
                        .foregroundStyle(PoulpyColor.onSurfaceVariant)
                        .padding(.leading, PoulpySpace.xs)
                        .padding(.top, PoulpySpace.sm)
                    ForEach(recap.parts, id: \.key) { part in PartBlockView(part: part) }
                }
            }
            .padding(.horizontal, PoulpySpace.lg)
            .padding(.vertical, PoulpySpace.md)
        }
        .background(PoulpyColor.background)
    }

    private var tileLines: [[RecapTile]] {
        let tiles = recap.tiles
        return stride(from: 0, to: tiles.count, by: 3).map { Array(tiles[$0..<min($0 + 3, tiles.count)]) }
    }

    private func tileView(_ tile: RecapTile) -> some View {
        VStack(alignment: .leading, spacing: 0) {
            Text(tile.label).font(PoulpyFont.label).foregroundStyle(PoulpyColor.onSurfaceVariant).lineLimit(1)
            Text(tile.value).font(.poulpy(.callout).weight(.heavy)).monospacedDigit().lineLimit(1)
        }
        .padding(.horizontal, 10)
        .padding(.vertical, 6)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(PoulpyColor.surfaceVariant, in: RoundedRectangle(cornerRadius: PoulpyRadius.sm))
    }
}

/// A part as an outlined block, its icon and name sitting on the top border; the unnamed block of steps has no header.
/// The border breaks around the title, so the title needs no background of its own.
private struct PartBlockView: View {
    let part: RecapPart
    @State private var headerWidth: CGFloat = 0

    var body: some View {
        let named = part.kind != nil
        let gapStart = PoulpySpace.sm
        return VStack(spacing: 0) {
            ForEach(Array(part.rows.enumerated()), id: \.element.key) { index, row in
                if index > 0 { Divider().overlay(PoulpyColor.outline) }
                rowView(row)
            }
        }
        .padding(.horizontal, 10)
        .padding(.top, named ? PoulpySpace.md : 0)
        .overlay(
            GappedOutline(gapStart: gapStart, gapEnd: named ? gapStart + headerWidth : 0, radius: PoulpyRadius.sm)
                .stroke(PoulpyColor.outline, lineWidth: 1)
        )
        .overlay(alignment: .topLeading) {
            if let kind = part.kind {
                partHeader(kind)
                    .background(GeometryReader { proxy in Color.clear.preference(key: HeaderWidthKey.self, value: proxy.size.width) })
                    .offset(x: gapStart, y: -13)
            }
        }
        .onPreferenceChange(HeaderWidthKey.self) { headerWidth = $0 }
        .padding(.top, named ? 13 : 0)
    }

    private func partHeader(_ kind: PartKind) -> some View {
        let work = kind == .work
        return HStack(spacing: 6) {
            Image(systemName: partIcon(kind))
                .font(.system(size: 11, weight: .bold))
                .foregroundStyle(work ? PoulpyColor.onPrimary : PoulpyColor.onPrimaryContainer)
                .frame(width: 24, height: 24)
                .background(work ? PoulpyColor.primary : PoulpyColor.primaryContainer, in: Circle())
                .accessibilityHidden(true)
            Text(part.name).font(.poulpy(.subheadline).weight(.heavy)).foregroundStyle(PoulpyColor.primary).lineLimit(1)
            Text(part.summary).font(.poulpy(.caption)).foregroundStyle(PoulpyColor.onSurfaceVariant).lineLimit(1)
        }
        .padding(.horizontal, 6)
        .accessibilityElement(children: .combine)
        .accessibilityAddTraits(.isHeader)
    }

    private func rowView(_ row: RecapRow) -> some View {
        HStack(spacing: 10) {
            PatternBadge(pattern: row.pattern, size: 32)
            VStack(alignment: .leading, spacing: 0) {
                Text(row.name).font(.poulpy(.subheadline).weight(.heavy))
                if !row.detail.isEmpty {
                    Text(row.detail).font(.poulpy(.footnote)).foregroundStyle(PoulpyColor.onSurfaceVariant)
                }
            }
            Spacer(minLength: 0)
        }
        .padding(.vertical, 6)
        .accessibilityElement(children: .combine)
    }
}

private struct HeaderWidthKey: PreferenceKey {
    static var defaultValue: CGFloat = 0
    static func reduce(value: inout CGFloat, nextValue: () -> CGFloat) { value = max(value, nextValue()) }
}

/// A rounded outline whose top edge is open from `gapStart` to `gapEnd` (from the left edge); closed when `gapEnd` is not past `gapStart`.
private struct GappedOutline: Shape {
    let gapStart: CGFloat
    let gapEnd: CGFloat
    let radius: CGFloat

    func path(in bounds: CGRect) -> Path {
        let rect = bounds.insetBy(dx: 0.5, dy: 0.5)
        let r = radius
        let broken = gapEnd > gapStart
        var path = Path()
        path.move(to: CGPoint(x: rect.minX + (broken ? gapEnd : r), y: rect.minY))
        path.addLine(to: CGPoint(x: rect.maxX - r, y: rect.minY))
        path.addArc(tangent1End: CGPoint(x: rect.maxX, y: rect.minY), tangent2End: CGPoint(x: rect.maxX, y: rect.minY + r), radius: r)
        path.addLine(to: CGPoint(x: rect.maxX, y: rect.maxY - r))
        path.addArc(tangent1End: CGPoint(x: rect.maxX, y: rect.maxY), tangent2End: CGPoint(x: rect.maxX - r, y: rect.maxY), radius: r)
        path.addLine(to: CGPoint(x: rect.minX + r, y: rect.maxY))
        path.addArc(tangent1End: CGPoint(x: rect.minX, y: rect.maxY), tangent2End: CGPoint(x: rect.minX, y: rect.maxY - r), radius: r)
        path.addLine(to: CGPoint(x: rect.minX, y: rect.minY + r))
        path.addArc(tangent1End: CGPoint(x: rect.minX, y: rect.minY), tangent2End: CGPoint(x: rect.minX + r, y: rect.minY), radius: r)
        if broken {
            path.addLine(to: CGPoint(x: rect.minX + gapStart, y: rect.minY))
        } else {
            path.closeSubpath()
        }
        return path
    }
}
