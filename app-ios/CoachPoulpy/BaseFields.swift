import SwiftUI
import UIKit

// The base fields every form is built from (docs/design-system.md "Base fields"):
// a text field, a select field that opens a sheet, a number field that selects its
// text and opens the numeric keyboard, and option sheets (one choice, several, or a
// step slider). Mockups: profile-edit, profile-*-sheet, body-log-sheet.

/// The box shared by the text, number and select fields: 16 radius, a 1.5 outline that turns primary on focus.
struct FieldBox<Content: View>: View {
    var focused = false
    var height: CGFloat = 52
    @ViewBuilder var content: () -> Content

    var body: some View {
        content()
            .padding(.horizontal, 16)
            .frame(maxWidth: .infinity, minHeight: height, alignment: .leading)
            .background(PoulpyColor.surface, in: RoundedRectangle(cornerRadius: 16))
            .overlay(RoundedRectangle(cornerRadius: 16).stroke(focused ? PoulpyColor.primary : PoulpyColor.outline, lineWidth: 1.5))
    }
}

/// A label above, the note under it.
private struct Labelled<Content: View>: View {
    let label: LocalizedStringKey
    let note: LocalizedStringKey?
    @ViewBuilder var content: () -> Content

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(label).font(.poulpy(.footnote)).foregroundStyle(PoulpyColor.onSurfaceVariant).padding(.horizontal, 4)
            content()
            if let note { FieldNote(note) }
        }
    }
}

struct FieldNote: View {
    let key: LocalizedStringKey
    init(_ key: LocalizedStringKey) { self.key = key }

    var body: some View {
        Text(key).font(.poulpy(.footnote)).foregroundStyle(PoulpyColor.onSurfaceVariant).padding(.horizontal, 4)
    }
}

/// A labelled free-text field, with an optional note under it.
struct PoulpyTextField: View {
    let label: LocalizedStringKey
    @Binding var text: String
    var note: LocalizedStringKey? = nil
    @FocusState private var focused: Bool

    var body: some View {
        Labelled(label: label, note: note) {
            FieldBox(focused: focused) {
                TextField("", text: $text)
                    .font(.poulpy(.headline))
                    .focused($focused)
                    .accessibilityLabel(Text(label))
            }
        }
    }
}

/// A number typed with the numeric keyboard. A tap selects what is there, so typing replaces it.
/// `text` is what is typed; the caller parses it (see `BodyDisplay`) and decides what is valid.
struct NumberField: View {
    @Binding var text: String
    let description: LocalizedStringKey
    var unit: String? = nil
    var decimal = false
    var large = false
    @State private var focused = false

    var body: some View {
        FieldBox(focused: focused, height: large ? 64 : 52) {
            HStack(spacing: 8) {
                SelectingTextField(text: $text, keyboard: decimal ? .decimalPad : .numberPad, decimal: decimal, large: large, focused: $focused)
                    .accessibilityLabel(Text(description))
                if let unit {
                    Text(verbatim: unit).font(.poulpy(.headline)).foregroundStyle(PoulpyColor.onSurfaceVariant)
                }
            }
        }
    }
}

/// A `UITextField` that selects all its text as editing begins and only lets numbers through.
private struct SelectingTextField: UIViewRepresentable {
    @Binding var text: String
    let keyboard: UIKeyboardType
    let decimal: Bool
    let large: Bool
    @Binding var focused: Bool

    func makeUIView(context: Context) -> UITextField {
        let field = UITextField()
        field.delegate = context.coordinator
        field.keyboardType = keyboard
        field.font = UIFont(name: PoulpyFontFace.nunitoExtraBold, size: large ? 28 : 17) ?? .boldSystemFont(ofSize: large ? 28 : 17)
        field.textColor = UIColor(PoulpyColor.onSurface)
        field.tintColor = UIColor(PoulpyColor.primary)
        field.textAlignment = large ? .right : .left
        field.adjustsFontForContentSizeCategory = true
        field.setContentHuggingPriority(.defaultLow, for: .horizontal)
        field.addTarget(context.coordinator, action: #selector(Coordinator.changed(_:)), for: .editingChanged)
        return field
    }

    func updateUIView(_ field: UITextField, context: Context) {
        context.coordinator.parent = self
        if field.text != text { field.text = text }
    }

    func makeCoordinator() -> Coordinator { Coordinator(self) }

    final class Coordinator: NSObject, UITextFieldDelegate {
        var parent: SelectingTextField
        init(_ parent: SelectingTextField) { self.parent = parent }

        @objc func changed(_ field: UITextField) {
            parent.text = field.text ?? ""
        }

        func textFieldDidBeginEditing(_ field: UITextField) {
            parent.focused = true
            DispatchQueue.main.async { field.selectAll(nil) }
        }

        func textFieldDidEndEditing(_ field: UITextField) {
            parent.focused = false
        }

        func textField(_ field: UITextField, shouldChangeCharactersIn range: NSRange, replacementString string: String) -> Bool {
            let allowed = parent.decimal ? "0123456789.," : "0123456789"
            return string.allSatisfy { allowed.contains($0) }
        }
    }
}

/// Round − and + on each side of a field, for small nudges after typing.
struct NumberEntry<Field: View>: View {
    let onLess: () -> Void
    let onMore: () -> Void
    @ViewBuilder var field: () -> Field

