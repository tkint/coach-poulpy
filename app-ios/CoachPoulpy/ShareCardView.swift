import SwiftUI
import UniformTypeIdentifiers
import Shared

/// The picture shared after a finished session, drawn at 1080 x 1350 pixels. The lines and their order come from
/// the shared `ShareCard`; this only draws them, laid out like the finished screen and in the colours of the theme in
/// use (`scheme`).
struct ShareCardView: View {
    let card: ShareCard
    let scheme: ColorScheme

    private static let columns = 3

    var body: some View {
        let columns = min(max(card.lines.count, 1), Self.columns)
        VStack(spacing: 0) {
            Image(card.drawing.imageName)
                .resizable()
                .scaledToFit()
                .frame(height: 256)
                .padding(.top, 56)
            Text(card.heading)
                .font(.custom(PoulpyFontFace.baloo2ExtraBold, size: 84))
                .foregroundStyle(PoulpyColor.onBackground)
                .lineLimit(1)
                .minimumScaleFactor(0.5)
                .padding(.horizontal, 88)
                .padding(.top, 8)
            Text(card.quote)
                .font(.custom(PoulpyFontFace.nunitoBold, size: 44))
                .foregroundStyle(PoulpyColor.primary)
                .lineLimit(1)
                .minimumScaleFactor(0.5)
                .padding(.horizontal, 88)
                .padding(.top, 8)
            Text(card.title)
                .font(.custom(PoulpyFontFace.nunitoExtraBold, size: 60))
                .foregroundStyle(PoulpyColor.onBackground)
                .lineLimit(1)
                .minimumScaleFactor(0.5)
                .padding(.horizontal, 88)
                .padding(.top, 12)
            Text(Self.dateLabel(card.startedAtMillis))
                .font(.custom(PoulpyFontFace.nunitoBold, size: 40))
                .foregroundStyle(PoulpyColor.onSurfaceVariant)
                .padding(.top, 4)
            LazyVGrid(columns: Array(repeating: GridItem(.flexible(), spacing: 24), count: columns), spacing: 24) {
                ForEach(Array(card.lines.enumerated()), id: \.offset) { _, line in
                    VStack(alignment: .leading, spacing: 4) {
                        Text(line.label.uppercased())
                            .font(.custom(PoulpyFontFace.nunitoExtraBold, size: 32))
                            .kerning(2)
                            .foregroundStyle(PoulpyColor.onSurfaceVariant)
                            .lineLimit(1)
                            .minimumScaleFactor(0.6)
                        Text(line.value)
                            .font(.custom(PoulpyFontFace.baloo2ExtraBold, size: 64))
                            .foregroundStyle(PoulpyColor.onSurface)
                            .lineLimit(1)
                            .minimumScaleFactor(0.4)
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(EdgeInsets(top: 24, leading: 28, bottom: 24, trailing: 28))
                    .background(PoulpyColor.surface, in: RoundedRectangle(cornerRadius: 40))
                    .overlay(RoundedRectangle(cornerRadius: 40).stroke(PoulpyColor.outline, lineWidth: 3))
                }
            }
            .padding(.horizontal, 88)
            .padding(.top, 40)
            let poulpy = card.poulpy
            HStack(spacing: 36) {
                TentacleRing(lit: Int(poulpy.tentaclesLit), on: PoulpyColor.primary, off: PoulpyColor.outline)
                    .frame(width: 168, height: 168)
                VStack(alignment: .leading, spacing: 8) {
                    Text(poulpy.title).font(.custom(PoulpyFontFace.baloo2ExtraBold, size: 56)).lineLimit(1).minimumScaleFactor(0.6)
                    Text(poulpy.line).font(.custom(PoulpyFontFace.nunitoBold, size: 36)).lineLimit(2)
                }
                .foregroundStyle(PoulpyColor.onPrimaryContainer)
                Spacer(minLength: 0)
            }
            .padding(32)
            .background(PoulpyColor.primaryContainer, in: RoundedRectangle(cornerRadius: 40))
            .padding(.horizontal, 88)
            .padding(.top, 40)
            Spacer(minLength: 0)
            Text(card.appName)
                .font(.custom(PoulpyFontFace.baloo2ExtraBold, size: 52))
                .foregroundStyle(PoulpyColor.primary)
                .padding(.bottom, 44)
        }
        .frame(width: 1080, height: 1350)
        .background(PoulpyColor.background)
        .environment(\.colorScheme, scheme)
    }

    /// "Sat 24 Oct · 18:05", as on the finished screen.
    private static func dateLabel(_ millis: Int64) -> String {
        let date = Date(timeIntervalSince1970: Double(millis) / 1000)
        return "\(date.formatted(.dateTime.weekday(.abbreviated).day().month(.abbreviated))) · \(date.formatted(date: .omitted, time: .shortened))"
    }
}

/// A finished session as the share sheet takes it: the picture is drawn only when a destination is picked.
struct ShareCardItem: Transferable {
    let card: ShareCard
    let scheme: ColorScheme

    static var transferRepresentation: some TransferRepresentation {
        FileRepresentation(exportedContentType: .png) { item in
            let data: Data? = await MainActor.run {
                let renderer = ImageRenderer(content: ShareCardView(card: item.card, scheme: item.scheme))
                renderer.scale = 1
                return renderer.uiImage?.pngData()
            }
            guard let data else { throw CocoaError(.fileWriteUnknown) }
            let folder = FileManager.default.temporaryDirectory.appendingPathComponent(UUID().uuidString, isDirectory: true)
            try FileManager.default.createDirectory(at: folder, withIntermediateDirectories: true)
            let url = folder.appendingPathComponent("coach-poulpy-session.png")
            try data.write(to: url)
            return SentTransferredFile(url)
        }
    }
}
