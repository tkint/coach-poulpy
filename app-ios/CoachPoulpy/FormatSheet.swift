import SwiftUI
import Shared

/// The format, Circuit or Sets, each card with what it fits and an example. A tap on a card is the choice:
/// no card is selected when a workout is created (`current` nil), and from the builder `current` is the one
/// it has. The ✕ and every other way of closing leave things as they were.
struct FormatSheet: View {
    let options: [WorkoutFormatOption]
    let current: WorkoutFormat?
    let onChoose: (WorkoutFormat) -> Void
    let onClose: () -> Void

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 12) {
                HStack(alignment: .top) {
                    SheetTitle(title: Text("builder.formatTitle"), subtitle: Text("builder.formatSubtitle"))
                    Spacer(minLength: 0)
                    Button(action: onClose) {
                        LucideIcon(.x, size: 20)
                            .foregroundStyle(PoulpyColor.onSurface)
                            .frame(width: 44, height: 44)
                    }
                    .accessibilityLabel("action.close")
                }
                ForEach(options, id: \.label) { option in
                    card(option)
                }
            }
            .padding(20)
        }
        .background(PoulpyColor.surface)
        .presentationDetents([.large])
    }

    private func card(_ option: WorkoutFormatOption) -> some View {
        let selected = current == option.format
        let circuit = option.format == .circuit
        return Button { onChoose(option.format) } label: {
            VStack(alignment: .leading, spacing: 6) {
                HStack(spacing: 10) {
                    LucideIcon(formatIcon(option.format), size: 20)
                        .foregroundStyle(selected ? PoulpyColor.onPrimary : PoulpyColor.onPrimaryContainer)
                        .frame(width: 36, height: 36)
                        .background(selected ? PoulpyColor.primary : PoulpyColor.primaryContainer, in: Circle())
                    Text(option.label).font(.custom(PoulpyFontFace.baloo2Bold, size: 19, relativeTo: .headline))
                }
                Text(LocalizedStringKey(circuit ? "builder.circuitDescription" : "builder.setsDescription"))
                    .font(.poulpy(.subheadline))
                    .fixedSize(horizontal: false, vertical: true)
                Text(LocalizedStringKey(circuit ? "builder.circuitExample" : "builder.setsExample"))
                    .font(.poulpy(.caption))
                    .foregroundStyle(PoulpyColor.onSurfaceVariant)
            }
            .foregroundStyle(PoulpyColor.onSurface)
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(14)
            .background(selected ? PoulpyColor.primaryContainer : PoulpyColor.surface, in: RoundedRectangle(cornerRadius: PoulpyRadius.lg))
            .overlay(RoundedRectangle(cornerRadius: PoulpyRadius.lg).stroke(selected ? PoulpyColor.primary : PoulpyColor.outline, lineWidth: selected ? 2 : 1))
        }
        .buttonStyle(.plain)
        .accessibilityAddTraits(selected ? .isSelected : [])
    }
}


/// After picking the other format in the builder: "Follow the new format" reorders the exercises the way it
/// does them, "Keep the order" only rewrites the steps. Each says what it gives for this workout. Cancel, or
/// closing the sheet, changes nothing.
struct FormatOrderSheet: View {
    let change: FormatChange
    let onChoose: (FormatSwitch) -> Void
    let onClose: () -> Void

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 12) {
                SheetTitle(title: Text("builder.orderTitle \(change.formatLabel)"), subtitle: Text("builder.orderSubtitle"))
                card("builder.followTitle", description: change.format == .circuit ? "builder.followToCircuit" : "builder.followToSets", result: change.followLine) {
                    onChoose(.followNewFormat)
                }
                card("builder.keepTitle", description: "builder.keepDescription", result: change.keepLine) {
                    onChoose(.keepOrder)
                }
                Button(action: onClose) {
                    Text("action.cancel")
                        .frame(maxWidth: .infinity)
                }
                .buttonStyle(PoulpyButtonStyle(.secondary, size: .lg))
            }
            .padding(20)
        }
        .background(PoulpyColor.surface)
        .presentationDetents([.large])
    }

    private func card(_ title: LocalizedStringKey, description: LocalizedStringKey, result: String, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            VStack(alignment: .leading, spacing: 6) {
                Text(title).font(.custom(PoulpyFontFace.baloo2Bold, size: 19, relativeTo: .headline))
                Text(description).font(.poulpy(.subheadline)).fixedSize(horizontal: false, vertical: true)
                Text(result).font(.poulpy(.caption)).foregroundStyle(PoulpyColor.onSurfaceVariant)
            }
            .foregroundStyle(PoulpyColor.onSurface)
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(14)
            .background(PoulpyColor.surface, in: RoundedRectangle(cornerRadius: PoulpyRadius.lg))
            .overlay(RoundedRectangle(cornerRadius: PoulpyRadius.lg).stroke(PoulpyColor.outline, lineWidth: 1))
        }
        .buttonStyle(.plain)
    }
}

extension FormatChange: Identifiable {
    public var id: String { format.name }
}
