import SwiftUI
import Shared

/// An SVG path made only of M, C and Z, as `design/body-map.json` draws every area, scaled to fill its frame.
private struct BodyShape: Shape {
    let source: String

    func path(in rect: CGRect) -> Path {
        let scale = rect.width / CGFloat(BodyMapBox.shared.WIDTH)
        var path = Path()
        var numbers: [CGFloat] = []
        var command: Character = "M"
        var token = ""
        func flush() {
            if !token.isEmpty, let value = Double(token) { numbers.append(CGFloat(value)) }
            token = ""
        }
        func apply() {
            let point = { (i: Int) in CGPoint(x: numbers[i] * scale + rect.minX, y: numbers[i + 1] * scale + rect.minY) }
            switch command {
            case "M" where numbers.count >= 2: path.move(to: point(0))
            case "C" where numbers.count >= 6: path.addCurve(to: point(4), control1: point(0), control2: point(2))
            default: break
            }
            numbers = []
        }
        for character in source {
            if character.isLetter {
                flush()
                apply()
                command = character
                if character == "Z" { path.closeSubpath() }
            } else if character == " " || character == "," {
                flush()
            } else if character == "-" && !token.isEmpty {
                flush()
                token = "-"
            } else {
                token.append(character)
            }
        }
        flush()
        apply()
        return path
    }
}

/// One side of the body as a muscle chart: each area is a shape, filled with the primary colour when it is in `selected`.
/// Skin has no fill. With `onToggle` each area can be tapped and is its own accessibility element ("Left knee").
struct BodyMapFigure: View {
    let figure: BodyFigure
    let view: Shared.BodyView
    var selected: Set<String> = []
    var width: CGFloat = 140
    var onToggle: ((String) -> Void)?

    var body: some View {
        let regions = BodyMapShapes.shared.regions(figure: figure, view: view)
        let height = width * CGFloat(BodyMapBox.shared.HEIGHT / BodyMapBox.shared.WIDTH)
        ZStack {
            ForEach(Array(regions.enumerated()), id: \.offset) { _, region in
                let on = region.key.map { selected.contains($0) } ?? false
                let shape = BodyShape(source: region.path)
                let drawn = shape
                    .fill(on ? PoulpyColor.primary : (region.skin ? Color.clear : PoulpyColor.surfaceVariant))
                    .overlay(shape.stroke(on ? PoulpyColor.primary : PoulpyColor.outline, lineWidth: 1.2 * width / 120))
                if let key = region.key, let onToggle {
                    drawn
                        .contentShape(shape)
                        .onTapGesture { onToggle(key) }
                        .accessibilityElement()
                        .accessibilityLabel(BodyMap.shared.label(key: key, languageCode: Language.code))
                        .accessibilityAddTraits(on ? [.isButton, .isSelected] : .isButton)
                } else {
                    drawn.allowsHitTesting(false).accessibilityHidden(true)
                }
            }
        }
        .frame(width: width, height: height)
    }
}

/// The fill of a muscle at `level` 1 to 3: a little, some, most. Nothing for level 0, a plain outline.
func heatFill(_ level: Int) -> Color? {
    switch level {
    case 1: return PoulpyColor.heat1
    case 2: return PoulpyColor.heat2
    case 3: return PoulpyColor.heat3
    default: return nil
    }
}

/// The edge of a muscle at `level`, one step darker than its fill.
private func heatEdge(_ level: Int) -> Color? {
    switch level {
    case 1: return PoulpyColor.heat2
    case 2, 3: return PoulpyColor.heat3
    default: return nil
    }
}

/// One side of the body with the muscles worked lit from light to dark (`heat`, 1 to 3 per area key). Areas in `hurting`
/// keep their heat and get the purple pain outline, with a halo so it reads on any fill. Only a picture.
struct HeatMapFigure: View {
    let figure: BodyFigure
    let view: Shared.BodyView
    let heat: [String: KotlinInt]
    let hurting: Set<String>
    var width: CGFloat = 100