    var body: some View {
        HStack(spacing: 8) {
            nudge("−", "body.less", onLess)
            field()
            nudge("+", "body.more", onMore)
        }
    }

    private func nudge(_ symbol: String, _ label: LocalizedStringKey, _ action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Text(verbatim: symbol)
                .font(.poulpy(.title2))
                .foregroundStyle(PoulpyColor.primary)
                .frame(width: 48, height: 48)
                .background(PoulpyColor.surfaceVariant, in: Circle())
        }
        .buttonStyle(.borderless)
        .accessibilityLabel(Text(label))
    }
}

/// A labelled value that opens a sheet when pressed.
struct SelectField: View {
    let label: LocalizedStringKey
    let value: String
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            FieldBox(height: 56) {
                HStack {
                    VStack(alignment: .leading, spacing: 2) {
                        Text(label).font(.poulpy(.caption)).foregroundStyle(PoulpyColor.onSurfaceVariant)
                        Text(value).font(.poulpy(.headline)).foregroundStyle(PoulpyColor.onSurface).lineLimit(1)
                    }
                    Spacer()
                    Image(systemName: "chevron.right").font(.footnote).foregroundStyle(PoulpyColor.onSurfaceVariant)
                }
            }
        }
        .buttonStyle(.plain)
    }
}

// MARK: - Option sheets

/// The toolbar of every option sheet: Cancel and Save.
private struct SheetBar: ViewModifier {
    let title: LocalizedStringKey
    let onSave: () -> Void
    @Environment(\.dismiss) private var dismiss

    func body(content: Content) -> some View {
        NavigationStack {
            content
                .navigationTitle(title)
                .navigationBarTitleDisplayMode(.inline)
                .toolbar {
                    ToolbarItem(placement: .cancellationAction) {
                        Button(LocalizedStringKey("action.cancel")) { dismiss() }
                    }
                    ToolbarItem(placement: .confirmationAction) {
                        Button(LocalizedStringKey("action.save")) {
                            onSave()
                            dismiss()
                        }
                    }
                }
        }
    }
}

private extension View {
    func sheetBar(_ title: LocalizedStringKey, onSave: @escaping () -> Void) -> some View {
        modifier(SheetBar(title: title, onSave: onSave))
    }
}

/// Pick one option and save it. A nil value is "not set". `extra` is shown under the choices with the current one,
/// for a choice that needs more (the custom pronouns field).
struct OptionSheet<T: Hashable, Extra: View>: View {
    let title: LocalizedStringKey
    let why: LocalizedStringKey?
    let note: LocalizedStringKey?
    let options: [(T?, String)]
    let onSave: (T?) -> Void
    @ViewBuilder let extra: (T?) -> Extra
    @State private var choice: T?

    init(title: LocalizedStringKey, why: LocalizedStringKey?, note: LocalizedStringKey?, selected: T?, options: [(T?, String)],
         onSave: @escaping (T?) -> Void, @ViewBuilder extra: @escaping (T?) -> Extra) {
        self.title = title
        self.why = why
        self.note = note
        self.options = options
        self.onSave = onSave
        self.extra = extra
        _choice = State(initialValue: selected)
    }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 12) {
                if let why { Text(why).font(.poulpy(.subheadline)).foregroundStyle(PoulpyColor.onSurfaceVariant) }
                VStack(spacing: 0) {
                    ForEach(options.indices, id: \.self) { index in
                        Button {
                            choice = options[index].0
                        } label: {
                            HStack {
                                Text(options[index].1).font(.poulpy(.body)).foregroundStyle(PoulpyColor.onSurface)
                                Spacer()
                                if choice == options[index].0 {
                                    Image(systemName: "checkmark").foregroundStyle(PoulpyColor.primary)
                                }
                            }
                            .frame(minHeight: 44)
                            .contentShape(Rectangle())
                        }
                        .buttonStyle(.plain)
                        .accessibilityAddTraits(choice == options[index].0 ? .isSelected : [])
                    }
                }
                extra(choice)
                if let note { Text(note).font(.poulpy(.footnote)).foregroundStyle(PoulpyColor.onSurfaceVariant) }
            }
            .padding(20)
        }
        .scrollDismissesKeyboard(.interactively)
        .sheetBar(title) { onSave(choice) }
        .presentationDetents([.medium, .large])
    }
}

