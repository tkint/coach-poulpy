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
                        Image(systemName: "chevron.left").font(.system(size: 17, weight: .bold)).modifier(FloatingButtonLook())
                    }
                    .accessibilityLabel("action.back")
                    Spacer()
                    HStack(spacing: PoulpySpace.sm) {
                        Button(action: onShare) {
                            Image(systemName: "square.and.arrow.up").font(.system(size: 17, weight: .bold)).modifier(FloatingButtonLook())
                        }
                        .accessibilityLabel("shareSession.title")
                        Button(action: onDelete) {
                            Image(systemName: "trash").font(.system(size: 17, weight: .bold)).modifier(FloatingButtonLook())
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
                ForEach(recap.parts, id: \.key) { part in PartBlockView(part: part) }
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

/// A part as an outlined block, its icon and name sitting on the top border's left and its time on the right.
/// The border breaks around both, so neither needs a background of its own.
private struct PartBlockView: View {
    let part: RecapPart
    @State private var headerWidth: CGFloat = 0
    @State private var timeWidth: CGFloat = 0

    var body: some View {
        let gap = PoulpySpace.sm
        return VStack(spacing: 0) {
            ForEach(Array(part.rows.enumerated()), id: \.element.key) { index, row in
                if index > 0 { Divider().overlay(PoulpyColor.outline) }
                rowView(row)
            }
        }
        .padding(.horizontal, 10)
        .padding(.top, PoulpySpace.md)
        .overlay(
            GappedOutline(gap: gap, headerWidth: headerWidth, timeWidth: part.time.isEmpty ? 0 : timeWidth, radius: PoulpyRadius.sm)
                .stroke(PoulpyColor.outline, lineWidth: 1)
        )
        .overlay(alignment: .topLeading) {
            partHeader(part.kind)
                .background(GeometryReader { proxy in Color.clear.preference(key: HeaderWidthKey.self, value: proxy.size.width) })
                .offset(x: gap, y: -13)
        }
        .overlay(alignment: .topTrailing) {
            if !part.time.isEmpty {
                timeLabel(part.time)
                    .padding(.horizontal, 6)
                    .background(GeometryReader { proxy in Color.clear.preference(key: TimeWidthKey.self, value: proxy.size.width) })
                    .offset(x: -gap, y: -9)
            }
        }
        .onPreferenceChange(HeaderWidthKey.self) { headerWidth = $0 }
        .onPreferenceChange(TimeWidthKey.self) { timeWidth = $0 }
        .padding(.top, 13)
    }

    private func timeLabel(_ time: String) -> some View {
        Text(time).font(.poulpy(.caption).weight(.heavy)).monospacedDigit().foregroundStyle(PoulpyColor.onSurfaceVariant).lineLimit(1)
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
            if !row.time.isEmpty { timeLabel(row.time) }
        }
        .padding(.vertical, 6)
        .accessibilityElement(children: .combine)
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

private func L(_ key: String, _ args: CVarArg...) -> String {
    String(format: NSLocalizedString(key, comment: ""), locale: Locale.current, arguments: args)
}
