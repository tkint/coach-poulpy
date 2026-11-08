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
                    ForEach(recap.parts, id: \.key) { part in partBlock(part) }
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

    /// A part as an outlined block, its icon and name sitting on the top border; the unnamed block of steps has no header.
    private func partBlock(_ part: RecapPart) -> some View {
        let named = part.kind != nil
        return VStack(spacing: 0) {
            ForEach(Array(part.rows.enumerated()), id: \.element.key) { index, row in
                if index > 0 { Divider().overlay(PoulpyColor.outline) }
                rowView(row)
            }
        }
        .padding(.horizontal, 10)
        .padding(.top, named ? PoulpySpace.md : 0)
        .overlay(RoundedRectangle(cornerRadius: PoulpyRadius.sm).stroke(PoulpyColor.outline))
        .overlay(alignment: .topLeading) {
            if let kind = part.kind { partHeader(part, kind).offset(x: PoulpySpace.sm, y: -13) }
        }
        .padding(.top, named ? 13 : 0)
    }

    private func partHeader(_ part: RecapPart, _ kind: PartKind) -> some View {
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
        .background(PoulpyColor.surface)
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
