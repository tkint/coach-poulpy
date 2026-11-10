import SwiftUI
import Shared

/// What opening a finished session shows (`session-recap-*` mockups): its name and date, the figures, then each part
/// with the exercises done in it. Everything is decided in the shared `SessionRecap`; this only draws it.
/// A session with muscles worked puts this in a sheet over the body map (`SessionRecapSheetView`); without, it is the page.
struct SessionRecapView: View {
    let recap: SessionRecap
    let dateLine: String

    var body: some View {
        ScrollView {
            RecapDetails(recap: recap, dateLine: dateLine)
                .padding(.horizontal, PoulpySpace.lg)
                .padding(.vertical, PoulpySpace.md)
        }
        .background(PoulpyColor.background)
    }
}

/// The muscles worked on a body map, and over it the sheet you drag up for the session's figures and exercises.
struct SessionRecapSheetView: View {
    let recap: SessionRecap
    let dateLine: String
    let figure: BodyFigure
    let onBack: () -> Void
    let onShare: () -> Void
    let onDelete: () -> Void

    var body: some View {
        DetailSheet(
            showBackgroundLabel: "workout.showMap",
            showDetailsLabel: "workout.showExercises",
            background: { room, safeTop in
                HeatMapPanel(
                    figure: figure, heat: recap.muscles.heat, hurting: [],
                    description: L("workout.mapLabel %@", recap.muscles.loads.map(\.label).joined(separator: ", ")),
                    room: room, safeTop: safeTop
                )
            },
            floating: {
                HStack {
                    Button(action: onBack) {
                        LucideIcon(.chevronLeft, size: 20).modifier(FloatingButtonLook())
                    }
                    .accessibilityLabel("action.back")
                    Spacer()
                    HStack(spacing: PoulpySpace.sm) {
                        Button(action: onShare) {
                            LucideIcon(.share2, size: 20).modifier(FloatingButtonLook())
                        }
                        .accessibilityLabel("shareSession.title")
                        Button(action: onDelete) {
                            LucideIcon(.trash, size: 20).modifier(FloatingButtonLook())
                        }
                        .accessibilityLabel("action.delete")
                    }
                }
            },
            content: {
                RecapDetails(recap: recap, dateLine: dateLine)
                    .padding(.bottom, PoulpySpace.lg)
            }
        )
    }
}

/// The name and date, the figures, then each part with the exercises done in it.
private struct RecapDetails: View {
    let recap: SessionRecap
    let dateLine: String

    var body: some View {
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
                ForEach(recap.parts, id: \.key) { part in
                    PartBlockView(kind: part.kind, name: part.name, summary: part.summary, time: part.time) {
                        ForEach(Array(part.rows.enumerated()), id: \.element.key) { index, row in
                            if index > 0 { Divider().overlay(PoulpyColor.outline) }
                            recapRow(row)
                        }
                    }
                }
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
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

/// A recap row: the exercise, what was done and the time it took.
private func recapRow(_ row: RecapRow) -> some View {
    HStack(spacing: 10) {
        PatternBadge(pattern: row.pattern, size: 32)
        VStack(alignment: .leading, spacing: 0) {
            Text(row.name).font(.poulpy(.subheadline).weight(.heavy))
            if !row.detail.isEmpty {
                Text(row.detail).font(.poulpy(.footnote)).foregroundStyle(PoulpyColor.onSurfaceVariant)
            }
        }
        Spacer(minLength: 0)
        if !row.time.isEmpty { PartTimeLabel(time: row.time) }
    }
    .padding(.vertical, 6)
    .accessibilityElement(children: .combine)
}

private func L(_ key: String, _ args: CVarArg...) -> String {
    String(format: NSLocalizedString(key, comment: ""), locale: Locale.current, arguments: args)
}