    var body: some View {
        let regions = BodyMapShapes.shared.regions(figure: figure, view: view)
        let height = width * CGFloat(BodyMapBox.shared.HEIGHT / BodyMapBox.shared.WIDTH)
        let unit = width / 120
        ZStack {
            ForEach(Array(regions.enumerated()), id: \.offset) { _, region in
                let level = region.key.flatMap { heat[$0] }.map { Int($0.intValue) } ?? 0
                let shape = BodyShape(source: region.path)
                shape
                    .fill(region.skin ? Color.clear : (heatFill(level) ?? Color.clear))
                    .overlay(shape.stroke(heatEdge(level) ?? PoulpyColor.outline, lineWidth: 1.2 * unit))
            }
            ForEach(Array(regions.enumerated()), id: \.offset) { _, region in
                if let key = region.key, hurting.contains(key) {
                    let shape = BodyShape(source: region.path)
                    shape.stroke(PoulpyColor.background, style: StrokeStyle(lineWidth: 5 * unit, lineJoin: .round))
                    shape.stroke(PoulpyColor.pain, style: StrokeStyle(lineWidth: 3 * unit, lineJoin: .round))
                }
            }
        }
        .frame(width: width, height: height)
        .allowsHitTesting(false)
        .accessibilityHidden(true)
    }
}

/// "Less ▢ ▢ ▢ More", the three heats; with `showPain` also the outlined "Hurts" swatch.
/// The map behind a `DetailSheet`: a legend, then the front and back figures, as tall as the `room` above the sheet allows.
/// Collapsed, it fills the room: as wide as two figures side by side fit the screen. Shared by the workout page and the session recap.
struct HeatMapPanel: View {
    let figure: BodyFigure
    let heat: [String: KotlinInt]
    let hurting: Set<String>
    let description: String
    let room: CGFloat
    let safeTop: CGFloat

    var body: some View {
        let available = room - safeTop - 48 - PoulpySpace.md
        let maxFigure = (UIScreen.main.bounds.width - PoulpySpace.lg * 2 - PoulpySpace.sm) / 2
        let width = min(max(available / CGFloat(BodyMapBox.shared.HEIGHT / BodyMapBox.shared.WIDTH), 56), maxFigure)
        VStack(spacing: PoulpySpace.lg) {
            HeatLegend(showPain: !hurting.isEmpty)
            HStack(spacing: PoulpySpace.sm) {
                HeatMapFigure(figure: figure, view: .front, heat: heat, hurting: hurting, width: width)
                HeatMapFigure(figure: figure, view: .back, heat: heat, hurting: hurting, width: width)
            }
        }
        .accessibilityElement(children: .ignore)
        .accessibilityLabel(description)
        .frame(maxWidth: .infinity)
        .padding(.top, safeTop + PoulpySpace.lg)
    }
}

struct HeatLegend: View {
    let showPain: Bool

    var body: some View {
        HStack(spacing: 6) {
            Text("workout.less")
            ForEach(1...3, id: \.self) { swatch(fill: heatFill($0) ?? .clear, edge: heatEdge($0) ?? PoulpyColor.outline, line: 1.2) }
            Text("workout.more")
            if showPain {
                swatch(fill: .clear, edge: PoulpyColor.pain, line: 2)
                Text("workout.hurts")
            }
        }
        .font(.poulpy(.caption).weight(.heavy))
        .foregroundStyle(PoulpyColor.onSurfaceVariant)
        .accessibilityHidden(true)
    }

    private func swatch(fill: Color, edge: Color, line: CGFloat) -> some View {
        RoundedRectangle(cornerRadius: 3)
            .fill(fill)
            .overlay(RoundedRectangle(cornerRadius: 3).stroke(edge, lineWidth: line))
            .frame(width: 12, height: 12)
            .padding(1)
    }
}