extension OptionSheet where Extra == EmptyView {
    init(title: LocalizedStringKey, why: LocalizedStringKey?, note: LocalizedStringKey?, selected: T?, options: [(T?, String)], onSave: @escaping (T?) -> Void) {
        self.init(title: title, why: why, note: note, selected: selected, options: options, onSave: onSave) { _ in EmptyView() }
    }
}

/// Pick any number of options (including none) and save them.
struct MultiOptionSheet<T: Hashable>: View {
    let title: LocalizedStringKey
    let why: LocalizedStringKey?
    let options: [(T, String)]
    let onSave: ([T]) -> Void
    @State private var chosen: Set<T>

    init(title: LocalizedStringKey, why: LocalizedStringKey?, selected: [T], options: [(T, String)], onSave: @escaping ([T]) -> Void) {
        self.title = title
        self.why = why
        self.options = options
        self.onSave = onSave
        _chosen = State(initialValue: Set(selected))
    }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 12) {
                if let why { Text(why).font(.poulpy(.subheadline)).foregroundStyle(PoulpyColor.onSurfaceVariant) }
                ForEach(options.indices, id: \.self) { index in
                    let value = options[index].0
                    let on = chosen.contains(value)
                    Button {
                        if on { chosen.remove(value) } else { chosen.insert(value) }
                    } label: {
                        HStack {
                            Text(options[index].1).font(.poulpy(.body)).foregroundStyle(PoulpyColor.onSurface)
                            Spacer()
                            Image(systemName: on ? "checkmark.square.fill" : "square")
                                .foregroundStyle(on ? PoulpyColor.primary : PoulpyColor.onSurfaceVariant)
                        }
                        .frame(minHeight: 44)
                        .contentShape(Rectangle())
                    }
                    .buttonStyle(.plain)
                    .accessibilityAddTraits(on ? .isSelected : [])
                }
            }
            .padding(20)
        }
        .sheetBar(title) { onSave(options.map { $0.0 }.filter { chosen.contains($0) }) }
        .presentationDetents([.medium, .large])
    }
}

/// A short scale of named steps on a slider with a dot per step, the chosen name above it and the two ends named below.
/// Starts on the middle step when nothing was chosen; Clear leaves it unset.
struct ScaleSheet<T: Hashable>: View {
    let title: LocalizedStringKey
    let why: LocalizedStringKey?
    let steps: [(T, String)]
    let onSave: (T?) -> Void
    @State private var index: Double
    @Environment(\.dismiss) private var dismiss

    init(title: LocalizedStringKey, why: LocalizedStringKey?, selected: T?, steps: [(T, String)], onSave: @escaping (T?) -> Void) {
        self.title = title
        self.why = why
        self.steps = steps
        self.onSave = onSave
        _index = State(initialValue: Double(steps.firstIndex { $0.0 == selected } ?? steps.count / 2))
    }

    var body: some View {
        let current = Int(index.rounded())
        VStack(alignment: .leading, spacing: 16) {
            if let why { Text(why).font(.poulpy(.subheadline)).foregroundStyle(PoulpyColor.onSurfaceVariant) }
            Text(steps[current].1)
                .font(PoulpyFont.headline)
                .frame(maxWidth: .infinity)
                .accessibilityHidden(true)
            VStack(spacing: 6) {
                Slider(value: $index, in: 0...Double(steps.count - 1), step: 1)
                    .tint(PoulpyColor.primary)
                    .accessibilityLabel(Text(title))
                    .accessibilityValue(Text(steps[current].1))
                HStack {
                    ForEach(steps.indices, id: \.self) { i in
                        Circle().fill(i == current ? PoulpyColor.primary : PoulpyColor.outline).frame(width: 10, height: 10)
                        if i < steps.count - 1 { Spacer() }
                    }
                }
                .padding(.horizontal, 14)
                .accessibilityHidden(true)
                HStack {
                    Text(steps.first!.1)
                    Spacer()
                    Text(steps.last!.1)
                }
                .font(.poulpy(.footnote).weight(.bold))
                .foregroundStyle(PoulpyColor.onSurfaceVariant)
            }
            Button(LocalizedStringKey("profile.clear")) {
                onSave(nil)
                dismiss()
            }
            .font(.poulpy(.subheadline).weight(.heavy))
            Spacer()
        }
        .padding(20)
        .sheetBar(title) { onSave(steps[current].0) }
        .presentationDetents([.height(340), .large])
    }
}
