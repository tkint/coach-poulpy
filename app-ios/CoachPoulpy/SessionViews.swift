import SwiftUI
import Shared
import UniformTypeIdentifiers

/// A session as a row: icon, title, time and its numbers. Used by Home's diary.
struct SessionCardView: View {
    let session: SessionRow
    var highlighted = false
    /// In the diary the card is the timeline's: tighter corners and no time of day, since the day heading says when.
    var diary = false
    @Environment(\.unitSystem) private var units

    var body: some View {
        HStack(spacing: 12) {
            LucideIcon(session.kind == .treadmill ? .gauge : .dumbbell, size: 22)
                .foregroundStyle(PoulpyColor.onPrimaryContainer)
                .frame(width: 44, height: 44)
                .background(PoulpyColor.primaryContainer, in: Circle())
            VStack(alignment: .leading, spacing: 2) {
                Text(session.title).font(.poulpy(.headline)).foregroundStyle(PoulpyColor.onSurface)
                Text(diary ? headline(session) : "\(time(session.startedAtMillis)) · \(headline(session))")
                    .font(.poulpy(.subheadline))
                    .monospacedDigit()
                    .foregroundStyle(PoulpyColor.onSurfaceVariant)
            }
            Spacer(minLength: 0)
        }
        .padding(.horizontal, diary ? PoulpySpace.md : 14)
        .padding(.vertical, diary ? PoulpySpace.md : 12)
        .background(PoulpyColor.surface, in: RoundedRectangle(cornerRadius: diary ? PoulpyRadius.md : PoulpyRadius.lg))
        .overlay(RoundedRectangle(cornerRadius: diary ? PoulpyRadius.md : PoulpyRadius.lg).stroke(highlighted ? PoulpyColor.primary : PoulpyColor.outline, lineWidth: highlighted ? 2 : 1))
    }

    /// The numbers that matter most for the row: duration, then distance for the treadmill or sets for a workout.
    private func headline(_ session: SessionRow) -> String {
        var parts = [clock(session.activeSeconds)]
        if let distance = session.distanceMeters { parts.append(Units.shared.distance(meters: distance.doubleValue, system: units, decimals: 2).text) }
        if session.kind == .workout, let done = session.setsDone, let planned = session.setsPlanned {
            parts.append(L("history.sets %lld %lld", Int64(done.intValue), Int64(planned.intValue)))
        }
        return parts.joined(separator: " · ")
    }
}

struct SessionDetailView: View {
    let graph: AppGraph
    let session: SessionRow
    let onDelete: () -> Void
    @Environment(\.dismiss) private var dismiss
    @State private var deleting = false
    @State private var sharing = false
    @State private var saving: FitDocument?
    @State private var recap: SessionRecap?
    private let figure: BodyFigure
    @Environment(\.unitSystem) private var units
    @Environment(\.colorScheme) private var colorScheme

    init(graph: AppGraph, session: SessionRow, onDelete: @escaping () -> Void) {
        self.graph = graph
        self.session = session
        self.onDelete = onDelete
        figure = (graph.profile(memory: UserDefaultsStore()).ui.value as! ProfileUi).bodyFigure ?? .shoulders
    }

    /// Whether the session has muscles worked to draw on a body map.
    private var mapped: Bool { !(recap?.muscles.heat.isEmpty ?? true) }

    private func dateLine(_ recap: SessionRecap) -> String {
        [dateTime(recap.startedAtMillis), recap.formatLabel].compactMap { $0 }.joined(separator: " · ")
    }