/// Pain or rehab: where it hurts on the front and back figures, then for how long.
struct PainSheet: View {
    let figure: BodyFigure
    let areas: [String]
    let duration: PainDuration?
    let onSave: ([String], PainDuration) -> Void
    let onClear: () -> Void
    @State private var picked: Set<String>
    @State private var length: PainDuration
    @State private var side: Shared.BodyView = .front
    @Environment(\.dismiss) private var dismiss

    init(figure: BodyFigure, areas: [String], duration: PainDuration?, onSave: @escaping ([String], PainDuration) -> Void, onClear: @escaping () -> Void) {
        self.figure = figure
        self.areas = areas
        self.duration = duration
        self.onSave = onSave
        self.onClear = onClear
        _picked = State(initialValue: Set(areas))
        _length = State(initialValue: duration ?? .hours72)
    }

    private func toggle(_ key: String) {
        if picked.contains(key) { picked.remove(key) } else { picked.insert(key) }
    }

    private var summaryLine: some View {
        let names: [String] = BodyMap.shared.valid(keys: Array(picked)).map { BodyMap.shared.label(key: $0, languageCode: Language.code) }
        let summary: String = names.isEmpty ? L("profile.painNothingPicked") : names.joined(separator: ", ")
        return Text(summary).font(.poulpy(.body).weight(.heavy)).foregroundStyle(PoulpyColor.onPrimaryContainer)
    }

    private var otherChip: some View {
        let key: String = BodyMap.shared.OTHER
        let on: Bool = picked.contains(key)
        return Button { toggle(key) } label: {
            Text(BodyMap.shared.label(key: key, languageCode: Language.code))
                .font(.poulpy(.subheadline).weight(.heavy))
                .foregroundStyle(on ? PoulpyColor.onPrimary : PoulpyColor.onSurface)
                .padding(.horizontal, 16)
                .frame(minHeight: 40)
                .background(on ? PoulpyColor.primary : Color.clear, in: Capsule())
                .overlay(Capsule().stroke(PoulpyColor.outline, lineWidth: 1.5))
        }
        .buttonStyle(.plain)
        .accessibilityAddTraits(on ? .isSelected : [])
    }

    private var durationList: some View {
        let choices: [(PainDuration, LocalizedStringKey, LocalizedStringKey?)] = [
            (.hours72, "profile.painDuration72", "profile.painDuration72Sub"),
            (.oneWeek, "profile.painDurationWeek", nil),
            (.twoWeeks, "profile.painDurationWeeks", nil),
            (.untilCleared, "profile.painDurationCleared", "profile.painDurationClearedSub"),
        ]
        return VStack(spacing: 0) {
            ForEach(Array(choices.enumerated()), id: \.offset) { index, choice in
                if index > 0 { Divider().overlay(PoulpyColor.outline) }
                durationRow(choice.0, choice.1, choice.2)
            }
        }
        .overlay(RoundedRectangle(cornerRadius: PoulpyRadius.lg).stroke(PoulpyColor.outline, lineWidth: 1))
    }

