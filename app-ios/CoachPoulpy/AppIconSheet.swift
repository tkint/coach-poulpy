import SwiftUI
import Shared

/// The home screen icon is an alternate icon set per `AppIcon` (AppIcon<Id> in the asset catalog, listed in
/// project.yml); the standard one is the primary icon and has no name. The phone keeps which one is shown.
extension AppIcon {
    var alternateName: String? {
        self == AppIcons.shared.standard ? nil : "AppIcon" + id.prefix(1).uppercased() + id.dropFirst()
    }

    /// The drawing the app shows for this face, the same one the icon is made from.
    var drawing: String {
        switch self {
        case .happy: return "PoulpyMoodHappy"
        case .proud: return "PoulpyMoodProud"
        case .annoyed: return "PoulpyMoodAnnoyed"
        case .grumpy: return "PoulpyMoodGrumpy"
        case .champion: return "PoulpyPoseChampion"
        default: return "PoulpyPoseCocky"
        }
    }

    /// "Happy · default" for the standard icon, the plain name for the others.
    var label: String {
        let name = L("appIcon.\(id)")
        return self == AppIcons.shared.standard ? L("appIcon.defaultName %@", name) : name
    }

    /// The icon on the home screen now.
    static var current: AppIcon {
        let name = UIApplication.shared.alternateIconName
        return AppIcons.shared.all.first { $0.alternateName == name } ?? AppIcons.shared.standard
    }

    /// Changes the icon. The system shows its own alert and applies it afterwards.
    func apply() {
        guard UIApplication.shared.supportsAlternateIcons, self != AppIcon.current else { return }
        UIApplication.shared.setAlternateIconName(alternateName)
    }
}

/// Settings › App › App icon: tap a face to apply it and close the sheet.
struct AppIconSheet: View {
    let selected: AppIcon
    let onPick: (AppIcon) -> Void
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 12) {
                    Text("appIcon.description").font(.poulpy(.subheadline)).foregroundStyle(PoulpyColor.onSurfaceVariant)
                    VStack(spacing: 0) {
                        ForEach(AppIcons.shared.all, id: \.id) { icon in
                            if icon != AppIcons.shared.all.first { Divider().overlay(PoulpyColor.outline) }
                            Button {
                                onPick(icon)
                                dismiss()
                            } label: {
                                HStack(spacing: 12) {
                                    Image(icon.drawing).resizable().scaledToFit().frame(width: 48, height: 48).accessibilityHidden(true)
                                    Text(icon.label).font(.poulpy(.body).weight(.bold)).foregroundStyle(PoulpyColor.onSurface)
                                    Spacer()
                                    if icon == selected { Image(systemName: "checkmark").foregroundStyle(PoulpyColor.primary) }
                                }
                                .frame(minHeight: PoulpySize.row)
                                .contentShape(Rectangle())
                            }
                            .buttonStyle(.plain)
                            .accessibilityAddTraits(icon == selected ? .isSelected : [])
                        }
                    }
                    Text("appIcon.tapNote").font(.poulpy(.footnote)).foregroundStyle(PoulpyColor.onSurfaceVariant)
                    Text("appIcon.iosNote").font(.poulpy(.footnote)).foregroundStyle(PoulpyColor.onSurfaceVariant)
                }
                .padding(20)
            }
            .navigationTitle("settings.appIcon")
            .navigationBarTitleDisplayMode(.inline)
            .safeAreaInset(edge: .bottom) {
                Button { dismiss() } label: {
                    Text("action.cancel")
                        .font(.poulpy(.headline).weight(.heavy))
                        .foregroundStyle(PoulpyColor.onSurface)
                        .frame(maxWidth: .infinity, minHeight: 56)
                        .overlay(Capsule().stroke(PoulpyColor.outline, lineWidth: 2))
                }
                .buttonStyle(.plain)
                .padding(20)
            }
        }
        .presentationDetents([.large])
    }
}

/// A localized string with printf-style arguments, as in Localizable.strings.
private func L(_ key: String, _ args: CVarArg...) -> String {
    String(format: NSLocalizedString(key, comment: ""), locale: Locale.current, arguments: args)
}