    var body: some View {
        Group {
            if let recap, mapped {
                SessionRecapSheetView(
                    recap: recap, dateLine: dateLine(recap), figure: figure,
                    onBack: { dismiss() }, onShare: { sharing = true }, onDelete: { deleting = true }
                )
            } else if let recap {
                SessionRecapView(recap: recap, dateLine: dateLine(recap))
            } else {
                PoulpyColor.background
            }
        }
        .onAppear { recap = graph.sessionRecap(sessionId: session.id, languageCode: Language.code) }
        .navigationBarTitleDisplayMode(.inline)
        // The body map goes under the bar: the sheet carries its own back, share and delete buttons.
        .navigationBarBackButtonHidden(mapped)
        .toolbar(mapped ? .hidden : .automatic, for: .navigationBar)
        .toolbar {
            Button {
                sharing = true
            } label: {
                Label("shareSession.title", lucide: .share2)
            }
            Button(role: .destructive) {
                deleting = true
            } label: {
                Label("action.delete", lucide: .trash)
            }
        }
        .sheet(isPresented: $sharing) {
            if #available(iOS 16.4, *) {
                shareSheet.presentationCornerRadius(24)
            } else {
                shareSheet
            }
        }
        .fileExporter(
            isPresented: Binding(get: { saving != nil }, set: { if !$0 { saving = nil } }),
            document: saving,
            contentType: FitDocument.contentType,
            defaultFilename: saving?.fileName
        ) { _ in saving = nil }
        .confirmationDialog("history.deleteTitle", isPresented: $deleting, titleVisibility: .visible) {
            Button("action.delete", role: .destructive) {
                onDelete()
                dismiss()
            }
        } message: {
            Text("history.deleteBody")
        }
    }

    /// The formats a session goes out in: a .FIT file to save, or an image to share.
    private var shareSheet: some View {
        ShareSheetView(title: "shareSession.title", why: "shareSession.why") {
            Button {
                sharing = false
                saving = graph.sessionFitFile(sessionId: session.id).map { FitDocument($0) }
            } label: {
                ShareFormatRow(title: "shareSession.fit", note: "shareSession.fitNote", icon: .download)
            }
            .buttonStyle(.plain)
            Divider().overlay(PoulpyColor.outline)
            if let card = graph.sessionShareCard(sessionId: session.id, units: units, languageCode: Language.code) {
                ShareLink(item: ShareCardItem(card: card, scheme: colorScheme), preview: SharePreview(session.title)) {
                    ShareFormatRow(title: "shareSession.image", note: "shareSession.imageNote", icon: .image)
                }
                .buttonStyle(.plain)
            }
        }
    }
}

/// A session's .FIT file for the system's save dialog, to keep it in Files on the phone.
private struct FitDocument: FileDocument {
    /// The type a `.fit` file is saved with; plain data when the system knows none.
    static let contentType = UTType(filenameExtension: "fit", conformingTo: .data) ?? .data
    static var readableContentTypes: [UTType] { [contentType] }

    let fileName: String
    let data: Data

    init(_ file: SessionFitFile) {
        let bytes = file.bytes
        fileName = file.fileName
        data = Data((0..<bytes.size).map { UInt8(bitPattern: bytes.get(index: $0)) })
    }

    init(configuration: ReadConfiguration) throws {
        throw CocoaError(.fileReadUnsupportedScheme)
    }

    func fileWrapper(configuration: WriteConfiguration) throws -> FileWrapper {
        FileWrapper(regularFileWithContents: data)
    }
}

/// A localized string with printf-style arguments, as in Localizable.strings.
private func L(_ key: String, _ args: CVarArg...) -> String {
    String(format: NSLocalizedString(key, comment: ""), locale: Locale.current, arguments: args)
}

private func clock(_ seconds: Int64) -> String {
    let h = seconds / 3600, m = (seconds % 3600) / 60, s = seconds % 60
    return h > 0 ? String(format: "%lld:%02lld:%02lld", h, m, s) : String(format: "%lld:%02lld", m, s)
}

private func date(_ millis: Int64) -> Date { Date(timeIntervalSince1970: Double(millis) / 1000) }

private func dateTime(_ millis: Int64) -> String {
    date(millis).formatted(date: .abbreviated, time: .shortened)
}

private func time(_ millis: Int64) -> String {
    date(millis).formatted(date: .omitted, time: .shortened)
}