    private func durationRow(_ value: PainDuration, _ title: LocalizedStringKey, _ sub: LocalizedStringKey?) -> some View {
        Button { length = value } label: {
            HStack {
                VStack(alignment: .leading, spacing: 2) {
                    Text(title).font(.poulpy(.body).weight(.bold)).foregroundStyle(PoulpyColor.onSurface)
                    if let sub { Text(sub).font(.poulpy(.footnote)).foregroundStyle(PoulpyColor.onSurfaceVariant) }
                }
                Spacer()
                if length == value { Image(systemName: "checkmark").foregroundStyle(PoulpyColor.primary) }
            }
            .frame(minHeight: PoulpySize.row)
            .padding(.horizontal, 16)
            .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
        .accessibilityAddTraits(length == value ? .isSelected : [])
    }

    private var sideSwitch: some View {
        Picker("profile.painWhere", selection: $side) {
            Text("profile.figureFront").tag(Shared.BodyView.front)
            Text("profile.figureBack").tag(Shared.BodyView.back)
        }
        .pickerStyle(.segmented)
    }

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 12) {
                    Text("profile.painWhy").font(.poulpy(.subheadline)).foregroundStyle(PoulpyColor.onSurfaceVariant)
                    Text("profile.painWhere").font(.poulpy(.footnote).weight(.heavy)).foregroundStyle(PoulpyColor.onSurfaceVariant)
                    FieldNote("profile.painHint")
                    sideSwitch
                    BodyMapFigure(figure: figure, view: side, selected: picked, width: 220, onToggle: toggle)
                        .frame(maxWidth: .infinity)
                    summaryLine
                    otherChip
                    Text("profile.painFor").font(.poulpy(.footnote).weight(.heavy)).foregroundStyle(PoulpyColor.onSurfaceVariant)
                    durationList
                    FieldNote("profile.painAgain")
                    if !areas.isEmpty {
                        Button(LocalizedStringKey("profile.clear")) {
                            onClear()
                            dismiss()
                        }
                        .font(.poulpy(.subheadline).weight(.heavy))
                    }
                }
                .padding(20)
            }
            .navigationTitle("profile.pain")
            .navigationBarTitleDisplayMode(.inline)
            .safeAreaInset(edge: .bottom) {
                SheetActions { onSave(BodyMap.shared.valid(keys: Array(picked)), length) }
            }
        }
        .presentationDetents([.large])
    }
}

/// The body figure: two builds side by side, drawn as the map will look. Only a drawing, so it says nothing about sex or skin.
struct BodyFigureSheet: View {
    let current: BodyFigure?
    let onSave: (BodyFigure?) -> Void
    @State private var choice: BodyFigure?
    @Environment(\.dismiss) private var dismiss

    init(current: BodyFigure?, onSave: @escaping (BodyFigure?) -> Void) {
        self.current = current
        self.onSave = onSave
        _choice = State(initialValue: current)
    }

    private func card(_ value: BodyFigure, _ label: LocalizedStringKey) -> some View {
        let on = choice == value
        return Button { choice = value } label: {
            VStack(spacing: 8) {
                BodyMapFigure(figure: value, view: Shared.BodyView.front, width: 96)
                Text(label).font(.poulpy(.body).weight(.heavy)).foregroundStyle(PoulpyColor.onSurface)
            }
            .frame(maxWidth: .infinity)
            .padding(.vertical, 12)
            .overlay(RoundedRectangle(cornerRadius: PoulpyRadius.lg).stroke(on ? PoulpyColor.primary : PoulpyColor.outline, lineWidth: on ? 2.5 : 1.5))
        }
        .buttonStyle(.plain)
        .accessibilityAddTraits(on ? .isSelected : [])
    }

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 12) {
                    Text("profile.figureWhy").font(.poulpy(.subheadline)).foregroundStyle(PoulpyColor.onSurfaceVariant)
                    HStack(spacing: 12) {
                        card(.hips, "profile.figureHips")
                        card(.shoulders, "profile.figureShoulders")
                    }
                    if current != nil {
                        Button(LocalizedStringKey("profile.clear")) {
                            onSave(nil)
                            dismiss()
                        }
                        .font(.poulpy(.subheadline).weight(.heavy))
                    }
                }
                .padding(20)
            }
            .navigationTitle("profile.bodyFigure")
            .navigationBarTitleDisplayMode(.inline)
            .safeAreaInset(edge: .bottom) { SheetActions { onSave(choice) } }
        }
        .presentationDetents([.large])
    }
}

private func L(_ key: String, _ args: CVarArg...) -> String {
    String(format: NSLocalizedString(key, comment: ""), locale: Locale.current, arguments: args)
}
