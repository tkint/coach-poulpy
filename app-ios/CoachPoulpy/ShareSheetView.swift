import SwiftUI

/// The ways a session or a workout goes out, in a sheet: a title, why, and one row per choice (`ShareFormatRow`).
struct ShareSheetView<Rows: View>: View {
    let title: LocalizedStringKey
    let why: LocalizedStringKey
    @ViewBuilder let rows: () -> Rows

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            VStack(alignment: .leading, spacing: 2) {
                Text(title).font(.poulpy(.title2).weight(.heavy)).accessibilityAddTraits(.isHeader)
                Text(why).font(.poulpy(.subheadline)).foregroundStyle(PoulpyColor.onSurfaceVariant)
            }
            VStack(spacing: 0, content: rows)
                .background(PoulpyColor.surfaceVariant, in: RoundedRectangle(cornerRadius: PoulpyRadius.lg))
        }
        .padding(20)
        .frame(maxWidth: .infinity, alignment: .leading)
        .presentationDetents([.height(260)])
    }
}

/// One row of a `ShareSheetView`: an icon, what it is, and a note on what it does.
struct ShareFormatRow: View {
    let title: LocalizedStringKey
    let note: LocalizedStringKey
    let icon: Lucide

    var body: some View {
        HStack(spacing: 14) {
            LucideIcon(icon).frame(width: 24)
            VStack(alignment: .leading, spacing: 2) {
                Text(title).font(.poulpy(.body).weight(.bold)).foregroundStyle(PoulpyColor.onSurface)
                Text(note).font(.poulpy(.footnote)).foregroundStyle(PoulpyColor.onSurfaceVariant)
            }
            Spacer()
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 10)
        .frame(minHeight: PoulpySize.row)
        .contentShape(Rectangle())
    }
}
