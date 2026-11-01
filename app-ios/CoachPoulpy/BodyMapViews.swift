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
    let view: BodyView
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

/// Pain or rehab: where it hurts on the front and back figures, then for how long.
struct PainSheet: View {
    let figure: BodyFigure
    let areas: [String]
    let duration: PainDuration?
    let onSave: ([String], PainDuration) -> Void
    let onClear: () -> Void
    @State private var picked: Set<String>
    @State private var length: PainDuration
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

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 12) {
                    Text("profile.painWhy").font(.poulpy(.subheadline)).foregroundStyle(PoulpyColor.onSurfaceVariant)
                    Text("profile.painWhere").font(.poulpy(.footnote).weight(.heavy)).foregroundStyle(PoulpyColor.onSurfaceVariant)
                    FieldNote("profile.painHint")
                    HStack(alignment: .top, spacing: 0) {
                        ForEach(Array([(BodyView.front, "profile.figureFront"), (BodyView.back, "profile.figureBack")].enumerated()), id: \.offset) { _, side in
                            let (view, label) = side
                            VStack(spacing: 4) {
                                BodyMapFigure(figure: figure, view: view, selected: picked, width: 128, onToggle: toggle)
                                Text(LocalizedStringKey(label)).font(.poulpy(.footnote)).foregroundStyle(PoulpyColor.onSurfaceVariant)
                            }
                            .frame(maxWidth: .infinity)
                        }
                    }
                    let summary = BodyMap.shared.valid(keys: Array(picked)).map { BodyMap.shared.label(key: $0, languageCode: Language.code) }.joined(separator: ", ")
                    Text(summary.isEmpty ? L("profile.painNothingPicked") : summary)
                        .font(.poulpy(.body).weight(.heavy)).foregroundStyle(PoulpyColor.onPrimaryContainer)
                    Button { toggle(BodyMap.shared.OTHER) } label: {
                        Text(BodyMap.shared.label(key: BodyMap.shared.OTHER, languageCode: Language.code))
                            .font(.poulpy(.subheadline).weight(.heavy))
                            .foregroundStyle(picked.contains(BodyMap.shared.OTHER) ? PoulpyColor.onPrimary : PoulpyColor.onSurface)
                            .padding(.horizontal, 16).frame(minHeight: 40)
                            .background(picked.contains(BodyMap.shared.OTHER) ? PoulpyColor.primary : Color.clear, in: Capsule())
                            .overlay(Capsule().stroke(PoulpyColor.outline, lineWidth: 1.5))
                    }
                    .buttonStyle(.plain)
                    .accessibilityAddTraits(picked.contains(BodyMap.shared.OTHER) ? .isSelected : [])
                    Text("profile.painFor").font(.poulpy(.footnote).weight(.heavy)).foregroundStyle(PoulpyColor.onSurfaceVariant)
                    VStack(spacing: 0) {
                        let choices: [(PainDuration, String, String?)] = [
                            (.hours72, "profile.painDuration72", "profile.painDuration72Sub"),
                            (.oneWeek, "profile.painDurationWeek", nil),
                            (.twoWeeks, "profile.painDurationWeeks", nil),
                            (.untilCleared, "profile.painDurationCleared", "profile.painDurationClearedSub"),
                        ]
                        ForEach(Array(choices.enumerated()), id: \.offset) { index, choice in
                            if index > 0 { Divider().overlay(PoulpyColor.outline) }
                            Button { length = choice.0 } label: {
                                HStack {
                                    VStack(alignment: .leading, spacing: 2) {
                                        Text(LocalizedStringKey(choice.1)).font(.poulpy(.body).weight(.bold)).foregroundStyle(PoulpyColor.onSurface)
                                        if let sub = choice.2 { Text(LocalizedStringKey(sub)).font(.poulpy(.footnote)).foregroundStyle(PoulpyColor.onSurfaceVariant) }
                                    }
                                    Spacer()
                                    if length == choice.0 { Image(systemName: "checkmark").foregroundStyle(PoulpyColor.primary) }
                                }
                                .frame(minHeight: 52)
                                .padding(.horizontal, 16)
                                .contentShape(Rectangle())
                            }
                            .buttonStyle(.plain)
                            .accessibilityAddTraits(length == choice.0 ? .isSelected : [])
                        }
                    }
                    .overlay(RoundedRectangle(cornerRadius: 14).stroke(PoulpyColor.outline, lineWidth: 1))
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

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 12) {
                    Text("profile.figureWhy").font(.poulpy(.subheadline)).foregroundStyle(PoulpyColor.onSurfaceVariant)
                    HStack(spacing: 12) {
                        ForEach(Array([(BodyFigure.hips, "profile.figureHips"), (BodyFigure.shoulders, "profile.figureShoulders")].enumerated()), id: \.offset) { _, option in
                            let (value, label) = option
                            let on = choice == value
                            Button { choice = value } label: {
                                VStack(spacing: 8) {
                                    BodyMapFigure(figure: value, view: .front, width: 96)
                                    Text(LocalizedStringKey(label)).font(.poulpy(.body).weight(.heavy)).foregroundStyle(PoulpyColor.onSurface)
                                }
                                .frame(maxWidth: .infinity)
                                .padding(.vertical, 12)
                                .overlay(RoundedRectangle(cornerRadius: 20).stroke(on ? PoulpyColor.primary : PoulpyColor.outline, lineWidth: on ? 2.5 : 1.5))
                            }
                            .buttonStyle(.plain)
                            .accessibilityAddTraits(on ? .isSelected : [])
                        }
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
