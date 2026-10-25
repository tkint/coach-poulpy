import SwiftUI

/// The filter row both Library lists share: the pill search, a row of chips, then the
/// count line with an action at its right ("Clear filters", or the workouts' sort).
struct ListFilters<Chips: View, Action: View>: View {
    @Binding var query: String
    let placeholder: LocalizedStringKey
    let label: LocalizedStringKey
    let countLabel: String
    @ViewBuilder let chips: () -> Chips
    @ViewBuilder let action: () -> Action

    var body: some View {
        VStack(spacing: 0) {
            PillSearchField(query: $query, placeholder: placeholder, label: label)
                .padding(.horizontal, 20)
                .padding(.top, 8)
                .padding(.bottom, 12)
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) { chips() }
                    .padding(.horizontal, 20)
            }
            HStack {
                Text(countLabel)
                    .font(.poulpy(.subheadline).weight(.bold))
                    .foregroundStyle(PoulpyColor.onSurfaceVariant)
                Spacer()
                action()
            }
            .frame(minHeight: 40)
            .padding(.leading, 24)
            .padding(.trailing, 20)
        }
    }
}

/// Poulpy, happy on the blue side, in a `primaryContainer` card: what the lists show
/// when nothing matches or nothing exists yet, with one primary button.
struct PoulpyCard: View {
    let title: Text
    let message: Text
    let button: Text
    var systemImage: String? = nil
    var poulpySize: CGFloat = 96
    let action: () -> Void

    var body: some View {
        VStack(spacing: 8) {
            Image("PoulpyMoodHappy")
                .resizable()
                .scaledToFit()
                .frame(width: poulpySize, height: poulpySize)
                .accessibilityHidden(true)
            title
                .font(PoulpyFont.headline)
                .foregroundStyle(PoulpyColor.onPrimaryContainer)
            message
                .font(.poulpy(.subheadline))
                .foregroundStyle(PoulpyColor.onSurfaceVariant)
            Button(action: action) {
                HStack(spacing: 8) {
                    if let systemImage {
                        Image(systemName: systemImage).font(.system(size: 15, weight: .bold))
                    }
                    button
                }
                .font(.poulpy(.subheadline).weight(.heavy))
                .padding(.horizontal, 22)
                .frame(height: 48)
                .foregroundStyle(PoulpyColor.onPrimary)
                .background(PoulpyColor.primary, in: Capsule())
            }
            .buttonStyle(.plain)
            .padding(.top, 8)
        }
        .multilineTextAlignment(.center)
        .frame(maxWidth: .infinity)
        .padding(.horizontal, 20)
        .padding(.vertical, 24)
        .background(PoulpyColor.primaryContainer, in: RoundedRectangle(cornerRadius: 24))
    }
}

/// The 48 high pill search with a 1.5 px outline, `primary` once something is typed, and a clear button.
struct PillSearchField: View {
    @Binding var query: String
    let placeholder: LocalizedStringKey
    let label: LocalizedStringKey

    var body: some View {
        HStack(spacing: 10) {
            Image(systemName: "magnifyingglass")
                .foregroundStyle(PoulpyColor.onSurfaceVariant)
                .accessibilityHidden(true)
            TextField(placeholder, text: $query)
                .font(PoulpyFont.body)
                .autocorrectionDisabled()
                .submitLabel(.search)
                .accessibilityLabel(Text(label))
            if !query.isEmpty {
                Button {
                    query = ""
                } label: {
                    Image(systemName: "xmark")
                        .font(.system(size: 13, weight: .bold))
                        .foregroundStyle(PoulpyColor.onSurfaceVariant)
                        .frame(width: 36, height: 36)
                        .background(PoulpyColor.surfaceVariant, in: Circle())
                }
                .buttonStyle(.plain)
                .accessibilityLabel(Text("exercises.clear.search"))
            }
        }
        .padding(.leading, 18)
        .padding(.trailing, 6)
        .frame(height: 48)
        .background(PoulpyColor.surface, in: Capsule())
        .overlay(Capsule().stroke(query.isEmpty ? PoulpyColor.outline : PoulpyColor.primary, lineWidth: 1.5))
    }
}

/// The design-system chip: 36 high, pill; on = `primary`, off = 1.5 px `outline`.
struct PoulpyChip: View {
    let title: Text
    let selected: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            title
                .font(.poulpy(.subheadline).weight(selected ? .heavy : .bold))
                .padding(.horizontal, 16)
                .frame(height: 36)
                .foregroundStyle(selected ? PoulpyColor.onPrimary : PoulpyColor.onSurface)
                .background(selected ? PoulpyColor.primary : PoulpyColor.surface, in: Capsule())
                .overlay(Capsule().stroke(selected ? Color.clear : PoulpyColor.outline, lineWidth: 1.5))
        }
        .buttonStyle(.plain)
        .accessibilityAddTraits(selected ? [.isSelected] : [])
    }
}

/// Lays its children out in rows, wrapping to the next row when one is full, like chips.
struct FlowLayout: Layout {
    var spacing: CGFloat = 8

    func sizeThatFits(proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) -> CGSize {
        let rows = arrange(width: proposal.width ?? .infinity, subviews: subviews)
        let width = rows.map { $0.width }.max() ?? 0
        let height = rows.map { $0.height }.reduce(0, +) + spacing * CGFloat(max(rows.count - 1, 0))
        return CGSize(width: proposal.width ?? width, height: height)
    }

    func placeSubviews(in bounds: CGRect, proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) {
        var y = bounds.minY
        for row in arrange(width: bounds.width, subviews: subviews) {
            var x = bounds.minX
            for index in row.indices {
                let size = subviews[index].sizeThatFits(.unspecified)
                subviews[index].place(at: CGPoint(x: x, y: y), proposal: ProposedViewSize(size))
                x += size.width + spacing
            }
            y += row.height + spacing
        }
    }

    private struct Row {
        var indices: [Int] = []
        var width: CGFloat = 0
        var height: CGFloat = 0
    }

    private func arrange(width: CGFloat, subviews: Subviews) -> [Row] {
        var rows = [Row()]
        for index in subviews.indices {
            let size = subviews[index].sizeThatFits(.unspecified)
            let gap = rows[rows.count - 1].indices.isEmpty ? 0 : spacing
            if rows[rows.count - 1].width + gap + size.width > width, !rows[rows.count - 1].indices.isEmpty {
                rows.append(Row())
            }
            let extra = rows[rows.count - 1].indices.isEmpty ? 0 : spacing
            rows[rows.count - 1].indices.append(index)
            rows[rows.count - 1].width += extra + size.width
            rows[rows.count - 1].height = max(rows[rows.count - 1].height, size.height)
        }
        return rows
    }
}
