import SwiftUI
import UniformTypeIdentifiers
import Shared

/// The picture shared after a finished session, drawn at 1080 x 1350 pixels. The lines and their order come from
/// the shared `ShareCard`; this only draws them, on the light colours whatever the theme, since the picture leaves
/// the phone.
struct ShareCardView: View {
    let card: ShareCard

    private static let ink = Color(light: 0x082F4A, dark: 0x082F4A)
    private static let muted = Color(light: 0x374555, dark: 0x374555)
    private static let paper = Color(light: 0xDDF0FB, dark: 0xDDF0FB)
    private static let tile = Color(light: 0xFFFFFF, dark: 0xFFFFFF)

    var body: some View {
        VStack(spacing: 0) {
            Image(card.drawing.imageName)
                .resizable()
                .scaledToFit()
                .frame(height: 256)
                .padding(.top, 56)
            Text(card.title)
                .font(.custom(PoulpyFontFace.baloo2ExtraBold, size: 88))
                .foregroundStyle(Self.ink)
                .lineLimit(1)
                .minimumScaleFactor(0.5)
                .padding(.top, 8)
            Text(Date(timeIntervalSince1970: Double(card.startedAtMillis) / 1000).formatted(.dateTime.weekday(.wide).day().month(.wide)))
                .font(.custom(PoulpyFontFace.nunitoBold, size: 44))
                .foregroundStyle(Self.muted)
                .padding(.top, 4)
            LazyVGrid(columns: [GridItem(.flexible(), spacing: 24), GridItem(.flexible(), spacing: 24)], spacing: 24) {
                ForEach(Array(card.lines.enumerated()), id: \.offset) { _, line in
                    VStack(alignment: .leading, spacing: 4) {
                        Text(line.label.uppercased())
                            .font(.custom(PoulpyFontFace.nunitoBold, size: 36))
                            .kerning(2)
                            .foregroundStyle(Self.muted)
                        Text(line.value)
                            .font(.custom(PoulpyFontFace.baloo2ExtraBold, size: 64))
                            .foregroundStyle(Self.ink)
                            .lineLimit(1)
                            .minimumScaleFactor(0.5)
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(EdgeInsets(top: 24, leading: 36, bottom: 24, trailing: 36))
                    .background(Self.tile, in: RoundedRectangle(cornerRadius: 40))
                }
            }
            .padding(.horizontal, 88)
            .padding(.top, 48)
            Spacer(minLength: 0)
            Text(card.appName)
                .font(.custom(PoulpyFontFace.baloo2ExtraBold, size: 52))
                .foregroundStyle(Self.ink)
                .padding(.bottom, 44)
        }
        .frame(width: 1080, height: 1350)
        .background(Self.paper)
        .environment(\.colorScheme, .light)
    }
}

/// A finished session as the share sheet takes it: the picture is drawn only when a destination is picked.
struct ShareCardItem: Transferable {
    let card: ShareCard

    static var transferRepresentation: some TransferRepresentation {
        FileRepresentation(exportedContentType: .png) { item in
            let data: Data? = await MainActor.run {
                let renderer = ImageRenderer(content: ShareCardView(card: item.card))
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
